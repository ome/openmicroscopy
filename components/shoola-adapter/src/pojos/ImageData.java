/*
 * pojos.ImageData
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

package pojos;


//Java imports
import java.sql.Timestamp;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies

/** 
 * The data that makes up an <i>OME</i> Image along with links to its
 * Pixels, enclosing Datasets, and the Experimenter that owns this Image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.1 $ $Date: 2005/05/09 19:50:41 $)
 * </small>
 * @since OME2.2
 */
public class ImageData
    implements DataObject
{

    /** The Image ID. */
    public int      id;
    
    /** 
     * The Image's name.
     * This field may not be <code>null</code>.  
     */
    public String   name;
    
    /** The Image's description. */
    public String   description;
    
    /**
     * The creation timestamp.
     * That is the time at which the Image was created.
     * This field may not be <code>null</code>.
     */
    public Timestamp  created;
    
    /**
     * The insertion timestamp.
     * That is the time at which the Image was inserted into the DB.
     * This field may not be <code>null</code>.
     */
    public Timestamp  inserted;
    
    /**
     * The default image data associated to this Image.
     * An <i>OME</i> Image can be associated to more than one 5D pixels set
     * (that is, the raw image data) if all those sets are derived from an
     * initial image file.  An example is a deconvolved image and the original
     * file: those two pixels sets would be represented by the same <i>OME</i>
     * Image.  
     * In the case there's more than one pixels set, this field identifies the
     * pixels that are used by default for analysis and visualization.  If the
     * Image only has one pixels set, then this field just points to that set.
     * This field may not be <code>null</code>.
     */
    public PixelsData defaultPixels;
    
    /**
     * All the Pixels that belong to this Image.
     * The elements of this set are {@link PixelsData} objects.
     * This field may not be <code>null</code> nor empty.  As a minimum, it
     * will contain the {@link #defaultPixels default} Pixels.
     * 
     * @see #defaultPixels
     */
    public Set        allPixels;
    
    /** 
     * All the Datasets that contain this Image.
     * The elements of this set are {@link DatasetData} objects.  If this
     * Image is not contained in any Dataset, then this set will be empty
     * &#151; but never <code>null</code>. 
     */
    public Set      datasets;
    
    /**
     * All the annotations related to this Image.
     * The elements of the set are {@link AnnotationData} objetcs.
     * If this Image hasn't been annotated, then this set will be empty
     * &#151; but never <code>null</code>. 
     */
    public Set      annotations;
    
    /** 
     * The Experimenter that owns this Dataset.
     * This field may not be <code>null</code>.  
     */
    public ExperimenterData owner;
    
}
