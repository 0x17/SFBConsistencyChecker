package org.andreschnabel.consistencychecker.model;

import java.util.LinkedList;
import java.util.List;

public class SubProject {

    public String name;
    public List<SupplyRelationship> supplies = new LinkedList<>();
    public List<SupplyRelationship> receives = new LinkedList<>();

    public SubProject(String name) {
        this.name = name;
    }

    public void addSuppliesTo(String to, String text, MiniDate date) {
        this.supplies.add(new SupplyRelationship(name, to, text, date));
    }

    public void addReceivesFrom(String from, String text, MiniDate date) {
        this.receives.add(new SupplyRelationship(from, name, text, date));
    }

    public List<String> getAdjacentSubprojects() {
        List<String> adjacents = new LinkedList<>();
        for(SupplyRelationship rel : supplies) {
            if(!adjacents.contains(rel.to))
                adjacents.add(rel.to);
        }
        for(SupplyRelationship rel : receives) {
            if(!adjacents.contains(rel.from))
                adjacents.add(rel.from);
        }
        return  adjacents;
    }


}
