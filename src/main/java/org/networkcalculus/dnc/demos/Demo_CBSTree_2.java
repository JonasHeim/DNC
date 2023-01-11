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

import org.networkcalculus.dnc.tsn_cbs.*;

import java.util.LinkedHashMap;
import java.util.LinkedList;

public class Demo_CBSTree_2 {

    public Demo_CBSTree_2() {
    }

    public static void main(String[] args) {
        Demo_CBSTree_2 demo = new Demo_CBSTree_2();

        try {
            demo.run();
        } catch (Exception e) {
        		e.printStackTrace();
        }
    }

    public void run() throws Exception {

        /* First step always */
        CBS_ServerGraph sg = new CBS_ServerGraph("CBS shaped tree network");

        LinkedHashMap <Integer, Double> idleSlopeMapping = new LinkedHashMap<Integer, Double>();
        idleSlopeMapping.put(0, 25.0e6);
        idleSlopeMapping.put(1, 25.0e6);

        /* Flows with priority 0 */
        CBS_Flow flow1 = new CBS_Flow("flow1", 1.0e-3, 6000, 1, 0, CBS_Flow.Periodicity.PERIODIC);
        CBS_Flow flow2 = new CBS_Flow("flow2", 1.0e-3, 6000, 1, 0, CBS_Flow.Periodicity.PERIODIC);
        CBS_Flow flow3 = new CBS_Flow("flow3", 1.0e-3, 6000, 1, 0, CBS_Flow.Periodicity.PERIODIC);

        /* Talker */
        CBS_Server CbsTalker1 = sg.addServer("Talker1", CBS_Server.SRV_TYPE.TALKER, idleSlopeMapping);
        CBS_Server CbsTalker2 = sg.addServer("Talker2", CBS_Server.SRV_TYPE.TALKER, idleSlopeMapping);
        CBS_Server CbsTalker3 = sg.addServer("Talker3", CBS_Server.SRV_TYPE.TALKER, idleSlopeMapping);

        /* Listener */
        CBS_Server CbsListener1 = sg.addServer("Listener1", CBS_Server.SRV_TYPE.LISTENER, idleSlopeMapping);

        /* Switches */
        CBS_Server CbsRlServer0 = sg.addServer("s0", CBS_Server.SRV_TYPE.SWITCH, idleSlopeMapping);
        CBS_Server CbsRlServer1 = sg.addServer("s1", CBS_Server.SRV_TYPE.SWITCH, idleSlopeMapping);
        CBS_Server CbsRlServer2 = sg.addServer("s2", CBS_Server.SRV_TYPE.SWITCH, idleSlopeMapping);
        CBS_Server CbsRlServer3 = sg.addServer("s3", CBS_Server.SRV_TYPE.SWITCH, idleSlopeMapping);

        /* Define links between server */
        CBS_Link t_T1_0 = sg.addLink("Talker1 --> s0", CbsTalker1, CbsRlServer0, 100.0e6);  // 100MBit/s capacity
        CBS_Link t_T2_0 = sg.addLink("Talker2 --> s0", CbsTalker2, CbsRlServer0, 100.0e6);  // 100MBit/s capacity
        CBS_Link t_T3_2 = sg.addLink("Talker3 --> s2", CbsTalker3, CbsRlServer2, 100.0e6);  // 100MBit/s capacity

        CBS_Link t_0_1  = sg.addLink("s0 --> s1", CbsRlServer0, CbsRlServer1, 100.0e6);     // 100MBit/s capacity
        CBS_Link t_1_3  = sg.addLink("s1 --> s3", CbsRlServer1, CbsRlServer3, 100.0e6);     // 100MBit/s capacity
        CBS_Link t_2_3  = sg.addLink("s2 --> s3", CbsRlServer2, CbsRlServer3, 100.0e6);     // 100MBit/s capacity

        CBS_Link t_3_L1  = sg.addLink("s3 --> Listener1", CbsRlServer3, CbsListener1, 100.0e6);     // 100MBit/s capacity

        /* Define path 0 from Talker 1 to Listener 1 */
        LinkedList<CBS_Link> path0 = new LinkedList<CBS_Link>();
        path0.add(t_T1_0);
        path0.add(t_0_1);
        path0.add(t_1_3);
        path0.add(t_3_L1);

        /* Define path 1 from Talker 2 to Listener 1 */
        LinkedList<CBS_Link> path1 = new LinkedList<CBS_Link>();
        path1.add(t_T2_0);
        path1.add(t_0_1);
        path1.add(t_1_3);
        path1.add(t_3_L1);

        /* Define path 2 from Talker 3 to Listener 1 */
        LinkedList<CBS_Link> path2 = new LinkedList<CBS_Link>();
        path2.add(t_T3_2);
        path2.add(t_2_3);
        path2.add(t_3_L1);

        /* Map paths to flows */
        flow1.setPath(path0);
        flow2.setPath(path1);
        flow3.setPath(path2);

        //Prio 0
        sg.addFlow(flow1);
        sg.addFlow(flow2);
        sg.addFlow(flow3);

        /* Finally compute all CBS queues within the server graph */
        sg.computeCBSQueues(CBS_ServerGraph.SHAPING_CONF.LINK_AND_CBS_SHAPING);

        CBS_TotalFlowAnalysis tfa = new CBS_TotalFlowAnalysis(sg);
        tfa.performAnalysis(flow1);
        System.out.println(tfa);
        tfa.performAnalysis(flow2);
        System.out.println(tfa);
        tfa.performAnalysis(flow3);
        System.out.println(tfa);

        //ToDo: enable for debugging
        System.out.println(sg);
    }
}