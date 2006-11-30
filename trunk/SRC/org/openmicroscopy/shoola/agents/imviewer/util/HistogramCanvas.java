/*
 * org.openmicroscopy.shoola.agents.imviewer.util.HistogramCanvas
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

package org.openmicroscopy.shoola.agents.imviewer.util;


//Java imports
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsStatsEntry;

/** 
 * The canvas displaying the histogram.
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
class HistogramCanvas
    extends JPanel
{

    /** The color of the background. */
    private static final Color  BACKGROUND = Color.BLACK;
    
    /** The color of the axis. */
    private static final Color  AXIS = Color.GRAY;
    
    /** The color of the bins. */
    private static final Color  BIN =  new Color(0, 0, 255, 150);;
    
    /** Color of the layer painted on top the selected area. */
    private static final Color  LAYER = new Color(192, 192, 192, 90);
    
    /** The data to paint. */
    private PixelsStatsEntry[] stats;
    
    /**
     * Creates a new instance.
     *
     */
    HistogramCanvas()
    {
        setBackground(Color.BLACK);
    }
    
    /**
     * Sets the data to display.
     * 
     * @param stats The data to display
     */
    void setBinData(PixelsStatsEntry[] stats)
    {
        if (stats == null) throw new IllegalArgumentException("No data to " +
                "display.");
        this.stats = stats;
        repaint();
    }
    
    /** 
     * Overriden to paint the histogram. 
     * @see JPanel#paintComponent(Graphics)
     */ 
    public void paintComponent(Graphics g)
    {
        
    }
}
