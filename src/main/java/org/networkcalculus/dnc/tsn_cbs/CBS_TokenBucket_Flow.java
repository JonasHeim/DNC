package org.networkcalculus.dnc.tsn_cbs;

import org.networkcalculus.dnc.curves.ArrivalCurve;
import org.networkcalculus.dnc.curves.Curve;

public class CBS_TokenBucket_Flow {
    public enum Periodicity {
        PERIODIC,
        APERIODIC
    }

    /* Identifier */
    private final String alias;

    /* Token Bucket arrival curve */
    private final ArrivalCurve arrivalCurve;

    /* TSN Class Measurement Interval in seconds */
    private final double cmi;

    /* TSN Maximum Frame Size in Bit */
    private final int mfs;

    /* TSN Maximum Interval Frames per CMI */
    private final int mif;

    /* TSN flow priority/class (0 is highest priority) */
    private final int priority;

    /* Maximum data in Bit that the flow can send in one CMI */
    private final double max_data_per_CMI;

    /* Periodic flow? */
    private final boolean is_periodic;

    /* Link capacity of flows talker in bit/s */
    private final double talker_link_capacity;

    public CBS_TokenBucket_Flow(String alias, double cmi, int mfs, int mif, int priority, double talker_link_capacity, Periodicity periodicity) {
        this.alias = alias;

        this.cmi = cmi;
        this.mfs = mfs;
        this.mif = mif;
        this.priority = priority;
        this.talker_link_capacity = talker_link_capacity;
        this.is_periodic = periodicity == Periodicity.PERIODIC;

        /* Calculate m */
        this.max_data_per_CMI = this.mfs * this.mif;

        /* Create Token Bucket arrival curve */
        double tb_rate = this.max_data_per_CMI / this.cmi;
        double burst_factor = this.is_periodic ? 1.0 : 2.0;
        double tb_burst = burst_factor * this.max_data_per_CMI * (1.0 - (tb_rate / talker_link_capacity));
        this.arrivalCurve = Curve.getFactory().createTokenBucket(tb_rate, tb_burst);
    }

    public ArrivalCurve getArrivalCurve() {
        return arrivalCurve;
    }

    public double getCmi() {
        return cmi;
    }

    public double getMfs() {
        return mfs;
    }

    public double getMif() {
        return mif;
    }

    public int getPriority() {
        return priority;
    }

    public double getMax_data_per_CMI() {
        return max_data_per_CMI;
    }

    public boolean isPeriodic() {
        return is_periodic;
    }

    public String getAlias() { return this.alias; }

    public String toString() {
        StringBuffer cbs_tb_flow_str = new StringBuffer();

        if (is_periodic) {
            cbs_tb_flow_str.append("Periodic ");
        } else {
            cbs_tb_flow_str.append("Aperiodic ");
        }
        cbs_tb_flow_str.append("CBS Token-Bucket Flow \"" + this.alias + "\" with TSpec: Priority " + this.priority);
        cbs_tb_flow_str.append(", CMI " + this.cmi);
        cbs_tb_flow_str.append("s, MFS " + this.mfs);
        cbs_tb_flow_str.append(" Bit., MIF " + this.mif);
        cbs_tb_flow_str.append(" pkts., Arrival Curve " + this.arrivalCurve);

        cbs_tb_flow_str.append("\r\n");


        return cbs_tb_flow_str.toString();
    }
}
