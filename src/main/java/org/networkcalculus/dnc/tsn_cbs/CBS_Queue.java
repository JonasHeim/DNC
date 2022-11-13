package org.networkcalculus.dnc.tsn_cbs;

import org.networkcalculus.dnc.curves.ArrivalCurve;
import org.networkcalculus.dnc.curves.Curve;
import org.networkcalculus.dnc.curves.ServiceCurve;

import java.util.LinkedList;

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
     * Cumulated IdleSlope/Bandwith reservation of the queue in bit/s
     */
    private final double idleSlope;

    /**
     * Cumulated SendSlope of the queue in bit/s
     */
    private final double sendSlope;

    /**
     * Bandwidth/capacity of the queues output link
     */
    private final double linkCapacity;

    /**
     * Maximum packet size of the queue in bit
     */
    //ToDo: is this always Best effort packet size of the maximum over all reserved flows?
    private double maxPacketSize;

    /**
     * Constant maximum packet size for BestEffort packets in bit (Ethernet MTU is 1500 Byte = 12kbit)
     *
     */
    private final double maxPacketSize_BestEffort = 12.0e3;

    /**
     * Aggregated Token-Bucket ArrivalCurve over all reserved flows at this queue
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
     * @param link      The output link of the queue
     */
    public CBS_Queue(CBS_Flow flow, ArrivalCurve ac, double idleSlope, CBS_Link link) {
        this.outputLink = link;
        this.priority = flow.getPriority();
        this.linkCapacity = link.getCapacity();
        this.idleSlope = idleSlope;
        this.sendSlope = this.idleSlope - this.linkCapacity;

        this.aggregateArrivalCurve = ac;
        this.maxPacketSize = flow.getMfs();
        this.recalculateQueue();
    }

    /**
     * @return  The priority of the queue.
     */
    int getPriority() { return this.priority; };

    /**
     * @return  The ServiceCurve of the queue modeled as a RateLatency ServiceCurve
     */
    public ServiceCurve getServiceCurve() {
        return serviceCurve;
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
    //ToDo: what about the BestEffort max. packet size here?
    public double getMaxPacketSize() {
        return maxPacketSize;
    }

    /**
     * Update the queue by a new traversing flow.
     * Changes IdleSlope, SendSlope, max. PacketSize, Credits, ServiceCurve and shaping curves
     * @param flow          The new flow that traverses the queue
     * @param ac            The TokenBucket ArrivalCurve of the new flow
     */
    public void update(CBS_Flow flow, ArrivalCurve ac) {
        this.aggregateArrivalCurve = Curve.getUtils().add(this.aggregateArrivalCurve, ac);
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

    /**
     * Update the Credit-Based-Shaper shaping curve based on the min. credit, max. credit and IdleSlope
     */
    private void calculateCBSShapingCurve() {
        double burst = this.getMaxCredit() - this.getMinCredit();
        this.cbsShapingCurve = Curve.getFactory().createTokenBucket(this.idleSlope, burst);
    }

    /**
     * Update the minimum Credit of the queue based on the output link capacity, max. packet size and SendSlope
     */
    private void calculateMinCredit() {
        this.minCredit = this.sendSlope * (this.maxPacketSize / this.linkCapacity);
    }

    /**
     * Update the maximum Credit of the queue based on the sum of all min. Creidts of all lower priority queues at
     * the server, the BestEffort max. packet size, the sum of all IdleSlopes of all lower priority queues at the
     * server, the output link rate/capacity and the IdleSlope of the queue.
     */
    private void calculateMaxCredit() {
        double maxCreditNumerator = 0.0;
        double maxCreditDenominator = 0.0;

        /* Calculate sum of min. credit of all higher priority queues for the same output link */
        LinkedList<CBS_Queue> queues = this.getOutputLink().getSource().getQueuesOfOutputLink(this.getOutputLink());
        for(CBS_Queue q: queues) {
            if(q.getPriority() < this.priority) {
                /* Add min. Credit and idleSlope of higher priority queue */
                maxCreditNumerator += q.getMinCredit();
                maxCreditDenominator += q.getIdleSlope();
            }
        }

        maxCreditNumerator -= this.maxPacketSize_BestEffort;
        maxCreditDenominator -= this.linkCapacity;

        this.maxCredit = this.idleSlope * (maxCreditNumerator / maxCreditDenominator );
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
                this.outputLink.getMaxPacketSize());
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
