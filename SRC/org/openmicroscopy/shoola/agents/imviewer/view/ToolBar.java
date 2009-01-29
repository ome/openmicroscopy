/*
 * org.openmicroscopy.shoola.agents.imviewer.view.ToolBar
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.imviewer.view;


//Java imports
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.jdesktop.swingx.JXBusyLabel;
import org.openmicroscopy.shoola.agents.imviewer.actions.UserAction;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.UIUtilities;


/** 
 * Presents the variable drawing controls.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class ToolBar
    extends JPanel
    implements ActionListener, ChangeListener
{

	/** Flag to indicate that the image is not compressed. */
	static final int				UNCOMPRESSED = 0;
	
	/** 
	 * Flag to indicate that the image is not compressed using a
	 * medium Level of compression. 
	 */
	static final int				MEDIUM = 1;
	
	/** 
	 * Flag to indicate that the image is not compressed using a
	 * low Level of compression. 
	 */
	static final int				LOW = 2;
	
	/** Horizontal space between the buttons. */
	private static final Dimension	H_SPACE = new Dimension(2, 5);
	
    /** Default text describing the compression check box.  */
    private static final String		PROJECTION_DESCRIPTION = 
    				"Select the type of projection.";
    
    /** The compression option. */
    private static final String[] 				compression;
    
	/** The type of projections supported. */
	private static final Map<Integer, String>	projections;
	
    static {
    	compression = new String[3];
    	compression[UNCOMPRESSED] = "No compression";
    	compression[MEDIUM] = "Medium compression";
    	compression[LOW] = "High compression";
    	projections = new LinkedHashMap<Integer, String>();
    	projections.put(ImViewer.MAX_INTENSITY, "Maximum Intensity");
    	projections.put(ImViewer.MEAN_INTENSITY, "Mean Intensity");
    	//projections.put(ImViewer.SUM_INTENSITY, "Sum Intensity");
    }
    
    /** Reference to the Control. */
    private ImViewerControl 		controller;
    
    /** Reference to the View. */
    private ImViewerUI				view;
    
    /** The tool bar hosting the controls. */
    private JToolBar        		bar;
    
    /** Button used to show or hide the renderer. */
    private JToggleButton			rndButton;
    
    /** Button used to show or hide the renderer. */
    private JToggleButton			metadataButton;
    
    /** Button used to show or hide the history of rendering changes. */
    private JToggleButton			historyButton;
    
    /** Box used to present the compression selected. */
    private JComboBox				compressionBox;

    /** Button to paste the rendering settings. */
	private JButton					pasteButton;
	
	/** The tool bar hosting the controls for the porjection. */
	private JPanel					projectionBar;
	
    /** The type of supported projections. */
    private JComboBox				projectionTypesBox;
    
	/** The type of projection. */
	private Map<Integer, Integer> 	projectionTypes;

    /** Sets the stepping for the mapping. */
    private JSpinner			   	projectionFrequency;

	/** Indicates the loading progress. */
	private JXBusyLabel				busyLabel;

    /** Helper method to create the tool bar hosting the buttons. */
    private void createControlsBar()
    {
        bar = new JToolBar();
        bar.setFloatable(false);
        bar.setRollover(true);
        bar.setBorder(null);
        pasteButton = new JButton(controller.getAction(
        		ImViewerControl.PASTE_RND_SETTINGS));
		
        UIUtilities.unifiedButtonLookAndFeel(pasteButton);
        pasteButton.setEnabled(view.hasSettingsToPaste());
        rndButton = new JToggleButton();
        rndButton.setSelected(view.isRendererShown());
        rndButton.setAction(controller.getAction(ImViewerControl.RENDERER));
        bar.add(rndButton);
        
        metadataButton = new JToggleButton();
        metadataButton.setSelected(view.isRendererShown());
        metadataButton.setAction(controller.getAction(
        		ImViewerControl.METADATA));
        bar.add(metadataButton);
        
        historyButton = new JToggleButton();
        historyButton.setSelected(view.isHistoryShown());
        historyButton.setAction(controller.getAction(ImViewerControl.HISTORY));
        bar.add(historyButton);
        bar.add(Box.createRigidArea(H_SPACE));
        bar.add(new JSeparator(JSeparator.VERTICAL));
        bar.add(Box.createRigidArea(H_SPACE));
        JButton button = new JButton(
        			controller.getAction(ImViewerControl.COPY_RND_SETTINGS));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);    
        bar.add(pasteButton);    
        button = new JButton(
    			controller.getAction(ImViewerControl.RESET_RND_SETTINGS));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);
        button = new JButton(controller.getAction(
        					ImViewerControl.SET_ORIGINAL_RND_SETTINGS));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);
        button = new JButton(
    			controller.getAction(ImViewerControl.SAVE_RND_SETTINGS));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);
        bar.add(new JSeparator(JSeparator.VERTICAL));
        button =  new JButton(
        			controller.getAction(ImViewerControl.MOVIE));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);    
        button =  new JButton(controller.getAction(ImViewerControl.LENS));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);  
        bar.add(new JSeparator(JSeparator.VERTICAL));
        button = new JButton(
        		controller.getAction(ImViewerControl.MEASUREMENT_TOOL));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button); 
        bar.add(new JSeparator(JSeparator.VERTICAL));
        button = new JButton(controller.getAction(ImViewerControl.SAVE));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);
        UserAction a = (UserAction) controller.getAction(ImViewerControl.USER);
        button = new JButton(a);
        button.addMouseListener(a);
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);
        
        busyLabel = new JXBusyLabel();
    	busyLabel.setEnabled(true);
    	busyLabel.setVisible(false);
    }
    
    /** Initializes the projection bar. */
    private void createProjectionBar()
    {	
    	String[] names = new String[projections.size()];
        int index = 0;
        Iterator<Integer> i = projections.keySet().iterator();
        projectionTypes = new HashMap<Integer, Integer>();
        int j;
        while (i.hasNext()) {
			j = i.next();
			projectionTypes.put(index, j);
			names[index] = projections.get(j);
			index++;
		}
        projectionTypesBox = EditorUtil.createComboBox(names, 0, 
        		getBackground());
        projectionTypesBox.setToolTipText(PROJECTION_DESCRIPTION);
        projectionTypesBox.addActionListener(this);
    }
    
    /** Initializes the components composing this tool bar. */
    private void initComponents()
    {
    	compressionBox = EditorUtil.createComboBox(compression, 0, 
    			getBackground());
        createControlsBar();
        createProjectionBar();
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
    	setBorder(null);
    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    	JPanel p = new JPanel();
    	p.setBorder(null);
    	p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    	p.add(UIUtilities.buildComponentPanel(bar));
    	p.add(UIUtilities.buildComponentPanelRight(busyLabel));
        add(p);
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param view			Reference to the view. Mustn't be <code>null</code>.
     * @param controller	Reference to the controller. 
     * 						Mustn't be <code>null</code>.
     */
    ToolBar(ImViewerUI view, ImViewerControl controller)
    {
        if (view == null) throw new NullPointerException("No View.");
        if (controller == null) throw new NullPointerException("No Control.");
        this.view = view;
        this.controller = controller;
        initComponents();
    }
    
    /** 
     * This method should be called straight after the metadata and the
     * rendering settings are loaded.
     */
    void buildComponent()
    { 
//    	Retrieve the preferences.
		ViewerPreferences pref = ImViewerFactory.getPreferences();
		if (pref != null) {
			//Action a = controller.getAction(ImViewerControl.RENDERER);
			//rndButton.removeActionListener(a);
	        rndButton.setSelected(pref.isRenderer());
	        //rndButton.setAction(a);
		}
    	
		bar.add(new JSeparator(JSeparator.VERTICAL));
		bar.add(UIUtilities.buildComponentPanel(compressionBox));
		compressionBox.setSelectedIndex(view.convertCompressionLevel());
		compressionBox.addActionListener(
    			controller.getAction(ImViewerControl.COMPRESSION));
		projectionFrequency = new JSpinner(new SpinnerNumberModel(1, 1, 
				view.getMaxZ()+1, 1));
		projectionFrequency.addChangeListener(this);
		JPanel bar = new JPanel();
		bar.setBorder(null);
		bar.add(projectionTypesBox);
		bar.add(new JLabel(" Every n-th slice: "));
		bar.add(projectionFrequency);
		projectionBar = new JPanel();
		projectionBar.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		projectionBar.add(bar);
    	buildGUI(); 
    }
    
    /** 
     * Adds the {@link #projectionBar} when the projection tabbed is 
     * selected.
     */
    void onTabbedSelection()
    {
    	remove(projectionBar);
    	if (view.getTabbedIndex() == ImViewer.PROJECTION_INDEX)
    		add(projectionBar);
    	revalidate();
    	repaint();
    }
    
    /** Selects or deselects the {@link #rndButton}. */
    void displayRenderer()
    { 
    	rndButton.setSelected(view.isRendererShown());
    	metadataButton.setSelected(view.isRendererShown());
    }

    /** Selects or deselects the {@link #historyButton}. */
    void displayHistory() { historyButton.setSelected(view.isHistoryShown()); }
    
	/**
	 * Sets the {@link #pasteButton} enable.
	 * 
	 * @param b Pass <code>true</code> to enable the button, <code>false</code>
	 * 			otherwise.
	 */
	void enablePasteButton(boolean b) { pasteButton.setEnabled(b); }

	/**
	 * Returns the stepping used for the projection.
	 * 
	 * @return See above.
	 */
	int getProjectionStepping()
	{
		return (Integer) projectionFrequency.getValue();
	}

	/**
	 * Returns the type of projection.
	 * 
	 * @return See above.
	 */
	int getProjectionType()
	{
		int index = projectionTypesBox.getSelectedIndex();
		return projectionTypes.get(index);
	}
    
	/**
	 * Returns a textual version of the type of projection.
	 * 
	 * @return See above.
	 */
	String getProjectionTypeName()
	{
		int index = projectionTypesBox.getSelectedIndex();
		return projections.get(index);
	}
	
	/**
     * Sets to <code>true</code> if loading data, to <code>false</code>
     * otherwise.
     * 
     * @param busy 	Pass <code>true</code> while loading data, 
     * 				<code>false</code> otherwise.
     */
    void setStatus(boolean busy)
    {
    	busyLabel.setBusy(busy);
    	busyLabel.setVisible(busy);
    }
    
    /**
     * Sets the enabled flag of the components.
     * 
     * @param b Pass <code>true</code> to enable the components, 
     * 			<code>false</code> otherwise.
     */
    void onStateChange(boolean b)
    {
    	if (compressionBox != null)
    		compressionBox.setEnabled(b);
    	if (projectionTypesBox != null)
    		projectionTypesBox.setEnabled(b);
    	if (projectionFrequency != null)
    		projectionFrequency.setEnabled(b);
	}
    
    /**
     * Returns the currently selected index of the compression level.
     * 
     * @return See above.
     */
	int getUICompressionLevel() { return compressionBox.getSelectedIndex(); }
	
    /**
     * Reacts to the selection of the {@link #compressionBox}.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
	public void actionPerformed(ActionEvent e)
	{
		Object src = e.getSource();
		if (src == projectionTypesBox)
			controller.setProjectionRange(true);
	}

	/**
	 * Reacts to selection in spinner.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		Object src = e.getSource();
		if (src == projectionFrequency)
			controller.setProjectionRange(true);
	}

}
