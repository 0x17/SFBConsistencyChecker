package org.andreschnabel.consistencychecker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {

    public static String extractSubprojectNameFromTaskName(String taskName, String prefix) throws Exception {
        Pattern pattern = Pattern.compile("(" + prefix + ")?(\\w\\d?)\\s*(\\(i+\\))?");
        Matcher matcher = pattern.matcher(taskName);
        if(!matcher.find()) {
            throw new Exception("Unable to extract subproject name from task name: " + taskName + "!");
        }
        return matcher.group(2);
    }

    public static String extractSubprojectNameFromFilename(String filename) {
        Pattern pattern = Pattern.compile("Zeitplan_(\\w\\d?)(_\\d+)?\\.mpp");
        Matcher matcher = pattern.matcher(filename);
        if(!matcher.find()) {
            try {
                throw new Exception("Unable to extract subproject name from filename: " + filename + "!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return matcher.group(1);
    }

    public static List<String> collectSubProjectNames(Path root) throws IOException {
        return Files.list(root).filter(p -> Files.isRegularFile(p) && p.getFileName().toString().startsWith("Zeitplan_") && p.getFileName().toString().endsWith(".mpp")).map(p -> Utils.extractSubprojectNameFromFilename(p.getFileName().toString())).collect(Collectors.toList());
    }

    public static boolean closeEnough(String a, String b) {
        return a.replaceAll("\\s+", "").toLowerCase().equals(b.replaceAll("\\s+", "").toLowerCase());
    }
}
