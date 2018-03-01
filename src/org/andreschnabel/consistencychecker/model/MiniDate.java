package org.andreschnabel.consistencychecker.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

public class MiniDate {

    public int month, year;

    public MiniDate(int month, int year) {
        this.month = month;
        this.year = year;
    }

    public MiniDate(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        month = localDate.getMonthValue();
        year = localDate.getYear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MiniDate miniDate = (MiniDate) o;
        return month == miniDate.month &&
                year == miniDate.year;
    }

    @Override
    public int hashCode() {
        return Objects.hash(month, year);
    }

    @Override
    public String toString() {
        return "MiniDate{" +
                "month=" + month +
                ", year=" + year +
                '}';
    }

    public String toMMYYYY() {
        return (month <= 9 ? "0" : "") + month + "." + year;
    }
}
