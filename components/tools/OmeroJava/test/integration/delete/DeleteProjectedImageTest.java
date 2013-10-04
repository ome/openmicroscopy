/*
 * $Id$
 *
 * Copyright 2013 University of Dundee. All rights reserved.
 * Use is subject to license terms supplied in LICENSE.txt
 */
package integration.delete;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import integration.AbstractServerTest;
import integration.DeleteServiceTest;
import omero.api.IProjectionPrx;
import omero.cmd.Delete;
import omero.cmd.DoAll;
import omero.cmd.Request;
import omero.constants.projection.ProjectionType;
import omero.model.IObject;
import omero.model.Pixels;

import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.springframework.util.ResourceUtils;
import org.testng.annotations.Test;

/**
 * Deleted projected image and/or source image.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 4.4.9
 */
public class DeleteProjectedImageTest  extends AbstractServerTest {

    /** Indicates to move the source image. */
    private static final int SOURCE_IMAGE = 0;

    /** Indicates to move the projected image. */
    private static final int PROJECTED_IMAGE = 1;

    /** Indicates to move the both images. */
    private static final int BOTH_IMAGES = 2;

    /**
     * Imports the small dv.
     * The image has 5 z-sections, 6 timepoints, 1 channel, signed 16-bit.
     * 
     * @return The id of the pixels set.
     * @throws Exception Thrown if an error occurred.
     */
    private Pixels importImage() throws Exception
    {
        File srcFile = ResourceUtils.getFile("classpath:tinyTest.d3d.dv");
        List<Pixels> pixels = null;
        try {
            pixels = importFile(srcFile, "dv");
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        return pixels.get(0);
    }

    /** 
     * Creates an image and projects it either by the owner or by another
     * member of the group. The image is then moved by the owner of the image
     * or by another user.
     * 
     * @param src The permissions of the source group.
     * @param target The permissions of the source group.
     * @param memberRole The role of the other group member projecting the
     * image or <code>-1</code> if the owner projects the image.
     * @param moveMemberRole The role of the member moving the image
     * image or <code>-1</code> if the owner projects the image.
     * @param action One of the constants defined by this class.
     * @throws Exception Thrown if an error occurred.
     */
    private void deleteImage(String src, String target, int memberRole,
            int moveMemberRole, int action)
            throws Exception
    {
        EventContext ctx = newUserAndGroup(src);
        if (memberRole > 0) { //create a second user in the group.
            EventContext ctx2 = newUserInGroup(ctx);
            switch (memberRole) {
            case AbstractServerTest.ADMIN:
                logRootIntoGroup(ctx2);
                break;
            case AbstractServerTest.GROUP_OWNER:
                makeGroupOwner();
            }
        }
        Pixels pixels = importImage();
        long id = pixels.getImage().getId().getValue();
        List<Integer> channels = Arrays.asList(0);
        IProjectionPrx svc = factory.getProjectionService();
        long projectedID = svc.projectPixels(pixels.getId().getValue(), null,
                ProjectionType.MAXIMUMINTENSITY, 0, 1, channels, 1, 0, 1,
                "projectedImage");

        //login is as root
        if (moveMemberRole == AbstractServerTest.ADMIN)
            logRootIntoGroup(ctx);
        //delete the image(s)
        switch (action) {
        case SOURCE_IMAGE:
            delete(client, new Delete(DeleteServiceTest.REF_IMAGE, id, null));
            break;
        case PROJECTED_IMAGE:
            delete(client, new Delete(DeleteServiceTest.REF_IMAGE, projectedID,
                    null));
            break;
        case BOTH_IMAGES:
            List<Request> commands = new ArrayList<Request>();
            commands.add(new Delete(DeleteServiceTest.REF_IMAGE, id, null));
            commands.add(new Delete(DeleteServiceTest.REF_IMAGE, projectedID,
                    null));
            DoAll all = new DoAll();
            all.requests = commands;
            doChange(all);
        }

        //Check the result
        ParametersI param = new ParametersI();
        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id in (:ids)");
        List<Long> ids = new ArrayList<Long>();
        List<IObject> images;
        switch (action) {
        case SOURCE_IMAGE:
            ids.add(id);
            param.addIds(ids);
            images = iQuery.findAllByQuery(sb.toString(), param);
            assertEquals(images.size(), 0);
            //check that the projected image is still there
            param = new ParametersI();
            ids.clear();
            ids.add(projectedID);
            param.addIds(ids);
            images = iQuery.findAllByQuery(sb.toString(), param);
            assertEquals(images.size(), 1);
            break;
        case PROJECTED_IMAGE:
            ids.add(projectedID);
            param.addIds(ids);
            images = iQuery.findAllByQuery(sb.toString(), param);
            assertEquals(images.size(), 0);
           //check that the original image is still there
            param = new ParametersI();
            ids.clear();
            ids.add(id);
            param.addIds(ids);
            images = iQuery.findAllByQuery(sb.toString(), param);
            assertEquals(images.size(), 1);
            break;
        case BOTH_IMAGES:
            ids.add(id);
            ids.add(projectedID);
            param.addIds(ids);
            images = iQuery.findAllByQuery(sb.toString(), param);
            assertEquals(images.size(), 0);
        }
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RW---- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByOwnerRWtoRW() throws Exception {
        deleteImage("rw----", "rw----", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RW---- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByOwnerRWtoRW() throws Exception {
        deleteImage("rw----", "rw----", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RW---- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByOwnerRWtoRW() throws Exception {
        deleteImage("rw----", "rw----", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWR--- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByOwnerRWtoRWR() throws Exception {
        deleteImage("rw----", "rwr---", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWR--- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByOwnerRWtoRWR() throws Exception {
        deleteImage("rw----", "rwr---", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWR--- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByOwnerRWtoRWR() throws Exception {
        deleteImage("rw----", "rwr---", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRA-- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByOwnerRWtoRWRA() throws Exception {
        deleteImage("rw----", "rwra--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRA-- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByOwnerRWtoRWRA() throws Exception {
        deleteImage("rw----", "rwra--", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRA-- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByOwnerRWtoRWRA() throws Exception {
        deleteImage("rw----", "rwra--", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRW-- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByOwnerRWtoRWRW() throws Exception {
        deleteImage("rw----", "rwrw--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRW-- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByOwnerRWtoRWRW() throws Exception {
        deleteImage("rw----", "rwrw--", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRW-- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByOwnerRWtoRWRW() throws Exception {
        deleteImage("rw----", "rwrw--", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWRW-- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByOwnerRWRtoRWRW() throws Exception {
        deleteImage("rwr---", "rwrw--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWRW-- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByOwnerRWRtoRWRW() throws Exception {
        deleteImage("rwr---", "rwrw--", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRW-- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByOwnerRWRtoRWRW() throws Exception {
        deleteImage("rwr---", "rwrw--", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWRA-- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByOwnerRWRtoRWRA() throws Exception {
        deleteImage("rwr---", "rwra--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWRA-- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByOwnerRWRtoRWRA() throws Exception {
        deleteImage("rwr---", "rwra--", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRA-- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByOwnerRWRtoRWRA() throws Exception {
        deleteImage("rwr---", "rwra--", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWR--- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByOwnerRWRtoRWR() throws Exception {
        deleteImage("rwr---", "rwra--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWR--- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByOwnerRWRtoRWR() throws Exception {
        deleteImage("rwr---", "rwr---", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWR--- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByOwnerRWRtoRWR() throws Exception {
        deleteImage("rwr---", "rwr---", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RW---- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByOwnerRWRtoRW() throws Exception {
        deleteImage("rwr---", "rwra--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RW---- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByOwnerRWRtoRW() throws Exception {
        deleteImage("rwr---", "rw----", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RW---- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByOwnerRWRtoRW() throws Exception {
        deleteImage("rwr---", "rw----", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RW---- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByOwnerRWRAtoRW() throws Exception {
        deleteImage("rwra--", "rwra--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RW---- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByOwnerRWRAtoRW() throws Exception {
        deleteImage("rwra--", "rw----", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RW---- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByOwnerRWRAtoRW() throws Exception {
        deleteImage("rwra--", "rw----", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RWR--- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByOwnerRWRAtoRWR() throws Exception {
        deleteImage("rwra--", "rwr--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RWR--- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByOwnerRWRAtoRWR() throws Exception {
        deleteImage("rwra--", "rwr---", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RWR--- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByOwnerRWRAtoRWR() throws Exception {
        deleteImage("rwra--", "rwr---", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RWRA-- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByOwnerRWRAtoRWRA() throws Exception {
        deleteImage("rwra--", "rwra--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWRA-- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByOwnerRWRAtoRWRA() throws Exception {
        deleteImage("rwra--", "rwr---", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RWRA-- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByOwnerRWRAtoRWRA() throws Exception {
        deleteImage("rwra--", "rwra--", -1, -1, BOTH_IMAGES);
    }
    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RWRW-- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByOwnerRWRAtoRWRW() throws Exception {
        deleteImage("rwra--", "rwrw--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWRW-- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByOwnerRWRAtoRWRW() throws Exception {
        deleteImage("rwra--", "rwrw--", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RWRW-- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByOwnerRWRAtoRWRW() throws Exception {
        deleteImage("rwra--", "rwrw--", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWRW-- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByOwnerRWRWtoRWRW() throws Exception {
        deleteImage("rwrw--", "rwrw--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWRW-- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByOwnerRWRWtoRWRW() throws Exception {
        deleteImage("rwrw--", "rwrw--", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWRW-- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByOwnerRWRWtoRWRW() throws Exception {
        deleteImage("rwrw--", "rwrw--", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWRA-- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByOwnerRWRWtoRWRA() throws Exception {
        deleteImage("rwrw--", "rwrw--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWRA-- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByOwnerRWRWtoRWRA() throws Exception {
        deleteImage("rwrw--", "rwra--", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWRW-- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByOwnerRWRWtoRWRA() throws Exception {
        deleteImage("rwrw--", "rwra--", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWR--- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByOwnerRWRWtoRWR() throws Exception {
        deleteImage("rwrw--", "rwr---", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWR--- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByOwnerRWRWtoRWR() throws Exception {
        deleteImage("rwrw--", "rwr--", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWR--- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByOwnerRWRWtoRWR() throws Exception {
        deleteImage("rwrw--", "rwr---", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RW---- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByOwnerRWRWtoRW() throws Exception {
        deleteImage("rwrw--", "rwr---", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RW---- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByOwnerRWRWtoRW() throws Exception {
        deleteImage("rwrw--", "rw---", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RW---- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByOwnerRWRWtoRW() throws Exception {
        deleteImage("rwrw--", "rw----", -1, -1, BOTH_IMAGES);
    }

    //Project by another member move by Admin.
    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RW---- group. The source image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByAdminRWtoRW() throws Exception {
        deleteImage("rw----", "rw----", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RW---- group. The projected image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberMoveByAdminRWtoRW() throws Exception {
        deleteImage("rw----", "rw----", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RW---- group. Both images are moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberMoveByAdminRWtoRW() throws Exception {
        deleteImage("rw----", "rw----", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWR--- group. The source image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByAdminRWtoRWR() throws Exception {
        deleteImage("rw----", "rwr---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWR--- group. The projected image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberMoveByAdminRWtoRWR() throws Exception {
        deleteImage("rw----", "rwr---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWR--- group. Both images are moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberMoveByAdminRWtoRWR() throws Exception {
        deleteImage("rw----", "rwr---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRA-- group. The source image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByAdminRWtoRWRA() throws Exception {
        deleteImage("rw----", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRA-- group. The projected image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberMoveByAdminRWtoRWRA() throws Exception {
        deleteImage("rw----", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRA-- group. Both images are moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberMoveByAdminRWtoRWRA() throws Exception {
        deleteImage("rw----", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRW-- group. The source image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByAdminRWtoRWRW() throws Exception {
        deleteImage("rw----", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRW-- group. The projected image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberMoveByAdminRWtoRWRW() throws Exception {
        deleteImage("rw----", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRW-- group. Both images are moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberMoveByAdminRWtoRWRW() throws Exception {
        deleteImage("rw----", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWRW-- group. The source image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByAdminRWRtoRWRW() throws Exception {
        deleteImage("rwr---", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWRW-- group. The projected image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberMoveByAdminRWRtoRWRW() throws Exception {
        deleteImage("rwr---", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRW-- group. Both images are moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberMoveByAdminRWRtoRWRW() throws Exception {
        deleteImage("rwr---", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWRA-- group. The source image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByAdminRWRtoRWRA() throws Exception {
        deleteImage("rwr---", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWRA-- group. The projected image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberMoveByAdminRWRtoRWRA() throws Exception {
        deleteImage("rwr---", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRA-- group. Both images are moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberMoveByAdminRWRtoRWRA() throws Exception {
        deleteImage("rwr---", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWR--- group. The source image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByAdminRWRtoRWR() throws Exception {
        deleteImage("rwr---", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWR--- group. The projected image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberMoveByAdminRWRtoRWR() throws Exception {
        deleteImage("rwr---", "rwr---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWR--- group. Both images are moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberMoveByAdminRWRtoRWR() throws Exception {
        deleteImage("rwr---", "rwr---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RW---- group. The source image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByAdminRWRtoRW() throws Exception {
        deleteImage("rwr---", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RW---- group. The projected image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberMoveByAdminRWRtoRW() throws Exception {
        deleteImage("rwr---", "rw----", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RW---- group. Both images are moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberMoveByAdminRWRtoRW() throws Exception {
        deleteImage("rwr---", "rw----", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RW---- group. The source image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByAdminRWRAtoRW() throws Exception {
        deleteImage("rwra--", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RW---- group. The projected image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberMoveByAdminRWRAtoRW() throws Exception {
        deleteImage("rwra--", "rw----", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RW---- group. Both images are moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberMoveByAdminRWRAtoRW() throws Exception {
        deleteImage("rwra--", "rw----", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RWR--- group. The source image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByAdminRWRAtoRWR() throws Exception {
        deleteImage("rwra--", "rwr--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RWR--- group. The projected image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberMoveByAdminRWRAtoRWR() throws Exception {
        deleteImage("rwra--", "rwr---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RWR--- group. Both images are moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberMoveByAdminRWRAtoRWR() throws Exception {
        deleteImage("rwra--", "rwr---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RWRA-- group. The source image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByAdminRWRAtoRWRA() throws Exception {
        deleteImage("rwra--", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWRA-- group. The projected image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberMoveByAdminRWRAtoRWRA() throws Exception {
        deleteImage("rwra--", "rwr---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RWRA-- group. Both images are moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberMoveByAdminRWRAtoRWRA() throws Exception {
        deleteImage("rwra--", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }
    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RWRW-- group. The source image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByAdminRWRAtoRWRW() throws Exception {
        deleteImage("rwra--", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWRW-- group. The projected image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberMoveByAdminRWRAtoRWRW() throws Exception {
        deleteImage("rwra--", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RWRW-- group. Both images are moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberMoveByAdminRWRAtoRWRW() throws Exception {
        deleteImage("rwra--", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWRW-- group. The source image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByAdminRWRWtoRWRW() throws Exception {
        deleteImage("rwrw--", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWRW-- group. The projected image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberMoveByAdminRWRWtoRWRW() throws Exception {
        deleteImage("rwrw--", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWRW-- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberMoveByAdminRWRWtoRWRW() throws Exception {
        deleteImage("rwrw--", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWRA-- group. The source image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByAdminRWRWtoRWRA() throws Exception {
        deleteImage("rwrw--", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWRA-- group. The projected image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberMoveByAdminRWRWtoRWRA() throws Exception {
        deleteImage("rwrw--", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWRW-- group. Both images are moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberMoveByAdminRWRWtoRWRA() throws Exception {
        deleteImage("rwrw--", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWR--- group. The source image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByAdminRWRWtoRWR() throws Exception {
        deleteImage("rwrw--", "rwr---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWR--- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberMoveByAdminRWRWtoRWR() throws Exception {
        deleteImage("rwrw--", "rwr--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWR--- group. Both images are moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberMoveByAdminRWRWtoRWR() throws Exception {
        deleteImage("rwrw--", "rwr---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RW---- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByAdminRWRWtoRW() throws Exception {
        deleteImage("rwrw--", "rwr---", AbstractServerTest.MEMBER, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RW---- group. The projected image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberMoveByAdminRWRWtoRW() throws Exception {
        deleteImage("rwrw--", "rw---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RW---- group. Both images are moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberMoveByAdminRWRWtoRW() throws Exception {
        deleteImage("rwrw--", "rw----", AbstractServerTest.MEMBER, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    //move by admin
    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RW---- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByAdminRWtoRW() throws Exception {
        deleteImage("rw----", "rw----", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RW---- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByAdminRWtoRW() throws Exception {
        deleteImage("rw----", "rw----", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RW---- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByAdminRWtoRW() throws Exception {
        deleteImage("rw----", "rw----", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWR--- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByAdminRWtoRWR() throws Exception {
        deleteImage("rw----", "rwr---", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWR--- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByAdminRWtoRWR() throws Exception {
        deleteImage("rw----", "rwr---", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWR--- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByAdminRWtoRWR() throws Exception {
        deleteImage("rw----", "rwr---", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRA-- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByAdminRWtoRWRA() throws Exception {
        deleteImage("rw----", "rwra--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRA-- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByAdminRWtoRWRA() throws Exception {
        deleteImage("rw----", "rwra--", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRA-- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByAdminRWtoRWRA() throws Exception {
        deleteImage("rw----", "rwra--", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRW-- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByAdminRWtoRWRW() throws Exception {
        deleteImage("rw----", "rwrw--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRW-- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByAdminRWtoRWRW() throws Exception {
        deleteImage("rw----", "rwrw--", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRW-- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByAdminRWtoRWRW() throws Exception {
        deleteImage("rw----", "rwrw--", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWRW-- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByAdminRWRtoRWRW() throws Exception {
        deleteImage("rwr---", "rwrw--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWRW-- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByAdminRWRtoRWRW() throws Exception {
        deleteImage("rwr---", "rwrw--", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRW-- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByAdminRWRtoRWRW() throws Exception {
        deleteImage("rwr---", "rwrw--", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWRA-- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByAdminRWRtoRWRA() throws Exception {
        deleteImage("rwr---", "rwra--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWRA-- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByAdminRWRtoRWRA() throws Exception {
        deleteImage("rwr---", "rwra--", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRA-- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByAdminRWRtoRWRA() throws Exception {
        deleteImage("rwr---", "rwra--", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWR--- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByAdminRWRtoRWR() throws Exception {
        deleteImage("rwr---", "rwra--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWR--- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByAdminRWRtoRWR() throws Exception {
        deleteImage("rwr---", "rwr---", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWR--- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByAdminRWRtoRWR() throws Exception {
        deleteImage("rwr---", "rwr---", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RW---- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByAdminRWRtoRW() throws Exception {
        deleteImage("rwr---", "rwra--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RW---- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByAdminRWRtoRW() throws Exception {
        deleteImage("rwr---", "rw----", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RW---- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByAdminRWRtoRW() throws Exception {
        deleteImage("rwr---", "rw----", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RW---- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByAdminRWRAtoRW() throws Exception {
        deleteImage("rwra--", "rwra--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RW---- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByAdminRWRAtoRW() throws Exception {
        deleteImage("rwra--", "rw----", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RW---- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByAdminRWRAtoRW() throws Exception {
        deleteImage("rwra--", "rw----", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RWR--- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByAdminRWRAtoRWR() throws Exception {
        deleteImage("rwra--", "rwr--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RWR--- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByAdminRWRAtoRWR() throws Exception {
        deleteImage("rwra--", "rwr---", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RWR--- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByAdminRWRAtoRWR() throws Exception {
        deleteImage("rwra--", "rwr---", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RWRA-- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByAdminRWRAtoRWRA() throws Exception {
        deleteImage("rwra--", "rwra--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWRA-- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByAdminRWRAtoRWRA() throws Exception {
        deleteImage("rwra--", "rwr---", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RWRA-- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByAdminRWRAtoRWRA() throws Exception {
        deleteImage("rwra--", "rwra--", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }
    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RWRW-- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByAdminRWRAtoRWRW() throws Exception {
        deleteImage("rwra--", "rwrw--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWRW-- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByAdminRWRAtoRWRW() throws Exception {
        deleteImage("rwra--", "rwrw--", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RWRW-- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByAdminRWRAtoRWRW() throws Exception {
        deleteImage("rwra--", "rwrw--", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWRW-- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByAdminRWRWtoRWRW() throws Exception {
        deleteImage("rwrw--", "rwrw--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWRW-- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByAdminRWRWtoRWRW() throws Exception {
        deleteImage("rwrw--", "rwrw--", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWRW-- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByAdminRWRWtoRWRW() throws Exception {
        deleteImage("rwrw--", "rwrw--", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWRA-- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByAdminRWRWtoRWRA() throws Exception {
        deleteImage("rwrw--", "rwrw--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWRA-- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByAdminRWRWtoRWRA() throws Exception {
        deleteImage("rwrw--", "rwra--", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWRW-- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByAdminRWRWtoRWRA() throws Exception {
        deleteImage("rwrw--", "rwra--", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWR--- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByAdminRWRWtoRWR() throws Exception {
        deleteImage("rwrw--", "rwr---", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWR--- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByAdminRWRWtoRWR() throws Exception {
        deleteImage("rwrw--", "rwr--", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWR--- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByAdminRWRWtoRWR() throws Exception {
        deleteImage("rwrw--", "rwr---", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RW---- group. The source image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerMoveByAdminRWRWtoRW() throws Exception {
        deleteImage("rwrw--", "rwr---", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RW---- group. The projected image is moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerMoveByAdminRWRWtoRW() throws Exception {
        deleteImage("rwrw--", "rw---", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RW---- group. Both images are moved.
     * The projection is done by the owner of the data and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerMoveByAdminRWRWtoRW() throws Exception {
        deleteImage("rwrw--", "rw----", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }
    
    //Projected by another member move by data owner.
    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RW---- group. The source image is moved.
     * The projection is done by a member and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByOwnerRWtoRW() throws Exception {
        deleteImage("rw----", "rw----", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWR--- group. The source image is moved.
     * The projection is done by a member and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByOwnerRWtoRWR() throws Exception {
        deleteImage("rw----", "rwr---", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRA-- group. The source image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByOwnerRWtoRWRA() throws Exception {
        deleteImage("rw----", "rwra--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RW---- to a RWRW-- group. The source image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByOwnerRWtoRWRW() throws Exception {
        deleteImage("rw----", "rwrw--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWRW-- group. The source image is moved.
     * The projection is done by a member and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByOwnerRWRtoRWRW() throws Exception {
        deleteImage("rwr---", "rwrw--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWRA-- group. The source image is moved.
     * The projection is done by a member and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByOwnerRWRtoRWRA() throws Exception {
        deleteImage("rwr---", "rwra--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RWR--- group. The source image is moved.
     * The projection is done by a member and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByOwnerRWRtoRWR() throws Exception {
        deleteImage("rwr---", "rwra--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWR--- to a RW---- group. The source image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByOwnerRWRtoRW() throws Exception {
        deleteImage("rwr---", "rwra--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RW---- group. The source image is moved.
     * The projection is done by a member and moved by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByOwnerRWRAtoRW() throws Exception {
        deleteImage("rwra--", "rwra--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RWR--- group. The source image is moved.
     * The projection is done by a member and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByOwnerRWRAtoRWR() throws Exception {
        deleteImage("rwra--", "rwr--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RWRA-- group. The source image is moved.
     * The projection is done by a member and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByOwnerRWRAtoRWRA() throws Exception {
        deleteImage("rwra--", "rwra--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRA-- to a RWRW-- group. The source image is moved.
     * The projection is done by a member and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByOwnerRWRAtoRWRW() throws Exception {
        deleteImage("rwra--", "rwrw--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWRW-- group. The source image is moved.
     * The projection is done by a member and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByOwnerRWRWtoRWRW() throws Exception {
        deleteImage("rwrw--", "rwrw--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWRA-- group. The source image is moved.
     * The projection is done by a member and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByOwnerRWRWtoRWRA() throws Exception {
        deleteImage("rwrw--", "rwrw--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RWR--- group. The source image is moved.
     * The projection is done by a member and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByOwnerRWRWtoRWR() throws Exception {
        deleteImage("rwrw--", "rwr---", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the move of the image that has been projected from a
     * RWRW-- to a RW---- group. The source image is moved.
     * The projection is done by a member and moved by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberMoveByOwnerRWRWtoRW() throws Exception {
        deleteImage("rwrw--", "rwr---", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

}
