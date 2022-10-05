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

package org.networkcalculus.dnc.demos;

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

public class Demo_DoubleStar {

    public Demo_DoubleStar() {
    }

    public static void main(String[] args) {
        Demo_DoubleStar demo = new Demo_DoubleStar();

        try {
            demo.run();
        } catch (Exception e) {
        		e.printStackTrace();
        }
    }

    public void run() throws Exception {

        /* First step always */
        ServerGraph sg = new ServerGraph();

        /* AC Flow 1 with rate of 8MBit/s and burst of 1.5kBit */
        ArrivalCurve ac_flow1 = Curve.getFactory().createTokenBucket(8.0e6, 1.5e3);

        /* AC Flow 2 with rate of 8MBit/s and burst of 1.5kBit */
        ArrivalCurve ac_flow2 = Curve.getFactory().createTokenBucket(8.0e6, 1.5e3);

        /* AC Flow 3 with rate of 8MBit/s and burst of 1.5kBit */
        ArrivalCurve ac_flow3 = Curve.getFactory().createTokenBucket(8.0e6, 1.5e3);

        /* AC Flow 4 with rate of 20MBit/s and burst of 3kBit */
        ArrivalCurve ac_flow4 = Curve.getFactory().createTokenBucket(20.0e6, 3.0e3);

        /* AC Flow 5 with rate of 20MBit/s and burst of 3kBit */
        ArrivalCurve ac_flow5 = Curve.getFactory().createTokenBucket(20.0e6, 3.0e3);

        /* Create a network of 9 systems in line topology */
        int numServers = 9;
        Server[] servers = new Server[numServers];

        /* FIFO Server 1 - 5 & 8,9 with rate of 50MBit/s and Latency of 20us */
        servers[0] = sg.addServer("S1", Curve.getFactory().createRateLatency(50.0e6, 20.0e-6), AnalysisConfig.Multiplexing.FIFO);
        servers[1] = sg.addServer("S2", Curve.getFactory().createRateLatency(50.0e6, 20.0e-6), AnalysisConfig.Multiplexing.FIFO);
        servers[2] = sg.addServer("S3", Curve.getFactory().createRateLatency(50.0e6, 20.0e-6), AnalysisConfig.Multiplexing.FIFO);
        servers[3] = sg.addServer("S4", Curve.getFactory().createRateLatency(50.0e6, 20.0e-6), AnalysisConfig.Multiplexing.FIFO);
        servers[4] = sg.addServer("S5", Curve.getFactory().createRateLatency(50.0e6, 20.0e-6), AnalysisConfig.Multiplexing.FIFO);
        servers[7] = sg.addServer("S8", Curve.getFactory().createRateLatency(50.0e6, 20.0e-6), AnalysisConfig.Multiplexing.FIFO);
        servers[8] = sg.addServer("S9", Curve.getFactory().createRateLatency(50.0e6, 20.0e-6), AnalysisConfig.Multiplexing.FIFO);

        /* FIFO Server 6 and 7 (center) with rate of 100MBit/s and Latency of 200us */
        servers[5] = sg.addServer("S6", Curve.getFactory().createRateLatency(100.0e6, 200.0e-6), AnalysisConfig.Multiplexing.FIFO);
        servers[6] = sg.addServer("S7", Curve.getFactory().createRateLatency(100.0e6, 200.0e-6), AnalysisConfig.Multiplexing.FIFO);

        /* Define links between server */
        Turn t_1_6 = sg.addTurn("S1 --> S6", servers[0], servers[5]);
        Turn t_2_6 = sg.addTurn("S2 --> S6", servers[1], servers[5]);
        Turn t_3_6 = sg.addTurn("S3 --> S6", servers[2], servers[5]);
        Turn t_4_6 = sg.addTurn("S4 --> S6", servers[3], servers[5]);
        Turn t_5_6 = sg.addTurn("S5 --> S6", servers[4], servers[5]);
        Turn t_6_7 = sg.addTurn("S6 --> S7", servers[5], servers[6]);
        Turn t_7_8 = sg.addTurn("S7 --> S8", servers[6], servers[7]);
        Turn t_7_9 = sg.addTurn("S7 --> S9", servers[6], servers[8]);

        /* Define path for flow 1 */
        LinkedList<Turn> path0 = new LinkedList<Turn>();
        path0.add(t_3_6);
        path0.add(t_6_7);
        path0.add(t_7_8);
        sg.addFlow("Flow 1", ac_flow1, path0);

        /* Define path for flow 2 */
        LinkedList<Turn> path1 = new LinkedList<Turn>();
        path1.add(t_4_6);
        path1.add(t_6_7);
        path1.add(t_7_8);
        sg.addFlow("Flow 2", ac_flow2, path1);

        /* Define path for flow 3 */
        LinkedList<Turn> path2 = new LinkedList<Turn>();
        path2.add(t_5_6);
        path2.add(t_6_7);
        path2.add(t_7_8);
        sg.addFlow("Flow 3", ac_flow3, path2);

        /* Define path for flow 4 */
        LinkedList<Turn> path3 = new LinkedList<Turn>();
        path3.add(t_1_6);
        path3.add(t_6_7);
        path3.add(t_7_9);
        sg.addFlow("Flow 4", ac_flow4, path3);

        /* Define path for flow 5 */
        LinkedList<Turn> path4 = new LinkedList<Turn>();
        path4.add(t_2_6);
        path4.add(t_6_7);
        path4.add(t_7_9);
        sg.addFlow("Flow 5", ac_flow5, path4);

        /* Create analysis */
        CompFFApresets compffa_analyses = new CompFFApresets( sg );
        TotalFlowAnalysis tfa = compffa_analyses.tf_analysis;
        SeparateFlowAnalysis sfa = compffa_analyses.sf_analysis;
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
                System.out.println("backlog bound   : " + tfa.getBacklogBound());
                System.out.println("     per server : " + tfa.getServerBacklogBoundMapString());
                System.out.println("alpha per server: " + tfa.getServerAlphasMapString());
            } catch (Exception e) {
                System.out.println("TFA analysis failed");
        			e.printStackTrace();
            }

            System.out.println();

            // SFA
            System.out.println("--- Separated Flow Analysis ---");
            try {
                sfa.performAnalysis(flow_of_interest);
                System.out.println("e2e SFA SCs     : " + sfa.getLeftOverServiceCurves());
                System.out.println("     per server : " + sfa.getServerLeftOverBetasMapString());
                System.out.println("xtx per server  : " + sfa.getServerAlphasMapString());
                System.out.println("delay bound     : " + sfa.getDelayBound());
                System.out.println("backlog bound   : " + sfa.getBacklogBound());
            } catch (Exception e) {
                System.out.println("SFA analysis failed");
        			e.printStackTrace();
            }

            System.out.println();

            // PMOO
            System.out.println("--- PMOO Analysis ---");
            try {
                pmoo.performAnalysis(flow_of_interest);
                System.out.println("e2e PMOO SCs    : " + pmoo.getLeftOverServiceCurves());
                System.out.println("xtx per server  : " + pmoo.getServerAlphasMapString());
                System.out.println("delay bound     : " + pmoo.getDelayBound());
                System.out.println("backlog bound   : " + pmoo.getBacklogBound());
            } catch (Exception e) {
                System.out.println("PMOO analysis failed");
                e.printStackTrace();
            }
            
            System.out.println();
            
            // TMA
            System.out.println("--- Tandem Matching Analysis ---");
            try {
             tma.performAnalysis(flow_of_interest);
                System.out.println("e2e TMA SCs     : " + tma.getLeftOverServiceCurves());
                System.out.println("xtx per server  : " + tma.getServerAlphasMapString());
                System.out.println("delay bound     : " + tma.getDelayBound());
                System.out.println("backlog bound   : " + tma.getBacklogBound());
            } catch (Exception e) {
                System.out.println("TMA analysis failed");
                e.printStackTrace();
            }
            
            System.out.println();
            System.out.println();
        }
    }
}