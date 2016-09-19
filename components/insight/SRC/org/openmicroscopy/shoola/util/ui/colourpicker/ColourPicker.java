/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.ColourPicker
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.util.ui.colourpicker;

//Java imports
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.NotificationDialog;
import org.openmicroscopy.shoola.util.ui.UIUtilities;


/** 
 * This is the picker which instantiates a dialog. Once the user hits 
 * cancel or accept the dialog will disappear. It contains 3 panels, HSV wheel
 * RGB Sliders and ColourSwatchs. 
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class ColourPicker 
	extends JDialog
{
   
    /** The default with of the component. */
    public static final int     DEFAULT_WIDTH = 240;
    
    /** The default height of the component. */
    public static final int     DEFAULT_HEIGHT = 310;
    
    /** Bounds property indicating that a new color is selected. */
    public static final String ACCEPT_PROPERTY = "colour";
    
    /** Bounds property indicating to cancel the dialog. */
    public static final String CANCEL_PROPERTY = "cancelColourPicker";
    
    /** Bounds property indicating to close the dialog. */
    public static final String CLOSE = "closeColourPicker";
    
    /** The title of the window. */
    private static final String TITLE = "Color Picker Window";
    
    /** The default color. */
    private static final Color DEFAULT_COLOR = Color.red;
    
    /** Reference to the model. */
    private RGBModel model;
    
    /** Reference to the UI. */
    private TabbedPaneUI tabbedPane;
    
    /** Sets the properties of the window. */
    private void setWindowProperties()
    {
        setTitle(TITLE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setModal(true);
        setResizable(false);
        setAlwaysOnTop(true);
    }
    
    /** Attaches the listeners.*/
    private void attachListeners()
    {
    	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) { cancel(); }
		});
    }
    
    /** Notifies the user that an invalid color component has been entered. */
    static void invalidColorValue()
    {
    	IconManager icons = IconManager.getInstance();
		NotificationDialog dialog = new NotificationDialog(
                			"Invalid color component value", 
                			"The value must be in the interval [0, 255].", 
                			icons.getIcon(IconManager.INFO_32));
		dialog.pack();
		UIUtilities.centerAndShow(dialog);
    }
    
    /** Closes and disposes. */
    void cancel()
    {
        setVisible(false);
        firePropertyChange(CANCEL_PROPERTY, Boolean.valueOf(false),
        		Boolean.valueOf(true));
    }

    /** Returns the current colour to the user. */
    void accept() {
        
        ColourObject newColor = new ColourObject();
        ColourObject oldColor = new ColourObject();
        
        newColor.color = model.getColour();
        oldColor.color = model.getOriginalColor();
        
        newColor.lut = model.getLUT();
        oldColor.lut = model.getOriginalLUT();
        
        newColor.revInt = model.getReversetIntensity();
        oldColor.revInt = model.getOriginalReversetIntensity();
        
        newColor.description = tabbedPane.getDescription();
        
        firePropertyChange(ACCEPT_PROPERTY, oldColor, newColor);
        
        setVisible(false);
        firePropertyChange(CLOSE, false, true);
    }
    
    /** Returns the current colour to the user. */
    void preview() {
        ColourObject newColor = new ColourObject();
        ColourObject oldColor = new ColourObject();
        
        newColor.color = model.getColour();
        oldColor.color = model.getOriginalColor();
        
        newColor.lut = model.getLUT();
        oldColor.lut = model.getOriginalLUT();
        
        newColor.revInt = model.getReversetIntensity();
        oldColor.revInt = model.getOriginalReversetIntensity();
        
        newColor.description = tabbedPane.getDescription();
        
        newColor.preview = true;
        
        firePropertyChange(ACCEPT_PROPERTY, oldColor, newColor);
    }
    
    /** Resets the color to the original.*/
    void reset()
    {
        ColourObject oldColor = new ColourObject();
        oldColor.color = model.getOriginalColor();
        oldColor.lut = model.getOriginalLUT();
        oldColor.revInt = model.getOriginalReversetIntensity();
        
    	firePropertyChange(ACCEPT_PROPERTY, null,oldColor);
    }
    
	/** 
     * Creates a new instance. 
     * 
     * @param owner     The owner of the window.
     * @param color     The original color. If <code>null</code>, sets to 
     *                  the default color.
     * @param field		Pass <code>true</code> to add a field, 
     * 					<code>false</code> otherwise.       
	 * @param luts      The selected lookup table
	 * @param revInt    The reverse intensity flag
	 * @param selectedLUT  All available lookup tables
     */
	public ColourPicker(JFrame owner, Color color, boolean field, Collection<String> luts, String selectedLUT, boolean revInt)
	{
	    super(owner);
	    if (color == null) color = DEFAULT_COLOR;
        setWindowProperties();
        float[] vals = new float[4];
        vals = color.getComponents(vals);
        model = new RGBModel(vals[0], vals[1], vals[2], vals[3], selectedLUT, luts, revInt);
        RGBControl control = new RGBControl(model);
        
        tabbedPane = new TabbedPaneUI(this, control, field);
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 80;
		gbc.weighty = 30;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		attachListeners();
		getContentPane().add(tabbedPane, gbc);
        pack();
	}

	/** 
	 * Creates a new instance with color set to <code>red</code>.
	 * 
	 * @param owner The owner of the window.
	 * @param color	The original color. If <code>null</code>, sets to 
	 * 				the default color.
	 */
	public ColourPicker(JFrame owner, Color color)
	{
		this(owner, color, false, null, null, false);
	}
	
    /**
     * Creates a new instance with color set to <code>red</code>.
     * 
     * @param owner
     *            The owner of the window.
     * @param color
     *            The original color. If <code>null</code>, sets to the default
     *            color.
     * @param field
     *            Pass <code>true</code> to add a field, <code>false</code>
     *            otherwise.
     */
    public ColourPicker(JFrame owner, Color color, boolean field) {
        this(owner, color, field, null, null, false);
    }
	
	/** 
     * Creates a new instance with color set to <code>red</code>.
     * 
     * @param owner The owner of the window.
     * @param color The original color. If <code>null</code>, sets to 
     *              the default color.
     * @param luts The available lookup tables
     * @param selectedLUT The selected lookup table
     */
    public ColourPicker(JFrame owner, Color color, Collection<String> luts, String selectedLUT, boolean revInt)
    {
        this(owner, color, false, luts, selectedLUT, revInt);
    }
    
	/** 
	 * Creates a new instance with color set to <code>red</code>.
	 * 
	 * @param owner The owner of the window.
	 */
	public ColourPicker(JFrame owner)
	{
		this(owner, null, false, null, null, false);
	}
	
	/** 
	 * Creates a new instance with color set to <code>red</code>.
	 * 
	 * @param owner  The owner of the window.
	 * @param field	 Pass <code>true</code> to add a field, 
     * 					<code>false</code> otherwise. 
	 */
	public ColourPicker(JFrame owner, boolean field)
	{
		this(owner, null, field, null, null, false);
	}
	
	/**
	 * Sets the description associated to the color.
	 * 
	 * @param description The value to set.
	 */
	public void setColorDescription(String description)
	{
		tabbedPane.setColorDescription(description);
	}
	
	/**
	 * Shows or hides the preview button.
	 * 
	 * @param visible Pass <code>true</code> to show the preview button,
	 * 				  <code>false</code> otherwise.
	 */
	public void setPreviewVisible(boolean visible)
	{
		tabbedPane.setPreviewVisible(visible);
		tabbedPane.repaint();
		pack();
	}

}
