/*
 * Created on Jun 5, 2005
 */
package org.openmicroscopy.omero.logic;

import java.util.List;
import java.util.Set;

import org.openmicroscopy.omero.model.Category;
import org.openmicroscopy.omero.model.CategoryGroup;

/**
 * @author josh
 */
public interface ContainerDao {
    public Object loadHierarchy(final Class arg0, final int arg1);

    public List findPDIHierarchies(final Set arg0);

    public List findCGCIHierarchies(final Set arg0);

    /** load necessary because of the whackyness of CategoryGroup 
     * @DEV.TODO TEMPORARY this must be moved to its own DAO if it can't be abolished all together 
     * @param id
     * @return a CategoryGroup
     */
    public CategoryGroup loadCG(Integer id);

    /** load necessary because of the whackyness of Category 
     * @DEV.TODO TEMPORARY this must be moved to its own DAO if it can't be abolished all together 
     * @param id
     * @return a Category
     */
    public Category loadC(Integer id);
}