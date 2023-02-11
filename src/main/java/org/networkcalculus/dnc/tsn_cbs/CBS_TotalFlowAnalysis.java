package org.networkcalculus.dnc.tsn_cbs;

import org.networkcalculus.dnc.Calculator;
import org.networkcalculus.dnc.curves.ArrivalCurve;
import org.networkcalculus.dnc.curves.Curve;
import org.networkcalculus.dnc.curves.ServiceCurve;
import org.networkcalculus.dnc.network.server_graph.Flow;
import org.networkcalculus.num.Num;

import java.util.*;

/**
 * Class representation of the latency Total-Flow-Analysis of a server graph
 */
public class CBS_TotalFlowAnalysis {

    private static class CBS_ResultsTotalFlowAnalysis {
        /**
         * Maps the server to their local delays for the given flow
         */
        private Map<CBS_Server, Num> serverLocalDelays;

        /**
         *  Summed up total delay over all local delays
         */
        private Num totalDelay;

        /**
         * Current foi used for the analysis
         */
        private CBS_Flow flow;

        /**
         * Constructor only resets analysis configuration
         */
        public CBS_ResultsTotalFlowAnalysis() {
            this.reset();
        }

        /**
         * Add a new local delay for a given server to the total delay.
         * @param server    Server
         * @param delay     Local delay of the server
         */
        public void addServerLocalDelay(CBS_Server server, Num delay) {
            this.serverLocalDelays.put(server, delay);
            this.totalDelay = Num.getUtils(Calculator.getInstance().getNumBackend()).add(this.totalDelay, delay);
        }

        /**
         * @return Total delay of the foi
         */
        public Num getTotalDelay() {
            return this.totalDelay;
        }

        /**
         * @return Local delays at the queues that are traversed by the foi
         */
        public Map<CBS_Server, Num> getServerLocalDelays() {
            return this.serverLocalDelays;
        }

        /**
         * Reset the analysis. Reset the total and all local delays as well as the foi reference
         */
        public void reset() {
            this.totalDelay = Num.getFactory(Calculator.getInstance().getNumBackend()).createZero();
            this.serverLocalDelays = new LinkedHashMap<CBS_Server, Num>();
            this.flow = null;
        }

        /**
         * Set the foi for the analysis
         * @param flow  foi
         */
        public void setFlow(CBS_Flow flow) {
            this.flow = flow;
        }

        /**
         * @return foi
         */
        public CBS_Flow getFlow() {
            return flow;
        }
    }

    /**
     * Configuration options for TFA arrival bounding.
     * TODO: Only deault supported atm. PBOOAB was disabled due to an unresolved error.
     */
    public enum TFA_CONFIG {
        DEFAULT_TFA,
        AGGR_PBOOAB_TFA //DO NOT USE!
    };

    /**
     * TFA configuration.
     * Determines mode of arrival bounding at CBS servers:
     * - DEFAULT_TFA: trivial arrival bounding
     * - AGGR_PBOOAB: aggregated PBOO arrival bounding
     */
    private TFA_CONFIG configuration;

    /**
     * @return Current TFA configuration
     */
    public TFA_CONFIG getConfiguration() {
        return configuration;
    }

    /**
     * Set TFA configuration
     * @param configuration configuration
     */
    public void setConfiguration(TFA_CONFIG configuration) {
        this.configuration = configuration;
    }

    /**
     * Possible shaping types of a server graph
     */
    public enum SHAPING_CONF {
        NO_SHAPING,
        LINK_SHAPING,
        CBS_SHAPING,
        LINK_AND_CBS_SHAPING
    }

    /**
     * Current shaping configuration
     */
    private SHAPING_CONF shaping_config;

    /**
     * Server graph used for the analysis
     */
    private final CBS_ServerGraph server_graph;

    /**
     * Stores the results of a TFA for a last analyzed flow
     */
    private final CBS_ResultsTotalFlowAnalysis results;

    /**
     * @param server_graph  Server Graph that will be used for the analysis
     */
    public CBS_TotalFlowAnalysis(CBS_ServerGraph server_graph, TFA_CONFIG config, SHAPING_CONF shaping_config) {
        this.configuration = config;
        this.shaping_config = shaping_config;
        this.server_graph = server_graph;
        this.results = new CBS_ResultsTotalFlowAnalysis();
    }

    /**
     * @return  Sum of all server local delays on the flows path
     */
    public Num getTotalDelay() {
        return this.results.getTotalDelay();
    }

    /**
     * @return  Mapping of all servers on the flows path and their local delays
     */
    public Map<CBS_Server, Num> getServerLocalDelays() {
        return this.results.getServerLocalDelays();
    }
    
    /**
     * Run the Total-Flow-Analyis of the given flow.
     * Only returns delay bound.
     * TODO: PBOOOAB was removed due to an unresolved error! Only default TFA supported
     * @param flow  The flow to perform the analysis on
     */
    public void performAnalysis(CBS_Flow flow) throws Exception {
        this.results.reset();
        this.results.setFlow(flow);
        LinkedList<CBS_Link> path = this.server_graph.getPath(flow);

        Num delay_bound = Num.getFactory(Calculator.getInstance().getNumBackend()).createZero();
        //ToDo: Eventually calculate backlog bound as well
        //Num backlog_bound = Num.getFactory(Calculator.getInstance().getNumBackend()).createZero();

        /* Calculate local delays for each server on flows path */
        for(CBS_Link link:path) {
            /* Skip initial talker hop. Only link shaping will be applied there when analyzing next hop */
            if(link.getSource().getServerType() == CBS_Server.SRV_TYPE.SWITCH)
            {
                delay_bound = deriveBoundsAtServer(flow, link.getSource(), link);

                //ToDo: Eventually calculate backlog bound as well
                //backlog_bound = Num.getUtils(Calculator.getInstance().getNumBackend()).max(backlog_bound, min_D_B.getSecond());

                this.results.addServerLocalDelay(link.getSource(), delay_bound);
            }
        }
    }

    /**
     * Derive the local NC bounds for a given flow at a server
     * @param flow  foi
     * @param source    server the bound will be calculated at
     * @param out_link  output link at the server
     * @return  Local NC delay bound
     * @throws Exception Only SWITCH server supported,
     */
    private Num deriveBoundsAtServer(CBS_Flow flow, CBS_Server source, CBS_Link out_link) throws Exception
    {
        if(CBS_Server.SRV_TYPE.SWITCH != source.getServerType())
        {
            throw new Exception("Only forwarding (SWITCH) devices supported.");
        }

        Num localDelay = Num.getFactory(Calculator.getInstance().getNumBackend()).createZero();;
        CBS_Queue queue = source.getQueue(flow.getPriority(), out_link);
        if(null == queue) {
            //No matching queue found, something is wrong in server configuration...
            throw new Exception("No matching queue found in TFA analysis for server " + source.getAlias());
        }
        else {
            Set<ArrivalCurve> alphas_server = computeArrivalBounds(flow, queue);

            ArrivalCurve ac = Curve.getFactory().createZeroArrivals();

            /* For TFA we assume that alphas_server has the size 1 because it is the aggregated AC of all flows */
            if(alphas_server.size() != 1)
            {
                throw new Exception("Arrival bounds at server " + source + " not 1");
            }
            else {
                for (ArrivalCurve ac_tmp : alphas_server) {
                    ac = Curve.getUtils().add(ac, ac_tmp);
                }
            }
            ServiceCurve sc = queue.getServiceCurve();

            //ToDo: Make FIFO/ARB configurable
            localDelay = Calculator.getInstance().getDncBackend().getBounds().delayFIFO(ac, sc);
            //Num localDelay = Calculator.getInstance().getDncBackend().getBounds().delayARB(ac, sc);
        }
        return localDelay;
    }

    /**
     * Compute arrival bounds for flow at queue
     * @param flow  foi
     * @param queue queue to bound the arrivals for
     * @return  Set of ACs
     * @throws Exception IdleSlope of the queue must be large enough for bounded ACs
     */
    private Set<ArrivalCurve> computeArrivalBounds(CBS_Flow flow, CBS_Queue queue) throws Exception {
        if(TFA_CONFIG.DEFAULT_TFA == this.configuration)
        {
            HashSet<ArrivalCurve> alphas = new HashSet<ArrivalCurve>();

            /* Re-Calculate aggregated AC over all incoming links */
            ArrivalCurve aggregatedArrival = Curve.getFactory().createZeroArrivals();
            for(CBS_Link prec_link:queue.getInputLinks())
            {
                ArrivalCurve alpha = Curve.getFactory().createZeroArrivals();
                CBS_Server prec_server = prec_link.getSource();

                if(CBS_Server.SRV_TYPE.TALKER == prec_server.getServerType())
                {
                    /* Just take the initial AC of the flow at the Talker. */
                    alpha = flow.getArrivalCurve();

                    if((SHAPING_CONF.LINK_SHAPING == this.shaping_config) || (SHAPING_CONF.LINK_AND_CBS_SHAPING == this.shaping_config))
                    {
                        /* Create link shaping curve manually here because we can no calculate it from the talker 'queue' */
                        ArrivalCurve linkShapingCurve = Curve.getFactory().createTokenBucket(prec_link.getCapacity(), prec_link.getMaxPacketSize());

                        /* Apply link shaping on initial arrival curve from talker */
                        alpha = Curve.getUtils().min(alpha, linkShapingCurve);
                    }
                }
                else {
                    /* Build aggregated AC of preceding queue over all traversing flows at that queue */
                    for(CBS_Flow flowCandidate:queue.getFlows())
                    {
                        ArrivalCurve arrivalBound = this.server_graph.calculateAcOfFlowAtQueue(flowCandidate, queue);
                        alpha = Curve.getUtils().add(alpha, arrivalBound);
                    }

                    /* Verify that idleSlope condition holds for the aggregate AC rate */
                    if(queue.getIdleSlope() < alpha.getUltAffineRate().doubleValue())
                    {
                        throw new Exception("IdleSlope of queue is smaller than rate of aggregate AC.");
                    }

                    /* Apply CBS shaping if configured */
                    if((SHAPING_CONF.CBS_SHAPING == this.shaping_config) || (SHAPING_CONF.LINK_AND_CBS_SHAPING == this.shaping_config))
                    {
                        alpha = Curve.getUtils().min(alpha, queue.getCbsShapingCurve());
                    }

                    /* Apply Link shaping if configured */
                    if((SHAPING_CONF.LINK_SHAPING == this.shaping_config) || (SHAPING_CONF.LINK_AND_CBS_SHAPING == this.shaping_config))
                    {
                        alpha = Curve.getUtils().min(alpha, queue.getLinkShapingCurve());
                    }
                }

                /* Finally add preceding link arrival curve to queue arrival curve */
                aggregatedArrival = Curve.getUtils().add(aggregatedArrival, alpha);
            }

            alphas.add(aggregatedArrival);
            return alphas;
        }
        else
        {
            //ToDo: Fix when PBOOAB computation is resolved
            throw new Exception("Only TFA_CONFIG.DEFAULT_TFA implemented for now!");
        }
    }

    /**
     * @return String representation of the TFA
     */
    public String toString() {
        StringBuffer cbs_tfa_str = new StringBuffer();

        cbs_tfa_str.append("\r\nCBS TFA results for flow " + this.results.getFlow().getAlias() + ":\r\n");
        cbs_tfa_str.append("Total delay : " + this.getTotalDelay().doubleValue() + "s \r\n");
        Map<CBS_Server, Num> localDelay = this.results.getServerLocalDelays();
        for(CBS_Server server:localDelay.keySet()) {
            cbs_tfa_str.append("\tDelay @ " + server.getAlias() + " : " + localDelay.get(server).doubleValue() + "s \r\n");
        }
        return cbs_tfa_str.toString();
    }
}

