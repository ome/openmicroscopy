/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.InfoPane 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.imviewer.browser;



//Java imports
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

//Third-party libraries
import info.clearthought.layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.model.LengthI;
import omero.model.enums.UnitsLength;


/** 
 * Displays information about the displayed image.
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
class InfoPane 
	extends JPanel
{
	
    /** String to represent the micron symbol. */
    private static final String MICRONS = "(in " + LengthI.lookupSymbol(UnitsLength.MICROMETER) + ")";

    /** Identifies the <code>SizeX</code> field. */
    private static final String SIZE_X = "Size X";
    
    /** Identifies the <code>SizeY</code> field. */
    private static final String SIZE_Y = "Size Y";
    
    /** Identifies the <code>PixelSizeX</code> field. */
    private static final String PIXEL_SIZE_X = "Pixel size X "+MICRONS;
    
    /** Identifies the <code>PixelSizeY</code> field. */
    private static final String PIXEL_SIZE_Y = "Pixel size Y "+MICRONS;
    
    /** Identifies the <code>PixelSizeZ</code> field. */
    private static final String PIXEL_SIZE_Z = "Pixel size Z "+MICRONS;
    
	/** Reference to the Model. */
    private BrowserModel	model;
    
    /** Flag indicating that the content has already been added. */
    private boolean			build;
    
    /**
     * Creates a non-editable text field.
     * 
     * @param value The textual value.
     * @return See above.
     */
    private JTextField createTextField(String value)
    {
    	JTextField area = new JTextField(value);
        area.setEditable(false);
        //area.setEnabled(false);
        return area;
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param model	Reference to the Model. Mustn't be <code>null</code>.
     */
	InfoPane(BrowserModel model)
	{
		if (model == null) throw new NullPointerException("No model.");
		this.model = model;
		build = false;
	}
	
	/** Builds and lays out the UI. */
    void buildGUI()
    {
    	if (build) return;
    	build = true;
    	double[][] tl = {{TableLayout.FILL, 5, 100}, 
				{TableLayout.FILL, 5, TableLayout.FILL, 5, TableLayout.FILL, 5,
    		TableLayout.FILL, 5, TableLayout.FILL}};
    	JPanel content = new JPanel();
    	content.setLayout(new TableLayout(tl));
    	JLabel label = UIUtilities.setTextFont(SIZE_X);
    	content.add(label, "0, 0");
    	JTextField area = createTextField(""+model.getMaxX());
    	content.add(area, "2, 0");
    	label = UIUtilities.setTextFont(SIZE_Y);
    	content.add(label, "0, 2");
    	area = createTextField(""+model.getMaxY());
    	content.add(area, "2, 2");
     	label = UIUtilities.setTextFont(PIXEL_SIZE_X);
     	content.add(label, "0, 4");
    	area = createTextField(""+model.getPixelsSizeX());
    	content.add(area, "2, 4");
     	label = UIUtilities.setTextFont(PIXEL_SIZE_Y);
     	content.add(label, "0, 6");
    	area = createTextField(""+model.getPixelsSizeY());
    	content.add(area, "2, 6");
     	label = UIUtilities.setTextFont(PIXEL_SIZE_Z);
     	content.add(label, "0, 8");
    	area = createTextField(""+model.getPixelsSizeZ());
    	content.add(area, "2, 8");
    	setLayout(new FlowLayout(FlowLayout.CENTER));
    	add(content);
    }
    
}
