package org.andreschnabel.consistencychecker.gui;

import org.andreschnabel.consistencychecker.ConflictCollector;
import org.andreschnabel.consistencychecker.ConflictStats;
import org.andreschnabel.consistencychecker.ConsistencyChecker;
import org.andreschnabel.consistencychecker.Utils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class LaunchFrame extends JFrame {

    public LaunchFrame(Path mppFilePath) throws IOException {
        super("SFBConsistencyChecker");
        setSize(640, 480);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        add(topPanel, BorderLayout.NORTH);

        topPanel.add(new JLabel("Name of subproject:"));
        List<String> lst = Utils.collectSubProjectNames(mppFilePath);
        lst.sort(String::compareTo);
        String[] spnames = new String[lst.size()];
        lst.toArray(spnames);
        JComboBox comboBox = new JComboBox<>(spnames);
        topPanel.add(comboBox);

        JTextArea textArea = new JTextArea(20, 60);
        textArea.setEditable(false);

        JScrollPane textAreaScrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        Runnable checkConsistencyTask = () -> {
            String selectedSubprojectName = spnames[comboBox.getSelectedIndex()];
            ConflictCollector coll = ConsistencyChecker.checkConsistencyForTargetSubProject(selectedSubprojectName, mppFilePath, progressBar::setValue);
            String wroteStr = "Wrote conflicts into " + selectedSubprojectName + "_Conflicts.txt";
            textArea.setText(ConsistencyChecker.additionalInformationStrFromCollector(coll) + "\n" + coll.toString() + "\n" + wroteStr);
            ConsistencyChecker.serializeConflicts(coll, selectedSubprojectName + "_Conflicts.txt");
        };

        JButton checkConsistencyBtn = new JButton("Check consistency");
        checkConsistencyBtn.addActionListener(e -> new Thread(checkConsistencyTask).start());
        topPanel.add(checkConsistencyBtn);

        JButton showStatsBtn = new JButton("Show stats");

        Runnable collectStatLinesTask = () -> {
            textArea.setText("");
            ConflictStats.statLinesForAllSubprojects(mppFilePath, progressBar::setValue).forEach(confLine -> textArea.append(confLine+"\n"));
        };
        showStatsBtn.addActionListener(e -> new Thread(collectStatLinesTask).start());
        topPanel.add(showStatsBtn);

        topPanel.add(progressBar);
        add(textAreaScrollPane, BorderLayout.CENTER);

        pack();
    }

}
