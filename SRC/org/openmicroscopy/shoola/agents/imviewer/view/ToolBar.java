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
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.actions.ClassifyAction;
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
    implements ItemListener
{
    
    /** Default text indicating that the image is compressed. */
    private static final String		COMPRESSED = "Compressed";
    
    /** Default text describing the compression check box.  */
    private static final String		COMPRESSED_DESCRIPTION = 
    				"The image is compressed depending on the connection \n" +
    				" speed. To view an uncompressed image, deselect the \n" +
    				"check box.";
    
    /** Reference to the Control. */
    private ImViewerControl controller;
    
    /** Reference to the View. */
    private ImViewerUI		view;
    
    /** The tool bar hosting the controls. */
    private JToolBar        bar;
    
    /** Selected if the image is compressed by default. */
    private JCheckBox		compressedBox;
    
    /** Button used to show or hide the renderer. */
    private JToggleButton	rndButton;
    
    /** Button displaying the category. */
    private JButton			categoryButton;
    
    /** Helper method to create the tool bar hosting the buttons. */
    private void createControlsBar()
    {
        bar = new JToolBar();
        bar.setFloatable(false);
        bar.setRollover(true);
        bar.setBorder(null);
        rndButton = new JToggleButton();
        rndButton.setSelected(view.isRendererShown());
        rndButton.setAction(controller.getAction(ImViewerControl.RENDERER));
        //UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(rndButton);
        JButton button =  new JButton(
        			controller.getAction(ImViewerControl.MOVIE));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);    
        button =  new JButton(controller.getAction(ImViewerControl.LENS));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);  
        bar.add(new JSeparator(JSeparator.VERTICAL));
        button =  new JButton(controller.getAction(ImViewerControl.ZOOM_IN));
        UIUtilities.unifiedButtonLookAndFeel(button);
        //bar.add(button);    
        button =  new JButton(controller.getAction(ImViewerControl.ZOOM_OUT));
        UIUtilities.unifiedButtonLookAndFeel(button);
        //bar.add(button);  
        button =  new JButton(controller.getAction(ImViewerControl.ZOOM_FIT));
        UIUtilities.unifiedButtonLookAndFeel(button);
        //bar.add(button);  
        //bar.add(new JSeparator(JSeparator.VERTICAL));
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
        bar.add(button);
        button = new JButton(controller.getAction(ImViewerControl.DOWNLOAD));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);  
    }
    
    /** Initializes the components composing this tool bar. */
    private void initComponents()
    {
    	compressedBox = new JCheckBox(COMPRESSED);
    	compressedBox.setToolTipText(COMPRESSED_DESCRIPTION);
        //compressedBoxsaveOnClose.setSelected(true);
        ClassifyAction a = 
			(ClassifyAction) controller.getAction(ImViewerControl.CATEGORY);
        categoryButton = new JButton(a);
        //UIUtilities.unifiedButtonLookAndFeel(categoryButton);
        //categoryButton.setVisible(true);
        categoryButton.addMouseListener(a);
        createControlsBar();
    }

    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
    	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    	JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        p.add(bar);
        add(p);
        add(UIUtilities.buildComponentPanelRight(categoryButton));
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
			Action a = controller.getAction(ImViewerControl.RENDERER);
			//rndButton.removeActionListener(a);
	        rndButton.setSelected(pref.isRenderer());
	        //rndButton.setAction(a);
		}
    	
    	boolean b = view.isImageCompressed();
    	if (b) {
    		bar.add(new JSeparator(JSeparator.VERTICAL));
            bar.add(compressedBox);
        	compressedBox.setSelected(b);
        	compressedBox.addItemListener(this);
    	}
    	buildGUI(); 
    }
    
    /** Selects or deselects the {@link #rndButton}. */
    void displayRenderer() { rndButton.setSelected(view.isRendererShown()); }

    /**
     * Reacts to the selection of the {@link #compressedBox}.
     * @see ItemListener#itemStateChanged(ItemEvent)
     */
	public void itemStateChanged(ItemEvent e)
	{
		view.setImageCompressed(compressedBox.isSelected());
	}
    
}
