/*
 * Copyright 2006-2016 University of Dundee. All rights reserved.
 * Use is subject to license terms supplied in LICENSE.txt
 */

package integration.chgrp;

import integration.AbstractServerTest;
import integration.DeleteServiceTest;

import java.util.ArrayList;
import java.util.List;

import ome.services.blitz.repo.path.FsFile;
import omero.api.RawFileStorePrx;
import omero.cmd.Chgrp2;
import omero.cmd.graphs.ChildOption;
import omero.gateway.util.Requests;
import omero.grid.ManagedRepositoryPrx;
import omero.grid.ManagedRepositoryPrxHelper;
import omero.grid.RepositoryMap;
import omero.grid.RepositoryPrx;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Move annotated objects. Annotation.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 3.0-Beta4
 */
public class AnnotationMoveTest extends AbstractServerTest {

    /**
     * Helper method uses to test the move of an image with annotation
     * added by 2 users. The annotations are not shared.
     *
     * @param src The permissions of the source group.
     * @param dest The permissions of the target group.
     * @param secondUserMemberOfTarget Indicates if the second user is a member
     * of the target group.
     * @throws Exception Thrown if an error occurred.
     */
    private void moveImageWithNonSharedAnnotation(String src, String dest,
            boolean secondUserMemberOfTarget)
            throws Exception
    {
        EventContext ctx = newUserAndGroup(src);
        Image img =
                (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());

        List<Long> annotationIdsUser1 = createNonSharableAnnotation(img, null);

        disconnect();
        // Add a user to that group
        EventContext ctx2 = newUserInGroup(ctx);
        init(ctx2);
        List<Long> annotationIdsUser2 = createNonSharableAnnotation(img, null);
        disconnect();

        List<Long> users = new ArrayList<Long>();
        users.add(ctx.userId);
        if (secondUserMemberOfTarget) users.add(ctx2.userId);

        ExperimenterGroup g = newGroupAddUser(dest, users);

        // reconnect as user1
        init(ctx);
        // now move the image.
        final Chgrp2 dc = Requests.chgrp().target(img).toGroup(g).build();
        callback(true, client, dc);

        // Annotation of user1 should be removed
        ParametersI param = new ParametersI();
        param.addIds(annotationIdsUser1);
        StringBuilder sb = new StringBuilder();
        sb.append("select i from Annotation i ");
        sb.append("where i.id in (:ids)");
        Assert.assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

        int n = annotationIdsUser2.size();
        if (src.equals("rwrw--")) n = 0;
        param = new ParametersI();
        param.addIds(annotationIdsUser2);
        Assert.assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

        loginUser(g);
        param = new ParametersI();
        param.addIds(annotationIdsUser1);
        Assert.assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(),
                annotationIdsUser1.size());
        n = 0;
        if (src.equals("rwrw--") && !dest.equals("rw----")) {
            n = annotationIdsUser2.size();
        }
        param = new ParametersI();
        param.addIds(annotationIdsUser2);
        Assert.assertEquals(n, iQuery.findAllByQuery(sb.toString(), param)
                .size(), "#9496? anns");
    }

    /**
     * Test to move an image with annotation.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveImageWithNonSharableAnnotation() throws Exception {
        String perms = "rw----";
        EventContext ctx = newUserAndGroup(perms);
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
        iAdmin.getEventContext(); // Refresh

        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        // Annotate the image.
        List<Long> annotationIds = createNonSharableAnnotation(img, null);
        // now move the image.
        final Chgrp2 dc = Requests.chgrp().target(img).toGroup(g).build();
        callback(true, client, dc);

        ParametersI param = new ParametersI();
        param.addId(img.getId().getValue());
        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        Assert.assertNull(iQuery.findByQuery(sb.toString(), param));

        param = new ParametersI();
        param.addIds(annotationIds);
        sb = new StringBuilder();
        sb.append("select i from Annotation i ");
        sb.append("where i.id in (:ids)");
        Assert.assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

        loginUser(g);
        Assert.assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(),
                annotationIds.size());
    }

    /**
     * Test to move an image with annotation that can be shared but we do not
     * keep them.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveImageWithSharableAnnotation() throws Exception {
        String perms = "rw----";
        EventContext ctx = newUserAndGroup(perms);
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
        iAdmin.getEventContext(); // Refresh

        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        // Annotate the image.
        List<Long> annotationIds = createSharableAnnotation(img, null);
        // now move the image.
        final Chgrp2 dc = Requests.chgrp().target(img).toGroup(g).build();
        callback(true, client, dc);
        ParametersI param = new ParametersI();
        param.addId(img.getId().getValue());
        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        Assert.assertNull(iQuery.findByQuery(sb.toString(), param));

        param = new ParametersI();
        param.addIds(annotationIds);
        sb = new StringBuilder();
        sb.append("select i from Annotation i ");
        sb.append("where i.id in (:ids)");
        Assert.assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

        loginUser(g);
        Assert.assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(),
                annotationIds.size());
    }

    /**
     * Test to move an image with annotation that can be shared but we keep
     * them.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveImageWithSharableAnnotationToKeep() throws Exception {
        String perms = "rwrw--";
        EventContext ctx = newUserAndGroup(perms);
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
        iAdmin.getEventContext(); // Refresh

        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        // Annotate the image.
        List<Long> annotationIds = createSharableAnnotation(img, null);
        Assert.assertTrue(annotationIds.size() > 0);
        // now move the image.
        long id = img.getId().getValue();
        final ChildOption option = Requests.option().excludeType(DeleteServiceTest.SHARABLE_TO_KEEP_LIST).build();
        final Chgrp2 dc = Requests.chgrp().target(img).option(option).toGroup(g).build();
        callback(true, client, dc);
        ParametersI param = new ParametersI();
        param.addId(id);
        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        Assert.assertNull(iQuery.findByQuery(sb.toString(), param));

        param = new ParametersI();
        param.addIds(annotationIds);
        sb = new StringBuilder();
        sb.append("select i from Annotation i ");
        sb.append("where i.id in (:ids)");
        Assert.assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(),
                annotationIds.size());

        loginUser(g);
        Assert.assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);
    }

    /**
     * Test to move an image with annotation. Context:
     * - 2 users annotate the image with non sharable annotations.
     * - Owner move the image from a RWRW-- to a RWR--- group.
     * - The annotation of the second user should be moved to the
     * destination group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveImageAnnotatedByUsersInDestinationGroupRWRWtoRWR()
            throws Exception {
        moveImageWithNonSharedAnnotation("rwrw--", "rwr---", true);
    }

    /**
     * Test to move an image with annotation. Context:
     * - 2 users annotate the image with non sharable annotations.
     * - Owner move the image from a RWRA-- to a RWR--- group.
     * - The annotation of the second user should be moved to the
     * destination group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveImageAnnotatedByUsersInDestinationGroupRWRAtoRWR()
            throws Exception {
        moveImageWithNonSharedAnnotation("rwra--", "rwr---", true);
    }

    /**
     * Test to move an image with annotation. Context:
     * - 2 users annotate the image with non sharable annotations.
     * - Owner move the image from a RWRA-- to a RW---- group.
     * - The annotation of the second user should be moved to the
     * destination group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveImageAnnotatedByUsersInDestinationGroupRWRAtoRW()
            throws Exception {
        moveImageWithNonSharedAnnotation("rwra--", "rw----", true);
    }

    /**
     * Test to move an image with annotation. Context:
     * - 2 users annotate the image with non sharable annotations.
     * - Owner move the image from a RWRW-- to a RWRW-- group.
     * - The annotation of the second user should be moved to the
     * destination group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveImageAnnotatedByUsersInDestinationGroupRWRWtoRWRW()
            throws Exception {
        moveImageWithNonSharedAnnotation("rwrw--", "rwrw--", true);
    }

    /**
     * Test to move an image with annotation. Context:
     * - 2 users annotate the image with non sharable annotations.
     * - Owner move the image from a RWRW-- to a RWRA-- group.
     * - The annotation of the second user should be moved to the
     * destination group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveImageAnnotatedByUsersInDestinationGroupRWRWtoRWRA()
            throws Exception {
        moveImageWithNonSharedAnnotation("rwrw--", "rwra--", true);
    }

    /**
     * Test to move an image with annotation. Context:
     * - 2 users annotate the image with non sharable annotations.
     * - Owner move the image from a RWRW-- to a RW---- group.
     * - The annotation of the second user should be moved to the
     * destination group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public void testMoveImageAnnotatedByUsersInDestinationGroupRWRWtoRW()
            throws Exception {
        moveImageWithNonSharedAnnotation("rwrw--", "rw----", true);
    }

    /**
     * Test to move an image with annotation. Context: 
     * - 2 users annotate the image with non sharable annotations.
     * - Owner move the image from a RWRA-- to a RWRA-- group.
     * - The annotation of the second user should be moved to the
     * destination group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveImageAnnotatedByUsersInDestinationGroupRWRAtoRWRA()
            throws Exception {
        moveImageWithNonSharedAnnotation("rwra--", "rwra--", true);
    }

    /**
     * Test to move an image with annotation. Context:
     * - 2 users annotate the image with non sharable annotations.
     * - Owner move the image from a RWRA-- to a RWRW-- group.
     * - The annotation of the second user should be moved to the
     * destination group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveImageAnnotatedByUsersInDestinationGroupRWRAtoRWRW()
            throws Exception {
        moveImageWithNonSharedAnnotation("rwra--", "rwrw--", true);
    }

    /**
     * Test to move an image with annotation. Context:
     * - 2 users annotate the image with non sharable annotations.
     * - Owner move the image to from a RWRW-- to a RWRW-- group.
     * - The annotation of the second user should be deleted and not
     * moved to the destination group b/c the second user is not a member of the
     * destination group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveImageAnnotatedByUsersOneNotInDestinationGroupDestinationGroupRWRWtoRWRW()
            throws Exception {
        moveImageWithNonSharedAnnotation("rwrw--", "rwrw--", false);
    }

    /**
     * Test to move an image with annotation. Context:
     * - 2 users annotate the image with non sharable annotations.
     * - Owner move the image to from a RWRW-- to a RWRA-- group.
     * - The annotation of the second user should be deleted and not
     * moved to the destination group b/c the second user is not a member of the
     * destination group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveImageAnnotatedByUsersOneNotInDestinationGroupDestinationGroupRWRWtoRWRA()
            throws Exception {
        moveImageWithNonSharedAnnotation("rwrw--", "rwra--", false);
    }

    /**
     * Test to move an image with annotation. Context:
     * - 2 users annotate the image with non sharable annotations.
     * - Owner move the image to from a RWRA-- to a RWR--- group.
     * - The annotation of the second user should be deleted and not
     * moved to the destination group b/c the second user is not a member of the
     * destination group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveImageAnnotatedByUsersOneNotInDestinationGroupDestinationGroupRWRWtoRWR()
            throws Exception {
        moveImageWithNonSharedAnnotation("rwrw--", "rwr---", false);
    }

    /**
     * Test to move an image with annotation. Context:
     * - 2 users annotate the image with non sharable annotations.
     * - Owner move the image to from a RWRW-- to a RW---- group.
     * - The annotation of the second user should be deleted and not
     * moved to the destination group b/c the second user is not a member of the
     * destination group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public void testMoveImageAnnotatedByUsersOneNotInDestinationGroupDestinationGroupRWRWtoRW()
            throws Exception {
        moveImageWithNonSharedAnnotation("rwrw--", "rw----", false);
    }

    /**
     * Test to move an image with annotation. Context:
     * - 2 users annotate the image with non sharable annotations.
     * - Owner move the image to from a RWRA-- to a RW---- group.
     * - The annotation of the second user should be deleted and not
     * moved to the destination group b/c the second user is not a member of the
     * destination group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveImageAnnotatedByUsersOneNotInDestinationGroupDestinationGroupRWRAtoRW()
            throws Exception {
        moveImageWithNonSharedAnnotation("rwra--", "rw----", false);
    }

    /**
     * Test to move an image with annotation. Context:
     * - 2 users annotate the image with non sharable annotations.
     * - Owner move the image to from a RWRA-- to a RWR--- group.
     * - The annotation of the second user should be deleted and not
     * moved to the destination group b/c the second user is not a member of the
     * destination group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveImageAnnotatedByUsersOneNotInDestinationGroupDestinationGroupRWRAtoRWR()
            throws Exception {
        moveImageWithNonSharedAnnotation("rwra--", "rwr---", false);
    }

    /**
     * Test to move an image with annotation. Context:
     * - 2 users annotate the image with non sharable annotations.
     * - Owner move the image to from a RWRA-- to a RWRA-- group.
     * - The annotation of the second user should be deleted and not
     * moved to the destination group b/c the second user is not a member of the
     * destination group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveImageAnnotatedByUsersOneNotInDestinationGroupDestinationGroupRWRAtoRWRA()
            throws Exception {
        moveImageWithNonSharedAnnotation("rwra--", "rwra--", false);
    }

    /**
     * Test to move an image with annotation. Context:
     * - 2 users annotate the image with non sharable annotations.
     * - Owner move the image to from a RWRA-- to a RWRW-- group.
     * - The annotation of the second user should be deleted and not
     * moved to the destination group b/c the second user is not a member of the
     * destination group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveImageAnnotatedByUsersOneNotInDestinationGroupDestinationGroupRWRAtoRWRW()
            throws Exception {
        moveImageWithNonSharedAnnotation("rwra--", "rwrw--", false);
    }

    /**
     * Test to move a tagged image, the tag is also used to tag another image.
     * The image will be moved and the tag should not be moved.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveTaggedImage() throws Exception {
        String perms = "rw----";
        EventContext ctx = newUserAndGroup(perms);
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
        iAdmin.getEventContext(); // Refresh

        Image img1 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());
        Image img2 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());
        //
        TagAnnotation c = new TagAnnotationI();
        c.setTextValue(omero.rtypes.rstring("tag"));
        c = (TagAnnotation) iUpdate.saveAndReturnObject(c);
        List<IObject> links = new ArrayList<IObject>();
        // Tag the 2 images
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.setChild(new TagAnnotationI(c.getId().getValue(), false));
        link.setParent(img1);
        links.add(link);

        link = new ImageAnnotationLinkI();
        link.setChild(new TagAnnotationI(c.getId().getValue(), false));
        link.setParent(img2);
        links.add(link);
        iUpdate.saveAndReturnArray(links);

        final ChildOption option = Requests.option().excludeType(DeleteServiceTest.SHARABLE_TO_KEEP_LIST).build();
        final Chgrp2 dc = Requests.chgrp().target(img1).option(option).toGroup(g).build();
        callback(true, client, dc);

        ParametersI param = new ParametersI();
        param.addId(c.getId().getValue());
        StringBuilder sb = new StringBuilder();
        sb.append("select i from Annotation i ");
        sb.append("where i.id = :id");
        Assert.assertNotNull(iQuery.findByQuery(sb.toString(), param));

        loginUser(g);
        param = new ParametersI();
        param.addId(img1.getId().getValue());
        sb = new StringBuilder();
        sb.append("select i from ImageAnnotationLink i ");
        sb.append("where i.parent.id = :id");
        List<IObject> results = iQuery.findAllByQuery(sb.toString(), param);

        Assert.assertEquals(0, results.size(), "#9496? anns");
    }

    /**
     * Check that moving acts appropriately with attachments that share files.
     * @throws Exception unexpected
     */
    @Test
    public void testMoveAttachmentsSharingFile() throws Exception {
        /* set up user and groups */
        final EventContext user = newUserAndGroup("rw----");
        final ExperimenterGroup fromGroup = new ExperimenterGroupI(user.groupId, false);
        final ExperimenterGroup toGroup = newGroupAddUser("rw----", user.userId);

        /* work in first group */
        loginUser(fromGroup);

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

        /* upload a file */
        final RawFileStorePrx rfs = repo.file(path + FsFile.separatorChar + System.nanoTime(), "rw");
        rfs.write(new byte[] {1, 2, 3, 4}, 0, 4);
        final OriginalFile file = new OriginalFileI(rfs.save().getId().getValue(), false);
        rfs.close();

        /* create two attachments that share the file */
        final List<FileAnnotation> attachments = new ArrayList<FileAnnotation>();
        for (int i = 1; i <= 2; i++) {
            final FileAnnotation attachment = new FileAnnotationI();
            attachment.setFile(file);
            attachments.add((FileAnnotation) iUpdate.saveAndReturnObject(attachment).proxy());
        }

        /* check that the file does not move without its attachment */
        Chgrp2 request;
        request = Requests.chgrp().target(file).toGroup(toGroup).build();
        doChange(client, factory, request, false);

        /* check that one attachment cannot move without the other */
        request = Requests.chgrp().target(attachments.get(0)).toGroup(toGroup).build();
        doChange(client, factory, request, false);

        /* check that the attachments can move together */
        request = Requests.chgrp().target(attachments.get(0)).target(attachments.get(1)).toGroup(toGroup).build();
        doChange(request);

        /* check what remains in the source group */
        Assert.assertNull(iQuery.findByQuery("FROM OriginalFile WHERE id = :id", new ParametersI().addId(file.getId())));
        for (final FileAnnotation attachment : attachments) {
            Assert.assertNull(iQuery.findByQuery("FROM FileAnnotation WHERE id = :id", new ParametersI().addId(attachment.getId())));
        }

        /* switch to the destination group */
        loginUser(toGroup);

        /* check what was moved to the destination group */
        Assert.assertNotNull(iQuery.findByQuery("FROM OriginalFile WHERE id = :id", new ParametersI().addId(file.getId())));
        for (final FileAnnotation attachment : attachments) {
            Assert.assertNotNull(iQuery.findByQuery("FROM FileAnnotation WHERE id = :id", new ParametersI().addId(attachment.getId())));
        }

        /* delete the test data */
        doChange(Requests.delete().target(attachments.get(0)).target(attachments.get(1)).build());
    }
}
