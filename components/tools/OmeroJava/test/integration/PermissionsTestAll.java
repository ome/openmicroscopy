/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package integration;

import static org.testng.AssertJUnit.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import omero.RString;
import omero.api.IAdminPrx;
import omero.api.IContainerPrx;
import omero.api.ServiceFactoryPrx;
import omero.cmd.Chgrp2;
import omero.cmd.CmdCallbackI;
import omero.cmd.DoAll;
import omero.cmd.DoAllRsp;
import omero.cmd.ERR;
import omero.cmd.HandlePrx;
import omero.cmd.OK;
import omero.cmd.Request;
import omero.cmd.Response;
import omero.gateway.util.Requests;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImageI;
import omero.model.OriginalFile;
import omero.model.Permissions;
import omero.model.PermissionsI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.TermAnnotation;
import omero.model.TermAnnotationI;
import omero.sys.ParametersI;
import omero.sys.Roles;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import pojos.GroupData;
import pojos.PermissionData;

import com.google.common.collect.ImmutableMap;

/**
 * 
 *
 * @author Balaji Ramalingam &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:b.ramalingam@dundee.ac.uk">b.ramalingam@dundee.ac.uk</a>
 * @since 5.1
 */
public class PermissionsTestAll extends AbstractServerTest {

    private final String uuid = UUID.randomUUID().toString();

    private final String[] testGroupTypes = { "rwra--", "rw----", "rwr---",
            "rwrw--" };

    private final List<String> subsetOfTestGroups = Arrays.asList(
            testGroupTypes[0], testGroupTypes[3]);

    private final String[] testUserNames = { "notOwner-notAdmin-1" + uuid,
            "notOwner-notAdmin2" + uuid, "owner" + uuid, "admin" + uuid,
            "notOwner-notAdmin-ra" + uuid, "notOwner-notAdmin-p" + uuid,
            "notOwner-notAdmin-ro" + uuid, "notOwner-notAdmin-rw" + uuid };

    private final Map<String, String> testGroupTypeNameMap = ImmutableMap.of(
            "rwra--", "Read-Annotate-" + uuid + "-",
            "rw----", "Private-" + uuid + "-",
            "rwr---", "Read-Only-" + uuid + "-",
            "rwrw--", "Read-Write-" + uuid + "-"
            );

    /** The security roles for that server. **/
    private Roles securityRoles;

    /** The password used for user. **/
    private final static String PASSWORD = "ome";

    @Override
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();
        setupGroups();
        setupTestImages();
        annotateAllImages();
    }

    /**
     * Sets up the groups.
     * @throws Exception Thrown if an error occurred.
     */
    void setupGroups() throws Exception {
        omero.client client = newRootOmeroClient();
        client.enableKeepAlive(60);
        factory = client.getSession();
        IAdminPrx svc = factory.getAdminService();

        securityRoles = svc.getSecurityRoles();
        List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
        List<ExperimenterGroup> groupCopies = new ArrayList<ExperimenterGroup>();

        // Create two copies of every group type mentioned above
        for (int i = 1; i <= 2; i++) {
            for (Entry<String, String> groupTypeName : testGroupTypeNameMap
                    .entrySet()) {
                String groupName = groupTypeName.getValue()
                        + Integer.toString(i);

                ExperimenterGroup group = new ExperimenterGroupI();
                group.setName(omero.rtypes.rstring(groupName));
                group.setLdap(omero.rtypes.rbool(false));
                final Permissions perms = new PermissionsI(
                        groupTypeName.getKey());
                group.getDetails().setPermissions(perms);

                group = new ExperimenterGroupI(svc.createGroup(group), false);
                groups.add(group);

                if (i == 1) {
                    groupCopies.add(group);
                }
            }
        }

        RString omeroPassword = omero.rtypes.rstring(PASSWORD);
        String admin = testUserNames[3];
        String owner = testUserNames[2];

        Roles roles = factory.getAdminService().getSecurityRoles();
        ExperimenterGroup userGroup = new ExperimenterGroupI(roles.userGroupId,
                false);
        ExperimenterGroup systemGroup = new ExperimenterGroupI(
                roles.systemGroupId, false);

        int cntr = 0;
        ExperimenterGroup defaultGroup;
        List<ExperimenterGroup> targetGroups;
        for (int i = 0; i < testUserNames.length; i++) {
            targetGroups = new ArrayList<ExperimenterGroup>();
            targetGroups.add(userGroup);
            String username = testUserNames[i];
            Experimenter experimenter = createExperimenterI(username,
                    username, username);

            // Add admin user to system group
            if (username.equalsIgnoreCase(admin)) {
                defaultGroup = systemGroup;
                targetGroups.addAll(groups);
            } else if (i <= 3) {
                // Add the first 4 users to all groups
                defaultGroup = groups.get(0);
                targetGroups.addAll(groups);
            } else {
                // Add the last 4 users to one group alone
                defaultGroup = groups.get(cntr);
                targetGroups.add(groupCopies.get(cntr));
                cntr++;
            }
            factory.getAdminService().createExperimenterWithPassword(
                    experimenter, omeroPassword, defaultGroup, targetGroups);

            // Make user : owner + uuid , owner of all the groups
            if (username.equalsIgnoreCase(owner)) {
                Experimenter user = svc.lookupExperimenter(testUserNames[i]);
                for (int l = 0; l < targetGroups.size(); l++) {
                    svc.setGroupOwner(new ExperimenterGroupI(targetGroups
                            .get(l).getId(), false),
                            new ExperimenterI(user.getId(), false));
                }
            }
        }

        client.closeSession();
    }

    /**
     * Creates 11 images for each user.
     *
     * @throws Exception
     *             Thrown if an error occurred during creation.
     */
    void setupTestImages() throws Exception {
        // Iterate through all the users
        for (int i = 0; i < testUserNames.length; i++) {
            omero.client client = new omero.client();
            ServiceFactoryPrx session = client
                    .createSession(testUserNames[i], PASSWORD);
            client.enableKeepAlive(60);

            IAdminPrx adminSvc = session.getAdminService();
            Experimenter user = adminSvc.lookupExperimenter(testUserNames[i]);
            List<Long> groupIds = adminSvc.getMemberOfGroupIds(user);

            // Switch group context for the user (Iterate through all the
            // groups, the user is part of)
            for (int j = 0; j < groupIds.size(); j++) {
                long groupId = groupIds.get(j);
                if (!isSecuritySystemGroup(groupId)) {
                    ExperimenterGroupI group = new ExperimenterGroupI(groupId,
                            false);
                    session.setSecurityContext(group);
                    iUpdate = session.getUpdateService();
                    mmFactory = new ModelMockFactory(session.getPixelsService());

                    // Create new Image Objects(with pixels) and attach it to
                    // the session
                    for (int k = 0; k <= groupIds.size(); k++) {
                        Image img = (Image) iUpdate
                                .saveAndReturnObject(mmFactory.simpleImage());
                        assertNotNull(img);
                    }
                }
            }

            client.closeSession();
        }
    }

    /**
     * Annotates all the images created.
     * @throws Exception Thrown if an error occurred.
     */
    void annotateAllImages() throws Exception {
        for (int i = 0; i < testUserNames.length; i++) {
            omero.client client = new omero.client();
            ServiceFactoryPrx session = client
                    .createSession(testUserNames[i], PASSWORD);
            String commentText = testUserNames[i] + "_comment";
            String tagText = testUserNames[i] + "_tag";

            IAdminPrx adminSvc = session.getAdminService();
            Experimenter user = adminSvc.lookupExperimenter(testUserNames[i]);
            List<Long> userGroups = adminSvc.getMemberOfGroupIds(user);
            Long userId = user.getId().getValue();

            for (int j = 0; j < userGroups.size(); j++) {
                if (!isSecuritySystemGroup(userGroups.get(j))) {
                    ExperimenterGroup group = adminSvc.getGroup(userGroups.get(j));
                    String permsAsString = permissionsAsString(group);

                    session.setSecurityContext(new ExperimenterGroupI(group.getId()
                            .getValue(), false));
                    iUpdate = session.getUpdateService();
                    mmFactory = new ModelMockFactory(session.getPixelsService());

                    List<Long> annotationIds = new ArrayList<Long>();

                    // Create Tags
                    TagAnnotation tag = new TagAnnotationI();
                    tag.setTextValue(omero.rtypes.rstring(tagText));
                    tag = (TagAnnotation) iUpdate.saveAndReturnObject(tag);
                    annotationIds.add(tag.getId().getValue());

                    // Create Comments
                    TermAnnotation comment = new TermAnnotationI();
                    comment.setTermValue(omero.rtypes.rstring(commentText));
                    comment = (TermAnnotation) iUpdate.saveAndReturnObject(comment);
                    annotationIds.add(comment.getId().getValue());

                    // Create File for FileAnnotation
                    OriginalFile originalFile = (OriginalFile) iUpdate
                            .saveAndReturnObject(mmFactory.createOriginalFile());
                    assertNotNull(originalFile);
                    FileAnnotation file = new FileAnnotationI();
                    file.setFile(originalFile);
                    file = (FileAnnotation) iUpdate.saveAndReturnObject(file);
                    annotationIds.add(file.getId().getValue());
                    ParametersI params = new omero.sys.ParametersI();
                    params.exp(user.getId());

                    IContainerPrx proxy = session.getContainerService();
                    List<Image> imageList = proxy.getUserImages(params);
                    for (int k = 0; k < imageList.size(); k++) {
                        Image img = imageList.get(k);
                        Long imageId = img.getId().getValue();
                        Long ownerId = img.getDetails().getOwner().getId().getValue();
                        if (ownerId == userId || subsetOfTestGroups.contains(permsAsString)) {
                            List<IObject> links = new ArrayList<IObject>();
                            // Create Links for Tags
                            ImageAnnotationLink link = new ImageAnnotationLinkI();
                            link.setChild(new TagAnnotationI(tag.getId().getValue(),
                                    false));
                            link.setParent(new ImageI(imageId, false));
                            links.add(link);

                            // Create Links for Comments
                            link = new ImageAnnotationLinkI();
                            link.setChild(new TermAnnotationI(comment.getId().getValue(),
                                    false));
                            link.setParent(new ImageI(imageId, false));
                            links.add(link);

                            // Create Links for Files
                            link = new ImageAnnotationLinkI();
                            link.setChild(new FileAnnotationI(file.getId().getValue(),
                                    false));
                            link.setParent(new ImageI(imageId, false));
                            links.add(link);
                            iUpdate.saveAndReturnArray(links);
                        }

                    }
                }
            }
            client.closeSession();
        }
    }

    @Override
    @AfterClass
    public void tearDown() throws Exception {

    }

    /**
     * Generates data for each user.
     * @return Object[][] data.
     */
    @DataProvider(name = "createData")
    public Object[][] createData() throws Exception {
        List<TestParam> testParams = new ArrayList<TestParam>();
        Object[][] data = null;
        for (int i = 0; i < testUserNames.length; i++) {
            omero.client client = new omero.client();
            ServiceFactoryPrx session = client.createSession(testUserNames[i],
                    PASSWORD);

            Experimenter experimenter = session.getAdminService()
                    .lookupExperimenter(testUserNames[i]);
            List<Long> groupIds = session.getAdminService()
                    .getMemberOfGroupIds(experimenter);
            int n = groupIds.size();

            for (int j = 0; j < n; j++) {
                Long userId = session.getAdminService()
                        .getEventContext().userId;
                ParametersI params = new omero.sys.ParametersI();
                params.exp(omero.rtypes.rlong(userId));
                long sourceGroup = groupIds.get(j);

                if (!isSecuritySystemGroup(sourceGroup)) {
                    ExperimenterGroupI group = new ExperimenterGroupI(
                            sourceGroup, false);
                    session.setSecurityContext(group);
                    IContainerPrx proxy = session.getContainerService();
                    List<Image> images = proxy.getUserImages(params);
                    Image img = null;

                    for (int k = 0; k < n; k++) {
                        Long targetGroup = groupIds.get(k);
                        if (!isSecuritySystemGroup(targetGroup)
                                && targetGroup != sourceGroup) {
                            img = images.get(k);
                            long imageId = img.getId().getValue();
                            final Chgrp2 dc = Requests.chgrp("Image", imageId, targetGroup);
                            testParams.add(new TestParam(dc, testUserNames[i],
                                    PASSWORD, sourceGroup));
                        }
                    }
                }
            }

            int index = 0;
            Iterator<TestParam> testParamsIter = testParams.iterator();
            data = new Object[testParams.size()][1];
            while (testParamsIter.hasNext()) {
                data[index][0] = testParamsIter.next();
                index++;
            }
        }

        return data;

    }

    /**
     * Tests the move of data.
     * @param param Hold information about the data to move.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(dataProvider = "createData")
    public void test(TestParam param) throws Exception {
        String username = param.getUser();
        String passwd = param.getPass();
        Long sourceGroup = param.getSrcID();

        // create session and switch context to source group
        omero.client client = new omero.client();
        ServiceFactoryPrx session = client.createSession(username, passwd);

        ExperimenterGroupI group = new ExperimenterGroupI(sourceGroup, false);
        session.setSecurityContext(group);
        DoAll all = new DoAll();
        List<Request> requests = new ArrayList<Request>();
        requests.add(param.getChgrp());
        all.requests = requests;
        HandlePrx handle = session.submit(all);

        long timeoutMove = scalingFactor * 1 * 100;

        CmdCallbackI cb = new CmdCallbackI(client, handle);
        cb.loop(10 * all.requests.size(), timeoutMove);
        Response response = cb.getResponse();
        Long userId = session.getAdminService().getEventContext().userId;
        long targetGroup = param.getChgrp().groupId;
        List<Long> targets = param.getChgrp().targetObjects.get(
                Image.class.getSimpleName());
        long imageId = targets.get(0);

        if (response == null) {
            String sourceGroupPerms = permissionsAsString(session
                    .getAdminService().getGroup(sourceGroup));
            String targetGroupPerms = permissionsAsString(session
                    .getAdminService().getGroup(targetGroup));

            Assert.fail("Failure : User id: " + userId + " (" + username + ")"
                    + " tried moving image " + imageId + " from " + sourceGroup
                    + "(" + sourceGroupPerms + ")" + " to " + targetGroup + "("
                    + targetGroupPerms + ") no response returned");
        } else if (response instanceof DoAllRsp) {
            List<Response> responses = ((DoAllRsp) response).responses;
            if (responses.size() == 1) {
                Response r = responses.get(0);
                Assert.assertTrue(r instanceof OK, "move OK");
            }
        } else if (response instanceof ERR) {
            String sourceGroupPerms = permissionsAsString(session
                    .getAdminService().getGroup(sourceGroup));
            String targetGroupPerms = permissionsAsString(session
                    .getAdminService().getGroup(targetGroup));

            ERR err = (ERR) response;
            Map<String, String> responseParameters = err.parameters;
            StringBuffer buf = new StringBuffer();
            for (Entry<String, String> responseParameter : responseParameters
                    .entrySet()) {
                buf.append(responseParameter.getKey() + " -> "
                        + responseParameter.getValue());
            }
            Assert.fail("Failure : User id: " + userId + " (" + username + ")"
                    + " tried moving image " + imageId + " from " + sourceGroup
                    + "(" + sourceGroupPerms + ")" + " to " + targetGroup + "("
                    + targetGroupPerms + ") "+buf.toString());
        }
        client.closeSession();
    }

    /**
     * Implemented as specified by {@link AdminService}.
     */
    private boolean isSecuritySystemGroup(long groupID) {
        if (securityRoles == null) {
            return false;
        }
        return (securityRoles.guestGroupId == groupID
                || securityRoles.systemGroupId == groupID
                || securityRoles.userGroupId == groupID);
    }

    /**
     * Extracts the permission of a group and presents it in string form.
     *
     * @param experimenterGroup
     *            the target group.
     * @return String the group permissions.
     */
    private String permissionsAsString(ExperimenterGroup experimenterGroup) {
        PermissionData perms = new PermissionData(experimenterGroup
                .getDetails().getPermissions());
        switch (perms.getPermissionsLevel()) {
            case GroupData.PERMISSIONS_GROUP_READ:
                return "rwr---";
            case GroupData.PERMISSIONS_GROUP_READ_LINK:
                return "rwra--";
            case GroupData.PERMISSIONS_GROUP_READ_WRITE:
                return "rwrw--";
            case GroupData.PERMISSIONS_PUBLIC_READ:
                return "rwrwr-";
            default:
                return "rw----";
        }
    }

    /**
     * Inner class hosting information about object to move.
     *
     */
    class TestParam {

        /** Hold information about the object to move.*/
        private Chgrp2 chgrp;

        /** The user to log as.*/
        private String user;

        /** The user's password.*/
        private String password;

        /** The identifier of the group to move the data from.*/
        private Long srcID;

        /**
         * Creates a new instance.
         *
         * @param chgrp Hold information about the object to move.
         * @param user The user to log as.
         * @param password The user's password.
         * @param srcID The identifier of the group to move the data from.
         */
        TestParam(Chgrp2 chgrp, String user, String password, Long srcID) {
            this.chgrp = chgrp;
            this.user = user;
            this.password = password;
            this.srcID = srcID;
        }

        /**
         * Returns the information object to move.
         *
         * @return See above.
         */
        Chgrp2 getChgrp() {
            return chgrp;
        }

        /**
         * Returns the user to log as.
         *
         * @return See above.
         */
        String getUser() {
            return user;
        }

        /**
         * Returns the user's password.
         *
         * @return See above.
         */
        String getPass() {
            return password;
        }

        /**
         * Returns the identifier of the group to move the data from.
         *
         * @return See above.
         */
        Long getSrcID() {
            return srcID;
        }
    }
}
