/*
 * org.openmicroscopy.shoola.util.image.roi.ROI3D
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
 * This class is the first container of the ROI selection algorithm.
 * The {@link #set(Copiable, int)} and {@link #get(int)} methods defined
 * by {@link CopiableArray} are overriden for type-safety.
 * <p>
 * A {@link ROI3D} can be seen as a <code>stack of ROIs</code> 
 * i.e. a collection of {@link PlaneArea} (i.e. a ROI drawn on a 2D-plane).
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
public class ROI3D
    extends CopiableArray
{

    /** 
     * Creates a new instance. 
     * 
     * @param size The size of the array.
     */
    public ROI3D(int size)
    {
        super(size);
    }
    
    /** 
     * Constructs a new {@link CopiableArray} of the specified size.
     * 
     * @param size number of elements in the array.
     * @return See above.
     */
    protected CopiableArray makeNew(int size) { return new ROI3D(size); }

    /** Sets the {@link PlaneArea} at the specified z-section. 
     * 
     * @param pa The 2D-ROI to set.
     * @param z The z-section.
     */
    public void setPlaneArea(PlaneArea pa, int z) { set(pa, z); }
    
    /** 
     * Overrides the {@link #set(Copiable, int)} method of 
     * {@link CopiableArray}. Controls if the element is an instance of 
     * the excepted type i.e. {@link PlaneArea}.
     * Note that a {@link PlaneArea} object can be set to <code>null</code>.
     */
    public void set(Copiable planeArea, int z)
    {
        if (planeArea != null && !(planeArea instanceof PlaneArea))
            throw new IllegalArgumentException();
        super.set(planeArea, z);
    }
    
    /** 
     * Returns an element of the correct type i.e. {@link PlaneArea} at the
     * specified z-section.
     * 
     * @param z The specified z-section.
     * @return  See above.
     */
    public PlaneArea getPlaneArea(int z) { return (PlaneArea) get(z); }
    
}
