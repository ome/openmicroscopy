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
{
    
    /** 
     * Default text indicating to save the rendering settings before
     * closing the window.
     */
    private static final String		SAVE_ON_CLOSE = "Save settings on close";
    
    /** 
     * Default text indicating to save the rendering settings before
     * closing the window.
     */
    private static final String		SAVE_ON_CLOSE_DESCRIPTION = 
    				"Save the rendering settings before closing the viewer.";
    
    /** Reference to the Control. */
    private ImViewerControl controller;
    
    /** Reference to the View. */
    private ImViewerUI		view;
    
    /** The tool bar hosting the controls. */
    private JToolBar        bar;
    
    /** Save rendering settings when closing the viewer. */
    private JCheckBox		saveOnClose;
    
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
        //bar.add(new JSeparator(JSeparator.VERTICAL));
        //bar.add(saveOnClose);
    }
    
    /** Initializes the components composing this tool bar. */
    private void initComponents()
    {
    	saveOnClose = new JCheckBox(SAVE_ON_CLOSE);
        saveOnClose.setToolTipText(SAVE_ON_CLOSE_DESCRIPTION);
        saveOnClose.setSelected(true);
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
    void buildComponent() { buildGUI(); }
    
    /**
     * Returns <code>true</code> if the rendering settings are 
     * saved before closing the viewer, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean saveSettingsOnClose() { return saveOnClose.isSelected(); }
    
    /** Selects or deselects the {@link #rndButton}. */
    void displayRenderer() { rndButton.setSelected(view.isRendererShown()); }
    
}
