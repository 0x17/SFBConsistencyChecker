package org.andreschnabel.consistencychecker;

import org.andreschnabel.consistencychecker.model.SubProject;
import org.andreschnabel.consistencychecker.model.SupplyRelationship;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;

public class EventExporter {

    private final SubProject subproject;

    public EventExporter(String subprojectName) {
        Path mppFilePath = Paths.get("C:\\Users\\a.schnabel\\Desktop\\ProjektplanAktuell");
        subproject = ModelReader.parseSubprojectFromDisk(subprojectName, mppFilePath);
    }

    private String eventsToCsv() {
        StringBuilder sb = new StringBuilder("from;to;date;text\n");
        for(SupplyRelationship supply : subproject.supplies) {
            sb.append(String.join(";", Arrays.asList(supply.from, supply.to, supply.date.toMMYYYY(), supply.text.trim())));
            sb.append("\n");
        }
        for(SupplyRelationship receives : subproject.receives) {
            sb.append(String.join(";", Arrays.asList(receives.from, receives.to, receives.date.toMMYYYY(), receives.text.trim())));
            sb.append("\n");
        }
        return sb.toString();
    }

    public void exportEventsToCsv(String filename) throws IOException {
        Utils.writeStringToFile(eventsToCsv(), filename);
    }

    public static void main(String[] args) throws IOException {
        EventExporter exporter = new EventExporter("D3");
        exporter.exportEventsToCsv("d3_events.txt");
    }

}
