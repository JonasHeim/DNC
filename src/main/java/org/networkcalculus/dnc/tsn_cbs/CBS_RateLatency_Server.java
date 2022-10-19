package org.networkcalculus.dnc.tsn_cbs;

import org.networkcalculus.dnc.curves.ArrivalCurve;
import org.networkcalculus.dnc.curves.Curve;
import org.networkcalculus.dnc.curves.ServiceCurve;
import org.networkcalculus.dnc.model.Link;

import java.util.*;

public class CBS_RateLatency_Server {

    public SRV_TYPE getServerType() {
        return serverType;
    }

    private class CBS_Queue {

        private final int priority;

        private double minCredit;
        private double maxCredit;

        private ServiceCurve serviceCurve;

        private ArrivalCurve cbsShapingCurves;

        /* Link Shaping Curve is modeled as Token-Bucket ArrivalCurve */
        private ArrivalCurve linkShapingCurve;

        /* IdleSlope/bandwidth reservation in Bit/s */
        private double idleSlope;

        /* SendSlope in Bit/s */
        private double sendSlope;

        /* Bandwidth capacity of output link in Bit/s */
        private final double linkCapacity;

        /* Maximum packet size of output port in Bit */
        private double maxPacketSize;

        private final CBS_RateLatency_Server nextHop;

        /* For BestEffort packets with lowest priority assume ethernet MTU of 1500 Byte */
        private final double maxPacketSize_BestEffort = 12.0e3;

        private ArrivalCurve aggregateArrivalCurve;


        private final CBS_Link outputLink;

        public CBS_Queue(CBS_TokenBucket_Flow flow, ArrivalCurve ac, double idleSlope, CBS_Link link) {
            this.outputLink = link;
            this.priority = flow.getPriority();
            this.linkCapacity = link.getCapacity();
            this.nextHop = link.getDestination();
            this.aggregateArrivalCurve = ac;

            this.maxPacketSize = flow.getMfs();

            this.idleSlope = idleSlope;
            this.sendSlope = this.idleSlope - this.linkCapacity;

            /* Calculate min. and max. Credits */
            this.minCredit = this.sendSlope * (this.maxPacketSize / this.linkCapacity);


            double maxCreditNumerator = 0.0 - this.maxPacketSize_BestEffort;
            double maxCreditDenominator = 0.0 - this.linkCapacity;

            /* Calculate sum of min. credit of all higher priority queues for the same output link */
            LinkedList<CBS_Queue> queues = link.getSource().getQueuesOfOutputLink(link);
            if(!queues.isEmpty()) {
                for (CBS_Queue q : queues) {
                    if (q.getPriority() < this.priority) {
                        /* Add min. Credit and idleSlope of higher priority queue */
                        maxCreditNumerator += q.getMinCredit();
                        maxCreditDenominator += q.getIdleSlope();
                    }
                }
            }

            this.maxCredit = this.idleSlope * (maxCreditNumerator / maxCreditDenominator );

            this.serviceCurve = Curve.getFactory().createRateLatency(this.idleSlope, this.maxCredit / this.idleSlope);
        }
        int getPriority() { return this.priority; };

        public ServiceCurve getServiceCurve() {
            return serviceCurve;
        }

        public ArrivalCurve getCbsShapingCurves() {
            return cbsShapingCurves;
        }

        public double getMinCredit() {
            return minCredit;
        }

        public double getMaxCredit() {
            return maxCredit;
        }

        public ArrivalCurve getLinkShapingCurve() {
            return linkShapingCurve;
        }

        public double getIdleSlope() {
            return idleSlope;
        }

        public double getSendSlope() {
            return sendSlope;
        }

        public double getLinkCapacity() {
            return linkCapacity;
        }

        public double getMaxPacketSize() {
            return maxPacketSize;
        }

        public CBS_RateLatency_Server getNextHop() {
            return nextHop;
        }

        public void update(CBS_TokenBucket_Flow flow, ArrivalCurve ac, double idleSlope, CBS_Link link) {
            //ToDo: implement
            //UPdate/recalculate all required metrics of the queue

            /* Add ArrivalCurve to aggregated ArrivalCurve of the queue */
            this.aggregateArrivalCurve = Curve.getUtils().add(this.aggregateArrivalCurve, ac);

            /* New max. packet size? */
            this.maxPacketSize = flow.getMfs() > this.maxPacketSize ? flow.getMfs() : this.maxPacketSize;

            /* IdleSlope increases */
            this.idleSlope += idleSlope;
            this.sendSlope += idleSlope;

            /* Recalculate min. and max. Credits */
            this.minCredit = this.sendSlope * (this.maxPacketSize / this.linkCapacity);

            this.recalculateMaxCredit();

            this.recalculateServiceCurve();
        }

        public ArrivalCurve getAggregateArrivalCurve() {
            return aggregateArrivalCurve;
        }

        private void recalculateMaxCredit() {
            double maxCreditNumerator = 0.0 - this.maxPacketSize_BestEffort;
            double maxCreditDenominator = 0.0 - this.linkCapacity;

            /* Calculate sum of min. credit of all higher priority queues for the same output link */
            LinkedList<CBS_Queue> queues = this.getOutputLink().getSource().getQueuesOfOutputLink(this.getOutputLink());
            for(CBS_Queue q: queues) {
                if(q.getPriority() < this.priority) {
                    /* Add min. Credit and idleSlope of higher priority queue */
                    maxCreditNumerator += q.getMinCredit();
                    maxCreditDenominator += q.getIdleSlope();
                }
            }

            this.maxCredit = this.idleSlope * (maxCreditNumerator / maxCreditDenominator );
        }

        private void recalculateServiceCurve() {
            this.serviceCurve = Curve.getFactory().createRateLatency(this.idleSlope, this.maxCredit / this.idleSlope);
        }

        public void recalculateQueue() {
            /* Recalculate max. Credit of the Queue */
            this.recalculateMaxCredit();
            /* Recalculate CBS Shaping Curve */
            //ToDo: implement
            /* Recalculate Service Curve */
            this.recalculateServiceCurve();
        }

        public CBS_Link getOutputLink() {
            return outputLink;
        }
    }

    public enum SRV_TYPE {
        TALKER,
        SWITCH,
        LISTENER
    }

    private final SRV_TYPE serverType;

    private final String alias;

    /* List of queues/priorities */
    private Set<Integer> priorities;


    private Map<Integer, Map<CBS_Link, CBS_Queue>> mapping_priorities_to_queues;

    public CBS_RateLatency_Server(String alias, SRV_TYPE serverType) {
        this.alias = alias;
        this.serverType = serverType;

        /* Initialize server with empty CBS queues */
        this.priorities = new TreeSet<Integer>();
        this.mapping_priorities_to_queues = new HashMap<Integer, Map<CBS_Link, CBS_Queue>>();

        //ToDo: whats with this?
        //calculateLinkShapingCurve();
    }

    /**
     * Add a flow to the server. Updates internal CBS Queue for flows priority and given output link.
     * @param flow      Flow to be added to server
     * @param idleSlp   IdleSlope of the flow in bit/s
     * @param link      Output link to next hop
     * @param ac        ArrivalCurve of the flow from previous server
     */
    public void addFlow(CBS_TokenBucket_Flow flow, ArrivalCurve ac, double idleSlp, CBS_Link link) {
        //ToDo: Arguments OK?

        //ToDo Implement

        int priority = flow.getPriority();

        /* Does the queue for given priority and output link already exist */
        if(this.mapping_priorities_to_queues.containsKey(priority)) {
            /* Does the queue for given link already exist? */
            if(this.mapping_priorities_to_queues.get(priority).containsKey(link)) {
                //Update existing queue
                System.out.println("CBS_Server.addFlow - Update queue for priority " + priority);
                this.mapping_priorities_to_queues.get(priority).get(link).update(flow, ac, idleSlp, link);
            }
            else {
                //Create new CBS queue
                System.out.println("CBS_Server.addFlow - Creating new queue for priority " + priority);
                CBS_Queue queue = new CBS_Queue(flow, ac, idleSlp, link);
                this.mapping_priorities_to_queues.get(priority).put(link, queue);
            }
        }
        else {
            /* No queue for priority exists yet so we will create one */
            System.out.println("CBS_Server.addFlow - Creating the first queue for priority " + priority);
            CBS_Queue queue = new CBS_Queue(flow, ac, idleSlp, link);
            HashMap<CBS_Link, CBS_Queue> hashMap = new HashMap<>();
            hashMap.put(link, queue);

            this.mapping_priorities_to_queues.put(priority, hashMap);
        }

        //ToDo: update all lower priority queues of the server because the credits, etc. will change
        this.updateAllQueuesLowerPrio(priority, link);
    }

    public void updateAllQueuesLowerPrio(int priority, CBS_Link link) {
        LinkedList<CBS_Queue> listQueues = this.getQueuesOfOutputLink(link);
        for(CBS_Queue queue:listQueues) {
            if(priority < queue.getPriority()) {
                /* Lower priority queue */
                queue.recalculateQueue();
            }
        }
    }

    public boolean addQueue(int priority, double idSlp, double maxPacketSize, CBS_Link link) {
        boolean retVal = false;



        return retVal;
    }

    public LinkedList<CBS_Queue> getQueuesOfOutputLink(CBS_Link link) {
        LinkedList<CBS_Queue> list = new LinkedList<CBS_Queue>();

        /*
            Get all queues for priority that have the same output link.
            There can be only one queue per priority.
         */
        for(int priority: this.mapping_priorities_to_queues.keySet()) {
            list.add(this.mapping_priorities_to_queues.get(priority).get(link));
        }

        return list;
    }


    /* Calculate the sum of idleSlopes over all lower priority queues */
    private double getSumHigherPriorityIdleSlopes(int priority) {
        double sum = 0.0;

        //ToDo: Rework
//        Iterator<Integer> iter = this.priorities.iterator();
//        while (iter.hasNext()) {
//            int tmp = iter.next();
//            if (tmp >= priority) {
//                /* Reached limit */
//                break;
//            } else {
//                sum += this.idleSlopes.get(tmp);
//            }
//        }

        return sum;
    }

    private void calculateAllLowerCBSCredits(int priority) {
        /* Calculate in ascending order beginning with given priority */
        int current_priority = 0;

        //ToDo: Rework
//        Iterator<Integer> iter = this.priorities.iterator();
//        // O( n * 2n
//        while (iter.hasNext()) {
//            current_priority = iter.next();
//
//            /* Lower priorities (higher priority value!) credits must be recalculated */
//            if (current_priority >= priority) {
//                double tmpCredit = this.sendSlopes.get(current_priority) * (this.maxPacketSize.get(current_priority) / this.linkCapacity);
//                this.minCredit.put(current_priority, tmpCredit);
//
//                /* Calculate max credit based on higher priorities O(2n) */
//                double maxCredit_numerator = getSumHigherPriorityMinCredit(current_priority) - getLowerPriorityMaxPacketSize(current_priority);
//                double maxCredit_denominator = getSumHigherPriorityIdleSlopes(current_priority) - this.linkCapacity;
//                tmpCredit = this.idleSlopes.get(current_priority) * (maxCredit_numerator / maxCredit_denominator);
//                this.maxCredit.put(current_priority, tmpCredit);
//            }
//        }
    }

    private void calculateAllCBSShapingCurves() {
        Iterator<Integer> iter = this.priorities.iterator();
        int current_priority = 0;

        //ToDo: Rework
//        while (iter.hasNext()) {
//            current_priority = iter.next();
//            /* HashMap.put() replaces element (CBS shaping curve) if already existing */
//            double burst = this.maxCredit.get(current_priority) - this.minCredit.get(current_priority);
//            this.cbsShapingCurves.put(current_priority, Curve.getFactory().createTokenBucket(this.idleSlopes.get(current_priority), burst));
//        }
    }

    private void calculateLinkShapingCurve() {
        double maxBurst = 0.0;

        //ToDo: Rework
//        /* Determine maximum packet size over all priorities */
//        for (int priority : this.priorities) {
//            if (maxPacketSize.get(priority) > maxBurst) {
//                maxBurst = maxPacketSize.get(priority);
//            }
//        }
//
//        this.linkShapingCurve = Curve.getFactory().createTokenBucket(this.linkCapacity, maxBurst);
    }

    public String getAlias() {
        return this.alias;
    }

    public String toString() {
        StringBuffer cbs_rl_server_str = new StringBuffer();

        cbs_rl_server_str.append("CBS Rate-Latency server \"" + this.alias);
        for(int priority:this.priorities) {
            cbs_rl_server_str.append("\r\n\tCBS queues for priority " + priority);
            Map<CBS_Link, CBS_Queue> queues = this.mapping_priorities_to_queues.get(priority);
            for (CBS_Queue queue: queues.values()) {
                cbs_rl_server_str.append("\r\n\t\tmax. PacketSize " + queue.getMaxPacketSize() + " Bit");
                cbs_rl_server_str.append("\r\n\t\tidSlp " + queue.getIdleSlope() + " Bit/s sdSlp " + queue.getSendSlope() + " Bit/s");
                cbs_rl_server_str.append("\r\n\t\tminCredit " + queue.getMinCredit() + " Bit maxCredit " + queue.getMaxCredit() + " Bit");
                cbs_rl_server_str.append("\r\n\t\tCBS-ServiceCurve " + queue.getServiceCurve());
                cbs_rl_server_str.append("\r\n\t\tCBS-ShapingCurve " + queue.getCbsShapingCurves());
            }
            cbs_rl_server_str.append("\r\n");
        }
        return cbs_rl_server_str.toString();
    }
}
