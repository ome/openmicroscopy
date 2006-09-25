/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.ColourPicker
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.ui.colourpicker;

//Java imports
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JDialog;

//Third-party libraries

//Application-internal dependencies

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
   
    /** Bounds property indicating that a new color is selected. */
    public static final String COLOUR_PROPERTY = "colour";
    
    /** Reference to the model. */
    private RGBModel model;
    
	/** Creates a new instance. 
     * 
     * @param color     The original color. If <code>null</code>, sets to 
     *                  the default color.
     */
	public ColourPicker(Color color)
	{
	    super();
		setTitle("Colour Picker Window");
		//setSize(250, 350);
        setModal(true);
        setResizable(false);
        float[] vals = new float[4];
        vals = color.getComponents(vals);
        model = new RGBModel(vals[0], vals[1], vals[2], vals[3]);
        RGBControl control = new RGBControl(model);
        
        TabbedPaneUI tabbedPane = new TabbedPaneUI(this, control);
        
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 80;
		gbc.weighty = 30;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
			
		this.getContentPane().add(tabbedPane, gbc);
        pack();
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
        Color original = model.getOriginalColor();
        if ((original.getRed() == c.getRed()) && 
                (original.getGreen() == c.getGreen()) &&
                (original.getBlue() == c.getBlue()) &&
                (original.getAlpha() == c.getAlpha())) return;
        firePropertyChange(COLOUR_PROPERTY, model.getOriginalColor(), c);
        cancel();
    }
	
}
