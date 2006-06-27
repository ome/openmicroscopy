/*
 * ome.server.utests.ApiConstraintCheckerTest
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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
package ome.server.utests;

//Java imports

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.*;

import ome.annotations.ApiConstraintChecker;
import ome.annotations.NotNull;
import ome.annotations.Validate;
import ome.conditions.ApiUsageException;
import ome.conditions.ValidationException;
import ome.logic.PojosImpl;
import ome.model.IObject;
import ome.model.containers.Project;


//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies


/**
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since Omero 3.0
 */
public class ApiConstraintCheckerTest extends TestCase {

    Class c;
    Method m;
    Object[] args;
    
    @Test
    @ExpectedExceptions(ApiUsageException.class)
    public void testNullCheck() throws Exception {
        ApiConstraintChecker.errorOnViolation(null,null,null);
    }
 
    /*   
         public Set<IObject> loadContainerHierarchy(
         @NotNull Class<IObject> rootNodeType, 
         @Validate(Long.class) Set<Long> rootNodeIds,
         Map options);
    */

    @Test
    @ExpectedExceptions(ApiUsageException.class)
    public void testNullClass() throws Exception {
        loadContainerHierarchy();
        args = new Object[]{null, Collections.EMPTY_SET, null};
        ApiConstraintChecker.errorOnViolation(c,m,args);
    }
    
    @Test
    public void testNullSet() throws Exception {
        loadContainerHierarchy();
        args = new Object[]{Project.class, null, null};
        ApiConstraintChecker.errorOnViolation(c,m,args);
    }
    
    @Test
    @ExpectedExceptions(ApiUsageException.class)
    public void testIntegersInSet() throws Exception {
        loadContainerHierarchy();
        args = new Object[]{Project.class, Collections.singleton( new Integer(1) ), null};
        ApiConstraintChecker.errorOnViolation(c,m,args);
    }

    /*
        public <T extends IObject> Set<IObject> findContainerHierarchies(
            @NotNull Class<T> rootNodeType, 
            @NotNull @Validate(Long.class) Set<Long> imagesIds,
            Map options);
     */

    @Test
    @ExpectedExceptions(ApiUsageException.class)
    public void testNullClass_find() throws Exception {
        findContainerHierarchies();
        args = new Object[]{null, Collections.EMPTY_SET, null};
        ApiConstraintChecker.errorOnViolation(c,m,args);
    }
    
    @Test
    @ExpectedExceptions(ApiUsageException.class)
    public void testNullSet_find() throws Exception {
        findContainerHierarchies();
        args = new Object[]{Project.class, null, null};
        ApiConstraintChecker.errorOnViolation(c,m,args);
    }
    
    @Test
    @ExpectedExceptions(ApiUsageException.class)
    public void testIntegersInSet_find() throws Exception {
        findContainerHierarchies();
        args = new Object[]{Project.class, Collections.singleton( new Integer(1) ), null};
        ApiConstraintChecker.errorOnViolation(c,m,args);
    }
    
    @Test
    public void testGood_find() throws Exception {
        findContainerHierarchies();
        args = new Object[]{Project.class, Collections.singleton( new Long(1) ), null};
        ApiConstraintChecker.errorOnViolation(c,m,args);
    }
    
    // ~ Helpers
    // =========================================================================

    
    private void loadContainerHierarchy() throws NoSuchMethodException, SecurityException
    {
        c = PojosImpl.class;
        m = c.getMethod("loadContainerHierarchy",Class.class,Set.class,Map.class);
    }

    private void findContainerHierarchies() throws NoSuchMethodException, SecurityException
    {
        c = PojosImpl.class;
        m = c.getMethod("findContainerHierarchies",Class.class,Set.class,Map.class);
    }

    
}
