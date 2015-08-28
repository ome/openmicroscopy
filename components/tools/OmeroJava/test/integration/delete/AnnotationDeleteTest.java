/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package integration.delete;

import static omero.rtypes.rlong;
import static omero.rtypes.rstring;
import integration.AbstractServerTest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import omero.RLong;
import omero.RString;
import omero.cmd.Delete2;
import omero.gateway.util.Requests;
import omero.model.Annotation;
import omero.model.AnnotationAnnotationLink;
import omero.model.AnnotationAnnotationLinkI;
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
import omero.model.PlaneInfo;
import omero.model.Roi;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.sys.EventContext;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Tests for deleting user ratings.
 *
 * @see ticket:2997
 * @see ticket:2994
 * @since 4.2.1
 */
@Test(groups = "ticket:2615")
public class AnnotationDeleteTest extends AbstractServerTest {

    /** Reference to the <code>Rating</code> name space. */
    public final static RString RATING = rstring(omero.constants.metadata.NSINSIGHTRATING.value);

    /**
     * Tests that the object, an annotation, and the link are all deleted.
     *
     * @param obj
     *            The Object to annotate.
     * @param command
     *            The command indicating the object to delete.
     * @param id
     *            The identifier of the object to delete.
     */
    private void annotateSaveDeleteAndCheck(IObject obj, String command,
            RLong id) throws Exception {
        annotateSaveDeleteAndCheck(obj, command, id, true);
    }

    /**
     * Tests that the object, the annotation link, and optionally the annotation
     * are all deleted.
     *
     * @param obj
     *            The Object to annotate.
     * @param command
     *            The command indicating the object to delete.
     * @param id
     *            The identifier of the object to delete.
     * @param annIsDeleted
     *            Pass <code>true</code> if the annotation is deleted,
     *            <code>false</code> otherwise.
     */
    private void annotateSaveDeleteAndCheck(IObject obj, String command,
            RLong id, boolean annIsDeleted) throws Exception {
        Annotation ann = (Annotation) iUpdate
                .saveAndReturnObject(new TagAnnotationI());
        IObject link = mmFactory.createAnnotationLink(obj.proxy(), ann);
        link = iUpdate.saveAndReturnObject(link);
        final Delete2 dc = Requests.delete(command, id.getValue());
        callback(true, client, dc);
        assertDoesNotExist(obj);
        assertDoesNotExist(link);
        if (annIsDeleted) {
            assertDoesNotExist(ann);
        } else {
            assertExists(ann);
        }
    }

    /**
     * Test to delete the file annotation of a given namespace.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = { "ticket:2994" })
    public void testDeleteFileAnnotationOfGivenNamespace() throws Exception {

        newUserAndGroup("rw----");
        List<RString> ns = new ArrayList<RString>();
        ns.add(omero.rtypes.rstring("Test"));
        FileAnnotation fa;
        OriginalFile file;
        Iterator<RString> i = ns.iterator();
        while (i.hasNext()) {
            fa = new FileAnnotationI();
            fa.setNs(i.next());
            fa.setFile(mmFactory.createOriginalFile());
            fa = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
            file = fa.getFile();
            final Delete2 dc = Requests.delete("Annotation", fa.getId().getValue());
            callback(true, client, dc);
            assertDoesNotExist(fa);
            assertDoesNotExist(file);
        }
    }

    /**
     * Test to make sure that the ratings linked to an image are deleted when
     * the image is deleted even if the ratings where made by others.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:2997")
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
        final Delete2 dc = Requests.delete("Image", i1.getId().getValue());
        callback(true, client, dc);
        assertDoesNotExist(i1);
        assertDoesNotExist(link);
        assertDoesNotExist(rating);
        disconnect();
    }

    //
    // Tests for the less common annotated types
    //

    /**
     * Test to make sure that the annotations linked to an annotation are
     * deleted when the annotation is deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = { "ticket:3002", "ticket:3015" })
    public void testAnnotationsRemovedFromAnnotation() throws Exception {
        newUserAndGroup("rw----");
        Annotation ann = (Annotation) iUpdate
                .saveAndReturnObject(new TagAnnotationI());
        annotateSaveDeleteAndCheck(ann, Annotation.class.getSimpleName(),
                ann.getId());
    }

    /**
     * Test to make sure that the annotations linked to a channel are deleted
     * when the channel is deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = { "ticket:3002" })
    public void testAnnotationsRemovedFromChannel() throws Exception {
        newUserAndGroup("rw----");
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());
        Channel ch = image.getPrimaryPixels().getChannel(0);
        annotateSaveDeleteAndCheck(ch, Image.class.getSimpleName(),
                image.getId());
    }

    /**
     * Test to make sure that the annotations linked to an original file are
     * deleted when the original file is deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = { "ticket:3002" })
    public void testAnnotationsRemovedFromOriginalFile() throws Exception {
        newUserAndGroup("rw----");
        OriginalFile file = (OriginalFile) iUpdate
                .saveAndReturnObject(mmFactory.createOriginalFile());
        annotateSaveDeleteAndCheck(file, OriginalFile.class.getSimpleName(),
                file.getId());
    }

    /**
     * Test to make sure that the annotations linked to a plane info are deleted
     * when the plane info is deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = { "ticket:3002" })
    public void testAnnotationsRemovedFromPlaneInfo() throws Exception {
        newUserAndGroup("rw----");
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());
        PlaneInfo info = image.getPixels(0).copyPlaneInfo().get(0);
        annotateSaveDeleteAndCheck(info, Image.class.getSimpleName(),
                image.getId());
    }

    /**
     * Test to make sure that the annotations linked to an ROI are deleted when
     * the ROI is deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = { "ticket:3002" })
    public void testAnnotationsRemovedFromRoi() throws Exception {
        newUserAndGroup("rw----");
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImageWithRoi());
        Roi roi = image.copyRois().get(0);
        annotateSaveDeleteAndCheck(roi, Roi.class.getSimpleName(), roi.getId());
    }

    /* child options to try using in deletion */
    private enum Option { NONE, INCLUDE, EXCLUDE, BOTH };

    /**
     * Test deletion of tag sets with variously linked tags.
     * @param option the child option to use in the deletion
     * @throws Exception unexpected
     */
    @Test(dataProvider = "child option")
    public void testDeleteTargetSharedTag(Option option) throws Exception {
        /* create two tag sets */
        final List<TagAnnotation> tagsets = new ArrayList<TagAnnotation>();
        for (int i = 1; i <= 2; i++) {
            final TagAnnotation tagset = new TagAnnotationI();
            tagset.setName(rstring("tagset #" + i));
            tagset.setNs(rstring(omero.constants.metadata.NSINSIGHTTAGSET.value));
            tagsets.add((TagAnnotation) iUpdate.saveAndReturnObject(tagset).proxy());
        }

        /* create three tags */
        final List<TagAnnotation> tags = new ArrayList<TagAnnotation>();
        for (int i = 1; i <= 3; i++) {
            final TagAnnotation tag = new TagAnnotationI();
            tag.setName(rstring("tag #" + i));
            tags.add((TagAnnotation) iUpdate.saveAndReturnObject(tag).proxy());
        }

        /* define how to link the tag sets to the tags */
        final SetMultimap<TagAnnotation, TagAnnotation> members = HashMultimap.create();
        members.put(tagsets.get(0), tags.get(0));
        members.put(tagsets.get(0), tags.get(1));
        members.put(tagsets.get(1), tags.get(1));
        members.put(tagsets.get(1), tags.get(2));

        /* perform the linking */
        for (final Map.Entry<TagAnnotation, TagAnnotation> toLink : members.entries()) {
            final AnnotationAnnotationLink link = new AnnotationAnnotationLinkI();
            link.setParent(toLink.getKey());
            link.setChild(toLink.getValue());
            iUpdate.saveObject(link);
        }

        /* delete the first tag set */
        final Delete2 request;
        final Long tagset0Id = tagsets.get(0).getId().getValue();
        switch (option) {
        case NONE:
            request = Requests.delete("Annotation", tagset0Id);
            break;
        case INCLUDE:
            request = Requests.delete("Annotation", tagset0Id, Requests.option("Annotation", null));
            break;
        case EXCLUDE:
            request = Requests.delete("Annotation", tagset0Id, Requests.option(null, "Annotation"));
            break;
        case BOTH:
            request = Requests.delete("Annotation", tagset0Id, Requests.option("Annotation", "Annotation"));
            break;
        default:
            request = null;
            Assert.fail("unexpected option for delete");
        }
        doChange(request);

        /* check that the tag set is deleted and the other remains */
        assertDoesNotExist(tagsets.get(0));
        assertExists(tagsets.get(1));

        /* check that only the expected tags are deleted */
        switch (option) {
        case NONE:
            assertDoesNotExist(tags.get(0));
            assertExists(tags.get(1));
            assertExists(tags.get(2));
            break;
        case BOTH:
            /* include overrides exclude */
        case INCLUDE:
            assertDoesNotExist(tags.get(0));
            assertDoesNotExist(tags.get(1));
            assertExists(tags.get(2));
            break;
        case EXCLUDE:
            assertExists(tags.get(0));
            assertExists(tags.get(1));
            assertExists(tags.get(2));
            /* delete the tag that is not in the second tag set */
            doChange(Requests.delete("Annotation", tags.get(0).getId().getValue()));
            break;
        }

        /* delete the second tag set */
        doChange(Requests.delete("Annotation", tagsets.get(1).getId().getValue()));

        /* check that the tag set and the remaining tags are deleted */
        assertNoneExist(tagsets);
        assertNoneExist(tags);
    }

    /**
     * @return the child options to try using in deletion
     */
    @DataProvider(name = "child option")
    public Object[][] provideChildOption() {
        final Option[] values = Option.values();
        final Object[][] testCases = new Object[values.length][1];
        int index = 0;
        for (final Option value : values) {
            testCases[index++][0] = value;
        }
        return testCases;
    }
}
