/*
 * org.openmicroscopy.shoola.agents.browser.images.PaintMethodZOrder
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
package org.openmicroscopy.shoola.agents.browser.images;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class that specifies the active paint methods for a particular
 * browser window, and in which order they should be called.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class PaintMethodZOrder
{
    private List bottomToTop; // the list from bottom to top.

    /**
     * The paint method is inactive.
     */
    public static final int NOT_LISTED = -1;

    /**
     * Constructs an active list and ordering.
     */
    public PaintMethodZOrder()
    {
        bottomToTop = new ArrayList();
    }

    /**
     * Adds a new paint method, to be drawn on the bottom.
     * @param m The paint method to add.
     */
    public void addMethodToBottom(PaintMethod m)
    {
        if (m == null)
        {
            return;
        }
        else
        {
            bottomToTop.add(0, m);
        }
    }

    /**
     * Adds a new paint method, to be drawn on the top.
     * @param m The paint method to add.
     */
    public void addMethodToTop(PaintMethod m)
    {
        if (m == null)
        {
            return;
        }
        else
        {
            bottomToTop.add(m);
        }
    }

    /**
     * Adds a new paint method, to be drawn at the specified z order.
     * @param index The z order (0=bottom, max=top) of the paint method.
     * @param m The paint method to add.
     */
    public void addMethodAtIndex(int index, PaintMethod m)
    {
        if (index < 0 || index > bottomToTop.size() || m == null)
        {
            return;
        }
        else
        {
            bottomToTop.add(index, m);
        }
    }

    /**
     * Sends the paint method one step back, if it is in the collection.
     * @param m The paint method to demote.
     */
    public void sendMethodBackward(PaintMethod m)
    {
        if (!bottomToTop.contains(m))
        {
            return;
        }
        else
        {
            int prev = bottomToTop.indexOf(m);
            if (prev != 0)
            {
                bottomToTop.remove(m);
                bottomToTop.add(prev - 1, m);
            }
        }
    }

    /**
     * Sends the paint method one step forward, if it is in the collection.
     * @param m The paint method to promote.
     */
    public void sendMethodForward(PaintMethod m)
    {
        if (!bottomToTop.contains(m))
        {
            return;
        }
        else
        {
            int prev = bottomToTop.indexOf(m);
            if (prev != bottomToTop.size() - 1)
            {
                bottomToTop.remove(m);
                bottomToTop.add(prev + 1, m);
            }
        }
    }

    /**
     * Sends the paint method to the back, if it is in the collection.
     * @param m The paint method to demote.
     */
    public void sendMethodToBack(PaintMethod m)
    {
        if (!bottomToTop.contains(m))
        {
            return;
        }
        else
        {
            bottomToTop.remove(m);
            addMethodToBottom(m);
        }
    }

    /**
     * Sends the paint method to the front, if it is in the collection.
     * @param m The paint method to promote.
     */
    public void sendMethodToFront(PaintMethod m)
    {
        if (!bottomToTop.contains(m))
        {
            return;
        }
        else
        {
            bottomToTop.remove(m);
            addMethodToTop(m);
        }
    }

    /**
     * Returns the current z-order of the specified method, or NOT_LISTED if
     * the method is not in the collection.  O=bottom, max=top.
     * @param m The paint method to investigate.
     * @return The z-order of the paint method, 0=bottom, max=top.
     */
    public int getMethodIndex(PaintMethod m)
    {
        if (!bottomToTop.contains(m))
        {
            return NOT_LISTED;
        }
        else
            return bottomToTop.indexOf(m);
    }

    /**
     * Returns the list of paint methods in order from bottom to top.
     * @return See above.
     */
    public List getMethodOrder()
    {
        return Collections.unmodifiableList(bottomToTop);
    }

    /**
     * Sets the z-order of the specified method to the specified value, if it is
     * valid and m is in the collection.
     * @param index The desired z-order.
     * @param m The paint method to promote or demote.
     */
    public void setMethodIndex(int index, PaintMethod m)
    {
        if (index < 0 || index > bottomToTop.size() - 1 || m == null)
        {
            return;
        }
        else
        {
            bottomToTop.remove(m);
            bottomToTop.add(index, m);
        }
    }
}
