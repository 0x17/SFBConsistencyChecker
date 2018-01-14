package org.andreschnabel.consistencychecker.gui;

import org.andreschnabel.consistencychecker.Utils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class LaunchFrame extends JFrame {

    public LaunchFrame() throws IOException {
        super("SFBConsistencyChecker");
        setSize(640, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout());
        add(new JLabel("Name of subproject:"));
        List<String> lst = Utils.collectSubProjectNames(Paths.get("."));
        lst.sort(String::compareTo);
        String[] spnames = new String[lst.size()];
        lst.toArray(spnames);
        JComboBox comboBox = new JComboBox<String>(spnames);
        add(comboBox);
        add(new JButton("Check consistency"));
        add(new JButton("Show stats"));

        JTextArea textArea = new JTextArea(20, 40);
        textArea.setEditable(false);
        add(textArea);
    }

}
