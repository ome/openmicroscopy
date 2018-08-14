/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package integration.delete;

import integration.AbstractServerTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ome.services.blitz.repo.path.FsFile;
import omero.RLong;
import omero.RString;
import omero.api.RawFileStorePrx;
import omero.cmd.CmdCallbackI;
import omero.cmd.Delete2;
import omero.cmd.HandlePrx;
import omero.gateway.util.Requests;
import omero.grid.ManagedRepositoryPrx;
import omero.grid.ManagedRepositoryPrxHelper;
import omero.grid.RepositoryMap;
import omero.grid.RepositoryPrx;
import omero.model.Annotation;
import omero.model.AnnotationAnnotationLink;
import omero.model.AnnotationAnnotationLinkI;
import omero.model.Channel;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.LongAnnotation;
import omero.model.LongAnnotationI;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.PlaneInfo;
import omero.model.Roi;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.sys.EventContext;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.primitives.Ints;

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
    public static final RString RATING = omero.rtypes.rstring(omero.constants.metadata.NSINSIGHTRATING.value);

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
        final Delete2 dc = Requests.delete().target(command).id(id).build();
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
            final Delete2 dc = Requests.delete().target(fa).build();
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
        rating.setLongValue(omero.rtypes.rlong(1L));
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.link((Image) i1.proxy(), rating);
        link = (ImageAnnotationLink) iUpdate.saveAndReturnObject(link);
        rating = (LongAnnotation) link.getChild();
        disconnect();

        loginUser(owner);
        final Delete2 dc = Requests.delete().target(i1).build();
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
        /* ensure a connection to the server */
        newUserAndGroup("rwra--");

        /* create two tag sets */
        final List<TagAnnotation> tagsets = new ArrayList<TagAnnotation>();
        for (int i = 1; i <= 2; i++) {
            final TagAnnotation tagset = new TagAnnotationI();
            tagset.setName(omero.rtypes.rstring("tagset #" + i));
            tagset.setNs(omero.rtypes.rstring(omero.constants.metadata.NSINSIGHTTAGSET.value));
            tagsets.add((TagAnnotation) iUpdate.saveAndReturnObject(tagset).proxy());
        }

        /* create three tags */
        final List<TagAnnotation> tags = new ArrayList<TagAnnotation>();
        for (int i = 1; i <= 3; i++) {
            final TagAnnotation tag = new TagAnnotationI();
            tag.setName(omero.rtypes.rstring("tag #" + i));
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
        final Delete2 request = Requests.delete().target(tagsets.get(0)).build();
        switch (option) {
        case NONE:
            break;
        case INCLUDE:
            request.childOptions = Collections.singletonList(Requests.option().includeType("Annotation").build());
            break;
        case EXCLUDE:
            request.childOptions = Collections.singletonList(Requests.option().excludeType("Annotation").build());
            break;
        case BOTH:
            request.childOptions = Collections.singletonList(Requests.option().includeType("Annotation")
                                                                              .excludeType("Annotation").build());
            break;
        default:
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
            doChange(Requests.delete().target(tags.get(0)).build());
            break;
        }

        /* delete the second tag set */
        doChange(Requests.delete().target(tagsets.get(1)).build());

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

    /**
     * Check that deletion acts appropriately with attachments that share files.
     * @throws Exception unexpected
     */
    @Test
    public void testDeleteAttachmentSharingFile() throws Exception {
        /* ensure a connection to the server */
        newUserAndGroup("rwra--");

        /* obtain the managed repository */
        ManagedRepositoryPrx repo = null;
        RepositoryMap rm = factory.sharedResources().repositories();
        for (int i = 0; i < rm.proxies.size(); i++) {
            final RepositoryPrx prx = rm.proxies.get(i);
            final ManagedRepositoryPrx tmp = ManagedRepositoryPrxHelper.checkedCast(prx);
            if (tmp != null) {
                repo = tmp;
            }
        }
        if (repo == null) {
            throw new Exception("Unable to find managed repository");
        }

        /* create a destination directory for upload */
        final EventContext ctx = iAdmin.getEventContext();
        final StringBuffer pathBuilder = new StringBuffer();
        pathBuilder.append(ctx.userName);
        pathBuilder.append('_');
        pathBuilder.append(ctx.userId);
        pathBuilder.append(FsFile.separatorChar);
        pathBuilder.append("test-");
        pathBuilder.append(getClass());
        pathBuilder.append(FsFile.separatorChar);
        pathBuilder.append(System.currentTimeMillis());
        final String path = pathBuilder.toString();
        repo.makeDir(path, true);

        /* upload files */
        final List<OriginalFile> files = new ArrayList<OriginalFile>();
        for (int i = 1; i <= 2; i++) {
            final RawFileStorePrx rfs = repo.file(path + FsFile.separatorChar + System.nanoTime(), "rw");
            rfs.write(new byte[] {1, 2, 3, 4}, 0, 4);
            files.add(new OriginalFileI(rfs.save().getId().getValue(), false));
            rfs.close();
        }

        /* create three attachments that use files 0, 1, 1 */
        final List<FileAnnotation> attachments = new ArrayList<FileAnnotation>();
        for (int i = 1; i <= 3; i++) {
            final FileAnnotation attachment = new FileAnnotationI();
            attachment.setFile(files.get(i/2));
            attachments.add((FileAnnotation) iUpdate.saveAndReturnObject(attachment).proxy());
        }

        /* delete the first attachment via its file */
        Delete2 request;
        request = Requests.delete().target(files.get(0)).build();
        doChange(request);
        assertDoesNotExist(files.get(0));
        assertExists(files.get(1));
        assertDoesNotExist(attachments.get(0));
        assertExists(attachments.get(1));
        assertExists(attachments.get(2));

        /* delete the second attachment: other file should remain */
        request = Requests.delete().target(attachments.get(1)).build();
        doChange(request);
        assertExists(files.get(1));
        assertDoesNotExist(attachments.get(1));
        assertExists(attachments.get(2));

        /* delete the final attachment: other file should be deleted */
        request = Requests.delete().target(attachments.get(2)).build();
        doChange(request);
        assertDoesNotExist(files.get(1));
        assertDoesNotExist(attachments.get(2));
    }

    /**
     * Test the effect of {@link Delete2#typesToIgnore}.
     * @throws Exception unexpected
     */
    @Test
    public void testIgnoreTypesEffect() throws Exception {
        newUserAndGroup("rwr---");

        /* create a commented image */
        CommentAnnotation comment = new CommentAnnotationI();
        comment.setTextValue(omero.rtypes.rstring("test for " + getClass().getSimpleName()));
        ImageAnnotationLink link = (ImageAnnotationLink) iUpdate.saveAndReturnObject(
                mmFactory.createAnnotationLink(mmFactory.simpleImage(), comment));
        Image image = link.getParent();
        comment = (CommentAnnotation) link.getChild();

        assertAllExist(link, image, comment);

        /* ignoring tags still has the comment deleted along with the image */
        Delete2 request = Requests.delete().target(image).build();
        request.typesToIgnore = Collections.singletonList("TagAnnotation");
        doChange(request);

        assertNoneExist(link, image, comment);

        /* create a commented image */
        comment = new CommentAnnotationI();
        comment.setTextValue(omero.rtypes.rstring("test for " + getClass().getSimpleName()));
        link = (ImageAnnotationLink) iUpdate.saveAndReturnObject(
                mmFactory.createAnnotationLink(mmFactory.simpleImage(), comment));
        image = link.getParent();
        comment = (CommentAnnotation) link.getChild();

        assertAllExist(link, image, comment);

        /* ignoring links prevents image deletion because a link cannot have its parent removed */
        request = Requests.delete().target(image).build();
        request.typesToIgnore = Collections.singletonList("IAnnotationLink");
        doChange(client, factory, request, false);

        assertAllExist(link, image, comment);

        /* ignoring all annotations allows image deletion although the comment remains */
        request.typesToIgnore = Collections.singletonList("Annotation");
        doChange(request);

        assertDoesNotExist(link);
        assertDoesNotExist(image);
        assertExists(comment);

        doChange(Requests.delete().target(comment).build());
    }

    /**
     * Test that {@link Delete2#typesToIgnore} runs faster than {@link omero.cmd.graphs.ChildOption#excludeType} on large data.
     * @throws Exception unexpected
     */
    @Test(timeOut = 200000)
    public void testIgnoreTypesPerformance() throws Exception {
        newUserAndGroup("rwr---");

        /* add the same rating to many images */
        LongAnnotation rating = new LongAnnotationI();
        rating.setLongValue(omero.rtypes.rlong(3));
        rating = (LongAnnotation) iUpdate.saveAndReturnObject(rating).proxy();
        final List<IObject> toSave = new ArrayList<>();
        while (toSave.size() < 2500) {
            final ImageAnnotationLink link = new ImageAnnotationLinkI();
            link.setParent(mmFactory.simpleImage());
            link.setChild(rating);
            toSave.add(link);
        }
        final List<ImageAnnotationLink> links = new ArrayList<>(toSave.size());
        final List<Image> images = new ArrayList<>(toSave.size());
        for (final IObject saved : iUpdate.saveAndReturnArray(toSave)) {
            final ImageAnnotationLink link = (ImageAnnotationLink) saved;
            links.add((ImageAnnotationLink) link.proxy());
            images.add((Image) link.getParent().proxy());
        }
        final Iterator<ImageAnnotationLink> linkIterator = links.iterator();

        /* repeatedly try deleting a single image annotation link */
        final ListMultimap<Boolean, Long> durations = LinkedListMultimap.create();
        int count = 0;
        do {
            for (final boolean isIgnoreTypes : new boolean[] {false, true}) {
                /* can delete with excludeType or typesToIgnore for the rating */
                final Delete2 request = Requests.delete().target(linkIterator.next()).build();
                if (isIgnoreTypes) {
                    request.typesToIgnore = Collections.singletonList("Annotation");
                } else {
                    request.childOptions = Collections.singletonList(Requests.option().excludeType("Annotation").build());
                }
                final long timeStart = System.currentTimeMillis();
                final HandlePrx handle = factory.submit(request, null);
                final CmdCallbackI callback = new CmdCallbackI(client, handle);
                callback.loop(/* with Java 8 can use Math.toIntExact */
                        Ints.checkedCast(scalingFactor), 20);
                final long timeEnd = System.currentTimeMillis();
                assertCmd(callback, true);
                durations.put(isIgnoreTypes, timeEnd - timeStart);
            }
        } while (++count < 5);

        /* clean up test data */
        doChange(client, factory, Requests.delete().target(images.toArray(new IObject[images.size()])).build(), true, null, 5);

        /* check that median performance is better when using typesToIgnore */
        final List<Long> timesWithIgnore = new ArrayList<>(durations.get(true));
        final List<Long> timesWithoutIgnore = new ArrayList<>(durations.get(false));
        Assert.assertEquals(timesWithIgnore.size(), count);
        Assert.assertEquals(timesWithoutIgnore.size(), count);
        Collections.sort(timesWithIgnore);
        Collections.sort(timesWithoutIgnore);
        Assert.assertTrue(timesWithIgnore.get(count / 2) < timesWithoutIgnore.get(count / 2));
    }
}
