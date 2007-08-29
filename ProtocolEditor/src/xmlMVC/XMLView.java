package xmlMVC;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
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
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;


public class XMLView implements XMLUpdateObserver, SelectionObserver {
	
	XMLModel xmlModel;

	DataFieldNode protocolRootNode;
	
	DataFieldNode rootOfImportTree;
	
	// state changes 
	int editingState = EDITING_FIELDS;
	public static final int EDITING_FIELDS = 0;
	public static final int IMPORTING_FIELDS = 1;
	
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
	
	
	JButton saveExperiment;
	JButton addAnInput;
	JButton deleteAnInput;
	JButton promoteField;
	JButton demoteField;
	JButton duplicateField;
	JTextField searchField;
	
	JMenuItem saveExp;
	JMenuItem saveExpAs;
	
	JMenuItem loadDefaultsMenuItem;
	JCheckBoxMenuItem editExperimentMenuItem;
	
	JCheckBoxMenuItem editProtocolMenuItem;
	JMenuItem saveProtocolMenuItem;
	JMenuItem addFieldMenuItem;
	JMenuItem deleteFieldMenuItem;
	JMenuItem moveStepUpMenuItem;
	JMenuItem moveStepDownMenuItem;
	JMenuItem promoteStepMenuItem;
	JMenuItem demoteStepMenuItem;
	JMenuItem duplicateFieldMenuItem;
	JMenuItem importFieldsMenuItem;
	
	JPanel protocolTab;
	
	static final int windowHeight = 460;
    static final int panelWidth = 830;
    
	
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
		XMLFrame = new JFrame("Lab Editor");
		// Build menus
		JMenuBar menuBar = new JMenuBar();
		EmptyBorder menuItemBorder = new EmptyBorder(0,5,0,5);
		
		// File menu
		JMenu fileMenu = new JMenu("File");
		fileMenu.setBorder(menuItemBorder);
		JMenuItem openFile = new JMenuItem("Open File..");
		JMenuItem newBlankProtocolMenuItem = new JMenuItem("New Blank Protocol");
		JMenuItem saveProtocol = new JMenuItem("Save Protocol As...");
		saveExp = new JMenuItem("Save Experiment");
		saveExpAs = new JMenuItem("Save Experiment As...");
		JMenuItem printExp= new JMenuItem("Print Experiment");
		JMenuItem indexFilesMenuItem = new JMenuItem("Index files for searching");
		
		openFile.addActionListener(new openFileListener());
		newBlankProtocolMenuItem.addActionListener(new newProtocolFileListener());
		saveProtocol.addActionListener(new saveProtocolListener());
		saveExp.addActionListener(new SaveCurrentExperimentListener());
		saveExpAs.addActionListener(new SaveExperimentAsListener());
		printExp.addActionListener(new PrintExperimentListener());
		indexFilesMenuItem.addActionListener(new IndexFilesListener());
		
		fileMenu.add(openFile);
		fileMenu.add(newBlankProtocolMenuItem);
		fileMenu.add(saveProtocol);
		fileMenu.add(saveExp);
		fileMenu.add(saveExpAs);
		fileMenu.add(printExp);
		fileMenu.add(indexFilesMenuItem);
		menuBar.add(fileMenu);
		
		// experiment menu
		JMenu experimentMenu = new JMenu("Experiment");
		experimentMenu.setBorder(menuItemBorder);
		editExperimentMenuItem = new JCheckBoxMenuItem("Edit Experiment");
		loadDefaultsMenuItem = new JMenuItem("Load Default Values");
		
		editExperimentMenuItem.addActionListener(new EditExperimentListener());
		loadDefaultsMenuItem.addActionListener(new LoadDefaultsListener());
		experimentMenu.add(editExperimentMenuItem);
		experimentMenu.add(loadDefaultsMenuItem);
		menuBar.add(experimentMenu);
		
		// protocol menu
		JMenu protocolMenu = new JMenu("Protocol");
		protocolMenu.setBorder(menuItemBorder);
		
		editProtocolMenuItem= new JCheckBoxMenuItem("Edit Protocol...");
		saveProtocolMenuItem = new JMenuItem("Save Protocol");
		addFieldMenuItem = new JMenuItem("Add Step");
		deleteFieldMenuItem = new JMenuItem("Delete Step");
		moveStepUpMenuItem = new JMenuItem("Move Step Up");
		moveStepDownMenuItem = new JMenuItem("Move Step Down");
		promoteStepMenuItem = new JMenuItem("Promote Step (indent to left)");
		demoteStepMenuItem = new JMenuItem("Demote Step (indent to right)");
		duplicateFieldMenuItem = new JMenuItem("Duplicate Step");
		importFieldsMenuItem = new JMenuItem("Import Steps");
		
		editProtocolMenuItem.addActionListener(new ToggleProtocolEditingListener());
		saveProtocolMenuItem.addActionListener(new saveProtocolListener());
		addFieldMenuItem.addActionListener(new addDataFieldListener());
		deleteFieldMenuItem.addActionListener(new deleteDataFieldListener());
		moveStepUpMenuItem.addActionListener(new MoveFieldUpListener());
		moveStepDownMenuItem.addActionListener(new MoveFieldDownListener());
		promoteStepMenuItem.addActionListener(new PromoteFieldListener());
		demoteStepMenuItem.addActionListener(new DemoteFieldListener());
		duplicateFieldMenuItem.addActionListener(new DuplicateFieldListener());
		importFieldsMenuItem.addActionListener(new InsertElementsFromFileListener());
		
		protocolMenu.add(editProtocolMenuItem);
		protocolMenu.add(saveProtocolMenuItem);
		protocolMenu.add(addFieldMenuItem);
		protocolMenu.add(deleteFieldMenuItem);
		protocolMenu.add(moveStepUpMenuItem);
		protocolMenu.add(moveStepDownMenuItem);
		protocolMenu.add(promoteStepMenuItem);
		protocolMenu.add(demoteStepMenuItem);
		protocolMenu.add(duplicateFieldMenuItem);
		protocolMenu.add(importFieldsMenuItem);
		menuBar.add(protocolMenu);
		
		
		// search Field and button
		searchField = new JTextField("Search", 20);
		searchField.addActionListener(new SearchButtonListener());	
		JPanel searchPanel = new JPanel();
		searchPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
		searchPanel.setLayout(new BorderLayout());
		searchPanel.add(searchField, BorderLayout.EAST);
		
		Icon searchIcon = ImageFactory.getInstance().getIcon(ImageFactory.SEARCH_ICON);
		JButton searchButton = new JButton(searchIcon);
		searchButton.addActionListener(new SearchButtonListener());
		searchButton.setBorder(new EmptyBorder(3,3,3,3));
		
		menuBar.add(searchPanel);
		menuBar.add(searchButton);
		
		
//		 Make a nice border
		EmptyBorder eb = new EmptyBorder(0,BUTTON_SPACING,BUTTON_SPACING,BUTTON_SPACING);
		EmptyBorder noLeftPadding = new EmptyBorder (0, 2, BUTTON_SPACING, BUTTON_SPACING);
		EmptyBorder noRightPadding = new EmptyBorder (0, BUTTON_SPACING, BUTTON_SPACING, 2);
	
		
		// Build left side with form UI

		
		xmlFormDisplay = new FormDisplay(this);
		backgroundColor = xmlFormDisplay.getBackground();
		
		XMLScrollPane = new JScrollPane(xmlFormDisplay, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		XMLScrollPane.setFocusable(true);
		XMLScrollPane.addMouseListener(new ScrollPaneMouseListener());
		XMLScrollPane.setPreferredSize(new Dimension( panelWidth, windowHeight));

		
		// buttons
		
		Icon newFileIcon = ImageFactory.getInstance().getIcon(ImageFactory.NEW_FILE_ICON);
		JButton newFileButton = new JButton(newFileIcon);
		newFileButton.setToolTipText("New Protocol");
		newFileButton.addActionListener(new newProtocolFileListener());
		newFileButton.setBorder(eb);
		
		Icon openFileIcon = ImageFactory.getInstance().getIcon(ImageFactory.OPEN_FILE_ICON);
		JButton openFileButton = new JButton(openFileIcon);
		openFileButton.setToolTipText("Open Protocol or Experiment");
		openFileButton.addActionListener(new openFileListener());
		openFileButton.setBorder(eb);
		
		Icon saveIcon = ImageFactory.getInstance().getIcon(ImageFactory.SAVE_ICON);
		saveExperiment = new JButton(saveIcon);
		saveExperiment.addActionListener(new SaveCurrentExperimentListener());
		saveExperiment.setToolTipText("Save experimental details");
		saveExperiment.setBorder(eb);
		
		Icon printIcon = ImageFactory.getInstance().getIcon(ImageFactory.PRINT_ICON);
		JButton printButton = new JButton(printIcon);
		printButton.addActionListener(new PrintExperimentListener());
		printButton.setToolTipText("Print the protocol with experimental details");
		printButton.setBorder(eb);
		
		Icon loadDefaultsIcon = ImageFactory.getInstance().getIcon(ImageFactory.LOAD_DEFAULTS_ICON);
		JButton loadDefaults = new JButton(loadDefaultsIcon);
		loadDefaults.setToolTipText("Load default values");
		loadDefaults.addActionListener(new LoadDefaultsListener());
		loadDefaults.setBorder(eb);
		
		
		// Protocol edit buttons
		
		JButton newFileButton2 = new JButton(newFileIcon);
		newFileButton2.setToolTipText("New Protocol");
		newFileButton2.addActionListener(new newProtocolFileListener());
		newFileButton2.setBorder(eb);
		
		JButton openFileButton2 = new JButton(openFileIcon);
		openFileButton2.setToolTipText("Open Protocol or Experiment");
		openFileButton2.addActionListener(new openFileListener());
		openFileButton2.setBorder(eb);
		
		JButton saveProtocolButton = new JButton(saveIcon);
		saveProtocolButton.setToolTipText("Save Protocol");
		saveProtocolButton.setBorder(eb);
		saveProtocolButton.addActionListener(new SaveCurrentProtocolListener());
		
		Icon saveFileAsIcon= ImageFactory.getInstance().getIcon(ImageFactory.SAVE_FILE_AS_ICON);
		JButton saveProtocolAsButton = new JButton(saveFileAsIcon);
		saveProtocolAsButton.setToolTipText("Save Protocol As..");
		saveProtocolAsButton.setBorder(eb);
		saveProtocolAsButton.addActionListener(new saveProtocolListener());
		

		
		Icon addIcon = ImageFactory.getInstance().getIcon(ImageFactory.ADD_ICON);
		addAnInput = new JButton(addIcon);
		addAnInput.setToolTipText("Add a step to the protocol");
		addAnInput.addActionListener(new addDataFieldListener());
		addAnInput.setBorder(eb);
		
		Icon deleteIcon = ImageFactory.getInstance().getIcon(ImageFactory.DELETE_ICON);
		deleteAnInput = new JButton(deleteIcon);
		deleteAnInput.setToolTipText("Delete the highlighted steps from the protocol");
		deleteAnInput.addActionListener(new deleteDataFieldListener());
		deleteAnInput.setBorder(eb);
		
		Icon moveUpIcon = ImageFactory.getInstance().getIcon(ImageFactory.MOVE_UP_ICON);
		JButton moveUpButton = new JButton(moveUpIcon);
		moveUpButton.setToolTipText("Move the step up");
		moveUpButton.addActionListener(new MoveFieldUpListener());
		moveUpButton.setBorder(noRightPadding);
		
		Icon moveDownIcon = ImageFactory.getInstance().getIcon(ImageFactory.MOVE_DOWN_ICON);
		JButton moveDownButton = new JButton(moveDownIcon);
		moveDownButton.setToolTipText("Move the step down");
		moveDownButton.addActionListener(new MoveFieldDownListener());
		moveDownButton.setBorder(noLeftPadding);
		
		Icon promoteIcon = ImageFactory.getInstance().getIcon(ImageFactory.PROMOTE_ICON);
		promoteField = new JButton(promoteIcon);
		promoteField.setToolTipText("Indent steps to left.  To select multiple steps use Shift-Click");
		promoteField.addActionListener(new PromoteFieldListener());
		promoteField.setBorder(noRightPadding);
		
		Icon demoteIcon = ImageFactory.getInstance().getIcon(ImageFactory.DEMOTE_ICON);
		demoteField = new JButton(demoteIcon);
		demoteField.setToolTipText("Indent steps to right");
		demoteField.addActionListener(new DemoteFieldListener());
		demoteField.setBorder(noLeftPadding);
		
		Icon duplicateIcon = ImageFactory.getInstance().getIcon(ImageFactory.DUPLICATE_ICON);
		duplicateField = new JButton(duplicateIcon);
		duplicateField.setToolTipText("Duplicate the selected step. To select multiple steps use Shift-Click");
		duplicateField.addActionListener(new DuplicateFieldListener());
		duplicateField.setBorder(eb);
		
		Icon importElementsIcon = ImageFactory.getInstance().getIcon(ImageFactory.IMPORT_ICON);
		JButton importElemetsButton = new JButton(importElementsIcon);
		importElemetsButton.setToolTipText("Import steps from another document");
		importElemetsButton.addActionListener(new InsertElementsFromFileListener());
		importElemetsButton.setBorder(eb);
		
		Box topLeftPanel = Box.createHorizontalBox();
		topLeftPanel.add(newFileButton);
		topLeftPanel.add(openFileButton);
		topLeftPanel.add(saveExperiment);
		topLeftPanel.add(printButton);
		topLeftPanel.add(loadDefaults);
		topLeftPanel.add(Box.createHorizontalGlue());
		
		Box topRightPanel = Box.createHorizontalBox();
		topRightPanel.add(newFileButton2);
		topRightPanel.add(openFileButton2);
		topRightPanel.add(saveProtocolButton);
		topRightPanel.add(saveProtocolAsButton);
		topRightPanel.add(addAnInput);
		topRightPanel.add(deleteAnInput);
		topRightPanel.add(moveUpButton);
		topRightPanel.add(moveDownButton);
		topRightPanel.add(promoteField);
		topRightPanel.add(demoteField);
		topRightPanel.add(duplicateField);
		topRightPanel.add(importElemetsButton);
		topRightPanel.add(Box.createHorizontalGlue());
		
		fieldEditor = new FieldEditor();
		//splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, XMLScrollPane, fieldEditor);
		//splitPane.setResizeWeight(1.0);
		
		protocolTab = new JPanel();
		protocolTab.setLayout(new BorderLayout());
		protocolTab.add(topRightPanel, BorderLayout.NORTH);
		protocolTab.add(XMLScrollPane, BorderLayout.CENTER);
		protocolTab.add(fieldEditor, BorderLayout.EAST);
		
		Box experimentTab = Box.createVerticalBox();
		experimentTab.add(topLeftPanel);
		experimentTab.add(XMLScrollPane);
		
		XMLTabbedPane = new JTabbedPane();
		XMLTabbedPane.addTab("Edit Experiment", experimentTab);;
		XMLTabbedPane.addTab("Edit Protocol", protocolTab);
		XMLTabbedPane.addChangeListener(new TabChangeListener());
		
		XMLUIPanel = new JSplitPane();
		XMLUIPanel.setPreferredSize(new Dimension( panelWidth, windowHeight ));
		XMLUIPanel.setDividerSize(0);
		XMLUIPanel.setDividerLocation(1.0);
		XMLUIPanel.setLeftComponent(XMLTabbedPane);
		XMLUIPanel.setRightComponent(null);
		
		enableProtocolEditing(false);	// turn off controls
		
		XMLFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		XMLFrame.setJMenuBar(menuBar);
		XMLFrame.getContentPane().add("Center", XMLUIPanel);
		XMLFrame.pack();
		XMLFrame.setLocation(200, 100);
		XMLFrame.setVisible(true);
		
		// an intro splash-screen to get users started
//		Custom button text
		Icon bigProtocolIcon = ImageFactory.getInstance().getIcon(ImageFactory.BIG_PROTOCOL_ICON);
		Object[] options = {"Start blank protocol",
		                    "Open existing file",
		                    "Open demo protocol"};
		int n = JOptionPane.showOptionDialog(XMLFrame, "<html>Welcome to the Protocol Editor. <br>"
		    + "Please choose an otpion to get you started.</html>", "Welcome",
		    JOptionPane.YES_NO_CANCEL_OPTION,
		    JOptionPane.QUESTION_MESSAGE,
		    bigProtocolIcon,
		    options,
		    options[0]);
		
		if (n == 0) newProtocolFile();
		else if (n == 1) openFile();
		else if (n == 2) {
			xmlModel.openDemoProtocolFile();
			XMLTabbedPane.setSelectedIndex(1); 	// goto edit protocol tab
		}
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
		
		editExperimentMenuItem.setEnabled(enabled);
		editExperimentMenuItem.setSelected(!enabled);
		
		setEditingOfExperimentalValues(!enabled);
	}

	
	// called by NotifyXMLObservers in XMLModel. 
	public void xmlUpdated() {
		protocolRootNode = xmlModel.getRootNode();
		xmlFormDisplay.refreshForm();		// refresh the form
		refreshProtocolEdited();
		
		if (editingState == EDITING_FIELDS) {  // ie. not showing import tree
			updateFieldEditor();
		}
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
			xmlModel.deleteDataFields(false);
			setProtocolEdited(true);
		}
		if (result == 1) {	// shift children to be siblings, then delete
			xmlModel.deleteDataFields(true);
			setProtocolEdited(true);
		}
	}
	
	public void promoteDataFields() {
		xmlModel.promoteDataFields();
		setProtocolEdited(true);
	}
	
	public void demoteDataFields() {
		xmlModel.demoteDataFields();
		setProtocolEdited(true);
	}
	
	public void moveFieldsUp() {
		// if the highlighted fields have a preceeding sister, move it below the highlighted fields
		xmlModel.moveFieldsUp();
		}
	public void moveFieldsDown() {
		xmlModel.moveFieldsDown();
	}
	
	public void addDataField() {
		// get selected Fields and add dataField after last seleted one		
		// create method returns the new dataField
		xmlModel.addDataField();

		setProtocolEdited(true);
		
	}
	
	
	// open a blank protocol (after checking you want to save!)
	public void newProtocolFile() {
		if (xmlModel.isCurrentProtocolEdited()) {
			int result = JOptionPane.showConfirmDialog
				(XMLUIPanel, "Save the current protocol before opening a new one?");
			if (result == JOptionPane.YES_OPTION) {
				saveFileAs(false);
			} else if (result == JOptionPane.CANCEL_OPTION) {
				return;
			}
		}
		xmlModel.openBlankProtocolFile();
		XMLTabbedPane.setSelectedIndex(1); 	// protocol Editing tab
		setProtocolEdited(false);	// at least until user starts editing
	}
	
	
	//open a file
	public void openFile() {
		
		// check whether you want to save edited file
		if (xmlModel.isCurrentProtocolEdited()) {
			int result = JOptionPane.showConfirmDialog
				(XMLUIPanel, "Save the current protocol before opening a new one?");
			if (result == JOptionPane.YES_OPTION) {
				saveFileAs(false);
			} else if (result == JOptionPane.CANCEL_OPTION) {
				return;
			}
		}
		
		// Create a file chooser
		final JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new openFileFilter());
		
		fc.setCurrentDirectory(xmlModel.getCurrentFile());
		int returnVal = fc.showOpenDialog(XMLFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            File xmlFile = fc.getSelectedFile();
            openThisFile(xmlFile);
            
            setProtocolEdited(false);
            
            if (xmlFile.getName().endsWith(".pro")) {
            	Object[] options = {"Yes",
                "No"};
            	int n = JOptionPane.showOptionDialog(XMLFrame,
            		    "Would you like to load default values?",
            		    "Load Default Values?",
            		    JOptionPane.YES_NO_OPTION,
            		    JOptionPane.QUESTION_MESSAGE,
            		    null,     //don't use a custom Icon
            		    options,  //the titles of buttons
            		    options[0]); //default button title
            	if (n == 0) {
            		copyDefaultValuesToInputFields();
            	}
            }
		}
	}
	
	public void openFile(File file) {
		
//		 check whether you want to save edited file
		if (xmlModel.isCurrentProtocolEdited()) {
			int result = JOptionPane.showConfirmDialog
				(XMLUIPanel, "Save the current protocol before opening a new one?");
			if (result == JOptionPane.YES_OPTION) {
				saveFileAs(false);
			} else if (result == JOptionPane.CANCEL_OPTION) {
				return;
			}
		}
		 openThisFile(file);
		 
	}
	
	public void openThisFile(File file) {
		 xmlModel.openXMLFile(file);
		 setProtocolEdited(false);
	}
	
	public class SearchButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			searchFiles();
		}
	}
	
	public void searchFiles() {
		JPanel searchPanel = new SearchPanel(searchField.getText(), this);
		updateSearchPanel(searchPanel);
		XMLTabbedPane.setSelectedIndex(EXPERIMENT_TAB);
		
	}
	
	public class IndexFilesListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			
//			 Create a file chooser
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
				enableProtocolEditing(false);
			}
			else { 
				xmlFormDisplay.setBackground(null);
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
			HtmlOutputter.outputHTML(protocolRootNode);
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
	
	
	public class SaveCurrentExperimentListener implements ActionListener {
		
		public void actionPerformed(ActionEvent event) {
			
			// check that protocol is saved
			if (xmlModel.isCurrentProtocolEdited()) return;	
			
			// if the current file is an .exp file
			if (xmlModel.getCurrentFile().getName().endsWith(".exp")) {
				xmlModel.saveTreeToXmlFile(xmlModel.getCurrentFile(), true);
				JOptionPane.showMessageDialog(XMLFrame, "Experiment saved.");
			}
			else
				saveFileAs(true);
		}
	}
	
	public class SaveCurrentProtocolListener implements ActionListener {
		
		public void actionPerformed(ActionEvent event) {
			
			// check that protocol has no exp children
			if (xmlModel.hasProtocolGotExpChildren()) {
			    JOptionPane.showMessageDialog
			    	(XMLFrame, "Protocol already has .exp children. Can't over-write.");
			    return;	
			}
			
			// if the current file is an .exp file, forget it
			if (xmlModel.getCurrentFile().getName().endsWith(".exp")) {
				JOptionPane.showMessageDialog
	    			(XMLFrame, "Protocol already has .exp children. Can't over-write.");
				return;
			}
			
			else if (xmlModel.getCurrentFile().getName().endsWith(".pro")) {
				xmlModel.saveTreeToXmlFile(xmlModel.getCurrentFile(), false);
				JOptionPane.showMessageDialog
    				(XMLFrame, "Protocol saved.");
				refreshProtocolEdited();
			}
			
			else saveFileAs(false);
		}
	}
	
	public class saveProtocolListener implements ActionListener {
		
		public void actionPerformed(ActionEvent event) {
			
			saveFileAs(false);	// false: Don't save experiment details
	        
		}
	}
	
	public class SaveExperimentAsListener implements ActionListener {
		
		public void actionPerformed(ActionEvent event) {
			
			if (xmlModel.isCurrentProtocolEdited()) return;	
			
			saveFileAs(true);	//true: saveExpDetails
		}
	}
	
	public class openFileFilter extends FileFilter {
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
	
	
	public void saveFileAs(boolean saveExpDetails) {
		// if saveExpDetails is true, saving .exp, otherwise saving .pro
		
		final JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new openFileFilter());
		fc.setCurrentDirectory(xmlModel.getCurrentFile());
		if (!saveExpDetails)	// if saving protocol, can't choose file to overwrite
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fc.showSaveDialog(XMLFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            File xmlFile = fc.getSelectedFile();  // this may be a directory! 
            
            if (xmlFile.isDirectory()) {
                JOptionPane.showMessageDialog(XMLFrame, "Please choose a file name (not a directory)");
                // try again! 
                saveFileAs(saveExpDetails);
                return;
            }

            // first, make sure the filename ends .pro or .exp
            String filePathAndName = xmlFile.getAbsolutePath();
            if (!saveExpDetails) { // filename must end .pro
            	if (!(filePathAndName.endsWith(".pro"))) {	// if not..
            		filePathAndName = filePathAndName + ".pro";
            		xmlFile = new File(filePathAndName);
            	}
            }
            else {	// saving experiment, filename must end .exp
            	if (!(filePathAndName.endsWith(".exp"))) {	// if not..
            		filePathAndName = filePathAndName + ".exp";
            		xmlFile = new File(filePathAndName);
            	}
            }
            
            
            // now check if the file exists. If so, take appropriate action
            if (xmlFile.exists()) {
            	if (saveExpDetails)	{	// saving exp. Check if OK to overwrite
            		int result = JOptionPane.showConfirmDialog(XMLUIPanel, "File exists. Overwrite it?");
            		if (!(result == JOptionPane.YES_OPTION)) {	// if not yes, then forget it!
            			return;
            		}
        		}
            	else {		// saving protocol. Can't overwrite
            		JOptionPane.showMessageDialog(XMLUIPanel,
            			    "Can't overwrite an existing protocol",
            			    "Save error",JOptionPane.ERROR_MESSAGE);
            		return;
            	}
            }
            
            xmlModel.saveTreeToXmlFile(xmlFile, saveExpDetails);
            
            refreshProtocolEdited();
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

	
	public void setProtocolEdited(boolean protocolEdited) {
		xmlModel.setProtocolEdited(protocolEdited);
		refreshProtocolEdited();
	}
	
	public void refreshProtocolEdited() {
		
		boolean protocolEdited = xmlModel.isCurrentProtocolEdited();
		
		if (protocolEdited) {
			saveExperiment.setToolTipText("You must save the protocol before saving the experimental details");
			saveExp.setToolTipText("You must save the protocol before saving the experimental details");
			saveExpAs.setToolTipText("You must save the protocol before saving the experimental details");
		}
		else {
			saveExperiment.setToolTipText("Save experimental details");
			saveExp.setToolTipText("Save experimental details");
			saveExpAs.setToolTipText("Save experimental details");
		}
			
		// if protocol edited, saveExp button and menus are disabled 
		saveExperiment.setEnabled(!protocolEdited);
		saveExp.setEnabled(!protocolEdited);
		saveExpAs.setEnabled(!protocolEdited);
	}
	
	// selection observer method, fired when tree changes selection
	public void selectionChanged() {
		if (editingState == EDITING_FIELDS) {
			updateFieldEditor();
			refreshProtocolEdited();
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
		fc.setFileFilter(new openFileFilter());
		
		fc.setCurrentDirectory(xmlModel.getCurrentFile());
		int returnVal = fc.showOpenDialog(XMLFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
           File xmlFile = fc.getSelectedFile();	
           
           Tree importTree = xmlModel.getTreeFromNewFile(xmlFile); 
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
		xmlModel.duplicateDataFields();
			
		// refresh UI after adding all (not after each time)
		xmlModel.notifyXMLObservers();
		setProtocolEdited(true);
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

