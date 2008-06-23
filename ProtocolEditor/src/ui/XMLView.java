
package ui;

/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cmd.ActionCmd;
import cmd.OpenFileCmd;

import actions.NewFileAction;

import ols.ObservationCreator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

import search.IndexFiles;
import search.SearchController;
import tree.DataField;
import tree.DataFieldNode;
import tree.Tree;
import tree.Tree.Actions;
import ui.components.FileListSelector;
import ui.components.FindToolBar;
import ui.components.PopupMenuButton;
import ui.components.ProtocolFileFilter;
import ui.fieldEditors.FieldEditor;
import util.ExceptionHandler;
import util.ImageFactory;
import util.PreferencesManager;
import xmlMVC.StartupShutdown;
import xmlMVC.XMLModel;


// the main View class. 
// defines the app window and lays out all components
// processes all user commands from buttons etc..

public class XMLView 
	extends 
	DefaultChangeListener
	{
	
	IModel xmlModel;
	Controller controller;
	
	DataFieldNode rootOfImportTree;

	
	// state changes 
	int editingState = EDITING_FIELDS;
	public static final int EDITING_FIELDS = 0;
	public static final int IMPORTING_FIELDS = 1;
	
	private boolean fieldEditorAndToolbarVisible = true;
	
	// commands 
	public static final String WINDOW = "window";
	
	
	public static final int SHIFT_CLICK = 17;
	public static final Font FONT_H1 = new Font("SansSerif", Font.PLAIN, 16);
	public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 12);
	public static final Font FONT_TINY = new Font("SansSerif", Font.PLAIN, 9);
	public static final Font FONT_INVISIBLE = new Font("Sanserif", Font.PLAIN, 1);
	public static final Color BLUE_HIGHLIGHT = new Color(181,213,255);
	
	public static final int BUTTON_SPACING = 5;
	
	JFrame XMLFrame;
	JPanel mainContentPane;		// this holds toolbar (North) and XMLUIPanel (Centre)
	JSplitPane splitPane;
	JScrollPane XMLScrollPane;	// contains the UI
	JSplitPane XMLUIPanel;		// the top-level panel of the frame - right used for search results
	JPanel searchResultsPanel;
	
	JPanel fieldEditor; 	// shows each field to be edited
	JPanel fieldEditorContainer;	// holds show/hide button above FieldEditor
	JButton showHideFieldEditorButton;
	
	JPanel fileManagerPanel; 	// tool bar for open/close/print etc. 
	
	SearchController searchController;

	JPanel importElementChooser;
	
	JTextField searchField;
	
	
	FindToolBar findToolBar;
	
	JCheckBoxMenuItem showEditToolbarMenuItem;
	
	//JButton xmlValidationButton;
	//JCheckBox xmlValidationCheckbox;
	// JPanel xmlValidationPanel;
	Icon closeIcon;
	Icon redBallIcon;

	JToolBar protocolEditToolBar;
	
	JPanel protocolTab;
	
	static final int windowHeight = 460;
    static final int panelWidth = 950;


	private Icon printIcon;

	private Icon clearFieldsIcon;

	private Icon loadDefaultsIcon;

	private Icon fileCloseIcon;
	private Icon moreLikeThisIcon;
	private Icon searchIcon;
	private Icon configureIcon;
	private Icon protocolIcon;
	private Icon sendCommentIcon;

	
    public XMLView(IModel xmlModel) {
    	
    	// adds this as a changeListener of model
    	super(xmlModel);
    	
    	this.xmlModel = xmlModel;
    	controller = new Controller(xmlModel, this);
    	
    	// buildXMLForm();
    	
    	buildUI();
    	
    }
    
    public void buildUI() {
		
		searchIcon = ImageFactory.getInstance().getIcon(ImageFactory.SEARCH_ICON);
		moreLikeThisIcon = ImageFactory.getInstance().getIcon(ImageFactory.MORE_LIKE_THIS_ICON);
		//validationIcon = ImageFactory.getInstance().getIcon(ImageFactory.VALIDATION_ICON);
		printIcon = ImageFactory.getInstance().getIcon(ImageFactory.PRINT_ICON);
		loadDefaultsIcon = ImageFactory.getInstance().getIcon(ImageFactory.LOAD_DEFAULTS_ICON);
		clearFieldsIcon = ImageFactory.getInstance().getIcon(ImageFactory.CLEAR_FIELDS_ICON);
		closeIcon = ImageFactory.getInstance().getIcon(ImageFactory.N0);
		
		fileCloseIcon = ImageFactory.getInstance().getIcon(ImageFactory.FILE_CLOSE_ICON);
		configureIcon = ImageFactory.getInstance().getIcon(ImageFactory.CONFIGURE_ICON);
		protocolIcon = ImageFactory.getInstance().getIcon(ImageFactory.PROTOCOL_ICON);
		sendCommentIcon = ImageFactory.getInstance().getIcon(ImageFactory.SEND_COMMENT_ICON);
		
		
//		 controls for changing currently opened file, and closing current file
		fileManagerPanel = new JPanel();
		fileManagerPanel.setLayout(new BorderLayout());
		Box fileManagerEastToolBar = Box.createHorizontalBox();
		Box fileManagerWestToolBar = Box.createHorizontalBox();
		Border fileManagerToolBarBorder = new EmptyBorder(2,BUTTON_SPACING,2,BUTTON_SPACING);
		
		// add buttons to the tool-bar 
		addButton(Controller.NEW_FILE, "", fileManagerToolBarBorder, fileManagerWestToolBar);
		addButton(Controller.OPEN_FILE, "", fileManagerToolBarBorder, fileManagerWestToolBar);
		addButton(Controller.OPEN_WWW_FILE, "", fileManagerToolBarBorder, fileManagerWestToolBar);
		addButton(Controller.SAVE_FILE, "", fileManagerToolBarBorder, fileManagerWestToolBar);
		addButton(Controller.SAVE_FILE_AS, "", fileManagerToolBarBorder, fileManagerWestToolBar);
		addButton(Controller.OPEN_CALENDAR, "", fileManagerToolBarBorder, fileManagerWestToolBar);

		Action[] printExportActions = new Action[] {
				controller.getAction(Controller.EXPORT_ALL_HTML),
				controller.getAction(Controller.EXPORT_HIGHLT_HTML),
				controller.getAction(Controller.EXPORT_ALL_TEXT),
				controller.getAction(Controller.EXPORT_HIGHLTD_TEXT)};
		JButton printButton = new PopupMenuButton("Export the current file for printing", 
				printIcon, printExportActions);
		addButton(printButton, fileManagerToolBarBorder, fileManagerWestToolBar);

		fileManagerWestToolBar.add(new JSeparator(JSeparator.VERTICAL));

		
		// buttons for editing variables, eg load defaults, clear fields, multiply values
		
		Action[] loadDefaultsActions = new Action[] {
				controller.getAction(Controller.LOAD_DEFAULTS_ALL),
				controller.getAction(Controller.LOAD_DEFAULTS_HIGHLT) };
		JButton loadDefaultsButton = new PopupMenuButton("Load Default Values", 
				loadDefaultsIcon, loadDefaultsActions);
		addButton(loadDefaultsButton, fileManagerToolBarBorder, fileManagerWestToolBar);
		
		
		Action[] clearFieldsActions = new Action[] {
				controller.getAction(Controller.CLEAR_FIELDS_ALL),
				controller.getAction(Controller.CLEAR_FIELDS_HIGHLT) };
		JButton clearFieldsButton = new PopupMenuButton("Clear Field Values", 
				clearFieldsIcon, clearFieldsActions);
		addButton(clearFieldsButton, fileManagerToolBarBorder, fileManagerWestToolBar);
		
		addButton(Controller.REQUIRED_FIELD, "", fileManagerToolBarBorder, fileManagerWestToolBar);
		addButton(Controller.MULTIPLY_VALUES, "", fileManagerToolBarBorder, fileManagerWestToolBar);
		
		fileManagerWestToolBar.add(new JSeparator(JSeparator.VERTICAL));
	
		
		// undo - redo buttons
		
		// "no-name" actions don't refresh name (so that the buttons don't display text)
		addButton(Controller.UNDO_NO_NAME, "", fileManagerToolBarBorder, fileManagerWestToolBar);
		addButton(Controller.REDO_NO_NAME, "", fileManagerToolBarBorder, fileManagerWestToolBar);
		fileManagerWestToolBar.add(new JSeparator(JSeparator.VERTICAL));
		
		
		// find controls
		
		findToolBar = new FindToolBar(this, xmlModel);
		fileManagerWestToolBar.add(findToolBar);
	
		// Add the toolBar to the left of the full tool bar
		fileManagerPanel.add(fileManagerWestToolBar, BorderLayout.WEST);
		

		
		// comboBox for switching between opened files 
		
		fileManagerEastToolBar.add(new JLabel("Current file:"));
		fileManagerEastToolBar.add(new FileListSelector(xmlModel));
		

		searchController = new SearchController(xmlModel);
		
		// more-like this button
		JButton moreLikeThisButton = new JButton(moreLikeThisIcon);
		moreLikeThisButton.setActionCommand(SearchController.MORE_LIKE_THIS_SEARCH);
		moreLikeThisButton.setToolTipText("'More Like This' search to find files like the current one");;
		moreLikeThisButton.setBorder(new EmptyBorder(0,2,0,4));
		searchController.addSearchActionSource(moreLikeThisButton);
		
		fileManagerEastToolBar.add(moreLikeThisButton);
		
		/*
		 * template-search button
		 * This is hidden for now, since this functionality is not finished.
		 * Will return to work on the after Beta-3.0
		 
		Icon templateSearchIcon = ImageFactory.getInstance().getIcon(ImageFactory.TEMPLATE_SEARCH_ICON);
		JButton templateSearchButton = new JButton(templateSearchIcon);
		templateSearchButton.setActionCommand(SearchController.TEMPLATE_SEARCH);
		templateSearchButton.setToolTipText("Use the current file as a search form. Fill in only those fields you wish to search for.");
		templateSearchButton.setBorder(new EmptyBorder(0,2,0,4));
		searchController.addSearchActionSource(templateSearchButton);
		
		fileManagerEastToolBar.add(templateSearchButton);
		*/
		
		// close-file button
		addButton(Controller.CLOSE_FILE, "", fileManagerToolBarBorder, fileManagerEastToolBar);
		
		fileManagerPanel.add(fileManagerEastToolBar, BorderLayout.EAST);
	
		
		// Build left side with form UI

		XMLScrollPane = new JScrollPane(new FormDisplay(xmlModel), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		XMLScrollPane.setFocusable(true);
		XMLScrollPane.setMinimumSize(new Dimension(200, 200));
		XMLScrollPane.addMouseListener(new ScrollPaneMouseListener());
		XMLScrollPane.setPreferredSize(new Dimension( panelWidth, windowHeight));

		
	
		// JButton compareFilesButton = new JButton("compare files");
		// compareFilesButton.setToolTipText("Compare the first two files in the list");
		// compareFilesButton.addActionListener(new CompareFileListener());
		
		// toolBar Border
		EmptyBorder tb = new EmptyBorder(BUTTON_SPACING,BUTTON_SPACING,BUTTON_SPACING,BUTTON_SPACING);
		
		// Protocol edit buttons
		
		protocolEditToolBar = new JToolBar("Close toolbar to re-Dock");
		
		addButton(Controller.LOCK_FIELDS, "", tb, protocolEditToolBar);
		addButton(Controller.ADD_FIELD, "", tb, protocolEditToolBar);
		addButton(Controller.DUPLICATE_FIELD, "", tb, protocolEditToolBar);
		addButton(Controller.DELETE_FIELD, "", tb, protocolEditToolBar);
		addButton(Controller.MOVE_FIELD_UP, "", tb, protocolEditToolBar);
		addButton(Controller.MOVE_FIELD_DOWN, "", tb, protocolEditToolBar);
		addButton(Controller.PROMOTE_FIELD, "", tb, protocolEditToolBar);
		addButton(Controller.DEMOTE_FIELD, "", tb, protocolEditToolBar);
		addButton(Controller.COPY_FIELD, "", tb, protocolEditToolBar);
		addButton(Controller.PASTE_FIELD, "", tb, protocolEditToolBar);
		addButton(Controller.IMPORT_FIELD, "", tb, protocolEditToolBar);
		addButton(Controller.IMPORT_TEXT, "", tb, protocolEditToolBar);

		
		/*
		// XML validation - currently not displayed. Demand for this functionality??
		Dimension xmlValidDim = new Dimension(150, 22);
		xmlValidationPanel = new JPanel(new BorderLayout());
		Box xmlValidationBox = Box.createHorizontalBox();
		xmlValidationPanel.setMaximumSize(xmlValidDim);		// to stop this expanding when window enlarges
		xmlValidationPanel.setPreferredSize(xmlValidDim);
		XmlValidationListener xmlValidationListener = new XmlValidationListener();
		
		xmlValidationButton = new JButton(validationIcon);
		xmlValidationButton.setBorder(new EmptyBorder(2,BUTTON_SPACING,2,0));
		xmlValidationButton.setToolTipText("Turn XML validation On / Off");
		xmlValidationButton.addActionListener(xmlValidationListener);
		
		
		xmlValidationCheckbox = new JCheckBox();
		xmlValidationCheckbox.setBorder(new EmptyBorder(2,0,2,0));
		xmlValidationCheckbox.setToolTipText("Turn XML validation On /Off");
		xmlValidationCheckbox.addActionListener(xmlValidationListener);
		// do this each time model changes
		xmlValidationCheckbox.setSelected(xmlModel.getXmlValidation());
		
		
		xmlValidationBox.add(xmlValidationButton);
		xmlValidationBox.add(xmlValidationCheckbox);
		xmlValidationPanel.add(xmlValidationBox, BorderLayout.EAST);
		//xmlValidationPanel.setMaximumSize(new Dimension(100, 22));
		 */

		
		protocolEditToolBar.add(Box.createHorizontalGlue());
		
		// Field Editor
		fieldEditor = new FieldEditor();
		fieldEditor.setVisible(fieldEditorAndToolbarVisible);
		fieldEditorContainer = new JPanel(new BorderLayout());		// holds show/hide button above FieldEditor
		showHideFieldEditorButton = new JButton(">>", fileCloseIcon);
		showHideFieldEditorButton.addActionListener(new ToggleEditTemplateControlsListener());
		showHideFieldEditorButton.setBorder(new EmptyBorder(2,2,2,2));
		JPanel buttonContainer = new JPanel(new BorderLayout());
		buttonContainer.add(showHideFieldEditorButton, BorderLayout.WEST);
		fieldEditorContainer.add(buttonContainer, BorderLayout.NORTH);
		fieldEditorContainer.add(fieldEditor, BorderLayout.CENTER);
		
		JPanel toolBarContainer = new JPanel(new BorderLayout());
		toolBarContainer.add(XMLScrollPane, BorderLayout.CENTER);
		protocolEditToolBar.setOrientation(JToolBar.VERTICAL);
		protocolEditToolBar.setVisible(fieldEditorAndToolbarVisible);
		toolBarContainer.add(protocolEditToolBar, BorderLayout.EAST);
		
		protocolTab = new JPanel();
		protocolTab.setLayout(new BorderLayout());
		protocolTab.add(toolBarContainer, BorderLayout.CENTER);
		protocolTab.add(fieldEditorContainer, BorderLayout.EAST);
		
		

		// split pane to display search results (right only visible after search)
		XMLUIPanel = new JSplitPane();
		XMLUIPanel.setPreferredSize(new Dimension( panelWidth, windowHeight ));
		XMLUIPanel.setDividerSize(0);
		XMLUIPanel.setDividerLocation(1.0);
		XMLUIPanel.setLeftComponent(protocolTab);
		XMLUIPanel.setRightComponent(null);
		
		mainContentPane = new JPanel(new BorderLayout());
		mainContentPane.add("Center", XMLUIPanel);
		mainContentPane.add("North", fileManagerPanel);
		
		refreshAnyFilesOpen();		// disable save etc.
	    
		
//		 not visible till a file is opened
		protocolTab.setVisible(false);
		
		
		//showStartUpDialog();
		
		// buildFrame();	// do this from ProtocolEditor.main() now.
	}
	
	public void buildFrame() {
	    	XMLFrame = new JFrame("OMERO.editor");
	    	XMLFrame.setIconImage( ((ImageIcon)protocolIcon).getImage());
	    	
			// Build menus
			JMenuBar menuBar = new JMenuBar();
			EmptyBorder menuItemBorder = new EmptyBorder(0,5,0,5);
			
			// File menu
			JMenu fileMenu = new JMenu("File");
			fileMenu.setBorder(menuItemBorder);
			
			JMenuItem openFile = new JMenuItem(controller.getAction(Controller.OPEN_FILE));
			setMenuItemAccelerator(openFile, KeyEvent.VK_O);
			fileMenu.add(openFile);
			
			JMenuItem newBlankProtocolMenuItem = new JMenuItem(controller.getAction(Controller.NEW_FILE));
			setMenuItemAccelerator(newBlankProtocolMenuItem, KeyEvent.VK_N);
			fileMenu.add(newBlankProtocolMenuItem);
			
			JMenuItem closeFileMenuItem = new JMenuItem(controller.getAction(Controller.CLOSE_FILE));
			setMenuItemAccelerator(closeFileMenuItem, KeyEvent.VK_W);
			fileMenu.add(closeFileMenuItem);
			
			JMenuItem saveFileMenuItem = new JMenuItem(controller.getAction(Controller.SAVE_FILE));
			setMenuItemAccelerator(saveFileMenuItem, KeyEvent.VK_S);
			fileMenu.add(saveFileMenuItem);
			
			fileMenu.add(new JMenuItem(controller.getAction(Controller.SAVE_FILE_AS)));
			
			JMenu printsubMenu = new JMenu("Export for printing...");
			printsubMenu.setIcon(printIcon);
			fileMenu.add(printsubMenu);
			
			JMenuItem printWholeFileMenuItem = new JMenuItem(controller.getAction(Controller.EXPORT_ALL_HTML));
			setMenuItemAccelerator(printWholeFileMenuItem, KeyEvent.VK_P);
			printsubMenu.add(printWholeFileMenuItem);
			
			printsubMenu.add(new JMenuItem(controller.getAction(Controller.EXPORT_HIGHLT_HTML)));
			
			fileMenu.add(new JMenuItem(controller.getAction(Controller.VALIDATE_XML)));
			fileMenu.add(new JMenuItem(controller.getAction(Controller.INDEX_FILES)));
			
			menuBar.add(fileMenu);
			
			
			// "Edit" menu
			JMenu editMenu = new JMenu("Edit");
			editMenu.setBorder(menuItemBorder);
			
			JMenu loadDefaultsSubMenu = new JMenu("Load Default Values");
			loadDefaultsSubMenu.setIcon(loadDefaultsIcon);
			JMenuItem loadDefaultsMenuItem = new JMenuItem(controller.getAction(Controller.LOAD_DEFAULTS_ALL));
			loadDefaultsSubMenu.add(loadDefaultsMenuItem);
			JMenuItem loadDefaultsHighLtFieldsMenuItem = new JMenuItem(controller.getAction(Controller.LOAD_DEFAULTS_HIGHLT));
			loadDefaultsSubMenu.add(loadDefaultsHighLtFieldsMenuItem);
			
			JMenu clearFieldsSubMenu = new JMenu("Clear Field Values");
			clearFieldsSubMenu.setIcon(clearFieldsIcon);
			JMenuItem clearFieldsMenuItem = new JMenuItem(controller.getAction(Controller.CLEAR_FIELDS_ALL));
			clearFieldsSubMenu.add(clearFieldsMenuItem);
			JMenuItem clearFieldsHighLtFieldsMenuItem = new JMenuItem(controller.getAction(Controller.CLEAR_FIELDS_HIGHLT));
			clearFieldsSubMenu.add(clearFieldsHighLtFieldsMenuItem);
			
			JMenuItem multiplyValueOfSelectedFieldsMenuItem = new JMenuItem(controller.getAction(Controller.MULTIPLY_VALUES));
			
			// undo-redo menu items
			JMenuItem undoMenuItem = new JMenuItem(controller.getAction(Controller.UNDO));
			setMenuItemAccelerator(undoMenuItem, KeyEvent.VK_Z);
			
			JMenuItem redoMenuItem = new JMenuItem(controller.getAction(Controller.REDO));
			setMenuItemAccelerator(redoMenuItem, KeyEvent.VK_Y);
			
			Icon findIcon = ImageFactory.getInstance().getIcon(ImageFactory.FIND_ICON);
			JMenuItem findMenuItem = new JMenuItem("Find", findIcon);
			findMenuItem.addActionListener(findToolBar);
			setMenuItemAccelerator(findMenuItem, KeyEvent.VK_F);
			
			editMenu.add(loadDefaultsSubMenu);
			editMenu.add(clearFieldsSubMenu);
			editMenu.add(multiplyValueOfSelectedFieldsMenuItem);
			editMenu.addSeparator();
			editMenu.add(undoMenuItem);
			editMenu.add(redoMenuItem);
			editMenu.add(findMenuItem);
			
			menuBar.add(editMenu);

			
			// Edit-Template menu
			JMenu protocolMenu = new JMenu("Edit-Template");
			protocolMenu.setBorder(menuItemBorder);
			
			showEditToolbarMenuItem= new JCheckBoxMenuItem("Show Edit-Template toolbar");
			showEditToolbarMenuItem.setSelected(true);		// start off visible
			showEditToolbarMenuItem.addActionListener(new ToggleEditTemplateControlsListener());
			
			protocolMenu.add(showEditToolbarMenuItem);
			protocolMenu.add(new JMenuItem(controller.getAction(Controller.ADD_FIELD)));
			protocolMenu.add(new JMenuItem(controller.getAction(Controller.DUPLICATE_FIELD)));
			protocolMenu.add(new JMenuItem(controller.getAction(Controller.DELETE_FIELD)));
			protocolMenu.add(new JMenuItem(controller.getAction(Controller.MOVE_FIELD_UP)));
			protocolMenu.add(new JMenuItem(controller.getAction(Controller.MOVE_FIELD_DOWN)));
			protocolMenu.add(new JMenuItem(controller.getAction(Controller.PROMOTE_FIELD)));
			protocolMenu.add(new JMenuItem(controller.getAction(Controller.DEMOTE_FIELD)));
			JMenuItem copyFieldsMenuItem = new JMenuItem(controller.getAction(Controller.COPY_FIELD));
			setMenuItemAccelerator(copyFieldsMenuItem, KeyEvent.VK_C);
			protocolMenu.add(copyFieldsMenuItem);
			JMenuItem pasteFieldsMenuItem = new JMenuItem(controller.getAction(Controller.PASTE_FIELD));
			setMenuItemAccelerator(copyFieldsMenuItem, KeyEvent.VK_V);
			protocolMenu.add(pasteFieldsMenuItem);
			menuBar.add(protocolMenu);
			
			
			// Import menu
			JMenu importMenu = new JMenu("Import");
			importMenu.setBorder(menuItemBorder);
			importMenu.add(new JMenuItem(controller.getAction(Controller.IMPORT_FIELD)));
			importMenu.add(new JMenuItem(controller.getAction(Controller.IMPORT_TEXT)));
			importMenu.add(new JMenuItem(controller.getAction(Controller.IMPORT_TABLE)));
			
			menuBar.add(importMenu);
			
			
			// Calendar menu
			JMenu calendarMenu = new JMenu("Calendar");
			calendarMenu.setBorder(menuItemBorder);
			calendarMenu.add(new JMenuItem(controller.getAction(Controller.OPEN_CALENDAR)));
			calendarMenu.add(new JMenuItem(controller.getAction(Controller.REPOPULATE_CALENDAR)));
			calendarMenu.add(new JMenuItem(controller.getAction(Controller.EXPORT_CALENDAR)));
			
			menuBar.add(calendarMenu);
			
			
			// Help menu
			JMenu helpMenu = new JMenu("Help");
			helpMenu.setBorder(menuItemBorder);
			
			helpMenu.add(new JMenuItem(controller.getAction(Controller.HELP_USER_GUIDE)));
			
			JMenuItem sendCommentMenuItem = new JMenuItem("Send Comment", sendCommentIcon);
			sendCommentMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ExceptionHandler.showCommentDialog();	
				}
			});
			helpMenu.add(sendCommentMenuItem);
			helpMenu.add(new JMenuItem(controller.getAction(Controller.OPEN_WWW_FILE)));
			menuBar.add(helpMenu);
			
			// search Field and button
			searchController.addChangeListener(new SearchDisplayListener());
			
			searchField = new JTextField("Search Files", 20);
			searchController.setSearchTermSource(searchField);
			searchController.addSearchActionSource(searchField);
			JPanel searchPanel = new JPanel();
			searchPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
			searchPanel.setLayout(new BorderLayout());
			searchPanel.add(searchField, BorderLayout.EAST);
			
			
			JButton searchButton = new JButton(searchIcon);
			searchButton.setActionCommand(SearchController.KEYWORD_SEARCH);
			searchController.addSearchActionSource(searchButton);
			searchButton.setBorder(new EmptyBorder(3,3,3,3));
	
			menuBar.add(searchPanel);
			menuBar.add(searchButton);
			
			// disable save etc.
			refreshAnyFilesOpen();
	
		    // set up frame
			XMLFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			XMLFrame.setName("editorMainWindow");
			XMLFrame.setJMenuBar(menuBar);
			XMLFrame.getContentPane().add(mainContentPane, BorderLayout.CENTER);

			XMLFrame.pack();
			
			// check to see if any files open before quitting when window closes
			/* XMLFrame.addWindowListener(new WindowAdapter() {
	            public void windowClosing(WindowEvent evt) {
	                mainWindowClosing();
	            }
	        }); */ 
			new TaskBarManager(XMLFrame);	// handles system quit command for Macs 
			
			XMLFrame.setLocation(200, 100);
			XMLFrame.setVisible(true);
			
			showStartUpDialog();
	    }

	public JPanel getMainContentPanel() {
		return mainContentPane;
	}
	
	// an intro splash-screen to get users started
	public void showStartUpDialog() {
		
		/*
		 * First, run statup check
		 */
		boolean isUpgradeNeeded = StartupShutdown.upgradeCheck();
		
		String message = "Welcome to the OMERO.editor.\n"
		    + "Please choose an option to get you started.";
		if (isUpgradeNeeded) {
			message = message + "\n \n" + "WARNING: You may not be using the " +
				"latest version of this software.\n" +
				"Please see " +
				"http://trac.openmicroscopy.org.uk/omero/wiki/MilestoneDownloads" +
				"\n";
		}
		
		//		Custom button text
				Icon bigProtocolIcon = ImageFactory.getInstance().getIcon(ImageFactory.BIG_PROTOCOL_ICON);
				Object[] options = {"Start blank protocol",
				                    "Open existing file",
				                    "Cancel"};
				int n = JOptionPane.showOptionDialog(mainContentPane, message, "Welcome",
				    JOptionPane.YES_NO_CANCEL_OPTION,
				    JOptionPane.QUESTION_MESSAGE,
				    bigProtocolIcon,
				    options,
				    options[0]);
				
				if (n == 0) {
					xmlModel.openBlankProtocolFile();
				}
				else if (n == 1) {
					ActionCmd action = new OpenFileCmd(xmlModel);
					action.execute();
				}
	}
	

	// change in the model
	public void stateChanged(ChangeEvent e) {
		updateFieldEditor();

		refreshAnyFilesOpen();
	}
	
	
	// if no files open, hide the tabbed pane
	public void refreshAnyFilesOpen() {
		
		boolean noFiles = (xmlModel.getOpenFileList().length == 0);

		protocolTab.setVisible(!noFiles);
	}
	
	
	
	// display the DataField in the XMLScrollPane
	public void displayThisDataField(DataField field) {
		
		// make sure field is not hidden under a collapsed parent
		field.getNode().expandAllAncestors();
		// highlight the field (clear others)
		field.dataFieldSelected(true);	
		
		// if necessary, scroll to show field
		int bottomOfPanel = field.getHeightOfFieldBottom();
		int heightOfScrollPane = XMLScrollPane.getHeight();
		
		int xCoord = 0;
		int yCoord = 0;
		
		if (bottomOfPanel > heightOfScrollPane) {
			yCoord = bottomOfPanel - heightOfScrollPane + 20;
		}
		
		Point newPoint = new Point(xCoord, yCoord);
		XMLScrollPane.getViewport().setViewPosition(newPoint);
		XMLScrollPane.repaint();
		
	}
	
	

	
	public void refreshFieldEditorAndToolbarVisibility() {
		boolean visible = fieldEditorAndToolbarVisible;
		
		protocolEditToolBar.setVisible(visible);
		fieldEditor.setVisible(visible);
		
		showEditToolbarMenuItem.setSelected(visible);
		showEditToolbarMenuItem.setIcon(visible ? fileCloseIcon : configureIcon);
		
		showHideFieldEditorButton.setIcon(visible ? fileCloseIcon : configureIcon);
		showHideFieldEditorButton.setText(visible ? ">>" : "<<");
	}
	
	
	
	public class ToggleEditTemplateControlsListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			fieldEditorAndToolbarVisible = !fieldEditorAndToolbarVisible;
				
			refreshFieldEditorAndToolbarVisibility();
		}
	}
	
	/*
	public class XmlValidationListener implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			boolean validationOn = xmlValidationCheckbox.isSelected();
			
			// toggle validation if source is not checkbox (if source is checkbox, done automatically!)
			if (event.getSource() != xmlValidationCheckbox) {
				validationOn = !validationOn;
				xmlValidationCheckbox.setSelected(validationOn);
			}
			
			enableXmlValidation(validationOn);
		}
		
	}
	
		// turn on and off the validation of the current xml tree. 
	public void enableXmlValidation(boolean validationOn) {
		xmlModel.setXmlValidation(validationOn);
		updateXmlValidationPanel();	
	}
	*/
	
	/*
	public class CompareFileListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			xmlModel.compareCurrentFiles();
		}	
	}
	*/

	
	public class SearchDisplayListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			JPanel resultsPanel = searchController.getSearchResultsPanel();
			updateSearchPanel(resultsPanel);
		}
	}
	
	/**
	 * This does the job of displaying an additional panel (eg search-results), by placing
	 * it in a SplitPane, <code>XMLUIPanel</code> to the right of the main window contents. 
	 * The main window contents are already in the left side of <code>XMLUIPanel</code>.
	 * When there is nothing to display (newPanel == null), the existence of the SplitPane
	 * is hidden, by setting the divider to 0 pixels.
	 * 
	 * @param newPanel		The JPanel that should be displayed.
	 */
	public void updateSearchPanel(JPanel newPanel) {
		XMLUIPanel.setRightComponent(newPanel);

		if (newPanel != null)	XMLUIPanel.setDividerSize(10);
		else XMLUIPanel.setDividerSize(0);
		
		XMLUIPanel.setDividerLocation(0.5f);
		
		XMLUIPanel.validate();
		XMLUIPanel.resetToPreferredSizes();
	}

	/*
	// this is called (via selectionChanged) xmlModel after validating xml
	public void updateXmlValidationPanel() {
		// if validation is turned on
		if (xmlModel.getXmlValidation()) {
			
			xmlModel.validateCurrentXmlFile();
			
			// if there are some error messages...
			if (!(xmlModel.getErrorMessages().isEmpty())) {
				String message = "<html>";
				for (String m : xmlModel.getErrorMessages()) {
					message = message + m + "<br>";
				}
				message = message + "</html>";
				
				xmlValidationButton.setText("XML is not valid...");
				xmlValidationButton.setToolTipText(message);
			}
			else {
			// no error messages, all OK
				xmlValidationButton.setText("XML valid");
				xmlValidationButton.setToolTipText(null);
			}
		} else {
		// validation not turned on
			xmlValidationButton.setText(null);
			xmlValidationButton.setToolTipText(null);
		}
	}
	*/
	
	/**
	 * This is called when the model changes.
	 * It refreshes the FieldEditor panel...
	 * If the model has an ImportTree (ie if the user has chosen a file, to import elements from)
	 *  then this Tree is displayed by an <code>ImportElementChooser</code>.
	 * Otherwise, the current FieldEditor to display is retrieved from the model. 
	 * 
	 */
	public void updateFieldEditor() {
		
		JPanel newDisplay = null;
		if ((xmlModel.getImportTreeRoot() != null)) {
			if (importElementChooser == null) {
				importElementChooser = new ImportElementChooser(xmlModel);
			}
			newDisplay = importElementChooser;
		} else {
			importElementChooser = null;
			newDisplay = xmlModel.getFieldEditorToDisplay();
		}
		
		updateFieldEditor(newDisplay);
	}
	
	/**
	 * This does the job of replacing the current panel in the FieldEditor with a new Panel.
	 * @param newDisplay	The new JPanel to be displayed.
	 */
	
	public void updateFieldEditor(JPanel newDisplay) {
		fieldEditor.setVisible(false);
		
		fieldEditorContainer.remove(fieldEditor);
		
		fieldEditor = newDisplay;
			
		fieldEditorContainer.add(fieldEditor, BorderLayout.EAST);
		
		protocolTab.validate();
		//fieldEditor.validate();
		fieldEditor.setVisible(fieldEditorAndToolbarVisible);	// need to hide, then show to get refresh to work
	}

	

	// used to draw focus away from fieldEditor panel, so that updateDataField() is called
	public class ScrollPaneMouseListener implements MouseListener {
		public void mouseEntered(MouseEvent event) {}
		public void mouseExited(MouseEvent event) {}
		public void mousePressed(MouseEvent event) {}
		public void mouseReleased(MouseEvent event) {}
		public void mouseClicked(MouseEvent event) {
			XMLScrollPane.requestFocusInWindow();
		}
	}
	
	/* 
	 * used for setting the power-user shortcuts to menu items 
	 * @param character  eg. KeyEvent.VK_C
	 */
	public static void setMenuItemAccelerator(JMenuItem menuItem, int character) {
		if (isMacOs()) {
			menuItem.setAccelerator(KeyStroke.getKeyStroke(character, ActionEvent.META_MASK));
		} else {
			menuItem.setAccelerator(KeyStroke.getKeyStroke(character, KeyEvent.CTRL_DOWN_MASK));
		}
	}
	
	/* method used for OS-specific UI settings */
	public static boolean isMacOs() {
		String osName = System.getProperty("os.name");
		return (osName.contains("Mac OS"));
	}
	
	public void addAbstractButton(AbstractButton button, String text, Icon icon, 
			String toolTipText, Border border, ActionListener actionListener, JComponent comp) {
		if (button == null) {
			button = new JButton();
		}
		if (text != null) button.setText(text);
		if (icon != null) button.setIcon(icon);
		if (toolTipText != null) button.setToolTipText(toolTipText);
		if (border != null) button.setBorder(border);
		if (actionListener != null)  button.addActionListener(actionListener);
		if (comp != null)  comp.add(button);
	}
	
	
	public void addButton(int controllerIndex, String text, Border border, JComponent comp) {
		JButton button = new JButton(controller.getAction(controllerIndex));
		if (text != null) button.setText(text);
		if (border != null) button.setBorder(border);
		if (comp != null)  comp.add(button);
	}
	public void addButton(JButton button, Border border, JComponent comp) {
		if (button == null) return;
		if (border != null) button.setBorder(border);
		if (comp != null)  comp.add(button);
	}
	
	public JFrame getFrame() {
		return XMLFrame;
	}
}

