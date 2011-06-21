/*
 * org.openmicroscopy.shoola.agents.imviewer.actions.ZoomAction
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

package org.openmicroscopy.shoola.agents.imviewer.actions;


//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Sets the magnification factor.
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
public class ZoomAction
    extends ViewerAction
{
    
	/** The Zoom fit factor. */
	public static final double	ZOOM_FIT_FACTOR = -1;
	
    /** The maximum value of the zoom factor. */
    public static final double  MAX_ZOOM_FACTOR = 12*ZoomCmd.INCREMENT;
    
    /** The minimum value of the zoom factor. */
    public static final double  MIN_ZOOM_FACTOR = ZoomCmd.INCREMENT;
    
    /** The default zooming factor. */
    public static final double  DEFAULT_ZOOM_FACTOR = 1.0;
    
    /** Identifies the <code>0.25</code> zooming factor. */
    public static final int     ZOOM_25 = 0;
    
    /** Identifies the <code>0.50</code> zooming factor. */
    public static final int     ZOOM_50 = 1;

    /** Identifies the <code>0.75</code> zooming factor. */
    public static final int     ZOOM_75 = 2;
    
    /** Identifies the <code>1.0</code> zooming factor. */
    public static final int     ZOOM_100 = 3;
    
    /** Identifies the <code>1.25</code> zooming factor. */
    public static final int     ZOOM_125 = 4;
    
    /** Identifies the <code>1.5</code> zooming factor. */
    public static final int     ZOOM_150 = 5;
    
    /** Identifies the <code>1.75</code> zooming factor. */
    public static final int     ZOOM_175 = 6;
    
    /** Identifies the <code>2.00</code> zooming factor. */
    public static final int     ZOOM_200 = 7;
    
    /** Identifies the <code>2.25</code> zooming factor. */
    public static final int     ZOOM_225 = 8;
    
    /** Identifies the <code>2.50</code> zooming factor. */
    public static final int     ZOOM_250 = 9;
    
    /** Identifies the <code>2.75</code> zooming factor. */
    public static final int     ZOOM_275 = 10;
    
    /** Identifies the <code>3.00</code> zooming factor. */
    public static final int     ZOOM_300 = 11;
    
    /** Indicates to zoom the image to fit to window size. */
    public static final int     ZOOM_FIT_TO_WINDOW = 12;
     
    /** The default zooming index. */
    public static final int  	DEFAULT_ZOOM_INDEX = ZOOM_100;
    
    /** The minimum value of zooming index. */
    public static final int  	MIN_ZOOM_INDEX = ZOOM_25;
    
    /** The maximum value of zooming index. */
    public static final int  	MAX_ZOOM_INDEX = ZOOM_300;
    
    /** The maximum value of zooming index. */
    public static final String  ZOOM_FIT_NAME = "Zoom to Fit";
    
    /** The number of supported identifiers. */
    private static final int    MAX = 12;
    
    /** The description of the action. */
    private static final String DESCRIPTION = "Zoom in or out.";

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
        factors[ZOOM_125] = factors[ZOOM_100]+ZoomCmd.INCREMENT;
        factors[ZOOM_150] = factors[ZOOM_125]+ZoomCmd.INCREMENT;
        factors[ZOOM_175] = factors[ZOOM_150]+ZoomCmd.INCREMENT;
        factors[ZOOM_200] = factors[ZOOM_175]+ZoomCmd.INCREMENT;
        factors[ZOOM_225] = factors[ZOOM_200]+ZoomCmd.INCREMENT;
        factors[ZOOM_250] = factors[ZOOM_225]+ZoomCmd.INCREMENT;
        factors[ZOOM_275] = factors[ZOOM_250]+ZoomCmd.INCREMENT;
        factors[ZOOM_300] = factors[ZOOM_275]+ZoomCmd.INCREMENT;
        factors[ZOOM_FIT_TO_WINDOW] = ZOOM_FIT_FACTOR;
        names = new String[MAX+1];
        names[ZOOM_25] = (int) (factors[ZOOM_25]*100)+"%";
        names[ZOOM_50] = (int) (factors[ZOOM_50]*100)+"%";
        names[ZOOM_75] = (int) (factors[ZOOM_75]*100)+"%";
        names[ZOOM_100] = (int) (factors[ZOOM_100]*100)+"%";
        names[ZOOM_125] = (int) (factors[ZOOM_125]*100)+"%";
        names[ZOOM_150] = (int) (factors[ZOOM_150]*100)+"%";
        names[ZOOM_175] = (int) (factors[ZOOM_175]*100)+"%";
        names[ZOOM_200] = (int) (factors[ZOOM_200]*100)+"%";
        names[ZOOM_225] = (int) (factors[ZOOM_225]*100)+"%";
        names[ZOOM_250] = (int) (factors[ZOOM_250]*100)+"%";
        names[ZOOM_275] = (int) (factors[ZOOM_275]*100)+"%";
        names[ZOOM_300] = (int) (factors[ZOOM_300]*100)+"%";
        names[ZOOM_FIT_TO_WINDOW] = ZOOM_FIT_NAME;
    }
    
    /** 
     * The index of the zooming action. One of the constants defined by
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
            case ZOOM_125:
            case ZOOM_150:
            case ZOOM_175:
            case ZOOM_200:
            case ZOOM_225:
            case ZOOM_250:
            case ZOOM_275:
            case ZOOM_300:
            case ZOOM_FIT_TO_WINDOW:
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
    	return ZOOM_FIT_TO_WINDOW;
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
    		return ZOOM_FIT_FACTOR;
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
    public ZoomAction(ImViewer model, int zoomingIndex)
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
        model.setZoomFactor(factors[zoomingIndex], zoomingIndex);
    }
    
}