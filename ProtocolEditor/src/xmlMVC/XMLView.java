package xmlMVC;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;
import java.io.File;
import java.util.Iterator;


// the main View class. 
// defines the app window and lays out all components
// processes all user commands from buttons etc..

public class XMLView implements XMLUpdateObserver, SelectionObserver, ActionListener {
	
	XMLModel xmlModel;

	DataFieldNode protocolRootNode;
	
	DataFieldNode rootOfImportTree;
	
	// state changes 
	int editingState = EDITING_FIELDS;
	public static final int EDITING_FIELDS = 0;
	public static final int IMPORTING_FIELDS = 1;
	
	// commands 
	public static final String COPY = "copy";
	public static final String PASTE = "paste";
	
	// tab positions 
	public static final int EXPERIMENT_TAB = 0;
	public static final int PROTOCOL_TAB = 1;
	
	public static final int CONTROL_CLICK = 18;
	public static final int SHIFT_CLICK = 17;
	public static final Font FONT_H1 = new Font("SansSerif", Font.BOLD, 18);
	public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 12);
	public static final Font FONT_TINY = new Font("SansSerif", Font.PLAIN, 9);
	public static final Font FONT_INVISIBLE = new Font("Sanserif", Font.PLAIN, 1);
	public static final Color BLUE_HIGHLIGHT = new Color(181,213,255);
	
	public static final int BUTTON_SPACING = 5;
	
	JFrame XMLFrame;
	JSplitPane splitPane;
	JScrollPane XMLScrollPane;	// contains the UI
	FormDisplay xmlFormDisplay;			// this is the UI-form
	JSplitPane XMLUIPanel;		// the top-level panel of the frame
	JPanel fieldEditor; 	// shows each field to be edited
	JTabbedPane XMLTabbedPane;
	Color backgroundColor;
	
	
	JButton saveFileButton;
	JButton saveFileAsButton;
	JButton addAnInput;
	JButton deleteAnInput;
	JButton promoteField;
	JButton demoteField;
	JButton duplicateField;
	JTextField searchField;
	
	JMenuItem saveFileMenuItem;
	JMenuItem saveFileAsMenuItem;
	
	JMenuItem loadDefaultsMenuItem;
	JMenuItem multiplyValueOfSelectedFieldsMenuItem;
	JCheckBoxMenuItem editExperimentMenuItem;
	
	JCheckBoxMenuItem editProtocolMenuItem;
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
	
	Box expTabToolBar;
	Box proTabToolBar;
	
	JComboBox currentlyOpenedFiles;
	JButton closeFileButton;
	FileListSelectionListener fileListSelectionListener;
	
	JPanel protocolTab;
	
	static final int windowHeight = 460;
    static final int panelWidth = 950;
    
	
    public XMLView(XMLModel xmlModel) {
    	
    	this.xmlModel = xmlModel;
    	
    	xmlModel.addXMLObserver(this);
    	xmlModel.addSelectionObserver(this);
    	
    	protocolRootNode = xmlModel.getRootNode();
    	
    	// buildXMLForm();
    	
    	buildUI();
    	
    }
    
	public void buildUI() {
		
	//	XMLPanel = new JPanel(); // parent panel fills all frame
		XMLFrame = new JFrame("Protocol Editor");
		// Build menus
		JMenuBar menuBar = new JMenuBar();
		EmptyBorder menuItemBorder = new EmptyBorder(0,5,0,5);
		
		// icons
		Icon newFileIcon = ImageFactory.getInstance().getIcon(ImageFactory.NEW_FILE_ICON);
		Icon openFileIcon = ImageFactory.getInstance().getIcon(ImageFactory.OPEN_FILE_ICON);
		Icon validationIcon = ImageFactory.getInstance().getIcon(ImageFactory.VALIDATION_ICON);
		Icon saveIcon = ImageFactory.getInstance().getIcon(ImageFactory.SAVE_ICON);
		Icon printIcon = ImageFactory.getInstance().getIcon(ImageFactory.PRINT_ICON);
		Icon loadDefaultsIcon = ImageFactory.getInstance().getIcon(ImageFactory.LOAD_DEFAULTS_ICON);
		Icon saveFileAsIcon= ImageFactory.getInstance().getIcon(ImageFactory.SAVE_FILE_AS_ICON);
		Icon addIcon = ImageFactory.getInstance().getIcon(ImageFactory.ADD_ICON);
		Icon deleteIcon = ImageFactory.getInstance().getIcon(ImageFactory.DELETE_ICON);
		Icon moveUpIcon = ImageFactory.getInstance().getIcon(ImageFactory.MOVE_UP_ICON);
		Icon moveDownIcon = ImageFactory.getInstance().getIcon(ImageFactory.MOVE_DOWN_ICON);
		Icon promoteIcon = ImageFactory.getInstance().getIcon(ImageFactory.PROMOTE_ICON);
		Icon demoteIcon = ImageFactory.getInstance().getIcon(ImageFactory.DEMOTE_ICON);
		Icon duplicateIcon = ImageFactory.getInstance().getIcon(ImageFactory.DUPLICATE_ICON);
		Icon importElementsIcon = ImageFactory.getInstance().getIcon(ImageFactory.IMPORT_ICON);
		Icon mathsIcon = ImageFactory.getInstance().getIcon(ImageFactory.EDU_MATHS);
		closeIcon = ImageFactory.getInstance().getIcon(ImageFactory.N0);
		redBallIcon = ImageFactory.getInstance().getIcon(ImageFactory.RED_BALL_ICON);
		
		// File menu
		JMenu fileMenu = new JMenu("File");
		fileMenu.setBorder(menuItemBorder);
		JMenuItem openFile = new JMenuItem("Open File..", openFileIcon);
		JMenuItem newBlankProtocolMenuItem = new JMenuItem("New Blank Protocol", newFileIcon);
		saveFileMenuItem = new JMenuItem("Save File", saveIcon);
		saveFileAsMenuItem = new JMenuItem("Save File As...", saveFileAsIcon);
		JMenuItem printExp= new JMenuItem("Print Experiment", printIcon);
		JMenuItem indexFilesMenuItem = new JMenuItem("Index files for searching");
		
		openFile.addActionListener(new openFileListener());
		newBlankProtocolMenuItem.addActionListener(new newProtocolFileListener());
		saveFileMenuItem.addActionListener(new SaveCurrentFileListener());
		saveFileAsMenuItem.addActionListener(new SaveFileAsListener());
		printExp.addActionListener(new PrintExperimentListener());
		indexFilesMenuItem.addActionListener(new IndexFilesListener());
		
		fileMenu.add(openFile);
		fileMenu.add(newBlankProtocolMenuItem);
		fileMenu.add(saveFileMenuItem);
		fileMenu.add(saveFileAsMenuItem);
		fileMenu.add(printExp);
		fileMenu.add(indexFilesMenuItem);
		menuBar.add(fileMenu);
		
		// experiment menu
		JMenu experimentMenu = new JMenu("Experiment");
		experimentMenu.setBorder(menuItemBorder);
		editExperimentMenuItem = new JCheckBoxMenuItem("Edit Experiment");
		loadDefaultsMenuItem = new JMenuItem("Load Default Values", loadDefaultsIcon);
		multiplyValueOfSelectedFieldsMenuItem = new JMenuItem("MultiplyValuesBy...", mathsIcon);
		
		editExperimentMenuItem.addActionListener(new EditExperimentListener());
		loadDefaultsMenuItem.addActionListener(new LoadDefaultsListener());
		multiplyValueOfSelectedFieldsMenuItem.addActionListener(new MultiplyValueOfSelectedFieldsListener());
		experimentMenu.add(editExperimentMenuItem);
		experimentMenu.add(loadDefaultsMenuItem);
		experimentMenu.add(multiplyValueOfSelectedFieldsMenuItem);
		menuBar.add(experimentMenu);
		
		// protocol menu
		JMenu protocolMenu = new JMenu("Protocol");
		protocolMenu.setBorder(menuItemBorder);
		
		editProtocolMenuItem= new JCheckBoxMenuItem("Edit Protocol...");
		JMenuItem printProtocolMenuItem = new JMenuItem("Print Protocol", printIcon);
		addFieldMenuItem = new JMenuItem("Add Step", addIcon);
		deleteFieldMenuItem = new JMenuItem("Delete Step", deleteIcon);
		moveStepUpMenuItem = new JMenuItem("Move Step Up", moveUpIcon);
		moveStepDownMenuItem = new JMenuItem("Move Step Down", moveDownIcon);
		promoteStepMenuItem = new JMenuItem("Promote Step (indent to left)", promoteIcon);
		demoteStepMenuItem = new JMenuItem("Demote Step (indent to right)", demoteIcon);
		duplicateFieldMenuItem = new JMenuItem("Duplicate Step", duplicateIcon);
		importFieldsMenuItem = new JMenuItem("Import Steps", importElementsIcon);
		copyFieldsMenuItem = new JMenuItem("Cntrl-C Copy Steps");
		copyFieldsMenuItem.setActionCommand(COPY);
		pasteFieldsMenuItem = new JMenuItem("Cntrl-V Paste Steps");
		pasteFieldsMenuItem.setActionCommand(PASTE);
		
		editProtocolMenuItem.addActionListener(new ToggleProtocolEditingListener());
		printProtocolMenuItem.addActionListener(new PrintProtocolListener());
		addFieldMenuItem.addActionListener(new addDataFieldListener());
		deleteFieldMenuItem.addActionListener(new deleteDataFieldListener());
		moveStepUpMenuItem.addActionListener(new MoveFieldUpListener());
		moveStepDownMenuItem.addActionListener(new MoveFieldDownListener());
		promoteStepMenuItem.addActionListener(new PromoteFieldListener());
		demoteStepMenuItem.addActionListener(new DemoteFieldListener());
		duplicateFieldMenuItem.addActionListener(new DuplicateFieldListener());
		importFieldsMenuItem.addActionListener(new InsertElementsFromFileListener());
		copyFieldsMenuItem.addActionListener(this);
		pasteFieldsMenuItem.addActionListener(this);
		
		protocolMenu.add(editProtocolMenuItem);
		protocolMenu.add(printProtocolMenuItem);
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
		
		
		// search Field and button
		searchField = new JTextField("Search", 20);
		searchField.addActionListener(new SearchFieldListener());	
		JPanel searchPanel = new JPanel();
		searchPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
		searchPanel.setLayout(new BorderLayout());
		searchPanel.add(searchField, BorderLayout.EAST);
		
		Icon searchIcon = ImageFactory.getInstance().getIcon(ImageFactory.SEARCH_ICON);
		JButton searchButton = new JButton(searchIcon);
		searchButton.addActionListener(new SearchFieldListener());
		searchButton.setBorder(new EmptyBorder(3,3,3,3));

		menuBar.add(searchPanel);
		menuBar.add(searchButton);
		
		
		
//		 controls for changing currently opened file, and closing current file
		JPanel fileManagerPanel = new JPanel();
		fileManagerPanel.setLayout(new BorderLayout());
		Box fileManagerEastToolBar = Box.createHorizontalBox();
		Box fileManagerWestToolBar = Box.createHorizontalBox();
		
		JButton newFileButton = new JButton(newFileIcon);
		newFileButton.setToolTipText("New Protocol");
		newFileButton.addActionListener(new newProtocolFileListener());
		newFileButton.setBorder(new EmptyBorder(2,BUTTON_SPACING + 3,2,BUTTON_SPACING));
		
		JButton openFileButton = new JButton(openFileIcon);
		openFileButton.setToolTipText("Open Protocol or Experiment");
		openFileButton.addActionListener(new openFileListener());
		openFileButton.setBorder(new EmptyBorder(2,BUTTON_SPACING,2,BUTTON_SPACING));
		
		saveFileAsButton = new JButton(saveFileAsIcon);
		saveFileAsButton.setToolTipText("Save Protocol As..");
		saveFileAsButton.setBorder(new EmptyBorder(2,BUTTON_SPACING,2,BUTTON_SPACING));
		saveFileAsButton.addActionListener(new saveProtocolListener());
		
		saveFileButton = new JButton(saveIcon);
		saveFileButton.addActionListener(new SaveCurrentFileListener());
		saveFileButton.setToolTipText("Save experimental details");
		saveFileButton.setBorder(new EmptyBorder(2,BUTTON_SPACING,2,BUTTON_SPACING));
		
		
		JButton moreLikeThisButton = new JButton("More Files Like This >", searchIcon);
		moreLikeThisButton.addActionListener(new MoreLikeThisListener());
		
		
		closeFileButton = new JButton(closeIcon);
		closeFileButton.setBorder(new EmptyBorder(0,2,0,4));
		closeFileButton.addActionListener(new CloseFileListener());
		
		String[] openFiles = xmlModel.getOpenFileList();
		currentlyOpenedFiles = new JComboBox(openFiles);
		fileListSelectionListener = new FileListSelectionListener();
		currentlyOpenedFiles.addActionListener(fileListSelectionListener);
		
		fileManagerWestToolBar.add(newFileButton);
		fileManagerWestToolBar.add(openFileButton);
		fileManagerWestToolBar.add(saveFileButton);
		fileManagerWestToolBar.add(saveFileAsButton);
		fileManagerPanel.add(fileManagerWestToolBar, BorderLayout.WEST);
		
		fileManagerEastToolBar.add(moreLikeThisButton);
		fileManagerEastToolBar.add(currentlyOpenedFiles);
		fileManagerEastToolBar.add(closeFileButton);
		fileManagerPanel.add(fileManagerEastToolBar, BorderLayout.EAST);
	
		
		// Build left side with form UI

		
		xmlFormDisplay = new FormDisplay(this);
		backgroundColor = xmlFormDisplay.getBackground();
		
		XMLScrollPane = new JScrollPane(xmlFormDisplay, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		XMLScrollPane.setFocusable(true);
		XMLScrollPane.addMouseListener(new ScrollPaneMouseListener());
		XMLScrollPane.setPreferredSize(new Dimension( panelWidth, windowHeight));

		
//		 Make a nice border
		EmptyBorder eb = new EmptyBorder(0,BUTTON_SPACING,BUTTON_SPACING,BUTTON_SPACING);
		EmptyBorder noLeftPadding = new EmptyBorder (0, 2, BUTTON_SPACING, BUTTON_SPACING);
		EmptyBorder noRightPadding = new EmptyBorder (0, BUTTON_SPACING, BUTTON_SPACING, 2);
	
		// buttons
		
		JButton printExperimentButton = new JButton(printIcon);
		printExperimentButton.addActionListener(new PrintExperimentListener());
		printExperimentButton.setToolTipText("Print the protocol with experimental details");
		printExperimentButton.setBorder(eb);
		
		JButton loadDefaults = new JButton(loadDefaultsIcon);
		loadDefaults.setToolTipText("Load default values");
		loadDefaults.addActionListener(new LoadDefaultsListener());
		loadDefaults.setBorder(eb);
		
		JButton multiplyValueOfSelectedFieldsButton = new JButton(mathsIcon);
		multiplyValueOfSelectedFieldsButton.setToolTipText("Multiply the selected numerical values by a factor of...");
		multiplyValueOfSelectedFieldsButton.addActionListener(new MultiplyValueOfSelectedFieldsListener());
		multiplyValueOfSelectedFieldsButton.setBorder(eb);
		
		// JButton compareFilesButton = new JButton("compare files");
		// compareFilesButton.setToolTipText("Compare the first two files in the list");
		// compareFilesButton.addActionListener(new CompareFileListener());
		
		// Protocol edit buttons
		
		JButton printProtocolButton = new JButton(printIcon);
		printProtocolButton.setToolTipText("Print the Protocol");
		printProtocolButton.setBorder(eb);
		printProtocolButton.addActionListener(new PrintProtocolListener());
		
		addAnInput = new JButton(addIcon);
		addAnInput.setToolTipText("Add a step to the protocol");
		addAnInput.addActionListener(new addDataFieldListener());
		addAnInput.setBorder(eb);
		
		deleteAnInput = new JButton(deleteIcon);
		deleteAnInput.setToolTipText("Delete the highlighted steps from the protocol");
		deleteAnInput.addActionListener(new deleteDataFieldListener());
		deleteAnInput.setBorder(eb);
		
		JButton moveUpButton = new JButton(moveUpIcon);
		moveUpButton.setToolTipText("Move the step up");
		moveUpButton.addActionListener(new MoveFieldUpListener());
		moveUpButton.setBorder(noRightPadding);
		
		JButton moveDownButton = new JButton(moveDownIcon);
		moveDownButton.setToolTipText("Move the step down");
		moveDownButton.addActionListener(new MoveFieldDownListener());
		moveDownButton.setBorder(noLeftPadding);
		
		promoteField = new JButton(promoteIcon);
		promoteField.setToolTipText("Indent steps to left.  To select multiple steps use Shift-Click");
		promoteField.addActionListener(new PromoteFieldListener());
		promoteField.setBorder(noRightPadding);
		
		demoteField = new JButton(demoteIcon);
		demoteField.setToolTipText("Indent steps to right");
		demoteField.addActionListener(new DemoteFieldListener());
		demoteField.setBorder(noLeftPadding);
		
		duplicateField = new JButton(duplicateIcon);
		duplicateField.setToolTipText("Duplicate the selected step. To select multiple steps use Shift-Click");
		duplicateField.addActionListener(new DuplicateFieldListener());
		duplicateField.setBorder(eb);
		
		JButton importElemetsButton = new JButton(importElementsIcon);
		importElemetsButton.setToolTipText("Import steps from another document");
		importElemetsButton.addActionListener(new InsertElementsFromFileListener());
		importElemetsButton.setBorder(eb);
		
		// XML validation
		xmlValidationPanel = new JPanel(new BorderLayout());
		Box xmlValidationBox = Box.createHorizontalBox();
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
		
		
		expTabToolBar = Box.createHorizontalBox();
		expTabToolBar.add(printExperimentButton);
		expTabToolBar.add(loadDefaults);
		expTabToolBar.add(multiplyValueOfSelectedFieldsButton);
		// expTabToolBar.add(compareFilesButton);
		expTabToolBar.add(Box.createHorizontalGlue());
		expTabToolBar.add(xmlValidationPanel);
		
		proTabToolBar = Box.createHorizontalBox();
		proTabToolBar.add(printProtocolButton);
		proTabToolBar.add(addAnInput);
		proTabToolBar.add(deleteAnInput);
		proTabToolBar.add(moveUpButton);
		proTabToolBar.add(moveDownButton);
		proTabToolBar.add(promoteField);
		proTabToolBar.add(demoteField);
		proTabToolBar.add(duplicateField);
		proTabToolBar.add(importElemetsButton);
		proTabToolBar.add(Box.createHorizontalGlue());
		
		
		fieldEditor = new FieldEditor();
		//splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, XMLScrollPane, fieldEditor);
		//splitPane.setResizeWeight(1.0);
		
		protocolTab = new JPanel();
		protocolTab.setLayout(new BorderLayout());
		protocolTab.add(proTabToolBar, BorderLayout.NORTH);
		protocolTab.add(XMLScrollPane, BorderLayout.CENTER);
		protocolTab.add(fieldEditor, BorderLayout.EAST);
		
		Box experimentTab = Box.createVerticalBox();
		experimentTab.add(expTabToolBar);
		experimentTab.add(XMLScrollPane);
		
		XMLTabbedPane = new JTabbedPane();
		XMLTabbedPane.addTab("Edit Experiment", experimentTab);;
		XMLTabbedPane.addTab("Edit Protocol", protocolTab);
		XMLTabbedPane.addChangeListener(new TabChangeListener());
		
		// split pane to display search results (right only visible after search)
		XMLUIPanel = new JSplitPane();
		XMLUIPanel.setPreferredSize(new Dimension( panelWidth, windowHeight ));
		XMLUIPanel.setDividerSize(0);
		XMLUIPanel.setDividerLocation(1.0);
		XMLUIPanel.setLeftComponent(XMLTabbedPane);
		XMLUIPanel.setRightComponent(null);
		
		enableProtocolEditing(false);	// turn off controls
		noFilesOpen(true);		// disable save etc.
		
		// set up copy and paste listeners
		KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.CTRL_MASK,false);
	    KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V,ActionEvent.CTRL_MASK,false);
	    protocolTab.registerKeyboardAction(this, COPY,copy,JComponent.WHEN_IN_FOCUSED_WINDOW);
	    protocolTab.registerKeyboardAction(this,PASTE,paste,JComponent.WHEN_IN_FOCUSED_WINDOW);
		
	    
	    // set up frame
		XMLFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		XMLFrame.setJMenuBar(menuBar);
		XMLFrame.getContentPane().add("Center", XMLUIPanel);
		XMLFrame.getContentPane().add("North", fileManagerPanel);
		XMLFrame.pack();
		XMLFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                mainWindowClosing();
            }
        });
		XMLFrame.setLocation(200, 100);
		XMLFrame.setVisible(true);
		
		
//		 not visible till a file is opened
		XMLTabbedPane.setVisible(false);
		
		// an intro splash-screen to get users started
//		Custom button text
		Icon bigProtocolIcon = ImageFactory.getInstance().getIcon(ImageFactory.BIG_PROTOCOL_ICON);
		Object[] options = {"Start blank protocol",
		                    "Open existing file",
		                    "Cancel"};
		int n = JOptionPane.showOptionDialog(XMLFrame, "<html>Welcome to the Protocol Editor. <br>"
		    + "Please choose an option to get you started.</html>", "Welcome",
		    JOptionPane.YES_NO_CANCEL_OPTION,
		    JOptionPane.QUESTION_MESSAGE,
		    bigProtocolIcon,
		    options,
		    options[0]);
		
		if (n == 0) newProtocolFile();
		else if (n == 1) openFile();
	}
	
	// turn on/off the controls for editing protocols
	public void enableProtocolEditing(boolean enabled){
		
		addFieldMenuItem.setEnabled(enabled);
		deleteFieldMenuItem.setEnabled(enabled);
		moveStepUpMenuItem.setEnabled(enabled);
		moveStepDownMenuItem.setEnabled(enabled);
		promoteStepMenuItem.setEnabled(enabled);
		demoteStepMenuItem.setEnabled(enabled);
		duplicateFieldMenuItem.setEnabled(enabled);
		importFieldsMenuItem.setEnabled(enabled);
		
		editProtocolMenuItem.setEnabled(!enabled);
		editProtocolMenuItem.setSelected(enabled);
		
		loadDefaultsMenuItem.setEnabled(!enabled);
		multiplyValueOfSelectedFieldsMenuItem.setEnabled(!enabled);
		
		editExperimentMenuItem.setEnabled(enabled);
		editExperimentMenuItem.setSelected(!enabled);
		
		setEditingOfExperimentalValues(!enabled);
	}

	
	// called by NotifyXMLObservers in XMLModel. 
	public void xmlUpdated() {
		protocolRootNode = xmlModel.getRootNode();
		xmlFormDisplay.refreshForm();		// refresh the form
		
		xmlValidationCheckbox.setSelected(xmlModel.getXmlValidation());
		
		selectionChanged();	// to take account of any other changes
	}
	
	// selection observer method, fired when tree changes selection
	// this method called when UI needs updating, but xml not changed.
	public void selectionChanged() {
		if (editingState == EDITING_FIELDS) {
			updateFieldEditor();
		}
		refreshFileEdited();
		updateFileList();
		updateXmlValidationPanel();
	}

	public void updateFileList() {
		currentlyOpenedFiles.removeActionListener(fileListSelectionListener);
		
		currentlyOpenedFiles.removeAllItems();
		
		String[] fileList = xmlModel.getOpenFileList();
		
		if (fileList.length == 0){
			noFilesOpen(true);
		} else {
			
			noFilesOpen(false);
		
			for (int i=0; i<fileList.length; i++) {
				currentlyOpenedFiles.addItem(fileList[i]);
			}
			currentlyOpenedFiles.setSelectedIndex(xmlModel.getCurrentFileIndex());
		}
		
		currentlyOpenedFiles.addActionListener(fileListSelectionListener);
	}
	
	// if no files open, hide the tabbed pane
	public void noFilesOpen(boolean noFiles) {
		
		enableProtocolEditing(noFiles); // disables menu items
		
		saveFileButton.setEnabled(!noFiles);
		saveFileAsButton.setEnabled(!noFiles);
		saveFileMenuItem.setEnabled(!noFiles);
		saveFileAsMenuItem.setEnabled(!noFiles);
		
		XMLTabbedPane.setVisible(!noFiles);
	}
	
	public void deleteDataFields() {
		
		Object[] options = {"Delete all substeps",
                "Move substeps up",
                "Cancel"};
		int result = JOptionPane.showOptionDialog(XMLFrame, 
				"<html>The steps you wish to delete may contain<br>"
				+ "substeps. Do you wish to delete them or move<br>"
				+ "them to a higher level in the hierarchy?</html>", "Delete Options",
		JOptionPane.YES_NO_CANCEL_OPTION,
 	    JOptionPane.QUESTION_MESSAGE,
 	    null,
 	    options,
 	    options[0]);

		
		if (result == 0) {	// delete all
			xmlModel.editCurrentTree(Tree.DELTE_FIELDS);
		}
		if (result == 1) {	// shift children to be siblings, then delete
			xmlModel.editCurrentTree(Tree.DELETE_FIELDS_SAVE_CHILDREN);
		}
	}
	
	public void promoteDataFields() {
		xmlModel.editCurrentTree(Tree.PROMOTE_FIELDS);
	}
	
	public void demoteDataFields() {
		xmlModel.editCurrentTree(Tree.DEMOTE_FIELDS);
	}
	
	public void moveFieldsUp() {
		// if the highlighted fields have a preceding sister, move it below the highlighted fields
		xmlModel.editCurrentTree(Tree.MOVE_FIELDS_UP);
		}
	public void moveFieldsDown() {
		xmlModel.editCurrentTree(Tree.MOVE_FIELDS_DOWN);
	}
	
	public void addDataField() {
		// add dataField after last selected one
		xmlModel.editCurrentTree(Tree.ADD_NEW_FIELD);
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
		XMLTabbedPane.setSelectedIndex(1); 	// protocol Editing tab
	}
	
	
	//open a file
	public void openFile() {
		
		// Create a file chooser
		final JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new OpenProExpXmlFileFilter());
		
		fc.setCurrentDirectory(xmlModel.getCurrentFile());
		int returnVal = fc.showOpenDialog(XMLFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            File xmlFile = fc.getSelectedFile();
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
		 
		 // logical to view experiment file in Experiment tab.
		 if(file.getName().endsWith(".exp")) {
			 setEditingOfExperimentalValues(true);	// if already in exp-tab, need to do this	
			 XMLTabbedPane.setSelectedIndex(EXPERIMENT_TAB);	
		 }
	}
	
	
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		System.out.println(command);
		if (command.equals(COPY)) xmlModel.editCurrentTree(Tree.COPY_FIELDS);
		else if (command.equals(PASTE)) xmlModel.editCurrentTree(Tree.PASTE_FIELDS);
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
		if (validationOn) xmlModel.saxValidateCurrentXmlFile();
		else updateXmlValidationPanel();	// already called when turning validation on. 
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
		XMLTabbedPane.setSelectedIndex(EXPERIMENT_TAB);
	}
	
	public void searchFiles(File file) {
		JPanel searchPanel = new SearchPanel(file, this);
		updateSearchPanel(searchPanel);
		XMLTabbedPane.setSelectedIndex(EXPERIMENT_TAB);
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
		
		XMLUIPanel.validate();
		XMLUIPanel.resetToPreferredSizes();
	}
	
	public class TabChangeListener implements ChangeListener {
		public void stateChanged(ChangeEvent event) {
			JTabbedPane tabbedPane = (JTabbedPane)event.getSource();
			int index = tabbedPane.getSelectedIndex();
			JComponent tab = (JComponent)tabbedPane.getComponentAt(index);
			// can't see the ScrollPane in both tabs at once, so need to keep swapping it to the active tab
			tab.add(XMLScrollPane);
			
			if (index == 0) { // Edit Experiment Tab
				xmlFormDisplay.setBackground(backgroundColor);
				expTabToolBar.add(xmlValidationPanel);
				enableProtocolEditing(false);
			}
			else { 
				xmlFormDisplay.setBackground(null);
				proTabToolBar.add(xmlValidationPanel);
				enableProtocolEditing(true);
			}
		}
	}
	
	
	public class ToggleProtocolEditingListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			XMLTabbedPane.setSelectedIndex(PROTOCOL_TAB);	// goto protocol editing tab
		}
	}
	
	public class EditExperimentListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			XMLTabbedPane.setSelectedIndex(EXPERIMENT_TAB);	// goto exp editing tab
		}
	}
	
	public class PrintExperimentListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			
			// Can't work out how to export xsl file into .jar
			// xmlModel.transformXmlToHtml();
			
			// use this method for now
			HtmlOutputter.outputHTML(protocolRootNode, false);
		}
	}
	
	public class PrintProtocolListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			HtmlOutputter.outputHTML(protocolRootNode, true);
		}
	}
	
	public class LoadDefaultsListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			copyDefaultValuesToInputFields();
		}
	}
	
	public class newProtocolFileListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			newProtocolFile();
		}
	}
	
	public class addDataFieldListener implements ActionListener {
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
				xmlModel.saveTreeToXmlFile(xmlModel.getCurrentFile());
				JOptionPane.showMessageDialog(XMLFrame, "Experiment saved.");
			}
		}
	}
	
	
	
	public class saveProtocolListener implements ActionListener {
		
		public void actionPerformed(ActionEvent event) {
			
			saveFileAs();	// false: Don't save experiment details
	        
		}
	}
	
	public class SaveFileAsListener implements ActionListener {
		
		public void actionPerformed(ActionEvent event) {
			
			if (xmlModel.isCurrentFileEdited()) return;	
			
			saveFileAs();	//true: saveExpDetails
		}
	}
	
	public class OpenProExpFileFilter extends FileFilter {
		public boolean accept(File file) {
			boolean recognisedFileType = 
				//	allows "MS Windows" to see directories (otherwise only .pro or .exp are visible)
				((file.getName().endsWith("pro")) || (file.getName().endsWith("exp")) || (file.isDirectory()));
			return recognisedFileType;
		}
		public String getDescription() {
			return " .pro and .exp files only";
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
		fc.setFileFilter(new OpenProExpFileFilter());
		fc.setCurrentDirectory(xmlModel.getCurrentFile());

		int returnVal = fc.showSaveDialog(XMLFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            File xmlFile = fc.getSelectedFile();  // this may be a directory! 
            
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
	
	public void copyDefaultValuesToInputFields() {
		
		xmlModel.copyDefaultValuesToInputFields();
	}
	
	
	// called when user changes to Editing Protocol. 
	public void setEditingOfExperimentalValues(boolean enabled) {
		
		if (protocolRootNode == null) return;
		
		Iterator iterator = protocolRootNode.createIterator();
			
			while (iterator.hasNext()) {
				DataFieldNode node = (DataFieldNode)iterator.next();
				node.getDataField().setExperimentalEditing(enabled);
			}
	}
	
	public void refreshFileEdited() {
		
		boolean protocolEdited = xmlModel.isCurrentFileEdited();
			
		// if file edited, the close button looks different
		closeFileButton.setIcon(protocolEdited ? redBallIcon : closeIcon);
	}
	
	// this is called (via selectionChanged) xmlModel after validating xml
	public void updateXmlValidationPanel() {
		// if validation is turned on
		if (xmlModel.getXmlValidation()) {
			
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
		//int divLoc = splitPane.getDividerLocation();
		protocolTab.remove(fieldEditor);
		
		fieldEditor = newDisplay;
			
		protocolTab.add(fieldEditor, BorderLayout.EAST);
		//splitPane.setRightComponent(fieldEditor);
		//splitPane.setDividerLocation(divLoc);
		protocolTab.validate();
		fieldEditor.validate();
		fieldEditor.setVisible(true);	// need to hide, then show to get refresh to work
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
		xmlModel.editCurrentTree(Tree.DUPLICATE_FIELDS);
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
	
}

