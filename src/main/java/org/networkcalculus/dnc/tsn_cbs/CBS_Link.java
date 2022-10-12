package org.networkcalculus.dnc.tsn_cbs;

public class CBS_Link {
    private final String alias;
    private final CBS_RateLatency_Server source;
    private final CBS_RateLatency_Server destination;
    private final double capacity;

    protected CBS_Link(String alias, CBS_RateLatency_Server source, CBS_RateLatency_Server destination, double capacity) {
        this.alias = alias;
        this.source = source;
        this.destination = destination;
        this.capacity = capacity;
    }

    public String getAlias() {
        return alias;
    }

    public CBS_RateLatency_Server getSource() {
        return source;
    }

    public CBS_RateLatency_Server getDestination() {
        return destination;
    }

    public double getCapacity() {
        return capacity;
    }

    public String toString() {
        StringBuffer cbs_link_str = new StringBuffer();

        cbs_link_str.append("CBS Link \"" + this.alias + "\" connecting " + this.source.getAlias() + " to " + this.destination.getAlias() + "\r\n");

        return cbs_link_str.toString();
    }
}
