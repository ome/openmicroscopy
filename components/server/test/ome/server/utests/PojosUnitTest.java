/*
 * ome.server.utests.PojosUnitTest
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

//Third-party libraries
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

//Application-internal dependencies
import ome.dao.AnnotationDao;
import ome.dao.ContainerDao;
import ome.dao.DaoFactory;
import ome.logic.PojosImpl;

/**
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since Omero 2.0
 */
public class PojosUnitTest extends MockObjectTestCase {
    protected PojosImpl manager;
    protected Mock annotationDao,containerDao;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        annotationDao = new Mock(AnnotationDao.class);
        containerDao = new Mock(ContainerDao.class);
        DaoFactory factory = new DaoFactory(null,(AnnotationDao) annotationDao.proxy(), (ContainerDao) containerDao.proxy(),null,null);
        manager = new PojosImpl(factory);
    }
    
    protected void tearDown() throws Exception {
        manager = null;
        annotationDao = null;
        containerDao = null;
    }
    
    public void testEmpty(){
    	//
    }
    
}
