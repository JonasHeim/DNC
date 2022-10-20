package org.networkcalculus.dnc.tsn_cbs;

import org.networkcalculus.dnc.curves.ArrivalCurve;
import org.networkcalculus.dnc.curves.Curve;

/**
 * Class representation of the Credit-Based shaped flow with Token-Bucket ArrivalCurve
 */
public class CBS_Flow {
    /**
     * Periodicity type of the flow
     */
    public enum Periodicity {
        PERIODIC,
        APERIODIC
    }

    /**
     * String identifier of the flow
     */
    private final String alias;

    /**
     * Token-Bucket arrival curve of the flow
     */
    private ArrivalCurve arrivalCurve;

    /**
     * TSN Class Measurement Interval in seconds
     */
    private final double cmi;

    /**
     * TSN Maximum Frame Size in Bit
     */
    private final int mfs;

    /**
     * TSN Maximum Interval Frames (max. number of frames sent per CMI)
     */
    private final int mif;

    /**
     * TSN flow priority (0 is highest priority)
     */
    private final int priority;

    /**
     * Maximum data in bit that the flow can send in one CMI
     */
    private final double max_data_per_CMI;

    /**
     * Is the flow periodic?
     */
    private final boolean is_periodic;

    /**
     * @param alias         String representation of the flow
     * @param cmi           TSN Class Measurement Interval
     * @param mfs           TSN Max Frame Size
     * @param mif           TSN Max Interval Frame
     * @param priority      Flow priority
     * @param periodicity   Periodicity of the flow
     */
    public CBS_Flow(String alias, double cmi, int mfs, int mif, int priority, Periodicity periodicity) {
        this.alias = alias;

        this.cmi = cmi;
        this.mfs = mfs;
        this.mif = mif;
        this.priority = priority;
        this.is_periodic = periodicity == Periodicity.PERIODIC;

        /* Calculate m */
        this.max_data_per_CMI = this.mfs * this.mif;
    }

    /**
     * @return ArrivalCurve of the flow
     */
    public ArrivalCurve getArrivalCurve() {
        return arrivalCurve;
    }

    /**
     * @return TSN Class Measurement Interval in s
     */
    public double getCmi() {
        return cmi;
    }

    /**
     * @return TSN Max Frame Size
     */
    public double getMfs() {
        return mfs;
    }

    /**
     * @return TSN Max Interval Frame
     */
    public double getMif() {
        return mif;
    }

    /**
     * @return Flows priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @return TSN Max data per CMI
     */
    public double getMax_data_per_CMI() {
        return max_data_per_CMI;
    }

    /**
     * @return True if the flow is a periodic TSN flow
     */
    public boolean isPeriodic() {
        return is_periodic;
    }

    /**
     * @return String identification of the flow
     */
    public String getAlias() { return this.alias; }

    /**
     * @return String representation of the flow
     */
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

    /**
     * Calculate and set the TokenBucket ArrivalCurve for a given link capacity
     * @param linkCapacity  Link capacity of the initial link of the flow at the talker
     * @return              Initial TokenBucket ArrivalCurve at the Talker
     */
    public ArrivalCurve calculateAndSetAC(double linkCapacity) {
        /* Create Token Bucket arrival curve */
        double tb_rate = this.max_data_per_CMI / this.cmi;
        double burst_factor = this.is_periodic ? 1.0 : 2.0;
        double tb_burst = burst_factor * this.max_data_per_CMI * (1.0 - (tb_rate / linkCapacity));
        this.arrivalCurve = Curve.getFactory().createTokenBucket(tb_rate, tb_burst);

        return this.arrivalCurve;
    }

}
