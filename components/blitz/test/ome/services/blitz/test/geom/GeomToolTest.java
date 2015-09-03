/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test.geom;

import static omero.rtypes.rfloat;
import static omero.rtypes.rint;
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;

import java.util.List;

import junit.framework.TestCase;

import ome.api.IPixels;
import ome.io.nio.PixelsService;
import ome.services.roi.GeomTool;
import ome.services.roi.PixelData;
import ome.services.util.Executor;
import ome.system.OmeroContext;
import ome.tools.hibernate.SessionFactory;
import ome.util.SqlAction;
import omero.model.Shape;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@Test(groups = "integration")
public class GeomToolTest extends TestCase {

    protected OmeroContext ctx;

    protected PixelData data;

    protected GeomTool geomTool;

    protected SessionFactory factory;

    protected SqlAction sql;

    protected Executor ex;

    protected String uuid;

    @BeforeTest
    public void setup() {
        ctx = OmeroContext.getManagedServerContext();
        sql = (SqlAction) ctx.getBean("simpleSqlAction");
        factory = (SessionFactory) ctx.getBean("omeroSessionFactory");
        data = new PixelData((PixelsService) ctx.getBean("/OMERO/Pixels"),
                (IPixels) ctx.getBean("internal-ome.api.IPixels"));

        ex = (Executor) ctx.getBean("executor");
        uuid = (String) ctx.getBean("uuid");
        geomTool = new GeomTool(data, sql, factory, ex, uuid);

    }

    public void testTicket2045() throws Exception {
        // Synchronization no longer performed!
    }

    public void testShapeConversion() throws Exception {
        List<Shape> shapes = geomTool.random(50000);
        for (Shape shape : shapes) {
            String path = geomTool.dbPath(shape);
            assertNotNull(path);
        }
    }

}
