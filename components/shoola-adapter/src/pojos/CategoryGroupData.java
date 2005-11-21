/*
 * pojos.CategoryGroupData
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
import ome.api.OMEModel;
import ome.model.CategoryGroup;
import ome.util.ModelMapper;

/** 
 * The data that makes up an <i>OME</i> Category Group along with links to its
 * contained Categories and the Experimenter that defined this group &#151;
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
public class CategoryGroupData
    implements DataObject
{
    
    /** The Category Group ID. */
    private int      id;
    
    /** 
     * The Category Group's name.
     * This field may not be <code>null</code>.  
     */
    private String   name;
    
    /** The Category Group's description. */
    private String   description;
    
    /**
     * All the Categories contained in this Category Group.
     * The elements of this set are {@link CategoryData} objects.  If this
     * Category Group contains no Categories, then this set will be empty
     * &#151; but never <code>null</code>.
     */
    private Set      categories;
    
    /** 
     * The Experimenter that defined this Category Group.
     * This field may not be <code>null</code>.  
     */
    private ExperimenterData owner;
    
    public void copy(OMEModel model, ModelMapper mapper) {
    	if (model instanceof CategoryGroup) {
			CategoryGroup cg = (CategoryGroup) model;
			this.setId(mapper.nullSafeInt(cg.getAttributeId()));
			this.setName(cg.getName());
			this.setDescription(cg.getDescription());
			this.setCategories((Set) mapper.findCollection(cg.getCategories()));
		} else {
			throw new IllegalArgumentException("CategoryGroupData can only copy from CategoryGroup types"); // TODO unified erros.
		}
    }

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
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

	public void setCategories(Set categories) {
		this.categories = categories;
	}

	public Set getCategories() {
		return categories;
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
