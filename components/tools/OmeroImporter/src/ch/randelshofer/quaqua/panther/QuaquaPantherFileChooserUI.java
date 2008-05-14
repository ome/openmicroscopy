/*
 * @(#)QuaquaPantherFileChooserUI.java  1.7  2006-04-08
 *
 * Copyright (c) 2004-2006 Werner Randelshofer
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * http://www.randelshofer.ch
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Werner Randelshofer. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Werner Randelshofer.
 */

package ch.randelshofer.quaqua.panther;

import ch.randelshofer.quaqua.*;
import ch.randelshofer.quaqua.filechooser.*;
import ch.randelshofer.quaqua.panther.filechooser.*;
import ch.randelshofer.quaqua.util.*;

//import ch.randelshofer.gui.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.tree.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.beans.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
//import java.util.regex.*;

/**
 * A replacement for the AquaFileChooserUI. Provides a column view similar
 * to the one provided with the native Aqua user interface on Mac OS X 10.3
 * (Panther).
 *
 * @author Werner Randelshofer
 * @version 1.7 2006-04-08 Use BasicFileView when UIManager property 
 * FileChooser.speed is set to true.
 * <br>1.6.6 2006-03-28 Update approve button state on text change in
 * filename field.
 * <br>1.6.5 2006-03-22 New folder was created in parent directory
 * when a folder was selected.
 * <br>1.6.4 2006-03-15 Couldn't save a new file.
 * <br>1.6.3 2006-03-14 Name of non-existing file was not written into
 * filename field.
 * <br>1.6.2 2006-03-05 Approve button did not work properly for multiple
 * selection. Disallow approval on double clicks on a directory.
 * <br>1.6.1 2006-02-06 Take infos from AliasFileSystemTreeModel.Node
 * instead of from File objects to reduce the amount of IO operations.
 * <br>1.6 2005-11-26 VolumesRenderer retrieves file icons from the
 * SidebarListModel.
 * <br>1.5.2 2005-11-13 Don't change text of fileNameTextField, when it
 * has focus.
 * <br>1.5.1 2005-11-07 Get "Labels" resource bundle from UIManager.
 * <br>1.5 2005-09-11 Celltips for browser added. Allways display
 * horizontal scroll bar of JBrowser.
 * <br>1.4.4 2005-08-25 Fixed a null pointer exception.
 * <br>1.4.3 2005-06-21 SplitPane divider size configurable by LookAndFeel.
 * <br>1.4.2 2005-06-05 Moved calls to System.getProperty into QuaquaManager.
 * <br>1.4.1 2005-04-22 Tweaked the layout.
 * <br>1.4 2005-03-22 Show as much path components as possible of
 * non-existent files in the directory combo box. Use Locale.getDefault()
 * in case the JFileChooser component can not provide a locale.
 * <br>1.3.1 2004-12-28 Call clearIconCache() on the IconView, if this
 * method is available.
 * <br>1.3 2004-11-01 Support for dropping files on the file chooser
 * added. File and directory selection is now handled in a more straightforward
 * way - this may affect code that depends on the values returned by methods
 * JFileChooser.getSelectedFiles(), JFileChooser.getSelectedFile() while the
 * file chooser is being shown.
 * 1.2.2 2004-10-26 setFileSelected had no effect, when a file with
 * a relative path was used.
 * <br>1.2.1 2004-10-21 Double clicking the browsers approves the selection.
 * When there is an approvable selection, the approve button is made the default
 * button.
 * <br>1.2 2004-10-17 Resolve alias files. Do not clear fileName text
 * field, when the user selects a directory. Volumes list did not update
 * properly when a volume was added/removed. Handle relative path names.
 * Display the real name of a file in the file name text field. Enable the
 * approve button only, when the right kind of file (or directory) is selected.
 * Treat non-traversable directories like files. Selecting a file in the volumes
 * list, sets the current directory to that file. Fixed a bug where the file
 * name in the file name field was used to determine the approved file altough
 * the field was not visible.
 * <br>1.1.2 2004-09-11 Fixed IndexOutOfBoundsException caused by method
 * addItem of inner class DirectoryComboBoxModel. Replaced all method
 * invocations to method QuaquaManager.getProperty to
 * QuaquaManager.getProperty.
 * <br>1.1.1 2004-08-28 Set the FileView of the JFileChooser.
 * <br>1.1 2004-07-31 Set the text of the approve button to "Choose", when
 * directory selection is enabled. Fixed layout issue with fileNameTextField.
 * 1.0 2004-07-04 Created.
 */
public class QuaquaPantherFileChooserUI extends BasicFileChooserUI {
    
    // Implementation derived from MetalFileChooserUI
    private static Log log = LogFactory.getLog(QuaquaPantherFileChooserUI.class);
    
    /* Models. */
    private DirectoryComboBoxModel directoryComboBoxModel;
    private Action directoryComboBoxAction = new DirectoryComboBoxAction();
    private FileView fileView;
    private FilterComboBoxModel filterComboBoxModel;
    private AliasFileSystemTreeModel model = null;
    private SubtreeTreeModel subtreeModel = null;
    
    // Preferred and Minimum sizes for the dialog box
    
    private static int PREF_WIDTH = 518;
    private static int PREF_HEIGHT = 378;
    private static Dimension PREF_SIZE = new Dimension(PREF_WIDTH, PREF_HEIGHT);
    
    private static int MIN_WIDTH = 518;
    private static int MIN_HEIGHT = 378;
    private static Dimension MIN_SIZE = new Dimension(MIN_WIDTH, MIN_HEIGHT);
    
    // Labels, mnemonics, and tooltips (oh my!)
    private int    fileNameLabelMnemonic = 0;
    private String fileNameLabelText = null;
    
    private int    filesOfTypeLabelMnemonic = 0;
    private String filesOfTypeLabelText = null;
    
    private String upFolderToolTipText = null;
    private String upFolderAccessibleName = null;
    
    private String homeFolderToolTipText = null;
    private String homeFolderAccessibleName = null;
    
    private String newFolderText = null;
    private String newFolderToolTipText = null;
    private String newFolderAccessibleName = null;
    
    protected String chooseButtonText = null;
    
    private String
    newFolderDialogPrompt,
    newFolderDefaultName,
    newFolderErrorText,
    newFolderExistsErrorText,
    newFolderTitleText;
    
    private final static File computer = AliasFileSystemTreeModel.COMPUTER;
    
    private SidebarListModel sidebarListModel;
    
    /**
     * This listener is used to determine whether the JFileChooser is showing.
     */
    private AncestorListener ancestorListener;
    
    /**
     * This listener is used to handle files that were dropped on the file chooser.
     */
    private FileTransferHandler fileTransferHandler;
    
    /**
     * Actions.
     */
    private Action newFolderAction = new NewFolderAction();
    private Action approveSelectionAction = new QuaquaApproveSelectionAction();
    
    
    /**
     * Values greater zero indicate that the UI is adjusting.
     * This is required to prevent the UI from changing the FileChooser's state
     * while processing a PropertyChangeEvent fired from the FileChooser.
     */
    private int isAdjusting = 0;
    
    // Variables declaration - do not modify
    // FIXME - accessoryPanel could be moved up to BasicFileChooserUI.
    private javax.swing.JPanel accessoryPanel;
    private javax.swing.JButton approveButton;
    private ch.randelshofer.quaqua.JBrowser browser;
    private javax.swing.JScrollPane browserScrollPane;
    private javax.swing.JToggleButton browserToggleButton;
    private javax.swing.JPanel cancelOpenPanel;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel controlsPanel;
    private javax.swing.JComboBox directoryComboBox;
    private javax.swing.JLabel fileNameLabel;
    private javax.swing.JPanel fileNamePanel;
    private javax.swing.JPanel fileNameSpringPanel;
    private javax.swing.JTextField fileNameTextField;
    private javax.swing.JLabel filesOfTypeLabel;
    private javax.swing.JComboBox filterComboBox;
    private javax.swing.JPanel formatPanel;
    private javax.swing.JPanel formatSpringPanel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel navigationButtonsPanel;
    private javax.swing.JPanel navigationPanel;
    private javax.swing.JPanel navigationSpringPanel;
    private javax.swing.JButton newFolderButton;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton previousButton;
    private javax.swing.JSeparator separator;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JTable table;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JToggleButton tableToggleButton;
    private javax.swing.ButtonGroup viewGroup;
    private javax.swing.JPanel viewsPanel;
    private javax.swing.JList sidebarList;
    private javax.swing.JScrollPane sidebarScrollPane;
    
    // End of variables declaration
    
    //
    // ComponentUI Interface Implementation methods
    //
    public static ComponentUI createUI(JComponent c) {
        return new QuaquaPantherFileChooserUI((JFileChooser) c);
    }
    
    public QuaquaPantherFileChooserUI(JFileChooser filechooser) {
        super(filechooser);
    }
    
    public void installComponents(JFileChooser fc) {
        FileSystemView fsv = fc.getFileSystemView();
        
        sidebarList = new javax.swing.JList() {
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = 10;
                return d;
            }
        };
        
        // Form definition  - do not modify
        java.awt.GridBagConstraints gridBagConstraints;
        
        viewGroup = new javax.swing.ButtonGroup();
        fileNamePanel = new javax.swing.JPanel();
        fileNameLabel = new javax.swing.JLabel();
        fileNameTextField = new javax.swing.JTextField();
        fileNameSpringPanel = new javax.swing.JPanel();
        separator = new javax.swing.JSeparator();
        mainPanel = new javax.swing.JPanel();
        navigationPanel = new javax.swing.JPanel();
        navigationButtonsPanel = new javax.swing.JPanel();
        previousButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        tableToggleButton = new javax.swing.JToggleButton();
        browserToggleButton = new javax.swing.JToggleButton();
        directoryComboBox = new javax.swing.JComboBox();
        navigationSpringPanel = new javax.swing.JPanel();
        splitPane = new javax.swing.JSplitPane();
        sidebarScrollPane = new javax.swing.JScrollPane();
        //sidebarList = new javax.swing.JList();
        viewsPanel = new javax.swing.JPanel();
        browserScrollPane = new javax.swing.JScrollPane();
        browser = new ch.randelshofer.quaqua.JBrowser();
        tableScrollPane = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        controlsPanel = new javax.swing.JPanel();
        accessoryPanel = new javax.swing.JPanel();
        formatPanel = new javax.swing.JPanel();
        filesOfTypeLabel = new javax.swing.JLabel();
        filterComboBox = new javax.swing.JComboBox();
        formatSpringPanel = new javax.swing.JPanel();
        buttonsPanel = new javax.swing.JPanel();
        cancelOpenPanel = new javax.swing.JPanel();
        newFolderButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        approveButton = new javax.swing.JButton();
        
        fc.setLayout(new java.awt.BorderLayout());
        
        fc.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(6, 0, 10, 0)));
        fileNamePanel.setLayout(new java.awt.GridBagLayout());
        
        fileNamePanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(4, 0, 3, 0)));
        fileNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        fileNameLabel.setText("Save As:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 0, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 0.5;
        fileNamePanel.add(fileNameLabel, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 250;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 0, 0);
        fileNamePanel.add(fileNameTextField, gridBagConstraints);
        
        fileNameSpringPanel.setLayout(null);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        fileNamePanel.add(fileNameSpringPanel, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        gridBagConstraints.weightx = 1.0;
        fileNamePanel.add(separator, gridBagConstraints);
        
        fc.add(fileNamePanel, java.awt.BorderLayout.NORTH);
        
        mainPanel.setLayout(new java.awt.BorderLayout());
        
        navigationPanel.setLayout(new java.awt.GridBagLayout());
        
        navigationPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 8, 4, 8)));
        navigationButtonsPanel.setLayout(new java.awt.GridBagLayout());
        
        previousButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/randelshofer/quaqua/panther/images/FileChooser.previousIcon.png")));
        navigationButtonsPanel.add(previousButton, new java.awt.GridBagConstraints());
        
        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/randelshofer/quaqua/panther/images/FileChooser.nextIcon.png")));
        navigationButtonsPanel.add(nextButton, new java.awt.GridBagConstraints());
        
        tableToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/randelshofer/quaqua/panther/images/FileChooser.tableIcon.png")));
        viewGroup.add(tableToggleButton);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        navigationButtonsPanel.add(tableToggleButton, gridBagConstraints);
        
        browserToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/randelshofer/quaqua/panther/images/FileChooser.browserIcon.png")));
        browserToggleButton.setSelected(true);
        viewGroup.add(browserToggleButton);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        navigationButtonsPanel.add(browserToggleButton, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        navigationPanel.add(navigationButtonsPanel, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.ipadx = 250;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 0, 0);
        navigationPanel.add(directoryComboBox, gridBagConstraints);
        
        navigationSpringPanel.setLayout(null);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        navigationPanel.add(navigationSpringPanel, gridBagConstraints);
        
        mainPanel.add(navigationPanel, java.awt.BorderLayout.NORTH);
        
        splitPane.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 8, 0, 8)));
        splitPane.setDividerLocation(130);
        //splitPane.setDividerSize(4);
        sidebarScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sidebarList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        sidebarScrollPane.setViewportView(sidebarList);
        
        splitPane.setLeftComponent(sidebarScrollPane);
        
        viewsPanel.setLayout(new java.awt.CardLayout());
        
        browserScrollPane.setViewportView(browser);
        
        viewsPanel.add(browserScrollPane, "browser");
        
        tableScrollPane.setViewportView(table);
        
        viewsPanel.add(tableScrollPane, "table");
        
        splitPane.setRightComponent(viewsPanel);
        
        mainPanel.add(splitPane, java.awt.BorderLayout.CENTER);
        
        fc.add(mainPanel, java.awt.BorderLayout.CENTER);
        
        controlsPanel.setLayout(new javax.swing.BoxLayout(controlsPanel, javax.swing.BoxLayout.Y_AXIS));
        
        accessoryPanel.setLayout(new java.awt.BorderLayout());
        
        accessoryPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(6, 8, 0, 8)));
        controlsPanel.add(accessoryPanel);
        
        formatPanel.setLayout(new java.awt.GridBagLayout());
        
        formatPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(6, 0, 0, 0)));
        filesOfTypeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        filesOfTypeLabel.setText("Format:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        formatPanel.add(filesOfTypeLabel, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.ipadx = 250;
        formatPanel.add(filterComboBox, gridBagConstraints);
        
        formatSpringPanel.setLayout(null);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        formatPanel.add(formatSpringPanel, gridBagConstraints);
        
        controlsPanel.add(formatPanel);
        
        buttonsPanel.setLayout(new java.awt.GridBagLayout());
        
        buttonsPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(6, 20, 0, 20)));
        newFolderButton.setText("New Folder");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        buttonsPanel.add(newFolderButton, gridBagConstraints);
        
        cancelOpenPanel.setLayout(new java.awt.GridLayout(1, 0, 8, 0));
        
        cancelButton.setText("Cancel");
        cancelOpenPanel.add(cancelButton);
        
        approveButton.setText("Open");
        cancelOpenPanel.add(approveButton);
        
        buttonsPanel.add(cancelOpenPanel, new java.awt.GridBagConstraints());
        
        //controlsPanel.add(buttonsPanel);
        
        fc.add(controlsPanel, java.awt.BorderLayout.SOUTH);
        // End of form definition
        
        // Tweak visual properties
        int dividerSize = UIManager.getInt("FileChooser.splitPaneDividerSize");
        if (dividerSize != 0) {
            splitPane.setDividerSize(dividerSize);
        }
        
        int h;
        h = fileNameLabel.getPreferredSize().height;
        fileNameLabel.setMinimumSize(new Dimension(0,h));
        fileNameLabel.setPreferredSize(new Dimension(0,h));
        fileNameLabel.setMaximumSize(new Dimension(32767,h));
        
        h = fileNameTextField.getPreferredSize().height;
        fileNameTextField.setPreferredSize(new Dimension(0,h));
        fileNameTextField.setMinimumSize(new Dimension(0,h));
        fileNameTextField.setMaximumSize(new Dimension(32767,h));
        
        h = directoryComboBox.getPreferredSize().height;
        directoryComboBox.setPreferredSize(new Dimension(0,h));
        directoryComboBox.setMinimumSize(new Dimension(0,h));
        directoryComboBox.setMaximumSize(new Dimension(32767,h));
        
        Dimension d = new Dimension(28,25);
        Dimension d2 = new Dimension(29,25);
        previousButton.setPreferredSize(d);
        nextButton.setPreferredSize(d2);
        tableToggleButton.setPreferredSize(d);
        browserToggleButton.setPreferredSize(d2);
        previousButton.setMinimumSize(d);
        nextButton.setMinimumSize(d2);
        tableToggleButton.setMinimumSize(d);
        browserToggleButton.setMinimumSize(d2);
        
        previousButton.setVisible(false);
        nextButton.setVisible(false);
        tableToggleButton.setVisible(false);
        browserToggleButton.setVisible(false);
        
        h = navigationButtonsPanel.getPreferredSize().height;
        navigationButtonsPanel.setMinimumSize(new Dimension(0,h));
        navigationButtonsPanel.setPreferredSize(new Dimension(0,h));
        navigationButtonsPanel.setMaximumSize(new Dimension(32767,h));
        
        h = filesOfTypeLabel.getPreferredSize().height;
        filesOfTypeLabel.setMinimumSize(new Dimension(0,h));
        filesOfTypeLabel.setPreferredSize(new Dimension(0,h));
        filesOfTypeLabel.setMaximumSize(new Dimension(32767,h));
        
        h = filterComboBox.getPreferredSize().height;
        filterComboBox.setPreferredSize(new Dimension(0,h));
        filterComboBox.setMinimumSize(new Dimension(0,h));
        filterComboBox.setMaximumSize(new Dimension(32767,h));
        
        
        //Configure JBrowser
        browser.setCellRenderer(
        new FileRenderer(
        fc,
        UIManager.getIcon("Browser.expandingIcon"),
        UIManager.getIcon("Browser.expandedIcon"),
        UIManager.getIcon("Browser.selectedExpandingIcon"),
        UIManager.getIcon("Browser.selectedExpandedIcon")
        )
        );
        if (fc.isMultiSelectionEnabled()) {
            browser.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        } else {
            browser.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        }
        browser.setModel(getTreeModel());
        browser.setPrototypeCellValue(getFileSystemTreeModel().getPrototypeValue());
        browser.addTreeSelectionListener(createBrowserSelectionListener(fc));
        browser.addMouseListener(createDoubleClickListener(fc));
        browser.setFixedCellWidth(170);
        browserScrollPane.putClientProperty("Quaqua.Component.visualMargin",new Insets(3,2,3,2));
        browserScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        browser.setShowCellTipOrigin((Point) UIManager.get("FileChooser.cellTipOrigin"));
        browser.setShowCellTips(true);
        
        // Configure Sidebar Panel
        sidebarScrollPane.putClientProperty("Quaqua.Component.visualMargin",new Insets(3,2,3,2));
        
        // Configure Format Panel
        formatPanel.setVisible(fc.getChoosableFileFilters().length > 1);
        
        // Configure Accessory Panel
        JComponent accessory = fc.getAccessory();
        if(accessory != null) {
            getAccessoryPanel().add(accessory);
        } else {
            accessoryPanel.setVisible(false);
        }
        
        // Configure filename Panel
        separator.putClientProperty("Quaqua.Component.visualMargin",new Insets(3,0,3,0));
        
        // Text assignment
        newFolderButton.setText(newFolderText);
        newFolderButton.setToolTipText(newFolderToolTipText);
        fileNameLabel.setText(fileNameLabelText);
        fileNameLabel.setDisplayedMnemonic(fileNameLabelMnemonic);
        
        approveButton.setText(getApproveButtonText(fc));
        // Note: Metal does not use mnemonics for approve and cancel
        approveButton.addActionListener(getApproveSelectionAction());
        approveButton.setToolTipText(getApproveButtonToolTipText(fc));
        
        cancelButton.setText(cancelButtonText);
        cancelButton.setToolTipText(cancelButtonToolTipText);
        cancelButton.addActionListener(getCancelSelectionAction());
        
        if(! fc.getControlButtonsAreShown()) {
            cancelButton.setVisible(false);
            approveButton.setVisible(false);
        }
        // End of Text assignment
        
        // Model and Renderer assignment
        directoryComboBoxModel = createDirectoryComboBoxModel(fc);
        directoryComboBox.setModel(directoryComboBoxModel);
        directoryComboBox.setRenderer(createDirectoryComboBoxRenderer(fc));
        sidebarList.setModel(sidebarListModel = new SidebarListModel(fc, new TreePath(getFileSystemTreeModel().getRoot()),getFileSystemTreeModel()));
        sidebarList.setCellRenderer(createVolumesRenderer(fc));
        filterComboBoxModel = createFilterComboBoxModel();
        filterComboBox.setModel(filterComboBoxModel);
        filterComboBox.setRenderer(createFilterComboBoxRenderer());
        // Model and Renderer assignment
        
        // Listener assignment
        directoryComboBox.addActionListener(directoryComboBoxAction);
        newFolderButton.addActionListener(getNewFolderAction());
        fileNameTextField.addFocusListener(new SaveTextFocusListener());
        fileNameTextField.getDocument()
        .addDocumentListener(new SaveTextDocumentListener());
        fileNameTextField.addActionListener(getApproveSelectionAction());
        sidebarList.addListSelectionListener(createVolumesSelectionListener(fc));
        // End of listener assignment
        
        // Transparent colors
        /*
        Color transparent = new Color(255,255,255,0);
            accessoryPanel.setBackground(transparent);
            buttonsPanel.setBackground(transparent);
            controlsPanel.setBackground(transparent);
            fileNamePanel.setBackground(transparent);
            fileNameSpringPanel.setBackground(transparent);
            formatPanel.setBackground(transparent);
            formatSpringPanel.setBackground(transparent);
            mainPanel.setBackground(transparent);
            navigationButtonsPanel.setBackground(transparent);
            navigationPanel.setBackground(transparent);
            navigationSpringPanel.setBackground(transparent);
            viewsPanel.setBackground(transparent);
         */
        
        // Drag and drop assignment
        fileTransferHandler = new FileTransferHandler(fc);
        Component[] dropComponents = {
            fc,
            accessoryPanel,
            approveButton,
            browser,
            browserScrollPane,
            browserToggleButton,
            buttonsPanel,
            cancelButton,
            controlsPanel,
            directoryComboBox,
            fileNameLabel,
            fileNamePanel,
            fileNameSpringPanel,
            fileNameTextField,
            filesOfTypeLabel,
            filterComboBox,
            
            formatPanel,
            
            formatSpringPanel,
            
            mainPanel,
            
            navigationButtonsPanel,
            
            navigationPanel,
            
            navigationSpringPanel,
            newFolderButton,
            nextButton,
            previousButton,
            separator,
            splitPane,
            table,
            tableScrollPane,
            tableToggleButton,
            
            viewsPanel,
            sidebarList,
            sidebarScrollPane
        };
        for (int i=0; i < dropComponents.length; i++) {
            new DropTarget(dropComponents[i], DnDConstants.ACTION_COPY,fileTransferHandler);
        }
        // End of drag and drop assignment
        
        // Change component visibility to match the dialog type
        boolean isSave = (fc.getDialogType() == JFileChooser.SAVE_DIALOG)
        || (fc.getDialogType() == JFileChooser.CUSTOM_DIALOG);
        fileNameTextField.setEnabled(isSave);
        fileNamePanel.setVisible(isSave);
        
        // Preview column
        //browser.setPreviewRenderer(new FilePreview());
        ResourceBundleUtil bundle = (ResourceBundleUtil) UIManager.get("Labels");
        boolean isLocalized = bundle.getLocale().getLanguage().equals(getLocale().getLanguage());
        browser.setPreviewRenderer((isSave || ! isLocalized) ? null : new FilePreview(fc));
    }
    
    public void uninstallComponents(JFileChooser fc) {
        fc.removeAll();
        
        // Remove listeners on UI components
        cancelButton.removeActionListener(getCancelSelectionAction());
        approveButton.removeActionListener(getApproveSelectionAction());
        fileNameTextField.removeActionListener(getApproveSelectionAction());
    }
    
    /**
     * Installs listeners.
     * We install the same listeners as BasicFileChooserUI plus an
     * AncestorListener and a property change listener.
     */
    protected void installListeners(JFileChooser fc) {
        super.installListeners(fc);
        ancestorListener = createAncestorListener(fc);
        if(ancestorListener != null) {
            fc.addAncestorListener(ancestorListener);
        }
        fc.addPropertyChangeListener(filterComboBoxModel);
    }
    protected void uninstallListeners(JFileChooser fc) {
        super.uninstallListeners(fc);
        if(ancestorListener != null) {
            fc.removeAncestorListener(ancestorListener);
        }
        fc.removePropertyChangeListener(filterComboBoxModel);
    }
    
    private Locale getLocale() {
        try {
            return getFileChooser().getLocale();
        } catch (IllegalComponentStateException e) {
            return Locale.getDefault();
        }
    }
    
    protected void installStrings(JFileChooser fc) {
        super.installStrings(fc);
        
        Locale l;
        try {
            l = getLocale();
        } catch (IllegalComponentStateException e) {
            l = Locale.getDefault();
        }
        
        
        // FIXME - We must not read these strings from the UIManager, as long
        //         as we don't provide them with our own Look and Feel. This
        //         is, because these strings are version dependent, and thus
        //         are not necessarily in sync with what we need in our UI.
        chooseButtonText   = UIManager.getString("FileChooser.chooseButtonText"/*,l*/);
        
        fileNameLabelMnemonic = UIManager.getInt("FileChooser.fileNameLabelMnemonic");
        fileNameLabelText = UIManager.getString("FileChooser.fileNameLabelText"/*,l*/);
        // XXX - Localize "Save as:" text.
        //if (fileNameLabelText == null || fileNameLabelText.charAt(fileNameLabelText.length() -1) != ':') fileNameLabelText = "Save as:";
        
        filesOfTypeLabelMnemonic = UIManager.getInt("FileChooser.filesOfTypeLabelMnemonic");
        filesOfTypeLabelText = UIManager.getString("FileChooser.filesOfTypeLabelText"/*,l*/);
        
        upFolderToolTipText =  UIManager.getString("FileChooser.upFolderToolTipText"/*,l*/);
        upFolderAccessibleName = UIManager.getString("FileChooser.upFolderAccessibleName"/*,l*/);
        
        homeFolderToolTipText =  UIManager.getString("FileChooser.homeFolderToolTipText"/*,l*/);
        homeFolderAccessibleName = UIManager.getString("FileChooser.homeFolderAccessibleName"/*,l*/);
        
        newFolderToolTipText = UIManager.getString("FileChooser.newFolderText"/*,l*/);
        newFolderAccessibleName = UIManager.getString("FileChooser.newFolderAccessibleName"/*,l*/);
        
        newFolderToolTipText = UIManager.getString("FileChooser.newFolderToolTipText"/*,l*/);
        newFolderAccessibleName = UIManager.getString("FileChooser.newFolderAccessibleName"/*,l*/);
        
        // New Folder Dialog
        newFolderErrorText = getString("FileChooser.newFolderErrorText", l, "Error occured during folder creation");
        newFolderExistsErrorText = getString("FileChooser.newFolderExistsErrorText", l, "That name is already taken");
        // FIXME - There is no "FileChooser.newFolderText", so we use the newFolderTitleText.
        newFolderText = getString("FileChooser.newFolderTitleText", l, "New Folder");
        newFolderTitleText = getString("FileChooser.newFolderTitleText", l, "New Folder");
        newFolderDialogPrompt = getString("FileChooser.newFolderPromptText", l, "Name of new folder:");
        newFolderDefaultName = getString("FileChooser.untitledFolderName", l, "untitled folder");
        newFolderToolTipText = UIManager.getString("FileChooser.newFolderToolTipText"/*, l*/);
        newFolderAccessibleName = getString("FileChooser.newFolderAccessibleName", l, newFolderTitleText);
    }
    
    /**
     * FIXME - This could be moved up to BasicFileChooserUI.
     */
    public JPanel getAccessoryPanel() {
        return accessoryPanel;
    }
    
    /**
     * Gets a locale dependent string.
     */
    private String getString(String string, Locale l, String defaultValue) {
        String value = UIManager.getString(string/*, l*/);
        return (value == null) ? defaultValue : value;
    }
    
    /**
     * Creates an AncestorListener.
     * The AncestorListener is used to take an action when the JFileChooser becomes
     * showing on screen.
     */
    protected AncestorListener createAncestorListener(JFileChooser fc) {
        return new FileChooserAncestorListener();
    }
    
    public void createModel() {
        JFileChooser fc = getFileChooser();
        model = new AliasFileSystemTreeModel(fc);
        subtreeModel = new SubtreeTreeModel(model);
        
        // FIXME - We should not overwrite the FileView attribute
        // of the JFileChooser.
        if (QuaquaManager.getBoolean("FileChooser.speed")) {
        fileView = new BasicFileView();
            } else {
        fileView = QuaquaFileSystemView.getQuaquaFileSystemView().createFileView(fc);
        }
        fc.setFileView(fileView);
        
        // FIXME - We should not overwrite the FileSystemView attribute
        // of the JFileChooser.
        fc.setFileSystemView(QuaquaFileSystemView.getQuaquaFileSystemView());
    }
    public SubtreeTreeModel getTreeModel() {
        return subtreeModel;
    }
    public AliasFileSystemTreeModel getFileSystemTreeModel() {
        return model;
    }
    
    /**
     * The array contains the selected file(s) of the JFileChooser.
     * All files have an absolute path.
     * If no file is selected, the length of the array is 0.
     * Always returns a non-null value.
     * All array elements are non-null.
     */
    private File[] getSelectedFiles() {
        JFileChooser fc = getFileChooser();
        
        if (fc.isMultiSelectionEnabled()) {
            File[] selectedFiles = fc.getSelectedFiles();
            ArrayList list = new ArrayList(selectedFiles.length);
            for (int i=0; i < selectedFiles.length; i++) {
                if (selectedFiles[i] != null) {
                    if (selectedFiles[i].isAbsolute()) {
                        list.add(selectedFiles[i]);
                    } else {
                        list.add(new File(QuaquaManager.getProperty("user.home"),selectedFiles[i].getName()));
                    }
                }
            }
            return (File[]) list.toArray(new File[list.size()]);
        } else {
            File f = fc.getSelectedFile();
            if (f == null) {
                return new File[0];
            } else {
                if (f.isAbsolute()) {
                    return new File[] { f };
                } else {
                    return new File[] { new File(QuaquaManager.getProperty("user.home"),f.getName()) };
                }
            }
        }
    }
    
    /**
     * Updates the selection in the JBrowser, to match the selected file/s
     * of the JFileChooser.
     */
    private void updateSelection() {
        JFileChooser fc = getFileChooser();
        File[] files = getSelectedFiles();
        TreePath fullPath = null;
        
        boolean isAtLeastOneFileSelected = false;
        boolean isAtLeastOneDirSelected = false;
        
        if (files.length != 0) {
            TreePath[] paths = new TreePath[files.length];
            ArrayList list = new ArrayList(paths.length);
            
            TreePath commonParentPath = null;
            for (int i=0; i < files.length; i++) {
                File file = files[i];
                
                fullPath = getFileSystemTreeModel().toPath(
                file,
                subtreeModel.toFullPath(browser.getSelectionPath())
                );
                AliasFileSystemTreeModel.Node node = (AliasFileSystemTreeModel.Node) fullPath.getLastPathComponent();
                
                boolean isDirectory = ! node.isLeaf();
                if (isDirectory) {
                    isAtLeastOneDirSelected = true;
                } else {
                    isAtLeastOneFileSelected = true;
                }
                
                if (files.length == 1 || ! isDirectory || fc.isDirectorySelectionEnabled()) {
                    TreePath subPath = getTreeModel().toSubPath(fullPath);
                    
                    // All parent paths must be equal
                    TreePath parentPath = (subPath == null) ? null : subPath.getParentPath();
                    if (list.size() == 0) {
                        commonParentPath = parentPath;
                    }
                    if (parentPath == null && commonParentPath == null
                    || parentPath != null && commonParentPath !=  null && parentPath.equals(commonParentPath)) {
                        list.add(subPath);
                    }
                }
            }
            if (list.size() == 0 && files.length > 0) {
                list.add(fc.getFileSystemView().getParentDirectory(files[0]));
                /*
                // Set the file name, if it does not exist
                if (! files[0].exists()) {
                setFileName(files[0].getName());
                }
                 */
            }
            browser.setSelectionPaths((TreePath[]) list.toArray(new TreePath[list.size()]));
            
            if (files.length == 1) {
                AliasFileSystemTreeModel.Node node = (AliasFileSystemTreeModel.Node) fullPath.getLastPathComponent();
                if (node.isLeaf() || ! files[0].exists()) {
                    setFileName(files[0].getName());
                }
            }
        }
        
        if (fullPath != null && fullPath.getPathCount() > 0) {
            AliasFileSystemTreeModel.Node node = (AliasFileSystemTreeModel.Node) fullPath.getLastPathComponent();
            directoryComboBoxModel.setPath(
            (node.isLeaf()) ?
            fullPath.getParentPath() :
                fullPath
                );
                
        }
        
        
        // Note: we are doing here redundant things as in method updateAproveButtonState()
        // for performance reason.
        boolean isEnabled = false;
        switch (fc.getFileSelectionMode()) {
            case JFileChooser.FILES_ONLY :
                isEnabled = isAtLeastOneFileSelected || isFileNameFieldVisible() && isFileNameFieldValid();
                break;
            case JFileChooser.DIRECTORIES_ONLY :
                isEnabled = ! isAtLeastOneFileSelected;
                break;
            case JFileChooser.FILES_AND_DIRECTORIES :
                isEnabled = true;
                break;
        }
        setApproveButtonEnabled(isEnabled);
        //updateApproveButtonState();
    }
    
    
    /**
     * Returns true, if the file name field contains a file name.
     */
    private boolean isFileNameFieldValid() {
        String string = getFileName();
        return string != null && !string.equals("");
    }
    /**
     * Returns true, if the file name field is visible.
     */
    private boolean isFileNameFieldVisible() {
        JFileChooser fc = getFileChooser();
        return (fc.getDialogType() == JFileChooser.SAVE_DIALOG)
        || (fc.getDialogType() == JFileChooser.CUSTOM_DIALOG);
    }
    
    private void updateApproveButtonState() {
        JFileChooser fc = getFileChooser();
        
        if (fc.getControlButtonsAreShown()) {
            File[] files = getSelectedFiles();
            
            boolean isFileSelected = false;
            boolean isDirectorySelected = false;
            for (int i=0; i < files.length; i++) {
                if (files[i].exists()) {
                    if (files[i].isDirectory() && fc.isTraversable(files[i])) {
                        isDirectorySelected = true;
                    } else {
                        isFileSelected = true;
                    }
                }
            }
            
            // Note: this code is redundant with the code in updateSelection()
            // for performance reason.
            boolean isEnabled = false;
            switch (fc.getFileSelectionMode()) {
                case JFileChooser.FILES_ONLY :
                    isEnabled = isFileSelected || isFileNameFieldVisible() && isFileNameFieldValid();
                    break;
                case JFileChooser.DIRECTORIES_ONLY :
                    isEnabled = ! isFileSelected || files.length == 1 && ! files[0].exists();
                    break;
                case JFileChooser.FILES_AND_DIRECTORIES :
                    isEnabled = true;
                    break;
            }
            setApproveButtonEnabled(isEnabled);
        }
    }
    private void setApproveButtonEnabled(boolean isEnabled) {
        JFileChooser fc = getFileChooser();
        if (fc.getControlButtonsAreShown()) {
            approveButton.setEnabled(isEnabled);
            if (isEnabled) {
                JRootPane rp = approveButton.getRootPane();
                if (rp != null) {
                    rp.setDefaultButton(approveButton);
                }
            }
        }
    }
    private void updateApproveButtonText() {
        JFileChooser fc = getFileChooser();
        
        approveButton.setText(getApproveButtonText(fc));
        approveButton
        .setToolTipText(getApproveButtonToolTipText(fc));
        approveButton.setMnemonic(getApproveButtonMnemonic(fc));
        //cancelButton.setToolTipText(getCancelButtonToolTipText(fc));
    }
    protected TreeSelectionListener createBrowserSelectionListener(JFileChooser fc) {
        return new BrowserSelectionListener();
    }
    /**
     * Selection listener for the list of files and directories.
     */
    protected class BrowserSelectionListener implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent e) {
            if (isAdjusting != 0) return;
            JFileChooser fc = getFileChooser();
            FileSystemView fsv = fc.getFileSystemView();
            TreePath path = browser.getSelectionPath();
            
            if (path != null) {
                model.lazyInvalidatePath(path);
                model.validatePath(path);
            }
            
            
            TreePath[] paths = browser.getSelectionPaths();
            
            // Determine the selected files. If multiple files are selected,
            // we strip directories from this list, if the JFileChooser does
            // not allow directory selection.
            int count = 0;
            File[] files = new File[(paths == null) ? 0 : paths.length];
            ArrayList list = new ArrayList(files.length);
            for (int i=0; i < files.length; i++) {
                AliasFileSystemTreeModel.Node node = (AliasFileSystemTreeModel.Node) paths[i].getLastPathComponent();
                File file = node.getFile();
                if (file != null) {
                    boolean isDirectory = ! node.isLeaf();
                    if (files.length == 1 || ! isDirectory || fc.isDirectorySelectionEnabled()) {
                        list.add(file);
                    }
                }
            }
            
            
            if (fc.isMultiSelectionEnabled()) {
                fc.setSelectedFiles((File[]) list.toArray(new File[list.size()]));
            } else {
                fc.setSelectedFile((list.size() > 0) ? (File) list.get(0) : null);
            }
        }
    }
    
    
    /**
     * Returns the preferred size of the specified
     * <code>JFileChooser</code>.
     * The preferred size is at least as large,
     * in both height and width,
     * as the preferred size recommended
     * by the file chooser's layout manager.
     *
     * @param c  a <code>JFileChooser</code>
     * @return   a <code>Dimension</code> specifying the preferred
     *           width and height of the file chooser
     */
    public Dimension getPreferredSize(JComponent c) {
        /*
        Dimension d = c.getLayout().preferredLayoutSize(c);
        if (d != null) {
            return new Dimension(
            Math.max(d.width, PREF_SIZE.width),
            Math.max(d.height, PREF_SIZE.height)
            );
        } else {
            return new Dimension(PREF_SIZE.width, PREF_SIZE.height);
        }*/
        return new Dimension(PREF_SIZE);
    }
    
    /**
     * Returns the minimum size of the <code>JFileChooser</code>.
     *
     * @param c  a <code>JFileChooser</code>
     * @return   a <code>Dimension</code> specifying the minimum
     *           width and height of the file chooser
     */
    public Dimension getMinimumSize(JComponent c) {
        return new Dimension(MIN_SIZE);
    }
    
    /**
     * Returns the maximum size of the <code>JFileChooser</code>.
     *
     * @param c  a <code>JFileChooser</code>
     * @return   a <code>Dimension</code> specifying the maximum
     *           width and height of the file chooser
     */
    public Dimension getMaximumSize(JComponent c) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
    
    
    /* The following methods are used by the PropertyChange Listener */
    
    private void doSelectedFileChanged(PropertyChangeEvent e) {
        updateSelection();
    }
    
    private void doSelectedFilesChanged(PropertyChangeEvent e) {
        updateSelection();
    }
    
    private void doDirectoryChanged(PropertyChangeEvent e) {
        JFileChooser fc = getFileChooser();
        FileSystemView fsv = fc.getFileSystemView();
        
        File[] files = getSelectedFiles();
        
        if (files.length == 0) {
            File dir = (File) e.getNewValue();
            TreePath path = model.toPath(dir, browser.getSelectionPath());
            directoryComboBoxModel.setPath(path);
            browser.setSelectionPath(path);
            model.lazyInvalidatePath(browser.getSelectionPath());
            
            if(dir != null) {
                getNewFolderAction().setEnabled(dir.canWrite());
                getChangeToParentDirectoryAction().setEnabled(!fsv.isRoot(dir));
                
                if (fc.getDialogType() == JFileChooser.OPEN_DIALOG) {
                    updateApproveButtonState();
                }
                
            }
        }
    }
    
    private void doFilterChanged(PropertyChangeEvent e) {
        clearIconCache();
        model.invalidatePath(browser.getSelectionPath());
        if (getFileChooser().isShowing()) {
            model.validatePath(browser.getSelectionPath());
        }
    }
    
    private void doFileSelectionModeChanged(PropertyChangeEvent e) {
        //Commented out, because there is no reason for clearing the icon cache
        //in this situation.
        //clearIconCache();
        
        JFileChooser fc = getFileChooser();
        File currentDirectory = fc.getCurrentDirectory();
        //setFileName(null);
        updateApproveButtonText();
        updateApproveButtonState();
    }
    
    private void doMultiSelectionChanged(PropertyChangeEvent e) {
        if (getFileChooser().isMultiSelectionEnabled()) {
            browser.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        } else {
            browser.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            getFileChooser().setSelectedFiles(null);
        }
    }
    
    private void doChoosableFilterChanged(PropertyChangeEvent e) {
        JFileChooser fc = getFileChooser();
        boolean isChooserVisible = ((FileFilter[]) e.getNewValue()).length > 1;
        formatPanel.setVisible(isChooserVisible);
    }
    private void doAccessoryChanged(PropertyChangeEvent e) {
        if(getAccessoryPanel() != null) {
            if(e.getOldValue() != null) {
                getAccessoryPanel().remove((JComponent) e.getOldValue());
            }
            JComponent accessory = (JComponent) e.getNewValue();
            if(accessory != null) {
                getAccessoryPanel().add(accessory, BorderLayout.CENTER);
            }
            accessoryPanel.setVisible(accessory != null);
        }
    }
    
    private void doApproveButtonTextChanged(PropertyChangeEvent e) {
        JFileChooser chooser = getFileChooser();
        approveButton.setText(getApproveButtonText(chooser));
        approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
    }
    
    private void doDialogTypeChanged(PropertyChangeEvent e) {
        JFileChooser fc = getFileChooser();
        approveButton.setText(getApproveButtonText(fc));
        approveButton.setToolTipText(getApproveButtonToolTipText(fc));
        boolean isSave = isFileNameFieldVisible();
        fileNameTextField.setEnabled(isSave);
        fileNamePanel.setVisible(isSave);
        model.setResolveAliasesToFiles(! isSave);
        ResourceBundleUtil bundle = (ResourceBundleUtil) UIManager.get("Labels");
        boolean isLocalized = bundle.getLocale().getLanguage().equals(getLocale().getLanguage());
        browser.setPreviewRenderer((isSave || ! isLocalized) ? null : new FilePreview(fc));
    }
    
    private void doApproveButtonMnemonicChanged(PropertyChangeEvent e) {
        // Note: Metal does not use mnemonics for approve and cancel
    }
    
    private void doControlButtonsChanged(PropertyChangeEvent e) {
        if(getFileChooser().getControlButtonsAreShown()) {
            addControlButtons();
        } else {
            removeControlButtons();
        }
    }
    
    /*
     * Listen for filechooser property changes, such as
     * the selected file changing, or the type of the dialog changing.
     */
    public PropertyChangeListener createPropertyChangeListener(JFileChooser fc) {
        return new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                isAdjusting++;
                
                String s = e.getPropertyName();
                if (s.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                    doSelectedFileChanged(e);
                } else if (s.equals(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY)) {
                    doSelectedFilesChanged(e);
                } else if(s.equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
                    doDirectoryChanged(e);
                } else if(s.equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY)) {
                    doFilterChanged(e);
                } else if(s.equals(JFileChooser.FILE_SELECTION_MODE_CHANGED_PROPERTY)) {
                    doFileSelectionModeChanged(e);
                } else if(s.equals(JFileChooser.MULTI_SELECTION_ENABLED_CHANGED_PROPERTY)) {
                    doMultiSelectionChanged(e);
                } else if(s.equals(JFileChooser.ACCESSORY_CHANGED_PROPERTY)) {
                    doAccessoryChanged(e);
                } else if(s.equals(JFileChooser.CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY)) {
                    doChoosableFilterChanged(e);
                } else if (s.equals(JFileChooser.APPROVE_BUTTON_TEXT_CHANGED_PROPERTY) ||
                s.equals(JFileChooser.APPROVE_BUTTON_TOOL_TIP_TEXT_CHANGED_PROPERTY)) {
                    doApproveButtonTextChanged(e);
                } else if(s.equals(JFileChooser.DIALOG_TYPE_CHANGED_PROPERTY)) {
                    doDialogTypeChanged(e);
                } else if(s.equals(JFileChooser.APPROVE_BUTTON_MNEMONIC_CHANGED_PROPERTY)) {
                    doApproveButtonMnemonicChanged(e);
                } else if(s.equals(JFileChooser.CONTROL_BUTTONS_ARE_SHOWN_CHANGED_PROPERTY)) {
                    doControlButtonsChanged(e);
                } else if (s.equals("componentOrientation")) {
                    /* FIXME - This needs JDK 1.4 to work.
                    ComponentOrientation o = (ComponentOrientation)e.getNewValue();
                    JFileChooser fc = (JFileChooser)e.getSource();
                    if (o != (ComponentOrientation)e.getOldValue()) {
                        fc.applyComponentOrientation(o);
                    }
                     */
                } else if (s.equals("ancestor")) {
                    if (e.getOldValue() == null && e.getNewValue() != null) {
                        // Ancestor was added, ensure path is visible and
                        // set initial focus
                        ensurePathIsVisible(subtreeModel.toFullPath(browser.getSelectionPath()));
                        fileNameTextField.selectAll();
                        fileNameTextField.requestFocus();
                    }
                }
                
                isAdjusting--;
            }
        };
    }
    
    protected void removeControlButtons() {
        cancelButton.setVisible(false);
        approveButton.setVisible(false);
    }
    
    protected void addControlButtons() {
        cancelButton.setVisible(true);
        approveButton.setVisible(true);
    }
    
    
    private void ensurePathIsVisible(TreePath path) {
        if (! subtreeModel.isSubPath(path.getPath())) {
            isAdjusting++;
            if (((AliasFileSystemTreeModel.Node) path.getLastPathComponent()).isLeaf()) {
                subtreeModel.setPathToRoot(path.getParentPath().getPath());
            } else {
                subtreeModel.setPathToRoot(path.getPath());
            }
            isAdjusting--;
        }
        browser.ensurePathIsVisible(path);
    }
    
    public String getFileName() {
        if (fileNameTextField != null) {
            return fileNameTextField.getText();
        } else {
            return null;
        }
    }
    
    public void setFileName(String filename) {
        if (fileNameTextField != null &&
        ! fileNameTextField.hasFocus() &&
        (filename == null || !fileNameTextField.getText().equals(filename))) {
            fileNameTextField.setText(filename);
        }
    }
    
    
    protected DirectoryComboBoxRenderer createDirectoryComboBoxRenderer(JFileChooser fc) {
        return new DirectoryComboBoxRenderer();
    }
    
    
    protected VolumesRenderer createVolumesRenderer(JFileChooser fc) {
        return new VolumesRenderer();
    }
    protected ListSelectionListener createVolumesSelectionListener(JFileChooser fc) {
        return new VolumesSelectionListener();
    }
    
    //
    // Renderer for DirectoryComboBox
    //
    class DirectoryComboBoxRenderer extends DefaultListCellRenderer  {
        IndentIcon ii = new IndentIcon();
        private Border border = new EmptyBorder(1,0,1,0);
        private JSeparator separator = new JSeparator();
        
        public DirectoryComboBoxRenderer() {
            separator.setPreferredSize(new Dimension(9,9));
        }
        
        public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected,
        boolean cellHasFocus) {
            
            
            // String objects are used to denote delimiters.
            if (value instanceof String) {
                super.getListCellRendererComponent(list, value, index, false, cellHasFocus);
                setText((String) value);
                setPreferredSize(new Dimension(10, 14));
                return this;
            }
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setPreferredSize(null);
            if (value instanceof File) {
                setText(value+" "+index);
                return this;
            }
            AliasFileSystemTreeModel.Node node = (AliasFileSystemTreeModel.Node) value;
            if (node == null) {
                return separator;
                /*
                File root = new File("/");
                setText(getFileChooser().getName(root));
                ii.icon = getFileChooser().getIcon(root);
                 */
            } else {
                setText(node.getUserName());
                ii.icon = node.getIcon();
            }
            ii.depth = 0;
            setIcon(ii);
            setBorder(border);
            return this;
        }
    }
    
    //
    // Renderer for Volumes list
    //
    class VolumesRenderer extends DefaultListCellRenderer   {
        /*
        private final  Border selectedBorder = new MatteBorder(1,0,1,0, new Color(7,131,216));
        private final  Border selectedBorderI = new MatteBorder(1,0,1,0, new Color(130,129,129));
        private final  Border normalBorder = new EmptyBorder(1,0,1,0);
        private final ColorPaint selectedBackground = new ColorPaint(0,96,255, new GradientPaint(0f,0f,new Color(62,155,228),0f,22f,new Color(0,96,255)));
        private final ColorPaint selectedBackgroundI = new ColorPaint(102,102,102, new GradientPaint(0f,0f,new Color(151,151,151),0f,22f,new Color(102,102,102)));
         */
        private Border border = new EmptyBorder(1,3,2,0);
        private JComponent separator = new JComponent() {
            public void paintComponent(Graphics g) {
                Dimension s = getSize();
                g.setColor(new Color(179,179,179));
                g.fillRect(3,s.height/2,s.width - 6,2);
            }
        };
        private int backgroundIndex;
        
        public VolumesRenderer() {
            separator.setPreferredSize(new Dimension(9,9));
        }
        /*
        public boolean isOpaque() {
            return false;
        }
         */
        protected void paintBorder(Graphics g) {
            // empty
        }
        protected void paintComponent(Graphics gr) {
            // Note: We draw here over the border insets
            //       This saves having borders for all three possible states
            
            Graphics2D g = (Graphics2D) gr;
            int width = getWidth();
            int height = getHeight();
            switch (backgroundIndex) {
                case 0 :
                    setOpaque(true);
                    break;
                case 1 :
                    g.setColor(new Color(7,131,216));
                    g.fillRect(0,0, width, 1);
                    g.setPaint(
                    new GradientPaint(
                    0f,1f,new Color(62,155,228),
                    0f,height - 2,new Color(0,96,255)
                    )
                    );
                    g.fillRect(0,1,width,height - 1);
                    setOpaque(false);
                    break;
                case 2 :
                    g.setColor(new Color(130,129,129));
                    g.fillRect(0,0, width, 1);
                    g.setPaint(
                    new GradientPaint(
                    0f,1f,new Color(151,151,151),
                    0f,height - 2,new Color(102,102,102)
                    )
                    );
                    g.fillRect(0,1,width,height - 1);
                    setOpaque(false);
                    break;
            }
            g.setColor(getForeground());
            super.paintComponent(g);
        }
        
        public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected,
        boolean cellHasFocus) {
            if (value instanceof File) {
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
            FileInfo info = (FileInfo) value;
            if (info == null) {
                return separator;
                /*
                File root = new File("/");
                setText(getFileChooser().getName(root));
                ii.icon = getFileChooser().getIcon(root);
                 */
            } else {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(info.getUserName());
                if (isSelected) {
                    if (QuaquaUtilities.isFocused(list)) {
                        backgroundIndex = 1;
                        //  setBorder(selectedBorder);
                        //  setBackground(selectedBackground);
                    } else {
                        backgroundIndex = 2;
                        //  setBorder(selectedBorderI);
                        //  setBackground(selectedBackgroundI);
                    }
                    setForeground(Color.white);
                } else {
                    backgroundIndex = 3;
                    //setBorder(normalBorder);
                }
                setIcon(info.getIcon());
                setBorder(border);
                return this;
            }
        }
    }
    final static int space = 10;
    class IndentIcon implements Icon {
        
        Icon icon = null;
        int depth = 0;
        
        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (icon != null) {
                if (c.getComponentOrientation().isLeftToRight()) {
                    icon.paintIcon(c, g, x+depth*space, y);
                } else {
                    icon.paintIcon(c, g, x, y);
                }
            }
        }
        
        public int getIconWidth() {
            return (icon == null) ? depth*space : icon.getIconWidth() + depth*space;
        }
        
        public int getIconHeight() {
            return (icon == null) ? 0 : icon.getIconHeight();
        }
        
    }
    
    //
    // DataModel for DirectoryComboxbox
    //
    protected DirectoryComboBoxModel createDirectoryComboBoxModel(JFileChooser fc) {
        return new DirectoryComboBoxModel();
    }
    
    /**
     * Data model for a directory selection combo-box.
     * There is always one node in the tree model: the file system root (aka
     * the comouter).
     */
    protected class DirectoryComboBoxModel extends AbstractListModel
    implements ComboBoxModel {
        TreePath path;
        AliasFileSystemTreeModel.Node selectedDirectory = null;
        JFileChooser chooser = getFileChooser();
        FileSystemView fsv = chooser.getFileSystemView();
        
        public DirectoryComboBoxModel() {
        }
        
        /**
         * Sets the path of the directory combo box.
         * TreePath<AliasFileSystemTreeModel.Node>
         */
        private void setPath(TreePath path) {
            if (this.path != null && this.path.getPathCount() > 0) {
                fireIntervalRemoved(this, 0, this.path.getPathCount() - 1);
            }
            this.path = path;
            if (this.path.getPathCount() > 0) {
                fireIntervalAdded(this, 0, this.path.getPathCount() - 1);
            }
            setSelectedItem(this.path.getLastPathComponent());
            
            /*
            // FIXME - This method needs optimization.
            // Change method signature: We must pass a AliasFileSystemTreeModel.Node
             
             
            File selectedDir = (File) getSelectedItem();
            if (selectedDir != null && directory != null &&
            selectedDir.equals(directory)) {
                return;
            }
            long start = System.currentTimeMillis();
             
            FileSystemView fsv = chooser.getFileSystemView();
            if (! (fsv instanceof QuaquaFileSystemView)) {
                fsv = QuaquaFileSystemView.getQuaquaFileSystemView();
            }
             
            isAdjusting++;
             
             
             
            int size = path.size();
            path.clear();
            if (size > 0) {
                fireIntervalRemoved(this, 0, size - 1);
            }
            /* Not needed. We require that the file is a directory in the API.
            while (directory != null && ! directory.isDirectory()) {
                directory = fsv.getParentDirectory(directory);
            }* /
            if (directory == null) directory = fsv.getRoots()[0];
             
            for (File f = directory; f != null; f = fsv.getParentDirectory(f)) {
                path.add(f);
            }
            if (path.size() == 0 || ! path.get(0).equals(computer)) {
                path.add(computer);
            }
             
            if (path.size() > 0) {
                fireIntervalAdded(this, 0, path.size() - 1);
            }
            setSelectedItem(directory);
             
            isAdjusting--;
            long end = System.currentTimeMillis();
            if (log.isDebugEnabled()) {
                log.debug("QuaquaPanttherFileChooserUI.DirectoryCombo.add "+(end-start));
            }
             */
        }
        
        public void setSelectedItem(Object selectedItem) {
            AliasFileSystemTreeModel.Node node = (AliasFileSystemTreeModel.Node) selectedItem;
            this.selectedDirectory = node;
            fireContentsChanged(this, -1, -1);
        }
        
        public Object getSelectedItem() {
            return selectedDirectory;
        }
        
        public int getSize() {
            return (path == null) ? 0 : path.getPathCount();
        }
        
        public Object getElementAt(int index) {
            return path.getPathComponent(path.getPathCount() - index - 1);
        }
    }
    
    //
    // Renderer for Types ComboBox
    //
    protected FilterComboBoxRenderer createFilterComboBoxRenderer() {
        return new FilterComboBoxRenderer();
    }
    
    /**
     * Render different type sizes and styles.
     */
    public class FilterComboBoxRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list,
        Object value, int index, boolean isSelected,
        boolean cellHasFocus) {
            
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value != null && value instanceof FileFilter) {
                setText(((FileFilter)value).getDescription());
            }
            
            return this;
        }
    }
    
    //
    // DataModel for Types Comboxbox
    //
    protected FilterComboBoxModel createFilterComboBoxModel() {
        return new FilterComboBoxModel();
    }
    
    /**
     * Data model for a type-face selection combo-box.
     */
    protected class FilterComboBoxModel
    extends AbstractListModel
    implements ComboBoxModel, PropertyChangeListener {
        protected FileFilter[] filters;
        protected FilterComboBoxModel() {
            super();
            filters = getFileChooser().getChoosableFileFilters();
        }
        
        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if(prop == JFileChooser.CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY) {
                filters = (FileFilter[]) e.getNewValue();
                fireContentsChanged(this, -1, -1);
            } else if (prop == JFileChooser.FILE_FILTER_CHANGED_PROPERTY) {
                fireContentsChanged(this, -1, -1);
            }
        }
        
        public void setSelectedItem(Object filter) {
            if(filter != null) {
                getFileChooser().setFileFilter((FileFilter) filter);
                setFileName(null);
                fireContentsChanged(this, -1, -1);
            }
        }
        
        public Object getSelectedItem() {
            // Ensure that the current filter is in the list.
            // NOTE: we shouldnt' have to do this, since JFileChooser adds
            // the filter to the choosable filters list when the filter
            // is set. Lets be paranoid just in case someone overrides
            // setFileFilter in JFileChooser.
            FileFilter currentFilter = getFileChooser().getFileFilter();
            boolean found = false;
            if(currentFilter != null) {
                for(int i=0; i < filters.length; i++) {
                    if(filters[i] == currentFilter) {
                        found = true;
                    }
                }
                if(found == false) {
                    getFileChooser().addChoosableFileFilter(currentFilter);
                }
            }
            return getFileChooser().getFileFilter();
        }
        
        public int getSize() {
            if(filters != null) {
                return filters.length;
            } else {
                return 0;
            }
        }
        
        public Object getElementAt(int index) {
            if(index > getSize() - 1) {
                // This shouldn't happen. Try to recover gracefully.
                return getFileChooser().getFileFilter();
            }
            if(filters != null) {
                return filters[index];
            } else {
                return null;
            }
        }
    }
    
    /**
     * Acts when DirectoryComboBox has changed the selected item.
     */
    protected class DirectoryComboBoxAction extends AbstractAction {
        protected DirectoryComboBoxAction() {
            super("DirectoryComboBoxAction");
        }
        
        public void actionPerformed(ActionEvent e) {
            if (isAdjusting != 0) return;
            
            JFileChooser fc = getFileChooser();
            AliasFileSystemTreeModel.Node node = (AliasFileSystemTreeModel.Node) directoryComboBox.getSelectedItem();
            if (node != null) {
                File file = node.getFile();
                if (fc.isMultiSelectionEnabled()) {
                    fc.setSelectedFiles(new File[] { file });
                } else {
                    fc.setSelectedFile(file);
                }
            }
        }
    }
    
    protected JButton getApproveButton(JFileChooser fc) {
        return approveButton;
    }
    
    public Action getApproveSelectionAction() {
        return approveSelectionAction;
    }
    
    protected class DoubleClickListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            // Note: We must not react on mouse clicks with clickCount=1.
            //       Because this interfers with the mouse handling code in
            //       the JBrowser which does list selection.
            JFileChooser fc = getFileChooser();
            if (SwingUtilities.isLeftMouseButton(e)
            && e.getClickCount() == 2
            && fc.getDialogType() != JFileChooser.SAVE_DIALOG) {
                maybeApproveSelection(false);
            }
        }
    }
    
    
    /**
     * This method is called, when the user double clicks the JBrowser, or
     * when she clicks at the approve button.
     *
     * @param allowDirectories true to allow selection of directories when
     * isMultiSelectionEnabled == true
     */
    private void maybeApproveSelection(boolean allowDirectories) {
        JFileChooser fc = getFileChooser();
        File selectedFile = null;
        File[] selectedFiles = null;
        
        String filename = null;
        if (isFileNameFieldVisible()) {
            filename = getFileName();
            if (filename.equals("")) filename = null;
        }
        
        if (fc.isMultiSelectionEnabled()) {
            TreePath[] selectedPaths = browser.getSelectionPaths();
            if (filename != null) {
                selectedFiles = new File[] {
                    new File(((AliasFileSystemTreeModel.Node) selectedPaths[0].getLastPathComponent()).getFile().getParent(), filename)
                };
            } else {
                selectedFiles = new File[selectedPaths.length];
                for (int i=0; i < selectedPaths.length; i++) {
                    selectedFiles[i] = ((AliasFileSystemTreeModel.Node) selectedPaths[i].getLastPathComponent()).getFile();
                }
            }
            
            // see if this is a single directory. If so, don't allow
            // double-click to select it.
            if (!allowDirectories && selectedFiles.length == 1 && selectedFiles[0].isDirectory()) {
                // block selection
                return;
            }
        } else {
            AliasFileSystemTreeModel.Node node = (AliasFileSystemTreeModel.Node) browser.getSelectionPath().getLastPathComponent();
            selectedFile = node.getFile();
            if (filename != null) {
                selectedFile = new File((! node.isLeaf()) ? selectedFile : selectedFile.getParentFile(), filename);
            } else if (fc.getFileSelectionMode() == JFileChooser.FILES_ONLY
            && ! node.isLeaf()) {
                // Abort we cannot approve a directory
                return;
            }
        }
        
        if (selectedFiles != null || selectedFile != null) {
            if (selectedFiles != null) {
                fc.setSelectedFiles(selectedFiles);
            } else if (fc.isMultiSelectionEnabled()) {
                fc.setSelectedFiles(new File[] { selectedFile });
            } else {
                fc.setSelectedFile(selectedFile);
            }
            fc.approveSelection();
        } else {
            if (fc.isMultiSelectionEnabled()) {
                fc.setSelectedFiles(null);
            } else {
                fc.setSelectedFile(null);
            }
            fc.cancelSelection();
        }
        
    }
    
    // *****************************
    // ***** Directory Actions *****
    // *****************************
    
    public Action getNewFolderAction() {
        return newFolderAction;
    }
    /**
     * Creates a new folder.
     */
    protected class NewFolderAction extends AbstractAction {
        protected NewFolderAction() {
            super("New Folder");
        }
        
        private String showNewFolderDialog() {
            JOptionPane optionPane = new JOptionPane(
            newFolderDialogPrompt,
            JOptionPane.PLAIN_MESSAGE,
            JOptionPane.OK_CANCEL_OPTION
            );
            optionPane.setWantsInput(true);
            optionPane.setInitialSelectionValue(newFolderDefaultName);
            JDialog dialog = optionPane.createDialog(getFileChooser(), newFolderTitleText);
            dialog.show();
            dialog.dispose();
            
            return (optionPane.getInputValue() == JOptionPane.UNINITIALIZED_VALUE) ? null : (String) optionPane.getInputValue();
        }
        
        public void actionPerformed(ActionEvent actionevent) {
            JFileChooser fc = getFileChooser();
            String newFolderName = showNewFolderDialog();
            
            if (newFolderName != null) {
                
                File newFolder;
                AliasFileSystemTreeModel.Node node = (AliasFileSystemTreeModel.Node) browser.getSelectionPath().getLastPathComponent();
                File currentFile = node.getFile();
                if (node.isLeaf()) {
                    currentFile = currentFile.getParentFile();
                }
                newFolder = new File(currentFile, newFolderName);
                if (newFolder.exists()) {
                    JOptionPane.showMessageDialog(
                    fc,
                    newFolderExistsErrorText,
                    newFolderTitleText, JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                try {
                    newFolder.mkdir();
                    fc.rescanCurrentDirectory();
                    fc.setCurrentDirectory(newFolder);
                    if (fc.isMultiSelectionEnabled()) {
                        fc.setSelectedFiles(new File[] { newFolder });
                    } else {
                        fc.setSelectedFile(newFolder);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                    fc,
                    newFolderErrorText,
                    newFolderTitleText, JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    protected class SaveTextFocusListener implements FocusListener {
        public void focusGained(FocusEvent focusevent) {
            updateApproveButtonState();
        }
        
        public void focusLost(FocusEvent focusevent) {
            /* empty */
        }
    }
    protected class SaveTextDocumentListener implements DocumentListener {
        public void insertUpdate(DocumentEvent documentevent) {
            textChanged();
        }
        
        public void removeUpdate(DocumentEvent documentevent) {
            textChanged();
        }
        
        public void changedUpdate(DocumentEvent documentevent) {
            //textChanged();
        }
        
        private void textChanged() {
            if (isAdjusting != 0) return;
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JFileChooser fc = getFileChooser();
                    AliasFileSystemTreeModel.Node node = (AliasFileSystemTreeModel.Node) browser.getSelectionPath().getLastPathComponent();
                    File file = node.getFile();
                    if (fileNameTextField.getText().length() != 0) {
                        if (! node.isLeaf()) {
            // Don't change the current directory when the user is entering
                            // text into the text field. It confuses our users!
                            // Instead, we update the state of the approve button
                            // only, and then we return!
                           // file = new File(file, fileNameTextField.getText());
                            updateApproveButtonState();
                            return;
                        } else {
                            file = new File(fc.getFileSystemView().getParentDirectory(file), fileNameTextField.getText());
                        }
                    }
                    if (fc.isMultiSelectionEnabled()) {
                        fc.setSelectedFiles(new File[] { file });
                    } else {
                        fc.setSelectedFile(file);
                    }
                            updateApproveButtonState();
                }
            });
        }
    }
    /**
     * The FileChooserAncestorListener listens for visibility changes of
     * the JFileChooser.
     * This is used to do validations (refreshes) of the tree model only,
     * when the JFileChooser is showing.
     */
    private class FileChooserAncestorListener implements AncestorListener {
        public void ancestorAdded(AncestorEvent event) {
            if (model != null) {
                model.setAutoValidate(QuaquaManager.getBoolean("FileChooser.autovalidate"));
                model.validatePath(browser.getSelectionPath());
                if (sidebarListModel != null) {
                    sidebarListModel.lazyValidate();
                }
            }
            //QuaquaUtilities.setWindowAlpha(SwingUtilities.getWindowAncestor(event.getAncestorParent()), 230);
        }
        
        public void ancestorRemoved(AncestorEvent event) {
            if (model != null) {
                model.setAutoValidate(false);
                model.stopValidation();
                model.invalidatePath(browser.getSelectionPath());
                clearIconCache();
            }
        }
        
        public void ancestorMoved(AncestorEvent event) {
        }
        
    }
    // *******************************************************
    // ************ FileChooserUI PLAF methods ***************
    // *******************************************************
    /**
     * API method of FileChooserUI.
     */
    public void ensureFileIsVisible(JFileChooser fc, File f) {
        if (f != null) {
            TreePath fullPath = getFileSystemTreeModel().toPath(f,
            subtreeModel.toFullPath(browser.getSelectionPath())
            );
            TreePath subPath = getTreeModel().toSubPath(fullPath);
            if (subPath == null) {
                isAdjusting++;
                getTreeModel().setPathToRoot(new Object[] {fullPath.getPathComponent(0)});
                isAdjusting--;
                subPath = fullPath;
            }
            ensurePathIsVisible(fullPath);
        }
    }
    /**
     * API method of FileChooserUI.
     */
    public String getApproveButtonText(JFileChooser fc) {
        String buttonText = fc.getApproveButtonText();
        if (buttonText != null) {
            return buttonText;
        } else if (fc.isDirectorySelectionEnabled() && chooseButtonText != null) {
            return chooseButtonText;
        } else if (fc.getDialogType() == JFileChooser.OPEN_DIALOG) {
            return openButtonText;
        } else if (fc.getDialogType() == JFileChooser.SAVE_DIALOG) {
            return saveButtonText;
        } else {
            return null;
        }
    }
    
    /**
     * API method of FileChooserUI.
     */
    public FileView getFileView(JFileChooser fc) {
        return fileView;
    }
    
    /**
     * API method of FileChooserUI.
     */
    public void rescanCurrentDirectory(JFileChooser fc) {
        // Validation is only necessary, when the JFileChooser is showing.
        if (fc.isShowing()) {
            //clearIconCache();
            model.lazyInvalidatePath(browser.getSelectionPath());
            model.validatePath(browser.getSelectionPath());
        }
    }
    // *******************************************************
    // ******** End of FileChooserUI PLAF methods ************
    // *******************************************************
    
    // *******************************************************
    // ********** BasicFileChooserUI PLAF methods ************
    // *******************************************************
    public void clearIconCache() {
        try {
            fileView.getClass()
            .getMethod("clearIconCache", new Class[0])
            .invoke(fileView, new Object[0]);
        } catch (Exception e) {
            // empty
        }
    }
    protected MouseListener createDoubleClickListener(JFileChooser fc) {
        return new DoubleClickListener();
    }
    
    
    
    // *******************************************************
    // ******* End of BasicFileChooserUI PLAF methods ********
    // *******************************************************
    
    private class VolumesSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            if (isAdjusting != 0) return;
            
            if (sidebarList != null) {
                FileInfo info = (FileInfo) sidebarList.getSelectedValue();
                if (info != null) {
                    JFileChooser fc = getFileChooser();
                    
                    isAdjusting++;
                    subtreeModel.setPathToRoot(
                    model.toPath(info.getFile(),
                    new TreePath(subtreeModel.toFullPath(browser.getSelectionPath()).getPath())
                    )
                    );
                    isAdjusting--;
                    if (fc.isMultiSelectionEnabled()) {
                        fc.setSelectedFiles(new File[] { info.getFile() });
                    } else {
                        fc.setSelectedFile(info.getFile());
                    }
                    
                }
                TreePath path = subtreeModel.getPathToRoot();
                getFileSystemTreeModel().lazyInvalidatePath(path);
                getFileSystemTreeModel().validatePath(path);
            }
        }
    }
    /**
     * Responds to an Open or Save request
     */
    /**
     * Responds to an Open or Save request
     */
    protected class QuaquaApproveSelectionAction extends AbstractAction {
        protected QuaquaApproveSelectionAction() {
            super("approveSelection");
        }
        public void actionPerformed(ActionEvent e) {
            maybeApproveSelection(true);
        }
    }
}