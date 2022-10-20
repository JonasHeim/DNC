package org.networkcalculus.dnc.tsn_cbs;

/**
 * Class representing of links between Credit-Based-Shaper server.
 */
public class CBS_Link {
    /**
     * String representation of the link
     */
    private final String alias;

    /**
     * Source CBS server of the link.
     */
    private final CBS_Server source;

    /**
     * Destination CBS server of the link.
     */
    private final CBS_Server destination;

    /**
     * Capacity of the link in Bit/s
     */
    private final double capacity;

    /**
     * @param alias         The link alias (not necessarily unique).
     * @param source        The links source server
     * @param destination   The links destination server
     * @param capacity      The link capacity in Bit/s
     */
    protected CBS_Link(String alias, CBS_Server source, CBS_Server destination, double capacity) {
        this.alias = alias;
        this.source = source;
        this.destination = destination;
        this.capacity = capacity;
    }

    /**
     * @return String representation of the link
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @return The source server of the link
     */
    public CBS_Server getSource() {
        return source;
    }

    /**
     * @return The destination server of the link
     */
    public CBS_Server getDestination() {
        return destination;
    }

    /**
     * @return The link capacity in bit/s
     */
    public double getCapacity() {
        return capacity;
    }

    /**
     * @return String representation of the link used for printing
     */
    public String toString() {
        StringBuffer cbs_link_str = new StringBuffer();

        cbs_link_str.append("CBS Link \"" + this.alias + "\" connecting " + this.source.getAlias() + " to " + this.destination.getAlias() + " with a capacity of " + this.getCapacity() + " Bit/s\r\n");

        return cbs_link_str.toString();
    }
}
