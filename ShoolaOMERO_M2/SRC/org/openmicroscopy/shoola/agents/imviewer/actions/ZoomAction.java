/*
 * org.openmicroscopy.shoola.agents.imviewer.actions.ZoomAction
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
    
    /** The maximun value of the zoom factor. */
    public static final double  MAX_ZOOM_FACTOR = 3.0;
    
    /** The minimun value of the zoom factor. */
    public static final double  MIN_ZOOM_FACTOR = 0.25;
    
    /** The default zooming factor. */
    public static final double  DEFAULT_ZOOM_FACTOR = 1.0;
    
    /**
     * Bounds property indicating that a new zooming factor is selected.
     */
    public static final String  ZOOM_PROPERTY = "zoom";
    
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
     
    /** The number of supported ids. */
    private static final int    MAX = 11;
    
    /** The description of the action. */
    private static final String DESCRIPTION = "Zoom in or out.";

    /** The array containing the actions' name. */
    private static String[]     names;
    
    /** The array containing the zooming factor. */
    private static double[]     factors;
    
    /** Defines the static fields. */
    static {
        names = new String[MAX+1];
        names[ZOOM_25] = "25%";
        names[ZOOM_50] = "50%";
        names[ZOOM_75] = "75%";
        names[ZOOM_100] = "100%";
        names[ZOOM_125] = "125%";
        names[ZOOM_150] = "150%";
        names[ZOOM_175] = "175%";
        names[ZOOM_200] = "200%";
        names[ZOOM_225] = "225%";
        names[ZOOM_250] = "250%";
        names[ZOOM_275] = "275%";
        names[ZOOM_300] = "300%";
        factors = new double[MAX+1];
        factors[ZOOM_25] = 0.25;
        factors[ZOOM_50] = 0.5;
        factors[ZOOM_75] = 0.75;
        factors[ZOOM_100] = 1;
        factors[ZOOM_125] = 1.25;
        factors[ZOOM_150] = 1.5;
        factors[ZOOM_175] = 1.75;
        factors[ZOOM_200] = 2;
        factors[ZOOM_225] = 2.25;
        factors[ZOOM_250] = 2.5;
        factors[ZOOM_275] = 2.75;
        factors[ZOOM_300] = 3;
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
            case ZOOM_125:
            case ZOOM_150:
            case ZOOM_175:
            case ZOOM_200:
            case ZOOM_225:
            case ZOOM_250:
            case ZOOM_275:
            case ZOOM_300:
                return;
            default:
                throw new IllegalArgumentException("Zoom index not supported.");
        }
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
        putValue(Action.NAME, 
                UIUtilities.formatToolTipText(names[zoomingIndex]));
        setName(names[zoomingIndex]);
    }
    
    /** 
     * Sets the zooming factor.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        firePropertyChange(ZOOM_PROPERTY, null, this);
        model.setZoomFactor(factors[zoomingIndex]);
    }
    
}
