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
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

//Third-party libraries
import org.jdesktop.swingx.JXBusyLabel;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.agents.imviewer.actions.ROIToolAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.UserAction;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.LookupNames;
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
	
    /** The compression option. */
    private static final String[] 				compression;

    /** The compression option. */
    private static final String[] 				compressionPartial;
    
    static {
    	compression = new String[3];
    	compression[UNCOMPRESSED] = "None";
    	compression[MEDIUM] = "Medium";
    	compression[LOW] = "High";
    	
    	compressionPartial = new String[2];
    	compressionPartial[MEDIUM-1] = "Medium";
    	compressionPartial[LOW-1] = "High";
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
    
    /** Button used to show or hide the renderer. */
    private JToggleButton			historyButton;

    /** Box used to present the compression selected. */
    private JComboBox				compressionBox;

    /** Button to paste the rendering settings. */
	private JButton					pasteButton;
	
	/** Button to launch the measurement tool. */
	private JButton					measurementButton;
	
	/** Indicates the launching of the measurement tool. */
	private JXBusyLabel				measurementLabel;
	
	/** Indicates the loading progress. */
	private JXBusyLabel				busyLabel;

	/** The index of the movie icon. */
	private static final int		MEASUREMENT_INDEX = 13;
	
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
        //bar.add(historyButton);
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
        //bar.add(button);
        button = new JButton(controller.getAction(
        					ImViewerControl.SET_ORIGINAL_RND_SETTINGS));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);
        button = new JButton(
    			controller.getAction(ImViewerControl.SAVE_RND_SETTINGS));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);
        bar.add(new JSeparator(JSeparator.VERTICAL));
        button = new JButton(
        			controller.getAction(ImViewerControl.MOVIE));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);    
        button =  new JButton(controller.getAction(ImViewerControl.LENS));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button); 
        bar.add(new JSeparator(JSeparator.VERTICAL));
        Action a = controller.getAction(ImViewerControl.MEASUREMENT_TOOL);
        measurementButton = new JButton(a);
        measurementButton.addMouseListener((ROIToolAction) a);
        UIUtilities.unifiedButtonLookAndFeel(measurementButton);
        bar.add(measurementButton); 
        Icon icon = measurementButton.getIcon();
        int h = UIUtilities.DEFAULT_ICON_HEIGHT;
		int w = UIUtilities.DEFAULT_ICON_WIDTH;
		if (icon != null) {
			if (icon.getIconHeight() > h) h = icon.getIconHeight();
			if (icon.getIconWidth() > w) w = icon.getIconWidth();
		}
        
        bar.add(new JSeparator(JSeparator.VERTICAL));
        button = new JButton(controller.getAction(ImViewerControl.SAVE));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);
        button = new JButton(controller.getAction(
        		ImViewerControl.CREATE_MOVIE));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);
        button = new JButton(controller.getAction(
        		ImViewerControl.EXPORT));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);
        
        
        a = controller.getAction(ImViewerControl.USER);
        button = new JButton(a);
        button.addMouseListener((UserAction) a);
        UIUtilities.unifiedButtonLookAndFeel(button);
        //bar.add(button);
        
        Dimension d = new Dimension(w, h);
        busyLabel = new JXBusyLabel(d);
    	busyLabel.setEnabled(true);
    	busyLabel.setVisible(false);
    	
    	measurementLabel = new JXBusyLabel(d);
    	measurementLabel.setToolTipText("Loading Measurements. Please wait.");
    	measurementLabel.setEnabled(true);
    	measurementLabel.setVisible(true);
    }
    
    /** Initializes the components composing this tool bar. */
    private void initComponents()
    {
    	compressionBox = EditorUtil.createComboBox(compression, 0, 
    			getBackground());
    	compressionBox.setBackground(getBackground());
        createControlsBar();
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
    	removeAll();
    	JPanel p = new JPanel();
    	JLabel l = new JLabel("Compression:");
    	p.add(l);
    	p.add(compressionBox);
    	
    	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    	setBorder(null);
    	JPanel bars = new JPanel();
    	bars.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    	bars.setBorder(null);
    	bars.add(bar);
    	if (p != null) bars.add(p);
    	add(UIUtilities.buildComponentPanel(bars));
    	add(UIUtilities.buildComponentPanelRight(busyLabel));
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
		int compression = ImViewerFactory.getCompressionLevel();
		int value = (Integer) 
			ImViewerAgent.getRegistry().lookup(LookupNames.CONNECTION_SPEED);
		int setUp = view.convertCompressionLevel(value);
		if (compression != setUp) compression = setUp;
		int index = view.convertCompressionLevel();
		if (view.isBigImage()) {
			compressionBox = EditorUtil.createComboBox(compressionPartial, 0, 
	    			getBackground());
	    	compressionBox.setBackground(getBackground());
	    	if (compression == MEDIUM || compression == LOW)
				index = compression-1;
	    	if (compression == UNCOMPRESSED)
	    		index = MEDIUM-1;
	    	compressionBox.setSelectedIndex(index);
		} else {
			if (compression >= UNCOMPRESSED && compression <= LOW)
				index = compression;
			compressionBox.setSelectedIndex(index);
		}
		
		
		compressionBox.addActionListener(
    			controller.getAction(ImViewerControl.COMPRESSION));
    	buildGUI(); 
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
	 * Replaces the component in the display either icon or busy label
	 * depending on the passed parameter.
	 * 
	 * @param b Pass <code>true</code> to indicate the creation,
	 * 			<code>false</code> to indicate that the creation is done.
	 */
	void setMeasurementLaunchingStatus(boolean b)
	{ 
		if (bar != null) {
			measurementLabel.setBusy(b);
    		if (!b) {
    			bar.remove(measurementLabel);
    			bar.add(measurementButton, MEASUREMENT_INDEX);
        	} else {
        		bar.remove(measurementButton);
        		bar.add(measurementLabel, MEASUREMENT_INDEX);
        	}
    		bar.revalidate();
    		bar.repaint();
    	} 
	}
	
    /**
     * Sets the enabled flag of the components.
     * 
     * @param b Pass <code>true</code> to enable the components, 
     * 			<code>false</code> otherwise.
     */
    void onStateChange(boolean b)
    {
    	if (compressionBox != null) compressionBox.setEnabled(b);
	}
    
    /**
     * Returns the currently selected index of the compression level.
     * 
     * @return See above.
     */
	int getUICompressionLevel()
	{ 
		int index = compressionBox.getSelectedIndex();
		if (!view.isBigImage()) return index;
		return index++;
	}
	
}
