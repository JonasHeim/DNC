package org.networkcalculus.dnc.tsn_cbs;

import org.networkcalculus.dnc.curves.ArrivalCurve;
import java.util.*;

/**
 * Class representation of a server that has Credit-Based shaped output queues
 */
public class CBS_Server {

    /**
     * Get all queues of this server over all priorities and output links
     * @return HashSet<CBS_Queue> of queues. Empty Set if no queues are present.
     */
    public Set<CBS_Queue> getQueues() {
        Set<CBS_Queue> queues = new HashSet<>();

        /* Merge all queues from all priorities and output links */
        for(Integer prioCandidate:this.mapping_priorities_to_queues.keySet())
        {
            for(CBS_Link linkCandidate:this.mapping_priorities_to_queues.get(prioCandidate).keySet())
            {
                queues.add(this.mapping_priorities_to_queues.get(prioCandidate).get(linkCandidate));
            }
        }

        return queues;
    }

    /**
     * Possible TSN server types
     */
    public enum SRV_TYPE {
        TALKER,
        SWITCH,
        LISTENER
    }

    /**
     * TSN server type
     */
    private final SRV_TYPE serverType;

    /**
     * String identification of the server
     */
    private final String alias;

    /**
     * Set of priorities that this server supports. Each priority has at least one output queue.
     */
    private final Set<Integer> priorities;

    /**
     * Mapping of priorities to a mapping of output links to output queues
     */
    private final Map<Integer, Map<CBS_Link, CBS_Queue>> mapping_priorities_to_queues;

    /**
     * Mapping of priorities to their idleSlope in bit/s */
     */
    private final Map<Integer, Double> idleSlopeMapping;

    /**
     * Create a new, empty server
     * @param alias         String identification
     * @param serverType    TSN server type
     */
    public CBS_Server(String alias, SRV_TYPE serverType, Map<Integer, Double> idleSlopes) {
        this.alias = alias;
        this.serverType = serverType;

        /* Initialize server with empty CBS queues */
        this.priorities = new TreeSet<Integer>();
        this.mapping_priorities_to_queues = new TreeMap<Integer, Map<CBS_Link, CBS_Queue>>();
        this.idleSlopeMapping = idleSlopes;
    }

    /**
     * Reset all CBS queues and registered priorities of this servers
     */
    public void resetCBSQueues() {

        this.mapping_priorities_to_queues.clear();
        this.priorities.clear();
    }

    /**
     * @return  TSN server type of this server
     */
    public SRV_TYPE getServerType() {
        return serverType;
    }

    /**
     * Gets specific output queue for given priority and output link
     * @param priority  Queue priority
     * @param link      Output link
     * @return          Matching CBS output queue or null if no queue found
     */
    public CBS_Queue getQueue(int priority, CBS_Link link) {
        return this.mapping_priorities_to_queues.get(priority).get(link);
    }

    /**
     * Add a flow to the server.
     * Updates internal CBS Queue for flows priority and given output link.
     * After adding or updating the queue all lower priority queues for the given
     * output link will recalculate their values (credits, ServiceCurve, ...).
     * @param flow      Flow to be added to server
     * @param ac        Shaped ArrivalCurve of the flow from previous server
     * @param link_out  Output link to next hop
     * @param link_in   Input link from previous hop
     */
    public void addFlow(CBS_Flow flow, ArrivalCurve ac, CBS_Link link_out, CBS_Link link_in) throws Exception {
        //ToDo: Arguments OK?
        int priority = flow.getPriority();
        this.priorities.add(priority);
        double idleSlp = this.idleSlopeMapping.get(priority);

        /* We must distinguish between the following cases:
            1. No output queue for given priority available yet
            2. There is at least one output queue for given priority but not for given output link
            3. There exists a output queue for given priority and output link.
         */

        if(this.mapping_priorities_to_queues.containsKey(priority)) {
            if(this.mapping_priorities_to_queues.get(priority).containsKey(link_out)) {
                /* Case 3: Update existing queue */
                System.out.println("CBS_Server.addFlow - Update queue for priority " + priority + " at server "
                        + this.getAlias());

                this.mapping_priorities_to_queues.get(priority).get(link_out).update(flow, ac, link_in);
            }
            else {
                /* Case 2: Create new CBS queue */
                System.out.println("CBS_Server.addFlow - Creating new queue for priority " + priority + " at server "
                        + this.getAlias());

                CBS_Queue queue = new CBS_Queue(this, flow, ac, idleSlp, link_out, link_in);
                this.mapping_priorities_to_queues.get(priority).put(link_out, queue);
            }
        }
        else {
            /* Case 1: No queue for priority exists yet so we will create one */
            System.out.println("CBS_Server.addFlow - Creating the first queue for priority " + priority + " at server "
                    + this.getAlias() + " for out link " + link_out.getAlias());

            CBS_Queue queue = new CBS_Queue(this, flow, ac, idleSlp, link_out, link_in);

            LinkedHashMap <CBS_Link, CBS_Queue> hashMap = new LinkedHashMap <>();
            hashMap.put(link_out, queue);
            this.mapping_priorities_to_queues.put(priority, hashMap);
        }

        /* All lower priority queues of the server must recalculate their values
         * because the credits, etc. will change if a higher priority queue was added or updated
         */
        this.updateAllQueuesLowerPriority(priority, link_out);
    }

    /**
     * Recalculate the values of all lower priority queues for given output link
     * @param priorityLimit     Upper (exclusive) priority limit for queues to recalculate.
     * @param link              Output link used to get all possible queue candidates.
     */
    public void updateAllQueuesLowerPriority(int priorityLimit, CBS_Link link) {
        LinkedList<CBS_Queue> listQueues = this.getQueuesOfOutputLink(link);
        for(CBS_Queue queue:listQueues) {
            if(priorityLimit < queue.getPriority()) {
                queue.recalculateQueue();
            }
        }
    }

    /**
     * Get all queues for given output link regardless of their priority.
     * @param link  Output link
     * @return  List of matching queues or null if no queues found.
     *          List is sorted by priority (high (0) -> low)
     */
    public LinkedList<CBS_Queue> getQueuesOfOutputLink(CBS_Link link) {
        LinkedList<CBS_Queue> list = new LinkedList<CBS_Queue>();

        for(int priority: this.mapping_priorities_to_queues.keySet()) {
            CBS_Queue outputQueue = this.mapping_priorities_to_queues.get(priority).get(link);
            if(null != outputQueue) {
                list.add(outputQueue);
            }
        }

        return list;
    }

    /**
     * @return String identification of the server.
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * @return String representation of the server to print out.
     */
    public String toString() {
        StringBuffer cbs_rl_server_str = new StringBuffer();

        cbs_rl_server_str.append("CBS Rate-Latency server \"" + this.alias + "\r\n");
        for(int priority:this.priorities) {
            cbs_rl_server_str.append("\r\n\tCBS queues for priority " + priority);
            Map<CBS_Link, CBS_Queue> queues = this.mapping_priorities_to_queues.get(priority);
            for (CBS_Queue queue: queues.values()) {
                cbs_rl_server_str.append("\r\n\t\tQueue:");
                cbs_rl_server_str.append("\r\n\t\t\tOutLink" + queue.getOutputLink());
                for(CBS_Link l:queue.getInputLinks())
                {
                    cbs_rl_server_str.append("\r\n\t\t\tInLink" + l);
                }
                cbs_rl_server_str.append("\r\n\t\t\tmax. PacketSize " + queue.getMaxPacketSize() + " Bit");
                cbs_rl_server_str.append("\r\n\t\t\tidSlp " + queue.getIdleSlope() + " Bit/s sdSlp " +
                        queue.getSendSlope() + " Bit/s");
                cbs_rl_server_str.append("\r\n\t\t\tminCredit " + queue.getMinCredit() + " Bit maxCredit " +
                        queue.getMaxCredit() + " Bit");
                cbs_rl_server_str.append("\r\n\t\t\tServiceCurve " + queue.getServiceCurve());
                cbs_rl_server_str.append("\r\n\t\t\tCBS-ShapingCurve " + queue.getCbsShapingCurve());
                cbs_rl_server_str.append("\r\n\t\t\tLink ShapingCurve " + queue.getLinkShapingCurve());
            }
            cbs_rl_server_str.append("\r\n");
        }
        return cbs_rl_server_str.toString();
    }
}
