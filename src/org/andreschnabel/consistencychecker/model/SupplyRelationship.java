package org.andreschnabel.consistencychecker.model;

import org.andreschnabel.consistencychecker.Utils;

import java.util.Objects;
import java.util.stream.Stream;

public class SupplyRelationship {

    public String from, to, text;
    public MiniDate date;

    public SupplyRelationship(String from, String to, String text, MiniDate date) {
        this.from = from;
        this.to = to;
        this.text = text;
        this.date = date;
    }

    public boolean canBeAssociatedWith(SupplyRelationship other) {
        return from.equals(other.from) && to.equals(other.to) && (Utils.closeEnough(text, other.text) || date.equals(other.date));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SupplyRelationship that = (SupplyRelationship) o;
        return Objects.equals(from, that.from) &&
                Objects.equals(to, that.to) &&
                Utils.closeEnough(text, that.text) &&
                Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {

        return Objects.hash(from, to, text, date);
    }

    @Override
    public String toString() {
        return "LB{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", text='" + text.replace("\n", "") + '\'' +
                ", date=" + date +
                '}';
    }
}
