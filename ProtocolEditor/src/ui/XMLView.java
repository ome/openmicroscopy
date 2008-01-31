/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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

package ui;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;


import ols.ObservationCreator;
import omeXml.OmeXmlQueryer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.*;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import search.IndexFiles;
import search.SearchPanel;
import tree.DataField;
import tree.DataFieldNode;
import tree.Tree;
import tree.Tree.Actions;
import ui.components.ExportDialog;
import util.ExceptionHandler;
import util.FileDownload;
import util.HtmlOutputter;
import util.ImageFactory;
import util.PropertiesManager;
import xmlMVC.XMLModel;


// the main View class. 
// defines the app window and lays out all components
// processes all user commands from buttons etc..

public class XMLView 
	implements XMLUpdateObserver, SelectionObserver, ActionListener 
	{
	
	XMLModel xmlModel;

	DataFieldNode protocolRootNode;
	
	DataFieldNode rootOfImportTree;
	
	ArrayList<DataField> findTextSearchHits = new ArrayList<DataField>();
	int findTextHitIndex; 	// the current hit
	
	// state changes 
	int editingState = EDITING_FIELDS;
	public static final int EDITING_FIELDS = 0;
	public static final int IMPORTING_FIELDS = 1;
	
	private boolean fieldEditorAndToolbarVisible = true;
	
	// commands 
	public static final String COPY = "copy";
	public static final String PASTE = "paste";
	public static final String FIND = "find";
	public static final String WINDOW = "window";
	public static final String HIDE_FIND_BOX = "HideFindBox";
	public static final String NEXT_FIND_HIT = "NextFindHit";
	public static final String PREV_FIND_HIT = "PrevFindHit";
	public static final String LOAD_DEFAULTS = "LoadDefaults";
	public static final String LOAD_DEFAULTS_HIGHLIGHTED_FIELDS = "LoadDefaultsHighlightedFields";
	
	// tab positions 
	public static final int EXPERIMENT_TAB = 0;
	public static final int PROTOCOL_TAB = 1;
	
	public static final int CONTROL_CLICK = 18;
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
	FormDisplay xmlFormDisplay;			// this is the UI-form
	JSplitPane XMLUIPanel;		// the top-level panel of the frame
	
	JPanel fieldEditor; 	// shows each field to be edited
	JPanel fieldEditorContainer;	// holds show/hide button above FieldEditor
	JButton showHideFieldEditorButton;
	
	Color backgroundColor;
	
	JPanel fileManagerPanel; 	// tool bar for open/close/print etc. 
	
	JButton saveFileButton;
	JButton saveFileAsButton;
	JButton printButton;
	
	JButton addAnInput;
	JButton deleteAnInput;
	JButton promoteField;
	JButton demoteField;
	JButton duplicateField;
	JTextField searchField;
	
	JMenuItem saveFileMenuItem;
	JMenuItem saveFileAsMenuItem;
	
	JMenuItem undoMenuItem;
	JMenuItem redoMenuItem;
	JButton undoButton;
	JButton redoButton;
	
	Box findBox;
	JButton findButton;
	JTextField findTextField;
	JButton findNextButton;
	JButton findPrevButton;
	JLabel hitsCountLabel;
	
	JMenu loadDefaultsSubMenu;
	JMenuItem clearFieldsMenuItem;
	JMenuItem multiplyValueOfSelectedFieldsMenuItem;
	
	JCheckBoxMenuItem showEditToolbarMenuItem;
	JMenuItem addFieldMenuItem;
	JMenuItem deleteFieldMenuItem;
	JMenuItem moveStepUpMenuItem;
	JMenuItem moveStepDownMenuItem;
	JMenuItem promoteStepMenuItem;
	JMenuItem demoteStepMenuItem;
	JMenuItem duplicateFieldMenuItem;
	JMenuItem importFieldsMenuItem;
	JMenuItem copyFieldsMenuItem;
	JMenuItem pasteFieldsMenuItem;
	
	JButton xmlValidationButton;
	JCheckBox xmlValidationCheckbox;
	JPanel xmlValidationPanel;
	Icon closeIcon;
	Icon redBallIcon;

	JToolBar protocolEditToolBar;
	
	JComboBox currentlyOpenedFiles;
	JButton closeFileButton;
	FileListSelectionListener fileListSelectionListener;
	
	JPanel protocolTab;
	
	static final int windowHeight = 460;
    static final int panelWidth = 950;

	private JPopupMenu printPopupMenu;

	private JPopupMenu loadDefaultsPopupMenu;

	private Icon newFileIcon;
	private Icon openFileIcon;
	private Icon validationIcon;
	private Icon saveIcon;
	private Icon printIcon;
	private Icon loadDefaultsIcon;
	private Icon clearFieldsIcon;
	private Icon saveFileAsIcon;
	private Icon addIcon;
	private Icon deleteIcon;
	private Icon moveUpIcon;
	private Icon moveDownIcon;
	private Icon promoteIcon;
	private Icon demoteIcon;
	private Icon duplicateIcon;
	private Icon copyIcon;
	private Icon pasteIcon;
	private Icon importElementsIcon;
	private Icon mathsIcon;
	private Icon undoIcon;
	private Icon redoIcon;
	private Icon wwwFileIcon;
	private Icon findIcon;
	private Icon fileCloseIcon;
	private Icon previousUpIcon;
	private Icon nextDownIcon;
	private Icon moreLikeThisIcon;
	private Icon searchIcon;
	private Icon configureIcon;
	private Icon protocolIcon;
	private Icon sendCommentIcon;

	protected JButton loadDefaultsButton;

	protected JButton clearFieldsButton;

	protected JButton multiplyValueOfSelectedFieldsButton;
    
	
    public XMLView(XMLModel xmlModel) {
    	
    	this.xmlModel = xmlModel;
    	
    	xmlModel.addXMLObserver(this);
    	xmlModel.addSelectionObserver(this);
    	
    	protocolRootNode = xmlModel.getRootNode();
    	
    	// buildXMLForm();
    	
    	buildUI();
    	
    }
    
    public void buildUI() {
		
		searchIcon = ImageFactory.getInstance().getIcon(ImageFactory.SEARCH_ICON);
		moreLikeThisIcon = ImageFactory.getInstance().getIcon(ImageFactory.MORE_LIKE_THIS_ICON);
		newFileIcon = ImageFactory.getInstance().getIcon(ImageFactory.NEW_FILE_ICON);
		openFileIcon = ImageFactory.getInstance().getIcon(ImageFactory.OPEN_FILE_ICON);
		validationIcon = ImageFactory.getInstance().getIcon(ImageFactory.VALIDATION_ICON);
		saveIcon = ImageFactory.getInstance().getIcon(ImageFactory.SAVE_ICON);
		printIcon = ImageFactory.getInstance().getIcon(ImageFactory.PRINT_ICON);
		loadDefaultsIcon = ImageFactory.getInstance().getIcon(ImageFactory.LOAD_DEFAULTS_ICON);
		clearFieldsIcon = ImageFactory.getInstance().getIcon(ImageFactory.CLEAR_FIELDS_ICON);
		saveFileAsIcon = ImageFactory.getInstance().getIcon(ImageFactory.SAVE_FILE_AS_ICON);
		addIcon = ImageFactory.getInstance().getIcon(ImageFactory.ADD_ICON);
		deleteIcon = ImageFactory.getInstance().getIcon(ImageFactory.DELETE_ICON);
		moveUpIcon = ImageFactory.getInstance().getIcon(ImageFactory.MOVE_UP_ICON);
		moveDownIcon = ImageFactory.getInstance().getIcon(ImageFactory.MOVE_DOWN_ICON);
		promoteIcon = ImageFactory.getInstance().getIcon(ImageFactory.PROMOTE_ICON);
		demoteIcon = ImageFactory.getInstance().getIcon(ImageFactory.DEMOTE_ICON);
		duplicateIcon = ImageFactory.getInstance().getIcon(ImageFactory.DUPLICATE_ICON);
		copyIcon = ImageFactory.getInstance().getIcon(ImageFactory.COPY_ICON);
		pasteIcon = ImageFactory.getInstance().getIcon(ImageFactory.PASTE_ICON);
		importElementsIcon = ImageFactory.getInstance().getIcon(ImageFactory.IMPORT_ICON);
		mathsIcon = ImageFactory.getInstance().getIcon(ImageFactory.EDU_MATHS);
		closeIcon = ImageFactory.getInstance().getIcon(ImageFactory.N0);
		redBallIcon = ImageFactory.getInstance().getIcon(ImageFactory.RED_BALL_ICON);
		undoIcon = ImageFactory.getInstance().getIcon(ImageFactory.UNDO_ICON);
		redoIcon = ImageFactory.getInstance().getIcon(ImageFactory.REDO_ICON);
		wwwFileIcon = ImageFactory.getInstance().getIcon(ImageFactory.WWW_FILE_ICON);
		findIcon = ImageFactory.getInstance().getIcon(ImageFactory.FIND_ICON);
		fileCloseIcon = ImageFactory.getInstance().getIcon(ImageFactory.FILE_CLOSE_ICON);
		previousUpIcon = ImageFactory.getInstance().getIcon(ImageFactory.PREVIOUS_UP_ICON);
		nextDownIcon = ImageFactory.getInstance().getIcon(ImageFactory.NEXT_DOWN_ICON);
		configureIcon = ImageFactory.getInstance().getIcon(ImageFactory.CONFIGURE_ICON);
		protocolIcon = ImageFactory.getInstance().getIcon(ImageFactory.PROTOCOL_ICON);
		sendCommentIcon = ImageFactory.getInstance().getIcon(ImageFactory.SEND_COMMENT_ICON);
		
		
//		 controls for changing currently opened file, and closing current file
		fileManagerPanel = new JPanel();
		fileManagerPanel.setLayout(new BorderLayout());
		Box fileManagerEastToolBar = Box.createHorizontalBox();
		Box fileManagerWestToolBar = Box.createHorizontalBox();
		Border fileManagerToolBarBorder = new EmptyBorder(2,BUTTON_SPACING,2,BUTTON_SPACING);
		
		JButton newFileButton = new JButton(newFileIcon);
		newFileButton.setToolTipText("New Blank File");
		newFileButton.addActionListener(new newProtocolFileListener());
		newFileButton.setBorder(new EmptyBorder(2,BUTTON_SPACING + 3,2,BUTTON_SPACING));
		
		JButton openFileButton = new JButton(openFileIcon);
		openFileButton.setToolTipText("Open File..");
		openFileButton.addActionListener(new openFileListener());
		openFileButton.setBorder(fileManagerToolBarBorder);
		
		JButton openWwwFileButton = new JButton(wwwFileIcon);
		openWwwFileButton.setToolTipText("Open file from URL");
		openWwwFileButton.addActionListener(new OpenWwwFileListener());
		openWwwFileButton.setBorder(fileManagerToolBarBorder);
		
		saveFileAsButton = new JButton(saveFileAsIcon);
		saveFileAsButton.setToolTipText("Save File As..");
		saveFileAsButton.setBorder(fileManagerToolBarBorder);
		saveFileAsButton.addActionListener(new SaveFileAsListener());
		
		saveFileButton = new JButton(saveIcon);
		saveFileButton.addActionListener(new SaveCurrentFileListener());
		saveFileButton.setToolTipText("Save File");
		saveFileButton.setBorder(fileManagerToolBarBorder);
		
		JMenuItem printWholeFile = new JMenuItem("Export the whole document");
		printWholeFile.addActionListener(new PrintWholeFileListener());
		JMenuItem printSelectedFields = new JMenuItem("Export highlighted fields");
		printSelectedFields.addActionListener(new PrintSelectedFieldsListener());
		printPopupMenu = new JPopupMenu("Print Options");
		printPopupMenu.add(printWholeFile);
		printPopupMenu.add(printSelectedFields);
		
		printButton = new JButton(printIcon);
		printButton.addMouseListener(new MouseListener() {
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}
			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}
			private void maybeShowPopup(MouseEvent e) {
				printPopupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		});
		//printButton.addActionListener(new PrintWholeFileListener());
		printButton.setToolTipText("Export the current file for printing..."); 
		printButton.setBorder(fileManagerToolBarBorder);
		
		

		// experiment tool-bar buttons
		
		loadDefaultsPopupMenu = new JPopupMenu("Load Defaults");
		JMenuItem loadDefaults = new JMenuItem("Load Defaults for All fields");
		loadDefaults.setActionCommand(LOAD_DEFAULTS);
		loadDefaults.addActionListener(new LoadDefaultsListener());
		loadDefaultsPopupMenu.add(loadDefaults);
		
		JMenuItem loadDefaultsHighLtFields = new JMenuItem("Load Defaults for Highlighted Fields (and all child fields)");
		loadDefaultsHighLtFields.setActionCommand(LOAD_DEFAULTS_HIGHLIGHTED_FIELDS);
		loadDefaultsHighLtFields.addActionListener(new LoadDefaultsListener());
		loadDefaultsPopupMenu.add(loadDefaultsHighLtFields);
		
		loadDefaultsButton = new JButton(loadDefaultsIcon);
		loadDefaultsButton.setToolTipText("Load Default Values");
		loadDefaultsButton.setBorder(fileManagerToolBarBorder);
		loadDefaultsButton.addMouseListener(new MouseListener() {
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}
			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}
			private void maybeShowPopup(MouseEvent e) {
				loadDefaultsPopupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		});
		
		clearFieldsButton = new JButton(clearFieldsIcon);
		clearFieldsButton.setToolTipText("Clear All Fields");
		clearFieldsButton.addActionListener(new ClearFieldsListener());
		clearFieldsButton.setBorder(fileManagerToolBarBorder);
		
		multiplyValueOfSelectedFieldsButton = new JButton(mathsIcon);
		multiplyValueOfSelectedFieldsButton.setToolTipText("Multiply the selected numerical values by a factor of...");
		multiplyValueOfSelectedFieldsButton.addActionListener(new MultiplyValueOfSelectedFieldsListener());
		multiplyValueOfSelectedFieldsButton.setBorder(fileManagerToolBarBorder);
	
		// undo - redo buttons
		
		undoButton = new JButton(undoIcon);
		undoButton.setToolTipText("Undo last action");
		undoButton.setBorder(fileManagerToolBarBorder);
		undoButton.addActionListener(new UndoActionListener());
		
		redoButton = new JButton(redoIcon);
		redoButton.setToolTipText("Redo last action");
		redoButton.setBorder(fileManagerToolBarBorder);
		redoButton.addActionListener(new RedoActionListener());
		
		// "Find" controls
		ActionListener findTextListener = new FindTextListener();
		findButton = new JButton(findIcon);
		findButton.setToolTipText("Find a word in the current file");
		findButton.setBorder(fileManagerToolBarBorder);
		findButton.addActionListener(findTextListener);
		
		findBox = Box.createHorizontalBox();
		findTextField = new JTextField();
		findTextField.setColumns(10);
		Dimension findTextFieldDim = new Dimension(200, 22);
		findTextField.addCaretListener(new FindTextCharacterListener());
		findTextField.setMinimumSize(findTextFieldDim);
		findTextField.setMaximumSize(findTextFieldDim);
		findTextField.setPreferredSize(findTextFieldDim);
		findTextField.addActionListener(findTextListener);
		
		JButton hideFindBoxButton = new JButton(fileCloseIcon);
		hideFindBoxButton.setToolTipText("Hide the 'Find' text field");
		hideFindBoxButton.setActionCommand(HIDE_FIND_BOX);
		hideFindBoxButton.addActionListener(findTextListener);
		hideFindBoxButton.setBorder(new EmptyBorder(1,1,1,1));
		
		findNextButton = new JButton(nextDownIcon);
		findNextButton.setToolTipText("Find next occurrence of search word");
		findNextButton.setActionCommand(NEXT_FIND_HIT);
		findNextButton.addActionListener(findTextListener);
		findNextButton.setBorder(new EmptyBorder(1,1,1,1));
		findNextButton.setBackground(null);
		
		findPrevButton = new JButton(previousUpIcon);
		findPrevButton.setToolTipText("Find previous occurrence of search word");
		findPrevButton.setActionCommand(PREV_FIND_HIT);
		findPrevButton.addActionListener(findTextListener);
		findPrevButton.setBackground(null);
		findPrevButton.setBorder(new EmptyBorder(1,1,1,1));
		
		hitsCountLabel = new JLabel("");
		hitsCountLabel.setBorder(new EmptyBorder(1,1,1,1));
		
		findBox.add(hideFindBoxButton);
		findBox.add(findTextField);
		findBox.add(findPrevButton);
		findBox.add(findNextButton);
		findBox.add(hitsCountLabel);
		findBox.setBorder(new EmptyBorder(1,1,1,1));
		
		
		// test button
		JButton printObservationsButton = new JButton("print observations");
		printObservationsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ObservationCreator.testPrintAllObservations(xmlModel);
			}
		});
		
		
		// more-like this button
		
		JButton moreLikeThisButton = new JButton(moreLikeThisIcon);
		moreLikeThisButton.setToolTipText("'More Like This' search to find files like the current one");;
		moreLikeThisButton.setBorder(new EmptyBorder(0,2,0,4));
		moreLikeThisButton.addActionListener(new MoreLikeThisListener());
		
		// comboBox for switching between opened files 
		// also, close file button
		
		closeFileButton = new JButton(closeIcon);
		closeFileButton.setToolTipText("Close the current file");
		closeFileButton.setBorder(new EmptyBorder(0,2,0,4));
		closeFileButton.addActionListener(new CloseFileListener());
		
		String[] openFiles = xmlModel.getOpenFileList();
		currentlyOpenedFiles = new JComboBox(openFiles);
		fileListSelectionListener = new FileListSelectionListener();
		currentlyOpenedFiles.addActionListener(fileListSelectionListener);
		
		fileManagerWestToolBar.add(newFileButton);
		fileManagerWestToolBar.add(openFileButton);
		fileManagerWestToolBar.add(openWwwFileButton);
		fileManagerWestToolBar.add(saveFileButton);
		fileManagerWestToolBar.add(saveFileAsButton);
		fileManagerWestToolBar.add(printButton);
		fileManagerWestToolBar.add(new JSeparator(JSeparator.VERTICAL));
		fileManagerWestToolBar.add(loadDefaultsButton);
		fileManagerWestToolBar.add(clearFieldsButton);
		fileManagerWestToolBar.add(multiplyValueOfSelectedFieldsButton);
		fileManagerWestToolBar.add(new JSeparator(JSeparator.VERTICAL));
		fileManagerWestToolBar.add(undoButton);
		fileManagerWestToolBar.add(redoButton);
		fileManagerWestToolBar.add(new JSeparator(JSeparator.VERTICAL));
		fileManagerWestToolBar.add(findButton);
		fileManagerWestToolBar.add(findBox);
		// fileManagerWestToolBar.add(printObservationsButton);
		fileManagerPanel.add(fileManagerWestToolBar, BorderLayout.WEST);
		
		fileManagerEastToolBar.add(new JLabel("Current file:"));
		fileManagerEastToolBar.add(currentlyOpenedFiles);
		fileManagerEastToolBar.add(moreLikeThisButton);
		fileManagerEastToolBar.add(closeFileButton);
		fileManagerPanel.add(fileManagerEastToolBar, BorderLayout.EAST);
	
		
		// Build left side with form UI

		
		xmlFormDisplay = new FormDisplay(this);
		backgroundColor = xmlFormDisplay.getBackground();
		
		XMLScrollPane = new JScrollPane(xmlFormDisplay, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		XMLScrollPane.setFocusable(true);
		XMLScrollPane.setMinimumSize(new Dimension(200, 200));
		XMLScrollPane.addMouseListener(new ScrollPaneMouseListener());
		XMLScrollPane.setPreferredSize(new Dimension( panelWidth, windowHeight));

		
	
		// JButton compareFilesButton = new JButton("compare files");
		// compareFilesButton.setToolTipText("Compare the first two files in the list");
		// compareFilesButton.addActionListener(new CompareFileListener());
		
//		 Make a nice border
		EmptyBorder eb = new EmptyBorder(BUTTON_SPACING,BUTTON_SPACING,BUTTON_SPACING,BUTTON_SPACING);
		EmptyBorder noLeftPadding = new EmptyBorder (BUTTON_SPACING, 2, BUTTON_SPACING, BUTTON_SPACING);
		EmptyBorder noRightPadding = new EmptyBorder (BUTTON_SPACING, BUTTON_SPACING, BUTTON_SPACING, 1);
		EmptyBorder thinLeftRightPadding = new EmptyBorder (BUTTON_SPACING, 4, BUTTON_SPACING, 3);
	
		
		// Protocol edit buttons
		
		addAnInput = new JButton(addIcon);
		addAnInput.setToolTipText("Add a field to the template");
		addAnInput.addActionListener(new AddDataFieldListener());
		addAnInput.setBorder(noRightPadding);
		
		duplicateField = new JButton(duplicateIcon);
		duplicateField.setToolTipText("Duplicate the selected field. To select multiple fields use Shift-Click");
		duplicateField.addActionListener(new DuplicateFieldListener());
		duplicateField.setBorder(thinLeftRightPadding);
		
		deleteAnInput = new JButton(deleteIcon);
		deleteAnInput.setToolTipText("Delete the highlighted fields from the protocol");
		deleteAnInput.addActionListener(new deleteDataFieldListener());
		deleteAnInput.setBorder(noLeftPadding);
		
		JButton moveUpButton = new JButton(moveUpIcon);
		moveUpButton.setToolTipText("Move the field up");
		moveUpButton.addActionListener(new MoveFieldUpListener());
		moveUpButton.setBorder(noRightPadding);
		
		JButton moveDownButton = new JButton(moveDownIcon);
		moveDownButton.setToolTipText("Move the field down");
		moveDownButton.addActionListener(new MoveFieldDownListener());
		moveDownButton.setBorder(noLeftPadding);
		
		promoteField = new JButton(promoteIcon);
		promoteField.setToolTipText("Indent fields to left.  To select multiple steps use Shift-Click");
		promoteField.addActionListener(new PromoteFieldListener());
		promoteField.setBorder(noRightPadding);
		
		demoteField = new JButton(demoteIcon);
		demoteField.setToolTipText("Indent fields to right");
		demoteField.addActionListener(new DemoteFieldListener());
		demoteField.setBorder(noLeftPadding);
		
		JButton importElemetsButton = new JButton(importElementsIcon);
		importElemetsButton.setToolTipText("Import fields from another document or XML file");
		importElemetsButton.addActionListener(new InsertElementsFromFileListener());
		importElemetsButton.setBorder(eb);
		
		JButton copyButton = new JButton(copyIcon);
		copyButton.setToolTipText("Copy fields to clipboard");
		copyButton.setActionCommand(COPY);
		copyButton.addActionListener(this);
		copyButton.setBorder(noRightPadding);
		
		JButton pasteButton = new JButton(pasteIcon);
		pasteButton.setToolTipText("Paste fields from clipboard");
		pasteButton.setActionCommand(PASTE);
		pasteButton.addActionListener(this);
		pasteButton.setBorder(noLeftPadding);
		
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
		
		xmlValidationBox.add(xmlValidationButton);
		xmlValidationBox.add(xmlValidationCheckbox);
		xmlValidationPanel.add(xmlValidationBox, BorderLayout.EAST);
		//xmlValidationPanel.setMaximumSize(new Dimension(100, 22));
		
		
		protocolEditToolBar = new JToolBar("Close toolbar to re-Dock");
		protocolEditToolBar.add(addAnInput);
		protocolEditToolBar.add(duplicateField);
		protocolEditToolBar.add(deleteAnInput);
		protocolEditToolBar.add(moveUpButton);
		protocolEditToolBar.add(moveDownButton);
		protocolEditToolBar.add(promoteField);
		protocolEditToolBar.add(demoteField);
		protocolEditToolBar.add(copyButton);
		protocolEditToolBar.add(pasteButton);
		protocolEditToolBar.add(importElemetsButton);
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
		updateUndoRedo();	// disable undo redo 
		findBox.setVisible(false); 	// not visible until findButton clicked
	    
		
//		 not visible till a file is opened
		protocolTab.setVisible(false);
		
		
		//showStartUpDialog();
		
		// buildFrame();	// do this from ProtocolEditor.main() now.
	}
	
	public void buildFrame() {
	    	XMLFrame = new JFrame("Protocol Editor");
	    	XMLFrame.setIconImage( ((ImageIcon)protocolIcon).getImage());
	    	
			// Build menus
			JMenuBar menuBar = new JMenuBar();
			EmptyBorder menuItemBorder = new EmptyBorder(0,5,0,5);
			
			// File menu
			JMenu fileMenu = new JMenu("File");
			fileMenu.setBorder(menuItemBorder);
			
			JMenuItem openFile = new JMenuItem("Open File..", openFileIcon);
			setMenuItemAccelerator(openFile, KeyEvent.VK_O);
			openFile.addActionListener(new openFileListener());
			
			JMenuItem newBlankProtocolMenuItem = new JMenuItem("New Blank Protocol", newFileIcon);
			setMenuItemAccelerator(newBlankProtocolMenuItem, KeyEvent.VK_N);
			newBlankProtocolMenuItem.addActionListener(new newProtocolFileListener());
			
			JMenuItem closeFileMenuItem = new JMenuItem("Close File", closeIcon);
			closeFileMenuItem.addActionListener(new CloseFileListener());
			setMenuItemAccelerator(closeFileMenuItem, KeyEvent.VK_W);
			
			saveFileMenuItem = new JMenuItem("Save File", saveIcon);
			setMenuItemAccelerator(saveFileMenuItem, KeyEvent.VK_S);
			saveFileMenuItem.addActionListener(new SaveCurrentFileListener());
			
			saveFileAsMenuItem = new JMenuItem("Save File As...", saveFileAsIcon);
			saveFileAsMenuItem.addActionListener(new SaveFileAsListener());
	
			JMenu printsubMenu = new JMenu("Export for printing...");
			printsubMenu.setIcon(printIcon);
			JMenuItem printWholeFileMenuItem = new JMenuItem("Export the whole document");
			printWholeFileMenuItem.addActionListener(new PrintWholeFileListener());
			setMenuItemAccelerator(printWholeFileMenuItem, KeyEvent.VK_P);
			printsubMenu.add(printWholeFileMenuItem);
			
			JMenuItem printSelectedFieldsMenuItem = new JMenuItem("Export highlighted fields");
			printSelectedFieldsMenuItem.addActionListener(new PrintSelectedFieldsListener());
			printsubMenu.add(printSelectedFieldsMenuItem);
			
			JMenuItem indexFilesMenuItem = new JMenuItem("Index files for searching");
			indexFilesMenuItem.addActionListener(new IndexFilesListener());
			
			
			fileMenu.add(openFile);
			fileMenu.add(newBlankProtocolMenuItem);
			fileMenu.add(closeFileMenuItem);
			fileMenu.add(saveFileMenuItem);
			fileMenu.add(saveFileAsMenuItem);
			fileMenu.add(printsubMenu);
			fileMenu.add(indexFilesMenuItem);
			menuBar.add(fileMenu);
			
			
			// "Edit" menu
			JMenu editMenu = new JMenu("Edit");
			editMenu.setBorder(menuItemBorder);
			
			loadDefaultsSubMenu = new JMenu("Load Default Values");
			loadDefaultsSubMenu.setIcon(loadDefaultsIcon);
			
			JMenuItem loadDefaultsMenuItem = new JMenuItem("Load Defaults for All Fields");
			loadDefaultsMenuItem.setActionCommand(LOAD_DEFAULTS);
			loadDefaultsMenuItem.addActionListener(new LoadDefaultsListener());
			loadDefaultsSubMenu.add(loadDefaultsMenuItem);
			JMenuItem loadDefaultsHighLtFieldsMenuItem = new JMenuItem("Load Defaults for Highlighted fields (and child fields)");
			loadDefaultsHighLtFieldsMenuItem.setActionCommand(LOAD_DEFAULTS_HIGHLIGHTED_FIELDS);
			loadDefaultsHighLtFieldsMenuItem.addActionListener(new LoadDefaultsListener());
			loadDefaultsSubMenu.add(loadDefaultsHighLtFieldsMenuItem);
			
			clearFieldsMenuItem = new JMenuItem("Clear Values from All Fields", clearFieldsIcon);
			clearFieldsMenuItem.addActionListener(new ClearFieldsListener());
			
			multiplyValueOfSelectedFieldsMenuItem = new JMenuItem("MultiplyValuesBy...", mathsIcon);
			multiplyValueOfSelectedFieldsMenuItem.addActionListener(new MultiplyValueOfSelectedFieldsListener());
			
			// undo-redo menu items
			undoMenuItem = new JMenuItem("Undo", undoIcon);
			undoMenuItem.addActionListener(new UndoActionListener());
			setMenuItemAccelerator(undoMenuItem, KeyEvent.VK_Z);
			
			redoMenuItem = new JMenuItem("Redo", redoIcon);
			redoMenuItem.addActionListener(new RedoActionListener());
			setMenuItemAccelerator(redoMenuItem, KeyEvent.VK_Y);
			
			JMenuItem findMenuItem = new JMenuItem("Find", findIcon);
			findMenuItem.addActionListener(new FindTextListener());
			setMenuItemAccelerator(findMenuItem, KeyEvent.VK_F);
			
			editMenu.add(loadDefaultsSubMenu);
			editMenu.add(clearFieldsMenuItem);
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
			addFieldMenuItem = new JMenuItem("Add Step", addIcon);
			deleteFieldMenuItem = new JMenuItem("Delete Step", deleteIcon);
			moveStepUpMenuItem = new JMenuItem("Move Step Up", moveUpIcon);
			moveStepDownMenuItem = new JMenuItem("Move Step Down", moveDownIcon);
			promoteStepMenuItem = new JMenuItem("Promote Step (indent to left)", promoteIcon);
			demoteStepMenuItem = new JMenuItem("Demote Step (indent to right)", demoteIcon);
			duplicateFieldMenuItem = new JMenuItem("Duplicate Step", duplicateIcon);
			importFieldsMenuItem = new JMenuItem("Import Steps", importElementsIcon);
			copyFieldsMenuItem = new JMenuItem("Copy Steps", copyIcon);
			copyFieldsMenuItem.setActionCommand(COPY);
			setMenuItemAccelerator(copyFieldsMenuItem, KeyEvent.VK_C);
			pasteFieldsMenuItem = new JMenuItem("Paste Steps", pasteIcon);
			pasteFieldsMenuItem.setActionCommand(PASTE);
			setMenuItemAccelerator(pasteFieldsMenuItem, KeyEvent.VK_V);
			
			showEditToolbarMenuItem.addActionListener(new ToggleEditTemplateControlsListener());
			addFieldMenuItem.addActionListener(new AddDataFieldListener());
			deleteFieldMenuItem.addActionListener(new deleteDataFieldListener());
			moveStepUpMenuItem.addActionListener(new MoveFieldUpListener());
			moveStepDownMenuItem.addActionListener(new MoveFieldDownListener());
			promoteStepMenuItem.addActionListener(new PromoteFieldListener());
			demoteStepMenuItem.addActionListener(new DemoteFieldListener());
			duplicateFieldMenuItem.addActionListener(new DuplicateFieldListener());
			importFieldsMenuItem.addActionListener(new InsertElementsFromFileListener());
			copyFieldsMenuItem.addActionListener(this);
			pasteFieldsMenuItem.addActionListener(this);
			
			protocolMenu.add(showEditToolbarMenuItem);
			protocolMenu.add(addFieldMenuItem);
			protocolMenu.add(deleteFieldMenuItem);
			protocolMenu.add(moveStepUpMenuItem);
			protocolMenu.add(moveStepDownMenuItem);
			protocolMenu.add(promoteStepMenuItem);
			protocolMenu.add(demoteStepMenuItem);
			protocolMenu.add(duplicateFieldMenuItem);
			protocolMenu.add(importFieldsMenuItem);
			protocolMenu.add(copyFieldsMenuItem);
			protocolMenu.add(pasteFieldsMenuItem);
			menuBar.add(protocolMenu);
			
			
			// Help menu
			JMenu helpMenu = new JMenu("Help");
			helpMenu.setBorder(menuItemBorder);
			
			JMenuItem sendCommentMenuItem = new JMenuItem("Send Comment", sendCommentIcon);
			sendCommentMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ExceptionHandler.showCommentDialog();	
				}
			});
			helpMenu.add(sendCommentMenuItem);
			menuBar.add(helpMenu);
			
			// search Field and button
			searchField = new JTextField("Search Files", 20);
			searchField.addActionListener(new SearchFieldListener());	
			JPanel searchPanel = new JPanel();
			searchPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
			searchPanel.setLayout(new BorderLayout());
			searchPanel.add(searchField, BorderLayout.EAST);
			
			
			JButton searchButton = new JButton(searchIcon);
			searchButton.addActionListener(new SearchFieldListener());
			searchButton.setBorder(new EmptyBorder(3,3,3,3));
	
			menuBar.add(searchPanel);
			menuBar.add(searchButton);
			
			// disable save etc.
			refreshAnyFilesOpen();
	
		    // set up frame
			XMLFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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
		//		Custom button text
				Icon bigProtocolIcon = ImageFactory.getInstance().getIcon(ImageFactory.BIG_PROTOCOL_ICON);
				Object[] options = {"Start blank protocol",
				                    "Open existing file",
				                    "Cancel"};
				int n = JOptionPane.showOptionDialog(mainContentPane, "<html>Welcome to the Protocol Editor. <br>"
				    + "Please choose an option to get you started.</html>", "Welcome",
				    JOptionPane.YES_NO_CANCEL_OPTION,
				    JOptionPane.QUESTION_MESSAGE,
				    bigProtocolIcon,
				    options,
				    options[0]);
				
				if (n == 0) newProtocolFile();
				else if (n == 1) openFile();
	}
	
	// turn on/off the controls for editing
	// these are turned off when no files open
	public void enableEditingControls(boolean enabled){
		
		// only modify menu items if the Frame has been built
		if (XMLFrame != null) {
			addFieldMenuItem.setEnabled(enabled);
			deleteFieldMenuItem.setEnabled(enabled);
			moveStepUpMenuItem.setEnabled(enabled);
			moveStepDownMenuItem.setEnabled(enabled);
			promoteStepMenuItem.setEnabled(enabled);
			demoteStepMenuItem.setEnabled(enabled);
			duplicateFieldMenuItem.setEnabled(enabled);
			importFieldsMenuItem.setEnabled(enabled);
		
			loadDefaultsSubMenu.setEnabled(enabled);
			clearFieldsMenuItem.setEnabled(enabled);
			multiplyValueOfSelectedFieldsMenuItem.setEnabled(enabled);
		}
		
		printButton.setEnabled(enabled);
		loadDefaultsButton.setEnabled(enabled);
		clearFieldsButton.setEnabled(enabled);
		multiplyValueOfSelectedFieldsButton.setEnabled(enabled);
		
	}

	
	// called by NotifyXMLObservers in XMLModel. 
	public void xmlUpdated() {
		protocolRootNode = xmlModel.getRootNode();
		xmlFormDisplay.refreshForm();		// refresh the form
		
		xmlValidationCheckbox.setSelected(xmlModel.getXmlValidation());
		// clear the Find-Text search hits: don't want to keep ref to deleted fields etc
		clearFindTextHits();
		
		selectionChanged();	// to take account of any other changes
	}
	
	// selection observer method, fired when tree changes selection
	// this method called when UI needs updating, but xml not changed.
	public void selectionChanged() {
		// System.out.println("XMLView.selectionChanged");
		if (editingState == EDITING_FIELDS) {
			updateFieldEditor();
		}
		updateUndoRedo();
		refreshFileEdited();
		updateFileList();
		updateXmlValidationPanel();
	}
	
	public void updateUndoRedo() {
		undoButton.setEnabled(xmlModel.canUndo());
		undoButton.setToolTipText(xmlModel.getUndoCommand());
		redoButton.setEnabled(xmlModel.canRedo());
		redoButton.setToolTipText(xmlModel.getRedoCommand());
		
		// only modify menus if Frame has been built
		if (XMLFrame != null) {
			undoMenuItem.setText(xmlModel.getUndoCommand());
			undoMenuItem.setEnabled(xmlModel.canUndo());
			redoMenuItem.setText(xmlModel.getRedoCommand());
			redoMenuItem.setEnabled(xmlModel.canRedo());
		}
	}

	public void updateFileList() {
		currentlyOpenedFiles.removeActionListener(fileListSelectionListener);
		
		currentlyOpenedFiles.removeAllItems();
		
		String[] fileList = xmlModel.getOpenFileList();
		
		if (fileList.length > 0){
		
			for (int i=0; i<fileList.length; i++) {
				currentlyOpenedFiles.addItem(fileList[i]);
			}
			currentlyOpenedFiles.setSelectedIndex(xmlModel.getCurrentFileIndex());
		}
		
		refreshAnyFilesOpen();
		
		currentlyOpenedFiles.addActionListener(fileListSelectionListener);
	}
	
	// if no files open, hide the tabbed pane
	public void refreshAnyFilesOpen() {
		
		boolean noFiles = (xmlModel.getOpenFileList().length == 0);
		
		enableEditingControls(!noFiles); // disables menu items
		
		saveFileButton.setEnabled(!noFiles);
		saveFileAsButton.setEnabled(!noFiles);
		
		// only modify menus if Frame has been built
		if (XMLFrame != null) {
			saveFileMenuItem.setEnabled(!noFiles);
			saveFileAsMenuItem.setEnabled(!noFiles);
		}
		protocolTab.setVisible(!noFiles);
	}
	
	public void deleteDataFields() {
		
		int result = JOptionPane.showConfirmDialog(XMLFrame, 
				"Delete highlighted fields?", "Delete?",
		JOptionPane.YES_NO_OPTION,
 	    JOptionPane.QUESTION_MESSAGE);

		if (result == 0) {	// delete all
			xmlModel.editCurrentTree(Actions.DELTE_FIELDS);
		}
	}
	
	public void promoteDataFields() {
		xmlModel.editCurrentTree(Actions.PROMOTE_FIELDS);
	}
	
	public void demoteDataFields() {
		xmlModel.editCurrentTree(Actions.DEMOTE_FIELDS);
	}
	
	public void moveFieldsUp() {
		// if the highlighted fields have a preceding sister, move it below the highlighted fields
		xmlModel.editCurrentTree(Actions.MOVE_FIELDS_UP);
		}
	public void moveFieldsDown() {
		xmlModel.editCurrentTree(Actions.MOVE_FIELDS_DOWN);
	}
	
	public void addDataField() {
		// add dataField after last selected one
		xmlModel.editCurrentTree(Actions.ADD_NEW_FIELD);
	}
	
	public void closeCurrentFile() {
		
//		 check whether you want to save edited file (Protocol is edited. No check for experiment edit / save)
		if (xmlModel.isCurrentFileEdited()) {
			int result = JOptionPane.showConfirmDialog
				(XMLUIPanel, "Save the current file before closing?");
			if (result == JOptionPane.YES_OPTION) {
				// save Protocol (no exp details)	Experiment must be saved by user manually
				saveFileAs();
			} else if (result == JOptionPane.CANCEL_OPTION) {
				return;
			}
		}
		
		xmlModel.closeCurrentFile();
	}
	
	// open a blank protocol (after checking you want to save!)
	public void newProtocolFile() {
		xmlModel.openBlankProtocolFile();
	}
	
	
	//open a file
	public void openFile() {
		
		// Create a file chooser
		final JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new OpenProExpXmlFileFilter());
		
		File currentLocation = null;
		if (PropertiesManager.getProperty(PropertiesManager.CURRENT_FILES_FOLDER) != null) {
			currentLocation = new File(PropertiesManager.getProperty(PropertiesManager.CURRENT_FILES_FOLDER));
		} 
		fc.setCurrentDirectory(currentLocation);

		int returnVal = fc.showOpenDialog(XMLFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            File xmlFile = fc.getSelectedFile();
         // remember where the user last saved a file
            PropertiesManager.setProperty(PropertiesManager.CURRENT_FILES_FOLDER, xmlFile.getParent());
            openThisFile(xmlFile);
		}
	}
	
	
	public void openThisFile(File file) {
		 boolean openOK = xmlModel.openXMLFile(file);
		 
		 if (!openOK) {
//			custom title, error icon
			 JOptionPane.showMessageDialog(XMLFrame,
			     "Problem reading file.",
			     "XML error",
			     JOptionPane.ERROR_MESSAGE);
			 return;
		 }
	}
	
	
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		if (command.equals(COPY)) xmlModel.copyHighlightedFieldsToClipboard();
		else if (command.equals(PASTE)) xmlModel.pasteHighlightedFieldsFromClipboard();
	}
	
	public void mainWindowClosing() {
		
		if (xmlModel.tryClosingAllFiles()) {
			System.exit(0);
		}
		
		Object[] options = {"Yes",
        "No"};
    	int n = JOptionPane.showOptionDialog(XMLFrame,
    		    "Some files failed to close because they are unsaved.\nWould you like to quit anyway?",
    		    "Files unsaved!",
    		    JOptionPane.YES_NO_OPTION,
    		    JOptionPane.QUESTION_MESSAGE,
    		    null,     //don't use a custom Icon
    		    options,  //the titles of buttons
    		    options[0]); //default button title
    	if (n == 0) {
    		System.exit(0);
    	} else {
    		xmlUpdated();	// refresh since some files may have been closed
    	}
	}
	
	public void multiplyValueOfSelectedFields() {
		String s = (String)JOptionPane.showInputDialog(
                XMLFrame,
                "Multiply all selected field values by:\n"
                + "(enter a number)\n NB: This will only apply to NUMBER fields \n"
                + "To divide, use /, eg '/3' ",
                "Enter a number",
                JOptionPane.QUESTION_MESSAGE);
		
		if (s != null && s.length() > 0) {
			boolean division = false;
			if (s.startsWith("/")) {
				s = s.substring(1);
				division = true;
			}
			
			float factor;
			try {
				factor = Float.parseFloat(s);
				if (division) xmlModel.multiplyValueOfSelectedFields(1/factor);
				else xmlModel.multiplyValueOfSelectedFields(factor);
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(XMLFrame, 
						"You didn't enter a valid number", 
						"Invalid multiplication factor", 
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public void findMoreLikeThis() {
		File file = xmlModel.getCurrentFile();
		
		if (file == null) return;
		
		// if file has not been saved yet...
		if (file.getName().equals("untitled")) {
			// this would over-write any old "untitled" file in this location
			xmlModel.saveTreeToXmlFile(file);
		}
		
		searchFiles(file);
	}
	
	// turn on and off the validation of the current xml tree. 
	public void enableXmlValidation(boolean validationOn) {
		xmlModel.setXmlValidation(validationOn);
		updateXmlValidationPanel();	
	}
	
	public void openWwwFile() {
		Object[] possibilities = {"http://cvs.openmicroscopy.org.uk/svn/specification/Xml/Working/completesample.xml",
				"http://trac.openmicroscopy.org.uk/~will/protocolFiles/experiments/AuroraB%20fix-stain.exp", 
				"http://trac.openmicroscopy.org.uk/~will/protocolFiles/experiments/arwen_slice_1.exp"};
		String url = (String)JOptionPane.showInputDialog(
                XMLFrame,
                "Enter a url for an XML file to open:",
                "Open a www file",
                JOptionPane.PLAIN_MESSAGE,
                null,
                possibilities,
                "");
		
		try {
			File downloadedFile = FileDownload.downloadFile(url);
			this.openThisFile(downloadedFile);
		} catch (MalformedURLException ex) {
			JOptionPane.showMessageDialog(XMLFrame, "invalid URL, please try again");
		}

	}
	
	/*
	 * "Find" function to highlight fields containing a search-word. 
	 */
	public void findTextInOpenFile() {

		String searchWord = findTextField.getText().trim();
		if (searchWord.length() == 0) {
			findTextSearchHits.clear();
			// update the findNext and findPrev buttons
			updateFindNextPrev();
			return;
		}
		
		
		findTextSearchHits = xmlModel.getSearchResults(searchWord);
			
		findTextHitIndex = 0;
		
		// if you have at least one result, display it
		if (findTextSearchHits != null && !findTextSearchHits.isEmpty()) {

			DataField field = findTextSearchHits.get(findTextHitIndex);
			displayThisDataField(field);
		}
		// update the findNext and findPrev buttons
		updateFindNextPrev();
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
	
	public void updateFindNextPrev() {
		hitsCountLabel.setText(findTextSearchHits.size() + ((findTextSearchHits.size()==1) ? " hit": " hits" ));
		if (findTextSearchHits.isEmpty()) {
			findNextButton.setEnabled(false);
			findPrevButton.setEnabled(false);
			return;
		}
		
		findNextButton.setEnabled(findTextSearchHits.size() > findTextHitIndex+1);
		findPrevButton.setEnabled(findTextHitIndex > 0);	
	}
	
	/* hide the findTextControlls - eg when user closes panel */
	public void hideFindTextControls() {
		findBox.setVisible(false);
	}
	/* show the findTextControls */
	public void showFindTextControls() {
		findBox.setVisible(true);
		XMLFrame.validate();
		findTextField.requestFocusInWindow();
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

	/* clear the search results - eg when user edits file */
	public void clearFindTextHits() {
		findTextSearchHits.clear();
		updateFindNextPrev();
	}
	
	public class ToggleEditTemplateControlsListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			fieldEditorAndToolbarVisible = !fieldEditorAndToolbarVisible;
				
			refreshFieldEditorAndToolbarVisibility();
		}
	}
	
	/* takes all events from the find text functionality */
	public class FindTextListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
		
			// first use of this function is to display textField if hidden - 
			// search is also performed (see below) in case the file was edited 
			if (!findBox.isVisible()) {
				showFindTextControls();
			}
			String actionCommand = event.getActionCommand();
			// actionEvent from hide button, hide 
			if (actionCommand.equals(HIDE_FIND_BOX)) {
				hideFindTextControls();
			} else if (actionCommand.equals(NEXT_FIND_HIT)) {
				findTextHitIndex++;
				DataField field = findTextSearchHits.get(findTextHitIndex);
				displayThisDataField(field);
				updateFindNextPrev();
			} else if (actionCommand.equals(PREV_FIND_HIT)) {
				findTextHitIndex--;
				DataField field = findTextSearchHits.get(findTextHitIndex);
				displayThisDataField(field);
				updateFindNextPrev();
			}
			// otherwise, do search
			else findTextInOpenFile();
		}
	}
	
	// "Find" performed each time a character is typed
	public class FindTextCharacterListener implements CaretListener {
		public void caretUpdate(CaretEvent arg0) {
			//findTextInOpenFile();
		}	
	}
	
	public class OpenWwwFileListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			openWwwFile();
		}
	}
	
	public class ClearFieldsListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			xmlModel.editCurrentTree(Actions.CLEAR_FIELDS);
		}
	}
	
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
	
	public class UndoActionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			xmlModel.editCurrentTree(Actions.UNDO_LAST_ACTION);
		}
	}
	public class RedoActionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			xmlModel.editCurrentTree(Actions.REDO_ACTION);
		}
	}
	
	public class MoreLikeThisListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			findMoreLikeThis();
		}
	}
	
	public class MultiplyValueOfSelectedFieldsListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			multiplyValueOfSelectedFields();
		}
	}
	
	public class CloseFileListener implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			closeCurrentFile();
		}
		
	}
	
	public class CompareFileListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			xmlModel.compareCurrentFiles();
		}	
	}
	
	public class FileListSelectionListener implements ActionListener {
			
		public void actionPerformed (ActionEvent event) {
			JComboBox source = (JComboBox)event.getSource();
			int selectedIndex = source.getSelectedIndex();
			xmlModel.changeCurrentFile(selectedIndex);
		}
	}	
	public class SearchFieldListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			searchFiles();
		}
	}
	
	public void searchFiles() {
		JPanel searchPanel = new SearchPanel(searchField.getText(), this);
		updateSearchPanel(searchPanel);
		//XMLTabbedPane.setSelectedIndex(EXPERIMENT_TAB);
	}
	
	public void searchFiles(File file) {
		JPanel searchPanel = new SearchPanel(file, this);
		updateSearchPanel(searchPanel);
		//XMLTabbedPane.setSelectedIndex(EXPERIMENT_TAB);
	}
	
	public class IndexFilesListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
				indexFiles();
		}
	}
	public void indexFiles() {
		 //Create a file chooser
		final JFileChooser fc = new JFileChooser();
			
		fc.setCurrentDirectory(xmlModel.getCurrentFile());
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
		int returnVal = fc.showOpenDialog(XMLFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File folderToIndex = fc.getSelectedFile();
			String[] path = {folderToIndex.getAbsolutePath()};
	            
			IndexFiles.main(path);
		}
	}
	
	public void updateSearchPanel(JPanel newPanel) {
		XMLUIPanel.setRightComponent(newPanel);
		if (newPanel != null)	XMLUIPanel.setDividerSize(10);
		else XMLUIPanel.setDividerSize(0);
		
		XMLUIPanel.setDividerLocation(0.5f);
		
		XMLUIPanel.validate();
		XMLUIPanel.resetToPreferredSizes();
	}
	
	
	public class PrintWholeFileListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			ArrayList<DataFieldNode> rootNode = new ArrayList<DataFieldNode>();
			rootNode.add(protocolRootNode); 
			
			printToHtml(rootNode);
		}
	}
	public class PrintSelectedFieldsListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			printToHtml(xmlModel.getHighlightedFields());
		}
	}
	
	
	public void printToHtml(ArrayList<DataFieldNode> rootNodes) {
		
		// Can't work out how to export xsl file into .jar
		// xmlModel.transformXmlToHtml();
		
		// use this method for now
		
		final JFileChooser fc = new JFileChooser();
		
		File currentLocation = null;
		if (PropertiesManager.getProperty(PropertiesManager.CURRENT_EXPORT_FOLDER) != null) {
			currentLocation = new File(PropertiesManager.getProperty(PropertiesManager.CURRENT_EXPORT_FOLDER));
		} 
		fc.setCurrentDirectory(currentLocation);

		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				if (f.getName().endsWith(".html") || (f.isDirectory()))
					return true;
				return false;
			}
			public String getDescription() {
				return ".html files";
			}
		});
		int returnVal = fc.showSaveDialog(XMLFrame);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = fc.getSelectedFile();
		if (file.isDirectory()) {
            JOptionPane.showMessageDialog(XMLFrame, "Please choose a file name (not a directory)");
            // try again! 
            printToHtml(rootNodes);
            return;
        }
		
		// remember where the user last exported a file
        PropertiesManager.setProperty(PropertiesManager.CURRENT_EXPORT_FOLDER, file.getParent());
		
		// now, make sure the filename ends .html
        String filePathAndName = file.getAbsolutePath();
        if (!(filePathAndName.endsWith(".html"))) {	// if not..
        	filePathAndName = filePathAndName + ".html";
        	file = new File(filePathAndName);
        }
		
		LinkedHashMap<String,Boolean> booleanMap = new LinkedHashMap<String,Boolean>();
		booleanMap.put("Show every field (include collapsed fields)", true);
		booleanMap.put("Show descriptions", true);
		booleanMap.put("Show default values", true);
		booleanMap.put("Show Url", true);
		booleanMap.put("Show Table Data", true);
		booleanMap.put("Show all other attributes", true);
	
		
		ExportDialog printDialog = new ExportDialog(XMLFrame, printButton, "Print Options", booleanMap);
		printDialog.pack();
		printDialog.setVisible(true);
		
		if (printDialog.getValue().equals(JOptionPane.OK_OPTION)) {
		
			booleanMap = printDialog.getBooleanMap();
			boolean showEveryField = booleanMap.get("Show every field (include collapsed fields)");
			boolean	showDescriptions = booleanMap.get("Show descriptions");
			boolean	showDefaultValues = booleanMap.get("Show default values");
			boolean	showUrl = booleanMap.get("Show Url");
			boolean	showAllOtherAttributes = booleanMap.get("Show all other attributes");
			boolean printTableData = booleanMap.get("Show Table Data");
		
			HtmlOutputter.outputHTML(file, rootNodes, showEveryField, 
					showDescriptions, showDefaultValues, 
					showUrl, showAllOtherAttributes, printTableData);
		}
	}

	
	public class LoadDefaultsListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			String command = event.getActionCommand();
			if (command.equals(LOAD_DEFAULTS)) {
				xmlModel.editCurrentTree(Actions.LOAD_DEFAULTS);
			} else if (command.equals(LOAD_DEFAULTS_HIGHLIGHTED_FIELDS)) {
				xmlModel.editCurrentTree(Actions.LOAD_DEFAULTS_HIGHLIGHTED_FIELDS);
			}
		}
	}
	
	public class newProtocolFileListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			newProtocolFile();
		}
	}
	
	public class AddDataFieldListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			addDataField();
		}
	}
	
	public class DuplicateFieldListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			duplicateFields();
		}
	}
	
	public class MoveFieldUpListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			moveFieldsUp();
		}
	}	
	public class MoveFieldDownListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			moveFieldsDown();
		}
	}
	public class PromoteFieldListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			promoteDataFields();
		}
	}
	public class DemoteFieldListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			demoteDataFields();
		}
	}
	
	public class deleteDataFieldListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			deleteDataFields();
		}
	}
	
	public class openFileListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			openFile();
		}
	}
	
	
	public class InsertElementsFromFileListener implements ActionListener {
		public void actionPerformed(ActionEvent event){
			insertFieldsFromFile();
		}
	}
	
	// saves updates to the current file - or calls saveAs
	public class SaveCurrentFileListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			// if the current file is not saved (still called "untitled")
			if (xmlModel.getCurrentFile() == null) return;
			
			if (xmlModel.getCurrentFile().getName().equals("untitled")) {
				saveFileAs();
			}
			else {
				int option = JOptionPane.showConfirmDialog(XMLFrame, "Save changes? " + 
						"\n This will over-write the original file",
						"Save Changes?", JOptionPane.OK_CANCEL_OPTION);
				if (option == JOptionPane.OK_OPTION) {
					xmlModel.saveTreeToXmlFile(xmlModel.getCurrentFile());
					JOptionPane.showMessageDialog(XMLFrame, "Experiment saved.");
				}
			}
		}
	}
	
	public class SaveFileAsListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			saveFileAs();
		}
	}
	

	public class OpenProExpXmlFileFilter extends FileFilter {
		public boolean accept(File file) {
			boolean recognisedFileType = 
				//	allows "MS Windows" to see directories
				((file.getName().endsWith("pro")) || (file.getName().endsWith("exp")) || 
						(file.getName().endsWith("xml")) || (file.isDirectory()));
			return recognisedFileType;
		}
		public String getDescription() {
			return " .pro .exp .xml files";
		}
	}
	
	public void saveFileAs() {
		
		final JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new OpenProExpXmlFileFilter());
		
		File currentLocation = null;
		if (PropertiesManager.getProperty(PropertiesManager.CURRENT_FILES_FOLDER) != null) {
			currentLocation = new File(PropertiesManager.getProperty(PropertiesManager.CURRENT_FILES_FOLDER));
		} 
		fc.setCurrentDirectory(currentLocation);

		int returnVal = fc.showSaveDialog(XMLFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            File xmlFile = fc.getSelectedFile();  // this may be a directory! 
            
            // remember where the user last saved a file
            PropertiesManager.setProperty(PropertiesManager.CURRENT_FILES_FOLDER, xmlFile.getParent());
            
            if (xmlFile.isDirectory()) {
                JOptionPane.showMessageDialog(XMLFrame, "Please choose a file name (not a directory)");
                // try again! 
                saveFileAs();
                return;
            }

            // first, make sure the filename ends .exp.xml
            String filePathAndName = xmlFile.getAbsolutePath();
            if (!(filePathAndName.endsWith(".pro.xml"))) {	// if not..
            	filePathAndName = filePathAndName + ".pro.xml";
            	xmlFile = new File(filePathAndName);
            }
               
            // now check if the file exists. If so, take appropriate action
            if (xmlFile.exists()) {
            	int result = JOptionPane.showConfirmDialog(XMLUIPanel, "File exists. Overwrite it?");
            	if (!(result == JOptionPane.YES_OPTION)) {	// if not yes, then forget it!
            		return;
            	}
        	}
            
            xmlModel.saveTreeToXmlFile(xmlFile);
            
            refreshFileEdited();
		}
	}
	
	
	// called when user changes to Editing Protocol. 
	/*public void setEditingOfExperimentalValues(boolean enabled) {
		
		if (protocolRootNode == null) return;
		
		Iterator iterator = protocolRootNode.createIterator();
			
			while (iterator.hasNext()) {
				DataFieldNode node = (DataFieldNode)iterator.next();
				node.getDataField().setExperimentalEditing(enabled);
			}
	}*/
	
	public void refreshFileEdited() {
		
		boolean protocolEdited = xmlModel.isCurrentFileEdited();
			
		// if file edited, the close button looks different
		closeFileButton.setIcon(protocolEdited ? redBallIcon : closeIcon);
	}
	
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
	
//	 refresh the right panel with details of a new dataField.
	public void updateFieldEditor() {
		
		JPanel newDisplay = xmlModel.getFieldEditorToDisplay();
		updateFieldEditor(newDisplay);
	}
	
	public void updateFieldEditor(JPanel newDisplay) {
		fieldEditor.setVisible(false);
		
		fieldEditorContainer.remove(fieldEditor);
		
		fieldEditor = newDisplay;
			
		fieldEditorContainer.add(fieldEditor, BorderLayout.EAST);
		
		protocolTab.validate();
		//fieldEditor.validate();
		fieldEditor.setVisible(fieldEditorAndToolbarVisible);	// need to hide, then show to get refresh to work
	}
	
	public void insertFieldsFromFile() {
//		 Create a file chooser
		final JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new OpenProExpXmlFileFilter());
		
		fc.setCurrentDirectory(xmlModel.getCurrentFile());
		int returnVal = fc.showOpenDialog(XMLFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
           File xmlFile = fc.getSelectedFile();	
           
           Tree importTree = xmlModel.getTreeFromNewFile(xmlFile); 
           
           if (importTree == null) { // problem parsing XML
       			 JOptionPane.showMessageDialog(XMLFrame,
       			     "Problem reading file.",
       			     "XML error",
       			     JOptionPane.ERROR_MESSAGE);
       			 return;
           }
           
           xmlModel.setImportTree(importTree);
           
           rootOfImportTree = importTree.getRootNode();
           
           JPanel importTreeView = new ImportElementChooser(rootOfImportTree, this);
           
           updateFieldEditor(importTreeView);
           
           editingState = IMPORTING_FIELDS;
		}
	}
	
	public void setEditingState(int newState) {
		editingState = newState;
		
		// if finished importing, kill the reference to imported data
		if (editingState != IMPORTING_FIELDS) 
			xmlModel.setImportTree(null);
	}
	
	public void importFieldsFromImportTree() {
		xmlModel.importFieldsFromImportTree();
	}
	
	public void duplicateFields() {
		// get selected Fields, duplicate and add after last selected one
		xmlModel.editCurrentTree(Actions.DUPLICATE_FIELDS);
	}

	
	public DataFieldNode getRootNode() {
		return protocolRootNode;
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
	
}

