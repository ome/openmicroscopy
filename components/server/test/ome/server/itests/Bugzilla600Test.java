/*
 * ome.server.itests.Bugzilla600Test
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
package ome.server.itests;

// Java imports
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

// Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

// Application-internal dependencies
import ome.aop.ApiConstraintChecker;
import ome.api.Pojos;
import ome.model.CategoryGroup;
import ome.security.Utils;
import ome.util.builders.PojoOptions;

/**
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 2.0
 */
public class Bugzilla600Test
        extends AbstractDependencyInjectionSpringContextTests
{

    protected static Log log = LogFactory.getLog(Bugzilla600Test.class);

    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations()
    {

        return ConfigHelper.getConfigLocations();
    }

    @Override
    protected void onSetUp() throws Exception
    {
        super.onSetUp();
        Utils.setUserAuth();
    }

    private Pojos psrv;

    public void testBug609StackOverFlowError() throws Exception
    {
        try
        {
            Set args = new HashSet(Arrays.asList(new Date()));
            psrv.findContainerHierarchies(CategoryGroup.class, args,
                    new PojoOptions().annotationsFor(1).exp(1).map());
            fail("There should be a boom");
        } catch (IllegalArgumentException iae)
        {
            // expected
            iae.getStackTrace()[0].getClassName().equals(
                    ApiConstraintChecker.class.getName());
        }

        try
        {
            psrv.findContainerHierarchies(CategoryGroup.class, null, null);
            fail("There should be a IllegalArgumentException here");
        } catch (IllegalArgumentException iae)
        {
            // super
            iae.getStackTrace()[0].getClassName().equals(
                    ApiConstraintChecker.class.getName());
        }
    }

    public void testOutOfMemoryError() throws Exception
    {
        Object o = psrv.getUserImages(new PojoOptions().exp(642).map());
        System.out.println(ome.util.Utils.structureSize(o));
    }
    
    public void setPsrv(Pojos psrv)
    {
        this.psrv = psrv;
    }

}
