/*
 * org.openmicroscopy.shoola.util.image.roi.ROI4D
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

package org.openmicroscopy.shoola.util.image.roi;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.math.geom2D.PlaneArea;
import org.openmicroscopy.shoola.util.mem.Copiable;
import org.openmicroscopy.shoola.util.mem.CopiableArray;

/** 
 * This <i>stateless</i> class extends {@link CopiableArray}.
 * This class is the second container of the ROI selection algorithm.
 * The {@link #set(Copiable, int)} and {@link #get(int)} methods defined
 * by {@link CopiableArray} are overriden for type-safety.
 * <p>
 * A {@link ROI4D} can be seen as a <code>stack of ROIs across time</code>
 * i.e. a collection of <code>stack of ROIs</code> {@link ROI3D objects}.
 * </p>
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
public class ROI4D
    extends CopiableArray
{

    /** Public constructor. */
    public ROI4D(int size)
    {
        super(size);
    }
    
    /** 
     * Constructs a new {@link CopiableArray} of the specified size.
     * 
     * @param size number of elements in the array.
     */
    protected CopiableArray makeNew(int size)
    {
        return new ROI4D(size);
    }

    /** 
     * Overrides the {@link #set(Copiable, int)} method of 
     * {@link CopiableArray}. Check if the element is an instance of 
     * the excepted type i.e. {@link ROI3D}. 
     * Note that a {@link ROI3D} object cannot be set to <code>null</code>.
     */
    public void set(Copiable roi3D, int t)
    {
        if (!(roi3D instanceof ROI3D))
            throw new IllegalArgumentException();
        super.set(roi3D, t);
    }
    
    /** Return an element of the correct type i.e. {@link ROI3D}. */
    public ROI3D getStack(int t) 
    {
        return (ROI3D) get(t);
    }
    
    /** Set the element i.e. {@link ROI3D} at the specified timepoint. */
    public void setStack(ROI3D stackROI, int t) { set(stackROI, t); }
    
    /** 
     * Return the {@link PlaneArea leaf} at the specified position in the 
     * hierarchy.
     * 
     * @param z         specified z-section.
     * @param t         speficied timepoint.         
     * @return          See above.
     */
    public PlaneArea getPlaneArea(int z, int t)
    {
        ROI3D stackROI = (ROI3D) get(t);
        if (stackROI == null) return null;
        return stackROI.getPlaneArea(z);
    }
    
    /** 
     * Replaces the {@link PlaneArea leaf} at the specified position with
     * the specified element.
     *  
     * @param pa        new {@link PlaneArea}.
     * @param z         specified z-section.
     * @param t         speficied timepoint.         
     */
    public void setPlaneArea(PlaneArea pa, int z, int t)
    {
        ROI3D stackROI = (ROI3D) get(t);
        if (stackROI != null) stackROI.setPlaneArea(pa, z);
    }
    
}