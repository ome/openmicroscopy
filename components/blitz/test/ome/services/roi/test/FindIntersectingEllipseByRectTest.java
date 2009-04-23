/*
 *   $Id$
 *   
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.roi.test;

import static omero.rtypes.rbool;
import static omero.rtypes.rint;
import omero.model.Ellipse;
import omero.model.Roi;

import org.testng.annotations.Test;

/**
 * Tests searching for ellipses with rectangles
 */
@Test(groups = "integration")
public class FindIntersectingEllipseByRectTest extends AbstractRoiITest {

    //
    // Booleans: Visible, Locked
    //  
    
    @Test
    public void testFindIntersecting_Ellipse_By_Rec_Visibility_True() throws Exception {

        Ellipse e = geomTool.ellipse(1, 1, .5, .5);
        e.setVisibility(rbool(true));
        Roi roi = createRoi("find e by r vis", e);

        //
        // Matches
        //

        test = geomTool.rect(0.0, 0.0, 1.0, 1.0);
        assertIntersection(roi, test, 1);

        test.setVisibility(rbool(true));
        assertIntersection(roi, test, 1);

        //
        // Doesn't match
        //

        test.setVisibility(rbool(false));
        assertIntersection(roi, test, 0);

        test = geomTool.rect(10.0, 10.0, 1.0, 1.0);
        assertIntersection(roi, test, 0);

    }
    
    @Test
    public void testFindIntersecting_Ellipse_By_Rec_Visibility_False() throws Exception {

        Ellipse e = geomTool.ellipse(1, 1, .5, .5);
        e.setVisibility(rbool(false));
        Roi roi = createRoi("find e by r vis", e);

        //
        // Matches
        //

        test = geomTool.rect(0.0, 0.0, 1.0, 1.0);
        assertIntersection(roi, test, 1);

        test.setVisibility(rbool(false));
        assertIntersection(roi, test, 1);

        //
        // Doesn't match
        //

        test.setVisibility(rbool(true));
        assertIntersection(roi, test, 0);

        test = geomTool.rect(10.0, 10.0, 1.0, 1.0);
        assertIntersection(roi, test, 0);

    }
    
    @Test
    public void testFindIntersecting_Ellipse_By_Rec_Locked_True() throws Exception {

        Ellipse e = geomTool.ellipse(1, 1, .5, .5);
        e.setLocked(rbool(true));
        Roi roi = createRoi("find e by r lock", e);

        //
        // Matches
        //

        test = geomTool.rect(0.0, 0.0, 1.0, 1.0);
        assertIntersection(roi, test, 1);

        test.setLocked(rbool(true));
        assertIntersection(roi, test, 1);

        //
        // Doesn't match
        //

        test.setLocked(rbool(false));
        assertIntersection(roi, test, 0);

        test = geomTool.rect(10.0, 10.0, 1.0, 1.0);
        assertIntersection(roi, test, 0);

    }
    
    @Test
    public void testFindIntersecting_Ellipse_By_Rec_Locked_False() throws Exception {

        Ellipse e = geomTool.ellipse(1, 1, .5, .5);
        e.setLocked(rbool(false));
        Roi roi = createRoi("find e by r lock", e);

        //
        // Matches
        //

        test = geomTool.rect(0.0, 0.0, 1.0, 1.0);
        assertIntersection(roi, test, 1);

        test.setLocked(rbool(false));
        assertIntersection(roi, test, 1);

        //
        // Doesn't match
        //

        test.setLocked(rbool(true));
        assertIntersection(roi, test, 0);

        test = geomTool.rect(10.0, 10.0, 1.0, 1.0);
        assertIntersection(roi, test, 0);

    }

    //
    // z & t
    //
    
    @Test
    public void testFindIntersecting_Ellipse_By_Rec_NoZ_NoT() throws Exception {

        Ellipse e = geomTool.ellipse(1, 1, .5, .5);
        Roi roi = createRoi("find e by r no z no t", e);

        //
        // Matches
        //

        test = geomTool.rect(0.0, 0.0, 1.0, 1.0);
        assertIntersection(roi, test, 1);

        //
        // Doesn't match
        //

        test = geomTool.rect(10.0, 10.0, 1.0, 1.0);
        assertIntersection(roi, test, 0);
    }

    @Test
    public void testFindIntersecting_Ellipse_By_Rec_Z_NoT() throws Exception {

        Ellipse e = geomTool.ellipse(1, 1, .5, .5);
        e.setTheZ(rint(1));
        Roi roi = createRoi("find e by r z no t", e);

        //
        // Matches
        //

        test = geomTool.rect(0.0, 0.0, 1.0, 1.0);
        assertIntersection(roi, test, 1);

        test.setTheZ(rint(1));
        assertIntersection(roi, test, 1);

        //
        // Doesn't match
        //

        test.setTheZ(rint(0));
        assertIntersection(roi, test, 0);

        test = geomTool.rect(10.0, 10.0, 1.0, 1.0);
        assertIntersection(roi, test, 0);

        test.setTheZ(rint(0));
        assertIntersection(roi, test, 0);

        test.setTheZ(rint(1));
        assertIntersection(roi, test, 0);
    }

    @Test
    public void testFindIntersecting_Ellipse_By_Rec_NoZ_T() throws Exception {

        Ellipse e = geomTool.ellipse(1, 1, .5, .5);
        e.setTheT(rint(1));
        Roi roi = createRoi("find e by r no z t", e);

        // Matches
        test = geomTool.rect(0.0, 0.0, 1.0, 1.0);
        assertIntersection(roi, test, 1);

        test.setTheT(rint(1));
        assertIntersection(roi, test, 1);

        //
        // Doesn't match
        //

        test.setTheT(rint(0));
        assertIntersection(roi, test, 0);

        test = geomTool.rect(10.0, 10.0, 1.0, 1.0);
        assertIntersection(roi, test, 0);

        test.setTheT(rint(0));
        assertIntersection(roi, test, 0);

        test.setTheT(rint(1));
        assertIntersection(roi, test, 0);
    }

    @Test
    public void testFindIntersecting_Ellipse_By_Rec_Z_T() throws Exception {

        Ellipse e = geomTool.ellipse(1, 1, .5, .5);
        e.setTheT(rint(1));
        e.setTheZ(rint(2));
        Roi roi = createRoi("find e by r z t", e);

        //
        // Matches
        //

        test = geomTool.rect(0.0, 0.0, 1.0, 1.0);
        assertIntersection(roi, test, 1);

        test.setTheT(rint(1));
        test.setTheZ(rint(2));
        assertIntersection(roi, test, 1);

        test.setTheT(rint(1));
        test.setTheT(null);
        assertIntersection(roi, test, 1);

        test.setTheT(rint(1));
        test.setTheZ(null);
        assertIntersection(roi, test, 1);

        //
        // Doesn't match
        //

        test.setTheT(rint(0));
        test.setTheZ(rint(2)); // Correct
        assertIntersection(roi, test, 0);

        test.setTheT(rint(1)); // Correct
        test.setTheZ(rint(0));
        assertIntersection(roi, test, 0);

        test.setTheT(rint(0));
        test.setTheZ(rint(0));
        assertIntersection(roi, test, 0);

        test = geomTool.rect(10.0, 10.0, 1.0, 1.0);
        assertIntersection(roi, test, 0);

        test.setTheT(rint(1)); // Correct
        assertIntersection(roi, test, 0);

        test.setTheZ(rint(2)); // Correct
        assertIntersection(roi, test, 0);

        test.setTheT(rint(1)); // Correct
        test.setTheZ(rint(2)); // Correct
        assertIntersection(roi, test, 0);

    }

}
