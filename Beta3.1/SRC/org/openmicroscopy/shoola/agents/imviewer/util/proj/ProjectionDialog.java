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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.util.ChannelButton;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.slider.TextualTwoKnobsSlider;
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
	
	/** Bound property indicating to close the window. */
	public static final String 		CLOSE_DIALOG_PROPERTY = "closeDialog";
	
    /** Dimension of the box between the channel buttons. */
    private static final Dimension	VBOX = new Dimension(1, 10);
   
    
	/** The type of projections supported. */
	private static final Map<Integer, String>	PROJECTIONS;
	
	/** The maximum number of z-sections. */
	private int            		  	maxZ;
	
	/** The maximum number of timepoints. */
	private int            		  	maxT;
    
	/** The pixels type of the original image. */
	private String				  	pixelsType;
	
	/** Component hosting a two knobs slider and text field. */
	private TextualTwoKnobsSlider 	textualSlider;
	
	/** The type of projection. */
	private Map<Integer, Integer> 	projectionType;

	/** Project the selected z-sections for the currently selected timepoint. */
	private JButton				  	previewButton;
	
	/** Project the selected z-sections of the whole image. */
	private JButton				   	projectionButton;
	
	 /** The bar notifying the user for the data retrieval progress. */
    private JProgressBar           	progressBar;
    
    /** The reference object hosting the parameters used to project. */
    private ProjectionRef		   	ref;
    
    /** The label displaying the status. */
    private JLabel				   	statusLabel;
    
    /** The type of supported projections. */
    private JComboBox			   	types;
    
    /** The UI delegate. */
    private ProjectionUI           	uiDelegate;
    
    /** Sets the stepping for the mapping. */
    private JSpinner			   	frequency;
    
    /** The name of the image. */
    private String 					imageName;
    
	/** 
	 * Set to <code>true</code> if the rendering settings of
	 * the original image are applied to the new one.
	 */
	private boolean			  		applySettings;
	
    /** One  {@link ChannelButton} per channel. */
    private List<ChannelButton>		channelButtons;
    
    /** Flag indicating that a preview has been done. */
    private boolean					preview;
    
    /** Reference to the control. */
	private ProjectionDialogControl	controller;
	
	static {
		PROJECTIONS = new LinkedHashMap<Integer, String>();
		PROJECTIONS.put(ImViewer.MAX_INTENSITY, "Maximum Intensity");
		PROJECTIONS.put(ImViewer.MEAN_INTENSITY, "Mean Intensity");
		PROJECTIONS.put(ImViewer.SUM_INTENSITY, "Sum Intensity");
	}

	/** Collects and stores the parameters used for projection. */
	private void fillProjectionRef()
	{
		if (ref == null) ref = new ProjectionRef();
		ref.setZInterval(textualSlider.getStartValue()-1, 
						textualSlider.getEndValue()-1);
		int value = (Integer) frequency.getValue();
		ref.setStepping(value);
		int index = types.getSelectedIndex();
		ref.setType(projectionType.get(index));
		Iterator<ChannelButton> i = channelButtons.iterator();
		ChannelButton button;
		List<Integer> channels = new ArrayList<Integer>();
		while (i.hasNext()) {
			button = i.next();
			if (button.isSelected()) 
				channels.add(button.getChannelIndex());
		}
		ref.setChannels(channels);
	}
	
	/** 
	 * Initializes the components composing the display. 
	 * 
	 * @param background  The background color.
	 */
	private void initComponents(Color background)
	{
		channelButtons = new ArrayList<ChannelButton>();
		frequency = new JSpinner(new SpinnerNumberModel(1, 1, maxZ, 1));
		textualSlider = new TextualTwoKnobsSlider(1, maxZ);
		textualSlider.setSliderLabelText("Slice: ");
		textualSlider.layoutComponents();
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
        
        String[] names = new String[PROJECTIONS.size()];
        int index = 0;
        Iterator<Integer> i = PROJECTIONS.keySet().iterator();
        projectionType = new HashMap<Integer, Integer>();
        int j;
        while (i.hasNext()) {
			j = i.next();
			projectionType.put(index, j);
			names[index] = PROJECTIONS.get(j);
			index++;
		}
        types = new JComboBox(names);
        getRootPane().setDefaultButton(previewButton);
        
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) { 
				super.windowClosing(e);
				firePropertyChange(CLOSE_DIALOG_PROPERTY, Boolean.FALSE, 
						Boolean.TRUE);
			}
		});
	}
	
	/** 
	 * Builds and lays out the component controlling the slices selection. 
	 * 
	 * @return See above.
	 */
	private JPanel buildControlComponent()
	{
		JPanel content = new JPanel();
		double size[][] =
        {{TableLayout.PREFERRED, TableLayout.PREFERRED},  // Columns
         {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}; // Rows
		content.setLayout(new TableLayout(size));
		content.add(new JLabel("Projection: "), "0, 0");
		content.add(types, "1, 0");
		content.add(new JLabel("Every n-th slice: "), "0, 1");
		content.add(UIUtilities.buildComponentPanel(frequency), "1, 1, l, c");
		
		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
		controls.add(textualSlider);
		controls.add(Box.createHorizontalStrut(5));
		controls.add(content);
		return controls;
	}
	
    /**
     * Creates a UI component hosting the {@link ChannelButton}s.
     * 
     * @return See above.
     */
    private JComponent buildChannelsPane()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        ChannelButton button;
        Iterator<ChannelButton> i = channelButtons.iterator();
        while (i.hasNext()) {
			button = i.next();
			button.addPropertyChangeListener(controller);
			p.add(button);
            p.add(Box.createRigidArea(VBOX));
		}
        if (channelButtons.size() > 10) 
        	return UIUtilities.buildComponentPanelCenter(new JScrollPane(p));
        return UIUtilities.buildComponentPanelCenter(p);
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
    	//bar.add(buildChannelsPane());
    	
    	JPanel content = new JPanel();
    	double[][] tl = {{TableLayout.PREFERRED}, 
				{TableLayout.PREFERRED, TableLayout.FILL}};
		content.setLayout(new TableLayout(tl));
		content.add(bar, "0, 0");
		content.add(buildChannelsPane(), "0, 1");
    	return content;
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
	 * @param maxZ        The number of optical sections.
	 * @param maxT		  The number of timepoints.
	 * @param pixelsType  The pixels type of the original image.
	 * @param background  The background color of the canvas.
	 * @param imageName	  The name of the original image.
	 * @param imageWidth  The width of the original image.
	 * @param imageHeight The width of the original image.
	 */
	public ProjectionDialog(JFrame owner, int maxZ, int maxT, String pixelsType, 
			             Color background, String imageName)
	{
		super(owner);
		controller = new ProjectionDialogControl(this);
		this.maxZ = maxZ;
		this.maxT = maxT;
		this.pixelsType = pixelsType;
		this.imageName = imageName;
		initComponents(background);
	}
	
	/**
	 * 
	 * @param imageWidth     The width of the original image.
	 * @param imageHeight    The width of the original image.
	 * @param channelButtons
	 */
	public void initialize(int imageWidth, int imageHeight, 
							List<ChannelButton> channelButtons)
	{
		if (channelButtons != null) 
			this.channelButtons = channelButtons;
		buildGUI();
		Dimension d = new Dimension(imageWidth, imageHeight);
		uiDelegate.setPreferredSize(d);
		uiDelegate.setSize(d);
		pack();
	}
	
	/**
	 * Returns the number of time point.
	 * 
	 * @return See above.
	 */
	int getMaxT() { return maxT; }
	
	/**
	 * Returns the selected algorithm.
	 * 
	 * @return See above.
	 */
	int getSelectedAlgorithm()
	{ 
		return projectionType.get(types.getSelectedIndex());
	}

	/** 
	 * Returns the pixels type of the original image.
	 * 
	 * @return See above.
	 */
	String getPixelsType() { return pixelsType; }
	
	/** Projects and previews. */
	void preview()
	{
		preview = true;
		enableButtons(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		fillProjectionRef();
		progressBar.setVisible(true);
		statusLabel.setText("Projecting...");
		firePropertyChange(PROJECTION_PREVIEW_PROPERTY, null, ref);
	}
	
	/**
	 * Updates the controls when a new channel is selected or deselected.
	 * 
	 * @param index The index of the channel.
	 * @param value Pass <code>true</code> to select the channel, 
	 * 				<code>false</code> otherwise.
	 */
	void selectChannel(int index, boolean value)
	{
		Iterator<ChannelButton> i = channelButtons.iterator();
		ChannelButton button;
		while (i.hasNext()) {
			button = i.next();
			if (button.getChannelIndex() == index)
				button.setSelected(value);
		}
		if (preview) preview();
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
	 * @param datasets    	The collection of datasets where to store the 
	 *                    	projected image.
	 * @param name        	The name of the projected image.
	 * @param allChannels 	Pass <code>true</code> to project all channels,
	 *                    	<code>false</code> to project the active channels.
	 * @param startT	  	The first timepoint to project.
	 * @param endT        	The last timepoint to project.
	 * @param pixelsType  	The pixels type of the destination set.
	 * @param applySettings Pass <code>true</code> to set the rendering settings
	 * 						of the original image to the new pixels set,
	 * 						<code>false</code> otherwise.
	 */
	void project(List<DatasetData> datasets, String name, int startT, int endT, 
			String pixelsType, boolean applySettings)
	{
		fillProjectionRef();
		ref.setImageDescription("Projection type: "+
				PROJECTIONS.get(ref.getType()));
		ref.setDatasets(datasets);
		ref.setImageName(name);
		ref.setTInterval(startT, endT);
		ref.setPixelsType(pixelsType);
		this.applySettings = applySettings;
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
		setModal(false);
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
	
	/**
	 * Returns <code>true</code> if the settings of the original image are set
	 * to the new pixels set, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isApplySettings() { return applySettings; }
	
	/**
	 * Callback used by data loaders to provide the viewer with feedback about
	 * the data retrieval.
	 * 
	 * @param description   Textual description of the ongoing operation.
	 * @param hide          Pass <code>true</code> to hide the progress bar,
	 * 						<code>false</code> to show it.
	 */
	public void setStatus(String description, boolean hide)
	{
		statusLabel.setText(description);
		progressBar.setVisible(!hide);
	}
	
}
