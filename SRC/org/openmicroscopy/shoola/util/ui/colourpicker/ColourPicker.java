/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.ColourPicker
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

package org.openmicroscopy.shoola.util.ui.colourpicker;

//Java imports
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.NotificationDialog;
import org.openmicroscopy.shoola.util.ui.UIUtilities;


/** 
 * This is the colourpicker which instatiates a dialog. Once the user hits 
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
    public static final String COLOUR_PROPERTY = "colour";
    
    /** The title of the window. */
    private static final String TITLE = "Colour Picker Window";
    
    /** Reference to the model. */
    private RGBModel model;
    
    /** Sets the properties of the window. */
    private void setWindowProperties()
    {
        setTitle(TITLE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setModal(true);
        setResizable(false);
        setAlwaysOnTop(true);
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
        dispose(); 
    }

    /**  Returns the current colour to the user. */
    void accept()
    {
        Color c = model.getColour();
        if (model.isOriginalColor(model.getColour())) return;
        Color original = model.getOriginalColor();
        if ((original.getRed() == c.getRed()) && 
                (original.getGreen() == c.getGreen()) &&
                (original.getBlue() == c.getBlue()) &&
                (original.getAlpha() == c.getAlpha())) return;
        firePropertyChange(COLOUR_PROPERTY, model.getOriginalColor(), c);
        cancel();
    }
    
	/** 
     * Creates a new instance. 
     * 
     * @param owner     The owner of the window.
     * @param color     The original color. If <code>null</code>, sets to 
     *                  the default color.
     */
	public ColourPicker(JFrame owner, Color color)
	{
	    super(owner);
        setWindowProperties();
        float[] vals = new float[4];
        vals = color.getComponents(vals);
        model = new RGBModel(vals[0], vals[1], vals[2], vals[3]);
        RGBControl control = new RGBControl(model);
        
        TabbedPaneUI tabbedPane = new TabbedPaneUI(this, control);
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 80;
		gbc.weighty = 30;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
			
		getContentPane().add(tabbedPane, gbc);
        pack();
	}

}
