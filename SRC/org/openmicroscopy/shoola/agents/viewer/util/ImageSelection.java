/*
 * org.openmicroscopy.shoola.agents.viewer.util.ImageSelection
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.viewer.util;


//Java imports
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class ImageSelection
    extends JPanel
{
    
    /** Selection of line color for the ROI. */
    static final int                    RED = 0;
    static final int                    GREEN = 1;
    static final int                    BLUE = 2;
    static final int                    CYAN = 3;
    static final int                    MAGENTA = 4;
    static final int                    ORANGE = 5;
    static final int                    PINK = 6;
    static final int                    YELLOW = 7;
    
    static final int                    MAX_COLOR = 7;
    
    private static final String[]       selections, selectionOfColors;
    
    static {
        selections = new String[ImageSaver.MAX_TYPE+1];
        selections[ImageSaver.IMAGE] = "Image";
        selections[ImageSaver.PIN_IMAGE] = "Lens image"; 
        selections[ImageSaver.PIN_ON_IMAGE] = "Lens on Image"; 
        selections[ImageSaver.PIN_AND_IMAGE] = "Lens and Image"; 
        selections[ImageSaver.PIN_ON_SIDE_TOP_LEFT] = "Lens in top-left corner";
        selections[ImageSaver.PIN_ON_SIDE_TOP_RIGHT] = "Lens in top-right " +
                                                    "corner";
        selections[ImageSaver.PIN_ON_SIDE_BOTTOM_LEFT] = "Lens in bottom-left " 
                                                    + "corner";
        selections[ImageSaver.PIN_ON_SIDE_BOTTOM_RIGHT] = "Lens in " +
                                                    "bottom-right corner"; 
        selections[ImageSaver.IMAGE_AND_ROI] = "Image and overlay";
        
        selectionOfColors = new String[MAX_COLOR+1];
        selectionOfColors[RED] = "Red";
        selectionOfColors[GREEN] = "Green";
        selectionOfColors[BLUE] = "Blue";
        selectionOfColors[CYAN] = "Cyan";
        selectionOfColors[MAGENTA] = "Magenta";
        selectionOfColors[ORANGE] = "Orange";
        selectionOfColors[PINK] = "Pink";
        selectionOfColors[YELLOW] = "Yellow";
    }

    JComboBox   imageTypes, colors;
    
    JCheckBox   paintingOnOff;
    
    ImageSelection()
    {
        initComponents();
        buildGUI();
    }

/** Initializes the components. */
    private void initComponents()
    {
        imageTypes = new JComboBox(selections);
        imageTypes.setSelectedIndex(ImageSaver.IMAGE);
        colors = new JComboBox(selectionOfColors);
        colors.setToolTipText(
                UIUtilities.formatToolTipText("Color of the lens' area."));
        paintingOnOff = new JCheckBox();
        paintingOnOff.setToolTipText(
                UIUtilities.formatToolTipText("Paint the lens's border."));
        paintingOnOff.setSelected(false);
    }

    private JPanel buildComponent(JPanel p, String txt)
    {
        JPanel result = new JPanel();
        result.setLayout(new BoxLayout(result, BoxLayout.X_AXIS));
        JLabel l = new JLabel(txt);
        result.add(l);
        result.add(p);
        return result;
    }

    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(buildComponent(UIUtilities.buildComponentPanel(imageTypes), 
                            "Images: "));
        add(buildComponent(UIUtilities.buildComponentPanel(colors), 
                            "Color lens' border"));
        add(buildComponent(UIUtilities.buildComponentPanel(paintingOnOff), 
                            "Paint lens' border"));
    }

}

