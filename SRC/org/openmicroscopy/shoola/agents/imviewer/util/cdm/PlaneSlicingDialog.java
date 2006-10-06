/*
 * org.openmicroscopy.shoola.agents.imviewer.util.cdm.PlaneSlicingDialog
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

package org.openmicroscopy.shoola.agents.imviewer.util.cdm;


//Java imports
import javax.swing.JComponent;
import javax.swing.JFrame;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import ome.model.display.CodomainMapContext;
import ome.model.display.PlaneSlicingContext;


/** 
 * 
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
public class PlaneSlicingDialog
    extends CodomainMapContextDialog
{

    static final String             PANE_INDEX_PROPERTY = "paneIndex";
    
    /** The window's title. */
    private static final String     TITLE = "Plane Slicing settings";
    
    /** Description of the dialog's action. */
    private static final String     TEXT = "Highlight a specific " +
                                           "gray-level range.";
    
    /** Index of the {@link PlaneSlicingPaneStatic} pane. */
    static final int                STATIC = 0;
    
    /** Index of the {@link PlaneSlicingPane} pane. */
    static final int                NON_STATIC = 0;
    
    /** Identifies the 1-bit plane. */
    static final int                B_ONE = 0;
    
    /** Identifies the 2-bit plane. */
    static final int                B_TWO = 1;
    
    /** Identifies the 3-bit plane. */
    static final int                B_THREE = 2;
    
    /** Identifies the 4-bit plane. */
    static final int                B_FOUR = 3;
    
    /** Identifies the 5-bit plane. */
    static final int                B_FIVE = 4;
    
    /** Identifies the 6-bit plane. */
    static final int                B_SIX = 5;
    
    /** Identifies the 7-bit plane. */
    static final int                B_SEVEN = 6;

    /** Identifies the 0-bit plane. */
    static final int                B_ZERO = -1;
    
    /** The index of the selected pane. */
    private int paneIndex;
    
    /** The index of the selected bit-plane. */
    private int bitPlaneIndex;
      
    /** 
     * Builds the component hosting the window's information.
     * @see CodomainMapContextDialog#buildTitlePane()
     */
    protected JComponent buildTitlePane()
    {
        return new TitlePanel(TITLE, TEXT, NOTE, 
                icons.getIcon(IconManager.PLANE_SLICING_BIG));
    }
    
    /** 
     * Builds the component hosting the settings controls.
     * @see CodomainMapContextDialog#buildBody()
     */
    protected JComponent buildBody()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Sets the settings. 
     * @see CodomainMapContextDialog#updateContext()
     */
    protected void updateContext()
    {
        PlaneSlicingContext psCtx = (PlaneSlicingContext) ctx;
        
    }

    /** Sets the window's title. */
    protected void setWindowTitle() { setTitle(TITLE); }

    /**
     * Creates a new instance.
     * 
     * @param owner     The owner of the dialog.
     * @param ctx       The codomain map context this dialog is for.
     * @param cdEnd     The upper bound of the codomain interval.
     * @param cdStart   The lower bound of the codomain interval.
     */
    public PlaneSlicingDialog(JFrame owner, CodomainMapContext ctx,
                            int cdEnd, int cdStart)
    {
        super(owner, ctx, cdEnd, cdStart);
    }
    
    /**
     * Sets the index of the selected pane.
     * 
     * @param index The index to set.
     */
    void setPaneIndex(int index)
    {
        paneIndex = index;
    }
    
    /**
     * Returns the index of the currently selected pane.
     * 
     * @return See above.
     */
    int getPaneIndex() { return paneIndex; }
    
    boolean isContextConstant()
    { 
        return ((PlaneSlicingContext) ctx).getConstant().booleanValue();
    }
    
    /** 
     * Sets the index of the bit-plane. One of the constant defined by this
     * class.
     * 
     * @param index The index of the bit-plane.
     */
    void setBitPlaneIndex(int index)
    {
        bitPlaneIndex = index;
        //convert it
    }
    
    /**
     * Returns the index of the bit-plane, one of the constants defined by this
     * class.
     * 
     * @return See above.
     */
    int getBitPlaneIndex() { return bitPlaneIndex; }
    
    int getLowerLimit()
    { 
        return ((PlaneSlicingContext) ctx).getLowerLimit().intValue(); 
    }
    
    int getUpperLimit()
    { 
        return ((PlaneSlicingContext) ctx).getUpperLimit().intValue(); 
    }
}

