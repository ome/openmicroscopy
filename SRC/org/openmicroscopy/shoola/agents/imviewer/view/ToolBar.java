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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.actions.UserAction;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.PixelsData;


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
    implements ActionListener
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
    private static final String		COMPRESSED_DESCRIPTION = 
    				"View your image has umcompressed or select " +
    				"the desired compression quality.";
    
    /** The compression option. */
    private static final String[] compression;
    
    static {
    	compression = new String[3];
    	compression[UNCOMPRESSED] = "No compression";
    	compression[MEDIUM] = "Medium compression";
    	compression[LOW] = "High compression";
    }
    
    /** Reference to the Control. */
    private ImViewerControl controller;
    
    /** Reference to the View. */
    private ImViewerUI		view;
    
    /** The tool bar hosting the controls. */
    private JToolBar        bar;
    
    /** Button used to show or hide the renderer. */
    private JToggleButton	rndButton;
    
    /** Button used to show or hide the history of rendering changes. */
    private JToggleButton	historyButton;
    
    /** Box used to present the compression selected. */
    private JComboBox		compressionBox;

    /** Box displaying the pixels sets associated to the image.*/
    private JComboBox		pixelsBox;
    
    /** Button to paste the rendering settings. */
	private JButton			pasteButton;
	
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
        button = new JButton(
        			controller.getAction(ImViewerControl.IMAGE_DETAILS));
        UIUtilities.unifiedButtonLookAndFeel(button);
        //bar.add(button);
        UserAction a = (UserAction) controller.getAction(ImViewerControl.USER);
        button = new JButton(a);
        button.addMouseListener(a);
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);
       
    }
    
    /** Initializes the components composing this tool bar. */
    private void initComponents()
    {
    	List<PixelsData> pixelsSets = view.getPixelsSets();
    	if (pixelsSets != null && pixelsSets.size() > 1) {
    		Iterator<PixelsData> i = pixelsSets.iterator();
            String[] pixelsName = new String[pixelsSets.size()];
            PixelsData data;
            int index = 0;
            int selectedIndex = 0;
            long id = view.getPixelsID();
            while (i.hasNext()) {
    			data = i.next();
    			if (data.getId() == id) selectedIndex = index;
    			pixelsName[index] = "Pixels ID:"+data.getId();
    			index++;
    		}
            pixelsBox = new JComboBox(pixelsName);
            pixelsBox.setSelectedIndex(selectedIndex);
            pixelsBox.addActionListener(this);
    	}
        
    	compressionBox = new JComboBox(compression);
    	compressionBox.setToolTipText(COMPRESSED_DESCRIPTION);
        //compressedBoxsaveOnClose.setSelected(true);
        createControlsBar();
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
    	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(UIUtilities.buildComponentPanel(bar));
        /*
        JButton button = new JButton(
        			controller.getAction(ImViewerControl.SEARCH));
        UIUtilities.unifiedButtonLookAndFeel(button);
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.setRollover(true);
        bar.setBorder(null);
        bar.add(button);
        */
        //add(UIUtilities.buildComponentPanelRight(bar));
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
		bar.add(compressionBox);
		compressionBox.setSelectedIndex(view.getCompressionLevel());
		compressionBox.addActionListener(this);
		if (pixelsBox != null) bar.add(pixelsBox);
    	buildGUI(); 
    }
    
    /** Selects or deselects the {@link #rndButton}. */
    void displayRenderer() { rndButton.setSelected(view.isRendererShown()); }

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
     * Reacts to the selection of the {@link #compressionBox}.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
	public void actionPerformed(ActionEvent e)
	{
		Object src = e.getSource();
		int index;
		if (src == compressionBox) {
			index = compressionBox.getSelectedIndex();
			view.setCompressionLevel(index);
		} else if (src == pixelsBox) {
			index = pixelsBox.getSelectedIndex();
			List<PixelsData> pixelsSet = view.getPixelsSets();
			PixelsData data = pixelsSet.get(index);
			if (data != null) 
				controller.loadRenderingControl(data.getId());
		}
	}
    
}
