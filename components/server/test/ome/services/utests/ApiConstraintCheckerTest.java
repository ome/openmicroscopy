/*
 * ome.services.utests.ApiConstraintCheckerTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.utests;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

import junit.framework.TestCase;
import ome.annotations.ApiConstraintChecker;
import ome.conditions.ApiUsageException;
import ome.logic.PojosImpl;
import ome.model.IObject;
import ome.model.containers.Project;
import ome.services.RenderingBean;

import org.testng.annotations.Test;

/**
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since Omero 3.0
 */
public class ApiConstraintCheckerTest extends TestCase {

    Class c;

    Method m;

    Object[] args;

    @Test
    public void testInterface() throws Exception {
        c = IObject.class;
        m = IObject.class.getMethod("isLoaded");
        args = null;
        ApiConstraintChecker.errorOnViolation(c, m, args);
    }

    @Test
    public void testNotService() throws Exception {
        c = Project.class;
        m = Project.class.getMethod("isLoaded");
        args = null;
        ApiConstraintChecker.errorOnViolation(c, m, args);
    }

    @Test
    public void testRenderingImplGetModel() throws Exception {
        c = RenderingBean.class;
        m = RenderingBean.class.getMethod("getModel");
        args = null;
        ApiConstraintChecker.errorOnViolation(c, m, args);
    }

    @Test
    public void testRenderingBeanGetModel() throws Exception {
        c = RenderingBean.class;
        m = RenderingBean.class.getMethod("getModel");
        args = null;
        ApiConstraintChecker.errorOnViolation(c, m, args);
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testNullCheck() throws Exception {
        ApiConstraintChecker.errorOnViolation(null, null, null);
    }

    /*
     * public Set<IObject> loadContainerHierarchy( @NotNull Class<IObject>
     * rootNodeType, @Validate(Long.class) Set<Long> rootNodeIds, Map options);
     */

    @Test(expectedExceptions = ApiUsageException.class)
    public void testNullClass() throws Exception {
        loadContainerHierarchy();
        args = new Object[] { null, Collections.EMPTY_SET, null };
        ApiConstraintChecker.errorOnViolation(c, m, args);
    }

    @Test
    public void testNullSet() throws Exception {
        loadContainerHierarchy();
        args = new Object[] { Project.class, null, null };
        ApiConstraintChecker.errorOnViolation(c, m, args);
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testIntegersInSet() throws Exception {
        loadContainerHierarchy();
        args = new Object[] { Project.class,
                Collections.singleton(new Integer(1)), null };
        ApiConstraintChecker.errorOnViolation(c, m, args);
    }

    /*
     * public <T extends IObject> Set<IObject> findContainerHierarchies(
     * @NotNull Class<T> rootNodeType, @NotNull @Validate(Long.class) Set<Long>
     * imagesIds, Map options);
     */

    @Test(expectedExceptions = ApiUsageException.class)
    public void testNullClass_find() throws Exception {
        findContainerHierarchies();
        args = new Object[] { null, Collections.EMPTY_SET, null };
        ApiConstraintChecker.errorOnViolation(c, m, args);
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testNullSet_find() throws Exception {
        findContainerHierarchies();
        args = new Object[] { Project.class, null, null };
        ApiConstraintChecker.errorOnViolation(c, m, args);
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testIntegersInSet_find() throws Exception {
        findContainerHierarchies();
        args = new Object[] { Project.class,
                Collections.singleton(new Integer(1)), null };
        ApiConstraintChecker.errorOnViolation(c, m, args);
    }

    @Test
    public void testGood_find() throws Exception {
        findContainerHierarchies();
        args = new Object[] { Project.class,
                Collections.singleton(new Long(1)), null };
        ApiConstraintChecker.errorOnViolation(c, m, args);
    }

    // ~ Helpers
    // =========================================================================

    private void loadContainerHierarchy() throws NoSuchMethodException,
            SecurityException {
        c = PojosImpl.class;
        m = c.getMethod("loadContainerHierarchy", Class.class, Set.class,
                ome.parameters.Parameters.class);
    }

    private void findContainerHierarchies() throws NoSuchMethodException,
            SecurityException {
        c = PojosImpl.class;
        m = c.getMethod("findContainerHierarchies", Class.class, Set.class,
                ome.parameters.Parameters.class);
    }

}
