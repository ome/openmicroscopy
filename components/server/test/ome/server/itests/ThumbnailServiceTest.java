/*
 * ome.server.itests.ThumbnailServiceTest
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

//Java imports

//Third-party libraries
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

//Application-internal dependencies
import ome.api.IPixels;
import ome.api.IQuery;
import ome.api.IThumb;
import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;
import ome.model.enums.RenderingModel;import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.system.OmeroContext;
import ome.system.ServiceFactory;
import ome.testing.ObjectFactory;
import omeis.providers.re.RenderingEngine;


@Test(
	groups = "integration"
)
public class ThumbnailServiceTest
        extends
            AbstractManagedContextTest {

    private static Log log = LogFactory.getLog(ThumbnailServiceTest.class);

    private IThumb tb;

    private IQuery qs;
    
    // =========================================================================
    // ~ Testng Adapter
    // =========================================================================
    @Configuration(beforeTestMethod = true)
    public void adaptSetUp() throws Exception
    {
        super.setUp();
    }

    @Configuration(afterTestMethod = true)
    public void adaptTearDown() throws Exception
    {
        super.tearDown();
    }
    // =========================================================================
    
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        //ome.security.Utils.setUserAuth();
       tb = factory.getThumbnailService();
       qs = factory.getQueryService();
    }

    // Currently this assumes that all renderingdefs are valid. This may
    // not be the case. Need better integration with bioformats-omero.
    // see ticket:218
    @Test( groups = {"ticket:410","tickets:218"} )
    public void testThumbnailsDirect() throws Exception {
    	
		RenderingDef def = (RenderingDef) qs.findAllByQuery(
				"from RenderingDef where pixels.sizeX > 8 and pixels.sizeY > 8",
				null).get(0);
		
		Pixels p = qs.get(Pixels.class, 
				def.getPixels().getId());
		
		tb.getThumbnailDirect(p, def, 8, 8);
		
	}
    
}
