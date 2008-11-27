/*
 * org.openmicroscopy.shoola.agents.imviewer.util.proj.ProjectionSavingDialog 
 *
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
 */
package org.openmicroscopy.shoola.agents.imviewer.util.proj;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;



//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.CreateFolderDialog;
import org.openmicroscopy.shoola.util.ui.slider.TextualTwoKnobsSlider;
import pojos.DatasetData;

/** 
 * Dialog used to determine what how to call the projected image, 
 * where to save it etc.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
class ProjectionSavingDialog 
	extends JDialog
	implements ActionListener, DocumentListener, PropertyChangeListener
{

	/** The suggested extension for the projected image. */
	private static final String DEFAULT_EXTENSION = "_proj";
	
	/** Action id to close the dialog. */
	private static final int 	CLOSE = 0;
	
	/** Action id to close the dialog. */
	private static final int 	PROJECT = 1;
	
	/** Action id to create a new folder. */
	private static final int 	NEWFOLDER = 2;
	
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
	
	/** Keep track of the selected dataset. */
	private Map<JCheckBox, DatasetData> selection;
	
	/** Component to select the time interval. */
	private TextualTwoKnobsSlider		timeSelection;
	
	/** The possible pixels Type. */
	private JComboBox					pixelsType;
	
	/** Check box to apply the rendering settings of the original image. */
	private JCheckBox					rndSettingsBox;
	
	/** Reference to the model. */
	private ProjectionDialog 			model;
	
	/** Closes and disposes. */
	private void close()
	{
		model.enableButtons(true);
		setVisible(false);
		dispose();
	}
	
	/** Projects the image. */
	private void project()
	{
		List<DatasetData> datasets = new ArrayList<DatasetData>();
		Iterator<JCheckBox> i = selection.keySet().iterator();
		JCheckBox box;
		while (i.hasNext()) {
			box = i.next();
			if (box.isSelected())
				datasets.add(selection.get(box));
		}
		int maxT = model.getMaxT();
		int startT = 0, endT = 0;
		if (maxT > 0) {
			startT = timeSelection.getStartValue()-1;
			endT = timeSelection.getEndValue()-1;
		}
		String type = null;
		if (model.getSelectedAlgorithm() == ImViewer.SUM_INTENSITY) {
			String value = (String) pixelsType.getSelectedItem();
			type = EditorUtil.PIXELS_TYPE.get(value);
			if (type.equals(pixelsType)) type = null;
		}
		model.project(datasets, nameField.getText(), startT, endT, type,
				rndSettingsBox.isSelected());
		close();
	}
	
	/** Sets the dialog's properties. */
	private void setProperties()
	{
		setModal(true);
		setTitle("Project Image");
	}
	
	/** 
	 * Initialises the components. 
	 * 
	 * @param datasets The datasets containin the image.
	 */
	private void initComponents(Collection datasets)
	{
		rndSettingsBox = new JCheckBox("Apply same rendering settings");
		rndSettingsBox.setToolTipText(
				UIUtilities.formatToolTipText(
						"Apply the rendering settings to " +
						"the projected image."));
		rndSettingsBox.setSelected(true);
		
		int maxT = model.getMaxT();
		//if (maxT > 1) {
			timeSelection = new TextualTwoKnobsSlider(1, maxT, 1, maxT);
			timeSelection.setSliderLabelText("Timepoint:");
			timeSelection.layoutComponents();
			timeSelection.setEnabled(maxT > 1);
		//}
			
		//if (model.getSelectedAlgorithm() == ImViewer.SUM_INTENSITY) {
			Map<String, String> map = EditorUtil.PIXELS_TYPE_DESCRIPTION;
			String[] data = new String[map.size()];
			Iterator<String> i = map.keySet().iterator();
			int index = 0;
			String originalType = model.getPixelsType();
			String key;
			int selectedIndex = 0;
			while (i.hasNext()) {
				key = i.next();
				data[index] = map.get(key);
				if (key.equals(originalType)) selectedIndex = index;
				index++;
			}
			pixelsType = new JComboBox(data);
			pixelsType.setSelectedIndex(selectedIndex);
			pixelsType.setEnabled(
					model.getSelectedAlgorithm() == ImViewer.SUM_INTENSITY);
		//}
		selectionPane =  new JPanel();
		selectionPane.setLayout(new BoxLayout(selectionPane, BoxLayout.Y_AXIS));
		closeButton = new JButton("Cancel");
		closeButton.setToolTipText(UIUtilities.formatToolTipText(
				"Close the window."));
		closeButton.setActionCommand(""+CLOSE);
		closeButton.addActionListener(this);
		projectButton = new JButton("Project");
		projectButton.setToolTipText(UIUtilities.formatToolTipText(
				"Project the image."));
		projectButton.setActionCommand(""+PROJECT);
		projectButton.addActionListener(this);
		newFolderButton = new JButton("New Dataset");
		newFolderButton.setToolTipText(UIUtilities.formatToolTipText(
				"Create a new dataset."));
		newFolderButton.setActionCommand(""+NEWFOLDER);
		newFolderButton.addActionListener(this);
		nameField = new JTextField();

		String s = EditorUtil.removeFileExtension(model.getImageName());
		s += DEFAULT_EXTENSION;
		nameField.setText(s);
		nameField.getDocument().addDocumentListener(this);
		//Display datasets
		selection = new HashMap<JCheckBox, DatasetData>();
		if (datasets != null && datasets.size() > 0) {
			
			ViewerSorter sorter = new ViewerSorter();
			List l = sorter.sort(datasets);
			Iterator j = l.iterator();
			JCheckBox box;
			DatasetData d;
			index = 0;
			while (j.hasNext()) {
				d = (DatasetData) j.next();
				box = new JCheckBox(d.getName());
				selection.put(box, d);
				if (index == 0) box.setSelected(true);
				index++;
			}
		}
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
		//p.add(buildChannelsPanel());
		//p.add(new JSeparator());
		if (timeSelection != null) {
			p.add(buildTimeRangePanel());
			p.add(new JSeparator());
		}
		if (pixelsType != null) {
			p.add(buildPixelsTypePanel());
			p.add(new JSeparator());
		}
		
		p.add(UIUtilities.buildComponentPanel(rndSettingsBox));
		JPanel r = UIUtilities.buildComponentPanel(p);
		r.setBorder(new TitledBorder(""));
		return r;
	}
	
	/**
	 * Builds and lays out the channels options.
	 * 
	 * @return See above.
	 */
	private JPanel buildTimeRangePanel()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(timeSelection);
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
        				TableLayout.PREFERRED, TableLayout.PREFERRED, height, 5,
        				TableLayout.PREFERRED, TableLayout.FILL} }; //rows
        content.setLayout(new TableLayout(tl));
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
       
        content.add(UIUtilities.setTextFont("Name "), "0, 1, l, c");
        content.add(nameField, "1, 1, f, c");
        content.add(new JLabel(), "0, 2, 1, 2");
        content.add(UIUtilities.setTextFont("Save in "), "0, 3, l, c");
        content.add(UIUtilities.setTextFont("datasets "), "0, 4, l, c");
    	content.add(new JScrollPane(selectionPane), "1, 3, 1, 5");
        if (selection != null) {
        	Iterator i = selection.keySet().iterator();
        	while (i.hasNext()) 
        		selectionPane.add((JComponent) i.next());
        }
        content.add(new JLabel(), "0, 6, 1, 6");
        content.add(UIUtilities.setTextFont("Project "), "0, 7, l, c");
        content.add(buildParametersPanel(), "1, 7, 1, 8");
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
		
		JPanel controls = new JPanel();
    	controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
    	controls.add(Box.createRigidArea(new Dimension(20, 5)));
    	controls.add(newFolderButton);
    	JPanel p = UIUtilities.buildComponentPanelRight(bar);
        p.setOpaque(true);
        controls.add(p);
        return controls;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		IconManager icons = IconManager.getInstance();
		TitlePanel tp = new TitlePanel("Projection", "Set the name and " +
				"where to save the projected image.", 
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
		JCheckBox box = new JCheckBox(name);
		box.setSelected(true);
		DatasetData d = new DatasetData();
		d.setName(name);
		selectionPane.removeAll();
		selectionPane.add(box);
		if (selection != null) {
        	Iterator i = selection.keySet().iterator();
        	while (i.hasNext()) {
        		selectionPane.add((JCheckBox) i.next());
        	}
        }
		selection.put(box, d);
		selectionPane.revalidate();
		selectionPane.repaint();
		newFolderButton.setEnabled(false);
	}
	
	/** Sets the enabled flag of the {@link #projectButton}. */
    private void enableSave()
    {
    	String name = nameField.getText();
    	if (name == null) projectButton.setEnabled(false);
    	name = name.trim();
    	int l = name.length();
    	projectButton.setEnabled(l > 0 && l < 256);
    }

	/**
	 * Creates a new instance.
	 * 
	 * @param model    Reference to the model Mustn't be <code>null</code>.
	 * @param datasets The datasets containing the image.
	 */
	ProjectionSavingDialog(ProjectionDialog model, Collection datasets)
	{
		super(model);
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.model = model;
		setProperties();
		initComponents(datasets);
		buildGUI();
		pack();
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
