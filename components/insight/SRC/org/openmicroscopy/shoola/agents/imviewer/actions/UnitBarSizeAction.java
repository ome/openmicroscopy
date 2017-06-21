/*
 * org.openmicroscopy.shoola.agents.imviewer.actions.UnitBarSizeAction
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
    public static final int 	ONE = 0;
    
    /** Identifies the scale bar of size <code>2</code>. */
    public static final int 	TWO = 1;
    
    /** Identifies the scale bar of size <code>5</code>. */
    public static final int 	FIVE = 2;
    
    /** Identifies the scale bar of size <code>10</code>. */
    public static final int 	TEN = 3;
    
    /** Identifies the scale bar of size <code>20</code>. */
    public static final int 	TWENTY = 4;
    
    /** Identifies the scale bar of size <code>50</code>. */
    public static final int 	FIFTY = 5;
    
    /** Identifies the scale bar of size <code>100</code>. */
    public static final int 	HUNDRED = 6;
    
    /** Identifies the scale bar of size customized. */
    public static final int 	CUSTOMIZED = 7;
   
    /** The default index. */
    public static final int 	DEFAULT_UNIT_INDEX = FIVE;
    
    /** The number of supported identifiers. */
    private static final int	MAX = 7;
    
    /** The description of the action. */
    private static final String DESCRIPTION = "Select the size of " +
            "the Scale bar displayed on top of the image.";
     
    /** 
     * Array of action names associated to the identifiers defined by this 
     * class.
     */
    private static String[]     		names;
    
    /** 
     * Array of values associated to the identifiers defined by this 
     * class.
     */
    private static int[]        		values;

    static {
        values = new int[MAX+1];
        values[ONE] = 1;
        values[TWO] = 2;
        values[FIVE] = 5;
        values[TEN] = 10;
        values[TWENTY] = 20;
        values[FIFTY] = 50;
        values[HUNDRED] = 100;
        names = new String[MAX+1];
        names[ONE] = ""+values[ONE];
        names[TWO] = ""+values[TWO];
        names[FIVE] = ""+values[FIVE];
        names[TEN] = ""+values[TEN];
        names[TWENTY] = ""+values[TWENTY];
        names[FIFTY] = ""+values[FIFTY];
        names[HUNDRED] = ""+values[HUNDRED];
        names[CUSTOMIZED] = "Custom";
    }

    /**
     * Returns the value associated to the default index.
     * 
     * @return See above.
     */
    public static int getDefaultValue() { return values[DEFAULT_UNIT_INDEX]; }
    
    /**
     * Returns the value associated to the default index.
     * 
     * @param size The size of reference.
     * @return See above.
     */
    public static int getDefaultIndex(double size)
    { 
    	if (size < 1) return FIVE; 
    	if (size >=1 && size < 2) return TEN; 
    	if (size >=2 && size < 3) return TWENTY; 
    	if (size >=3 && size < 4) return FIFTY;
    	return HUNDRED;
    }
    
    /**
     * Returns the value corresponding to the passed index or <code>-1</code>.
     * 
     * @param index The index to handle.
     * @return See above.
     */
    public static int getValue(int index)
    {
    	if (index < ONE || index > MAX) return -1;
    	return values[index];
    }
    
    /**
     * Returns the index corresponding to the passed value or
     * <code>CUSTOMIZED</code>.
     * 
     * @param value
     *            The value to handle.
     * @return See above.
     */
    public static int getIndex(double value) {
        if (value < values[TWO])
            return ONE;
        if (value < values[FIVE])
            return TWO;
        if (value < values[TEN])
            return FIVE;
        if (value < values[TWENTY])
            return TEN;
        if (value < values[FIFTY])
            return TWENTY;
        if (value < values[HUNDRED])
            return FIFTY;
        if (value < 1000)
            return HUNDRED;
        return CUSTOMIZED;
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
     * Returns the index of the action
     * 
     * @return See above.
     */
    public int getIndex() { return index; }
    
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
