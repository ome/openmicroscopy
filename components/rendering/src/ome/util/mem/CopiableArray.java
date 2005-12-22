/*
 * ome.util.mem.CopiableArray
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

package ome.util.mem;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * This abstract class provides an implementation of the {@link Copiable}
 * interface. It constructs an array of {@link Copiable}s , note that
 * all subclasses must implement the {@link #makeNew(int)} method.
 * It provides methods to manipulate elements of the array 
 * {@link #set(Copiable, int)} and {@link #get(int)}.
 * It also implements a {@link #copy(int, int)} method which allows to copy 
 * an element from a specified position in the array into a new specified
 * position.
 * Subclasses inherit the {@link #copy()} method
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.1 $ $Date: 2005/06/09 15:01:57 $)
 * </small>
 * @since OME2.2
 */
public abstract class CopiableArray
    implements Copiable
{
    
    private Copiable[]  elements;
    
    /** Constructor. */
    protected CopiableArray(int size)
    {
        if (size <= 0) 
            throw new IllegalArgumentException("Size cannot be <=0");
        elements = new Copiable[size];
    }
    
    /** Contruct a new array of the speficied size. */
    protected abstract CopiableArray makeNew(int size);
    
    /** Return the number of elements in the array. */
    public int getSize() { return elements.length; }
    
    /** 
     * Replaces the element at the specified position with the 
     * specified {@link Copiable}.
     * 
     * @param element Copiable to set.
     * @param index   position.
     * */
    public void set(Copiable element, int index)
    {
        if (index >= elements.length || index < 0)
            throw new IllegalArgumentException("index not valid");
        elements[index] = element;
    }
    
    /** 
     * Return the {@link Copiable} at the specified position.
     * 
     * @param index The position.
     * @return See above.
     */
    public Copiable get(int index)
    {
        if (index >= elements.length || index < 0)
            throw new IllegalArgumentException("index not valid");
        return elements[index];
    }
 
    /** 
     * Copy the {@link Copiable} from the specified position <code>from</code>
     * into the specified position <code>to</code>.
     * 
     * @param from  position.
     * @param to    position.
     */
    public void copy(int from, int to)
    {
        if (from >= elements.length || from < 0)
            throw new IllegalArgumentException("from index not valid");
        if (to >= elements.length || to < 0)
            throw new IllegalArgumentException("to index not valid");
        if (from > to) 
            throw new IllegalArgumentException(from+" must be <= than "+to);
        
        Copiable master = elements[from];
        if (master != null) {
            for (int i = from+1; i <= to; i++)
                elements[i] = (Copiable) master.copy();
        } else {
            for (int i = from+1; i <= to; i++)
                elements[i] = null;
        }
    }
    
    /** Implements the method as specified by {@link Copiable}. */
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
