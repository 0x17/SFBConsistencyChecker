package org.andreschnabel.consistencychecker;

import org.andreschnabel.consistencychecker.model.SubProject;
import org.andreschnabel.consistencychecker.model.SupplyRelationship;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConsistencyChecker {

    public static ConflictCollector checkConsistencyForTargetSubProject(String targetSubprojectName, Path mppFilePath) {
        return checkConsistencyForTargetSubProject(targetSubprojectName, mppFilePath, null);
    }

    public static ConflictCollector checkConsistencyForTargetSubProject(String targetSubprojectName, Path mppFilePath, Consumer<Integer> progressCallback) {
        ConflictCollector out = new ConflictCollector(targetSubprojectName);
        try {
            SubProject targetSubProject = ModelReader.parseSubprojectFromDisk(targetSubprojectName, mppFilePath);
            List<String> knownProjectNames = Utils.collectSubProjectNames(mppFilePath);
            List<SubProject> adjacentSubProjects = targetSubProject.getAdjacentSubprojects().stream().filter(knownProjectNames::contains).map((String subprojectName) -> ModelReader.parseSubprojectFromDisk(subprojectName, mppFilePath)).collect(Collectors.toList());

            if(progressCallback != null) progressCallback.accept(0);

            int ctr = 0;
            for (SubProject adjacentSubProject : adjacentSubProjects) {
                checkRelations(out, targetSubProject, adjacentSubProject);
                if(progressCallback != null) progressCallback.accept((int)((float)(ctr+1)/(float)adjacentSubProjects.size()*100.0f));
                ctr++;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    public static String additionalInformationStrFromCollector(ConflictCollector out) {
        StringBuilder ostr = new StringBuilder();
        try {
            List<String> others = out.getOthers();
            ostr.append("Conflicts with the following " + others.size() + " other subprojects: " + others + "\n\n");
            if (Files.exists(Paths.get("SFBTPDaten.json"))) {
                List<String> persons = JsonData.mapSubprojectNamesToContactPersons("SFBTPDaten.json", others);
                List<String> contactPersonMails = JsonData.mapSubprojectNamesToContactPersonMails("SFBTPDaten.json", others);
                ostr.append("Corresponding contact persons: " + persons + "\n");
                ostr.append("Their mails: " + contactPersonMails + "\n");
            }
            ostr.append(out.stats());
        } catch(Exception e) {
            e.printStackTrace();
        }
        return ostr.toString();
    }

    public static void serializeConflicts(ConflictCollector out, String fn) {
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fn))) {
                writer.write(out.toString());
            }
            System.out.println(additionalInformationStrFromCollector(out));
            System.out.println("Wrote conflicts into " + fn);
        } catch(Exception e) {
            e.printStackTrace();
        }
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
        final String unmatchedAtReceiverPrefixStr = "\n\nUnmatched candidates at " + receiverSubProject.name + ":\n";

        for(SupplyRelationship ssr : senderDeliversToReceiverRels) {
            if(recipientReceivesFromSenderRels.stream().noneMatch(rsr -> rsr.equals(ssr))) {
                List<SupplyRelationship> correspondingCandidates = recipientReceivesFromSenderRels.stream().filter(rsr -> rsr.canBeAssociatedWith(ssr)).collect(Collectors.toList());

                if(correspondingCandidates.isEmpty()) {
                    String unmatchedAtReceiverRelsStr = collectUnmatchedRelationsshipsAtReceiver(recipientReceivesFromSenderRels, senderDeliversToReceiverRels).map(SupplyRelationship::toString).collect(Collectors.joining("\n"));
                    String unmatchedAtReceiverDisplayStr = unmatchedAtReceiverRelsStr.isEmpty() ? "" : unmatchedAtReceiverPrefixStr + unmatchedAtReceiverRelsStr + "\n";
                    out.add(ConflictCollector.ConflictType.CORRESPONDENCE_MISMATCH, "Missing at " + receiverSubProject.name + ": " + ssr + unmatchedAtReceiverDisplayStr);
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

    private static Stream<SupplyRelationship> collectUnmatchedRelationsshipsAtReceiver(List<SupplyRelationship> recipientReceivesFromSenderRels, List<SupplyRelationship> senderDeliversToReceiverRels) {
        return recipientReceivesFromSenderRels.stream().filter(rsr -> senderDeliversToReceiverRels.stream().anyMatch(rsr::canBeAssociatedWith));
    }
}
