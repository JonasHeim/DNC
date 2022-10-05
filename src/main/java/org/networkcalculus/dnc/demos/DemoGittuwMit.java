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

public class DemoGittuwMit {
  private static final Logger LOGGER = Logger.getLogger("DemoGittuwMit");

  public static void main(String[] args) {
    final DemoGittuwMit demo = new DemoGittuwMit();

    try {
      demo.run();
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Demo failed with exception", e);
    }
  }

  public void run() throws Exception {
    final ArrivalCurve arrivalCurve = Curve.getFactory().createTokenBucket(0.1e6, 0.1 * 0.1e6);

    final ServiceCurve serviceCurve = Curve.getFactory().createRateLatency(10.0e6, 0.01);

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

    Flow flowOfInterest = flows.get(2);

    // Analyze the network
    final Scanner scanner = new Scanner(System.in);

    boolean runLoop = true;
    while(runLoop) {

      System.out.println("Select an option:");
      System.out.println("1) TFA");
      System.out.println("2) SFA");
      System.out.println("3) PMOO");
      System.out.println("4) TMA");
      System.out.println("5) Exit");
      System.out.flush();

      System.out.print("Option > ");
      final String input = scanner.nextLine();

      switch(input) {
        case "1":
          analyzeWithTFA(sg, configuration, flowOfInterest);
          break;
        case "2":
          analyzeWithSFA(sg, configuration, flowOfInterest);
          break;
        case "3":
          analyzeWithPMOO(sg, configuration, flowOfInterest);
          break;
        case "4":
          analyzeWithTMA(sg, configuration, flowOfInterest);
          break;
        case "5": case "exit":
          runLoop = false;
          break;
        default:
          LOGGER.info("Invalid Option");
      }
    }
  }

  private void analyzeWithTMA(ServerGraph sg, AnalysisConfig configuration, Flow flowOfInterest) {
    System.out.println("--- Tandem Matching Analysis ---");
    final TandemMatchingAnalysis tma = new TandemMatchingAnalysis(sg, configuration);

    try {
      tma.performAnalysis(flowOfInterest);
      System.out.println("[TMA] e2e TMA SCs     : " + tma.getLeftOverServiceCurves());
      System.out.println("[TMA] xtx per server  : " + tma.getServerAlphasMapString());
      System.out.println("[TMA] delay bound     : " + tma.getDelayBound());
      System.out.println("[TMA] backlog bound   : " + tma.getBacklogBound());
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "TMA analysis failed", e);
    }
  }

  private void analyzeWithPMOO(ServerGraph sg, AnalysisConfig configuration, Flow flowOfInterest) {
    System.out.println("--- PMOO Analysis ---");
    final PmooAnalysis pmoo = new PmooAnalysis(sg, configuration);

    try {
      pmoo.performAnalysis(flowOfInterest);
      System.out.println("[PMOO] e2e PMOO SCs    : " + pmoo.getLeftOverServiceCurves());
      System.out.println("[PMOO] xtx per server  : " + pmoo.getServerAlphasMapString());
      System.out.println("[PMOO] delay bound     : " + pmoo.getDelayBound());
      System.out.println("[PMOO] backlog bound   : " + pmoo.getBacklogBound());
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "PMOO analysis failed", e);
    }
  }

  private void analyzeWithSFA(ServerGraph sg, AnalysisConfig configuration, Flow flowOfInterest) {
    System.out.println("--- Separated Flow Analysis ---");
    final SeparateFlowAnalysis sfa = new SeparateFlowAnalysis(sg, configuration);

    try {
      sfa.performAnalysis(flowOfInterest);
      System.out.println("[SFA] e2e SFA SCs     : " + sfa.getLeftOverServiceCurves());
      System.out.println("[SFA]      per server : " + sfa.getServerLeftOverBetasMapString());
      System.out.println("[SFA] xtx per server  : " + sfa.getServerAlphasMapString());
      System.out.println("[SFA] delay bound     : " + sfa.getDelayBound());
      System.out.println("[SFA] backlog bound   : " + sfa.getBacklogBound());
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "SFA analysis failed", e);
    }
  }

  private void analyzeWithTFA(ServerGraph sg, AnalysisConfig configuration, Flow flowOfInterest) {
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