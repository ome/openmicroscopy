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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.model.IObject;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.CategoryGroupCategoryLink;
import ome.util.ModelMapper;
import ome.util.ReverseModelMapper;

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
    extends DataObject
{

    public final static String NAME = CategoryGroup.NAME;
    public final static String DESCRIPTION = CategoryGroup.DESCRIPTION;
    public final static String CATEGORY_LINKS = CategoryGroup.CATEGORYLINKS;
    
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
    
    public void copy(IObject model, ModelMapper mapper) {
    	if (model instanceof CategoryGroup) {
			CategoryGroup cg = (CategoryGroup) model;
			super.copy(model,mapper);

            // Details
            if (cg.getDetails()!=null){
                this.setOwner((ExperimenterData) 
                        mapper.findTarget(
                                cg.getDetails().getOwner()));
            }
            
            // Fields
			this.setName(cg.getName());
			this.setDescription(cg.getDescription());
            
            // Collections 
			if (cg.getCategoryLinks() != null){
			    Set categories = new HashSet();
                for (Iterator i = cg.getCategoryLinks().iterator(); i.hasNext();)
                {
                    CategoryGroupCategoryLink cgcl = (CategoryGroupCategoryLink) i.next();
                    categories.add(cgcl.child());
                }
                this.setCategories((Set) mapper.findCollection(categories));
                // FIXME this won't work. Needs CGCL as pointer to original
                // otherwise you get non-referential integrity
            }

		} else {
			throw new IllegalArgumentException("CategoryGroupData can only copy from CategoryGroup types"); // TODO unified erros.
		}
    }
    
    public IObject asIObject(ReverseModelMapper mapper)
    {
        CategoryGroup cg = new CategoryGroup();
        if (super.fill(cg)) {
            cg.setName(this.getName());
            cg.setDescription(this.getDescription());
            for (Iterator it = this.getCategories().iterator(); it.hasNext();)
            {
                CategoryData c = (CategoryData) it.next();
                cg.addCategory((Category)mapper.map(c));
            }
        }
        return cg;
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
	
}
