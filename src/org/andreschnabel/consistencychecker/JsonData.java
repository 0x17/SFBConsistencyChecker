package org.andreschnabel.consistencychecker;

import com.jayway.jsonpath.JsonPath;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

public class JsonData {

    private static List<String> mapSubprojectNamesToAttribute(String dataFilename, List<String> subprojectNames, String attrName) throws Exception {
        String jsonStr = Utils.readFile(dataFilename, Charset.defaultCharset());
        return subprojectNames.stream().map(spname -> (String) JsonPath.read(jsonStr, "$." + spname + "."+attrName)).collect(Collectors.toList());
    }

    public static List<String> mapSubprojectNamesToContactPersons(String dataFilename, List<String> subprojectNames) throws Exception {
        return mapSubprojectNamesToAttribute(dataFilename, subprojectNames,"Name");
    }

    public static List<String> mapSubprojectNamesToContactPersonMails(String dataFilename, List<String> subprojectNames) throws Exception {
        return mapSubprojectNamesToAttribute(dataFilename, subprojectNames,"Mail");
    }
}
