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
public class ROI3D
    extends CopiableArray
{

    public ROI3D(int size)
    {
        super(size);
    }
    
    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.util.mem.CopiableArray#makeNew(int)
     */
    protected CopiableArray makeNew(int size)
    {
        return new ROI3D(size);
    }

    public void set(PlaneArea pa, int z)
    {
        set(pa, z);
    }
    
    public void set(Copiable planeArea, int z)
    {
        if (!(planeArea instanceof PlaneArea))
            throw new IllegalArgumentException();
        super.set(planeArea, z);
    }
    
    public PlaneArea getPlaneArea(int z)
    {
        return (PlaneArea) get(z);
    }
    
}
