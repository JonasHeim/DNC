package org.networkcalculus.dnc.tsn_cbs;

import org.networkcalculus.dnc.Calculator;
import org.networkcalculus.dnc.curves.ArrivalCurve;
import org.networkcalculus.dnc.curves.ServiceCurve;
import org.networkcalculus.num.Num;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Class representation of the latency Total-Flow-Analysis of a server graph
 */
public class CBS_TotalFlowAnalysis {
    private static class CBS_ResultsTotalFlowAnalysis {
        private Map<CBS_Server, Double> serverLocalDelays;

        private double totalDelay;

        private CBS_Flow flow;

        public CBS_ResultsTotalFlowAnalysis() {
            this.reset();
        }

        public void addServerLocalDelay(CBS_Server server, double delay) {
            this.serverLocalDelays.put(server, delay);
            this.totalDelay += delay;
        }

        public double getTotalDelay() {
            return this.totalDelay;
        }

        public Map<CBS_Server, Double> getServerLocalDelays() {
            return this.serverLocalDelays;
        }

        public void reset() {
            this.totalDelay = 0.0;
            this.serverLocalDelays = new LinkedHashMap<CBS_Server, Double>();
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
    public double getTotalDelay() {
        return this.results.getTotalDelay();
    }

    /**
     * @return  Mapping of all servers on the flows path and their local delays
     */
    public Map<CBS_Server, Double> getServerLocalDelays() {
        return this.results.getServerLocalDelays();
    }
    
    /**
     * Run the Total-Flow-Analyis of the given flow.
     * @param flow  The flow to perform the analysis on
     */
    //ToDo: Rework exception handling
    public void performAnalysis(CBS_Flow flow) throws Exception
    {
        this.results.reset();
        this.results.setFlow(flow);
        LinkedList<CBS_Link> path = this.server_graph.getPath(flow);

        for(CBS_Link link:path) {
            if(link.getSource().getServerType() == CBS_Server.SRV_TYPE.SWITCH)
            {
                CBS_Queue queue = link.getSource().getQueue(flow.getPriority(), link);
                if(null == queue) {
                    //No matching queue found, something is wrong in server configuration...
                    throw new Exception("No matching queue found in TFA analysis for server " + link.getSource().getAlias());
                }
                else {
                    ArrivalCurve ac = queue.getAggregateArrivalCurve();
                    ServiceCurve sc = queue.getServiceCurve();
                    //ToDo: assume FIFO per micro flow property
                    Num localDelay = Calculator.getInstance().getDncBackend().getBounds().delayFIFO(ac, sc);
                    this.results.addServerLocalDelay(link.getSource(), localDelay.doubleValue());
                }
            }
            //else skip
        }
    }

    public String toString() {
        StringBuffer cbs_tfa_str = new StringBuffer();

        cbs_tfa_str.append("\r\nCBS TFA results for flow " + this.results.getFlow().getAlias() + ":\r\n");
        Map<CBS_Server, Double> localDelay = this.results.getServerLocalDelays();
        for(CBS_Server server:localDelay.keySet()) {
            cbs_tfa_str.append("Delay @ " + server.getAlias() + " : " + new BigDecimal(localDelay.get(server)) + "s \r\n");
        }
        cbs_tfa_str.append("Total delay : " + new BigDecimal(this.getTotalDelay()) + "s \r\n");
        return cbs_tfa_str.toString();
    }
}

