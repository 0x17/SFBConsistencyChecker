package org.andreschnabel.consistencychecker;

import org.andreschnabel.consistencychecker.gui.LaunchFrame;

import javax.swing.*;
import java.io.File;
import java.nio.file.Path;

public class GuiMain {

    public static void main(String[] args) throws Exception {
        Path chosenPath = queryMppFilePathFromUser();
        if(chosenPath == null) return;
        LaunchFrame lf = new LaunchFrame(chosenPath);
        lf.setVisible(true);
    }

    private static Path queryMppFilePathFromUser() {
        JFileChooser fc = new JFileChooser();

        fc.setCurrentDirectory(new File("."));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int res = fc.showOpenDialog(null);
        while(res != JFileChooser.APPROVE_OPTION || Utils.collectSubProjectNames(fc.getSelectedFile().toPath()).isEmpty()) {
            if(res == JFileChooser.CANCEL_OPTION) return null;
            JOptionPane.showMessageDialog(null, "Chosen directory invalid!", "Fail", JOptionPane.ERROR_MESSAGE);
            res = fc.showOpenDialog(null);
        }
        return fc.getSelectedFile().toPath();
    }
}
