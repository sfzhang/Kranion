/*
 * The MIT License
 *
 * Copyright 2019 Focused Ultrasound Foundation.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.fusfoundation.kranion;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JFileChooser;
import org.fusfoundation.kranion.view.DefaultView;

/**
 *
 * @author jsnell
 */
public class FileDialog extends FlyoutDialog {
        
    private ListControl dirList = new ListControl(10, 50, 400, 300, this);
    private ListControl fileList = new ListControl(500, 50, 400, 300, this);
    private ScrollBarControl fileScroll = new ScrollBarControl();
    private ScrollBarControl dirScroll = new ScrollBarControl();
    private PullDownSelection rootSelector = new PullDownSelection(10, 350, 200, 25);
    private TextBox currentLocation = new TextBox();
    private TextBox dirLabel = new TextBox();
    private TextBox fileLabel = new TextBox();
    private TextBox selectedFileDisplay = new TextBox();
    private Button okButton = new Button(Button.ButtonType.BUTTON, 10, 10, 100, 25, this);
    private Button cancelButton = new Button(Button.ButtonType.BUTTON, 125, 10, 100, 25, this);
    private TextBox titleDisplay = new TextBox();
    private static Font titleFont = new Font("Helvetica", Font.PLAIN | Font.TRUETYPE_FONT, 20);
    
    private String[] fileFilters = null;
    private File selectedFile = null;
    private File currentDirectory = null;
    
    private fileChooseMode chooseMode = fileChooseMode.EXISTING_FILES;
    
    public static enum fileChooseMode {EXISTING_FILES, EXISTING_DIRECTORIES, FILES};
    
    public FileDialog() {
        this.setBounds(0, 0, 512, 256);
        this.setFlyDirection(direction.SOUTH);
        
        selectedFileDisplay.setText("");
        selectedFileDisplay.setTitle("Selected file:");
        selectedFileDisplay.addActionListener(this);
        selectedFileDisplay.setCommand("fileNameTyping");
        
        titleDisplay.setText("File Dialog");
        titleDisplay.showBackgroundBox(false);
        titleDisplay.setTextFont(titleFont);
        titleDisplay.setTextHorzAlignment(HPosFormat.HPOSITION_CENTER);
        
        currentLocation.setTitle("Look in:");
        
        addChild(okButton.setTitle("Ok").setCommand("OK"));
        addChild(cancelButton.setTitle("Cancel").setCommand("CANCEL"));
        addChild(dirList);
        addChild(fileList);
        addChild(dirScroll);
        addChild(fileScroll);
        addChild(currentLocation);
        addChild(rootSelector);
        
        dirLabel.setText("Directories");
        fileLabel.setText("Files");
        dirLabel.showBackgroundBox(false);
        fileLabel.showBackgroundBox(false);
        addChild(dirLabel);
        addChild(fileLabel);
        addChild(selectedFileDisplay);
        addChild(titleDisplay);
        
        rootSelector.setCommand("rootSelected");
        rootSelector.addActionListener(this);
        
        dirScroll.setCommand("dirScroll");
        dirScroll.addActionListener(this);
        
        fileScroll.setCommand("fileScroll");
        fileScroll.addActionListener(this);
        
        dirList.setCommand("dirSelected");
        fileList.setCommand("fileSelected");
        
        String homeDocPath = new JFileChooser().getFileSystemView().getDefaultDirectory().toString();
        File home = new File(homeDocPath);

        populateRoots();
        
        //populateLists(new File(System.getProperty("user.home")));
        populateLists(home);
        currentDirectory = home;
                
                dirList.sort();
                fileList.sort();
                
    }
    
    public void setDialogTitle(String title) {
        titleDisplay.setText(title);
    }
    
    public void setFileChooseMode(fileChooseMode mode) {
        chooseMode = mode;
    }
    
    public File open() {
        return open(null);
    }
    
    public File open(String[] filters) {
        if (chooseMode == fileChooseMode.FILES) {
            this.selectedFileDisplay.setTextEditable(true);
            this.fileList.setIsEnabled(true);
            selectedFileDisplay.setTitle("Selected file:");

        } else if (chooseMode == fileChooseMode.EXISTING_FILES) {
            this.selectedFileDisplay.setTextEditable(false);
            this.fileList.setIsEnabled(true);
            selectedFileDisplay.setTitle("Selected file:");

        } else if (chooseMode == fileChooseMode.EXISTING_DIRECTORIES) {
            this.selectedFileDisplay.setTextEditable(false);
            this.fileList.setIsEnabled(false);
            selectedFileDisplay.setTitle("Selected directory:");

        }

        fileFilters = filters;
        selectedFile = null;
        selectedFileDisplay.setText("");
        okButton.setIsEnabled(false);
        
        populateLists(currentDirectory);

        show();
        return selectedFile;
    }
    
    protected void populateRoots() {
        rootSelector.clear();
        File[] roots = File.listRoots();
        if (roots == null || roots.length == 0) return;
        
        rootSelector.addItem("HOME");
        rootSelector.addItem("DOCUMENTS");
        for (int i = 0; i < roots.length; i++) {
            System.out.println("Root[" + i + "]:" + roots[i]);
            rootSelector.addItem(roots[i].toString());
        }
        rootSelector.setTitle(roots[0].toString());
    }
    
    protected void populateLists(File root) {

        if (root == null) {
            return;
        }
        
        currentDirectory = root;
        
        if (root.toString().equals("HOME")) {
            root = new File(System.getProperty("user.home"));
        }
        else if (root.toString().equals("DOCUMENTS")) {
            root = new File(new JFileChooser().getFileSystemView().getDefaultDirectory().toString());
        }

        Path homePath = root.toPath();
        String currentRoot = homePath.getRoot().toString();
//        System.out.println("Root = " + currentRoot);
        
        currentLocation.setText(homePath.toString());
        
        for (int i=0; i<rootSelector.getItemCount(); i++) {
            if (currentRoot.equals(rootSelector.getItem(i))) {
                rootSelector.setSelectionIndex(i);
                break;
            }
        }
        
        dirList.clear();
        fileList.clear();

        if (root.getParent() != null) {
//            System.out.println("Adding parent directory: " + root.getParent());
            dirList.addItem("[Parent directory]", new File(root.getParent()));
        }

        File[] files = root.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.getName().startsWith(".")) {
                    if (file.isDirectory()) {
//                            System.out.println("Directory: " + file.getName());
                        dirList.addItem(file.getName(), file);
//            showFiles(file.listFiles()); // Calls same method again.
                    } else {
//                            System.out.println("File: " + file.getName());
                        if (fileFilters != null) {
                            String name = file.getName();
                            for (int i = 0; i < fileFilters.length; i++) {
                                if (name.endsWith(fileFilters[i])) {
                                    fileList.addItem(file.getName(), file);
                                    break;
                                }
                            }
                        } else {
                            fileList.addItem(file.getName(), file);
                        }
                    }
                }
            }
        }

        dirScroll.setValue(0);
        fileScroll.setValue(0);
        dirList.setScroll(0);
        fileList.setScroll(0);

        doLayout();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
//        System.out.println(this + ": " + e.getActionCommand());
        switch (e.getActionCommand()) {
            case "fileNameTyping":
                if (this.selectedFileDisplay.getText().length() > 0) {
                    okButton.setIsEnabled(true);
                }
                else {
                    okButton.setIsEnabled(false);
                }
                break;
            case "OK":
                if (selectedFileDisplay.getText().length() > 0) {
                    if (chooseMode == fileChooseMode.EXISTING_DIRECTORIES) {
                        selectedFile = new File(selectedFileDisplay.getText());
                    }
                    else {
                        selectedFile = new File(currentLocation.getText() + File.separator + selectedFileDisplay.getText());
                    }
//                    System.out.println(selectedFile);
                }
                else {
                    selectedFile = null;
                }
            case "CANCEL":
//                File[] roots = File.listRoots();
//                for (int i = 0; i < roots.length; i++) {
//                    System.out.println("Root[" + i + "]:" + roots[i]);
//                }
//                System.out.println(System.getProperty("user.home"));
//                File[] files = new File(System.getProperty("user.home")).listFiles();
//                for (File file : files) {
//                    if (!file.getName().startsWith(".")) {
//                        if (file.isDirectory()) {
//                            System.out.println("Directory: " + file.getName());
////            showFiles(file.listFiles()); // Calls same method again.
//                        } else {
//                            System.out.println("File: " + file.getName());
//                        }
//                    }
//                }
                flyin();
                isClosed = true;
                break;
            case "fileScroll":
                {
                    ScrollBarControl scroll = (ScrollBarControl)e.getSource();
                    fileList.setScroll(scroll.getValue());
                }
                break;
            case "dirScroll":
                {
                    ScrollBarControl scroll = (ScrollBarControl)e.getSource();
                    dirList.setScroll(scroll.getValue());
                }
                break;
            case "fileSelected":
                File thisSelection = (File)(fileList.getSelected());
                if (thisSelection != null) {
                    selectedFileDisplay.setText(thisSelection.getName());
                    okButton.setIsEnabled(true);
                }
                break;
            case "dirSelected":
                populateLists((File)dirList.getSelected());
                if (chooseMode == fileChooseMode.EXISTING_DIRECTORIES) {
                    selectedFileDisplay.setText(currentLocation.getText());                    
                    okButton.setIsEnabled(true);
                }
                else {
                    selectedFileDisplay.setText("");
                    okButton.setIsEnabled(false);
                }
                ((DefaultView)Main.getView()).setDoTransition(true);
                break;
            case "rootSelected":
                populateLists(new File(rootSelector.getItem(rootSelector.getSelectionIndex())));
                break;
            case "listWheel":
                ListControl lc = (ListControl)e.getSource();
                if (lc == fileList) {
                    fileScroll.setValue(fileList.getScroll());                   
                }
                else if (lc == dirList) {
                    dirScroll.setValue(dirList.getScroll());
                }
                break;
        }
    }    

    @Override
    public void doLayout() {
        super.doLayout(); //To change body of generated methods, choose Tools | Templates.
        
        okButton.setBounds(bounds.width - 220, 10, 100, 25);
        cancelButton.setBounds(bounds.width - 110, 10, 100, 25);
        
        dirList.setBounds(10, 50, bounds.width/2 - 50, bounds.height - 150);
        dirLabel.setBounds(10, 50+bounds.height-150, dirList.getBounds().width, 25);
        fileList.setBounds(bounds.width/2, 50, bounds.width/2 - 35, bounds.height - 150);
        fileLabel.setBounds(fileList.getBounds().x, 50+bounds.height-150, fileList.getBounds().width, 25);
        
        Rectangle listBounds = dirList.getBounds();
        dirScroll.setBounds(listBounds.x + listBounds.width, listBounds.y, 25, listBounds.height);
        listBounds = fileList.getBounds();
        fileScroll.setBounds(listBounds.x + listBounds.width, listBounds.y, 25, listBounds.height);
        
        dirScroll.setPageLength((float)dirList.itemsDisplayed()/dirList.size());
        fileScroll.setPageLength((float)fileList.itemsDisplayed()/fileList.size());
        
        rootSelector.setBounds(10, bounds.height-75, 130, 25);
        currentLocation.setBounds(230, bounds.height - 75, bounds.width - 240, 25);
        selectedFileDisplay.setBounds(150, 10, bounds.width - 380, 25);
        
        titleDisplay.setBounds(10, bounds.height-50, bounds.width-20, 50);
    }
}
