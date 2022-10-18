package org.networkcalculus.dnc.tsn_cbs;

import org.networkcalculus.dnc.AnalysisConfig;
import org.networkcalculus.dnc.network.server_graph.ServerGraph;
import org.networkcalculus.dnc.tandem.analyses.TotalFlowResults;

public class CBS_TotalFlowAnalysis {
    private final CBS_ServerGraph server_graph;
    public CBS_TotalFlowAnalysis(CBS_ServerGraph server_graph) {
        this.server_graph = server_graph;
    }

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
