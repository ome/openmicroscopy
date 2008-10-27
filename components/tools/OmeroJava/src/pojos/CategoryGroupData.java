/*
 * pojos.CategoryGroupData
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package pojos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static omero.rtypes.*;
import omero.model.CategoryGroup;
import omero.model.CategoryGroupCategoryLink;
import omero.model.CategoryGroupI;

/**
 * The data that makes up an <i>OME</i> Category Group along with links to its
 * contained Categories and the Experimenter that defined this group &#151; and
 * hence, owns it.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date$) </small>
 * @since OME2.2
 */
public class CategoryGroupData extends DataObject {

    /** Identifies the {@link CategoryGroup#NAME} field. */
    public final static String NAME = CategoryGroupI.NAME;

    /** Identifies the {@link CategoryGroup#DESCRIPTION} field. */
    public final static String DESCRIPTION = CategoryGroupI.DESCRIPTION;

    /** Identifies the {@link CategoryGroup#CATEGORYLINKS} field. */
    public final static String CATEGORY_LINKS = CategoryGroupI.CATEGORYLINKS;

    /**
     * All the Categories contained in this Category Group. The elements of this
     * set are {@link CategoryData} objects. If this Category Group contains no
     * Categorie, then this set will be empty &#151; but never <code>null</code>.
     */
    private Set categories;

    /** Creates a new instance. */
    public CategoryGroupData() {
        setDirty(true);
        setValue(new CategoryGroupI());
    }

    /**
     * Creates a new instance.
     * 
     * @param group
     *            Back pointer to the {@link CategoryGroup} model object.
     *            Mustn't be <code>null</code>.
     * @throws IllegalArgumentException
     *             If the object is <code>null</code>.
     */
    public CategoryGroupData(CategoryGroup group) {
        setValue(group);
    }

    // Immutables
    /**
     * Sets the name of the category group.
     * 
     * @param name
     *            The name of the category group. Mustn't be <code>null</code>.
     * @throws IllegalArgumentException
     *             If the name is <code>null</code>.
     */
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("The name cannot be null.");
        }
        setDirty(true);
        asCategoryGroup().setName(rstring(name));
    }

    /**
     * Returns the name of the category group.
     * 
     * @return See above.
     */
    public String getName() {
        omero.RString n = asCategoryGroup().getName();
        if (n == null || n.getValue() == null) {
            throw new IllegalStateException(
                    "The name should never have been null.");
        }
        return n.getValue();
    }

    /**
     * Sets the description of the category group.
     * 
     * @param description
     *            The description of the category group.
     */
    public void setDescription(String description) {
        setDirty(true);
        asCategoryGroup().setDescription(rstring(description));
    }

    /**
     * Returns the description of the category group.
     * 
     * @return See above.
     */
    public String getDescription() {
        omero.RString d = asCategoryGroup().getDescription();
        return d == null ? null : d.getValue();
    }

    // Lazy loaded sets

    /**
     * Returns the categories contained in this category group.
     * 
     * @return See above.
     */
    public Set getCategories() {
        if (categories == null && asCategoryGroup().sizeOfCategoryLinks() >= 0) {
            categories = new HashSet<CategoryGroupData>();
            List<CategoryGroupCategoryLink> links = asCategoryGroup()
                    .copyCategoryLinks();
            for (CategoryGroupCategoryLink link : links) {
                categories.add(new CategoryData(link.getChild()));
            }
        }
        return categories == null ? null : new HashSet(categories);
    }

    // Set mutations

    /**
     * Sets the categories contained in this category group.
     * 
     * @param newValue
     *            The set of images.
     */
    public void setCategories(Set<CategoryData> newValue) {
        Set<CategoryData> currentValue = getCategories();
        SetMutator<CategoryData> m = new SetMutator<CategoryData>(currentValue,
                newValue);

        while (m.moreDeletions()) {
            setDirty(true);
            asCategoryGroup().unlinkCategory(m.nextDeletion().asCategory());
        }

        while (m.moreAdditions()) {
            setDirty(true);
            asCategoryGroup().linkCategory(m.nextAddition().asCategory());
        }

        categories = new HashSet<CategoryData>(m.result());
    }

}
