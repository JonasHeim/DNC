package org.networkcalculus.dnc.tsn_cbs;

import org.networkcalculus.dnc.curves.ArrivalCurve;
import org.networkcalculus.dnc.curves.Curve;
import org.networkcalculus.dnc.curves.ServiceCurve;

import java.util.*;

public class CBS_RateLatency_Server {

    private final String alias;
    /* One Rate-Latency Service Curve for every priority */
    private Map<Integer, ServiceCurve> serviceCurves;

    /* List of queues/priorities */
    private Set<Integer> priorities;

    /* IdleSlope/bandwidth reservation in Bit/s */
    private Map<Integer, Double> idleSlopes;

    /* SendSlope in Bit/s */
    private Map<Integer, Double> sendSlopes;

    /* Maximum CBS credit in Bit */
    private Map<Integer, Double> maxCredit;

    /* Minimum CBS credit in Bit */
    private Map<Integer, Double> minCredit;

    /* Bandwidth capacity of output link in Bit/s */
    private final double linkCapacity;

    /* Maximum packet size at output port in Bit */
    private Map<Integer, Double> maxPacketSize;

    /* For BestEffort packets with lowest priority assume ethernet MTU of 1500 Byte */
    private final double maxPacketSize_BestEffort = 12.0e3;

    /* Link Shaping Curve is modeled as Token-Bucket ArrivalCurve */
    private ArrivalCurve linkShapingCurve;

    /* One CBS shaping curve (modeled as ArrivalCurve) for every priority */
    private Map<Integer, ArrivalCurve> cbsShapingCurves;

    public CBS_RateLatency_Server(String alias, double linkCapacity) {
        this.alias = alias;

        //ToDo: Move to link class
        this.linkCapacity = linkCapacity;

        /* Initialize server with empty CBS queues */
        this.priorities = new TreeSet<Integer>();
        this.serviceCurves = new HashMap<Integer, ServiceCurve>();
        this.idleSlopes = new HashMap<Integer, Double>();
        this.sendSlopes = new HashMap<Integer, Double>();
        this.maxCredit = new HashMap<Integer, Double>();
        this.minCredit = new HashMap<Integer, Double>();
        this.maxPacketSize = new HashMap<Integer, Double>();
        this.cbsShapingCurves = new HashMap<Integer, ArrivalCurve>();

        calculateLinkShapingCurve();
    }

    public boolean addQueue(int priority, double idSlp, double maxPacketSize) {
        boolean retVal = false;

        if ( (idSlp <= this.linkCapacity) && (maxPacketSize >= 0) ) {
            /* Is the priority new? */
            if (this.priorities.add(priority)) {
                //ToDo: check retVal for put()?
                this.idleSlopes.put(priority, idSlp);
                this.sendSlopes.put(priority, idSlp - this.linkCapacity);
                this.maxPacketSize.put(priority, maxPacketSize);

                /* Recalculation required because the max credits changes for lower priorities after adding */
                this.calculateAllLowerCBSCredits(priority);
                this.calculateAllCBSShapingCurves();
                this.calculateLinkShapingCurve();

                this.serviceCurves.put(priority, Curve.getFactory().createRateLatency(this.maxCredit.get(priority) / this.idleSlopes.get(priority), idSlp));
            } else {
                System.out.println("Could not add priority " + priority + " to server.");
            }
        } else {
            System.out.println("Could not add priority. Check parameter");
        }

        return retVal;
    }

    /* Determine the maximum packet size over all lower priority queues */
    private double getLowerPriorityMaxPacketSize(int priority) {
        /* Even if there is no CBS queue we have at least the BestEffort traffic max packet size */
        double maximum = maxPacketSize_BestEffort;

        Iterator<Integer> iter = this.priorities.iterator();
        while (iter.hasNext()) {
            double tmp = iter.next();
            if (tmp < priority) {
                if (tmp > maximum) {
                    maximum = tmp;
                }
            } else {
                /* Only interested in lower priorities */
                break;
            }
        }

        return maximum;
    }

    /* Calculate the sum of minimal credit over all higer priority queues */
    private double getSumHigherPriorityMinCredit(int priority) {
        double sum = 0.0;

        Iterator<Integer> iter = this.priorities.iterator();
        while (iter.hasNext()) {
            int tmp = iter.next();
            if (tmp >= priority) {
                /* Reached limit */
                break;
            } else {
                sum += this.minCredit.get(tmp);
            }
        }

        return sum;
    }

    /* Calculate the sum of idleSlopes over all lower priority queues */
    private double getSumHigherPriorityIdleSlopes(int priority) {
        double sum = 0.0;

        Iterator<Integer> iter = this.priorities.iterator();
        while (iter.hasNext()) {
            int tmp = iter.next();
            if (tmp >= priority) {
                /* Reached limit */
                break;
            } else {
                sum += this.idleSlopes.get(tmp);
            }
        }

        return sum;
    }

    private void calculateAllLowerCBSCredits(int priority) {
        /* Calculate in ascending order beginning with given priority */
        int current_priority = 0;

        Iterator<Integer> iter = this.priorities.iterator();
        // O( n * 2n
        while (iter.hasNext()) {
            current_priority = iter.next();

            /* Lower priorities (higher priority value!) credits must be recalculated */
            if (current_priority >= priority) {
                double tmpCredit = this.sendSlopes.get(current_priority) * (this.maxPacketSize.get(current_priority) / this.linkCapacity);
                this.minCredit.put(current_priority, tmpCredit);

                /* Calculate max credit based on higher priorities O(2n) */
                double maxCredit_numerator = getSumHigherPriorityMinCredit(current_priority) - getLowerPriorityMaxPacketSize(current_priority);
                double maxCredit_denominator = getSumHigherPriorityIdleSlopes(current_priority) - this.linkCapacity;
                tmpCredit = this.idleSlopes.get(current_priority) * (maxCredit_numerator / maxCredit_denominator);
                this.maxCredit.put(current_priority, tmpCredit);
            }
        }
    }

    private void calculateAllCBSShapingCurves() {
        Iterator<Integer> iter = this.priorities.iterator();
        int current_priority = 0;
        while (iter.hasNext()) {
            current_priority = iter.next();
            /* HashMap.put() replaces element (CBS shaping curve) if already existing */
            double burst = this.maxCredit.get(current_priority) - this.minCredit.get(current_priority);
            this.cbsShapingCurves.put(current_priority, Curve.getFactory().createTokenBucket(this.idleSlopes.get(current_priority), burst));
        }
    }

    private void calculateLinkShapingCurve() {
        double maxBurst = 0.0;

        /* Determine maximum packet size over all priorities */
        for (int priority : this.priorities) {
            if (maxPacketSize.get(priority) > maxBurst) {
                maxBurst = maxPacketSize.get(priority);
            }
        }

        this.linkShapingCurve = Curve.getFactory().createTokenBucket(this.linkCapacity, maxBurst);
    }

    public String getAlias() {
        return this.alias;
    }

    public String toString() {
        StringBuffer cbs_rl_server_str = new StringBuffer();

        cbs_rl_server_str.append("\r\nCBS Rate-Latency server \"" + this.alias + "\" with " + this.priorities.size() + " CBS queues, link capacity " + this.linkCapacity + " Bit/s and link shaping curve " + this.linkShapingCurve);
        for(int priority:this.priorities) {
            cbs_rl_server_str.append("\r\n\tPriority " + priority);
            cbs_rl_server_str.append("\r\n\t\tmax. PacketSize " + this.maxPacketSize.get(priority) + " Bit");
            cbs_rl_server_str.append("\r\n\t\tidSlp " + this.idleSlopes.get(priority) + " Bit/s sdSlp " + this.sendSlopes.get(priority) + " Bit/s");
            cbs_rl_server_str.append("\r\n\t\tminCredit " + this.minCredit.get(priority) + " Bit maxCredit " + this.maxCredit.get(priority) + " Bit");
            cbs_rl_server_str.append("\r\n\t\tCBS-ServiceCurve " + this.serviceCurves.get(priority));
            cbs_rl_server_str.append("\r\n\t\tCBS-ShapingCurve " + this.cbsShapingCurves.get(priority));
        }
        cbs_rl_server_str.append("\r\n");

        return cbs_rl_server_str.toString();
    }
}
