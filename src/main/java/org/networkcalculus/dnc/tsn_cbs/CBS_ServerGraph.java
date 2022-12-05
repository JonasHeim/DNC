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
     * Possible shaping types of a server graph
     */
    public enum SHAPING_CONF {
        NO_SHAPING,
        LINK_SHAPING,
        CBS_SHAPING,
        LINK_AND_CBS_SHAPING
    }

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
     * Maps prioritiey to CBS flows
     */
    private Map<Integer, LinkedList<CBS_Flow>> mapping_priorityToFlow;

    /**
     * All links that connect CBS server
     */
    private final Set<CBS_Link> links;

    /**
     * All registered CBS flows
     */
    private final Set<CBS_Flow> flows;
    private Set<Integer> priorities;

    /**
     * Create a new, empty CBS server graph
     * @param alias String identification of the server graph
     */
    public CBS_ServerGraph(String alias) {
        this.alias = alias;
        this.servers = new HashSet<CBS_Server>();
        this.links = new HashSet<CBS_Link>();
        this.flows = new HashSet<CBS_Flow>();
        this.priorities = new TreeSet<Integer>();
        this.mapping_flow_to_path = new LinkedHashMap<CBS_Flow, LinkedList<CBS_Link>>();
        this.mapping_priorityToFlow = new HashMap<Integer, LinkedList<CBS_Flow>>();
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
     *
     * @param flow Flow to reserve
     */
    public void addFlow(CBS_Flow flow) throws Exception {
        LinkedList<CBS_Link> path = flow.getPath();
        if(path.isEmpty()) {
            throw new Exception("Empty path");
        }
        else if(CBS_Server.SRV_TYPE.TALKER != path.getFirst().getSource().getServerType()) {
            throw new Exception("First server of the path must be a TSN talker device.");
        }
        else if (CBS_Server.SRV_TYPE.LISTENER != path.getLast().getDestination().getServerType()) {
            throw new Exception("Last server of the path must be a TSN listener device.");
        }
        else if (path.size() <= 2) {
            throw new Exception("Path must contain at least one TSN forwarding/switching device");
        }

        this.flows.add(flow);
        this.mapping_flow_to_path.put(flow, path);
        this.priorities.add(flow.getPriority());

        LinkedList<CBS_Flow> list = this.mapping_priorityToFlow.get(flow.getPriority());
        if(null == list)
        {
            /* First flow of priority */
            list = new LinkedList<CBS_Flow>();
            this.mapping_priorityToFlow.put(flow.getPriority(), list);
        }

        list.add(flow);
    }

    /**
     * Clear all CBS queues on all servers of the graph
     */
    private void resetCBSQueues() {
        for (CBS_Server server: this.servers) {
            server.resetCBSQueues();
        }
    }

    /**
     * Calculate all CBS queues for all flows of the server graph.
     * Will delete all existing queues of the servers.
     * @param shapingConf Enum value of shaping curves supported by sever graph.
     */
    public void computeCBSQueues(SHAPING_CONF shapingConf) {
        /* Reset server graph in case the CBS queues have been calculated already and need to be updated */
        this.resetCBSQueues();

        /* Go through all priorities of the server graph, from highest (0) to lowest */
        /* For each priority p: */
        for (int p:this.priorities) {
            /* Go through all flows with that priority */
            /* For each flow f: */
            for (CBS_Flow flow: this.mapping_priorityToFlow.get(p)) {
                /* Traverse path of flow and update/create CBS queues, ACs, SCs on each server on the path */
                /* For each server s: */
                /* s.updateCBSQueue(f) */
                /* For the first link from Talker to the first hop only Link-Shaping is applied to the AC */
                LinkedList<CBS_Link> path = flow.getPath();
                double linkCapacity = path.getFirst().getCapacity();

                ArrivalCurve ac = Curve.getFactory().createZeroArrivals();

                for(CBS_Link link: path) {
                    System.out.println("SG addFlow @ link " + link + " with dst. server " + link.getDestination());

                    if(CBS_Server.SRV_TYPE.TALKER == link.getSource().getServerType()) {
                        /* Initial link */
                        ac = flow.calculateAndSetAC(linkCapacity);

                        /* Apply link shaping to initial link? */
                        if( (SHAPING_CONF.LINK_SHAPING == shapingConf) || (SHAPING_CONF.LINK_AND_CBS_SHAPING == shapingConf) ) {
                            ArrivalCurve shaperLink = Curve.getFactory().createTokenBucket(linkCapacity, link.getMaxPacketSize());

                            /* Get the shaped ArrivalCurve for the first hop */
                            ac = Curve.getUtils().min(ac, shaperLink);
                        }

                        System.out.println("SG initial AC " + ac);
                    }
                    else if(CBS_Server.SRV_TYPE.SWITCH == link.getSource().getServerType()) {
                        /* Only update CBS Queues for forwarding devices */
                        CBS_Server serverSource = link.getSource();

                        /* Update the queue at the server with flows properties.
                         * Calculates aggregated ArrivalCurve, min./max. Credits, ServiceCurve, etc. at the Queue */
                        serverSource.addFlow(flow, ac, link);
                        CBS_Queue queue = serverSource.getQueue(flow.getPriority(), link);

                        //ToDo: is this needed?
                        //ac = queue.getAggregateArrivalCurve();

                        /* Calculate output flow bound */
                        ServiceCurve sc = queue.getServiceCurve();
                        ac = Deconvolution_Disco_Affine.deconvolve(ac, sc);
                        System.out.println("Output flow bound AC for flow " + flow.getAlias() + " at server " + serverSource.getAlias() + " : " + ac);

                        /* Apply CBS shaping? */
                        if( (SHAPING_CONF.CBS_SHAPING == shapingConf) || (SHAPING_CONF.LINK_AND_CBS_SHAPING == shapingConf) ) {
                            /* Apply CBS shaping */
                            ac = Curve.getUtils().min(ac, queue.getCbsShapingCurve());
                        }

                        /* Apply link shaping? */
                        if( (SHAPING_CONF.LINK_SHAPING == shapingConf) || (SHAPING_CONF.LINK_AND_CBS_SHAPING == shapingConf) ) {
                            /* Apply link shaping */
                            ac = Curve.getUtils().min(ac, queue.getLinkShapingCurve());
                        }
                    }
                }
            }
        }
    }

    /**
     * Determine all crossflows for given flow
     * @param flow      Given flow of interest to find cross flows for
     * @param server    Server to start from
     * @return          Set of all cross flows. Empty set if there are none
     * @throws Exception    Parameter error.
     */
    public Set<CBS_Flow> getCrossFlowsAtServer(CBS_Flow flow, CBS_Server server) throws Exception {
        Set<CBS_Flow> setCrossFlows = new HashSet<CBS_Flow>();

        if(!this.flows.contains(flow)) {
            throw new Exception("Flow not added to the server graph");
        }
        else if(!this.servers.contains(server))
        {
            throw new Exception("Server not contained in the server graph");
        }

        /* Simple algorithm: Just check for all flows that are registered in the server graph if they eventually traverse the given server */
        /* If they do check path from that server on - if they part eventually they are cross flows */
        for(CBS_Flow candidate:this.flows)
        {
            if( (flow == candidate) || (candidate.getPriority() != flow.getPriority()) )
            {
                /* Same flow or  mismatching priority - can't be a cross flow */
                continue;
            }

            else
            {
                /* We always assume acyclic paths! so every server can only be in the path once */
                Iterator<CBS_Link> iterFlow = flow.getPath().iterator();
                /* Move iterator to position */
                while(iterFlow.hasNext()){
                    if(server == iterFlow.next().getSource())
                    {
                        break;
                    }
                }

                /* We always assume acyclic paths! so every server can only be in the path once */
                Iterator<CBS_Link> iterCandidate = candidate.getPath().iterator();
                /* Move iterator to position */
                while(iterCandidate.hasNext()){
                    if(server == iterCandidate.next().getSource())
                    {
                        break;
                    }
                }

                if(!iterFlow.hasNext() || !iterCandidate.hasNext())
                {
                    /* This is already the last server so it can't be a cross flow */
                    continue;
                }

                /* Compare rest of the candidates path until they divide or either one ends */
                while(iterCandidate.hasNext() && iterFlow.hasNext())
                {
                    /* Is the outgoing link for both flows the same? */
                    if(iterCandidate.next() != iterFlow.next())
                    {
                        setCrossFlows.add(candidate);
                        break;
                    }
                }

                /* If either one of the paths has at least one successor they are CrossFlows

                 */

            }
        }

        return setCrossFlows;
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
