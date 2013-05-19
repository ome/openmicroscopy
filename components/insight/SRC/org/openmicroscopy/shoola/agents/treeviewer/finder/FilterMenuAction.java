/*
 * org.openmicroscopy.shoola.agents.treeviewer.finder.actions.FilterMenuAction
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

package org.openmicroscopy.shoola.agents.treeviewer.finder;



//Java imports
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Action;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.finder.Finder;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Brings up on screen the filter menu.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
public class FilterMenuAction
	extends FinderAction
	implements MouseListener
{
    
    /** The description of the action. */
    private static final String DESCRIPTION = "Menu. Press (Shift) to keep " +
                                        "the menu on screen during selection";
    
    /** The location of the mouse pressed. */
    private Point pressedPoint;
    
    /** 
     * Sets the action enabled depending on the text entered.
     * @see FinderAction#onTextChanged()
     */
    protected void onTextChanged() { setEnabled(!model.isTextEmpty()); }
    
    /**
     * Creates a new instance. 
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public FilterMenuAction(Finder model)
    {
        super(model);
        setEnabled(true);
        IconManager im = IconManager.getInstance();
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        putValue(Action.SMALL_ICON, im.getIcon(IconManager.FILTER_MENU));
    }
    
    /** 
     * Sets the location of the point where the <code>mousePressed</code>
     * event occurred. 
     * @see MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent me) { pressedPoint = me.getPoint(); }
    
    /** 
     * Brings up the menu. 
     * @see MouseListener#mouseReleased(MouseEvent)
     */
    public void mouseReleased(MouseEvent me)
    {
        Object source = me.getSource();
        if (source instanceof Component && isEnabled())
            model.showMenu((Component) source, pressedPoint);
    }
    
    /** 
     * Required by {@link MouseListener} I/F but not actually needed in our
     * case, no-op implementation.
     * @see MouseListener#mouseEntered(MouseEvent)
     */   
    public void mouseEntered(MouseEvent e) {}

    /** 
     * Required by {@link MouseListener} I/F but not actually needed in our
     * case, no-op implementation.
     * @see MouseListener#mouseExited(MouseEvent)
     */   
    public void mouseExited(MouseEvent e) {}
    
    /** 
     * Required by {@link MouseListener} I/F but not actually needed in our
     * case, no-op implementation.
     * @see MouseListener#mouseClicked(MouseEvent)
     */   
    public void mouseClicked(MouseEvent e) {}

}
