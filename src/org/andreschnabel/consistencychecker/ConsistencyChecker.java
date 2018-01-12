package org.andreschnabel.consistencychecker;

import org.andreschnabel.consistencychecker.model.SubProject;
import org.andreschnabel.consistencychecker.model.SupplyRelationship;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ConsistencyChecker {

    public static ConflictCollector checkConsistencyForTargetSubProject(String targetSubprojectName) throws Exception {
        ConflictCollector out = new ConflictCollector();

        SubProject targetSubProject = ModelReader.parseSubprojectFromDisk(targetSubprojectName);
        List<String> knownProjectNames = Utils.collectSubProjectNames(Paths.get("."));
        List<SubProject> adjacentSubProjects = targetSubProject.getAdjacentSubprojects().stream().filter(knownProjectNames::contains).map(ModelReader::parseSubprojectFromDisk).collect(Collectors.toList());

        for(SubProject adjacentSubProject : adjacentSubProjects) {
            checkRelations(out, targetSubProject, adjacentSubProject);
        }

        return out;
    }

    public static void serializeConflicts(ConflictCollector out, String fn) throws Exception {
        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(fn))) {
            writer.write(out.toString());
        }
        List<String> others = out.getOthers();
        System.out.println("Conflicts with the following "+others.size()+" other subprojects: " + others + "\n");
        System.out.println(out.stats());
        System.out.println("Wrote conflicts into " + fn);
    }

    private static void checkRelations(ConflictCollector out, SubProject targetSubProject, SubProject adjacentSubProject) {
        List<SupplyRelationship> targetDeliversToAdjacentRels = targetSubProject.supplies.stream().filter(sr -> sr.to.equals(adjacentSubProject.name)).collect(Collectors.toList());
        List<SupplyRelationship> adjacentReceivesFromTargetRels = adjacentSubProject.receives.stream().filter(sr -> sr.from.equals(targetSubProject.name)).collect(Collectors.toList());

        List<SupplyRelationship> targetReceivesFromAdjacentRels = targetSubProject.receives.stream().filter(sr -> sr.from.equals(adjacentSubProject.name)).collect(Collectors.toList());
        List<SupplyRelationship> adjacentDeliversToTargetRels = adjacentSubProject.supplies.stream().filter(sr -> sr.to.equals(targetSubProject.name)).collect(Collectors.toList());

        checkRelationConsistency(out, targetSubProject, adjacentSubProject, targetDeliversToAdjacentRels, adjacentReceivesFromTargetRels);
        checkRelationConsistency(out, adjacentSubProject, targetSubProject, adjacentDeliversToTargetRels, targetReceivesFromAdjacentRels);
    }

    private static void checkRelationConsistency(ConflictCollector out, SubProject senderSubProject, SubProject receiverSubProject, List<SupplyRelationship> senderDeliversToReceiverRels, List<SupplyRelationship> recipientReceivesFromSenderRels) {
        if(senderDeliversToReceiverRels.size() != recipientReceivesFromSenderRels.size()) {
            out.add(ConflictCollector.ConflictType.COUNT_MISMATCH, senderSubProject.name + " promises to deliver " + senderDeliversToReceiverRels.size() + " times to " + receiverSubProject.name + " but receiver knows of " + recipientReceivesFromSenderRels.size() + " transactions!");
        }

        checkForCorrespondence(out, receiverSubProject, senderDeliversToReceiverRels, recipientReceivesFromSenderRels, true);
        checkForCorrespondence(out, senderSubProject, recipientReceivesFromSenderRels, senderDeliversToReceiverRels, false);
    }

    private static void checkForCorrespondence(ConflictCollector out, SubProject receiverSubProject, List<SupplyRelationship> senderDeliversToReceiverRels, List<SupplyRelationship> recipientReceivesFromSenderRels, boolean checkForTextAndDateMismatch) {
        for(SupplyRelationship ssr : senderDeliversToReceiverRels) {
            boolean identicalExists = recipientReceivesFromSenderRels.stream().anyMatch(rsr -> rsr.date.equals(ssr.date) && Utils.closeEnough(rsr.text, ssr.text));
            if(!identicalExists) {
                List<SupplyRelationship> correspondingCandidates = recipientReceivesFromSenderRels.stream().filter(rsr -> rsr.date.equals(ssr.date) || Utils.closeEnough(rsr.text, ssr.text)).collect(Collectors.toList());

                if(correspondingCandidates.isEmpty()) {
                    out.add(ConflictCollector.ConflictType.CORRESPONDENCE_MISMATCH, "Missing at " + receiverSubProject.name + ": " + ssr);
                }

                if(checkForTextAndDateMismatch) {
                    for (SupplyRelationship rsr : correspondingCandidates) {
                        boolean dateEquals = rsr.date.equals(ssr.date);
                        boolean textEquals = Utils.closeEnough(rsr.text, ssr.text);
                        String msg = "Supplier ("+ssr.from+"): " + ssr + "\n" + "Receiver ("+ssr.to+"): " + rsr;
                        if (dateEquals && !textEquals) {
                            out.add(ConflictCollector.ConflictType.TEXT_MISMATCH, msg);
                        } else if (textEquals && !dateEquals) {
                            out.add(ConflictCollector.ConflictType.DATE_MISMATCH, msg);
                        }
                    }
                }
            }
        }
    }
}
