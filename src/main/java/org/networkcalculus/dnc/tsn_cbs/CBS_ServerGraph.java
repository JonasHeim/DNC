package org.networkcalculus.dnc.tsn_cbs;

import org.networkcalculus.dnc.curves.ArrivalCurve;
import org.networkcalculus.dnc.curves.Curve;

import java.util.*;

public class CBS_ServerGraph {
    private final String alias;


    private Set<CBS_RateLatency_Server> servers;

    private Map<CBS_RateLatency_Server, CBS_Link> mapping_server_to_link;
    private Map<CBS_TokenBucket_Flow, LinkedList<CBS_Link>> mapping_flow_to_path;
    private Set<CBS_Link> links;

    private Set<CBS_TokenBucket_Flow> flows;
    public CBS_ServerGraph(String alias) {
        this.alias = alias;
        this.servers = new HashSet<CBS_RateLatency_Server>();
        this.links = new HashSet<CBS_Link>();
        this.flows = new HashSet<CBS_TokenBucket_Flow>();
        this.mapping_server_to_link = new HashMap<CBS_RateLatency_Server, CBS_Link>();
        this.mapping_flow_to_path = new HashMap<CBS_TokenBucket_Flow, LinkedList<CBS_Link>>();
    }

    public CBS_ServerGraph(String alias, HashSet<CBS_RateLatency_Server> servers, HashSet<CBS_Link> links) {
        this.alias = alias;
        /* ToDO: No verification happening yet.
           Make sure links and flows paths must only contain  servers that are path of the graph.
         */
        this.servers = servers;
        this.links = links;
    }

    public CBS_RateLatency_Server addServer(String alias, CBS_RateLatency_Server.SRV_TYPE serverType) {
        CBS_RateLatency_Server new_server = new CBS_RateLatency_Server(alias, serverType);

        this.servers.add(new_server);
        return new_server;
    }

    public CBS_Link addLink(String alias, CBS_RateLatency_Server source, CBS_RateLatency_Server destination, double linkCapacity) {
        CBS_Link new_link = new CBS_Link(alias, source, destination, linkCapacity);

        this.links.add(new_link);
        return new_link;
    }


    public void addFlow(LinkedList<CBS_Link> path, CBS_TokenBucket_Flow flow, double idleSlope) {
        /*
         ToDo: check arguments
            - Are all servers of the path part of the server graph?
         */

        this.mapping_flow_to_path.put(flow, path);

        //ToDo: implement algorithm that updates all servers on flows path and adds/updated the queues at the servers
        /*
            Add the flow to each server on its path.
            For each server a CBS-Queue with the same priority as the flow must be reserved.
            For each CBS-Queue the
         */

        /* For the first link from Talker to the first hop only Link-Shaping is applied to the AC */
        double linkCapacity = path.getFirst().getCapacity();
        double maxPacketSize = flow.getMfs();

        ArrivalCurve ac = flow.calculateAndSetAC(linkCapacity);
        ArrivalCurve shaperLink = Curve.getFactory().createTokenBucket(linkCapacity, flow.getMfs());

        /* Apply link shaping to get the ArrivalCurve for the first hop */
        ac = Curve.getUtils().min(ac, shaperLink);
        System.out.println("SG initial AC " + ac);

        for(CBS_Link link: path) {
            System.out.println("SG addFlow @ link " + link + " with dst. server " + link.getDestination());

            /* Only update CBS Queues for forwarding devices */
            if(CBS_RateLatency_Server.SRV_TYPE.SWITCH == link.getSource().getServerType()) {

                /* Update the queue at the server with flows properties.
                 * Calculates aggregated ArrivalCurve, min./max. Credits, ServiceCurve, etc. at the Queue */
                link.getSource().addFlow(flow, ac, idleSlope, link);

                /* Calculate ArrivalCurve for the next hop */
                /* Get CBS Shaper arrival curve */
                /* Get Link Shaper arrival curve */
                /* Build minimum and set ac */
            }
        }

    }

    public String getAlias() { return this.alias; }

    public String toString() {
        StringBuffer cbs_servergraph_str = new StringBuffer();

        cbs_servergraph_str.append("\r\n----------------------------------------------------------\r\n");
        cbs_servergraph_str.append("CBS ServerGraph \"" + this.alias + "\"");
        for(CBS_RateLatency_Server server: this.servers) {
            cbs_servergraph_str.append(server.toString());
        }
        cbs_servergraph_str.append("\r\n");
        for(CBS_Link link:this.links) {
            cbs_servergraph_str.append(link.toString());
        }
        cbs_servergraph_str.append("\r\n");
        for(CBS_TokenBucket_Flow flow: this.flows) {
            cbs_servergraph_str.append(flow.toString());
        }
        cbs_servergraph_str.append("\r\n----------------------------------------------------------\r\n");

        return cbs_servergraph_str.toString();
    }
}
