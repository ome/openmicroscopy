/*
 * org.openmicroscopy.shoola.agents.browser.events.CompositePiccoloAction
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser.events;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * Allows for the addition/removal of behaviors in addition to a simple,
 * base behavior.  Ideal if you need to attach a mode-specific event on top
 * of a normal event.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class CompositePiccoloAction extends PiccoloAction
{
    /**
     * The base action which is always executed (can be a NOOP)
     */
    protected PiccoloAction baseAction;
    
    /*
     * INVARIANT: This always points to the index of the baseAction, so we
     * don't have to concern ourselves with equals() checks.
     */
    private int basePosition;
    
    /**
     * All the actions, including the base action (the accessors/mutators
     * keep track of the base action to make sure it never gets changed or
     * removed)
     */
    protected List allActions;
    
    /**
     * Initialize a composite PiccoloAction, with the baseAction as the
     * action which is always executed (although possibly not first).  If
     * <code>baseAction</code> is set to <code>null</code>, this action will
     * be a <code>PiccoloAction.PNOOP_ACTION</code>.
     * 
     * @param baseAction The action which is always executed (i.e., what
     *                   would happen if this was a normal PiccoloAction)
     */
    public CompositePiccoloAction(PiccoloAction baseAction)
    {
        if(baseAction == null)
        {
            this.baseAction = PiccoloAction.PNOOP_ACTION;
        }
        else
        {
            this.baseAction = baseAction;
        }
        allActions = new ArrayList();
        allActions.add(baseAction);
        basePosition = 0;
    }
    
    /**
     * Adds an action to the list of actions executed when either variant
     * of <code>execute()</code> is called.  If <code>seq</code> is 0 or
     * negative, the specified action will be promoted to the first action
     * execute in the sequence.  If <code>seq</code> is greater than
     * <code>size()</code>, it is guaranteed to be the last action executed
     * in the <code>execute()</code> call.
     * 
     * @param seq In which slot to execute the action.
     * @param action The action to execute.
     */
    public void addAction(int seq, PiccoloAction action)
    {
        if(seq < 0)
        {
            allActions.add(0,action);
            basePosition++;
        }
        else if(seq > allActions.size())
        {
            allActions.add(action);
        }
        else
        {
            allActions.add(seq,action);
            if(seq <= basePosition)
            {
                basePosition++;
            }
        }
    }
    
    /**
     * Get the number of actions to be executed in this composite action.
     * @return See above.
     */
    public int size()
    {
        return allActions.size();
    }
    
    /**
     * Get the current index of the base (always-executed) action.
     * @return See above.
     */
    public int getBaseActionIndex()
    {
        return basePosition;
    }
    
    /**
     * Remove the action at the specified index from the sequence of execution. 
     * @param seq The index.
     */
    public void removeAction(int seq)
    {
        /*
         * Don't remove the base case.
         */
        if(seq != basePosition && seq >= 0 && seq < allActions.size())
        {
            allActions.remove(seq);
            if(seq < basePosition)
            {
                basePosition--;
            }
        }
    }
    
    /**
     * Only execute the base action, clearing all other actions from
     * the CompositePiccoloAction.
     */
    public void revertToOriginal()
    {
        for(int i=0;i<basePosition;i++)
        {
            allActions.remove(i);
        }
        for(int i=basePosition+1;i<allActions.size();i++)
        {
            allActions.remove(i);
        }
    }
    
    /**
     * Execute (without input context)
     * @see org.openmicroscopy.shoola.agents.browser.events.PiccoloAction#execute()
     */
    public void execute()
    {
        for(Iterator iter = allActions.iterator(); iter.hasNext();)
        {
            PiccoloAction action = (PiccoloAction)iter.next();
            action.execute();
        }
    }
    
    /**
     * Execute (with input context)
     * @param e The input context.
     * @see org.openmicroscopy.shoola.agents.browser.events.PiccoloAction#execute(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void execute(PInputEvent e)
    {
        for(Iterator iter = allActions.iterator(); iter.hasNext();)
        {
            PiccoloAction action = (PiccoloAction)iter.next();
            action.execute(e);
        }
    }
}
