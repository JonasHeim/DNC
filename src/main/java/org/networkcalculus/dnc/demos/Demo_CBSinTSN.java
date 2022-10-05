package org.networkcalculus.dnc.demos;

import org.networkcalculus.dnc.AnalysisConfig;
import org.networkcalculus.dnc.curves.ArrivalCurve;
import org.networkcalculus.dnc.curves.Curve;
import org.networkcalculus.dnc.curves.MaxServiceCurve;
import org.networkcalculus.dnc.curves.ServiceCurve;
import org.networkcalculus.dnc.network.server_graph.Flow;
import org.networkcalculus.dnc.network.server_graph.Server;
import org.networkcalculus.dnc.network.server_graph.ServerGraph;
import org.networkcalculus.dnc.network.server_graph.Turn;
import org.networkcalculus.dnc.tandem.analyses.PmooAnalysis;
import org.networkcalculus.dnc.tandem.analyses.SeparateFlowAnalysis;
import org.networkcalculus.dnc.tandem.analyses.TandemMatchingAnalysis;
import org.networkcalculus.dnc.tandem.analyses.TotalFlowAnalysis;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Demo_CBSinTSN {
  private static final Logger LOGGER = Logger.getLogger("Demo_CBSinTSN");

  /*
    TSN input parameter used for flows, ArrivalCurves, ServiceCurves and CBS behaviour
   */

  /* ***** TSN Class A ***** */

  /* Maximum number of packets of flows of TSN class A */
  private static final double cTSN_MIF_ClassA = 1.0;

  /* Maximum size of packets in bytes of flows of TSN class A */
  private static final double cTSN_MFS_ClassA = 200.0;

  /* Sending interval/sliding window length of flows of TSN class A */
  private static final double cTSN_CMI_ClassA = 125e-6;

  /* ***** TSN Class B ***** */

  /* Maximum number of packets of flows of TSN class B */
  private static final double cTSN_MIF_ClassB = 1.0;

  /* Maximum size of packets in bytes of flows of TSN class B */
  private static final double cTSN_MFS_ClassB = 200.0;

  /* Sending interval/sliding window length of flows of TSN class B */
  private static final double cTSN_CMI_ClassB = 250e-6;

  /*
    Network/Graph parameter
  */

  /* Capacity of link between talker and first hop of flow 1 */
  private static final double cFlow1_InitialLinkCapacity = 100.0e6; // 100MBit/s

  /* Capacity of link between talker and first hop of flow 2 */
  private static final double cFlow2_InitialLinkCapacity = cFlow1_InitialLinkCapacity; // same as flow 1

  /* Starting point of demo */
  public static void main(String[] args) {
    final Demo_CBSinTSN demo = new Demo_CBSinTSN();

    try {
      demo.run();
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Demo failed with exception", e);
    }
  }

  public void run() throws Exception {

    /* ***** Definition of parameters of flow 1 of TSN class A */

    /* Max. data flow can send in CMI in bits */
    final double cFlow1_MF = cTSN_MIF_ClassA * cTSN_MFS_ClassA * 8.0;

    /* Arrival rate of flow 1 of TSN class A */
    final double cFlow1_rate = cFlow1_MF / cTSN_CMI_ClassA;

    /* Arrival burst of periodic flow 1 of TSN class A from talker to first hop */
    final double cFlow1_burst = cFlow1_MF * (1.0 - (cFlow1_rate/cFlow1_InitialLinkCapacity));

    LOGGER.log(Level.INFO, "Flow 1 parameter: m=" + cFlow1_MF + " rate=" + cFlow1_rate + " burst=" + cFlow1_burst);

    /* ***** Definition of parameters of flow 2 of TSN class B */

    /* Max. data flow can send in CMI in bits */
    final double cFlow2_MF = cTSN_MIF_ClassB * cTSN_MFS_ClassB * 8.0;

    /* Arrival rate of flow 2 of TSN class B */
    final double cFlow2_rate = cFlow2_MF / cTSN_CMI_ClassB;

    /* Arrival burst of periodic flow 2 of TSN class B from talker to first hop */
    final double cFlow2_burst = cFlow2_MF * (1.0 - (cFlow2_rate/cFlow2_InitialLinkCapacity));

    LOGGER.log(Level.INFO, "Flow 2 parameter: m=" + cFlow2_MF + " rate=" + cFlow2_rate + " burst=" + cFlow2_burst);










    /* Arrival Curve for flow 1 of TSN class A */
    final ArrivalCurve arrivalCurve = Curve.getFactory().createTokenBucket(cFlow1_rate, cFlow1_burst);

    /* Arrival Curve for flow 2 of TSN class B */

    /* Service Curve for CBS server - same curve for all */
    final ServiceCurve serviceCurve = Curve.getFactory().createRateLatency(10.0e6, 0.01);
    final ServiceCurve tsnServiceCurve = Curve.getFactory().createRateLatency(10.6, 0.5);

    final MaxServiceCurve maxServiceCurve = Curve.getFactory().createRateLatencyMSC(100.0e6, 0.001);

    ServerGraph sg = new ServerGraph();
    AnalysisConfig configuration = new AnalysisConfig();

    List<Server> serverList =
            List.of(
                    sg.addServer(serviceCurve, maxServiceCurve),
                    sg.addServer(serviceCurve, maxServiceCurve),
                    sg.addServer(serviceCurve, maxServiceCurve),
                    sg.addServer(serviceCurve, maxServiceCurve));

    Map<String, Turn> turns =
            Map.of(
                    "01", sg.addTurn(serverList.get(0), serverList.get(1)),
                    "12", sg.addTurn(serverList.get(1), serverList.get(2)),
                    "23", sg.addTurn(serverList.get(2), serverList.get(3)));

    List<Flow> flows =
            List.of(
                    sg.addFlow(arrivalCurve, List.of(turns.get("01"), turns.get("12"), turns.get("23"))),
                    sg.addFlow(arrivalCurve, serverList.get(0)),
                    sg.addFlow(arrivalCurve, List.of(turns.get("23"))),
                    sg.addFlow(arrivalCurve, List.of(turns.get("01"), turns.get("12"))));

//    configuration.enforceMaxSC(AnalysisConfig.MaxScEnforcement.GLOBALLY_ON);
//    configuration.enforceMaxScOutputRate(AnalysisConfig.MaxScEnforcement.GLOBALLY_ON);

    //do TFAA foreach flow f in flows
    Flow flowOfInterest = flows.get(2);

    System.out.println("--- Total Flow Analysis ---");
    final TotalFlowAnalysis tfa = new TotalFlowAnalysis(sg, configuration);

    try {
      tfa.performAnalysis(flowOfInterest);
      System.out.println("[TFA] delay bound     : " + tfa.getDelayBound());
      System.out.println("[TFA]      per server : " + tfa.getServerDelayBoundMapString());
      System.out.println("[TFA] backlog bound   : " + tfa.getBacklogBound());
      System.out.println("[TFA]      per server : " + tfa.getServerBacklogBoundMapString());
      System.out.println("[TFA] alpha per server: " + tfa.getServerAlphasMapString());
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "TFA analysis failed", e);
    }
  }
}