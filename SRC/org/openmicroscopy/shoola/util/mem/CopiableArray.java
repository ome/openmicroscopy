/*
 * org.openmicroscopy.shoola.util.mem.CopiableArray
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

package org.openmicroscopy.shoola.util.mem;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public abstract class CopiableArray
    implements Copiable
{
    
    private Copiable[]  elements;
    
    protected CopiableArray(int size)
    {
        elements = new Copiable[size];
    }
    
    protected abstract CopiableArray makeNew(int size);
    
    public void set(Copiable element, int index)
    {
        elements[index] = element;
    }
    
    public Copiable get(int index)
    {
        return elements[index];
    }
 
    public void copy(int from, int to)
    {
        Copiable master = elements[from];
        if (master != null) {
            for (int i = from+1; i <= to; i++)
                elements[i] = (Copiable) master.copy();
        } else {
            for (int i = from+1; i <= to; i++)
                elements[i] = null;
        }

    }
    
    public Object copy()
    {
        CopiableArray copy = makeNew(elements.length);
        Copiable c;
        for (int i = 0; i < elements.length; i++) {
            c = elements[i];
            if (c != null) c = (Copiable) c.copy();
            copy.set(c, i);
        }
        return copy;
    }
    
}
