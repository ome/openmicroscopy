/*
 * pojos.DatasetData
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
import java.util.Set;

//Third-party libraries

//Application-internal dependencies

/** 
 * The data that makes up an <i>OME</i> Dataset along with links to its
 * contained Images and enclosing Project as well as the Experimenter that 
 * owns this Dataset.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class DatasetData
    implements DataObject
{

    /** The Dataset ID. */
    public int      id;
    
    /** 
     * The Dataset's name.
     * This field may not be <code>null</code>.  
     */
    public String   name;
    
    /** The Dataset's description. */
    public String   description;
    
    /** 
     * All the Images contained in this Dataset.
     * The elements of this set are {@link ImageData} objects.  If this
     * Dataset contains no Images, then this set will be empty &#151;
     * but never <code>null</code>. 
     */
    public Set      images;
    
    /** 
     * All the Projects that contain this Dataset.
     * The elements of this set are {@link ProjectData} objects.  If this
     * Dataset is not contained in any Project, then this set will be empty
     * &#151; but never <code>null</code>. 
     */
    public Set      projects;
    
    /** 
     * The Experimenter that owns this Dataset.
     * This field may not be <code>null</code>.  
     */
    public ExperimenterData owner;
    
}
