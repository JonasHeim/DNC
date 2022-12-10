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

package org.networkcalculus.dnc.demos.Eval_CBSRenault;

import org.networkcalculus.dnc.tsn_cbs.*;

import java.util.LinkedHashMap;
import java.util.LinkedList;

public class Eval_CBSRenault {

    public Eval_CBSRenault() {
    }

    public static void main(String[] args) {
        Eval_CBSRenault demo = new Eval_CBSRenault();

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

        LinkedHashMap <Integer, Double> idleSlopeMapping = new LinkedHashMap <Integer, Double>();
        idleSlopeMapping.put(0, 5.0e6);
        idleSlopeMapping.put(1, 10.0e6);

        /* First step always */
        CBS_ServerGraph sg = new CBS_ServerGraph("CBS shaped line network");

        /****************** Definition of flows ***************/

        /* Priority 1 */
        CBS_Flow flow0 = new CBS_Flow("flow0", 20.0e-3, 12000, 2, 1,
                CBS_Flow.Periodicity.PERIODIC);
        CBS_Flow flow1 = new CBS_Flow("flow1", 20.0e-3, 12000, 2, 1,
                CBS_Flow.Periodicity.PERIODIC);
        CBS_Flow flow2 = new CBS_Flow("flow2", 20.0e-3, 12000, 2, 1,
                CBS_Flow.Periodicity.PERIODIC);
        CBS_Flow flow3 = new CBS_Flow("flow3", 20.0e-3, 12000, 2, 1,
                CBS_Flow.Periodicity.PERIODIC);

        /* Priority 0 */
        CBS_Flow flow4 = new CBS_Flow("flow4", 20.0e-3, 12000, 2, 0,
                CBS_Flow.Periodicity.PERIODIC);
        CBS_Flow flow5 = new CBS_Flow("flow5", 20.0e-3, 12000, 2, 0,
                CBS_Flow.Periodicity.PERIODIC);
        CBS_Flow flow6 = new CBS_Flow("flow6", 20.0e-3, 12000, 2, 0,
                CBS_Flow.Periodicity.PERIODIC);

        CBS_Flow flows[] = { flow0, flow1, flow2, flow3, flow4, flow5, flow6 };

        /****************** Definition of servers ***************/
        /* Talker */
        CBS_Server CbsTalker1 = sg.addServer("Talker1", CBS_Server.SRV_TYPE.TALKER, idleSlopeMapping);
        CBS_Server CbsTalker2 = sg.addServer("Talker2", CBS_Server.SRV_TYPE.TALKER, idleSlopeMapping);
        CBS_Server CbsTalker3 = sg.addServer("Talker3", CBS_Server.SRV_TYPE.TALKER, idleSlopeMapping);
        CBS_Server CbsTalker4 = sg.addServer("Talker4", CBS_Server.SRV_TYPE.TALKER, idleSlopeMapping);

        /* Listener */
        CBS_Server CbsListener1 = sg.addServer("Listener1", CBS_Server.SRV_TYPE.LISTENER, idleSlopeMapping);
        CBS_Server CbsListener2 = sg.addServer("Listener2", CBS_Server.SRV_TYPE.LISTENER, idleSlopeMapping);
        CBS_Server CbsListener3 = sg.addServer("Listener3", CBS_Server.SRV_TYPE.LISTENER, idleSlopeMapping);
        CBS_Server CbsListener4 = sg.addServer("Listener4", CBS_Server.SRV_TYPE.LISTENER, idleSlopeMapping);

        /* Switches */
        CBS_Server CbsRlServer1 = sg.addServer("s1", CBS_Server.SRV_TYPE.SWITCH, idleSlopeMapping);
        CBS_Server CbsRlServer2 = sg.addServer("s2", CBS_Server.SRV_TYPE.SWITCH, idleSlopeMapping);
        CBS_Server CbsRlServer3 = sg.addServer("s3", CBS_Server.SRV_TYPE.SWITCH, idleSlopeMapping);
        CBS_Server CbsRlServer4 = sg.addServer("s4", CBS_Server.SRV_TYPE.SWITCH, idleSlopeMapping);
        CBS_Server CbsRlServer5 = sg.addServer("s5", CBS_Server.SRV_TYPE.SWITCH, idleSlopeMapping);
        CBS_Server CbsRlServer6 = sg.addServer("s6", CBS_Server.SRV_TYPE.SWITCH, idleSlopeMapping);
        CBS_Server CbsRlServer7 = sg.addServer("s7", CBS_Server.SRV_TYPE.SWITCH, idleSlopeMapping);

        /****************** Definition of links ***************/

        /* Links: Talker -> Server */
        CBS_Link t_T1_4 = sg.addLink("Talker1 --> s4", CbsTalker1, CbsRlServer4, 100.0e6);
        CBS_Link t_T2_4 = sg.addLink("Talker2 --> s4", CbsTalker2, CbsRlServer4, 100.0e6);
        CBS_Link t_T3_5 = sg.addLink("Talker3 --> s5", CbsTalker3, CbsRlServer5, 100.0e6);
        CBS_Link t_T4_5 = sg.addLink("Talker4 --> s5", CbsTalker4, CbsRlServer5, 100.0e6);

        /* Links: Server -> Server */
        CBS_Link t_4_2 = sg.addLink("s4 --> s2", CbsRlServer4, CbsRlServer2, 100.0e6);
        CBS_Link t_5_2 = sg.addLink("s5 --> s2", CbsRlServer5, CbsRlServer2, 100.0e6);
        CBS_Link t_2_1 = sg.addLink("s2 --> s1", CbsRlServer2, CbsRlServer1, 100.0e6);
        CBS_Link t_1_3 = sg.addLink("s1 --> s3", CbsRlServer1, CbsRlServer3, 100.0e6);
        CBS_Link t_3_6 = sg.addLink("s3 --> s6", CbsRlServer3, CbsRlServer6, 100.0e6);
        CBS_Link t_3_7 = sg.addLink("s3 --> s7", CbsRlServer3, CbsRlServer7, 100.0e6);

        /* Links: Server -> Listener */
        CBS_Link t_7_L1 = sg.addLink("s7 --> Listener1", CbsRlServer7, CbsListener1, 100.0e6);
        CBS_Link t_7_L2 = sg.addLink("s7 --> Listener2", CbsRlServer7, CbsListener2, 100.0e6);
        CBS_Link t_6_L3 = sg.addLink("s6 --> Listener3", CbsRlServer6, CbsListener3, 100.0e6);
        CBS_Link t_6_L4 = sg.addLink("s6 --> Listener4", CbsRlServer6, CbsListener4, 100.0e6);

        /***************** Definition of paths ****************/
        /* Paths */
        LinkedList<CBS_Link> path0 = new LinkedList<CBS_Link>();
        path0.add(t_T1_4);
        path0.add(t_4_2);
        path0.add(t_2_1);
        path0.add(t_1_3);
        path0.add(t_3_7);
        path0.add(t_7_L1);

        LinkedList<CBS_Link> path1 = new LinkedList<CBS_Link>();
        path1.add(t_T2_4);
        path1.add(t_4_2);
        path1.add(t_2_1);
        path1.add(t_1_3);
        path1.add(t_3_7);
        path1.add(t_7_L2);

        LinkedList<CBS_Link> path2 = new LinkedList<CBS_Link>();
        path2.add(t_T3_5);
        path2.add(t_5_2);
        path2.add(t_2_1);
        path2.add(t_1_3);
        path2.add(t_3_6);
        path2.add(t_6_L3);

        LinkedList<CBS_Link> path3 = new LinkedList<CBS_Link>();
        path3.add(t_T4_5);
        path3.add(t_5_2);
        path3.add(t_2_1);
        path3.add(t_1_3);
        path3.add(t_3_6);
        path3.add(t_6_L4);

        /****************** Mapping of flows to paths. ***************/
        flow0.setPath(path0);
        flow1.setPath(path1);
        flow2.setPath(path2);
        flow3.setPath(path3);

        flow4.setPath(path1);
        flow5.setPath(path2);
        flow6.setPath(path3);

        /************** Addition of flows to server graph. *************/
        for(CBS_Flow f:flows)
        {
            sg.addFlow(f);
        }

        /****************** Calculate server graph ***************/
        sg.computeCBSQueues(CBS_ServerGraph.SHAPING_CONF.NO_SHAPING);

        /******************************************************
         ******************* Apply TFA ************************
         ******************************************************/
        CBS_TotalFlowAnalysis tfa = new CBS_TotalFlowAnalysis(sg);
        for(CBS_Flow f:flows)
        {
            tfa.performAnalysis(f);
            System.out.println(tfa);
        }

        //ToDo: enable for debugging
        System.out.println(sg);

    }
}
