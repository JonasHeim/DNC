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
import org.networkcalculus.dnc.tsn_cbs.*;

import java.util.LinkedList;

public class Demo_CBSTree {

    public Demo_CBSTree() {
    }

    public static void main(String[] args) {
        Demo_CBSTree demo = new Demo_CBSTree();

        try {
            demo.run();
        } catch (Exception e) {
        		e.printStackTrace();
        }
    }

    public void run() throws Exception {

        /* First step always */
        CBS_ServerGraph sg = new CBS_ServerGraph("CBS shaped tree network");

        CBS_Flow flow1 = new CBS_Flow("flow1", 1.0e-3, 512, 1, 0, CBS_Flow.Periodicity.PERIODIC);
        CBS_Flow flow2 = new CBS_Flow("flow2", 1.0e-3, 512, 1, 1, CBS_Flow.Periodicity.PERIODIC);
        CBS_Flow flow3 = new CBS_Flow("flow3", 1.0e-3, 512, 1, 2, CBS_Flow.Periodicity.PERIODIC);

//        /* AC Flow 1 with rate of 30MBit/s and burst of 10kBit */
//        ArrivalCurve ac_flow1 = Curve.getFactory().createTokenBucket(30.0e6, 10.0e3);
//
//        /* AC Flow 2 with rate of 15MBit/s and burst of 5kBit */
//        ArrivalCurve ac_flow2 = Curve.getFactory().createTokenBucket(15.0e6, 5.0e3);
//
//        /* AC Flow 3 with rate of 30MBit/s and burst of 10kBit */
//        ArrivalCurve ac_flow3 = Curve.getFactory().createTokenBucket(30.0e6, 10.0e3);


        /* Talker */
        CBS_Server CbsTalker1 = sg.addServer("Talker1", CBS_Server.SRV_TYPE.TALKER);
        CBS_Server CbsTalker2 = sg.addServer("Talker2", CBS_Server.SRV_TYPE.TALKER);
        CBS_Server CbsTalker3 = sg.addServer("Talker3", CBS_Server.SRV_TYPE.TALKER);

        /* Listener */
        CBS_Server CbsListener1 = sg.addServer("Listener1", CBS_Server.SRV_TYPE.LISTENER);

        /* Switches */
        CBS_Server CbsRlServer1 = sg.addServer("s1", CBS_Server.SRV_TYPE.SWITCH);
        CBS_Server CbsRlServer2 = sg.addServer("s2", CBS_Server.SRV_TYPE.SWITCH);
        CBS_Server CbsRlServer3 = sg.addServer("s3", CBS_Server.SRV_TYPE.SWITCH);
        CBS_Server CbsRlServer4 = sg.addServer("s4", CBS_Server.SRV_TYPE.SWITCH);

//        /* FIFO Server 1 and 2 with rate of 50MBit/s and Latency of 200us */
//        servers[0] = sg.addServer("S1", Curve.getFactory().createRateLatency(50.0e6, 200.0e-6), AnalysisConfig.Multiplexing.FIFO);
//        servers[1] = sg.addServer("S2", Curve.getFactory().createRateLatency(50.0e6, 200.0e-6), AnalysisConfig.Multiplexing.FIFO);
//        /* FIFO Server 3 and 4 with rate of 200MBit/s and Latency of 10us */
//        servers[2] = sg.addServer("S3", Curve.getFactory().createRateLatency(200.0e6, 10.0e-6), AnalysisConfig.Multiplexing.FIFO);
//        servers[3] = sg.addServer("S4", Curve.getFactory().createRateLatency(200.0e6, 10.0e-6), AnalysisConfig.Multiplexing.FIFO);

        /* Define links between server */
        CBS_Link t_T1_1 = sg.addLink("Talker1 --> s1", CbsTalker1, CbsRlServer1, 100.0e6);  // 100MBit/s capacity
        CBS_Link t_1_3  = sg.addLink("s1 --> s3", CbsRlServer1, CbsRlServer3, 100.0e6);     // 100MBit/s capacity
        CBS_Link t_T2_2 = sg.addLink("Talker2 --> s2", CbsTalker2, CbsRlServer2, 100.0e6);  // 100MBit/s capacity
        CBS_Link t_T3_2 = sg.addLink("Talker3 --> s2", CbsTalker3, CbsRlServer2, 100.0e6);  // 100MBit/s capacity
        CBS_Link t_2_3  = sg.addLink("s2 --> s3", CbsRlServer2, CbsRlServer3, 100.0e6);     // 100MBit/s capacity
        CBS_Link t_3_4  = sg.addLink("s3 --> s4", CbsRlServer3, CbsRlServer4, 100.0e6);     // 100MBit/s capacity
        CBS_Link t_4_L1  = sg.addLink("s4 --> Listener1", CbsRlServer4, CbsListener1, 100.0e6);     // 100MBit/s capacity

        /* Define path for flow 1 */
        LinkedList<CBS_Link> path0 = new LinkedList<CBS_Link>();
        path0.add(t_T1_1);
        path0.add(t_1_3);
        path0.add(t_3_4);
        path0.add(t_4_L1);
        sg.addFlow(path0, flow1, 5.0e6);    // 5MBit/s IdleSlope

        /* Define path for flow 2 */
        LinkedList<CBS_Link> path1 = new LinkedList<CBS_Link>();
        path1.add(t_T2_2);
        path1.add(t_2_3);
        path1.add(t_3_4);
        path1.add(t_4_L1);
        sg.addFlow(path1, flow2, 5.0e6);    // 5MBit/s IdleSlope

        /* Define path for flow 3 */
        LinkedList<CBS_Link> path2 = new LinkedList<CBS_Link>();
        path2.add(t_T3_2);
        path2.add(t_2_3);
        path2.add(t_3_4);
        path2.add(t_4_L1);
        sg.addFlow(path2, flow3, 5.0e6);    // 5MBit/s IdleSlope

        System.out.println(sg);

        CBS_TotalFlowAnalysis tfa = new CBS_TotalFlowAnalysis(sg);
        tfa.performAnalysis(flow1);
        System.out.println("Total delay flow1: " + tfa.getTotalDelay() + "s");
        tfa.performAnalysis(flow2);
        System.out.println("Total delay flow2: " + tfa.getTotalDelay() + "s");
        tfa.performAnalysis(flow3);
        System.out.println("Total delay flow3: " + tfa.getTotalDelay() + "s");
    }
}