package org.andreschnabel.consistencychecker;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ConflictCollector {

    private final String targetSubprojectName;

    @Override
    public String toString() {
        StringBuilder ostr = new StringBuilder();

        for(ConflictType type : conflicts.keySet()) {
            ostr.append("\nConflicts of type ").append(type).append(":\n");
            for(String message : conflicts.get(type)) {
                ostr.append(message).append("\n");
            }
        }

        return ostr.toString();
    }

    public String stats() {
        StringBuilder ostr = new StringBuilder();
        for(ConflictType type : conflicts.keySet()) {
            ostr.append("Found ").append(conflicts.get(type).size()).append(" conflicts of type ").append(type).append("\n");
        }
        return ostr.toString();
    }

    public Map<ConflictType, Integer> statCounts() {
        Map<ConflictType, Integer> counts = new HashMap<>();
        for(ConflictType type : conflicts.keySet()) {
            counts.put(type, conflicts.get(type).size());
        }
        return counts;
    }

    public int totalCount() {
        return statCounts().values().stream().mapToInt(Integer::intValue).sum();
    }

    public List<String> getOthers() {
        return others;
    }

    enum ConflictType {
        COUNT_MISMATCH,
        TEXT_MISMATCH,
        DATE_MISMATCH,
        CORRESPONDENCE_MISMATCH
    }

    private Map<ConflictType, List<String>> conflicts = new HashMap<>();

    ConflictCollector(String targetSubprojectName) {
        this.targetSubprojectName = targetSubprojectName;
        for(ConflictType type : ConflictType.values()) {
            conflicts.put(type, new LinkedList<>());
        }
    }

    private List<String> others = new LinkedList<>();

    public void add(ConflictType type, String message) {
        conflicts.get(type).add(message);
        trackOthers(message);
    }

    private void trackOthers(String message) {
        for(String spname : Utils.extractSubprojectNamesFromMessage(message))
            if(!others.contains(spname) && !spname.equals(targetSubprojectName))
                others.add(spname);
    }

    public List<String> getMessagesForType(ConflictType type) {
        return conflicts.get(type);
    }

}
