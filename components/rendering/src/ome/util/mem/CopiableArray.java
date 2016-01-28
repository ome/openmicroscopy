/*
 * ome.util.mem.CopiableArray
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.mem;

/**
 * This abstract class provides an implementation of the {@link Copiable}
 * interface. It constructs an array of {@link Copiable}s , note that all
 * subclasses must implement the {@link #makeNew(int)} method. It provides
 * methods to manipulate elements of the array {@link #set(Copiable, int)} and
 * {@link #get(int)}. It also implements a {@link #copy(int, int)} method which
 * allows to copy an element from a specified position in the array into a new
 * specified position. Subclasses inherit the {@link #copy()} method
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/09 15:01:57 $) </small>
 * @since OME2.2
 */
public abstract class CopiableArray implements Copiable {

    private Copiable[] elements;

    /**
     * Creates a new instance.
     * 
     * @param size
     *            The size of the array.
     * @throws IllegalArgumentException
     *             If the size is not strictly positive,
     */
    protected CopiableArray(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size cannot be <=0");
        }
        elements = new Copiable[size];
    }

    /**
     * Creates a new array of the speficied size.
     * 
     * @param size
     *            The size of the array.
     * @return See above.
     */
    protected abstract CopiableArray makeNew(int size);

    /**
     * Returns the number of elements in the array.
     * 
     * @return See above.
     */
    public int getSize() {
        return elements.length;
    }

    /**
     * Replaces the element at the specified position with the specified
     * {@link Copiable}.
     * 
     * @param element
     *            Copiable to set.
     * @param index
     *            The position in the array.
     * @throws IllegalArgumentException
     *             If the index is not valid.
     */
    public void set(Copiable element, int index) {
        if (index >= elements.length || index < 0) {
            throw new IllegalArgumentException("index not valid");
        }
        elements[index] = element;
    }

    /**
     * Return the {@link Copiable} at the specified position.
     * 
     * @param index
     *            The position in the array.
     * @return See above.
     * @throws IllegalArgumentException
     *             If the index is not valid.
     */
    public Copiable get(int index) {
        if (index >= elements.length || index < 0) {
            throw new IllegalArgumentException("index not valid");
        }
        return elements[index];
    }

    /**
     * Copies the {@link Copiable} from the specified position <code>from</code>
     * into the specified position <code>to</code>.
     * 
     * @param from
     *            The starting position.
     * @param to
     *            The ending position.
     * @throws IllegalArgumentException
     *             If the indexes are not valid.
     */
    public void copy(int from, int to) {
        if (from >= elements.length || from < 0) {
            throw new IllegalArgumentException("from index not valid");
        }
        if (to >= elements.length || to < 0) {
            throw new IllegalArgumentException("to index not valid");
        }
        if (from > to) {
            throw new IllegalArgumentException(from + " must be <= than " + to);
        }

        Copiable master = elements[from];
        if (master != null) {
            for (int i = from + 1; i <= to; i++) {
                elements[i] = (Copiable) master.copy();
            }
        } else {
            for (int i = from + 1; i <= to; i++) {
                elements[i] = null;
            }
        }
    }

    /**
     * Implements the method as specified by {@link Copiable}.
     * 
     * @see Copiable#copy()
     */
    public Object copy() {
        CopiableArray copy = makeNew(elements.length);
        Copiable c;
        for (int i = 0; i < elements.length; i++) {
            c = elements[i];
            if (c != null) {
                c = (Copiable) c.copy();
            }
            copy.set(c, i);
        }
        return copy;
    }

}
