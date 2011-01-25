/*
 * org.openmicroscopy.shoola.agents.imviewer.actions.ZoomGridAction 
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
package org.openmicroscopy.shoola.agents.imviewer.actions;


//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Sets the magnification factor for the grid view.
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
public class ZoomGridAction
	extends ViewerAction
{
    
    /** The maximum value of the zoom factor. */
    public static final double  MAX_ZOOM_FACTOR = 4*ZoomCmd.INCREMENT;
    
    /** The minimum value of the zoom factor. */
    public static final double  MIN_ZOOM_FACTOR = ZoomCmd.INCREMENT;
    
    /** The default zooming factor. */
    public static final double  DEFAULT_ZOOM_FACTOR = 2*ZoomCmd.INCREMENT;
    
    /** Identifies the <code>0.25</code> zooming factor. */
    public static final int     ZOOM_25 = 0;
    
    /** Identifies the <code>0.50</code> zooming factor. */
    public static final int     ZOOM_50 = 1;

    /** Identifies the <code>0.75</code> zooming factor. */
    public static final int     ZOOM_75 = 2;
    
    /** Identifies the <code>1.0</code> zooming factor. */
    public static final int     ZOOM_100 = 3;
     
    /** The default zooming index. */
    public static final int  	DEFAULT_ZOOM_INDEX = ZOOM_50;
    
    /** The minimum value of zooming index. */
    public static final int  	MIN_ZOOM_INDEX = ZOOM_25;
    
    /** The maximum value of zooming index. */
    public static final int  	MAX_ZOOM_INDEX = ZOOM_100;
    
    /** The number of supported identifiers. */
    private static final int    MAX = 4;
    
    /** The description of the action. */
    private static final String DESCRIPTION = "Zoom in or out the grid image.";

    /** The array containing the actions' name. */
    private static String[]     names;
    
    /** The array containing the zooming factor. */
    private static double[]     factors;
    
    /** Defines the static fields. */
    static {
        factors = new double[MAX+1];
        factors[ZOOM_25] = ZoomCmd.INCREMENT;
        factors[ZOOM_50] = factors[ZOOM_25]+ZoomCmd.INCREMENT;
        factors[ZOOM_75] = factors[ZOOM_50]+ZoomCmd.INCREMENT;
        factors[ZOOM_100] = factors[ZOOM_75]+ZoomCmd.INCREMENT;
        names = new String[MAX+1];
        names[ZOOM_25] = (int) (factors[ZOOM_25]*100)+"%";
        names[ZOOM_50] = (int) (factors[ZOOM_50]*100)+"%";
        names[ZOOM_75] = (int) (factors[ZOOM_75]*100)+"%";
        names[ZOOM_100] = (int) (factors[ZOOM_100]*100)+"%";
    }
    
    /** 
     * The index of the zooming action. One of the contants defined by
     * this class.
     */
    private int zoomingIndex;
    
    /**
     * Checks if the passed index is supported.
     * 
     * @param index The index to control.
     */
    private void controlsIndex(int index)
    {
        switch (index) {
            case ZOOM_25:
            case ZOOM_50:
            case ZOOM_75:
            case ZOOM_100:
                    return;
            default:
                throw new IllegalArgumentException("Zoom index not supported.");
        }
    }
    
    /**
     * Returns the index corresponding to the passed factor.
     * 
     * @param f The factor used to retrieve the index.
     * @return See above.
     */
    public static int getIndex(double f)
    {
    	for (int i = 0; i < factors.length; i++)
			if (factors[i] == f) return i;
    	return -1;
    }

    /**
     * Returns the magnification factor corresponding to the passed index.
     * 
     * @param index The magnification index.
     * @return See above.
     */
    public static double getZoomFactor(int index)
    {
    	if (index < 0 || index >= (factors.length-1))
    		return -1;
    	return factors[index];
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model         Reference to the model. Mustn't be
     *                      <code>null</code>.
     * @param zoomingIndex  The index of the zooming action.
     *                      One of the constants defined by this class.
     */
    public ZoomGridAction(ImViewer model, int zoomingIndex)
    {
        super(model);
        controlsIndex(zoomingIndex);
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        this.zoomingIndex = zoomingIndex;
        putValue(Action.NAME, names[zoomingIndex]);
        name = names[zoomingIndex];
    }
    
    /**
     * Returns the zoom index associated to this action.
     * 
     * @return See above.
     */
    public int getIndex() { return zoomingIndex; }
    
    /** 
     * Sets the zooming factor.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        model.setGridMagnificationFactor(factors[zoomingIndex]);
    }
    

}
