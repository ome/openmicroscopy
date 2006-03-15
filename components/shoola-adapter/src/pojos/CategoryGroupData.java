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
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.model.IObject;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.util.CBlock;

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

    /** Identifies the {@link CategoryGroup#NAME} field. */
    public final static String NAME = CategoryGroup.NAME;
    
    /** Identifies the {@link CategoryGroup#DESCRIPTION} field. */
    public final static String DESCRIPTION = CategoryGroup.DESCRIPTION;
    
    /** Identifies the {@link CategoryGroup#CATEGORYLINKS} field. */
    public final static String CATEGORY_LINKS = CategoryGroup.CATEGORYLINKS;
    
    /**
     * All the Categories contained in this Category Group.
     * The elements of this set are {@link CategoryData} objects. If this
     * Category Group contains no Categorie, then this set will be empty
     * &#151; but never <code>null</code>.
     */
    private Set      categories;

    /** Creates a new instance. */
    public CategoryGroupData()
    {
        setDirty(true);
        setValue(new CategoryGroup());
    }
    
    /**
     * Creates a new instance.
     * 
     * @param group     Back pointer to the {@link CategoryGroup} model object.
     *                  Mustn't be <code>null</code>.
     * @throws IllegalArgumentException If the object is <code>null</code>.
     */
    public CategoryGroupData(CategoryGroup group)
    {
        setValue(group);
    }
    
    // Immutables
    /**
     * Sets the name of the category group.
     * 
     * @param name The name of the category group. Mustn't be <code>null</code>.
     * @throws IllegalArgumentException If the name is <code>null</code>.
     */
    public void setName(String name)
    {
        if (name == null) 
            throw new IllegalArgumentException("The name cannot be null.");
        setDirty(true);
        asCategoryGroup().setName(name);
    }

    /** 
     * Returns the name of the category group.
     * 
     * @return See above.
     */
    public String getName() { return asCategoryGroup().getName(); }

    /**
     * Sets the description of the category group.
     * 
     * @param description The description of the category group.
     */
    public void setDescription(String description)
    {
        setDirty(true);
        asCategoryGroup().setDescription(description);
    }

    /**
     * Returns the description of the category group.
     * 
     * @return See above.
     */
    public String getDescription()
    {
        return asCategoryGroup().getDescription();
    }

    // Lazy loaded sets
    
    /**
     * Returns the categories contained in this category group.
     * 
     * @return See above.
     */
    public Set getCategories()
    {
        if (categories == null && asCategoryGroup().sizeOfCategoryLinks() >= 0)
        {
            categories = new HashSet( asCategoryGroup().eachLinkedCategory(
                    new CBlock() {
                        public Object call(IObject object)
                        {
                            return new CategoryData( (Category) object );
                        }
                    }));
        }
        return categories == null ? null : new HashSet( categories );
    }
    
    // Set mutations
    
    /**
     * Sets the categories contained in this category group.
     * 
     * @param newValue The set of images.
     */
    public void setCategories(Set newValue) 
    {
        Set currentValue = getCategories(); 
        SetMutator m = new SetMutator(currentValue, newValue);
        
        while (m.moreDeletions()) {
            setDirty(true);
            asCategoryGroup().unlinkCategory(m.nextDeletion().asCategory() );
        }
        
        while (m.moreAdditions()) {
            setDirty(true);
            asCategoryGroup().linkCategory(m.nextAddition().asCategory());
        }

        categories = m.result();
    }
    
}
