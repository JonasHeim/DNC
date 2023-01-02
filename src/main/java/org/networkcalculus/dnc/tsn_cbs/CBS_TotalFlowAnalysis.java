package org.networkcalculus.dnc.tsn_cbs;

import org.apache.commons.math3.util.Pair;
import org.networkcalculus.dnc.Calculator;
import org.networkcalculus.dnc.curves.ArrivalCurve;
import org.networkcalculus.dnc.curves.ServiceCurve;
import org.networkcalculus.dnc.feedforward.ArrivalBoundDispatch;
import org.networkcalculus.dnc.tandem.analyses.TotalFlowResults;
import org.networkcalculus.num.Num;
import org.networkcalculus.num.values.NaN;

import java.util.*;

/**
 * Class representation of the latency Total-Flow-Analysis of a server graph
 */
public class CBS_TotalFlowAnalysis {
    private static class CBS_ResultsTotalFlowAnalysis {
        private Map<CBS_Server, Num> serverLocalDelays;

        private Num totalDelay;

        private CBS_Flow flow;

        public CBS_ResultsTotalFlowAnalysis() {
            this.reset();
        }

        public void addServerLocalDelay(CBS_Server server, Num delay) {
            this.serverLocalDelays.put(server, delay);
            this.totalDelay = Num.getUtils(Calculator.getInstance().getNumBackend()).add(this.totalDelay, delay);
        }

        public Num getTotalDelay() {
            return this.totalDelay;
        }

        public Map<CBS_Server, Num> getServerLocalDelays() {
            return this.serverLocalDelays;
        }

        public void reset() {
            this.totalDelay = Num.getFactory(Calculator.getInstance().getNumBackend()).createZero();
            this.serverLocalDelays = new LinkedHashMap<CBS_Server, Num>();
            this.flow = null;
        }

        public void setFlow(CBS_Flow flow) {
            this.flow = flow;
        }

        public CBS_Flow getFlow() {
            return flow;
        }
    }

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
    public CBS_TotalFlowAnalysis(CBS_ServerGraph server_graph) {
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
     * Run the Total-Flow-Analyis + PBOO Arrival Bounding of the given flow.
     * @param flow  The flow to perform the analysis on
     */
    //ToDo: Rework exception handling
    public void performAnalysis(CBS_Flow flow) throws Exception {
        this.results.reset();
        this.results.setFlow(flow);
        LinkedList<CBS_Link> path = this.server_graph.getPath(flow);

        Num delay_bound = Num.getFactory(Calculator.getInstance().getNumBackend()).createZero();
        Num backlog_bound = Num.getFactory(Calculator.getInstance().getNumBackend()).createZero();

        /* Calculate local delays for each server on flows path */
        for(CBS_Link link:path) {
            if(link.getSource().getServerType() == CBS_Server.SRV_TYPE.SWITCH)
            {
                delay_bound = deriveBoundsAtServer(flow, link.getSource(), link);

                //backlog_bound = Num.getUtils(Calculator.getInstance().getNumBackend()).max(backlog_bound, min_D_B.getSecond());

                this.results.addServerLocalDelay(link.getSource(), delay_bound);
            }
            //else skip
        }
    }

    private Num deriveBoundsAtServer(CBS_Flow flow, CBS_Server source, CBS_Link link) throws Exception
    {
        Num localDelay = Num.getFactory(Calculator.getInstance().getNumBackend()).createZero();;
        CBS_Queue queue = source.getQueue(flow.getPriority(), link);
        if(null == queue) {
            //No matching queue found, something is wrong in server configuration...
            throw new Exception("No matching queue found in TFA analysis for server " + source.getAlias());
        }
        else {

            //ToDo: PBOOAB computation
            Set<ArrivalCurve> alphas_server = computeArrivalBounds(flow, source);



            ArrivalCurve ac = queue.getAggregateArrivalCurve();
            ServiceCurve sc = queue.getServiceCurve();

//            /* Get CrossFlows and Calculate LeftOverServiceCurve on that specific queue */
//            Set<CBS_Flow> crossFlows = this.server_graph.getCrossFlowsAtServer(flow, link.getSource());
//            for(CBS_Flow cFlow:crossFlows)
//            {
//                /* Get AC of crossflow */
//                ArrivalCurve singleAC = queue.getArrivalCurveOfFlow(cFlow);
//
//                /* Subtract AC from SC */
//                if(null != singleAC)
//                {
//                    sc = Calculator.getInstance().getDncBackend().getCurveUtils().sub(sc, singleAC);
//                    //ToDo: check if SC >= 0? -> infeasible then
//                }
//            }

            //ToDo: Make FIFO/ARB configurable
            localDelay = Calculator.getInstance().getDncBackend().getBounds().delayFIFO(ac, sc);
            //Num localDelay = Calculator.getInstance().getDncBackend().getBounds().delayARB(ac, sc);
        }
        return localDelay;
    }

    private Set<ArrivalCurve> computeArrivalBounds(CBS_Flow flow, CBS_Server source) {
        //ToDo: implement PBOOAB
        return null;
    }

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

