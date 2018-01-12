package org.andreschnabel.consistencychecker.model;

public class SupplyRelationship {

    public String from, to, text;
    public MiniDate date;

    public SupplyRelationship(String from, String to, String text, MiniDate date) {
        this.from = from;
        this.to = to;
        this.text = text;
        this.date = date;
    }

    @Override
    public String toString() {
        return "SupplyRelationship{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", text='" + text.replace("\n", "") + '\'' +
                ", date=" + date +
                '}';
    }
}
