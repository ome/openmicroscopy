/*
 * $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration.delete;

import static omero.rtypes.rlong;
import static omero.rtypes.rstring;
import integration.AbstractTest;
import integration.DeleteServiceTest;

import java.util.HashMap;
import java.util.Map;

import omero.RLong;
import omero.api.delete.DeleteCommand;
import omero.model.Annotation;
import omero.model.Channel;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.LongAnnotation;
import omero.model.LongAnnotationI;
import omero.model.OriginalFile;
import omero.model.Pixels;
import omero.model.PlaneInfo;
import omero.model.Roi;
import omero.model.TagAnnotationI;
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

    @Test(groups = "ticket:2959")
    public void testForceCannotBeSetByUser() throws Exception {
        EventContext owner = newUserAndGroup("rwrw--");
        FileAnnotation fa = new FileAnnotationI();
        fa.setNs(EXPERIMENT);
        fa.setFile(mmFactory.createOriginalFile());
        fa = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        OriginalFile file = fa.getFile();
        disconnect();

        newUserInGroup(owner);
        Map<String, String> options = new HashMap<String, String>();
        options.put("/Annotation", "FORCE"); // Would delete other users' data
        delete(false, iDelete, client, new DeleteCommand(
                DeleteServiceTest.REF_ANN, fa.getId().getValue(), options));

        assertExists(fa);
        assertExists(file);

    }

    @Test(groups = "ticket:2959")
    public void testForceCanBeSetByAdmin() throws Exception {
        EventContext owner = newUserAndGroup("rwrw--");
        FileAnnotation fa = new FileAnnotationI();
        fa.setNs(EXPERIMENT);
        fa.setFile(mmFactory.createOriginalFile());
        fa = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        OriginalFile file = fa.getFile();
        disconnect();

        logRootIntoGroup(owner);
        Map<String, String> options = new HashMap<String, String>();
        options.put("/Annotation", "FORCE"); // Would delete other users' data
        delete(true, iDelete, client, new DeleteCommand(
                DeleteServiceTest.REF_ANN, fa.getId().getValue(), options));

        assertDoesNotExist(fa);
        assertDoesNotExist(file);

    }

    @Test(groups = { "ticket:2994" })
    public void testDeleteFileAnnotationExperiment() throws Exception {

        newUserAndGroup("rw----");
        FileAnnotation fa = new FileAnnotationI();
        fa.setNs(EXPERIMENT);
        fa.setFile(mmFactory.createOriginalFile());
        fa = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        OriginalFile file = fa.getFile();

        delete(client, new DeleteCommand(DeleteServiceTest.REF_ANN, fa.getId()
                .getValue(), null));

        assertDoesNotExist(fa);
        assertDoesNotExist(file);

    }

    @Test(groups = { "ticket:2994" })
    public void testDeleteFileAnnotationProtocol() throws Exception {

        newUserAndGroup("rw----");
        FileAnnotation fa = new FileAnnotationI();
        fa.setNs(PROTOCOL);
        fa.setFile(mmFactory.createOriginalFile());
        fa = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        OriginalFile file = fa.getFile();

        delete(client, new DeleteCommand(DeleteServiceTest.REF_ANN, fa.getId()
                .getValue(), null));

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
        rating.setNs(RATING);
        rating.setLongValue(rlong(1L));
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

    //
    // Tests for the less common annotated types
    //

    @Test(enabled = false, groups = { "ticket:3002", "ticket:3015" })
    public void testAnnotationsRemovedFromAnnotation() throws Exception {
        newUserAndGroup("rw----");
        Annotation ann = (Annotation) iUpdate
                .saveAndReturnObject(new TagAnnotationI());
        annotateSaveDeleteAndCheck(ann, DeleteServiceTest.REF_ANN, ann.getId(),
                false);
    }

    @Test(groups = { "ticket:3002" })
    public void testAnnotationsRemovedFromChannel() throws Exception {
        newUserAndGroup("rw----");
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());
        Channel ch = image.getPrimaryPixels().getChannel(0);
        annotateSaveDeleteAndCheck(ch, DeleteServiceTest.REF_IMAGE,
                image.getId());
    }

    @Test(groups = { "ticket:3002" })
    public void testAnnotationsRemovedFromOriginalFile() throws Exception {
        newUserAndGroup("rw----");
        OriginalFile file = (OriginalFile) iUpdate
                .saveAndReturnObject(mmFactory.createOriginalFile());
        annotateSaveDeleteAndCheck(file, DeleteServiceTest.REF_ORIGINAL_FILE,
                file.getId());
    }

    @Test(groups = { "ticket:3002" })
    public void testAnnotationsRemovedFromPixels() throws Exception {
        newUserAndGroup("rw----");
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());
        Pixels pixels = image.getPrimaryPixels();
        annotateSaveDeleteAndCheck(pixels, DeleteServiceTest.REF_IMAGE,
                image.getId());
    }

    @Test(groups = { "ticket:3002" })
    public void testAnnotationsRemovedFromPlaneInfo() throws Exception {
        newUserAndGroup("rw----");
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());
        PlaneInfo info = image.getPixels(0).copyPlaneInfo().get(0);
        annotateSaveDeleteAndCheck(info, DeleteServiceTest.REF_IMAGE,
                image.getId());
    }

    @Test(groups = { "ticket:3002" })
    public void testAnnotationsRemovedFromRoi() throws Exception {
        newUserAndGroup("rw----");
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImageWithRoi());
        Roi roi = image.copyRois().get(0);
        annotateSaveDeleteAndCheck(roi, DeleteServiceTest.REF_ROI, roi.getId());
    }

    //
    // Helpers
    //

    /**
     * Tests that the object, an annotation, and the link are all deleted.
     */
    void annotateSaveDeleteAndCheck(IObject obj, String command, RLong id) throws Exception {
        annotateSaveDeleteAndCheck(obj, command, id, true);
    }

    /**
     * Tests that the object, the annotation link, and optionally the annotation
     * are all deleted.
     */
    void annotateSaveDeleteAndCheck(IObject obj, String command, RLong id,
            boolean annIsDeleted) throws Exception {
        Annotation ann = (Annotation) iUpdate
                .saveAndReturnObject(new TagAnnotationI());
        IObject link = mmFactory.createAnnotationLink(obj.proxy(), ann);
        link = iUpdate.saveAndReturnObject(link);
        delete(client, new DeleteCommand(command, id.getValue(), null));
        assertDoesNotExist(obj);
        assertDoesNotExist(link);
        if (annIsDeleted) {
            assertDoesNotExist(ann);
        } else {
            assertExists(ann);
        }

    }

}
