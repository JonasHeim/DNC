/*
 * This file is part of the Deterministic Network Calculator (DNC).
 *
 * Copyright (C) 2011 - 2018 Steffen Bondorf
 * Copyright (C) 2017 - 2018 The DiscoDNC contributors
 * Copyright (C) 2019+ The DNC contributors
 *
 * http://networkcalculus.org
 *
 *
 * The Deterministic Network Calculator (DNC) is free software;
 * you can redistribute it and/or modify it under the terms of the 
 * GNU Lesser General Public License as published by the Free Software Foundation; 
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */

package org.networkcalculus.dnc.demos.Eval_CBSLine;

import org.networkcalculus.dnc.AnalysisConfig;
import org.networkcalculus.dnc.CompFFApresets;
import org.networkcalculus.dnc.curves.ArrivalCurve;
import org.networkcalculus.dnc.curves.Curve;
import org.networkcalculus.dnc.network.server_graph.Flow;
import org.networkcalculus.dnc.network.server_graph.Server;
import org.networkcalculus.dnc.network.server_graph.ServerGraph;
import org.networkcalculus.dnc.network.server_graph.Turn;
import org.networkcalculus.dnc.tandem.analyses.PmooAnalysis;
import org.networkcalculus.dnc.tandem.analyses.SeparateFlowAnalysis;
import org.networkcalculus.dnc.tandem.analyses.TandemMatchingAnalysis;
import org.networkcalculus.dnc.tandem.analyses.TotalFlowAnalysis;

import java.util.LinkedList;

public class Eval_Line_Prio0 {

    public Eval_Line_Prio0() {
    }

    public static void main(String[] args) {
        Eval_Line_Prio0 demo = new Eval_Line_Prio0();

        try {
            demo.run();
        } catch (Exception e) {
        		e.printStackTrace();
        }
    }

    public void run() throws Exception {

        /******************************************************
         *************** Server graph setup *******************
         ******************************************************/
        ServerGraph sg = new ServerGraph();

        /****************** Definition of flows ***************/

        ArrivalCurve ac_flow1 = Curve.getFactory().createTokenBucket(9.984E7, 19.968000000000572);
        ArrivalCurve ac_flow2 = Curve.getFactory().createTokenBucket(9.984E7, 19.968000000000572);
        ArrivalCurve ac_flow3 = Curve.getFactory().createTokenBucket(9.984E7, 19.968000000000572);
        ArrivalCurve ac_flow4 = Curve.getFactory().createTokenBucket(9.984E7, 19.968000000000572);
        ArrivalCurve ac_flow5 = Curve.getFactory().createTokenBucket(9.984E7, 19.968000000000572);

        /****************** Definition of servers ***************/

        int numServers = 11;
        Server[] servers = new Server[numServers];

        servers[0] = sg.addServer("S1.1 to S2", Curve.getFactory().createRateLatency(2.5E7, 1.2336E-4), AnalysisConfig.Multiplexing.FIFO);
        servers[1] = sg.addServer("S2.1 to S3", Curve.getFactory().createRateLatency(2.5E7, 1.2336E-4), AnalysisConfig.Multiplexing.FIFO);
        servers[2] = sg.addServer("S2.1 to L2", Curve.getFactory().createRateLatency(2.5E7, 1.2336E-4), AnalysisConfig.Multiplexing.FIFO);
        servers[3] = sg.addServer("S3.1 to S4", Curve.getFactory().createRateLatency(2.5E7, 1.2336E-4), AnalysisConfig.Multiplexing.FIFO);
        servers[4] = sg.addServer("S3.1 to L3", Curve.getFactory().createRateLatency(2.5E7, 1.2336E-4), AnalysisConfig.Multiplexing.FIFO);
        servers[5] = sg.addServer("S4.1 to S5", Curve.getFactory().createRateLatency(2.5E7, 1.2336E-4), AnalysisConfig.Multiplexing.FIFO);
        servers[6] = sg.addServer("S4.1 to L4", Curve.getFactory().createRateLatency(2.5E7, 1.2336E-4), AnalysisConfig.Multiplexing.FIFO);
        servers[7] = sg.addServer("S5.1 to S6", Curve.getFactory().createRateLatency(2.5E7, 1.2336E-4), AnalysisConfig.Multiplexing.FIFO);
        servers[8] = sg.addServer("S5.1 to L5", Curve.getFactory().createRateLatency(2.5E7, 1.2336E-4), AnalysisConfig.Multiplexing.FIFO);
        servers[9] = sg.addServer("S6.1 to L1", Curve.getFactory().createRateLatency(2.5E7, 1.2336E-4), AnalysisConfig.Multiplexing.FIFO);
        servers[10] = sg.addServer("S6.1 to L6", Curve.getFactory().createRateLatency(2.5E7, 1.2336E-4), AnalysisConfig.Multiplexing.FIFO);

        /****************** Definition of links ***************/

        /* Define links between server */
        Turn t_S1_S2_to_S2_L2 = sg.addTurn("S1_S2 --> S2_L2", servers[0], servers[2]);
        Turn t_S2_S3_to_S3_L3 = sg.addTurn("S2_S3 --> S3_L3", servers[1], servers[4]);
        Turn t_S3_S4_to_S4_L4 = sg.addTurn("S3_S4 --> S4_L4", servers[3], servers[6]);
        Turn t_S4_S5_to_S5_L5 = sg.addTurn("S4_S5 --> S5_L5", servers[5], servers[8]);
        Turn t_S5_S6_to_S6_L6 = sg.addTurn("S5_S6 --> S6_L6", servers[7], servers[10]);

        /***************** Definition of paths ****************/

        /* Define path for flow 1 */
        LinkedList<Turn> path1 = new LinkedList<Turn>();
        path1.add(t_S1_S2_to_S2_L2);

        /* Define path for flow 2 */
        LinkedList<Turn> path2 = new LinkedList<Turn>();
        path2.add(t_S2_S3_to_S3_L3);

        /* Define path for flow 3 */
        LinkedList<Turn> path3 = new LinkedList<Turn>();
        path3.add(t_S3_S4_to_S4_L4);

        /* Define path for flow 4 */
        LinkedList<Turn> path4 = new LinkedList<Turn>();
        path4.add(t_S4_S5_to_S5_L5);

        /* Define path for flow 5 */
        LinkedList<Turn> path5 = new LinkedList<Turn>();
        path5.add(t_S5_S6_to_S6_L6);

        /************** Addition of flows to server graph. *************/
        sg.addFlow("Flow 1", ac_flow1, path1);
        sg.addFlow("Flow 2", ac_flow2, path2);
        sg.addFlow("Flow 3", ac_flow3, path3);
        sg.addFlow("Flow 4", ac_flow4, path4);
        sg.addFlow("Flow 5", ac_flow5, path5);

        /******************************************************
         ***************** Apply analysis *********************
         ******************************************************/

        /* Create analysis */
        CompFFApresets compffa_analyses = new CompFFApresets( sg );
        TotalFlowAnalysis tfa = compffa_analyses.tf_analysis;   //enforced FIFO multiplexing
        SeparateFlowAnalysis sfa = compffa_analyses.sf_analysis;//enforced FIFO multiplexing
        PmooAnalysis pmoo = compffa_analyses.pmoo_analysis;
        TandemMatchingAnalysis tma = compffa_analyses.tandem_matching_analysis;

        for (Flow flow_of_interest : sg.getFlows()) {

            System.out.println("Flow of interest : " + flow_of_interest.toString());
            System.out.println();

            // Analyze the server graph
            // TFA
            System.out.println("--- Total Flow Analysis ---");
            try {
                tfa.performAnalysis(flow_of_interest);
                System.out.println("delay bound     : " + tfa.getDelayBound());
                System.out.println("     per server : " + tfa.getServerDelayBoundMapString());
                //System.out.println("backlog bound   : " + tfa.getBacklogBound());
                //System.out.println("     per server : " + tfa.getServerBacklogBoundMapString());
                //System.out.println("alpha per server: " + tfa.getServerAlphasMapString());
            } catch (Exception e) {
                System.out.println("TFA analysis failed");
                e.printStackTrace();
            }

            System.out.println();

            // SFA
            System.out.println("--- Separated Flow Analysis ---");
            try {
                sfa.performAnalysis(flow_of_interest);
                System.out.println("delay bound     : " + sfa.getDelayBound());
                System.out.println("e2e SFA SCs     : " + sfa.getLeftOverServiceCurves());
                System.out.println("     per server : " + sfa.getServerLeftOverBetasMapString());
                //System.out.println("xtx per server  : " + sfa.getServerAlphasMapString());
                //System.out.println("backlog bound   : " + sfa.getBacklogBound());
            } catch (Exception e) {
                System.out.println("SFA analysis failed");
                e.printStackTrace();
            }

            System.out.println();

            // PMOO
            System.out.println("--- PMOO Analysis ---");
            try {
                pmoo.performAnalysis(flow_of_interest);
                System.out.println("delay bound     : " + pmoo.getDelayBound());
                System.out.println("e2e PMOO SCs    : " + pmoo.getLeftOverServiceCurves());
                //System.out.println("xtx per server  : " + pmoo.getServerAlphasMapString());
                //System.out.println("backlog bound   : " + pmoo.getBacklogBound());
            } catch (Exception e) {
                System.out.println("PMOO analysis failed");
                e.printStackTrace();
            }

            System.out.println();

            // TMA
            System.out.println("--- Tandem Matching Analysis ---");
            try {
                tma.performAnalysis(flow_of_interest);
                System.out.println("delay bound     : " + tma.getDelayBound());
                System.out.println("e2e TMA SCs     : " + tma.getLeftOverServiceCurves());
                //System.out.println("xtx per server  : " + tma.getServerAlphasMapString());
                //System.out.println("backlog bound   : " + tma.getBacklogBound());
            } catch (Exception e) {
                System.out.println("TMA analysis failed");
                e.printStackTrace();
            }

            System.out.println();
            System.out.println();

        }
    }
}