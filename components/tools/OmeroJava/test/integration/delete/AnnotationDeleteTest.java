/*
 * $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration.delete;

import static omero.rtypes.rstring;
import static omero.rtypes.rlong;
import integration.AbstractTest;
import integration.DeleteServiceTest;
import omero.api.delete.DeleteCommand;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.LongAnnotation;
import omero.model.LongAnnotationI;
import omero.model.OriginalFile;
import omero.sys.EventContext;

import org.testng.annotations.Test;

/**
 * Tests for deleting user ratings.
 *
 * @see ticket:2997
 * @see ticket:2994
 * @since 4.2.1
 */
@Test(groups = { "delete", "integration", "ticket:2615" })
public class AnnotationDeleteTest extends AbstractTest {

    public final static omero.RString RATING = rstring(omero.constants.metadata.NSINSIGHTRATING.value);

    public final static omero.RString EXPERIMENT = rstring(omero.constants.metadata.NSEDITOREXPERIMENT.value);

    public final static omero.RString PROTOCOL = rstring(omero.constants.metadata.NSEDITORPROTOCOL.value);

    @Test(groups = { "ticket:2994" })
    public void testDeleteFileAnnotationExperiment() throws Exception {

        newUserAndGroup("rw----");
        FileAnnotation fa = new FileAnnotationI();
        fa.setNs( EXPERIMENT );
        fa.setFile( mmFactory.createOriginalFile() );
        fa = (FileAnnotation) iUpdate.saveAndReturnObject( fa );
        OriginalFile file = fa.getFile();

        delete(client, new DeleteCommand(DeleteServiceTest.REF_ANN, fa
                .getId().getValue(), null));

        assertDoesNotExist(fa);
        assertDoesNotExist(file);

    }

    @Test(groups = { "ticket:2994" })
    public void testDeleteFileAnnotationProtocol() throws Exception {

        newUserAndGroup("rw----");
        FileAnnotation fa = new FileAnnotationI();
        fa.setNs( PROTOCOL );
        fa.setFile( mmFactory.createOriginalFile() );
        fa = (FileAnnotation) iUpdate.saveAndReturnObject( fa );
        OriginalFile file = fa.getFile();

        delete(client, new DeleteCommand(DeleteServiceTest.REF_ANN, fa
                .getId().getValue(), null));

        assertDoesNotExist(fa);
        assertDoesNotExist(file);

    }

    @Test(enabled = false, groups = { "ticket:2997" })
    public void testOtherUsersRatingsIsDeleted() throws Exception {

        EventContext owner = newUserAndGroup("rwrw--");
        Image i1 = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
        disconnect();

        newUserInGroup(owner);
        LongAnnotation rating = new LongAnnotationI();
        rating.setNs( RATING );
        rating.setLongValue( rlong(1L) );
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.link((Image) i1.proxy(), rating);
        link = (ImageAnnotationLink) iUpdate.saveAndReturnObject(link);
        rating = (LongAnnotation) link.getChild();
        disconnect();

        loginUser(owner);
        delete(client, new DeleteCommand(DeleteServiceTest.REF_IMAGE, i1
                .getId().getValue(), null));

        assertDoesNotExist(i1);
        assertDoesNotExist(link);
        assertDoesNotExist(rating);
        disconnect();

    }

    @Test(enabled = false, groups = { "ticket:2997" })
    public void testOtherUsersRatingsIsNotDeletedIfReused() throws Exception {
        fail("NYI");
    }

}
