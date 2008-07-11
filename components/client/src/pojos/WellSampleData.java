/*
 * pojos.WellSampleData 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package pojos;


//Java imports
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.model.IObject;
import ome.model.core.Image;
import ome.model.screen.WellSample;
import ome.util.CBlock;

/** 
 * The data that makes up an <i>OME</i> WellSample along with links to its
 * images and the Experimenter that owns this WellSample.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
public class WellSampleData 
	extends DataObject
{

    /**
     * All the images that are linked to this well sample.
     * The elements of this set are {@link ImageData} objects. 
     * If this well sample is not linked to any Image, 
     * then this set will be empty &#151; but never <code>null</code>.
     */
    private Set<ImageData> images;
    
	/** Creates a new instance. */
    public WellSampleData()
    {
        setDirty(true);
        setValue(new WellSample());
    }
    
    /**
     * Creates a new instance.
     * 
     * @param wellSample Back pointer to the {@link WellSample} model object. 
     * 			   		 Mustn't be <code>null</code>.
     * @throws IllegalArgumentException If the object is <code>null</code>.
     */
    public WellSampleData(WellSample wellSample)
    {
        if (wellSample == null) 
            throw new IllegalArgumentException("Object cannot null.");
        setValue(wellSample);
    }
    
    /**
     * Returns the image related to that sample if any. Possible values are
     * <code>0</code> or <code>1</code>.
     * 
     * @return See above.
     */
    public ImageData getImage()
    {
    	Set img = getImages();
    	if (img == null) return null;
    	//Should have only one.
    	Iterator i = img.iterator();
    	while (i.hasNext()) 
			return (ImageData) i.next();
		return null;
    }
    
//  Lazy loaded links

    /**
     * Returns a set of images linked to this well sample
     * 
     * @return See above.
     */
    public Set getImages()
    {
    	/*
        if (images == null && asWellSample().sizeOfImageLinks() >= 0) {
            images = new HashSet<ImageData>(
            		asWellSample().eachLinkedImage(new CBlock() {
                public ImageData call(IObject object) {
                    return new ImageData((Image) object);
                }
            }));
        }
        return images == null ? null : new HashSet<ImageData>(images);
        */
    	return null;
    }
    
//  Link mutations

    /**
     * Sets the images linked to this well sample
     * 
     * @param newValue
     *            The set of images.
     */
    public void setImages(Set<ImageData> newValue)
   	{
    	/*
        Set<ImageData> currentValue = getImages();
        SetMutator<ImageData> 
        	m = new SetMutator<ImageData>(currentValue, newValue);

        while (m.moreDeletions()) {
            setDirty(true);
            asWellSample().unlinkImage(m.nextDeletion().asImage());
        }

        while (m.moreAdditions()) {
            setDirty(true);
            asWellSample().linkImage(m.nextAddition().asImage());
        }

        images = new HashSet<ImageData>(m.result());
        */
    }
    
}
