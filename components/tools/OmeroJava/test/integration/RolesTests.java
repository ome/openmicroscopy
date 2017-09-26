/*
 * Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package integration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

import com.google.common.collect.ImmutableList;

import ome.services.blitz.repo.path.FsFile;
import omero.RString;
import omero.SecurityViolation;
import omero.ServerError;
import omero.api.ServiceFactoryPrx;
import omero.gateway.util.Utils;
import omero.grid.ImportLocation;
import omero.model.AdminPrivilege;
import omero.model.AdminPrivilegeI;
import omero.model.Dataset;
import omero.model.Experimenter;
import omero.model.ExperimenterI;
import omero.model.FileAnnotation;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.OriginalFile;
import omero.model.Permissions;
import omero.model.PermissionsI;
import omero.model.Session;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.sys.EventContext;
import omero.sys.ParametersI;
import omero.sys.Principal;
import omero.util.TempFileManager;

/**
 * Helper methods class for supporting of "new role" workflow
 * and security tests.
 * @author p.walczysko@dundee.ac.uk
 * @since 5.4.0
 */
public class RolesTests extends AbstractServerImportTest {

    private static final TempFileManager TEMPORARY_FILE_MANAGER = new TempFileManager(
            "test-" + LightAdminRolesTest.class.getSimpleName());

    protected File fakeImageFile = null;

    /**
     * @return test cases for fileAnnotation workflow in testFileAttachmentNoSudo
     */
    @DataProvider(name = "fileAttachment privileges cases")
    public Object[][] provideFileAttachmentPrivilegesCases() {
        int index = 0;
        final int PERM_CHOWN = index++;
        final int PERM_WRITEOWNED = index++;
        final int PERM_WRITEFILE = index++;
        final int GROUP_PERMS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};
        final List<Object[]> testCases = new ArrayList<Object[]>();

            for (final boolean permChown : booleanCases) {
                for (final boolean permWriteOwned : booleanCases) {
                    for (final boolean permWriteFile : booleanCases) {
                        for (final String groupPerms : permsCases) {
                            final Object[] testCase = new Object[index];
                            testCase[PERM_CHOWN] = permChown;
                            testCase[PERM_WRITEOWNED] = permWriteOwned;
                            testCase[PERM_WRITEFILE] = permWriteFile;
                            testCase[GROUP_PERMS] = groupPerms;
                            //DEBUG if (permChown == true && permWriteOwned == true && permWriteFile == true && groupPerms.equals("rwr---"))
                            testCases.add(testCase);
                        }
                    }
                }
            }
        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return test cases for testCreateLinkImportSudo and testEdit
     */
    @DataProvider(name = "isSudoing and WriteOwned privileges cases")
    public Object[][] provideIsSudoingAndWriteOwned() {
        int index = 0;
        final int IS_SUDOING = index++;
        final int PERM_WRITEOWNED = index++;
        final int GROUP_PERMS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};
        final List<Object[]> testCases = new ArrayList<Object[]>();

            for (final boolean isSudoing : booleanCases) {
                for (final boolean permWriteOwned : booleanCases) {
                    for (final String groupPerms : permsCases) {
                        final Object[] testCase = new Object[index];
                        if (isSudoing && permWriteOwned)
                            /* not an interesting case */
                            continue;
                        testCase[IS_SUDOING] = isSudoing;
                        testCase[PERM_WRITEOWNED] = permWriteOwned;
                        testCase[GROUP_PERMS] = groupPerms;
                        // DEBUG if (isSudoing == true && permWriteOwned == true && groupPerms.equals("rwr---")))
                        testCases.add(testCase);
                    }
                }
            }
        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return provide WriteOwned, WriteFile, WriteManagedRepo and Chown cases
     * for testImporterAsNoSudoChownOnlyWorkflow
     */
    @DataProvider(name = "WriteOwned, WriteFile, WriteManagedRepo and Chown privileges cases")
    public Object[][] provideWriteOwnedWriteFileWriteManagedRepoAndChown() {
        int index = 0;
        final int PERM_WRITEOWNED = index++;
        final int PERM_WRITEFILE = index++;
        final int PERM_WRITEMANAGEDREPO = index++;
        final int PERM_CHOWN = index++;
        final int GROUP_PERMS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};
        final List<Object[]> testCases = new ArrayList<Object[]>();

            for (final boolean permWriteOwned : booleanCases) {
                for (final boolean permWriteFile : booleanCases) {
                    for (final boolean permWriteManagedRepo : booleanCases) {
                        for (final boolean permChown : booleanCases) {
                            for (final String groupPerms : permsCases) {
                                final Object[] testCase = new Object[index];
                                if (!permWriteOwned && !permWriteFile)
                                    /* not an interesting case */
                                    continue;
                                if (!permWriteOwned && !permWriteManagedRepo)
                                    /* not an interesting case */
                                    continue;
                                if (!permWriteOwned && !permWriteFile && !permWriteManagedRepo)
                                    /* not an interesting case */
                                    continue;
                                testCase[PERM_WRITEOWNED] = permWriteOwned;
                                testCase[PERM_WRITEFILE] = permWriteFile;
                                testCase[PERM_WRITEMANAGEDREPO] = permWriteManagedRepo;
                                testCase[PERM_CHOWN] = permChown;
                                testCase[GROUP_PERMS] = groupPerms;
                                // DEBUG if (permWriteOwned == true && permWriteFile == true && permWriteManagedRepo == true
                                // && permChown == true && groupPerms.equals("rwr---"))
                                testCases.add(testCase);
                            }
                        }
                    }
                }
            }
        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return WriteOwned and Chown test cases for
     * testLinkNoSudo and testROIAndRenderingSettingsNoSudo
     */
    @DataProvider(name = "WriteOwned and Chown privileges cases")
    public Object[][] provideWriteOwnedAndChown() {
        int index = 0;
        final int PERM_WRITEOWNED = index++;
        final int PERM_CHOWN = index++;
        final int GROUP_PERMS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};
        final List<Object[]> testCases = new ArrayList<Object[]>();

            for (final boolean permWriteOwned : booleanCases) {
                for (final boolean permChown : booleanCases) {
                    for (final String groupPerms : permsCases) {
                        final Object[] testCase = new Object[index];
                        if (!permWriteOwned && !permChown)
                            /* not an interesting case */
                            continue;
                        testCase[PERM_WRITEOWNED] = permWriteOwned;
                        testCase[PERM_CHOWN] = permChown;
                        testCase[GROUP_PERMS] = groupPerms;
                        // DEBUG if (permWriteOwned == true && permChown == true && groupPerms.equals("rwr---"))
                        testCases.add(testCase);
                    }
                }
            }
        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return WriteOwned and isAdmin test cases for
     * {@link #testLinkMemberOfGroupNoSudo}
     */
    @DataProvider(name = "WriteOwned and isAdmin cases")
    public Object[][] provideWriteOwnedAndIsAdmin() {
        int index = 0;
        final int PERM_WRITEOWNED = index++;
        final int IS_ADMIN = index++;
        final int GROUP_PERMS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};
        final List<Object[]> testCases = new ArrayList<Object[]>();

            for (final boolean permWriteOwned : booleanCases) {
                for (final boolean isAdmin : booleanCases) {
                    for (final String groupPerms : permsCases) {
                        final Object[] testCase = new Object[index];
                        if (!permWriteOwned && !isAdmin)
                            /* not an interesting case */
                            continue;
                        testCase[PERM_WRITEOWNED] = permWriteOwned;
                        testCase[IS_ADMIN] = isAdmin;
                        testCase[GROUP_PERMS] = groupPerms;
                        // DEBUG if (permWriteOwned == true && isAdmin == true && groupPerms.equals("rwr---"))
                        testCases.add(testCase);
                    }
                }
            }
        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return Chgrp and Chown test cases for testImporterAsNoSudoChgrpChownWorkflow
     */
    @DataProvider(name = "Chgrp and Chown privileges cases")
    public Object[][] provideChgrpAndChown() {
        int index = 0;
        final int PERM_CHGRP = index++;
        final int PERM_CHOWN = index++;
        final int GROUP_PERMS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};
        final List<Object[]> testCases = new ArrayList<Object[]>();

            for (final boolean permChgrp : booleanCases) {
                for (final boolean permChown : booleanCases) {
                    for (final String groupPerms : permsCases) {
                        final Object[] testCase = new Object[index];
                        /* No test cases are excluded here, because Chgrp
                         * and Chown are two separate steps which can work
                         * independently on each other and both are tested
                         * in the test.*/
                        testCase[PERM_CHGRP] = permChgrp;
                        testCase[PERM_CHOWN] = permChown;
                        testCase[GROUP_PERMS] = groupPerms;
                        // DEBUG if (permChgrp == true && permChown == true && groupPerms.equals("rwr---"))
                        testCases.add(testCase);
                    }
                }
            }
        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return createLightAdmin test cases for {@link #testModifyUserCreateLight}
     */
    @DataProvider(name = "createLightAdmin cases")
    public Object[][] provideCreateLightAdminCases() {
        int index = 0;
        final int PERM_MODIFYUSER = index++;
        final int LIGHT_ADMIN_TYPES = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"DataViewer", "Importer", "Analyst", "Organizer"};
        final List<Object[]> testCases = new ArrayList<Object[]>();

            for (final boolean permModifyUser : booleanCases) {
                for (final String lightAdminType : permsCases) {
                    final Object[] testCase = new Object[index];
                    testCase[PERM_MODIFYUSER] = permModifyUser;
                    testCase[LIGHT_ADMIN_TYPES] = lightAdminType;
                    // DEBUG if (permModifyUser == true && createdAdminType.equals("DataViewer"))
                    testCases.add(testCase);
                }
            }
        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return isSudoing and Chown test cases for testChown
     */
    @DataProvider(name = "isSudoing and Chown privileges cases")
    public Object[][] provideIsSudoingAndChown() {
        int index = 0;
        final int IS_SUDOING = index++;
        final int PERM_CHOWN = index++;
        final int GROUP_PERMS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};
        final List<Object[]> testCases = new ArrayList<Object[]>();

            for (final boolean isSudoing : booleanCases) {
                for (final boolean permChown : booleanCases) {
                    for (final String groupPerms : permsCases) {
                        final Object[] testCase = new Object[index];
                        /* No test cases are excluded here, because isSudoing
                         * is in a sense acting to annule Chown permission
                         * which is tested in the testChown and is an interesting case.*/
                        testCase[IS_SUDOING] = isSudoing;
                        testCase[PERM_CHOWN] = permChown;
                        testCase[GROUP_PERMS] = groupPerms;
                        // DEBUG  if (isSudoing == true && permChown == true && groupPerms.equals("rwr---"))
                        testCases.add(testCase);
                    }
                }
            }
        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return isPrivileged test cases. The isPrivileged parameter translates into one
     * tested privilege in particular tests (for example in testScriptUpload isPrivileged
     * means specifically WriteScriptRepo privilege).
     */
    @DataProvider(name = "isPrivileged cases")
    public Object[][] provideIsPrivilegedCases() {
        int index = 0;
        final int IS_PRIVILEGED = index++;
        final int GROUP_PERMS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};
        final List<Object[]> testCases = new ArrayList<Object[]>();

            for (final boolean isPrivileged : booleanCases) {
                for (final String groupPerms : permsCases) {
                    final Object[] testCase = new Object[index];
                    testCase[IS_PRIVILEGED] = isPrivileged;
                    testCase[GROUP_PERMS] = groupPerms;
                    // DEBUG if (isPrivileged == true && groupPerms.equals("rwr---"))
                    testCases.add(testCase);
                }
            }
        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return test cases for {@link #testDeleteGroupMemberNoSudo}
     */
    @DataProvider(name = "isAdmin and Delete cases")
    protected Object[][] provideIsAdminDeleteOwned() {
        int index = 0;
        final int IS_ADMIN = index++;
        final int PERM_DELETEOWNED = index++;
        final int GROUP_PERMS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};
        final List<Object[]> testCases = new ArrayList<Object[]>();

            for (final boolean isAdmin : booleanCases) {
                for (final boolean permDeleteOwned : booleanCases) {
                    for (final String groupPerms : permsCases) {
                        final Object[] testCase = new Object[index];
                        if (!isAdmin && permDeleteOwned)
                            /* not an interesting case */
                            continue;
                        testCase[IS_ADMIN] = isAdmin;
                        testCase[PERM_DELETEOWNED] = permDeleteOwned;
                        testCase[GROUP_PERMS] = groupPerms;
                        // DEBUG if (isAdmin == true && permDeleteOwned == true && groupPerms.equals("rwr---"))
                        testCases.add(testCase);
                    }
                }
            }
        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return test cases for testDelete
     */
    @DataProvider(name = "isSudoing and Delete privileges cases")
    protected Object[][] provideIsSudoingAndDeleteOwned() {
        int index = 0;
        final int IS_SUDOING = index++;
        final int PERM_DELETEOWNED = index++;
        final int GROUP_PERMS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};
        final List<Object[]> testCases = new ArrayList<Object[]>();

            for (final boolean isSudoing : booleanCases) {
                for (final boolean permDeleteOwned : booleanCases) {
                    for (final String groupPerms : permsCases) {
                        final Object[] testCase = new Object[index];
                        if (isSudoing && permDeleteOwned)
                            /* not an interesting case */
                            continue;
                        testCase[IS_SUDOING] = isSudoing;
                        testCase[PERM_DELETEOWNED] = permDeleteOwned;
                        testCase[GROUP_PERMS] = groupPerms;
                        // DEBUG if (isSudoing == true && permDeleteOwned == true && groupPerms.equals("rwr---"))
                        testCases.add(testCase);
                    }
                }
            }
        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return test cases for testChgrp and testChgrpNonMember
     */
    @DataProvider(name = "isSudoing and Chgrp privileges cases")
    public Object[][] provideIsSudoingAndChgrpOwned() {
        int index = 0;
        final int IS_SUDOING = index++;
        final int PERM_CHGRP = index++;
        final int GROUP_PERMS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};
        final List<Object[]> testCases = new ArrayList<Object[]>();

            for (final boolean isSudoing : booleanCases) {
                for (final boolean permChgrp : booleanCases) {
                    for (final String groupPerms : permsCases) {
                        final Object[] testCase = new Object[index];
                        /* No test cases are excluded here, because isSudoing
                         * is in a sense acting to annule Chgrp permission
                         * which is tested in the testChgrp and is an interesting case.*/
                        testCase[IS_SUDOING] = isSudoing;
                        testCase[PERM_CHGRP] = permChgrp;
                        testCase[GROUP_PERMS] = groupPerms;
                        // DEBUG  if (isSudoing == true && permChgrp == true && groupPerms.equals("rwr---"))
                        testCases.add(testCase);
                    }
                }
            }
        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * Create a fake image file for use in import tests.
     * @throws IOException unexpected
     */
    @BeforeClass
    protected void createFakeImageFile() throws IOException {
        final File temporaryDirectory = TEMPORARY_FILE_MANAGER.createPath("images", null, true);
        fakeImageFile = new File(temporaryDirectory, "image.fake");
        fakeImageFile.createNewFile();
    }

    /* These permissions do not permit anything.*/
    @SuppressWarnings("serial")
    private static final Permissions NO_PERMISSIONS = new PermissionsI("------") {
        @Override
        public boolean isDisallow(int restriction, Ice.Current c) {
            return true;
        }
    };

    /**
     * Get the current permissions for the given object.
     * @param object a model object previously retrieved from the server
     * @return the permissions for the object in the current context
     * @throws ServerError if the query caused a server error
     * (except for security violations, returns NO_PERMISSIONS)
     */
    protected Permissions getCurrentPermissions(IObject object) throws ServerError {
        final String objectClass = object.getClass().getSuperclass().getSimpleName();
        final long objectId = object.getId().getValue();
        try {
            final IObject objectRetrieved;
            if (objectClass.endsWith("Link")) {
                objectRetrieved = iQuery.findByQuery("FROM " + objectClass + " link JOIN FETCH link.child WHERE link.id = :id",
                        new ParametersI().addId(objectId), ALL_GROUPS_CONTEXT);
            } else {
                objectRetrieved = iQuery.get(objectClass, objectId, ALL_GROUPS_CONTEXT);
            }
            if (objectRetrieved == null) {
                return NO_PERMISSIONS;
            }
            return objectRetrieved.getDetails().getPermissions();
        } catch (SecurityViolation sv) {
            return NO_PERMISSIONS;
        }
    }

    /**
     * Import an image with original file into a given dataset.
     * @param dataset dataset to which to import the image if not null
     * @return the original file and the imported image
     * @throws Exception if the import fails
     */
    protected List<IObject> importImageWithOriginalFile(Dataset dataset) throws Exception {
        final String omeroGroup = client.getImplicitContext().get(omero.constants.GROUP.value);
        final long currentGroupId = StringUtils.isBlank(omeroGroup) ? iAdmin.getEventContext().groupId : Long.parseLong(omeroGroup);
        final ImportLocation importLocation = importFileset(Collections.singletonList(fakeImageFile.getPath()), 1, dataset);
        final RString imagePath = omero.rtypes.rstring(importLocation.sharedPath + FsFile.separatorChar);
        final RString imageName = omero.rtypes.rstring(fakeImageFile.getName());
        String hql = "FROM OriginalFile o WHERE o.path = :path AND o.name = :name";
        final ParametersI params = new ParametersI().add("path", imagePath).add("name", imageName);
        if (currentGroupId >= 0) {
            hql += " AND o.details.group.id = :group_id";
            params.addLong("group_id", currentGroupId);
        }
        final OriginalFile remoteFile = (OriginalFile) iQuery.findByQuery(hql, params);
        final Image image = (Image) iQuery.findByQuery(
                "FROM Image WHERE fileset IN "
                + "(SELECT fileset FROM FilesetEntry WHERE originalFile.id = :id)",
                new ParametersI().addId(remoteFile.getId()));
        return ImmutableList.of(remoteFile, image);
    }

    /**
     * Annotate image with tag and file annotation and return the annotation objects
     * including the original file of the file annotation and the links
     * @param image the image to be annotated
     * @return the list of the tag, original file of the file annotation, file annotation
     * and the links between the tag and image and the file annotation and image
     * @throws Exception
     */
    protected List<IObject> annotateImageWithTagAndFile(Image image) throws Exception {
        TagAnnotation tagAnnotation = new TagAnnotationI();
        tagAnnotation = (TagAnnotation) iUpdate.saveAndReturnObject(tagAnnotation);
        final ImageAnnotationLink tagAnnotationLink = linkParentToChild(image, tagAnnotation);
        /* add a file attachment with original file to the imported image.*/
        final ImageAnnotationLink fileAnnotationLink = linkParentToChild(image, mmFactory.createFileAnnotation());
        /* link was saved in previous step with the whole graph, including fileAnnotation and original file */
        final FileAnnotation fileAnnotation = (FileAnnotation) fileAnnotationLink.getChild();
        final OriginalFile annotOriginalFile = fileAnnotation.getFile();
        /* make a list of annotation objects in order to simplify checking of owner and group */
        List<IObject> annotOriginalFileAnnotationTagAndLinks = new ArrayList<IObject>();
        annotOriginalFileAnnotationTagAndLinks.addAll(Arrays.asList(annotOriginalFile, fileAnnotation, tagAnnotation,
                tagAnnotationLink, fileAnnotationLink));
        return annotOriginalFileAnnotationTagAndLinks;
    }

    /**
     * Sudo to the given user.
     * @param targetName the name of a user
     * @return context for a session owned by the given user
     * @throws Exception if the sudo could not be performed
     */
    protected EventContext sudo(String targetName) throws Exception {
        final Principal principal = new Principal();
        principal.name = targetName;
        final Session session = factory.getSessionService().createSessionWithTimeout(principal, 100 * 1000);
        final omero.client client = newOmeroClient();
        final String sessionUUID = session.getUuid().getValue();
        client.createSession(sessionUUID, sessionUUID);
        return init(client);
    }

    /**
     * Creates a new administrator without any privileges
     * and create a new {@link omero.client}.
     */
    protected EventContext logNewAdminWithoutPrivileges() throws Exception {
        return loginNewAdmin(true, Collections.<String>emptyList());
    }

    /**
     * Create a light administrator, with a specific privilege, and log in as them.
     * All the other privileges will be set to false.
     * @param isAdmin if the user should be a member of the <tt>system</tt> group
     * @param permission the privilege that the user should have, or {@code null} if they should have no privileges
     * @return the new user's context
     * @throws Exception if the light administrator could not be created
     */
    protected EventContext loginNewAdmin(boolean isAdmin, String permission) throws Exception {
        return loginNewAdmin(isAdmin, Arrays.asList(permission));
    }

    /**
     * Create a light administrator, with a specific list of privileges, and log in as them.
     * All the other privileges will be set to False.
     * @param isAdmin if the user should be a member of the <tt>system</tt> group
     * @param permissions the privileges that the user should have, or {@code null} if they should have no privileges
     * @return the new user's context
     * @throws Exception if the light administrator could not be created
     */
    protected EventContext loginNewAdmin(boolean isAdmin, List <String> permissions) throws Exception {
        final EventContext ctx = isAdmin ? newUserInGroup(iAdmin.lookupGroup(roles.systemGroupName), false) : newUserAndGroup("rwr-r-");
        final ServiceFactoryPrx rootSession = root.getSession();
        Experimenter user = new ExperimenterI(ctx.userId, false);
        user = (Experimenter) rootSession.getQueryService().get("Experimenter", ctx.userId);
        final List<AdminPrivilege> privileges = Utils.toEnum(AdminPrivilege.class, AdminPrivilegeI.class, permissions);
        rootSession.getAdminService().setAdminPrivileges(user, Collections.<AdminPrivilege>emptyList());
        rootSession.getAdminService().setAdminPrivileges(user, privileges);
        /* avoid old session as privileges are briefly cached */
        loginUser(ctx);
        return ctx;
    }

    /**
     * Sudo to the given user.
     * @param target a user
     * @throws Exception if the sudo could not be performed
     */
    protected void sudo(Experimenter target) throws Exception {
        if (!target.isLoaded()) {
            target = root.getSession().getAdminService().getExperimenter(target.getId().getValue());
        }
        sudo(target.getOmeName().getValue());
    }

}
