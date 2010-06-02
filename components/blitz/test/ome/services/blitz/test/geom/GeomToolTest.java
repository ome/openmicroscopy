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
import ome.model.IObject;
import ome.services.roi.GeomTool;
import ome.services.roi.PixelData;
import ome.services.util.Executor;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.tools.hibernate.SessionFactory;
import omero.model.Ellipse;
import omero.model.ImageI;
import omero.model.Line;
import omero.model.Point;
import omero.model.Rect;
import omero.model.RoiI;
import omero.model.Shape;
import omero.util.IceMapper;

import org.hibernate.Session;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@Test(groups = "integration")
public class GeomToolTest extends TestCase {

    protected OmeroContext ctx;

    protected PixelData data;

    protected GeomTool geomTool;

    protected SessionFactory factory;

    protected SimpleJdbcOperations jdbc;

    protected Executor ex;

    protected String uuid;

    @BeforeTest
    public void setup() {
        ctx = OmeroContext.getManagedServerContext();
        jdbc = (SimpleJdbcOperations) ctx.getBean("simpleJdbcTemplate");
        factory = (SessionFactory) ctx.getBean("omeroSessionFactory");
        data = new PixelData((PixelsService) ctx.getBean("/OMERO/Pixels"),
                (IPixels) ctx.getBean("internal-ome.api.IPixels"));

        ex = (Executor) ctx.getBean("executor");
        uuid = (String) ctx.getBean("uuid");
        geomTool = new GeomTool(data, jdbc, factory, ex, uuid);

    }

    public void testTicket2045() throws Exception {
        Rect r = geomTool.rect(237, 65, 166, 170);
        r.setTheT(rint(0));
        r.setTheZ(rint(0));
        r.setTextValue(rstring("Text"));
        r.setStrokeWidth(rint(1));

        ImageI img = new ImageI();
        RoiI roi = new RoiI();
        roi.addShape(r);
        img.addRoi(roi);
        img.setName(rstring("for roi"));
        img.setAcquisitionDate(rtime(0));


        IceMapper mapper = new IceMapper();
        final IObject o = (IObject) mapper.reverse(img);

        ex.execute(new Principal(uuid, "system", "Internal"),
                new Executor.SimpleWork(this, "save", o) {
                    @Transactional(readOnly = false)
                    public Object doWork(Session session, ServiceFactory sf) {

                        sf.getUpdateService().saveAndReturnObject(o);

                        // now there should be at least one shape to be parsed
                        List<Long> before = geomTool.getNullShapes();
                        assertTrue(before.size() > 0);
                        geomTool.synchronizeShapeGeometries(before);
                        List<Long> after = geomTool.getNullShapes();
                        assertEquals(0, after.size());

                        return null;
                    }

                });

    }

    public void testShapeConversion() throws Exception {
        List<Shape> shapes = geomTool.random(50000);
        for (Shape shape : shapes) {
            String path = geomTool.dbPath(shape);
            assertNotNull(path);
        }
    }

    public void testIntersectionWithRectangeAfterConversionToPath()
            throws Exception {
        Rect target = geomTool.rect(1.0, 1.0, 1.0, 1.0);
        String target_p = geomTool.dbPath(target);

        Rect r2 = geomTool.rect(0.0, 0.0, 2.0, 2.0);
        String r2_p = geomTool.dbPath(r2);
        assertIntersection(target_p, r2_p);

        Point p2 = geomTool.pt(1.5, 1.5);
        String p2_p = geomTool.dbPath(p2);
        assertIntersection(target_p, p2_p);

        Line l2 = geomTool.ln(0.0, 0.0, 2.0, 2.0);
        String l2_p = geomTool.dbPath(l2);
        assertIntersection(target_p, l2_p);

        Ellipse e2 = geomTool.ellipse(1.0, 1.0, 0.5, 0.5);
        String e2_p = geomTool.dbPath(e2);
        assertIntersection(target_p, e2_p);

    }

    //
    // assertions
    //

    private Boolean assertIntersection(String target_p, String r2_p) {
        Boolean b = (Boolean) jdbc.queryForObject(String.format(
                "select %s::polygon && %s::polygon", target_p, r2_p),
                Boolean.class);
        assertTrue(b);
        return b;
    }

}
