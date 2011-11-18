/*
 * org.openmicroscopy.shoola.agents.imviewer.util.proj.ProjSavingDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
 */
package org.openmicroscopy.shoola.agents.imviewer.util.proj;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries
import info.clearthought.layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.agents.util.browser.DataNode;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.CreateFolderDialog;
import org.openmicroscopy.shoola.util.ui.slider.TextualTwoKnobsSlider;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ProjectData;

/** 
 * Dialog used to set the extra parameters required to project the image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ProjSavingDialog 
	extends JDialog
	implements ActionListener, DocumentListener, PropertyChangeListener
{

	/** Bound property indicating to project the image. */
	public static final String 	PROJECTION_PROPERTY = "projection";
	
	/** Bound property indicating to load all the datasets. */
	public static final String	LOAD_ALL_PROPERTY = "loadAll";
	
	/** The default text. */
	private static final String	PROJECT_TXT = "Project";
	
	/** The default text. */
	private static final String	DATASET_TXT = "Dataset";
	
	/** The title of the dialog. */
	private static final String	TITLE = "Projection";
	
	/** The suggested extension for the projected image. */
	private static final String DEFAULT_EXTENSION = "_proj";
	
	/** Action id to close the dialog. */
	private static final int 	CLOSE = 0;
	
	/** Action id to close the dialog. */
	private static final int 	PROJECT = 1;
	
	/** Action id to create a new folder. */
	private static final int 	NEWFOLDER = 2;
	
	/** Action id to load all the available datasets. */
	private static final int 	OTHER = 3;
	
	/** The text field hosting the name of the file. */
	private JTextField 					nameField;

	/** Closes the window. */
	private JButton		 				closeButton;
	
	/** Project the image. */
	private JButton		 				projectButton;
	
	/** Button to create a new dataset. */
	private JButton		 				newFolderButton;

	/** The component hosting the datasets containing the image. */
	private JPanel						selectionPane;
	
	/** Component to select the time interval. */
	private TextualTwoKnobsSlider		timeSelection;
	
	/** Component to select the z-interval. */
	private TextualTwoKnobsSlider		zrangeSelection;
	
	/** The possible pixels Type. */
	private JComboBox					pixelsType;
	
	/** Check box to apply the rendering settings of the original image. */
	private JCheckBox					rndSettingsBox;
	
	/** The maximum number of timepoints. */
	private int							maxT;
	
	/** The selected projection algorithm. */
	private int							algorithm;
	
	/** Used to sort the containers. */
	private ViewerSorter 				sorter;
	
	/** Component used to select the dataset. */
	private JComboBox					datasetsBox;
	
	/** Component used to select the project. */
	private JComboBox					parentsBox;
	
	/** The listener linked to the parents box. */
	private ActionListener				parentsBoxListener;
	
	/** The listener linked to the datasets box. */
	private ActionListener				datasetsBoxListener;
	
	/** The selected container where to import the data. */
	private DataObject					selectedContainer;
	
	/** The selected container where to import the data. */
	private DataObject					selectedGrandParentContainer;
	
	/** The selected dataset.*/
	private DatasetData					selectedDataset;
	
	/** The container indicating where to save the projected image.*/
	private JLabel						containerLabel;
	
	/** Sets the properties of the dialog. */
	private void setProperties()
	{
		setTitle(TITLE);
		setModal(true);
	}
	
	/** Displays where to save the image.*/
	private void setLabelText()
	{
		StringBuffer buffer = new StringBuffer();
		if (parentsBox.getItemCount() > 0) {
			DataNode node = (DataNode) parentsBox.getSelectedItem();
			if (!node.isDefaultProject())
				buffer.append(node.toString()+"/");
		}
		if (selectedDataset != null)
			buffer.append(selectedDataset.getName());
		containerLabel.setText(buffer.toString());
	}
	
	/** Populates the datasets box depending on the selected project. */
	private void populateDatasetsBox()
	{
		DataNode n = (DataNode) parentsBox.getSelectedItem();
		List<DataNode> list = n.getUIDatasetNodes();
		List l;
		if (list == null || list.size() == 0) {
			l = new ArrayList();
			l.add(DataNode.createDefaultDataset());
		} else l = sorter.sort(list);
		
		datasetsBox.removeActionListener(datasetsBoxListener);
		datasetsBox.removeAllItems();
		
		datasetsBox.setModel(new DefaultComboBoxModel(l.toArray()));
		if (selectedContainer != null && 
			selectedContainer instanceof DatasetData) {
			DatasetData d = (DatasetData) selectedContainer;
			Iterator<DataNode> i = l.iterator();
			while (i.hasNext()) {
				n = i.next();
				if (n.getDataObject().getId() == d.getId()) {
					datasetsBox.setSelectedItem(n);
					selectedDataset = (DatasetData) n.getDataObject();
					break;
				}
			}
		} else { // no node selected
			if (l.size() > 1) {
				Iterator<DataNode> i = l.iterator();
				while (i.hasNext()) {
					n = i.next();
					if (!n.isDefaultDataset()) {
						datasetsBox.setSelectedItem(n);
						break;
					}
				}
			}
		}
		datasetsBox.addActionListener(datasetsBoxListener);
		setLabelText();
	}
	
	/**
	 * Initializes the components.
	 * 
	 * @param imageName	The name of the image.
	 * @param type		The type of projection.
	 * @param maxZ		The maximum number of z-sections.
	 * @param startZ	The lower bound of the z-section interval.
	 * @param endZ		The upper bound of the z-section interval.
	 */
	private void initComponents(String imageName, String type, int maxZ,
			int startZ, int endZ)
	{
		containerLabel = new JLabel();
		parentsBox = new JComboBox();
		parentsBoxListener = new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				populateDatasetsBox();
			}
		};
		parentsBox.addActionListener(parentsBoxListener);
		datasetsBox = new JComboBox();
		datasetsBoxListener = new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				DataNode node = (DataNode) datasetsBox.getSelectedItem();
				if (node != null) {
					selectedDataset = (DatasetData) node.getDataObject();
					setLabelText();
				}
			}
		};
		datasetsBox.addActionListener(datasetsBoxListener);
		rndSettingsBox = new JCheckBox("Apply same rendering settings");
		rndSettingsBox.setToolTipText(
				UIUtilities.formatToolTipText(
						"Apply the rendering settings to " +
						"the projected image."));
		rndSettingsBox.setSelected(true);

		zrangeSelection = new TextualTwoKnobsSlider(1, maxZ, startZ, endZ);
		zrangeSelection.layoutComponents(TextualTwoKnobsSlider.LAYOUT_FIELDS);
		
		timeSelection = new TextualTwoKnobsSlider(1, maxT, 1, maxT);
		timeSelection.layoutComponents(TextualTwoKnobsSlider.LAYOUT_FIELDS);
		timeSelection.setEnabled(maxT > 1);
			
		Map<String, String> map = EditorUtil.PIXELS_TYPE_DESCRIPTION;
		String[] data = new String[map.size()];
		Set set = map.entrySet();
		Entry entry;
		Iterator i = set.iterator();
		int index = 0;
		//String originalType = type;
		String key;
		int selectedIndex = 0;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			key = (String) entry.getKey();
			data[index] = (String) entry.getValue();
			if (key.equals(type)) selectedIndex = index;
			index++;
		}
		pixelsType = new JComboBox(data);
		pixelsType.setSelectedIndex(selectedIndex);
		pixelsType.setEnabled(algorithm == ProjectionParam.SUM_INTENSITY);
		
		selectionPane =  new JPanel();
		selectionPane.setLayout(new BoxLayout(selectionPane, BoxLayout.Y_AXIS));
		
		closeButton = new JButton("Cancel");
		closeButton.setToolTipText(UIUtilities.formatToolTipText(
				"Close the window."));
		closeButton.setActionCommand(""+CLOSE);
		closeButton.addActionListener(this);
		projectButton = new JButton("Save");
		projectButton.setToolTipText(UIUtilities.formatToolTipText(
				"Project the image."));
		projectButton.setActionCommand(""+PROJECT);
		projectButton.addActionListener(this);
		newFolderButton = new JButton("New Dataset...");
		newFolderButton.setToolTipText(UIUtilities.formatToolTipText(
				"Create a new Dataset."));
		newFolderButton.setActionCommand(""+NEWFOLDER);
		newFolderButton.addActionListener(this);
		nameField = new JTextField();

		StringBuffer buffer = new StringBuffer();
		buffer.append(UIUtilities.removeFileExtension(imageName));
		buffer.append(DEFAULT_EXTENSION);
		nameField.setText(buffer.toString());
		nameField.getDocument().addDocumentListener(this);
		//Display datasets
		getRootPane().setDefaultButton(projectButton);
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) { close(); }
		});
	}
	
	/**
	 * Builds the various panels used to set the parameters of the
	 * projection.
	 * 
	 * @return See above.
	 */
	private JPanel buildParametersPanel()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(buildRangePanel(zrangeSelection, "Z Range: "));
		p.add(buildRangePanel(timeSelection, "Timepoint: "));
		
		if (pixelsType != null) {
			p.add(new JSeparator());
			p.add(buildPixelsTypePanel());
		}
		
		JPanel r = UIUtilities.buildComponentPanel(p);
		r.setBorder(new TitledBorder(""));
		return r;
	}
	
	/**
	 * Builds and lays out the passed component.
	 * 
	 * @param comp The component to lay out.
	 * @param text The text displayed in front of the component.
	 * @return See above.
	 */
	private JPanel buildRangePanel(JComponent comp, String text)
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(new JLabel(text));
		p.add(UIUtilities.buildComponentPanel(comp));
        return p;
	}
	
	/**
	 * Builds and lays out the pixels type options.
	 * 
	 * @return See above.
	 */
	private JPanel buildPixelsTypePanel()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(new JLabel("Data Type: "));
		p.add(UIUtilities.buildComponentPanel(pixelsType));
        return p;
	}
	
	/**
	 * Builds the controls.
	 * 
	 * @return See above.
	 */
	private JPanel buildControls()
	{
		JPanel p = new JPanel();
		p.setBorder(new TitledBorder(""));
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(selectionPane);
		p.add(Box.createHorizontalStrut(5));
		p.add(newFolderButton);
		return p;
	}
	
	/**
	 * Creates a row.
	 * 
	 * @return See above.
	 */
	private JPanel createRow()
	{
		JPanel row = new JPanel();
		row.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		row.setBorder(null);
		return row;
	}
	
	/**
	 * Returns the file queue and indicates where the files will be imported.
	 * 
	 * @return See above.
	 */
	private void buildLocationPane()
	{
		selectionPane.removeAll();
		JPanel row = createRow();
		selectionPane.add(row);
		selectionPane.add(Box.createVerticalStrut(2));
		row = createRow();
		row.add(UIUtilities.setTextFont(PROJECT_TXT));
		row.add(parentsBox);
		selectionPane.add(row);
		selectionPane.add(Box.createVerticalStrut(8));
		row = createRow();
		row.add(UIUtilities.setTextFont(DATASET_TXT));
		row.add(datasetsBox);
		selectionPane.add(row);
	}
	
	/**
	 * Builds the main component.
	 * 
	 * @return See above.
	 */
	private JPanel buildBody()
	{
		JPanel content = new JPanel();
        int height = 80;
        double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL}, //columns
        				{TableLayout.PREFERRED, TableLayout.PREFERRED, 5, 
        				TableLayout.PREFERRED, TableLayout.PREFERRED, 
        				TableLayout.PREFERRED, height, 5,
        				TableLayout.PREFERRED, TableLayout.FILL} }; //rows
        content.setLayout(new TableLayout(tl));
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
       
        content.add(UIUtilities.setTextFont("Name "), "0, 1, LEFT, CENTER");
        content.add(nameField, "1, 1, FULL, CENTER");
        content.add(new JLabel(), "0, 2, 1, 2");
        content.add(UIUtilities.setTextFont("Save in "), "0, 3, LEFT, CENTER");
        content.add(containerLabel, "1, 3, LEFT, CENTER");
        //content.add(UIUtilities.buildComponentPanel(buildControls()), 
        //		"0, 5, l, c");
    	content.add(buildControls(), "1, 4, 1, 6");
        content.add(new JLabel(), "0, 7, 1, 7");
        content.add(UIUtilities.setTextFont("Parameters "), "0, 8," +
        		"LEFT, CENTER");
        content.add(buildParametersPanel(), "1, 8, 1, 9");
		return content;
	}
	
	/** 
	 * Builds and lays out the control.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBar()
	{
		JPanel bar = new JPanel();
		bar.add(closeButton);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(projectButton);
		bar.add(Box.createHorizontalStrut(20));
		return UIUtilities.buildComponentPanelRight(bar);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		IconManager icons = IconManager.getInstance();
		TitlePanel tp = new TitlePanel(TITLE, "Set the projection's " +
				"parameters.", 
				icons.getIcon(IconManager.PROJECTION_48));
		Container c = getContentPane();
		c.setLayout(new BorderLayout(5, 5));
		c.add(tp, BorderLayout.NORTH);
		c.add(buildBody(), BorderLayout.CENTER);
		c.add(buildToolBar(), BorderLayout.SOUTH);
	}
	
	/** 
	 * Adds a new entry to the display, and indicates to create a new dataset.
	 * 
	 * @param name The name of the dataset to create.
	 */
	private void createDataset(String name)
	{
		DatasetData d = new DatasetData();
		d.setName(name);
		selectedDataset = d;
		setLabelText();
		/*
		JCheckBox newBox = new JCheckBox(name);
		newBox.setSelected(true);
		DatasetData d = new DatasetData();
		d.setName(name);
		selectionPane.removeAll();
		selectionPane.add(newBox);
		if (selection != null) {
        	Iterator i = selection.keySet().iterator();
        	JCheckBox box;
        	while (i.hasNext()) {
        		selectionPane.add((JCheckBox) i.next());
        	}
        }
		if (selection == null) 
			selection = new LinkedHashMap<JCheckBox, DatasetData>();
		selection.put(newBox, d);
		selectionPane.revalidate();
		selectionPane.repaint();
		newFolderButton.setEnabled(false);
		*/
	}
	
	/** Closes and disposes. */
	private void close()
	{
		setVisible(false);
		dispose();
	}
	
	/** Sets the enabled flag of the {@link #projectButton}. */
    private void enableSave()
    {
    	String name = nameField.getText();
    	if (name == null) projectButton.setEnabled(false);
    	else {
    		name = name.trim();
        	int l = name.length();
        	projectButton.setEnabled(l > 0 && l < 256);
    	}
    }
    
    /** Projects the image. */
    private void project()
    {
    	/*
		Iterator<JCheckBox> i = selection.keySet().iterator();
		JCheckBox box;
		while (i.hasNext()) {
			box = i.next();
			if (box.isSelected())
				datasets.add(selection.get(box));
		}
		*/
		int startT = 0, endT = 0;
		if (maxT > 0) {
			startT = (int) timeSelection.getStartValue()-1;
			endT = (int) timeSelection.getEndValue()-1;
		}
		/*
		String type = null;
		if (algorithm == ImViewer.SUM_INTENSITY) {
			String value = (String) pixelsType.getSelectedItem();
			type = EditorUtil.PIXELS_TYPE.get(value);
			if (type.equals(value)) type = null;
		}
		*/
		ProjectionRef ref = new ProjectionRef();
		ref.setDatasets(Arrays.asList(selectedDataset));
		if (selectedDataset.getId() <= 0) {
			DataNode node = (DataNode) parentsBox.getSelectedItem();
			if (!node.isDefaultNode()) 
				ref.setProject((ProjectData) node.getDataObject());
		}
		ref.setImageName(nameField.getText());
		ref.setTInterval(startT, endT);
		double s = zrangeSelection.getStartValue();
		double e = zrangeSelection.getEndValue();
		ref.setZInterval((int) s-1, (int) e-1);
		ref.setApplySettings(rndSettingsBox.isSelected());
		firePropertyChange(PROJECTION_PROPERTY, null, ref);
		close();
    }

	/**
	 * Creates a new instance.
	 * 
	 * @param owner	The owner of the frame.
	 * @param selectedContainer The default container.
	 * @param selectedGrandParentContainer The container hosting the dataset.
	 */
	public ProjSavingDialog(JFrame owner, DataObject selectedContainer, 
			DataObject selectedGrandParentContainer)
	{
		super(owner);
		this.selectedContainer = selectedContainer;
		this.selectedGrandParentContainer = selectedGrandParentContainer;
		setProperties();
		sorter = new ViewerSorter();
	}
	
	/**
	 * Initializes the dialog.
	 * 
	 * @param algorithm		The selected projection algorithm.
	 * @param maxT			The maximum number of time-points.
	 * @param pixelsType	The type of pixels of the original image.
	 * @param imageName		The name of the original image.
	 * @param containers	The containers containing the image.
	 * @param maxZ			The maximum number of z-sections.
	 * @param startZ		The lower bound of the z-section interval.
	 * @param endZ			The upper bound of the z-section interval.
	 */
	public void initialize(int algorithm, int maxT, String pixelsType, 
						String imageName, Collection containers, int maxZ, 
						int startZ, int endZ)
	{
		this.maxT = maxT;
		this.algorithm = algorithm;
		initComponents(imageName, pixelsType, maxZ, startZ, endZ);
		buildGUI();
		setContainers(containers);
	}
	
	/**
	 * Sets the projection interval value.
	 * 
	 * @param startZ The lower bound of the z-section interval.
	 * @param endZ   The upper bound of the z-section interval.
	 */
	public void setProjectionInterval(int startZ, int endZ)
	{
		zrangeSelection.setInterval(startZ, endZ);
	}
	
	/** 
	 * Sets the available containers.
	 * 
	 * @param containers The value to set.
	 */
	public void setContainers(Collection containers)
	{
		if (containers == null || containers.size() == 0) return;
		parentsBox.removeActionListener(parentsBoxListener);
		parentsBox.removeAllItems();
		parentsBox.addActionListener(parentsBoxListener);
		datasetsBox.removeAllItems();
		List<DataNode> topList = new ArrayList<DataNode>();
		List<DataNode> datasetsList = new ArrayList<DataNode>();
		DataObject ho;
		DataNode n;
		if (containers != null && containers.size() > 0) {
			Iterator<DataObject> i = containers.iterator();
			while (i.hasNext()) {
				ho = i.next();
				if (ho instanceof ProjectData) {
					n = new DataNode((DataObject) ho);
					topList.add(n); 
				} else if (ho instanceof DatasetData) {
					n = new DataNode((DataObject) ho);
					datasetsList.add(n);
				}
			}
		}
		List sortedList = new ArrayList();
		if (topList.size() > 0) {
			sortedList = sorter.sort(topList);
		}
		
		//check if new top nodes
		List finalList = new ArrayList();
		if (datasetsList.size() > 0)
			finalList.add(new DataNode(datasetsList));
		finalList.addAll(sortedList);
		parentsBox.removeActionListener(parentsBoxListener);
		parentsBox.setModel(new DefaultComboBoxModel(finalList.toArray()));
		
		if (selectedGrandParentContainer != null &&
				selectedGrandParentContainer instanceof ProjectData) {
			
			for (int i = 0; i < parentsBox.getItemCount(); i++) {
				n = (DataNode) parentsBox.getItemAt(i);
				if (n.getDataObject().getId() == 
					selectedGrandParentContainer.getId()) {
					parentsBox.setSelectedIndex(i);
					break;
				}
			}
		}
		parentsBox.addActionListener(parentsBoxListener);
		populateDatasetsBox();
		buildLocationPane();
		
		/*
		if (datasets == null) return;
		List<String> selected = getSelectedDatasets();
		JCheckBox box;
		DatasetData d;
		if (selection == null)
			selection = new LinkedHashMap<JCheckBox, DatasetData>();
		else {
			selection.clear();
		}
		if (datasets != null && datasets.size() > 0) {
			List l = sorter.sort(datasets);
			Iterator j = l.iterator();
			while (j.hasNext()) {
				d = (DatasetData) j.next();
				box = new JCheckBox(d.getName());
				selection.put(box, d);
				box.setSelected(selected.contains(d.getName()));
			}
			selectionPane.removeAll();
			Iterator i = selection.keySet().iterator();
        	while (i.hasNext()) 
        		selectionPane.add((JComponent) i.next());
        	selectionPane.validate();
        	selectionPane.repaint();
        	pane.revalidate();
		}
		*/
	}
	
	/**
	 * Closes or projects the image.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case PROJECT:
				project();
				break;
			case CLOSE:
				close();
				break;
			case OTHER:
				firePropertyChange(LOAD_ALL_PROPERTY, Boolean.valueOf(false), 
						Boolean.valueOf(true));
				break;
			case NEWFOLDER:
				CreateFolderDialog d = new CreateFolderDialog(this, 
						"New Dataset");
				d.setDefaultName("untitled dataset");
				d.addPropertyChangeListener(
						CreateFolderDialog.CREATE_FOLDER_PROPERTY, this);
				d.pack();
				UIUtilities.centerAndShow(this, d);
		}
	}

	/**
	 * Creates a new dataset.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (CreateFolderDialog.CREATE_FOLDER_PROPERTY.equals(name)) {
			String folderName = (String) evt.getNewValue();
			if (folderName != null && folderName.trim().length() > 0) 
				createDataset(folderName);
		}
	}
	
	/**
	 * Enables the save button depending on the value entered for the name.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e) { enableSave(); }

	/**
	 * Enables the save button depending on the value entered for the name.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e) { enableSave(); }
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation
	 * in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}
	
}
