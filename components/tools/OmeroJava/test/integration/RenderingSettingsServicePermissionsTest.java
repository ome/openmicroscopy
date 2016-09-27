/*
 * $Id$
 *
 *  Copyright 2006-2011 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import omero.api.IRenderingSettingsPrx;
import omero.cmd.Chmod2;
import omero.gateway.util.Requests;
import omero.model.ChannelBinding;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.RenderingDef;
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests copy/paste of rendering settings depending on the group's permissions.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class RenderingSettingsServicePermissionsTest extends AbstractServerTest {

    /**
     * Tests the <code>resetDefaultsByOwnerInSet</code>
     *
     * @param permissions
     * @param role
     * @throws Exception
     */
    private void resetDefaultByOwnerInSetFor(String permissions, int role)
            throws Exception {
        EventContext ctx = newUserAndGroup(permissions);
        Image image = createBinaryImage();
        Pixels pixels = image.getPrimaryPixels();
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        prx.setOriginalSettingsInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));
        // Image
        disconnect();
        EventContext ctx2 = newUserInGroup(ctx);
        switch (role) {
            case ADMIN:
                logRootIntoGroup(ctx2);
                break;
            case GROUP_OWNER:
                makeGroupOwner();
        }
        factory.getRenderingSettingsService().setOriginalSettingsInSet(
                Image.class.getName(),
                Arrays.asList(image.getId().getValue()));
        List<Long> ids = new ArrayList<Long>();
        ids.add(image.getId().getValue());
        List<Long> v = factory.getRenderingSettingsService()
                .resetDefaultsByOwnerInSet(Image.class.getName(), ids);
        Assert.assertNotNull(v);
        Assert.assertEquals(1, v.size());
        ParametersI param = new ParametersI();
        param.addLong("pid", pixels.getId().getValue());
        String sql = "select rdef from RenderingDef as rdef "
                + "where rdef.pixels.id = :pid";
        List<IObject> values = iQuery.findAllByQuery(sql, param);
        Assert.assertNotNull(values);
        Assert.assertEquals(2, values.size());
    }

    /**
     * Applies the settings to a given image.
     *
     * @param permissions
     *            The permissions of the group.
     * @param role
     *            The role of the user. The user can be the group owner, and
     *            administrator or a group member.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    private void saveSettingsOtherUserImage(String permissions, int role)
            throws Exception {
        EventContext ctx = newUserAndGroup(permissions);
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = createBinaryImage();
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        // Generate settings for the 3 images.
        prx.setOriginalSettingsInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));

        disconnect();
        EventContext ctx2 = newUserInGroup(ctx);
        switch (role) {
            case ADMIN:
                logRootIntoGroup(ctx2);
                break;
            case GROUP_OWNER:
                makeGroupOwner();
        }
        factory.getRenderingSettingsService().setOriginalSettingsInSet(
                Image.class.getName(), Arrays.asList(image.getId().getValue()));
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        Assert.assertNotNull(def);
        ChannelBinding cb = def.getChannelBinding(0);
        boolean b = cb.getActive().getValue();
        cb.setActive(omero.rtypes.rbool(!b));

        factory.getPixelsService().saveRndSettings(def);
        disconnect();
    }

    /**
     * Applies the settings to a given image.
     *
     * @param permissions
     *            The permissions of the group.
     * @param role
     *            The role of the user. The user can be the group owner, and
     *            administrator or a group member.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    private void applySettingsToSetSourcetImageViewedByOther(
            String permissions, int role) throws Exception {
        EventContext ctx = newUserAndGroup(permissions);
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = createBinaryImage();
        Image image2 = createBinaryImage();
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        List<Long> ids = new ArrayList<Long>();
        ids.add(image.getId().getValue());
        ids.add(image2.getId().getValue());
        // Generate settings for the 3 images.
        prx.setOriginalSettingsInSet(Image.class.getName(), ids);
        disconnect();
        EventContext ctx2 = newUserInGroup(ctx);
        switch (role) {
            case ADMIN:
                logRootIntoGroup(ctx2);
                break;
            case GROUP_OWNER:
                makeGroupOwner();
        }
        factory.getRenderingSettingsService().setOriginalSettingsInSet(
                Image.class.getName(), Arrays.asList(id));
        disconnect();
        init(ctx);
        // method already tested
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        long pix2 = image2.getPrimaryPixels().getId().getValue();
        ChannelBinding cb = def.getChannelBinding(0);
        boolean b = cb.getActive().getValue();
        cb.setActive(omero.rtypes.rbool(!b));
        def = (RenderingDef) iUpdate.saveAndReturnObject(def);

        ids.clear();
        ids.add(image2.getId().getValue());
        // Change the settings of image 2
        factory.getRenderingSettingsService().applySettingsToSet(id,
                Image.class.getName(), ids);
        RenderingDef def2 = factory.getPixelsService()
                .retrieveRndSettings(pix2);

        cb = def2.getChannelBinding(0);
        Assert.assertEquals(!b, cb.getActive().getValue());
        ids.add(image.getId().getValue());
        factory.getRenderingSettingsService().applySettingsToSet(id,
                Image.class.getName(), ids);
    }

    /**
     * Applies the settings to a given image.
     *
     * @param permissions
     *            The permissions of the group.
     * @param role
     *            The role of the user. The user can be the group owner, and
     *            administrator or a group member.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    private void applySettingsToSetTargetImageViewedByOther(String permissions,
            int role) throws Exception {
        EventContext ctx = newUserAndGroup(permissions);
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = createBinaryImage();
        Image image2 = createBinaryImage();
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        List<Long> ids = new ArrayList<Long>();
        ids.add(image.getId().getValue());
        ids.add(image2.getId().getValue());
        // Generate settings for the 3 images.
        prx.setOriginalSettingsInSet(Image.class.getName(), ids);
        disconnect();
        EventContext ctx2 = newUserInGroup(ctx);
        switch (role) {
            case ADMIN:
                logRootIntoGroup(ctx2);
                break;
            case GROUP_OWNER:
                makeGroupOwner();
        }
        factory.getRenderingSettingsService()
                .setOriginalSettingsInSet(Image.class.getName(),
                        Arrays.asList(image2.getId().getValue()));
        disconnect();
        init(ctx);

        // method already tested
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        long pix2 = image2.getPrimaryPixels().getId().getValue();
        ChannelBinding cb = def.getChannelBinding(0);
        boolean b = cb.getActive().getValue();
        cb.setActive(omero.rtypes.rbool(!b));
        def = (RenderingDef) iUpdate.saveAndReturnObject(def);

        ids.clear();
        ids.add(image2.getId().getValue());
        // Change the settings of image 2
        factory.getRenderingSettingsService().applySettingsToSet(id,
                Image.class.getName(), ids);
        RenderingDef def2 = factory.getPixelsService()
                .retrieveRndSettings(pix2);

        cb = def2.getChannelBinding(0);
        Assert.assertEquals(!b, cb.getActive().getValue());
    }

    /**
     *
     * @param permissions
     *            The permissions of the group.
     * @param role
     *            The role of the user. The user can be the group owner, and
     *            administrator or a group member.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    private void applySettingsToSetTargetImageNoSettings(String permissions,
            int role) throws Exception {
        EventContext ctx = newUserAndGroup(permissions);
        switch (role) {
            case ADMIN:
                logRootIntoGroup(ctx);
                break;
            case GROUP_OWNER:
                makeGroupOwner();
        }
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = createBinaryImage();
        Image image2 = createBinaryImage();
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        List<Long> ids = new ArrayList<Long>();
        ids.add(image.getId().getValue());
        // Generate settings for the 3 images.
        prx.setOriginalSettingsInSet(Image.class.getName(), ids);

        // method already tested
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        long pix2 = image2.getPrimaryPixels().getId().getValue();
        ChannelBinding cb = def.getChannelBinding(0);
        boolean b = cb.getActive().getValue();
        cb.setActive(omero.rtypes.rbool(!b));
        def = (RenderingDef) iUpdate.saveAndReturnObject(def);

        ids.clear();
        ids.add(image2.getId().getValue());
        // apply the settings of image1 to image2 and 3
        prx.applySettingsToSet(id, Image.class.getName(), ids);
        RenderingDef def2 = factory.getPixelsService()
                .retrieveRndSettings(pix2);
        cb = def2.getChannelBinding(0);
        Assert.assertEquals(!b, cb.getActive().getValue());
    }

    /**
     * Applies the rendering settings to image when the group permissions are
     * modified.
     *
     * @param initial
     *            The initial permissions.
     * @param modified
     *            The modified permissions.
     * @throws Exception
     */
    private void applySettingsToSetForImageGroupDowngrade(String initial,
            String modified) throws Exception {
        EventContext ctx = newUserAndGroup(initial);
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = createBinaryImage();
        Image image2 = createBinaryImage();
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        List<Long> ids = new ArrayList<Long>();
        ids.add(image.getId().getValue());
        ids.add(image2.getId().getValue());
        // Generate settings for the 3 images.
        prx.setOriginalSettingsInSet(Image.class.getName(), ids);

        disconnect();
        EventContext ctx2 = newUserInGroup(ctx);
        prx = factory.getRenderingSettingsService();
        prx.setOriginalSettingsInSet(Image.class.getName(), Arrays.asList(id));
        disconnect();
        init(ctx);

        final Chmod2 chmod = Requests.chmod().target("ExperimenterGroup").id(ctx.groupId).toPerms(modified).build();
        doChange(root, root.getSession(), chmod, true);
        // method already tested
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        long pix2 = image2.getPrimaryPixels().getId().getValue();
        ChannelBinding cb = def.getChannelBinding(0);
        boolean b = cb.getActive().getValue();
        cb.setActive(omero.rtypes.rbool(!b));
        def = (RenderingDef) iUpdate.saveAndReturnObject(def);

        ids.clear();
        ids.add(image2.getId().getValue());
        // apply the settings of image1 to image2 and 3
        factory.getRenderingSettingsService().applySettingsToSet(id,
                Image.class.getName(), ids);
        RenderingDef def2 = factory.getPixelsService()
                .retrieveRndSettings(pix2);
        cb = def2.getChannelBinding(0);
        Assert.assertEquals(!b, cb.getActive().getValue());
    }

    /**
     * Applies the rendering settings to images by a member.
     *
     * @param permissions
     *            The permissions of the group.
     * @param role
     *            The role of the user. The user can be the group owner, and
     *            administrator or a group member.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    private void applySettingsToSetForImage(String permissions, int role)
            throws Exception {
        EventContext ctx = newUserAndGroup(permissions);
        switch (role) {
            case ADMIN:
                logRootIntoGroup(ctx);
                break;
            case GROUP_OWNER:
                makeGroupOwner();
        }
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = createBinaryImage();
        Image image2 = createBinaryImage();
        Image image3 = createBinaryImage();
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        List<Long> ids = new ArrayList<Long>();
        ids.add(image.getId().getValue());
        ids.add(image2.getId().getValue());
        ids.add(image3.getId().getValue());
        // Generate settings for the 3 images.
        prx.setOriginalSettingsInSet(Image.class.getName(), ids);

        // method already tested
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        long pix2 = image2.getPrimaryPixels().getId().getValue();
        long pix3 = image3.getPrimaryPixels().getId().getValue();
        ChannelBinding cb = def.getChannelBinding(0);
        boolean b = cb.getActive().getValue();
        cb.setActive(omero.rtypes.rbool(!b));
        def = (RenderingDef) iUpdate.saveAndReturnObject(def);

        ids.clear();
        ids.add(image2.getId().getValue());
        ids.add(image3.getId().getValue());
        // apply the settings of image1 to image2 and 3
        prx.applySettingsToSet(id, Image.class.getName(), ids);
        RenderingDef def2 = factory.getPixelsService()
                .retrieveRndSettings(pix2);
        RenderingDef def3 = factory.getPixelsService()
                .retrieveRndSettings(pix3);
        cb = def2.getChannelBinding(0);
        Assert.assertEquals(!b, cb.getActive().getValue());
        cb = def3.getChannelBinding(0);
        Assert.assertEquals(!b, cb.getActive().getValue());

        // Now pass the original image too.
        ids.add(image.getId().getValue());
        prx.applySettingsToSet(id, Image.class.getName(), ids);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * private group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForImageByGroupOwnerRW() throws Exception {
        applySettingsToSetForImage("rw----", GROUP_OWNER);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * private group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForImageByAdminOwnerRW() throws Exception {
        applySettingsToSetForImage("rw----", ADMIN);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * private group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForImageByMemberOwnerRW()
            throws Exception {
        applySettingsToSetForImage("rw----", MEMBER);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWR---</code> group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForImageByGroupOwnerRWR()
            throws Exception {
        applySettingsToSetForImage("rwr---", GROUP_OWNER);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWR---</code> group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForImageByAdminRWR() throws Exception {
        applySettingsToSetForImage("rwr---", ADMIN);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWR---</code> group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForImageByMemberRWR() throws Exception {
        applySettingsToSetForImage("rwr---", MEMBER);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWRA--</code> group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForImageByGroupOwnerRWRA()
            throws Exception {
        applySettingsToSetForImage("rwra--", GROUP_OWNER);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWRA--</code> group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForImageByAdminRWRA() throws Exception {
        applySettingsToSetForImage("rwra--", ADMIN);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWRA--</code> group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForImageByMemberRWRA() throws Exception {
        applySettingsToSetForImage("rwra--", MEMBER);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWRW--</code> group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForImageByGroupOwnerRWRW()
            throws Exception {
        applySettingsToSetForImage("rwrw--", GROUP_OWNER);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWRW--</code> group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForImageByAdminRWRW() throws Exception {
        applySettingsToSetForImage("rwrw--", ADMIN);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWRA--</code> group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForImageByMemberRWRW() throws Exception {
        applySettingsToSetForImage("rwrw--", MEMBER);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWR---</code> group. In that case the target image has been viewed
     * by another user to.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetTargetImageViewedByOtherByMemberRWR()
            throws Exception {
        applySettingsToSetTargetImageViewedByOther("rwr---", MEMBER);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWR---</code> group. In that case the target image has been viewed
     * by another user to.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetTargetImageViewedByOtherByGroupOwnerRWR()
            throws Exception {
        applySettingsToSetTargetImageViewedByOther("rwr---", GROUP_OWNER);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWR---</code> group. In that case the target image has been viewed
     * by another user to.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetTargetImageViewedByOtherByAdminRWR()
            throws Exception {
        applySettingsToSetTargetImageViewedByOther("rwr---", ADMIN);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWR---</code> group. In that case the target image has been viewed
     * by another user to.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetTargetImageViewedByOtherByMemberRWRA()
            throws Exception {
        applySettingsToSetTargetImageViewedByOther("rwra--", MEMBER);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWR---</code> group. In that case the target image has been viewed
     * by another user to.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetTargetImageViewedByOtherByGroupOwnerRWRA()
            throws Exception {
        applySettingsToSetTargetImageViewedByOther("rwra--", GROUP_OWNER);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWR---</code> group. In that case the target image has been viewed
     * by another user to.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetTargetImageViewedByOtherByAdminRWRA()
            throws Exception {
        applySettingsToSetTargetImageViewedByOther("rwra--", ADMIN);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWR---</code> group. In that case the target image has been viewed
     * by another user to.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetTargetImageViewedByOtherByMemberRWRW()
            throws Exception {
        applySettingsToSetTargetImageViewedByOther("rwrw--", MEMBER);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWR---</code> group. In that case the target image has been viewed
     * by another user to.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetTargetImageViewedByOtherByGroupOwnerRWRW()
            throws Exception {
        applySettingsToSetTargetImageViewedByOther("rwrw--", GROUP_OWNER);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWR---</code> group. In that case the target image has been viewed
     * by another user to.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetTargetImageViewedByOtherByAdminRWRW()
            throws Exception {
        applySettingsToSetTargetImageViewedByOther("rwrw--", ADMIN);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWR---</code> group. In that case the target image has been viewed
     * by another user to.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetSourcetImageViewedByOtherByAdminRWR()
            throws Exception {
        applySettingsToSetSourcetImageViewedByOther("rwr---", ADMIN);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWR---</code> group. In that case the target image has been viewed
     * by another user to.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetSourcetImageViewedByOtherByGroupRWR()
            throws Exception {
        applySettingsToSetSourcetImageViewedByOther("rwr---", GROUP_OWNER);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWR---</code> group. In that case the target image has been viewed
     * by another user to.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetSourcetImageViewedByOtherByMemberRWR()
            throws Exception {
        applySettingsToSetSourcetImageViewedByOther("rwr---", MEMBER);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWR---</code> group. In that case the target image has been viewed
     * by another user to.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetSourcetImageViewedByOtherByAdminRWRA()
            throws Exception {
        applySettingsToSetSourcetImageViewedByOther("rwra--", ADMIN);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWR---</code> group. In that case the target image has been viewed
     * by another user to.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetSourcetImageViewedByOtherByGroupRWRA()
            throws Exception {
        applySettingsToSetSourcetImageViewedByOther("rwra--", GROUP_OWNER);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWR---</code> group. In that case the target image has been viewed
     * by another user to.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetSourcetImageViewedByOtherByMemberRWRA()
            throws Exception {
        applySettingsToSetSourcetImageViewedByOther("rwra--", MEMBER);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWR---</code> group. In that case the target image has been viewed
     * by another user to.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetSourcetImageViewedByOtherByAdminRWRW()
            throws Exception {
        applySettingsToSetSourcetImageViewedByOther("rwrw--", ADMIN);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWR---</code> group. In that case the target image has been viewed
     * by another user to.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetSourcetImageViewedByOtherByGroupRWRW()
            throws Exception {
        applySettingsToSetSourcetImageViewedByOther("rwrw--", GROUP_OWNER);
    }

    /**
     * Tests to apply the rendering settings to a collection of images in a
     * <code>RWR---</code> group. In that case the target image has been viewed
     * by another user to.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetSourcetImageViewedByOtherByMemberRWRW()
            throws Exception {
        applySettingsToSetSourcetImageViewedByOther("rwrw--", MEMBER);
    }

    /**
     * Tests to apply the rendering settings to a collection of images The group
     * is initially <code>RWRW--</code> and then <code>RWR---</code>.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForImageGroupDowngradeRWRWToRWR()
            throws Exception {
        applySettingsToSetForImageGroupDowngrade("rwrw--", "rwr---");
    }

    /**
     * Tests to apply the rendering settings to a collection of images The group
     * is initially <code>RWRW--</code> and then <code>RWR---</code>.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForImageGroupDowngradeRWRWToRWRA()
            throws Exception {
        applySettingsToSetForImageGroupDowngrade("rwrw--", "rwra--");
    }

    /**
     * Tests to apply the rendering settings to a collection of images The group
     * is initially <code>RWRA--</code> and then <code>RWR---</code>.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForImageGroupDowngradeRWRAToRWR()
            throws Exception {
        applySettingsToSetForImageGroupDowngrade("rwra--", "rwr---");
    }

    /**
     * Tests to apply the rendering settings to a collection of images The group
     * is initially <code>RWR---</code> and then <code>RW----</code>.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForImageGroupDowngradeRWRToRW()
            throws Exception {
        applySettingsToSetForImageGroupDowngrade("rwr---", "rw----");
    }

    /**
     * Tests to apply the rendering settings to a collection of images The group
     * is initially <code>RWRA--</code> and then <code>RW----</code>.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForImageGroupDowngradeRWRAToRW()
            throws Exception {
        applySettingsToSetForImageGroupDowngrade("rwra--", "rw----");
    }

    /**
     * Tests to apply the rendering settings to a collection of images The group
     * is initially <code>RWRW--</code> and then <code>RW----</code>.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForImageGroupDowngradeRWRWToRW()
            throws Exception {
        applySettingsToSetForImageGroupDowngrade("rwrw--", "rw----");
    }

    /**
     * Tests to apply the rendering settings to an image w/o previous rendering
     * settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetTargetImageNoSettingsByGroupOwnerRW()
            throws Exception {
        applySettingsToSetTargetImageNoSettings("rw----", GROUP_OWNER);
    }

    /**
     * Tests to apply the rendering settings to an image w/o previous rendering
     * settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetTargetImageNoSettingsByAdminRW()
            throws Exception {
        applySettingsToSetTargetImageNoSettings("rw----", ADMIN);
    }

    /**
     * Tests to apply the rendering settings to an image w/o previous rendering
     * settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetTargetImageNoSettingsByMemberRW()
            throws Exception {
        applySettingsToSetTargetImageNoSettings("rw----", MEMBER);
    }

    /**
     * Tests to apply the rendering settings to an image w/o previous rendering
     * settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetTargetImageNoSettingsByGroupOwnerRWR()
            throws Exception {
        applySettingsToSetTargetImageNoSettings("rwr---", GROUP_OWNER);
    }

    /**
     * Tests to apply the rendering settings to an image w/o previous rendering
     * settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetTargetImageNoSettingsByAdminRWR()
            throws Exception {
        applySettingsToSetTargetImageNoSettings("rwr---", ADMIN);
    }

    /**
     * Tests to apply the rendering settings to an image w/o previous rendering
     * settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetTargetImageNoSettingsByMemberRWR()
            throws Exception {
        applySettingsToSetTargetImageNoSettings("rwr---", MEMBER);
    }

    /**
     * Tests to apply the rendering settings to an image w/o previous rendering
     * settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetTargetImageNoSettingsByGroupOwnerRWRA()
            throws Exception {
        applySettingsToSetTargetImageNoSettings("rwra--", GROUP_OWNER);
    }

    /**
     * Tests to apply the rendering settings to an image w/o previous rendering
     * settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetTargetImageNoSettingsByAdminRWRA()
            throws Exception {
        applySettingsToSetTargetImageNoSettings("rwra--", ADMIN);
    }

    /**
     * Tests to apply the rendering settings to an image w/o previous rendering
     * settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetTargetImageNoSettingsByMemberRWRA()
            throws Exception {
        applySettingsToSetTargetImageNoSettings("rwra--", MEMBER);
    }

    /**
     * Tests to apply the rendering settings to an image w/o previous rendering
     * settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetTargetImageNoSettingsByGroupOwnerRWRW()
            throws Exception {
        applySettingsToSetTargetImageNoSettings("rwrw--", GROUP_OWNER);
    }

    /**
     * Tests to apply the rendering settings to an image w/o previous rendering
     * settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetTargetImageNoSettingsByAdminRWRW()
            throws Exception {
        applySettingsToSetTargetImageNoSettings("rwrw--", ADMIN);
    }

    /**
     * Tests to apply the rendering settings to an image w/o previous rendering
     * settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetTargetImageNoSettingsByMemberRWRW()
            throws Exception {
        applySettingsToSetTargetImageNoSettings("rwrw--", MEMBER);
    }

    /**
     * Tests to apply the rendering settings to an image w/o previous rendering
     * settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetTargetImageNoSettingsAndBinary()
            throws Exception {
        EventContext ctx = newUserAndGroup("rw----");
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = createBinaryImage();
        Image image2 = mmFactory.createImage();
        image2 = (Image) iUpdate.saveAndReturnObject(image);

        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        List<Long> ids = new ArrayList<Long>();
        ids.add(image.getId().getValue());
        ids.add(image2.getId().getValue());
        prx.resetDefaultsInSet(Image.class.getName(), ids);

        // method already tested
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        long pix2 = image2.getPrimaryPixels().getId().getValue();
        ChannelBinding cb = def.getChannelBinding(0);
        boolean b = cb.getActive().getValue();
        cb.setActive(omero.rtypes.rbool(!b));
        def = (RenderingDef) iUpdate.saveAndReturnObject(def);

        ids.clear();
        ids.add(image2.getId().getValue());
        // apply the settings of image1 to image2 and 3
        prx.applySettingsToSet(id, Image.class.getName(), ids);
        RenderingDef def2 = factory.getPixelsService()
                .retrieveRndSettings(pix2);
        cb = def2.getChannelBinding(0);
        Assert.assertEquals(!b, cb.getActive().getValue());
    }

    /**
     * Tests to apply the rendering settings to a collection of images.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForImageModifyIntensity()
            throws Exception {
        EventContext ctx = newUserAndGroup("rw----");
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = createBinaryImage();
        Image image2 = createBinaryImage();

        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        List<Long> ids = new ArrayList<Long>();
        ids.add(image.getId().getValue());
        ids.add(image2.getId().getValue());
        prx.setOriginalSettingsInSet(Image.class.getName(), ids);

        // method already tested
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        long pix2 = image2.getPrimaryPixels().getId().getValue();
        ChannelBinding cb = def.getChannelBinding(0);
        boolean b = cb.getActive().getValue();
        cb.setActive(omero.rtypes.rbool(!b));
        cb.setInputStart(omero.rtypes.rdouble(cb.getInputEnd().getValue() - 1));
        def = (RenderingDef) iUpdate.saveAndReturnObject(def);

        ids.clear();
        ids.add(image2.getId().getValue());
        // apply the settings of image1 to image2 and 3
        prx.applySettingsToSet(id, Image.class.getName(), ids);
        RenderingDef def2 = factory.getPixelsService()
                .retrieveRndSettings(pix2);
        cb = def2.getChannelBinding(0);
        Assert.assertEquals(!b, cb.getActive().getValue());
    }

    // Test save rendering settings.
    /**
     * Tests to apply the rendering settings to an image w/o previous rendering
     * settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSaveSettingsImageOtherUserByGroupOwnerRWRW()
            throws Exception {
        saveSettingsOtherUserImage("rwrw--", GROUP_OWNER);
    }

    /**
     * Tests to apply the rendering settings to an image w/o previous rendering
     * settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSaveSettingsImageOtherUserByAdminRWRW() throws Exception {
        saveSettingsOtherUserImage("rwrw--", ADMIN);
    }

    /**
     * Tests to apply the rendering settings to an image w/o previous rendering
     * settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSaveSettingsImageOtherUserByMemberRWRW() throws Exception {
        saveSettingsOtherUserImage("rwrw--", MEMBER);
    }

    /**
     * Tests to apply the rendering settings to an image w/o previous rendering
     * settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSaveSettingsImageOtherUserByGroupOwnerRWRA()
            throws Exception {
        saveSettingsOtherUserImage("rwra--", GROUP_OWNER);
    }

    /**
     * Tests to apply the rendering settings to an image w/o previous rendering
     * settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSaveSettingsImageOtherUserByAdminRWRA() throws Exception {
        saveSettingsOtherUserImage("rwra--", ADMIN);
    }

    /**
     * Tests to apply the rendering settings to an image w/o previous rendering
     * settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSaveSettingsImageOtherUserByMemberRWRA() throws Exception {
        saveSettingsOtherUserImage("rwra--", MEMBER);
    }

    /**
     * Tests to apply the rendering settings to an image w/o previous rendering
     * settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSaveSettingsImageOtherUserByGroupOwnerRWR()
            throws Exception {
        saveSettingsOtherUserImage("rwr---", GROUP_OWNER);
    }

    /**
     * Tests to apply the rendering settings to an image w/o previous rendering
     * settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSaveSettingsImageOtherUserByAdminRWR() throws Exception {
        saveSettingsOtherUserImage("rwr---", ADMIN);
    }

    /**
     * Tests to set the default rendering settings for a set.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultByOwnerInSetByGroupOwnerRWR() throws Exception {
        resetDefaultByOwnerInSetFor("rwr---", GROUP_OWNER);
    }

    /**
     * Tests to set the default rendering settings for a set.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultByOwnerInSetByAdminRWR() throws Exception {
        resetDefaultByOwnerInSetFor("rwr---", ADMIN);
    }

    /**
     * Tests to set the default rendering settings for a set.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultByOwnerInSetByGroupOwnerRWRA() throws Exception {
        resetDefaultByOwnerInSetFor("rwr---", GROUP_OWNER);
    }

    /**
     * Tests to set the default rendering settings for a set.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultByOwnerInSetByAdminRWRA() throws Exception {
        resetDefaultByOwnerInSetFor("rwra--", ADMIN);
    }

    /**
     * Tests to set the default rendering settings for a set.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultByOwnerInSetRWRA() throws Exception {
        resetDefaultByOwnerInSetFor("rwra--", MEMBER);
    }

    /**
     * Tests to set the default rendering settings for a set.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultByOwnerInSetByGroupOwnerRWRW() throws Exception {
        resetDefaultByOwnerInSetFor("rwrw--", GROUP_OWNER);
    }

    /**
     * Tests to set the default rendering settings for a set.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultByOwnerInSetByAdminRWRW() throws Exception {
        resetDefaultByOwnerInSetFor("rwrw--", ADMIN);
    }

    /**
     * Tests to set the default rendering settings for a set.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultByOwnerInSetRWRW() throws Exception {
        resetDefaultByOwnerInSetFor("rwrw--", MEMBER);
    }
}
