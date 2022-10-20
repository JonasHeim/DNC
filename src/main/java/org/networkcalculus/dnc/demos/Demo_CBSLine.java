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

import org.networkcalculus.dnc.curves.ArrivalCurve;
import org.networkcalculus.dnc.curves.Curve;
import org.networkcalculus.dnc.tsn_cbs.*;

import java.util.*;

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
        CBS_ServerGraph sg = new CBS_ServerGraph("CBS shaped line network");

        CBS_Flow flow1 = new CBS_Flow("flow1", 1.0e-3, 512, 1, 0, CBS_Flow.Periodicity.PERIODIC);
        CBS_Flow flow2 = new CBS_Flow("flow2", 10.0e-3, 12000, 2, 1, CBS_Flow.Periodicity.PERIODIC);
        CBS_Flow flow3 = new CBS_Flow("flow3", 20.0e-3, 12000, 5, 2, CBS_Flow.Periodicity.PERIODIC);
        CBS_Flow flow4 = new CBS_Flow("flow4", 1.0e-3, 512, 1, 0, CBS_Flow.Periodicity.PERIODIC);

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

        CBS_Link t_T1_1 = sg.addLink("Talker1 --> s1", CbsTalker1, CbsRlServer1, 100.0e6);      // 100MBit/s link capacity
        CBS_Link t_1_2 = sg.addLink("s1 --> s2", CbsRlServer1, CbsRlServer2, 100.0e6);          // 100MBit/s link capacity
        CBS_Link t_T2_2 = sg.addLink("Talker2 --> s2", CbsTalker2, CbsRlServer2, 100.0e6);      // 100MBit/s link capacity
        CBS_Link t_2_3 = sg.addLink("s2 --> s3", CbsRlServer2, CbsRlServer3, 100.0e6);          // 100MBit/s link capacity
        CBS_Link t_T3_3 = sg.addLink("Talker3 --> s1", CbsTalker3, CbsRlServer3, 100.0e6);      // 100MBit/s link capacity
        CBS_Link t_3_4 = sg.addLink("s3 --> s4", CbsRlServer3, CbsRlServer4, 100.0e6);          // 100MBit/s link capacity
        CBS_Link t_4_L1 = sg.addLink("s4 --> Listener1", CbsRlServer4, CbsListener1, 100.0e6);  // 100MBit/s link capacity

        LinkedList<CBS_Link> path0 = new LinkedList<CBS_Link>();
        path0.add(t_T1_1);
        path0.add(t_1_2);
        path0.add(t_2_3);
        path0.add(t_3_4);
        path0.add(t_4_L1);
        sg.addFlow(path0, flow1, 512.0e3);    //Add Flow with 2MBit/s bandwidth reservation

        LinkedList<CBS_Link> path1 = new LinkedList<CBS_Link>();
        path1.add(t_T2_2);
        path1.add(t_2_3);
        path1.add(t_3_4);
        path1.add(t_4_L1);
        sg.addFlow(path1, flow2, 2.4e6);    //Add Flow with 5MBit/s bandwidth reservation

        LinkedList<CBS_Link> path2 = new LinkedList<CBS_Link>();
        path2.add(t_T3_3);
        path2.add(t_3_4);
        path2.add(t_4_L1);
        sg.addFlow(path2, flow3, 3.0e6);    //Add Flow with 5MBit/s bandwidth reservation

        System.out.println(sg);

        /* Now add another flow with the highest priority to all servers */
        //ToDo: what happens when a second flor with the same priority is added? What happens at the servers queues? Only IdleSlope increases?
        sg.addFlow(path0, flow4, 512.0e3);
        System.out.println(sg);

        CBS_TotalFlowAnalysis tfa = new CBS_TotalFlowAnalysis(sg);
        tfa.performAnalysis(flow1);
        System.out.println("Total delay flow1: " + tfa.getTotalDelay() + "s");
        tfa.performAnalysis(flow2);
        System.out.println("Total delay flow2: " + tfa.getTotalDelay() + "s");
        tfa.performAnalysis(flow3);
        System.out.println("Total delay flow3: " + tfa.getTotalDelay() + "s");
        tfa.performAnalysis(flow4);
        System.out.println("Total delay flow4: " + tfa.getTotalDelay() + "s");

        //ToDo: if the aggrgated ArrivalCurve is used in min(aggrAC, CBS_Shaper) flow 1 & flow 4 have the same total delay. Does this make sense?

        /* ToDo: Remove
           Test to determine minimum of two Token-Bucket ArrivalCurves
        */
        ArrivalCurve ac_flow1 = Curve.getFactory().createTokenBucket(8.0e6, 1.5e3);
        System.out.println("AC Flow 1: " + ac_flow1);
        ArrivalCurve ac_flow2 = Curve.getFactory().createTokenBucket(20.0e6, 3.0e3);
        System.out.println("AC Flow 2: " + ac_flow2);

        System.out.println("\r\n----- Test of DNC minimum of two ArrivalCurves -----");
        ArrivalCurve minAC = Curve.getUtils().min(ac_flow1, ac_flow2);
        System.out.println("Minimum AC of Flows 1 and 2 is " + minAC); // should be flow 1

        ArrivalCurve aggrAC = Curve.getUtils().add(ac_flow1, ac_flow2);
        System.out.println("Aggregation AC of Flows 1 and 2 is " + aggrAC); // should be flow 1
    }
}
