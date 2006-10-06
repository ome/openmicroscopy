/*
 * org.openmicroscopy.shoola.agents.imviewer.actions.UnitBarSizeAction
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
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.util.UnitBarSizeDialog;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;


/** 
 * Increases or decreases the size of the unit bar.
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
public class UnitBarSizeAction
    extends ViewerAction
{

    /** Identifies the scale bar of size <code>1</code>. */
    public static final int ONE = 0;
    
    /** Identifies the scale bar of size <code>2</code>. */
    public static final int TWO = 1;
    
    /** Identifies the scale bar of size <code>5</code>. */
    public static final int FIVE = 2;
    
    /** Identifies the scale bar of size <code>10</code>. */
    public static final int TEN = 3;
    
    /** Identifies the scale bar of size <code>20</code>. */
    public static final int TWENTY = 4;
    
    /** Identifies the scale bar of size <code>50</code>. */
    public static final int FIFTY = 5;
    
    /** Identifies the scale bar of size <code>100</code>. */
    public static final int HUNDRED = 6;
    
    /** Identifies the scale bar of size customized. */
    public static final int CUSTOMIZED = 7;
    
    /** The number of supported ids. */
    private static final int    MAX = 7;
    
    
    /** The description of the action. */
    private static final String DESCRIPTION = "Selects the size of " +
            "the Scale bar displayed on top of the image.";
    
    
    /** 
     * Array of action names associated to the identifiers defined by this 
     * class.
     */
    private static String[]     names;
    
    /** 
     * Array of values associated to the identifiers defined by this 
     * class.
     */
    private static int[]        values;
    
    static {
        names = new String[MAX+1];
        names[ONE] = "1";
        names[TWO] = "2";
        names[FIVE] = "5";
        names[TEN] = "10";
        names[TWENTY] = "20";
        names[FIFTY] = "50";
        names[HUNDRED] = "100";
        names[CUSTOMIZED] = "Custom";
        values = new int[MAX+1];
        values[ONE] = 1;
        values[TWO] = 2;
        values[FIVE] = 5;
        values[TEN] = 10;
        values[TWENTY] = 20;
        values[FIFTY] = 50;
        values[HUNDRED] = 100;
    }

    /** One of the constant defined by this class. */
    private int     index;
    
    /** 
     * Controls if the specified index is valid.
     * 
     * @param i The index to check.
     */
    private void checkIndex(int i)
    {
        switch (i) {
            case ONE:
            case TWO:
            case FIVE:
            case TEN:
            case TWENTY:
            case FIFTY:
            case HUNDRED:
            case CUSTOMIZED:
                return;
            default:
                throw new IllegalArgumentException("Index not supported.");
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model     Reference to the model. Mustn't be <code>null</code>.
     * @param index     One of the constant defined by this class.
     */
    public UnitBarSizeAction(ImViewer model, int index)
    {
        super(model);
        checkIndex(index);
        this.index = index;
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        putValue(Action.NAME, names[index]);
    }
    
    /**
     * Sets the size of the unit bar.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        if (index != CUSTOMIZED) model.setUnitBarSize(values[index]);
        else model.showUnitBarSelection();
    }

}
