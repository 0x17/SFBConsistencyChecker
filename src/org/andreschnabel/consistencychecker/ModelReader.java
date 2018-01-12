package org.andreschnabel.consistencychecker;

import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Task;
import net.sf.mpxj.mpp.MPPReader;
import net.sf.mpxj.reader.ProjectReader;
import org.andreschnabel.consistencychecker.model.MiniDate;
import org.andreschnabel.consistencychecker.model.SubProject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ModelReader {

    public enum State {
        None,
        DeliverTasks,
        ReceiveTasks
    }

    public static SubProject parseSubprojectFromDisk(String subprojectName) {
        SubProject sp = null;
        try {
            sp = new SubProject(subprojectName);

            final String deliverTaskNamePrefix = "Lieferant ",
                    receiveTaskNamePrefix = "Empfänger ",
                    publicationsTaskNamePrefix = "Veröffentlichungen ";

            ProjectReader reader = new MPPReader();

            ProjectFile project = reader.read(findMSProjectFilenameForSubproject(subprojectName));

            State curState = State.None;
            for(Task task : project.getTasks()) {
                String name = task.getName();
                if(name.startsWith(deliverTaskNamePrefix+subprojectName)) {
                    curState = State.DeliverTasks;
                } else if(name.startsWith(receiveTaskNamePrefix+subprojectName)) {
                    curState = State.ReceiveTasks;
                } else if(name.startsWith(publicationsTaskNamePrefix+subprojectName)) {
                    break;
                } else {
                    String notes = task.getNotes();
                    MiniDate start = new MiniDate(task.getEarlyStart());

                    switch(curState) {
                        case None:
                            break;
                        case DeliverTasks:
                            sp.addSuppliesTo(targetSubprojectFromName(name), notes, start);
                            break;
                        case ReceiveTasks:
                            sp.addReceivesFrom(sourceSubprojectFromName(name), notes, start);
                            break;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sp;
    }

    private static String findMSProjectFilenameForSubproject(String subprojectName) throws Exception {
        String defaultFilename = "Zeitplan_"+subprojectName+".mpp";
        if(Files.exists(Paths.get(defaultFilename))) return defaultFilename;
        List<Path> matching = Files.list(Paths.get(".")).filter(p -> Files.isRegularFile(p) && p.getFileName().toString().startsWith("Zeitplan_" + subprojectName)).collect(Collectors.toList());
        if(matching.isEmpty())
            throw new Exception("Unable to identify MS project file for subproject with name " + subprojectName);
        else
            return matching.get(0).getFileName().toString();
    }

    private static String sourceSubprojectFromName(String taskName) throws Exception {
        return Utils.extractSubprojectNameFromTaskName(taskName, "von ");
    }

    private static String targetSubprojectFromName(String taskName) throws Exception {
        return Utils.extractSubprojectNameFromTaskName(taskName, "an ");
    }

}
