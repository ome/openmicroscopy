/*
 * ome.server.itests.ThumbnailServiceTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
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
import ome.api.ThumbnailStore;
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

    private ThumbnailStore tb;

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
       tb = factory.createThumbnailService();
       qs = factory.getQueryService();
    }

    // Currently this assumes that all renderingdefs are valid. This may
    // not be the case. Need better integration with bioformats-omero.
    // see ticket:218
    @Test( groups = {"ticket:410","tickets:218","broken"} )
    public void testThumbnailsDirect() throws Exception {
    	
		RenderingDef def = (RenderingDef) qs.findAllByQuery(
				"from RenderingDef where pixels.sizeX > 8 and pixels.sizeY > 8"+
				" and details.owner.id = :id and pixels.details.owner.id = :id",
				new Parameters().addId( iAdmin.getEventContext().getCurrentUserId()))
				.get(0);
		if ( def != null )
		{
			Pixels p = qs.get(Pixels.class, 
					def.getPixels().getId());
			
			tb.setPixelsId(p.getId());
			tb.setRenderingDefId(def.getId());
			tb.getThumbnailDirect(8, 8);
		}		
	}
    
}
