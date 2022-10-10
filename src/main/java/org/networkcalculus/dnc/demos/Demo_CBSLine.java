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

public class Demo_CBSLine {

    public Demo_CBSLine() {
    }

    public static void main(String[] args) {
        Demo_CBSLine demo = new Demo_CBSLine();

        try {
            demo.run();
        } catch (Exception e) {
        		e.printStackTrace();
        }
    }

    public void run() throws Exception {

        /* First step always */
        ServerGraph sg = new ServerGraph();

        /* Set up CBS TSpec */


        /* AC Flow 1 with rate of 8MBit/s and burst of 1.5kBit */
        ArrivalCurve ac_flow1 = Curve.getFactory().createTokenBucket(8.0e6, 1.5e3);

        /* AC Flow 2 with rate of 8MBit/s and burst of 1.5kBit */
        ArrivalCurve ac_flow2 = Curve.getFactory().createTokenBucket(8.0e6, 1.5e3);

        /* AC Flow 3 with rate of 20MBit/s and burst of 3kBit */
        ArrivalCurve ac_flow3 = Curve.getFactory().createTokenBucket(20.0e6, 3.0e3);

        /* Create a network of 5 systems in line topology */
        int numServers = 4;
        Server[] servers = new Server[numServers];

        /* FIFO Server 1 and 2 with rate of 50MBit/s and Latency of 20us */
        servers[0] = sg.addServer("S1", Curve.getFactory().createRateLatency(50.0e6, 20.0e-6), AnalysisConfig.Multiplexing.FIFO);
        servers[1] = sg.addServer("S2", Curve.getFactory().createRateLatency(50.0e6, 20.0e-6), AnalysisConfig.Multiplexing.FIFO);

        /* FIFO Server 3 and 4 with rate of 80MBit/s and Latency of 50us */
        servers[2] = sg.addServer("S3", Curve.getFactory().createRateLatency(80.0e6, 50.0e-6), AnalysisConfig.Multiplexing.FIFO);
        servers[3] = sg.addServer("S4", Curve.getFactory().createRateLatency(80.0e6, 50.0e-6), AnalysisConfig.Multiplexing.FIFO);

        /* Define links between server */
        Turn t_1_2 = sg.addTurn("S1 --> S2", servers[0], servers[1]);
        Turn t_2_3 = sg.addTurn("S2 --> S3",servers[1], servers[2]);
        Turn t_3_4 = sg.addTurn("S3 --> S4",servers[2], servers[3]);

        /* Define path for flow 1 */
        LinkedList<Turn> path0 = new LinkedList<Turn>();
        path0.add(t_1_2);
        path0.add(t_2_3);
        path0.add(t_3_4);
        sg.addFlow("Flow 1", ac_flow1, path0);

        /* Define path for flow 2 */
        LinkedList<Turn> path1 = new LinkedList<Turn>();
        path1.add(t_2_3);
        path1.add(t_3_4);
        sg.addFlow("Flow 2", ac_flow2, path1);

        /* Define path for flow 3 */
        LinkedList<Turn> path2 = new LinkedList<Turn>();
        path2.add(t_3_4);
        sg.addFlow("Flow 3", ac_flow3, path2);

        /* Do CBS TFA analysis */



    }


}