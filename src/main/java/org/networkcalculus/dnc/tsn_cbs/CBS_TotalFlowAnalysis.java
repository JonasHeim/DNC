package org.networkcalculus.dnc.tsn_cbs;

import org.networkcalculus.dnc.AnalysisConfig;
import org.networkcalculus.dnc.network.server_graph.ServerGraph;
import org.networkcalculus.dnc.tandem.analyses.TotalFlowResults;

/**
 * Class representation of the latency Total-Flow-Analysis of a server graph
 */
public class CBS_TotalFlowAnalysis {

    /**
     * Server graph used for the analysis
     */
    private final CBS_ServerGraph server_graph;

    /**
     * @param server_graph  Server Graph that will be used for the analysis
     */
    public CBS_TotalFlowAnalysis(CBS_ServerGraph server_graph) {
        this.server_graph = server_graph;
    }

    /**
     * @param flow  The flow to perform the analysis on
     */
    public void performAnalysis(CBS_TokenBucket_Flow flow)
    {
        //ToDo: implement TFA for given flow

        /*
        1. Get all arrival curves
        2. For each server on flow.path
            2.1. Get all arrival curves

         */
    }
}
