/*
 * org.openmicroscopy.shoola.agents.fsimporter.chooser.ImportDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.fsimporter.chooser;




//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

//Third-party libraries
import info.clearthought.layout.TableLayout;
import org.jdesktop.swingx.JXTaskPane;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.fsimporter.IconManager;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.util.SelectionWizard;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;

/** 
 * Dialog used to select the files to import.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ImportDialog extends JDialog
	implements ActionListener, ChangeListener, PropertyChangeListener
{

	/** Bound property indicating to load the tags. */
	public static final String	LOAD_TAGS_PROPERTY = "loadTags";
	
	/** Bound property indicating that the cancel button is pressed. */
	public static final String	CANCEL_SELECTION_PROPERTY = "cancelSelection";
	
	/** Bound property indicating to import the selected files. */
	public static final String	IMPORT_PROPERTY = "import";

	/** Action id indicating to import the selected files. */
	public static final int		IMPORT = 0;
	
	/** Action id indicating to close the dialog. */
	public static final int		CANCEL = 1;
	
	/** Action id indicating to refresh the file view. */
	private static final int	REFRESH = 2;
	
	/** Action id indicating to reset the names. */
	private static final int	RESET = 3;
	
	/** Action id indicating to apply the partial names to all. */
	private static final int	APPLY_TO_ALL = 4;
	
	/** Action id indicating to add tags to the file. */
	private static final int	TAG = 5;
	
	/** Action id indicating to override the name or not. */
	private static final int	OVERRIDE = 6;
	
	/** The title of the dialog. */
	private static final String TITLE = "Import";
	
	/** The message to display in the header. */
	private static final String MESSAGE = "Selects the files or directories " +
			"to import";
	
	/** The message to display in the header. */
	private static final String MESSAGE_PLATE = "Selects the plates to import";
	
	/** The message to display in the header. */
	private static final String END = ".";
	
	/** Text of the sub-message. */
	private static final String SUB_MESSAGE = "The name of the file will be, " +
			"by default, the absolute path. \n You can either edit the name " +
			"or set the number of directories before the file's name.";
	
	/** Warning when de-selecting the name overriding option. */
	private static final List<String> WARNING;
	
	
	/** The length of a column. */
	private static final int		COLUMN_WIDTH = 200;
	
	static {
		WARNING = new ArrayList<String>();
		WARNING.add("NOTE: Some file formats do not include the file name " +
				"in their metadata, ");
		WARNING.add("and disabling this option may result in files being " +
				"imported without a");
		WARNING.add("reference to their file name e.g. " +
				"'myfile.lsm [image001]'");
		WARNING.add("would show up as 'image001' with this optioned " +
				"turned off.");
	}
	
	/** The approval option the user chose. */
	private int					option;

	/** The table hosting the file to import. */
	private FileSelectionTable  table;
	
	/** The file chooser. */
	private JFileChooser	    chooser;
	
	/** Button to close the dialog. */
	private JButton				cancelButton;
	
	/** Button to import the files. */
	private JButton				importButton;
	
	/** Button to import the files. */
	private JButton				refreshButton;
	
	/** 
	 * Resets the name of all files to either the full path
	 * or the partial name if selected. 
	 */
	private JButton				resetButton;
	
	/** Apply the partial name to all files. */
	private JButton				applyToAllButton;
	
	/** Box indicating to archive or not the files. */
	private JCheckBox			archived;
	
	/** Indicates to use a partial name. */
	private JRadioButton		partialName;
	
	/** Indicates to use a full name. */
	private JRadioButton		fullName;
	
	/** Button indicating to override the name if selected. */
	private JCheckBox			overrideName;
	
	/** Text field indicating how many folders to include. */
	private NumericalTextField	numberOfFolders;
	
	/** The collection of supported filters. */
	private List<FileFilter> 	filters;
	
	/** The title panel of the window. */
	private TitlePanel			titlePane;
	
	/** Component indicating to use the folder as a tag. */
	private JCheckBox			folderAsTag;
	
	/** Button to bring up the tags wizard. */
	private JButton						tagButton;
	
	/** Button indicating to use the folder as a container. */
	private JCheckBox					folderAsContainer;
		
	/** The fields hosting the pixels size. First
	 * is for the size along the X-axis, then Y-axis, finally Z-axis
	 */
	private List<NumericalTextField>	pixelsSize;
	
	/** Components hosting the tags. */
	private JPanel						tagsPane;
	
	/** Map hosting the tags. */
	private Map<JButton, TagAnnotationData> tagsMap;
	
	/** The action listener used to handle tag selection. */
	private ActionListener				listener;
	
	private DataObject			container;
	
	/**
	 * Handles the selection of tags.
	 * 
	 * @param tags The selected tags.
	 */
	private void handleTagsSelection(Collection tags)
	{
		Collection<TagAnnotationData> set = tagsMap.values();
		Map<String, TagAnnotationData> 
			newTags = new HashMap<String, TagAnnotationData>();
		TagAnnotationData tag;
		Iterator i = set.iterator();
		while (i.hasNext()) {
			tag = (TagAnnotationData) i.next();
			if (tag.getId() < 0)
				newTags.put(tag.getTagValue(), tag);
		}
		List<TagAnnotationData> toKeep = new ArrayList<TagAnnotationData>();
		i = tags.iterator();
		while (i.hasNext()) {
			tag = (TagAnnotationData) i.next();
			if (tag.getId() < 0) {
				if (!newTags.containsKey(tag.getTagValue())) {
					toKeep.add(tag);
				}
			} else toKeep.add(tag);
		}
		toKeep.addAll(newTags.values());
		
		//layout the tags
		tagsMap.clear();
		tagsPane.removeAll();
		i = toKeep.iterator();
		IconManager icons = IconManager.getInstance();
		JPanel entry;
		JPanel p = initRow();
		int width = 0;
		while (i.hasNext()) {
			tag = (TagAnnotationData) i.next();
			entry = buildTagEntry(tag, icons.getIcon(IconManager.MINUS_11));
			if (width+entry.getPreferredSize().width >= COLUMN_WIDTH)
		    {
		    	tagsPane.add(p);
		    	p = initRow();
				width = 0;
		    } else {
		    	width += entry.getPreferredSize().width;
		    	width += 2;
		    }
			p.add(entry);
		}
		if (p.getComponentCount() > 0) tagsPane.add(p);
		tagsPane.validate();
		tagsPane.repaint();
	}
	
	/**
	 * Creates a row.
	 * 
	 * @return See above.
	 */
	private JPanel initRow()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		return p;
	}
	
	/** 
	 * Builds and lays out a tag.
	 * 
	 * @param tag The tag to display.
	 * @param icon The icon used to remove the tag from the display.
	 * @return See above.
	 */
	private JPanel buildTagEntry(TagAnnotationData tag, Icon icon)
	{
		JButton b = new JButton(icon);
		UIUtilities.unifiedButtonLookAndFeel(b);
		//add listener
		b.addActionListener(listener);
		tagsMap.put(b, tag);
		JPanel p = new JPanel();
		JLabel l = new JLabel();
		l.setText(tag.getTagValue());
		p.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		p.add(l);
		p.add(b);
		return p;
	}
	
	
	/**
	 * Shows the selection wizard.
	 * 
	 * @param type			The type of objects to handle.
	 * @param available 	The available objects.
	 * @param selected  	The selected objects.
	 * @param addCreation	Pass <code>true</code> to add a component
	 * 						allowing creation of object of the passed type,
	 * 						<code>false</code> otherwise.
	 */
	private void showSelectionWizard(Class type, Collection available, 
									Collection selected, boolean addCreation)
	{
		IconManager icons = IconManager.getInstance();
		Registry reg = ImporterAgent.getRegistry();
		String title = "";
		String text = "";
		Icon icon = null;
		if (TagAnnotationData.class.equals(type)) {
			title = "Tags Selection";
			text = "Select the Tags to add or remove, \nor Create new Tags";
			icon = icons.getIcon(IconManager.TAGS_48);
		} 
		long userID = MetadataViewerAgent.getUserDetails().getId();
		SelectionWizard wizard = new SelectionWizard(
				reg.getTaskBar().getFrame(), available, selected, type,
				addCreation, userID);
		wizard.setAcceptButtonText("Save");
		wizard.setTitle(title, text, icon);
		wizard.addPropertyChangeListener(this);
		UIUtilities.centerAndShow(wizard);
	}
	
	/** Sets the properties of the dialog. */
	private void setProperties()
	{
		setTitle(TITLE);
        setModal(true);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
		
			/** 
			 * Cancels the selection.
			 * @see WindowAdapter#windowClosing(WindowEvent)
			 */
			public void windowClosing(WindowEvent e) {
				cancelSelection();
			}
		
		});
	}

	/** 
	 * Initializes the components composing the display. 
	 * 
	 * @param container The container to import the data into.
	 * @param type One of the type constants.
	 */
	private void initComponents(DataObject container, int type)
	{
		listener = new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				Object src = e.getSource();
				if (src instanceof JButton) {
					TagAnnotationData tag = tagsMap.get(src);
					if (tag != null) {
						tagsMap.remove(src);
						handleTagsSelection(tagsMap.values());
					}
				}
			}
		};
		numberOfFolders = new NumericalTextField();
		numberOfFolders.setMinimum(0);
		numberOfFolders.addPropertyChangeListener(this);
		numberOfFolders.setText("0");
		numberOfFolders.setColumns(3);
		numberOfFolders.setEnabled(false);
		
		tagsMap = new LinkedHashMap<JButton, TagAnnotationData>();
		if (container == null || container instanceof ProjectData) {
			switch (type) {
				case Importer.PROJECT_TYPE:
					folderAsContainer = new JCheckBox("Folder as Dataset");
					folderAsContainer.setToolTipText("Create a Dataset " +
							"using the name of the folder");
					folderAsContainer.setEnabled(false);
					numberOfFolders.setMinimum(1);
					numberOfFolders.setText("1");
					if (container == null)
						this.container = new DatasetData();
					break;
				case Importer.SCREEN_TYPE:
					folderAsContainer = new JCheckBox("Folder as Screen");
					folderAsContainer.setToolTipText("Create a Screen " +
					"using the name of the folder");
					this.container = new ScreenData();
					break;
			}
			if (folderAsContainer != null) {
				folderAsContainer.setSelected(true);
				folderAsContainer.addChangeListener(this);
			}
				
		}
		/*
		if (container != null) {
			folderAsTag = new JCheckBox("Folder as Tag");
			folderAsTag.setToolTipText("Create a Tag using the name " +
					"of the folder.");
		}
		*/
		IconManager icons = IconManager.getInstance();
		tagButton = new JButton(icons.getIcon(IconManager.PLUS_12));
		UIUtilities.unifiedButtonLookAndFeel(tagButton);
		tagButton.addActionListener(this);
		tagButton.setActionCommand(""+TAG);
		tagButton.setToolTipText("Add Tags.");
		tagsPane = new JPanel();
		tagsPane.setLayout(new BoxLayout(tagsPane, BoxLayout.Y_AXIS));
		
		archived = new JCheckBox();
		archived.setText("Archived files");
		Boolean b = 
			(Boolean) ImporterAgent.getRegistry().lookup("/options/Archived");
		archived.setSelected(b);
		b = (Boolean) ImporterAgent.getRegistry().lookup(
					"/options/ArchivedAvailable");
		archived.setVisible(b);

		overrideName = new JCheckBox("Override default naming " +
				"using the File Name.");
		overrideName.setToolTipText(UIUtilities.formatToolTipText(WARNING));
		overrideName.setSelected(true);
		overrideName.addChangeListener(this);
		ButtonGroup group = new ButtonGroup();
		fullName = new JRadioButton("Full Path+File name");
		fullName.setSelected(true);
		group.add(fullName);
		partialName = new JRadioButton();
		partialName.addChangeListener(this);
		partialName.setText("Directories+File name");
		group.add(partialName);

		chooser = new JFileChooser();
		File f = UIUtilities.getDefaultFolder();
		if (f != null) chooser.setCurrentDirectory(f);
		chooser.addPropertyChangeListener(this);
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		//chooser.setAcceptAllFileFilterUsed(false);
		chooser.setControlButtonsAreShown(false);
		chooser.setApproveButtonText("Import");
		chooser.setApproveButtonToolTipText("Import the selected files " +
				"or directories");
		
		if (filters != null) {
			Iterator<FileFilter> i = filters.iterator();
			while (i.hasNext()) {
				chooser.setFileFilter(i.next());
			}
		}
		chooser.setAcceptAllFileFilterUsed(true);
		
		table = new FileSelectionTable(this);
		table.addPropertyChangeListener(this);
		cancelButton = new JButton("Close");
		cancelButton.setToolTipText("Close the dialog and do not import.");
		cancelButton.setActionCommand(""+CANCEL);
		cancelButton.addActionListener(this);
		importButton = new JButton("Import");
		importButton.setToolTipText("Import the selected files or" +
				" directories.");
		importButton.setActionCommand(""+IMPORT);
		importButton.addActionListener(this);
		importButton.setEnabled(false);
		refreshButton = new JButton("Refresh");
		refreshButton.setToolTipText("Reloads the files view.");
		refreshButton.setActionCommand(""+REFRESH);
		refreshButton.addActionListener(this);
		resetButton = new JButton("Reset");
		resetButton.setToolTipText("Resets the name of all files to either " +
				"the full path or the partial name if selected.");
		resetButton.setActionCommand(""+RESET);
		resetButton.addActionListener(this);
		applyToAllButton = new JButton("Apply Partial Name");
		applyToAllButton.setToolTipText("Apply the partial name to " +
				"all files in the queue.");
		applyToAllButton.setActionCommand(""+APPLY_TO_ALL);
		applyToAllButton.addActionListener(this);
		applyToAllButton.setEnabled(false);
		//getRootPane().setDefaultButton(cancelButton);
		
		pixelsSize = new ArrayList<NumericalTextField>();
		NumericalTextField field;
		for (int i = 0; i < 3; i++) {
			field = new NumericalTextField();
			field.setNumberType(Double.class);
			field.setColumns(2);
			pixelsSize.add(field);
		}
	}
	
	/** 
	 * Builds and lays out the tool bar. 
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBarRight()
	{
		JPanel bar = new JPanel();
		bar.setLayout(new FlowLayout(FlowLayout.RIGHT));
		//bar.add(applyToAllButton);
		//bar.add(Box.createHorizontalStrut(5));
		bar.add(resetButton);
		bar.add(Box.createHorizontalStrut(20));
		bar.add(cancelButton);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(importButton);
		bar.add(Box.createHorizontalStrut(10));
		return bar;
	}
	
	/** 
	 * Builds and lays out the tool bar. 
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBarLeft()
	{
		JPanel bar = new JPanel();
		bar.setLayout(new FlowLayout(FlowLayout.LEFT));
		bar.add(refreshButton);
		return bar;
	}
	
	/**
	 * Returns the text corresponding to the passed container.
	 * 
	 * @param container Container where to import the image.
	 * @return See above.
	 */
	private String getContainerText(Object container)
	{
		if (container instanceof DatasetData) {
			return MESSAGE+" into dataset: "+
					((DatasetData) container).getName()+END;
		} else if (container instanceof ScreenData) {
			return MESSAGE_PLATE+" into screen: "+
			((ScreenData) container).getName()+END;
		}
		return MESSAGE+END;
	}
	
	/**
	 * Builds and lays out the components.
	 * 
	 * @return See above
	 */
	private JPanel buildPathComponent()
	{
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel l = new JLabel();
		l.setText("Depth: ");
		p.add(l);
		p.add(Box.createHorizontalStrut(5));
		p.add(numberOfFolders);
		l = new JLabel();
		l.setText("Directories");
		p.add(l);
		return p;
	}
	
	/**
	 * Builds and lays out the component displaying the options for the 
	 * metadata.
	 * 
	 * @return See above.
	 */
	private JXTaskPane buildMetadataComponent()
	{
		JXTaskPane pane = new JXTaskPane();
		pane.setCollapsed(true);
		pane.setTitle("Metadata Defaults");
		pane.add(buildPixelSizeComponent());
		return pane;
	}
	
	/**
	 * Builds and lays out the pixels size options.
	 * 
	 * @return See above.
	 */
	private JPanel buildPixelSizeComponent()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createTitledBorder("Pixels Size Defaults"));
		JLabel l = new JLabel();
		l.setText("Used if no values included in the file:");
		p.add(UIUtilities.buildComponentPanel(l));
		JPanel row = new JPanel();
		row.setLayout(new FlowLayout(FlowLayout.LEFT));
		l = new JLabel();
		l.setText("X: ");
		row.add(l);
		row.add(pixelsSize.get(0));
		l = new JLabel();
		l.setText("Y: ");
		row.add(l);
		row.add(pixelsSize.get(1));
		l = new JLabel();
		l.setText("Z: ");
		row.add(l);
		row.add(pixelsSize.get(2));
		p.add(row);
		return UIUtilities.buildComponentPanel(p);
	}
	
	/**
	 * Builds and lays out the components displaying the naming options.
	 * 
	 * @return See above.
	 */
	private JComponent buildNamingComponent()
	{
		JPanel p = new JPanel();
		double[][] size = {{15, TableLayout.PREFERRED}, 
				{TableLayout.PREFERRED, TableLayout.PREFERRED, 
			TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}};
		p.setLayout(new TableLayout(size));
		if (folderAsContainer != null) {
			p.add(folderAsContainer, "0, 0, 1, 0");
			p.add(buildPathComponent(), "1, 1");
			p.add(overrideName, "0, 2, 1, 2");
		}
		
		p.add(UIUtilities.buildComponentPanel(archived), "0, 3, 1, 3");
		p.add(buildLocationComponent(), "0, 4, 1, 4");
		return UIUtilities.buildComponentPanel(p);
		/*
		p.add(overrideName, "0, 0, 1, 0");
		p.add(UIUtilities.buildComponentPanel(fullName), "1, 1");
		p.add(buildPathComponent(), "1, 2");
		p.add(UIUtilities.buildComponentPanel(archived), "0, 3, 1, 3");
		JXTaskPane pane = new JXTaskPane();
		pane.setTitle("Naming");
		pane.add(p);
		return pane;
		*/
	}
	
	/**
	 * Builds the component indicating where to import the files.
	 * 
	 * @return See above.
	 */
	private JPanel buildLocationComponent()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		/*
		if (folderAsContainer != null) 
			p.add(UIUtilities.buildComponentPanel(folderAsContainer));
		if (folderAsTag != null)
			p.add(UIUtilities.buildComponentPanel(folderAsTag));
		*/
		//p.add(buildNamingComponent());
		JLabel l = new JLabel();
		l.setText("Add Tag");
		JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tagPanel.add(l);
		tagPanel.add(tagButton);
		l = new JLabel();
		l.setText(": ");
		tagPanel.add(l);
		tagPanel.add(tagsPane);
		
		p.add(tagPanel);
		return UIUtilities.buildComponentPanel(p);
	}
	
	/**
	 * Builds and lays out the import options available.
	 * 
	 * @param container Container where to import the image.
	 * @return See above.
	 */
	private JPanel buildOptionsPane()
	{
		//Lays out the options
		JPanel options = new JPanel();
		double[][] size = {{TableLayout.PREFERRED}, 
				{TableLayout.PREFERRED, TableLayout.PREFERRED, 
			TableLayout.PREFERRED}};
		options.setLayout(new TableLayout(size));
		//options.add(buildLocationComponent(), "0, 0");
		options.add(buildNamingComponent(), "0, 1");
		options.add(buildMetadataComponent(), "0, 2");
		return options;
	}
	
	/** 
	 * Builds and lays out the UI. 
	 * 
	 * @param container The container where to import the files or 
	 * 					<code>null</code>.
	 */
	private void buildGUI(Object container)
	{
		Container c = getContentPane();
		c.setLayout(new BorderLayout(0, 0));
		IconManager icons = IconManager.getInstance();
		titlePane = new TitlePanel(TITLE, getContainerText(container), 
				icons.getIcon(IconManager.IMPORT_48));
		titlePane.setSubtitle(SUB_MESSAGE);
		c.add(titlePane, BorderLayout.NORTH);
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add("Files to import", table);
		tabbedPane.add("Options", buildOptionsPane());
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(table.buildControls());
		p.add(tabbedPane);
		JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chooser, 
				p);
		c.add(pane, BorderLayout.CENTER);
		
		
		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
		
		//Lays out the buttons.
		JPanel bar = new JPanel();
		bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
		bar.add(buildToolBarLeft());
		bar.add(buildToolBarRight());
		controls.add(new JSeparator());
		controls.add(bar);
		
		c.add(controls, BorderLayout.SOUTH);
		if (JDialog.isDefaultLookAndFeelDecorated()) {
			boolean supportsWindowDecorations = 
				UIManager.getLookAndFeel().getSupportsWindowDecorations();
			if (supportsWindowDecorations)
				getRootPane().setWindowDecorationStyle(
						JRootPane.FILE_CHOOSER_DIALOG);
		}
	}
	
    /** Closes the window and disposes. */
    private void cancelSelection()
    {
    	firePropertyChange(CANCEL_SELECTION_PROPERTY, Boolean.valueOf(false), 
    			Boolean.valueOf(true));
    	option = CANCEL;
    	setVisible(false);
    	dispose();
    }
    
    /** Imports the selected files. */
    private void importFiles()
    {
    	option = IMPORT;
    	//Set the current directory as the defaults
    	UIUtilities.setDefaultFolder(
    			chooser.getCurrentDirectory().toString());

    	Map<File, Boolean> files = null;//table.getFilesToImport();
    	ImportableObject object = new ImportableObject(files,
    			archived.isSelected(), overrideName.isSelected());
    	
    	if (partialName.isSelected()) {
    		Integer number = (Integer) numberOfFolders.getValueAsNumber();
        	if (number != null && number >= 0) object.setDepth(number);
    	} else { //fullName
    		
    	}
    	//List of tags
    	Collection tags = tagsMap.values();
    	if (folderAsTag != null && folderAsTag.isSelected()) {
    		List<TagAnnotationData> 
    		folders = new ArrayList<TagAnnotationData>();
    		Set<File> keys = files.keySet();
    		Iterator<File> j = keys.iterator();
    		File f;
    		while (j.hasNext()) {
				f = j.next();
				if (f.isDirectory()) 
					folders.add(new TagAnnotationData(f.getName()));
			}
    		if (folders.size() > 0)
    			tags.addAll(folders);
    	}
    	object.setTags(tags);
    	long id = container.getId();
    	if (id >= 0 && container.isLoaded()) {
    		object.setContainer(container);
    	} else {
    		object.setFolderAsContainer(container.getClass());
    	}
    	NumericalTextField f;
    	Iterator<NumericalTextField> i = pixelsSize.iterator();
    	Number n;
    	double[] size = new double[3];
    	int index = 0;
    	int count = 0;
    	while (i.hasNext()) {
			f = i.next();
			n = f.getValueAsNumber();
			if (n != null) {
				count++;
				size[index] = n.doubleValue();
			} else size[index] = 1;
			index++;
		}
    	if (count > 0) object.setPixelsSize(size);	
    	firePropertyChange(IMPORT_PROPERTY, null, object);
    	setVisible(false);
    	dispose();
    }
    
	/**
	 * Returns <code>true</code> if the file can be imported, 
	 * <code>false</code> otherwise.
	 * 
	 * @param f The file to check.
	 * @return See above.
	 */
	private boolean isFileImportable(File f)
	{
		/*
		Iterator<FileFilter> i = filters.iterator();
		FileFilter filter;
		while (i.hasNext()) {
			filter = i.next();
			if (filter.accept(f)) return true;
		}
		return false;
		*/
		return true;
	}
	
	/**
	 * Checks if the file can be added to the passed list.
	 * 
	 * @param f The file to handle.
	 * @param l The list to populate.
	 */
	private void checkFile(File f, List<File> l)
	{
		if (f == null || f.isHidden()) return;
		if (f.isFile()) {
			if (isFileImportable(f)) l.add(f);
		} else if (f.isDirectory()) {
			File[] list = f.listFiles();
			if (list != null && list.length > 0) l.add(f);
			/*
			File[] list = f.listFiles();
			if (list != null && list.length > 0) {
				for (int i = 0; i < list.length; i++) {
					checkFile(list[i], l);
				}
			}
			*/
		}
	}
    
	/**
	 * Returns <code>true</code> if the file will be archived, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isArchived() { return archived.isSelected(); }
	
	/**
	 * Returns <code>true</code> if the user can interact with the archived
	 * options, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean canArchived() { return archived.isVisible(); }
	
	/**
	 * Returns the name to display for a file.
	 * 
	 * @param fullPath The file's absolute path.
	 * @return See above.
	 */
	String getDisplayedFileName(String fullPath)
	{
		if (fullPath == null || !partialName.isSelected()) return fullPath;
		Integer number = (Integer) numberOfFolders.getValueAsNumber();
		return UIUtilities.getDisplayedFileName(fullPath, number);
	}
	
    /** 
     * Creates a new instance.
     * 
     * @param owner 	The owner of the dialog.
     * @param filters 	The list of filters.
     * @param container The container where to import the files.
     * @param type 		One of the type constants.
     */
    public ImportDialog(JFrame owner, List<FileFilter> filters, DataObject 
    		container, int type)
    {
    	super(owner);
    	this.filters = filters;
    	setProperties();
    	initComponents(container, type);
    	buildGUI(container);
    	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    	setSize(7*(screenSize.width/10), 7*(screenSize.height/10));
    }

    /**
     * Resets the text and remove all the files to import.
     * 
     * @param container The container where to import the files.
     */
	public void resetObject(Object container)
	{
		titlePane.setTextHeader(getContainerText(container));
		titlePane.setSubtitle(SUB_MESSAGE);
		table.removeAllFiles();
	}
	
    /**
     * Shows the chooser dialog. 
     * 
     * @return The option selected.
     */
    public int showDialog()
    {
	    UIUtilities.setLocationRelativeToAndShow(getParent(), this);
	    return option;
    }

    /**
     * Shows the chooser dialog. 
     * 
     * @return The option selected.
     */
    public int centerDialog()
    {
	    UIUtilities.centerAndShow(this);
	    return option;
    }
    
	/**
	 * Sets the collection of existing tags.
	 * 
	 * @param tags The collection of existing tags.
	 */
	public void setTags(Collection tags)
	{
		if (tags == null) return;
		Collection<TagAnnotationData> set = tagsMap.values();
		List<Long> ids = new ArrayList<Long>();
		List available = new ArrayList();
		List selected = new ArrayList();
		TagAnnotationData tag;
		Iterator i = set.iterator();
		while (i.hasNext()) {
			tag = (TagAnnotationData) i.next();
			if (tag.getId() > 0)
				ids.add(tag.getId());
		}
		i = tags.iterator();
		while (i.hasNext()) {
			tag = (TagAnnotationData) i.next();
			if (ids.contains(tag.getId())) 
				selected.add(tag);
			else available.add(tag);
		}
		//show the selection wizard
		showSelectionWizard(TagAnnotationData.class, available, selected, true);
	}
	
	/**
	 * Reacts to property fired by the table.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (FileSelectionTable.ADD_PROPERTY.equals(name)) {
			File[] files = chooser.getSelectedFiles();
			if (files == null || files.length == 0) return;
			List<File> l = new ArrayList<File>();
			for (int i = 0; i < files.length; i++) 
				checkFile(files[i], l);
			table.addFiles(l);
			importButton.setEnabled(table.hasFilesToImport());
		} else if (FileSelectionTable.REMOVE_PROPERTY.equals(name)) {
			importButton.setEnabled(table.hasFilesToImport());
		} else if (JFileChooser.SELECTED_FILES_CHANGED_PROPERTY.equals(name)) {
			File[] files = chooser.getSelectedFiles();
			table.allowAddition(files != null && files.length > 0);
		} else if (NumericalTextField.TEXT_UPDATED_PROPERTY.equals(name)) {
			if (partialName.isSelected()) {
		    	Integer number = (Integer) numberOfFolders.getValueAsNumber();
		    	if (number != null && number >= 0) table.applyToAll();
			}
		} else if (SelectionWizard.SELECTED_ITEMS_PROPERTY.equals(name)) {
			Map m = (Map) evt.getNewValue();
			if (m == null || m.size() != 1) return;
			Set set = m.entrySet();
			Entry entry;
			Iterator i = set.iterator();
			Class type;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				type = (Class) entry.getKey();
				if (TagAnnotationData.class.getName().equals(type.getName()))
					handleTagsSelection((Collection) entry.getValue());
			}
		}
	}
	
	/**
	 * Cancels or imports the files.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt)
	{
		int index = Integer.parseInt(evt.getActionCommand());
		switch (index) {
			case IMPORT:
				importFiles();
				break;
			case CANCEL:
				cancelSelection();
				break;
			case REFRESH:
				chooser.rescanCurrentDirectory();
				chooser.repaint();
				break;
			case RESET: 
				partialName.setSelected(false);
				table.resetFilesName();
				break;
			case APPLY_TO_ALL:
				table.applyToAll();
				break;
			case TAG:
				firePropertyChange(LOAD_TAGS_PROPERTY, Boolean.valueOf(false), 
						Boolean.valueOf(true));
				
		}
	}

	/** 
	 * Sets the enabled flag of the numerical field.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		Object src = e.getSource();
		/*
		if (src == overrideName) {
			boolean b = overrideName.isSelected();
			fullName.setEnabled(b);
			partialName.setEnabled(b);
			numberOfFolders.setEnabled(partialName.isSelected());
		} else if (src == partialName) {
			numberOfFolders.setEnabled(partialName.isSelected());
			//table.applyToAll();
			//applyToAllButton.setEnabled(partialName.isSelected());
			//numberOfFolders.setEnabled(partialName.isSelected());
		} 
		*/
		if (src == folderAsContainer) {
			boolean b = folderAsContainer.isSelected();
			numberOfFolders.setEnabled(b);
		}
	}
	
}
