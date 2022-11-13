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

public class Demo_Line_Prio1 {

    public Demo_Line_Prio1() {
    }

    public static void main(String[] args) {
        Demo_Line_Prio1 demo = new Demo_Line_Prio1();

        try {
            demo.run();
        } catch (Exception e) {
        		e.printStackTrace();
        }
    }

    public void run() throws Exception {

        /* First step always */
        ServerGraph sg = new ServerGraph();

        ArrivalCurve ac_flow2 = Curve.getFactory().createTokenBucket(1.6e6, 15744.0);

        /* Create a network of 4 systems in line topology */
        int numServers = 4;
        Server[] servers = new Server[numServers];

        servers[0] = sg.addServer("S1", Curve.getFactory().createRateLatency(10.0e6, 1.33355789e-4), AnalysisConfig.Multiplexing.FIFO);
        servers[1] = sg.addServer("S2", Curve.getFactory().createRateLatency(10.0e6, 1.33355789e-4), AnalysisConfig.Multiplexing.FIFO);
        servers[2] = sg.addServer("S3", Curve.getFactory().createRateLatency(10.0e6, 1.33355789e-4), AnalysisConfig.Multiplexing.FIFO);
        servers[3] = sg.addServer("S4", Curve.getFactory().createRateLatency(10.0e6, 1.33355789e-4), AnalysisConfig.Multiplexing.FIFO);

        /* Define links between server */
        Turn t_1_2 = sg.addTurn("S1 --> S2", servers[0], servers[1]);
        Turn t_2_3 = sg.addTurn("S2 --> S3", servers[1], servers[2]);
        Turn t_3_4 = sg.addTurn("S3 --> S4", servers[2], servers[3]);

        /* Define path for flow 2 */
        LinkedList<Turn> path0 = new LinkedList<Turn>();
        path0.add(t_2_3);
        path0.add(t_3_4);
        sg.addFlow("Flow 2", ac_flow2, path0);


        /* Create analysis */
        CompFFApresets compffa_analyses = new CompFFApresets( sg );
        /* ToDo: the default config calculates weird TFA (and probably also SFA, PMOO and TMA) results.
         * Need to find out whats going on here. With a blank config the results are correct
         * */
        TotalFlowAnalysis tfa = new TotalFlowAnalysis(sg, new AnalysisConfig());
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