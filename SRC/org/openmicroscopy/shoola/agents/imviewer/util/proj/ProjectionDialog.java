/*
 * org.openmicroscopy.shoola.agents.imviewer.util.ProjectionDialog 
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
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.slider.TwoKnobsSlider;
import pojos.DatasetData;

/** 
 * The dialog used to select the projection parameters and the projected
 * image.
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
public class ProjectionDialog
	extends JDialog
{

	/** Bound property indicating to project part of the selected stack. */
	public static final String		PROJECTION_PREVIEW_PROPERTY = 
										"projectionPreview";
	
	/** Bound property indicating to project the image. */
	public static final String 		PROJECTION_PROPERTY = "projection";
	
	/** Bound property indicating to load the datasets containing the image. */
	public static final String 		LOAD_DATASETS_PROPERTY = "loadDatasets";
	
	/** The maximum number of z-sections. */
	private int            		   maxZ;
	
	/** Slider to select the z interval. */
	private TwoKnobsSlider 		   slider;
	
	/** Field to display the first z-stack to project. */
    private NumericalTextField     startField;
    
    /** Field to display the last z-stack to project. */
    private NumericalTextField     endField;
    
	/** The type of projection. */
	private Map<Integer, Integer>  projectionType;

	/** Project the selected z-sections for the currently selected timepoint. */
	private JButton				   previewButton;
	
	/** Project the selected z-sections of the whole image. */
	private JButton				   projectionButton;
	
	 /** The bar notifying the user for the data retrieval progress. */
    private JProgressBar           progressBar;
    
    /** The reference object hosting the parameters used to project. */
    private ProjectionRef		   ref;
    
    /** The label displaying the status. */
    private JLabel				   statusLabel;
    
    /** The type of supported projections. */
    private JComboBox			   types;
    
    /** The UI delegate. */
    private ProjectionUI           uiDelegate;
    
    /** Sets the stepping for the mapping. */
    private JSpinner			   frequency;
    
    /** The first optical section. */
    private int					   startZ;
    
    /** The last optical section. */
    private int					    endZ;
    
    /** The name of the image. */
    private String 					imageName;
    
    /** Reference to the control. */
	private ProjectionDialogControl	controller;
	
	/** Collects and stores the parameters used for projection. */
	private void fillProjectionRef()
	{
		if (ref == null) ref = new ProjectionRef();
		ref.setZInterval(startZ-1, endZ-1);
		int value = (Integer) frequency.getValue();
		ref.setStepping(value);
		int index = types.getSelectedIndex();
		ref.setType(projectionType.get(index));
	}
	
	/** 
	 * Initializes the components composing the display. 
	 * 
	 * @param projections The projection supported.
	 * @param background  The background color.
	 */
	private void initComponents(Map<Integer, String> projections, 
			                   Color background)
	{
		frequency = new JSpinner(new SpinnerNumberModel(1, 1, maxZ, 1));
		startZ = 1;
		endZ = maxZ;
		slider = new TwoKnobsSlider(startZ, maxZ, startZ, maxZ);
		slider.setPaintLabels(false);
		slider.setPaintEndLabels(false);
		slider.setPaintTicks(false);
		slider.addPropertyChangeListener(controller);
		int length = (""+maxZ).length()-2; 
        startField = new NumericalTextField(startZ, endZ);
        startField.setColumns(length);
        endField = new NumericalTextField(startZ, endZ);
        endField.setColumns(length);
        startField.setText(""+startZ);
        endField.setText(""+endZ);
        controller.attachFieldListeners(startField, 
        		ProjectionDialogControl.START_Z);
        controller.attachFieldListeners(endField, 
        		ProjectionDialogControl.END_Z);
		uiDelegate = new ProjectionUI(background);
		
		previewButton = new JButton("Preview");
		previewButton.setToolTipText(UIUtilities.formatToolTipText(
				"Project the interval for the current timepoint."));
		previewButton.setActionCommand(""+ProjectionDialogControl.PREVIEW);
		previewButton.addActionListener(controller);
		projectionButton = new JButton("Project");
		projectionButton.setToolTipText(UIUtilities.formatToolTipText(
		"Project the interval for the whole image."));
		projectionButton.setActionCommand(""+ProjectionDialogControl.PROJECT);
		projectionButton.addActionListener(controller);
		
		progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        statusLabel = new JLabel();
        
        String[] names = new String[projections.size()];
        int index = 0;
        Iterator<Integer> i = projections.keySet().iterator();
        projectionType = new HashMap<Integer, Integer>();
        int j;
        while (i.hasNext()) {
			j = i.next();
			projectionType.put(index, j);
			names[index] = projections.get(j);
			index++;
		}
        types = new JComboBox(names);
        getRootPane().setDefaultButton(previewButton);
	}
	
	/** 
	 * Builds and lays out the component controlling the slices selection. 
	 * 
	 * @return See above.
	 */
	private JPanel buildControlComponent()
	{
		int charWidth = getFontMetrics(getFont()).charWidth('m');;
		JPanel p = new JPanel();
		
		Insets insets = endField.getInsets();
		int length = (""+maxZ).length();
		int x = insets.left+length*charWidth+insets.left;
		Dimension d = startField.getPreferredSize();
		startField.setPreferredSize(new Dimension(x, d.height));
		d = endField.getPreferredSize();
		endField.setPreferredSize(new Dimension(x, d.height));
		GridBagConstraints c = new GridBagConstraints();
		p.setLayout(new GridBagLayout());
		c.weightx = 0;        
		c.anchor = GridBagConstraints.WEST;
		p.add(new JLabel(" Start "), c);
		c.gridx = 1;
		c.ipadx = x;
		c.weightx = 0.5;
		p.add(UIUtilities.buildComponentPanel(startField), c);
		c.gridx = 2;
		c.ipadx = 0;
		c.weightx = 0;
		p.add(new JLabel(" End "), c);
		c.gridx = 3;
		c.ipadx = x;
		c.weightx = 0.5;
		p.add(UIUtilities.buildComponentPanel(endField), c);
		
		JPanel content = new JPanel();
		double size[][] =
        {{TableLayout.PREFERRED, TableLayout.PREFERRED, 5, 
        	TableLayout.PREFERRED, TableLayout.PREFERRED},  // Columns
         {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}; // Rows
		content.setLayout(new TableLayout(size));
		content.add(new JLabel("Slice: "), "0, 0");
		content.add(UIUtilities.buildComponentPanel(slider), "1, 0");
		content.add(p, "1, 1");
		content.add(new JLabel("Projection: "), "3, 0");
		content.add(types, "4, 0");
		content.add(new JLabel("Every n-th slice: "), "3, 1");
		content.add(UIUtilities.buildComponentPanel(frequency), "4, 1, l, c");
		content.add(new JSeparator(), "0, 2, 5, 2");
		return content;
	}
	
	/** 
	 * Builds and lays out the main component of the dialog.
	 * 
	 * @return See above.
	 */
	private JPanel buildBody()
	{
		JPanel body = new JPanel();
		double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL}, 
				{TableLayout.PREFERRED, TableLayout.FILL}};
		body.setLayout(new TableLayout(tl));
		body.add(buildControlComponent(), "0, 0, 1, 0");
		body.add(buildToolBar(), "0, 1");
		body.add(uiDelegate, "1, 1");
		return body;
	}
	
	/**
	 * Builds the tool bar.
	 * 
	 * @return See above
	 */
	private JPanel buildToolBar()
	{
		JPanel bar = new JPanel();
		bar.setLayout(new BoxLayout(bar, BoxLayout.Y_AXIS));
    	bar.setBorder(null);
    	bar.add(previewButton);
    	bar.add(Box.createVerticalStrut(10));
    	bar.add(projectionButton);
    	return bar;
	}
	
	/**
	 * Builds and lays out the status bar.
	 * 
	 * @return See above.
	 */
	private JPanel buildStatusBar()
	{
		IconManager icons = IconManager.getInstance();
		JPanel statusBar = new JPanel();
		statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.X_AXIS));
		statusBar.setBorder(BorderFactory.createEtchedBorder());
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(new JLabel(icons.getIcon(IconManager.INFO)));
		p.add(Box.createHorizontalStrut(5));
		p.add(statusLabel);
		statusBar.add(UIUtilities.buildComponentPanel(p));
		statusBar.add(UIUtilities.buildComponentPanelRight(progressBar));
		p.add(Box.createHorizontalStrut(10));
		return statusBar;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		IconManager icons = IconManager.getInstance();
		TitlePanel tp = new TitlePanel("Projection", "Select the Optical " +
				"sections to project.", 
				icons.getIcon(IconManager.PROJECTION_48));
		Container c = getContentPane();
		c.add(tp, BorderLayout.NORTH);
		c.add(buildBody(), BorderLayout.CENTER);
		c.add(buildStatusBar(), BorderLayout.SOUTH);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param owner       The owner of the dialog.
	 * @param projections The type of projection.
	 * @param maxZ        The number of optical sections.
	 * @param background  The background color of the canvas.
	 * @param imageName	  The name of the original image.
	 * @param imageWidth  The width of the original image.
	 * @param imageHeight  The width of the original image.
	 */
	public ProjectionDialog(JFrame owner, Map<Integer, String> projections, 
			             int maxZ, Color background, String imageName, 
			             int imageWidth, int imageHeight)
	{
		super(owner);
		controller = new ProjectionDialogControl(this);
		this.maxZ = maxZ;
		this.imageName = imageName;
		initComponents(projections, background);
		buildGUI();
		Dimension d = new Dimension(imageWidth, imageHeight);
		uiDelegate.setPreferredSize(d);
		uiDelegate.setSize(d);
		pack();
	}
	
	/** 
	 * Updates the first optical section.
	 * 
	 * @param value The value to set
	 */
	void updateStartField(int value)
	{
		startZ = value;
		controller.removeFieldListeners(startField);
		startField.setText(""+value);
		endField.setMinimum(startZ);
		controller.attachFieldListeners(startField, 
							ProjectionDialogControl.START_Z);
	}
	
	/** 
	 * Updates the last optical section.
	 * 
	 * @param value The value to set
	 */
	void updateEndField(int value)
	{
		endZ = value;
		controller.removeFieldListeners(endField);
		endField.setText(""+value);
		startField.setMaximum(endZ);
		controller.attachFieldListeners(endField, 
							ProjectionDialogControl.END_Z);
	}
	
	/** Sets the first optical section. */
	void setStartZ()
	{
		boolean valid = false;
		int val = 0;
		try {
            val = Integer.parseInt(startField.getText());
            
            if (1 <= val && val < endZ) valid = true;
        } catch(NumberFormatException nfe) {}
        if (!valid) {
            startField.selectAll();
            return;
        }
        startZ = val;
        endField.setMinimum(startZ);
        slider.setStartValue(startZ);
	}
	
	/** Sets the last optical section. */
	void setEndZ()
	{
		boolean valid = false;
		int val = 0;
		try {
            val = Integer.parseInt(endField.getText());
            
            if (startZ < val && val <= maxZ) valid = true;
        } catch(NumberFormatException nfe) {}
        if (!valid) {
            endField.selectAll();
            return;
            
        }
        endZ = val;
        startField.setMaximum(endZ);
        slider.setEndValue(endZ);
	}
	
	/** Invokes when lost of focus on text fields. */
	void handleFocusLost()
	{
		String s = ""+startZ;
		String e = ""+endZ;
		String startVal = startField.getText();
		String endVal = endField.getText();
		if (startVal == null || !startVal.equals(s))
			startField.setText(s);        
		if (endVal == null || !endVal.equals(e)) 
			endField.setText(e);
	}
	
	/** Projects and previews. */
	void preview()
	{
		enableButtons(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		fillProjectionRef();
		progressBar.setVisible(true);
		statusLabel.setText("Projecting...");
		firePropertyChange(PROJECTION_PREVIEW_PROPERTY, null, ref);
		setModal(true);
	}
	
	/**
	 * Sets the <code>enabled</code> flag of the {@link #previewButton}
	 * and {@link #projectionButton}.
	 * 
	 * @param enabled The value to set.
	 */
	void enableButtons(boolean enabled)
	{
		previewButton.setEnabled(enabled);
		projectionButton.setEnabled(enabled);
	}
	
	/**
	 * Returns the name of the original image.
	 * 
	 * @return See above.
	 */
	String getImageName() { return imageName; }
	
	/** Loads the datasets containing the image to project. */
	void loadDatasets()
	{
		firePropertyChange(LOAD_DATASETS_PROPERTY, Boolean.FALSE, Boolean.TRUE);
		setModal(true);
	}
	
	/**
	 * Projects the image.
	 * 
	 * @param datasets    The collection of datasets where to store the 
	 *                    projected image.
	 * @param name        The name of the projected image.
	 * @param allChannels Pass <code>true</code> to project all channels,
	 *                    <code>false</code> to project the active channels.
	 */
	void project(List<DatasetData> datasets, String name, boolean allChannels)
	{
		fillProjectionRef();
		ref.setAllChannels(allChannels);
		ref.setDatasets(datasets);
		ref.setImageName(name);
		enableButtons(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		progressBar.setVisible(true);
		statusLabel.setText("Projecting...");
		firePropertyChange(PROJECTION_PROPERTY, null, ref);
		setModal(true);
	}
	
	/**
	 * Sets the containers containing the image.
	 * 
	 * @param containers The value to set.
	 */
	public void setContainers(Collection containers)
	{
		enableButtons(false);
		ProjectionSavingDialog d = new ProjectionSavingDialog(this, containers);
		UIUtilities.centerAndShow(this, d);
	}
	
	/**
	 * Sets the projected image.
	 * 
	 * @param image The image to set.
	 */
	public void setProjectedImage(BufferedImage image)
	{
		enableButtons(true);
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		if (image == null) return;
		progressBar.setVisible(false);
		statusLabel.setText("");
		uiDelegate.setProjectedImage(image);
		setModal(false);
	}
	
}
