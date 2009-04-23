/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test.geom;

import junit.framework.TestCase;
import ome.services.roi.GeomTool;
import ome.system.OmeroContext;
import omero.model.Ellipse;
import omero.model.Line;
import omero.model.Point;
import omero.model.Rect;

import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@Test(groups = "integration")
public class GeomToolTest extends TestCase {

    protected OmeroContext ctx;
    
    protected GeomTool geomTool;

    protected SimpleJdbcOperations jdbc;
    
    @BeforeTest
    public void setup() {
        ctx = OmeroContext.getManagedServerContext();
        geomTool = (GeomTool) ctx.getBean("geomTool");
        jdbc = (SimpleJdbcOperations) ctx.getBean("simpleJdbcTemplate");
    }

    public void testShapeConversion() throws Exception {
        Rect r = geomTool.rect(0.0, 0.0, 1.0, 1.0);
        String path = geomTool.dbPath(r);
    }

    public void testIntersectionWithRectangeAfterConversionToPath() throws Exception {
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
    
    public void testFindByIntersection() throws Exception {
        fail("NYI - need synchronization and the column");
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
