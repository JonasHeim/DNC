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

import java.util.HashMap;
import java.util.LinkedHashMap;
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

        LinkedHashMap <Integer, Double> idleSlopeMapping = new LinkedHashMap<Integer, Double>();
        idleSlopeMapping.put(0, 25.0e6);
        idleSlopeMapping.put(1, 25.0e6);
        idleSlopeMapping.put(2, 25.0e6);

        /* Flows with priority 0 */
        CBS_Flow flow1 = new CBS_Flow("flow1", 1.0e-3, 12000, 1, 0, CBS_Flow.Periodicity.PERIODIC);
        CBS_Flow flow2 = new CBS_Flow("flow2", 1.0e-3, 12000, 1, 0, CBS_Flow.Periodicity.PERIODIC);
        CBS_Flow flow3 = new CBS_Flow("flow3", 1.0e-3, 12000, 1, 0, CBS_Flow.Periodicity.APERIODIC);
        CBS_Flow flow4 = new CBS_Flow("flow4", 1.0e-3, 12000, 1, 0, CBS_Flow.Periodicity.APERIODIC);

        /* Flows with priority 1 */
        CBS_Flow flow5 = new CBS_Flow("flow5", 1.0e-3, 12000, 1, 1, CBS_Flow.Periodicity.PERIODIC);
        CBS_Flow flow6 = new CBS_Flow("flow6", 1.0e-3, 12000, 1, 1, CBS_Flow.Periodicity.PERIODIC);

        /* Flows with priority 2 */
        CBS_Flow flow7 = new CBS_Flow("flow7", 1.0e-3, 12000, 1, 2, CBS_Flow.Periodicity.PERIODIC);
        CBS_Flow flow8 = new CBS_Flow("flow8", 1.0e-3, 12000, 1, 2, CBS_Flow.Periodicity.PERIODIC);

        /* Talker */
        CBS_Server CbsTalker1 = sg.addServer("Talker1", CBS_Server.SRV_TYPE.TALKER, idleSlopeMapping);
        CBS_Server CbsTalker2 = sg.addServer("Talker2", CBS_Server.SRV_TYPE.TALKER, idleSlopeMapping);
        CBS_Server CbsTalker3 = sg.addServer("Talker3", CBS_Server.SRV_TYPE.TALKER, idleSlopeMapping);
        CBS_Server CbsTalker4 = sg.addServer("Talker4", CBS_Server.SRV_TYPE.TALKER, idleSlopeMapping);

        /* Listener */
        CBS_Server CbsListener1 = sg.addServer("Listener1", CBS_Server.SRV_TYPE.LISTENER, idleSlopeMapping);

        /* Switches */
        CBS_Server CbsRlServer1 = sg.addServer("s1", CBS_Server.SRV_TYPE.SWITCH, idleSlopeMapping);
        CBS_Server CbsRlServer2 = sg.addServer("s2", CBS_Server.SRV_TYPE.SWITCH, idleSlopeMapping);
        CBS_Server CbsRlServer3 = sg.addServer("s3", CBS_Server.SRV_TYPE.SWITCH, idleSlopeMapping);
        CBS_Server CbsRlServer4 = sg.addServer("s4", CBS_Server.SRV_TYPE.SWITCH, idleSlopeMapping);

        /* Define links between server */
        CBS_Link t_T1_1 = sg.addLink("Talker1 --> s1", CbsTalker1, CbsRlServer1, 100.0e6);  // 100MBit/s capacity
        CBS_Link t_T2_1 = sg.addLink("Talker2 --> s1", CbsTalker2, CbsRlServer1, 100.0e6);  // 100MBit/s capacity
        CBS_Link t_T3_2 = sg.addLink("Talker3 --> s2", CbsTalker3, CbsRlServer2, 100.0e6);  // 100MBit/s capacity
        CBS_Link t_T4_2 = sg.addLink("Talker4 --> s2", CbsTalker4, CbsRlServer2, 100.0e6);  // 100MBit/s capacity
        CBS_Link t_1_3  = sg.addLink("s1 --> s3", CbsRlServer1, CbsRlServer3, 100.0e6);     // 100MBit/s capacity
        CBS_Link t_2_3  = sg.addLink("s2 --> s3", CbsRlServer2, CbsRlServer3, 100.0e6);     // 100MBit/s capacity
        CBS_Link t_3_4  = sg.addLink("s3 --> s4", CbsRlServer3, CbsRlServer4, 100.0e6);     // 100MBit/s capacity
        CBS_Link t_4_L1  = sg.addLink("s4 --> Listener1", CbsRlServer4, CbsListener1, 100.0e6);     // 100MBit/s capacity

        /* Define path 0 from Talker 1 to Listener 1 */
        LinkedList<CBS_Link> path0 = new LinkedList<CBS_Link>();
        path0.add(t_T1_1);
        path0.add(t_1_3);
        path0.add(t_3_4);
        path0.add(t_4_L1);

        /* Define path 1 from Talker 2 to Listener 1 */
        LinkedList<CBS_Link> path1 = new LinkedList<CBS_Link>();
        path1.add(t_T2_1);
        path1.add(t_1_3);
        path1.add(t_3_4);
        path1.add(t_4_L1);

        /* Define path 2 from Talker 3 to Listener 1 */
        LinkedList<CBS_Link> path2 = new LinkedList<CBS_Link>();
        path2.add(t_T3_2);
        path2.add(t_2_3);
        path2.add(t_3_4);
        path2.add(t_4_L1);


        /* Define path 3 from Talker 4 to Listener 1 */
        LinkedList<CBS_Link> path3 = new LinkedList<CBS_Link>();
        path3.add(t_T4_2);
        path3.add(t_2_3);
        path3.add(t_3_4);
        path3.add(t_4_L1);

        /* Add flows with decreasing priority */
        //Prio 0
        sg.addFlow(path0, flow1);
        sg.addFlow(path1, flow2);
        sg.addFlow(path2, flow3);
        sg.addFlow(path3, flow4);

        //Prio 1
        sg.addFlow(path0, flow5);
        sg.addFlow(path2, flow6);

        //Prio 2
        sg.addFlow(path1, flow7);
        sg.addFlow(path3, flow8);

        System.out.println(sg);

        CBS_TotalFlowAnalysis tfa = new CBS_TotalFlowAnalysis(sg);
        tfa.performAnalysis(flow1);
        System.out.println(tfa);
        tfa.performAnalysis(flow2);
        System.out.println(tfa);
        tfa.performAnalysis(flow3);
        System.out.println(tfa);
        tfa.performAnalysis(flow4);
        System.out.println(tfa);
        tfa.performAnalysis(flow5);
        System.out.println(tfa);
        tfa.performAnalysis(flow6);
        System.out.println(tfa);
        tfa.performAnalysis(flow7);
        System.out.println(tfa);
        tfa.performAnalysis(flow8);
        System.out.println(tfa);
    }
}