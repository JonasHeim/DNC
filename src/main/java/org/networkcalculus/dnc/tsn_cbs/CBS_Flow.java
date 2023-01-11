package org.networkcalculus.dnc.tsn_cbs;

import org.networkcalculus.dnc.curves.ArrivalCurve;
import org.networkcalculus.dnc.curves.Curve;

import java.util.LinkedList;

/**
 * Class representation of the Credit-Based shaped flow with Token-Bucket ArrivalCurve
 */
public class CBS_Flow {

    private LinkedList<CBS_Link> path;

    /**
     * Periodicity type of the flow
     */
    public enum Periodicity {
        PERIODIC,
        APERIODIC
    }

    private Periodicity periodicity;

    /**
     * Number of bytes that are added to the number of payload bytes and make up
     * a 802.1Q tagged ethernet frame.
     * Includes:
     *  Preamble (7 Byte), Start frame delimiter (1 Byte),
     *  Dst. MAC (6 Byte), Src. MAC (6 Byte),
     *  802.1Q tag (4 Byte), Ethertype (2 Byte),
     *  CRC (4 Byte) and IPG (12 Byte)
     *
     */
    private final int ethFrameOverhead = 42 * 8;

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
     * @param cmi           TSN Class Measurement Interval [s]
     * @param mfs           TSN Max Frame Size [Byte]
     *                      Only lenght of the payload. No support of jumbo frames.
     *                      Min. : 46 Byte
     *                      Max. : 1500 Byte
     *                      Preamble (7 Byte), start frame delim. (1 Byte),
     *                      Src. MAC (6 Byte), Dst. MAC (6 Byte),
     *                      802.1Q tag (4 Byte), ethertype (2 Byte),
     *                      CRC (4 Byte) and IPG (12 Byte) will be added.
     * @param mif           TSN Max Interval Frame
     * @param priority      Flow priority [0 - ...]
     * @param periodicity   Periodicity of the flow
     */
    public CBS_Flow(String alias, double cmi, int mfs, int mif, int priority, Periodicity periodicity) {
        this.alias = alias;

        this.path = new LinkedList<CBS_Link>();

        this.cmi = cmi;
        this.mfs = mfs + ethFrameOverhead;
        this.mif = mif;
        this.priority = priority;
        this.periodicity = periodicity;
        this.is_periodic = periodicity == Periodicity.PERIODIC;

        /* Calculate m */
        double burst_factor = this.is_periodic ? 1.0 : 2.0;
        this.max_data_per_CMI = burst_factor * this.mfs * this.mif;
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
     * Set path for this flow
     * @param path New path
     */
    public void setPath(LinkedList<CBS_Link> path) {
        this.path = path;

        /* Traverse path once and update max. packet size */
        for(CBS_Link link: this.path) {
            link.updateMaxPacketSize(this.getMfs());
        }
    }

    /**
     * @return Path of the flow
     */
    public LinkedList<CBS_Link> getPath() {
        return this.path;
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
        double tb_rate = this.getMax_data_per_CMI() / this.cmi;
        double tb_burst = this.getMax_data_per_CMI() * (1.0 - (tb_rate / linkCapacity));
        this.arrivalCurve = Curve.getFactory().createTokenBucket(tb_rate, tb_burst);

        System.out.println("\r\nAC of Flow " + this.getAlias() + " : " + this.getArrivalCurve());

        return this.arrivalCurve;
    }

}
