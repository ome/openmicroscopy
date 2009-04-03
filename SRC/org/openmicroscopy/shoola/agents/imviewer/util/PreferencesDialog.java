/*
 * org.openmicroscopy.shoola.agents.imviewer.util.PreferencesDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.imviewer.util;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.view.ViewerPreferences;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class PreferencesDialog
	extends JDialog
	implements ActionListener
{

	/** Bound property indicating to set the preferences */ 
    public static final String      VIEWER_PREF_PROPERTY = "viewerPref";
    
	/** The default title of the window. */
    private static final String     TITLE = "Viewer Preferences...";
    
    /** Brief description of the dialog purpose. */
    private static final String     TEXT = "Sets the Viewer preferences";
    
    /** The horizontal space between the buttons. */
    private static final Dimension  H_BOX = new Dimension(10, 0);
    
    /** ID for the <code>Cancel</code> action. */
    private static final int		CANCEL = 0;
    
    /** ID for the <code>Apply</code> action. */
    private static final int		APPLY = 1;
    
    /** ID for the <code>Apply</code> action. */
    private static final int		CLEAR = 2;
    
    /** Button to save the changes. */
    private JButton         	finishButton;
    
    /** Button to close the window without saving. */
    private JButton         	cancelButton;
    
    /** Button to clear the preferences and closes the window. */
    private JButton         	clearButton;
  
    /** Box used to select the window bounds. */
    private JCheckBox			windowBounds;
    
    /** Box used to select the renderer. */
    private JCheckBox			rnd;
    
    /** Box used to select the history. */
    private JCheckBox			history;
    
    /** Box used to select the zoom factor. */
    private JCheckBox			zoom;
    
    /** Box used to select the scale bar. */
    private JCheckBox			scaleBar;
    
    /** Box used to select the background color. */
    private JCheckBox			bgColor;
    
    /** 
     * Initializes the component composing the display. 
     * 
     * @param preferences The viewer preferences if any.
     */
    private void initComponents(ViewerPreferences preferences)
    {
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand(""+CANCEL);
        finishButton = new JButton("Apply");
        finishButton.addActionListener(this);
        finishButton.setActionCommand(""+APPLY);
        clearButton = new JButton("Clear");
        clearButton.addActionListener(this);
        clearButton.setActionCommand(""+CLEAR);
        getRootPane().setDefaultButton(finishButton);
        
        windowBounds = new JCheckBox(ViewerPreferences.WINDOWS_BOUNDS);
        rnd = new JCheckBox(ViewerPreferences.RENDERER);
        history = new JCheckBox(ViewerPreferences.HISTORY);
        zoom = new JCheckBox(ViewerPreferences.ZOOM_FACTOR);
        scaleBar = new JCheckBox(ViewerPreferences.SCALE_BAR);
        bgColor = new JCheckBox(ViewerPreferences.BG_COLOR);
        if (preferences != null) {
        	rnd.setSelected(preferences.isFieldSelected(
        					ViewerPreferences.RENDERER));
        	history.setSelected(preferences.isFieldSelected(
							ViewerPreferences.HISTORY));
        	zoom.setSelected(preferences.isFieldSelected(
					ViewerPreferences.ZOOM_FACTOR));
        	scaleBar.setSelected(preferences.isFieldSelected(
					ViewerPreferences.SCALE_BAR));
        	bgColor.setSelected(preferences.isFieldSelected(
					ViewerPreferences.BG_COLOR));
        }
    }
    
    /** Closes and disposes. */
    private void close()
    {
        setVisible(false);
        dispose();
    }
    
    /** Sets the preferences. */
    private void save()
    {
    	Map<String, Boolean> m = new HashMap<String, Boolean>();
    	boolean b = false;
    	if (windowBounds.isSelected()) b = true;
    	m.put(ViewerPreferences.WINDOWS_BOUNDS, windowBounds.isSelected());
    	if (rnd.isSelected()) b = true;
    	m.put(ViewerPreferences.RENDERER, rnd.isSelected());
    	if (history.isSelected()) b = true;
    	m.put(ViewerPreferences.HISTORY, history.isSelected());
    	if (zoom.isSelected()) b = true;
    	m.put(ViewerPreferences.ZOOM_FACTOR, zoom.isSelected());
    	if (scaleBar.isSelected()) b = true;
    	m.put(ViewerPreferences.SCALE_BAR, scaleBar.isSelected());
    	if (bgColor.isSelected()) b = true;
    	m.put(ViewerPreferences.BG_COLOR, bgColor.isSelected());
    	
    	if (!b) m = null;
        firePropertyChange(VIEWER_PREF_PROPERTY, null, m);
        close();
    }

    /** Sets the preferences to <code>null</code>. */
    private void clear()
    {
        firePropertyChange(VIEWER_PREF_PROPERTY, "", null);
        close();
    }
    
    /** Sets the properties of this window. */
    private void setDialogProperties()
    {
        setModal(true);
        setResizable(true);
        setTitle(TITLE);
    }
    
    /**
     *  Builds and lays out the tool bar. 
     *  
     * @return See above.
     */
    private JPanel buildToolBar()
    {
        JPanel toolBar = new JPanel();
        //toolBar.setBorder(BorderFactory.createEtchedBorder());
        toolBar.add(cancelButton);
        toolBar.add(Box.createRigidArea(H_BOX));
        toolBar.add(clearButton);
        toolBar.add(Box.createRigidArea(H_BOX));
        toolBar.add(finishButton);
        return toolBar;
    }
    
    /**
     * Builds and lays out the body displaying the preferences.
     * 
     * @return See above.
     */
    private JPanel buildBody()
    {
    	JPanel body = new JPanel();
    	body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
    	//body.add(windowBounds);
    	body.add(rnd);
    	body.add(history);
    	body.add(zoom);
    	body.add(scaleBar);
    	body.add(bgColor);
    	return body;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        Container c = getContentPane();
        IconManager icons = IconManager.getInstance();
        TitlePanel tp = new TitlePanel(TITLE, TEXT, 
        							icons.getIcon(IconManager.PREFERENCES_48));
        c.add(tp, BorderLayout.NORTH);
        c.add(buildBody(), BorderLayout.CENTER);
        c.add(UIUtilities.buildComponentPanelRight(buildToolBar()),
                BorderLayout.SOUTH);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param owner	The owner of the frame.
     * @param pref  The preferences to update if any.
     */
    public PreferencesDialog(JFrame owner, ViewerPreferences pref)
    {
    	 super(owner);
    	 setDialogProperties();
    	 initComponents(pref);
    	 buildGUI();
    	 pack();
    }
    
    /** 
     * Handles the <code>Cancel</code> or <code>Apply</code> action
     * @see ActionListener#actionPerformed(ActionEvent)
     */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CANCEL:
				close();
				break;
			case APPLY:
				save();
				break;
			case CLEAR:
				clear();
		}
	}
    
}
