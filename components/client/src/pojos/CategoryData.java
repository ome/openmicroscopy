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

// Java imports
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Third-party libraries

// Application-internal dependencies
import ome.model.IObject;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.core.Image;
import ome.util.CBlock;

/**
 * The data that makes up an <i>OME</i> Category along with a back pointer to
 * the Category Group that contains this Category and all the Images that have
 * been classified under this Category. Moreover there's a link to the
 * Experimenter that defined this Category &#151; and hence, owns it.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision: $ $Date: $)
 *          </small>
 * @since OME2.2
 */
public class CategoryData
    extends DataObject
{

    /** Identifies the {@link Category#NAME} field. */
    public final static String NAME = Category.NAME;

    /** Identifies the {@link Category#DESCRIPTION} field. */
    public final static String DESCRIPTION = Category.DESCRIPTION;

    /** Identifies the {@link Category#IMAGELINKS} field. */
    public final static String IMAGES = Category.IMAGELINKS;

    /** Identifies the {@link Category#CATEGORYGROUPLINKS} field. */
    public final static String CATEGORY_GROUP_LINKS = 
                                            Category.CATEGORYGROUPLINKS;

    /**
     * All the Images classified under this Category. The elements of this set
     * are {@link ImageData} objects. If this Category contains no Image, then
     * this set will be empty &#151; but never <code>null</code>.
     */
    private Set                images;

    /** The Category Group this Category belongs in. */
    private CategoryGroupData  group;

    /** Creates a new instance. */
    public CategoryData()
    {
        setDirty(true);
        setValue(new Category());
    }

    /**
     * Creates a new instance.
     * 
     * @param category  Back pointer to the {@link Category} model object.
     *                  Mustn't be <code>null</code>.
     * @throws IllegalArgumentException If the object is <code>null</code>.
     */
    public CategoryData(Category category)
    {
        if (category == null)
            throw new IllegalArgumentException("Category cannot null.");
        setValue(category);
    }

    // Immutables
    
    /**
     * Sets the name of the category.
     * 
     * @param name The name of the category. Mustn't be <code>null</code>.
     * @throws IllegalArgumentException If the name is <code>null</code>.
     */
    public void setName(String name)
    {
        if (name == null) 
            throw new IllegalArgumentException("The name cannot be null.");
        setDirty(true);
        asCategory().setName(name);
    }

    /** 
     * Returns the name of the category.
     * 
     * @return See above.
     */
    public String getName() { return asCategory().getName(); }

    /**
     * Sets the description of the category.
     * 
     * @param description The description of the category.
     */
    public void setDescription(String description)
    {
        setDirty(true);
        asCategory().setDescription(description);
    }

    /**
     * Returns the description of the category.
     * 
     * @return See above.
     */
    public String getDescription() { return asCategory().getDescription(); }

    // Singleton set.
    
    /**
     * Returns the Category group this category belongs to.
     * 
     * @return See above.
     */
    public CategoryGroupData getGroup()
    {
        if (group == null && asCategory().sizeOfCategoryGroupLinks() >= 0) {
            List list = asCategory().linkedCategoryGroupList();
            if (list != null)
                if (list.size() > 0 )
                    group = new CategoryGroupData((CategoryGroup) list.get(0));
                else
                    ;// TODO what now?
        }
        return group;
    }

    /**
     * Sets the Category group this category belongs to.
     * 
     * @param group The group the category belongs to.
     */
    public void setGroup(CategoryGroupData group)
    {
        if (group == getGroup()) return;
        setDirty(true);
        this.group = group;
        asCategory().clearCategoryGroupLinks();
        if (group != null)
            asCategory().linkCategoryGroup(group.asCategoryGroup());
    }

    // Lazy loaded links
    
    /**
     * Returns a set of images contained in the category.
     *
     * @return See above.
     */
    public Set getImages()
    {
        if (images == null && asCategory().sizeOfImageLinks() >= 0) {
            images = new HashSet(asCategory().eachLinkedImage(new CBlock() {
                public Object call(IObject object)
                {
                    return new ImageData((Image) object);
                }
            }));
        }
        return images == null ? null : new HashSet(images);
    }

    // Link mutators
    
    /**
     * Sets the images contained in this category.
     * 
     * @param newValue The set of images.
     */
    public void setImages(Set newValue )
    {
        Set currentValue = getImages(); 
        SetMutator m = new SetMutator(currentValue, newValue);
        ImageData imgData;
        Set categories;
        while (m.moreDeletions()) {
            setDirty(true);
            imgData = (ImageData) m.nextAddition();
            asCategory().unlinkImage(imgData.asImage());
            categories = imgData.getCategories();
            categories.remove(this);
            imgData.setCategories(categories);
        }
        
        while (m.moreAdditions()) {
            setDirty(true);
            imgData = (ImageData) m.nextAddition();
            asCategory().linkImage(imgData.asImage());
            categories = imgData.getCategories();
            categories.add(this);
            imgData.setCategories(categories);
        }
        images = m.result();
    }

}
