/*
 * pojos.CategoryData
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.model.IObject;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroupCategoryLink;
import ome.model.containers.CategoryImageLink;
import ome.model.internal.Details;
import ome.util.ModelMapper;

/** 
 * The data that makes up an <i>OME</i> Category along with a back pointer
 * to the Category Group that contains this Category and all the Images that
 * have been classified under this Category.
 * Morover there's a link to the Experimenter that defined this Category &#151;
 * and hence, owns it.
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
public class CategoryData
    implements DataObject
{

    /** The Category ID. */
    private long      id;
    
    /** 
     * The Category's name.
     * This field may not be <code>null</code>.  
     */
    private String   name;
    
    /** The Category's description. */
    private String   description;
    
    /** 
     * All the Images classified under this Category.
     * The elements of this set are {@link ImageData} objects.  If this
     * Category contains no Images, then this set will be empty &#151;
     * but never <code>null</code>. 
     */
    private Set      images;
    
    /** The Category Group this Category belongs in. */
    private CategoryGroupData group;
    
    /** 
     * The Experimenter that defined this Category Group.
     * This field may not be <code>null</code>.  
     */
    private ExperimenterData owner;
    
    public void copy(IObject model, ModelMapper mapper) {
    	if (model instanceof Category) {
			Category c = (Category) model;
			this.setId(mapper.nullSafeLong(c.getId()));
			this.setName(c.getName());
			this.setDescription(c.getDescription());
			Set _images = new HashSet();
            if (null != c.getImageLinks()) {
			for (Iterator i = c.getImageLinks().iterator(); i.hasNext();) {
                CategoryImageLink cil = (CategoryImageLink) i.next();
				if (cil.child()!=null){
					_images.add(mapper.findTarget(cil.child()));
				}
			}
            }
			this.setImages(_images);
            
            Set _categories = c.getCategoryGroupLinks();
            if (_categories != null && _categories.size() > 0) {
                // FIXME and if size > 1
                CategoryGroupCategoryLink cgcl = 
                    (CategoryGroupCategoryLink)_categories.iterator().next(); 
                this.setGroup((CategoryGroupData) 
                        mapper.findTarget(cgcl.parent()));    
            }
			Details details = c.getDetails();
			if (details!=null){
				this.setOwner((ExperimenterData) mapper.findTarget(details.getOwner()));
			}
		} else {
			throw new IllegalArgumentException("CategoryData can only copy Category type.");
		}
    }

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setImages(Set images) {
		this.images = images;
	}

	public Set getImages() {
		return images;
	}

	public void setGroup(CategoryGroupData group) {
		this.group = group;
	}

	public CategoryGroupData getGroup() {
		return group;
	}

	public void setOwner(ExperimenterData owner) {
		this.owner = owner;
	}

	public ExperimenterData getOwner() {
		return owner;
	}
	
	public String toString() {
		return getClass().getName()+":"+getName()+" (id="+getId()+")";
	}
    
}
