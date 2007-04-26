/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio.utests;

import org.testng.annotations.*;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.core.Pixels;
import ome.model.enums.PixelsType;
import java.util.Properties;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class PixelServiceCreatesDirectoryUnitTest extends TestCase {
    private Pixels pixels;

    private PixelBuffer pixelBuffer;

    private PixelsService service;
	
	private ApplicationContext testContext;

    @Override
    @Configuration(beforeTestMethod = true)
    protected void setUp() {
        pixels = new Pixels();
        pixels.setId(1234567890123L);
        pixels.setSizeX(256);
        pixels.setSizeY(256);
        pixels.setSizeZ(64);
        pixels.setSizeC(3);
        pixels.setSizeT(50);

        PixelsType type = new PixelsType();
        type.setValue("uint16");
        pixels.setPixelsType(type);
		testContext = new FileSystemXmlApplicationContext(
    			"components/romio/resources/beanRefContext.xml");
		PropertiesUtil util = (PropertiesUtil) testContext.getBean("properties");
		String path = util.getProperties().getProperty("omero.data.dir");		
        service = new PixelsService(path);
    }

    @Test
    public void testLargeId() throws Exception {
        pixelBuffer = service.createPixelBuffer(pixels);
    }

}
