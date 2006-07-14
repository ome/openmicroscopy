/*
 * ome.testing.MockServiceFactory
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
package ome.testing;

// Java imports

// Third-party libraries
import org.jmock.Mock;

// Application-internal dependencies
import ome.api.IAdmin;
import ome.api.IAnalysis;
import ome.api.IPixels;
import ome.api.IPojos;
import ome.api.IQuery;
import ome.api.ITypes;
import ome.api.IUpdate;
import ome.system.ServiceFactory;
import omeis.providers.re.RenderingEngine;

/**
 * <a href="http://jmock.org">JMock'ed</a> ServiceFactory whose public fields
 * can be set like:
 *  
 * <code>
 *   mockServiceFactory.mockUpdate = mock(IUpdate.class);
 * </code>
 * 
 * and later used like:
 *  
 * <code>
 *   mockServiceFactory.mockUpdate.expects( once() ).method( "saveObject" );
 * </code>
 * 
 * when the test-case subclasses {@link org.jmock.MockObjectTestCase}.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 3.0
 * @see org.jmock.MockObjectTestCase
 */
public class MockServiceFactory extends ServiceFactory
{
	
	@Override
	protected String getDefaultContext() {
		return null;
	}
    
	public Mock mockAdmin;
	public IAdmin getAdminService()
	{
		return (IAdmin) mockAdmin.proxy( ); 
	}
	
    public Mock mockAnalysis;
    public IAnalysis getAnalysisService()
    {
        return (IAnalysis) mockAnalysis.proxy( );
    }

    public Mock mockPixels;
    public IPixels getPixelsService()
    {
        return (IPixels) mockPixels.proxy( );
    }

    public Mock mockPojos;
    public IPojos getPojosService()
    {
        return (IPojos) mockPojos.proxy( );
    }

    public Mock mockQuery;
    public IQuery getQueryService()
    {
        return (IQuery) mockQuery.proxy( );
    }

    public Mock mockRendering;
    public RenderingEngine getRenderingService()
    {
        return (RenderingEngine) mockRendering.proxy( );
    }
    
    public Mock mockUpdate;
    public IUpdate getUpdateService()
    {
        return (IUpdate) mockUpdate.proxy( );
    }
    
    public Mock mockTypes;
    public ITypes getTypesService()
    {
    	return (ITypes) mockTypes.proxy();
    }

}