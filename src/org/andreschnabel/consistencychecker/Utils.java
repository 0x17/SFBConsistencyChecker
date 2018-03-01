package org.andreschnabel.consistencychecker;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {

    public static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static void writeStringToFile(String s, String fn) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fn))) {
            writer.write(s);
        }
    }

    public static String extractSubprojectNameFromTaskName(String taskName, String prefix) throws Exception {
        Pattern pattern = Pattern.compile("(" + prefix + ")?([a-zA-Z]\\d?)\\s*(\\(i+\\))?");
        Matcher matcher = pattern.matcher(taskName);
        if(!matcher.find()) {
            throw new Exception("Unable to extract subproject name from task name: " + taskName + "!");
        }
        return matcher.group(2);
    }

    public static String extractSubprojectNameFromFilename(String filename) {
        Pattern pattern = Pattern.compile("Zeitplan_([a-zA-Z]\\d?)(_\\d+)?\\.mpp");
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

    public static List<String> collectSubProjectNames(Path root) {
        try {
            return Files.list(root).filter(p -> Files.isRegularFile(p) && p.getFileName().toString().startsWith("Zeitplan_") && p.getFileName().toString().endsWith(".mpp")).map(p -> Utils.extractSubprojectNameFromFilename(p.getFileName().toString())).collect(Collectors.toList());
        } catch(Exception e) {
            return new LinkedList<>();
        }
    }

    public static boolean closeEnough(String a, String b) {
        return a.replaceAll("\\s+", "").toLowerCase().equals(b.replaceAll("\\s+", "").toLowerCase());
    }

    public static List<String> extractSubprojectNamesFromMessage(String message) {
        Pattern pattern = Pattern.compile("([a-zA-Z]\\d)");
        Matcher matcher = pattern.matcher(message);
        List<String> spnames = new LinkedList<>();
        while(matcher.find()) {
            spnames.add(matcher.group(1));
        }

        String[] withS = {"from='S'", "to='S'"};
        if(Arrays.stream(withS).anyMatch(message::contains))
            spnames.add("S");

        return spnames;
    }
}
