/*
 * org.openmicroscopy.shoola.util.image.roi.ROI5D
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
 * This class is the top container of the ROI selection algorithm.
 * The {@link #set(Copiable, int)} and {@link #get(int)} methods defined
 * by {@link CopiableArray} are overridden for type-safety.
 * It also provides two utility methods to access directly the 
 * {@link PlaneArea elements} positioned at the bottom of the hierarchy.
 * <p>
 * A {@link ROI5D} object can be seen as a collection of 
 * <code>stack of ROIs across time</code> {@link ROI4D objects}.
 * </p>
 * <p>
 * A {@link ROI5D} object is an array of {@link ROI4D} elements.
 * In turn, a {@link ROI4D} object is an array of {@link ROI3D} elements.
 * A {@link ROI3D} is an array of {@link PlaneArea} objects (leaf).
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
public class ROI5D
    extends CopiableArray
{

    /** 
     * Creates a new instance. 
     * 
     * @param size The size of the array.
     */
    public ROI5D(int size)
    {
        super(size);
    }
    
    /** 
     * Constructs a new {@link CopiableArray} of the specified size.
     * 
     * @param size number of elements in the array.
     * @return See above.
     */
    protected CopiableArray makeNew(int size) { return new ROI5D(size); }

    /** 
     * Overrides the {@link #set(Copiable, int)} method of 
     * {@link CopiableArray}. Check if the element is an instance of 
     * the excepted type i.e. {@link ROI4D}. 
     * Note that a {@link ROI4D} object cannot be set to <code>null</code>.
     */
    public void set(Copiable roi4D, int channel)
    {
        if (!(roi4D instanceof ROI4D))
            throw new IllegalArgumentException();
        super.set(roi4D, channel);
    }
   
    /** 
     * Set an element of the correct type i.e. {@link ROI4D} 
     * at the specified channel. 
     * 
     * @param stackAcrossTimeROI The {@link ROI4D} objet to set.
     * @param channel The specified channel.             
     * */
    public void setChannel(ROI4D stackAcrossTimeROI, int channel)
    {
        set(stackAcrossTimeROI, channel);
    }
    
    /** 
     * Returns an {@link ROI4D} for the specified channel.
     * 
     * @param channel The specified channel.
     * @return See above.
     */
    public ROI4D getChannel(int channel) { return (ROI4D) get(channel); }
    
    /** 
     * Returns the {@link PlaneArea leaf} at the specified position in the 
     * hierarchy.
     * 
     * @param z The specified z-section.
     * @param t The speficied timepoint.         
     * @param channel The specified channel.
     * @return See above.
     */
    public PlaneArea getPlaneArea(int z, int t, int channel)
    {
        ROI4D stackAcrossTimeROI = (ROI4D) get(channel);
        if (stackAcrossTimeROI == null) return null; 
        ROI3D stackROI = (ROI3D) stackAcrossTimeROI.get(t);
        if (stackROI == null) return null; 
        return stackROI.getPlaneArea(z);
    }
    
    /** 
     * Replaces the {@link PlaneArea leaf} at the specified positioned with
     * the specified element.
     *  
     * @param pa The {@link PlaneArea} to set.
     * @param z The specified z-section.
     * @param t The speficied timepoint.         
     * @param channel The specified channel.
     */
    public void setPlaneArea(PlaneArea pa, int z, int t, int channel)
    {
        ROI4D stackAcrossTimeROI = (ROI4D) get(channel);
        if (stackAcrossTimeROI != null) {
            ROI3D stackROI = (ROI3D) stackAcrossTimeROI.get(t);
            if (stackROI != null)
                stackROI.setPlaneArea(pa, z);
        }
    }
    
}
