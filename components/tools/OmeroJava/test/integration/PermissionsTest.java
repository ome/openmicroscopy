/*
 *   Copyright 2012 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;


//Java imports
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

//Third-party libraries
import org.testng.annotations.Test;

//Application-internal dependencies
import omero.RLong;
import omero.api.IPixelsPrx;
import omero.api.IRenderingSettingsPrx;
import omero.model.AcquisitionMode;
import omero.model.ArcType;
import omero.model.Binning;
import omero.model.Channel;
import omero.model.ContrastMethod;
import omero.model.Correction;
import omero.model.DetectorType;
import omero.model.DimensionOrder;
import omero.model.ExperimentType;
import omero.model.Family;
import omero.model.FilamentType;
import omero.model.FilterType;
import omero.model.Format;
import omero.model.IObject;
import omero.model.Illumination;
import omero.model.Image;
import omero.model.Immersion;
import omero.model.LaserMedium;
import omero.model.LaserType;
import omero.model.LogicalChannel;
import omero.model.Medium;
import omero.model.MicrobeamManipulationType;
import omero.model.MicroscopeType;
import omero.model.PhotometricInterpretation;
import omero.model.Pixels;
import omero.model.PixelsType;
import omero.model.Pulse;
import omero.model.RenderingDef;
import omero.model.RenderingModel;
import omero.sys.ParametersI;

/**
 * Tests for the updated group permissions of
 * 4.3 and 4.4.
 *
 * @since 4.4.0
 */
@Test(groups = { "client", "integration", "permissions" })
public class PermissionsServiceTest
	extends AbstractServerTest {
}
    // chmod
    // ==============================================

    /*
     * See #8277 permissions returned from the server
     * should now be immutable.
     */
    public void testImmutablePermissions() {

        // Test on the raw object
        PermissionI p = new omero.model.PermissionsI();
        p.ice_postUnmarshal();
        try {
            p.setPerm1(1);
            fail("throw!");
        } catch (omero.ClientError err) {
            // good
        }

        // and on one returned from the server
        CommentAnnotationI c = new omero.model.CommentAnnotationI();
        c = self.update.saveAndReturnObject(c)
        p = c.details.permissions
        try {
                p.setPerm1(1);
        } catch (omero.ClientError err) {
            // good
        }
    }

    public void testDisallow() {
        PermissionsI p = new omero.model.PermissionsI();
        assertTrue(p.canAnnotate());
        assertTrue(p.canEdit());
    }

    public void testClientSet() {
        CommentAnnotationI c = new omero.model.CommentAnnotationI();
        c = self.update.saveAndReturnObject(c)
        DetailsI d = c.getDetails();
        assertTrue( d.getClient() != null);
        //self.assertTrue( d.getSession() is not None)
        //self.assertTrue( d.getCallContext() is not None)
        //self.assertTrue( d.getEventContext() is not None)
    }
