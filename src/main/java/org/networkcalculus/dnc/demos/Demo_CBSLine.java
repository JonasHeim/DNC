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
import org.networkcalculus.dnc.tsn_cbs.CBS_Link;
import org.networkcalculus.dnc.tsn_cbs.CBS_RateLatency_Server;
import org.networkcalculus.dnc.tsn_cbs.CBS_ServerGraph;
import org.networkcalculus.dnc.tsn_cbs.CBS_TokenBucket_Flow;

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
        CBS_ServerGraph sg = new CBS_ServerGraph("CBS shapes line network");

        CBS_TokenBucket_Flow flow1 = new CBS_TokenBucket_Flow("flow1", 1.0e-3, 512, 1, 0, 100.0e6, CBS_TokenBucket_Flow.Periodicity.PERIODIC);
        System.out.println(flow1);

        CBS_TokenBucket_Flow flow2 = new CBS_TokenBucket_Flow("flow2", 10.0e-3, 12000, 2, 1, 100.0e6, CBS_TokenBucket_Flow.Periodicity.PERIODIC);
        System.out.println(flow2);

        CBS_TokenBucket_Flow flow3 = new CBS_TokenBucket_Flow("flow3", 20.0e-3, 12000, 5, 2, 100.0e6, CBS_TokenBucket_Flow.Periodicity.PERIODIC);
        System.out.println(flow3);

        CBS_RateLatency_Server CbsRlServer1 = sg.addServer("s1", 100e6); // 100MBit/s link capacity
        CbsRlServer1.addQueue(0, 2.0e6, 512);
        CbsRlServer1.addQueue(2, 5.0e6, 12e3);
        /* Credits of prio 2 should change after adding higher prio 1 */
        CbsRlServer1.addQueue(1, 2.0e6, 512000);
        //System.out.println(CbsRlServer1);

        CBS_RateLatency_Server CbsRlServer2 = sg.addServer("s2", 100e6); // 100MBit/s link capacity
        CbsRlServer2.addQueue(0, 2.0e6, 512);
        CbsRlServer2.addQueue(1, 2.0e6, 512000);
        CbsRlServer2.addQueue(2, 5.0e6, 12e3);
        // System.out.println(CbsRlServer1);

        CBS_RateLatency_Server CbsRlServer3 = sg.addServer("s3", 100e6); // 100MBit/s link capacity
        CBS_RateLatency_Server CbsRlServer4 = sg.addServer("s4", 100e6); // 100MBit/s link capacity

        CBS_Link t_1_2 = sg.addLink("s1 --> s2", CbsRlServer1, CbsRlServer2, 100e6); // 100MBit/s link capacity
        CBS_Link t_2_3 = sg.addLink("s2 --> s3", CbsRlServer2, CbsRlServer3, 100e6); // 100MBit/s link capacity
        CBS_Link t_3_4 = sg.addLink("s3 --> s4", CbsRlServer3, CbsRlServer4, 100e6); // 100MBit/s link capacity

        LinkedList<CBS_Link> path0 = new LinkedList<CBS_Link>();
        path0.add(t_1_2);
        path0.add(t_2_3);
        path0.add(t_3_4);
        sg.addFlow(path0, flow1);

        LinkedList<CBS_Link> path1 = new LinkedList<CBS_Link>();
        path1.add(t_2_3);
        path1.add(t_3_4);
        sg.addFlow(path1, flow2);

        LinkedList<CBS_Link> path2 = new LinkedList<CBS_Link>();
        path2.add(t_3_4);
        sg.addFlow(path2, flow3);

        System.out.println(sg);

        /* Do CBS TFA analysis */
        //ToDo: design and implement
        /*
            Für jeden Server eines Pfads eines Flows sukzessive:
                1. Für jeden eingehenden Link in diesen Server
                    1.1. Hole alle ACs von Flows gleicher Priorität
                    1.2. Bilde aggr. AC dieser Flows
                    1.3. Bilde minimum

         */

        /* ToDo: Remove
           Test to determine minimum of two Token-Bucket ArrivalCurves
        */
        ArrivalCurve ac_flow1 = Curve.getFactory().createTokenBucket(8.0e6, 1.5e3);
        ArrivalCurve ac_flow2 = Curve.getFactory().createTokenBucket(20.0e6, 3.0e3);
        System.out.println("\r\n----- Test of DNC minimum of two ArrivalCurves -----");
        ArrivalCurve minAC = Curve.getUtils().min(ac_flow1, ac_flow2);
        System.out.println("Minimum AC of Flows 1 and 2 is " + minAC); // should be flow 1
    }
}
