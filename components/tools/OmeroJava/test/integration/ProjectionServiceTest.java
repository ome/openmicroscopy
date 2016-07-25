/*
 * $Id$
 *
 * Copyright 2013 University of Dundee. All rights reserved.
 * Use is subject to license terms supplied in LICENSE.txt
 */
package integration;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import omero.ValidationException;
import omero.api.IProjectionPrx;
import omero.constants.projection.ProjectionType;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.PixelsType;
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.springframework.util.ResourceUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test the projection of an image by different users in all groups type.
 * Test also methods by passing invalid parameters.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4.9
 */
public class ProjectionServiceTest extends AbstractServerTest
{

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
     * member of the group.
     * 
     * @param perms The permissions of the group.
     * @param role The role of the other group member projecting the image or
     * <code>-1</code> if the owner projects the image.
     * @throws Exception Thrown if an error occurred.
     */
    private void projectImage(String perms, int memberRole)
            throws Exception
    {
        EventContext ctx = newUserAndGroup(perms);
        long ownerID = ctx.userId;
        if (memberRole > 0) { //create a second user in the group.
            EventContext ctx2 = newUserInGroup(ctx);
            switch (memberRole) {
            case AbstractServerTest.ADMIN:
                logRootIntoGroup(ctx2);
                break;
            case AbstractServerTest.GROUP_OWNER:
                makeGroupOwner();
            }
            ctx2 = iAdmin.getEventContext();
            ownerID = ctx2.userId;
        }
        Pixels pixels = importImage();
        List<Integer> channels = Arrays.asList(0);
        Image img = projectImage(pixels, 0, pixels.getSizeT().getValue()-1, 0,
                pixels.getSizeZ().getValue()-1, 1,
                ProjectionType.MAXIMUMINTENSITY, null, channels);
        Assert.assertEquals(ownerID, img.getDetails().getOwner().getId().getValue());
    }

    /**
     * Projects the image.
     * 
     * @param pixels The pixels set.
     * @param startT The lower bound of the timepoint interval.
     * @param endT The upper bound of the timepoint interval.
     * @param startZ The lower bound of the z-section interval.
     * @param endZ The upper bound of the z-section interval.
     * @param stepping The stepping.
     * @param prjType The type of projection to perform.
     * @param pixelsType The type of pixels to generate.
     * @param channels The list of channels' indexes.
     * @return The projected image.
     * @throws Exception Thrown if an error occurred.
     */
    private Image projectImage(Pixels pixels, int startT, int endT, int startZ,
            int endZ, int stepping, ProjectionType prjType,
            PixelsType pixelsType, List<Integer> channels)
            throws Exception
    {
        IProjectionPrx svc = factory.getProjectionService();
        long imageID = svc.projectPixels(pixels.getId().getValue(), pixelsType,
                prjType, startT, endT, channels, stepping, startZ, endZ,
                "projectedImage");
        Assert.assertTrue(imageID > 0);
        List<Image> images =
                factory.getContainerService().getImages(Image.class.getName(),
                Arrays.asList(imageID), new ParametersI());
        Assert.assertEquals(1, images.size());
        Pixels p = images.get(0).getPixels(0);
        Assert.assertEquals(channels.size(), p.getSizeC().getValue());
        Assert.assertEquals(Math.abs(startT-endT)+1, p.getSizeT().getValue());
        Assert.assertEquals(1, p.getSizeZ().getValue());
        if (pixelsType == null) pixelsType = pixels.getPixelsType();
        Assert.assertEquals(pixelsType.getValue().getValue(),
                p.getPixelsType().getValue().getValue());
        return images.get(0);
    }

    /**
     * Projects the image.
     * 
     * @param pixelsID The id of the pixels set.
     * @param timepoint The selected timepoint.
     * @param startZ The lower bound of the z-section interval.
     * @param endZ The upper bound of the z-section interval.
     * @param stepping The stepping.
     * @param prjType The type of projection to perform.
     * @param pixelsType The type of pixels to generate.
     * @param channelIndex The channel's index.
     * @throws Exception Thrown if an error occurred.
     */
    private void projectStackImage(long pixelsID, int timepoint, int startZ,
            int endZ, int stepping, ProjectionType prjType,
            PixelsType pixelsType, int channelIndex)
            throws Exception
    {
        IProjectionPrx svc = factory.getProjectionService();
        byte[] value = svc.projectStack(pixelsID, pixelsType, prjType,
                timepoint, channelIndex, stepping, startZ, endZ);
        Assert.assertTrue(value.length > 0);
        //TODO: more check to be added
    }
    
    /**
     * Test the possible projection type.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectionMeanIntensity() throws Exception {
        Pixels pixels = importImage();
        List<Integer> channels = Arrays.asList(0);
        projectImage(pixels, 0, pixels.getSizeT().getValue()-1, 0,
                pixels.getSizeZ().getValue()-1, 1,
                ProjectionType.MEANINTENSITY, null, channels);
    }

    /**
     * Test the possible projection type.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectionSumIntensity() throws Exception {
        Pixels pixels = importImage();
        List<Integer> channels = Arrays.asList(0);
        projectImage(pixels, 0, 0, 0,
                pixels.getSizeZ().getValue()-1, 1,
                ProjectionType.SUMINTENSITY, null, channels);
    }

    /**
     * Test the possible projection type.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectionMaxIntensity() throws Exception {
        Pixels pixels = importImage();
        List<Integer> channels = Arrays.asList(0);
        projectImage(pixels, 0, pixels.getSizeT().getValue()-1, 0,
                pixels.getSizeZ().getValue()-1, 1,
                ProjectionType.MAXIMUMINTENSITY, null, channels);
    }

    /**
     * Test the projection with an invalid timepoint range.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test(expectedExceptions = ValidationException.class)
    public void testWrongTimepointIntervalUpperBound() throws Exception {
        Pixels pixels = importImage();
        List<Integer> channels = Arrays.asList(0);
        projectImage(pixels, 0, pixels.getSizeT().getValue(), 0,
                pixels.getSizeZ().getValue()-1, 1,
                ProjectionType.MAXIMUMINTENSITY, null, channels);
    }

    /**
     * Test the projection with an invalid timepoint range.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test(expectedExceptions = ValidationException.class)
    public void testWrongTimepointIntervalLowerBound() throws Exception {
        Pixels pixels = importImage();
        List<Integer> channels = Arrays.asList(0);
        projectImage(pixels, -1, pixels.getSizeT().getValue()-1, 0,
                pixels.getSizeZ().getValue()-1, 1,
                ProjectionType.MAXIMUMINTENSITY, null, channels);
    }

    /**
     * Test the projection with a timepoint range with lower bound = upper bound
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSameTimepointInterval() throws Exception {
        Pixels pixels = importImage();
        List<Integer> channels = Arrays.asList(0);
        projectImage(pixels, 0, 0, 0, pixels.getSizeZ().getValue()-1, 1,
                ProjectionType.MAXIMUMINTENSITY, null, channels);
    }

    /**
     * Test the projection with an invalid timepoint range.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test(expectedExceptions = ValidationException.class)
    public void testWrongTimepointInterval() throws Exception {
        Pixels pixels = importImage();
        List<Integer> channels = Arrays.asList(0);
        projectImage(pixels, 6, 7, 0, pixels.getSizeZ().getValue()-1, 1,
                ProjectionType.MAXIMUMINTENSITY, null, channels);
    }

    /**
     * Test the projection with no channels specified.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test(expectedExceptions = ValidationException.class)
    public void testNoChannels() throws Exception {
        Pixels pixels = importImage();
        List<Integer> channels = new ArrayList<Integer>();
        projectImage(pixels, 0, pixels.getSizeT().getValue()-1, 0,
                pixels.getSizeZ().getValue(), 1,
                ProjectionType.MAXIMUMINTENSITY, null, channels);
    }

    /**
     * Test the projection with <code>null</code> channels list.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test(expectedExceptions = ValidationException.class)
    public void testNullChannels() throws Exception {
        Pixels pixels = importImage();
        projectImage(pixels, 0, pixels.getSizeT().getValue()-1, 0,
                pixels.getSizeZ().getValue(), 1,
                ProjectionType.MAXIMUMINTENSITY, null, null);
    }

    /**
     * Test the projection with an invalid channel index
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test(expectedExceptions = ValidationException.class)
    public void testWrongChannels() throws Exception {
        Pixels pixels = importImage();
        List<Integer> channels = Arrays.asList(1);
        projectImage(pixels, 0, pixels.getSizeT().getValue()-1, 0,
                pixels.getSizeZ().getValue(), 1,
                ProjectionType.MAXIMUMINTENSITY, null, channels);
    }

    /**
     * Test the projection with an invalid z-section range.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test(expectedExceptions = ValidationException.class)
    public void testWrongZSectionIntervalUpperBound() throws Exception {
        Pixels pixels = importImage();
        List<Integer> channels = Arrays.asList(0);
        projectImage(pixels, 0, pixels.getSizeT().getValue()-1, 0,
                pixels.getSizeZ().getValue(), 1,
                ProjectionType.MAXIMUMINTENSITY, null, channels);
    }

    /**
     * Test the projection with a z-sections range with same value.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSameZSectionInterval() throws Exception {
        Pixels pixels = importImage();
        List<Integer> channels = Arrays.asList(0);
        projectImage(pixels, 0, pixels.getSizeT().getValue()-1, 0, 1, 1,
                ProjectionType.MAXIMUMINTENSITY, null, channels);
    }

    /**
     * Test the projection with an invalid timepoint range.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test(expectedExceptions = ValidationException.class)
    public void testWrongZSectionIntervalLowerBound() throws Exception {
        Pixels pixels = importImage();
        List<Integer> channels = Arrays.asList(0);
        projectImage(pixels, 0, pixels.getSizeT().getValue()-1, -1,
                pixels.getSizeZ().getValue()-1, 1,
                ProjectionType.MAXIMUMINTENSITY, null, channels);
    }

    /**
     * Test the projection with a negative stepping
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test(expectedExceptions = ValidationException.class)
    public void testNegativeStepping() throws Exception {
        Pixels pixels = importImage();
        List<Integer> channels = Arrays.asList(0);
        projectImage(pixels, 0, pixels.getSizeT().getValue()-1, 0,
                pixels.getSizeZ().getValue()-1, -10,
                ProjectionType.MAXIMUMINTENSITY, null, channels);
    }

    /**
     * Test the projection with 2 steps
     *
     * @throws Exception Thrown if an error occurred.
     */
    public void testTwoSteps() throws Exception {
        Pixels pixels = importImage();
        List<Integer> channels = Arrays.asList(0);
        projectImage(pixels, 0, pixels.getSizeT().getValue()-1, 0,
                pixels.getSizeZ().getValue()-1, 2,
                ProjectionType.MAXIMUMINTENSITY, null, channels);
    }

    /**
     * Test the projection with 0 step
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test(expectedExceptions = ValidationException.class)
    public void testZeroStep() throws Exception {
        Pixels pixels = importImage();
        List<Integer> channels = Arrays.asList(0);
        projectImage(pixels, 0, pixels.getSizeT().getValue()-1, 0,
                pixels.getSizeZ().getValue()-1, 0,
                ProjectionType.MAXIMUMINTENSITY, null, channels);
    }

    /**
     * Test the projection of stack with a negative stepping
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test(expectedExceptions = ValidationException.class)
    public void testProjectStackNegativeStepping() throws Exception {
        Pixels pixels = importImage();
        projectStackImage(pixels.getId().getValue(), 0, 0,
                pixels.getSizeZ().getValue()-1, -10,
                ProjectionType.MAXIMUMINTENSITY, null, 0);
    }

    /**
     * Test the projection of stack with a zero stepping
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test(expectedExceptions = ValidationException.class)
    public void testProjectStackZero() throws Exception {
        Pixels pixels = importImage();
        projectStackImage(pixels.getId().getValue(), 0, 0,
                pixels.getSizeZ().getValue()-1, 0,
                ProjectionType.MAXIMUMINTENSITY, null, 0);
    }

    //Permissions testing.
    /**
     * Test the projection of the image by the owner of the data in a
     * RW---- group.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectImageByOwnerRW() throws Exception {
        projectImage("rw----", -1);
    }

    /**
     * Test the projection of the image by the owner of the data in a
     * RWR--- group.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectImageByOwnerRWR() throws Exception {
        projectImage("rwr---", -1);
    }

    /**
     * Test the projection of the image by the owner of the data in a
     * RWRA-- group.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectImageByOwnerRWRA() throws Exception {
        projectImage("rwra--", -1);
    }

    /**
     * Test the projection of the image by the owner of the data in a
     * RWR--- group.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectImageByOwnerRWRW() throws Exception {
        projectImage("rwrw--", -1);
    }

    /**
     * Test the projection of the image by the member of the group
     * in a RW---- group.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectImageByMemberRW() throws Exception {
        projectImage("rw----", AbstractServerTest.MEMBER);
    }

    /**
     * Test the projection of the image by the group owner of the group
     * in a RW---- group.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectImageByGroupOwnerRW() throws Exception {
        projectImage("rw----", AbstractServerTest.GROUP_OWNER);
    }

    /**
     * Test the projection of the image by an administrator.
     * in a RW---- group.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectImageByAdminRW() throws Exception {
        projectImage("rw----", AbstractServerTest.ADMIN);
    }

    /**
     * Test the projection of the image by a member of the group
     * in a RWR--- group.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectImageByGroupMemberRWR() throws Exception {
        projectImage("rwr---", AbstractServerTest.MEMBER);
    }

    /**
     * Test the projection of the image by the group owner of the group
     * in a RWR--- group.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectImageByGroupOwnerRWR() throws Exception {
        projectImage("rwr---", AbstractServerTest.GROUP_OWNER);
    }

    /**
     * Test the projection of the image by an administrator.
     * in a RWR--- group.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectImageByAdminRWR() throws Exception {
        projectImage("rwr---", AbstractServerTest.ADMIN);
    }

    /**
     * Test the projection of the image by a member of the group
     * in a RWRA-- group.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectImageByGroupMemberRWRA() throws Exception {
        projectImage("rwra--", AbstractServerTest.MEMBER);
    }

    /**
     * Test the projection of the image by the group owner of the group
     * in a RWRA-- group.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectImageByGroupOwnerRWRA() throws Exception {
        projectImage("rwra--", AbstractServerTest.GROUP_OWNER);
    }

    /**
     * Test the projection of the image by an administrator.
     * in a RWRA-- group.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectImageByAdminRWRA() throws Exception {
        projectImage("rwra--", AbstractServerTest.ADMIN);
    }

    /**
     * Test the projection of the image by a member of the group
     * in a RWRW-- group.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectImageByGroupMemberRWRW() throws Exception {
        projectImage("rwrw--", AbstractServerTest.MEMBER);
    }

    /**
     * Test the projection of the image by the group owner of the group
     * in a RWRW-- group.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectImageByGroupOwnerRWRW() throws Exception {
        projectImage("rwrw--", AbstractServerTest.GROUP_OWNER);
    }

    /**
     * Test the projection of the image by an administrator.
     * in a RWRW-- group.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectImageByAdminRWRW() throws Exception {
        projectImage("rwrw--", AbstractServerTest.ADMIN);
    }
}
