package org.networkcalculus.dnc.tsn_cbs;

import org.networkcalculus.dnc.curves.ArrivalCurve;
import org.networkcalculus.dnc.curves.Curve;
import org.networkcalculus.dnc.curves.ServiceCurve;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

public class CBS_Queue {

    /**
     * Priority of the queue
     */
    private final int priority;

    /**
     * Minimum credit in bit
     */
    private double minCredit;

    /**
     * Maximum credit in bit
     */
    private double maxCredit;

    /**
     * Rate-Latency ServiceCurve of the queue
     */
    private ServiceCurve serviceCurve;

    /**
     * CBS ShapingCurve of the queue modeled as a Token-Bucket ArrivalCurve
     */
    private ArrivalCurve cbsShapingCurve;

    /**
     * Link shaping curve of the queue modeled as a Token-Bucket ArrivalCurve
     */
    private ArrivalCurve linkShapingCurve;

    /**
     * IdleSlope/Bandwith reservation of the queue in bit/s.
     */
    private final double idleSlope;

    /**
     * SendSlope of the queue in bit/s
     */
    private final double sendSlope;

    /**
     * Bandwidth/capacity of the queues output link
     */
    private final double linkCapacity;

    /**
     * Maximum packet size of the queue in bit
     */
    private double maxPacketSize;

    /**
     * The TSN server the queue is located at.
     */
    private final CBS_Server server;

    /**
     * Set of links over which flows arrive at the queue
     */
    private HashSet<CBS_Link> inputLinks;

    /**
     * Mapping of input links and their corresponding arrival curves with the queues priority
     */
    private Map<CBS_Link, ArrivalCurve> acOfInputLinks;

    /**
     * Constant maximum packet size for BestEffort packets in bit (Ethernet MTU is 1500 Byte = 12kbit + overhead)
     */
    private final double maxPacketSize_BestEffort = 12.336e3;

    /**
     * Aggregated Token-Bucket ArrivalCurve over all reserved flows at this queue.
     * Represents the sum of acOfInputLinks over all links
     */
    private ArrivalCurve aggregateArrivalCurve;

    /**
     * Output link of the queue
     */
    private final CBS_Link outputLink;

    /**
     * Create a new CBS shaped output queue.
     * Calculates min. and max. credit values, ServiceCurve and ShapingCurves.
     * @param flow      The flow traversing this queue
     * @param ac        The TokenBucket ArrivalCurve of the traversing flow
     * @param idleSlope IdleSlope/Bandwidth reservation of the traversing flow in bit/s
     * @param outLink   The output link of the queue
     * @param in_link   Link over which the flow comes into the queue
     */
    public CBS_Queue(CBS_Server server, CBS_Flow flow, ArrivalCurve ac, double idleSlope, CBS_Link outLink, CBS_Link in_link) {
        this.server = server;

        /* New input link */
        this.inputLinks = new HashSet<CBS_Link>();
        this.inputLinks.add(in_link);

        /* Remember arrival curve on input link */
        this.acOfInputLinks = new HashMap<CBS_Link, ArrivalCurve>();
        this.acOfInputLinks.put(in_link, ac);

        this.aggregateArrivalCurve = ac;

        /* Set output link */
        this.outputLink = outLink;
        this.linkCapacity = outLink.getCapacity();

        this.priority = flow.getPriority();

        this.idleSlope = idleSlope;
        this.sendSlope = this.idleSlope - this.linkCapacity;

        this.maxPacketSize = flow.getMfs();
        this.recalculateQueue();
    }

    /**
     * @return  The priority of the queue.
     */
    int getPriority() { return this.priority; };

    /**
     * @return  The ServiceCurve of the queue modeled as a Rate-Latency ServiceCurve
     */
    public ServiceCurve getServiceCurve() {
        return serviceCurve;
    }

    /**
     * Get the Arrival Curve of a specific link that precedes this queue
     * @param inLink  Link of interest
     * @return  Arrival Curve of the link or null if not available.
     */
    public ArrivalCurve getArrivalCurveOfInputLink(CBS_Link inLink) {
        return this.acOfInputLinks.get(inLink);
    }

    /**
     * @return  The Credit-Based-Shaper shaping curve of the queue
     */
    public ArrivalCurve getCbsShapingCurve() {
        return cbsShapingCurve;
    }

    /**
     * @return  The minimal credit of the queue in bit
     */
    public double getMinCredit() {
        return minCredit;
    }

    /**
     * @return  The maximum credit of the queue in bit
     */
    public double getMaxCredit() {
        return maxCredit;
    }

    /**
     * @return  The link shaping curve modeled as a TokenBucket ArrivalCurve
     */
    public ArrivalCurve getLinkShapingCurve() {
        return linkShapingCurve;
    }

    /**
     * @return  The cumulative IdleSlope of the queue in bit/s
     */
    public double getIdleSlope() {
        return idleSlope;
    }

    /**
     * @return  The cumulative SendSlope of the queue in bit/s
     */
    public double getSendSlope() {
        return sendSlope;
    }

    /**
     * @return  The maximum packet size of the queue over all reserved flows
     */
    public double getMaxPacketSize() {
        return maxPacketSize;
    }

    /**
     * Update the queue by a new traversing flow.
     * Recalculates IdleSlope, SendSlope, max. PacketSize, Credits, ServiceCurve and shaping curves
     *
     * @param flow    The new flow that traverses the queue
     * @param ac      The TokenBucket ArrivalCurve of the new flow
     * @param inLink  Input link over which the flow arrives
     */
    public void update(CBS_Flow flow, ArrivalCurve ac, CBS_Link inLink) {

        /* Update AC of given link */
        this.acOfInputLinks.put(inLink, ac);
        this.inputLinks.add(inLink);

        /* Re-Calculate aggregated AC over all incoming links because of the update */
        ArrivalCurve tmp_aggrAc = Curve.getFactory().createZeroArrivals();
        for(CBS_Link l:this.inputLinks)
        {
            tmp_aggrAc = Curve.getUtils().add(this.acOfInputLinks.get(l), tmp_aggrAc);
        }
        this.aggregateArrivalCurve = tmp_aggrAc;

        /* New max. packet size? */
        this.maxPacketSize = Math.max(flow.getMfs(), this.maxPacketSize);

        this.recalculateQueue();
    }

    /**
     * @return Aggregated ArrivalCurve over all reserved flows at this queue
     */
    public ArrivalCurve getAggregateArrivalCurve() {
        return aggregateArrivalCurve;
    }

    /**
     * @return  The output link of the queue
     */
    public CBS_Link getOutputLink() {
        return this.outputLink;
    }

    public HashSet<CBS_Link> getInputLinks() {
        return this.inputLinks;
    }

    /**
     * Update the Credit-Based-Shaper shaping curve.
     * Based on the min. credit, max. credit, max. Packetsize of the queue
     * and the IdleSlope
     */
    private void calculateCBSShapingCurve() {
        double burst = this.getMaxCredit() - this.getMinCredit() + this.maxPacketSize;
        this.cbsShapingCurve = Curve.getFactory().createTokenBucket(this.idleSlope, burst);
    }

    /**
     * Update the minimum Credit of the queue based on the output link capacity, max. packet size and SendSlope
     */
    private void calculateMinCredit() {
        this.minCredit = this.sendSlope * (this.maxPacketSize / this.linkCapacity);
    }

    /**
     * Update the maximum Credit of the queue based on the sum of all min. Credits of all higher priority queues at
     * the server, the BestEffort max. packet size, the sum of all IdleSlopes of all higher priority queues at the
     * server, the output link rate and the IdleSlope of this queue.
     */
    private void calculateMaxCredit() {
        double maxCreditNumerator = 0.0;
        double maxCreditDenominator = 0.0;

        /* Calculate sum of min. credit of all higher priority queues for the same output link */
        LinkedList<CBS_Queue> queues = this.server.getQueuesOfOutputLink(this.getOutputLink());
        for(CBS_Queue q: queues) {
            if(q.getPriority() < this.priority) {
                /* Higher priority queue */
                maxCreditNumerator += q.getMinCredit();
                maxCreditDenominator += q.getIdleSlope();
            }
        }

        maxCreditNumerator -= this.maxPacketSize_BestEffort;
        maxCreditDenominator -= this.linkCapacity;

        this.maxCredit = this.idleSlope * (maxCreditNumerator / maxCreditDenominator);
    }

    /**
     * Update the Rate-Latency ServiceCurve of the queue based on the IdleSlope and max. Credit.
     */
    private void calculateServiceCurve() {
        this.serviceCurve = Curve.getFactory().createRateLatency(this.idleSlope,
                this.maxCredit / this.idleSlope);
    }

    /**
     * Update the Link-Shaping curve of the queue based on the output links capacity/rate and the max. packet
     * size of the queue.
     */
    private void calculateLinkShapingCurve() {
        this.linkShapingCurve = Curve.getFactory().createTokenBucket(this.outputLink.getCapacity(),
                this.maxPacketSize);

    }

    /**
     * Update min. and max. Credit, CBS-ShapingCurve, Link-ShapingCurve and ServiceCurve of the queue
     */
    public void recalculateQueue() {
        this.calculateMinCredit();
        this.calculateMaxCredit();
        this.calculateCBSShapingCurve();
        this.calculateLinkShapingCurve();
        this.calculateServiceCurve();
    }
}
