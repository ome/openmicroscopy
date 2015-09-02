/*
 * pojos.MultiImageData 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package omero.gateway.model;

//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import omero.model.OriginalFile;

/** 
 * Handle image file composed of multiples files e.g. lei. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class MultiImageData 
	extends FileData
{

    /** The collection of files composing the image. */
    private List<ImageData> components;
    
    /** 
     * Creates a new instance. 
     * 
     * @param image The object to host.
     */
    public MultiImageData(OriginalFile image)
    {
        super(image);
    }
    
    /**
     * Returns the collection of images composing the file.
     * 
     * @return See above.
     */
    public List<ImageData> getComponents()
    {
    	return components;
    }
    
    /**
     * Sets the images composing the file.
     * 
     * @param components The value to set.
     */
    public void setComponents(List<ImageData> components)
    { 
    	this.components = components;
    }
    
}
