/*
 * org.openmicroscopy.shoola.util.image.roi.ROI4D
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
 * by {@link CopiableArray} are overridden for type-safety.
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

    /** 
     * Creates a new instance. 
     * 
     * @param size The size of the array.
     */
    public ROI4D(int size)
    {
        super(size);
    }
    
    /** 
     * Constructs a new {@link CopiableArray} of the specified size.
     * 
     * @param size number of elements in the array.
     * @return See above.
     */
    protected CopiableArray makeNew(int size) { return new ROI4D(size); }

    /** 
     * Overrides the {@link #set(Copiable, int)} method of 
     * {@link CopiableArray}. Controls if the element is an instance of 
     * the excepted type i.e. {@link ROI3D}. 
     * Note that a {@link ROI3D} object cannot be set to <code>null</code>.
     */
    public void set(Copiable roi3D, int t)
    {
        if (!(roi3D instanceof ROI3D))
            throw new IllegalArgumentException();
        super.set(roi3D, t);
    }
    
    /** 
     * Returns the stack i.e. {@link ROI3D} for the specified timepoint.
     * 
     * @param t The timepoint.
     * @return See above.
     */
    public ROI3D getStack(int t)  { return (ROI3D) get(t); }
    
    /**
     * Sets the element i.e. {@link ROI3D} for the specified timepoint. 
     * 
     * @param stackROI The element to set.
     * @param t The timepoint.
     */
    public void setStack(ROI3D stackROI, int t) { set(stackROI, t); }
    
    /** 
     * Return the {@link PlaneArea leaf} at the specified position in the 
     * hierarchy.
     * 
     * @param z The specified z-section.
     * @param t The speficied timepoint.         
     * @return See above.
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
     * @param pa The {@link PlaneArea} to set.
     * @param z The specified z-section.
     * @param t The speficied timepoint.         
     */
    public void setPlaneArea(PlaneArea pa, int z, int t)
    {
        ROI3D stackROI = (ROI3D) get(t);
        if (stackROI != null) stackROI.setPlaneArea(pa, z);
    }
    
}