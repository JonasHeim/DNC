package org.networkcalculus.dnc.tsn_cbs;

import org.networkcalculus.dnc.algebra.disco.affine.Deconvolution_Disco_Affine;
import org.networkcalculus.dnc.curves.ArrivalCurve;
import org.networkcalculus.dnc.curves.Curve;
import org.networkcalculus.dnc.curves.ServiceCurve;

import java.util.*;

/**
 * Class representing of a server graph with CBS shaped servers
 */
public class CBS_ServerGraph {
    /**
     * String identification of the server graph
     */
    private final String alias;

    /**
     * List of CBS servers that are part of the server graph
     */
    private final Set<CBS_Server> servers;

    /**
     * Maps CBS flows to the path that the flow traverses in the server graph
     */
    private final Map<CBS_Flow, LinkedList<CBS_Link>> mapping_flow_to_path;

    /**
     * All links that connect CBS server
     */
    private final Set<CBS_Link> links;

    /**
     * All registered CBS flows
     */
    private final Set<CBS_Flow> flows;

    /**
     * Create a new, empty CBS server graph
     * @param alias String identification of the server graph
     */
    public CBS_ServerGraph(String alias) {
        this.alias = alias;
        this.servers = new HashSet<CBS_Server>();
        this.links = new HashSet<CBS_Link>();
        this.flows = new HashSet<CBS_Flow>();
        this.mapping_flow_to_path = new LinkedHashMap<CBS_Flow, LinkedList<CBS_Link>>();
    }

    /**
     * Retrieves the path that the flow traverses in this server graph
     * @param flow  Flow of interest
     * @return  Path of given flow as a list of CBS_Links
     */
    public LinkedList<CBS_Link> getPath(CBS_Flow flow) {
        return this.mapping_flow_to_path.get(flow);
    }

    /**
     * @return  String identification of the server graph
     */
    public String getAlias() { return this.alias; }

    /**
     * Add a new CBS server to the server graph
     *
     * @param alias            String representation of the new CBS server
     * @param serverType       TSN type of the server
     * @param idleSlopeMapping Mapping of idleSlopes for priorities
     * @return Handle to the new created server
     */
    public CBS_Server addServer(String alias, CBS_Server.SRV_TYPE serverType, LinkedHashMap<Integer, Double> idleSlopeMapping) {
        CBS_Server new_server = new CBS_Server(alias, serverType, idleSlopeMapping);

        this.servers.add(new_server);
        return new_server;
    }

    /**
     * Add a link between CBS servers in the server graph
     * @param alias         String identification of the new link
     * @param source        Source server of the link
     * @param destination   Destination server of the link
     * @param linkCapacity  Link capacity in bit/s
     * @return              Handle to the new created link
     */
    public CBS_Link addLink(String alias, CBS_Server source, CBS_Server destination, double linkCapacity) {
        CBS_Link new_link = new CBS_Link(alias, source, destination, linkCapacity);

        this.links.add(new_link);
        return new_link;
    }

    /**
     * Rerserve a flow in the server graph
     * @param path          Path of server that the flow traverses
     * @param flow          Flow to reserve
     */
    public void addFlow(LinkedList<CBS_Link> path, CBS_Flow flow) {
        /*le
         ToDo: check arguments
            - Are all servers of the path part of the server graph?
         */
        this.flows.add(flow);
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
        ArrivalCurve shaperLink = Curve.getFactory().createTokenBucket(linkCapacity, maxPacketSize);

        /* Apply link shaping to get the ArrivalCurve for the first hop */
        ac = Curve.getUtils().min(ac, shaperLink);
        System.out.println("SG initial AC " + ac);

        for(CBS_Link link: path) {
            System.out.println("SG addFlow @ link " + link + " with dst. server " + link.getDestination());

            /* Only update CBS Queues for forwarding devices */
            if(CBS_Server.SRV_TYPE.SWITCH == link.getSource().getServerType()) {

                CBS_Server serverSource = link.getSource();

                /* Update the queue at the server with flows properties.
                 * Calculates aggregated ArrivalCurve, min./max. Credits, ServiceCurve, etc. at the Queue */
                serverSource.addFlow(flow, ac, link);
                CBS_Queue queue = serverSource.getQueue(flow.getPriority(), link);
                ac = queue.getAggregateArrivalCurve();

                /* Calculate output flow bound */
                //ToDo: do we need to use the simple ArrivalCurve or the aggregated ArrivalCurve?
                ServiceCurve sc = queue.getServiceCurve();
                ac = Deconvolution_Disco_Affine.deconvolve(ac, sc);
                System.out.println("Output flow bound AC for flow " + flow.getAlias() + " at server " + serverSource.getAlias() + " : " + ac);

                /* Apply CBS shaping */
                ac = Curve.getUtils().min(ac, queue.getCbsShapingCurve());

                /* Apply link shaping */
                ac = Curve.getUtils().min(ac, queue.getLinkShapingCurve());
            }
            // ToDo: else SRV_TYPE.LISTENER ends the path. Shall we remember the total output flow bound?
        }

    }

    /**
     * @return String representation of the server graph used for printing
     */
    public String toString() {
        StringBuffer cbs_servergraph_str = new StringBuffer();

        cbs_servergraph_str.append("\r\n----------------------------------------------------------\r\n");
        cbs_servergraph_str.append("CBS ServerGraph \"" + this.alias + "\"");
        for(CBS_Server server: this.servers) {
            cbs_servergraph_str.append(server.toString());
        }
        cbs_servergraph_str.append("\r\n");
        for(CBS_Link link:this.links) {
            cbs_servergraph_str.append(link.toString());
        }
        cbs_servergraph_str.append("\r\n");
        for(CBS_Flow flow: this.flows) {
            cbs_servergraph_str.append(flow.toString());
        }
        cbs_servergraph_str.append("\r\n----------------------------------------------------------\r\n");

        return cbs_servergraph_str.toString();
    }
}
