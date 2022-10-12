package org.networkcalculus.dnc.tsn_cbs;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class CBS_ServerGraph {
    private final String alias;
    private Set<CBS_RateLatency_Server> servers;
    private Set<CBS_Link> links;

    private Set<CBS_TokenBucket_Flow> flows;
    public CBS_ServerGraph(String alias) {
        this.alias = alias;
        this.servers = new HashSet<CBS_RateLatency_Server>();
        this.links = new HashSet<CBS_Link>();
        this.flows = new HashSet<CBS_TokenBucket_Flow>();
    }

    public CBS_ServerGraph(String alias, HashSet<CBS_RateLatency_Server> servers, HashSet<CBS_Link> links, HashSet<CBS_TokenBucket_Flow> flows) {
        this.alias = alias;
        /* ToDO: No verification happening yet.
           Make sure links and flows paths must only contain  servers that are path of the graph.
         */
        this.servers = servers;
        this.links = links;
        this.flows = flows;
    }

    public CBS_RateLatency_Server addServer(String alias, double linkCapacity) {
        CBS_RateLatency_Server new_server = new CBS_RateLatency_Server(alias, linkCapacity);
        //ToDo: Update internal representation with new server
        this.servers.add(new_server);
        return new_server;
    }

    public CBS_Link addLink(String alias, CBS_RateLatency_Server source, CBS_RateLatency_Server destination, double linkCapacity) {
        CBS_Link new_link = new CBS_Link(alias, source, destination, linkCapacity);
        //ToDo: Update internal representation
        this.links.add(new_link);
        return new_link;
    }


    public void addFlow(LinkedList<CBS_Link> path, CBS_TokenBucket_Flow flow) {
        //ToDo: implement
    }

    public String getAlias() { return this.alias; }

    public String toString() {
        StringBuffer cbs_servergraph_str = new StringBuffer();

        cbs_servergraph_str.append("-------------------------------------------");
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
        cbs_servergraph_str.append("\r\n");
        cbs_servergraph_str.append("-------------------------------------------\r\n");

        return cbs_servergraph_str.toString();
    }
}
