/*
 *   Copyright 2012 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import omero.RBool;
import omero.RMap;
import omero.RType;
import omero.ServerError;
import omero.gateway.util.Requests;
import omero.model.BooleanAnnotation;
import omero.model.BooleanAnnotationI;
import omero.model.CommentAnnotationI;
import omero.model.DetailsI;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.IObject;
import omero.model.Namespace;
import omero.model.Permissions;
import omero.model.PermissionsI;
import omero.model.Session;
import omero.sys.EventContext;
import omero.sys.Parameters;
import omero.sys.ParametersI;

/**
 * Tests for the updated group permissions of 4.3 and 4.4.
 *
 * @since 4.4.0
 */
public class PermissionsTest extends AbstractServerTest {

    // chmod
    // ==============================================

    /*
     * See #8277 permissions returned from the server should now be immutable.
     */
    @Test
    public void testImmutablePermissions() throws Exception {

        // Test on the raw object
        PermissionsI p = new omero.model.PermissionsI();
        p.ice_postUnmarshal();
        try {
            p.setPerm1(1);
            Assert.fail("throw!");
        } catch (omero.ClientError err) {
            // good
        }

        // and on one returned from the server
        CommentAnnotationI c = new omero.model.CommentAnnotationI();
        c = (CommentAnnotationI) this.iUpdate.saveAndReturnObject(c);
        p = (PermissionsI) c.getDetails().getPermissions();
        try {
            p.setPerm1(1);
        } catch (omero.ClientError err) {
            // good
        }
    }

    @Test
    public void testDisallow() {
        PermissionsI p = new omero.model.PermissionsI();
        Assert.assertTrue(p.canAnnotate());
        Assert.assertTrue(p.canEdit());
    }

    @Test
    public void testClientSet() throws Exception {
        CommentAnnotationI c = new omero.model.CommentAnnotationI();
        c = (CommentAnnotationI) this.iUpdate.saveAndReturnObject(c);
        DetailsI d = (DetailsI) c.getDetails();
        Assert.assertNotNull(d.getClient());
        Assert.assertNotNull(d.getSession());
        Assert.assertNotNull(d.getCallContext());
        Assert.assertNotNull(d.getEventContext());
    }

    /**
     * Test that {@link omero.api.IQueryPrx#get(String, long)} returns object permissions reporting that the <tt>root</tt> user
     * <q>can</q> do everything.
     * @throws Exception unexpected
     */
    @Test
    public void testRootCanPermissionsByGet() throws Exception {
        final EventContext normalUser = newUserAndGroup("rwr---");
        final long projectId = iUpdate.saveAndReturnObject(mmFactory.simpleProject()).getId().getValue();
        logRootIntoGroup(normalUser.groupId);
        final Permissions projectPerms = iQuery.get("Project", projectId).getDetails().getPermissions();
        Assert.assertTrue(projectPerms.canEdit());
        Assert.assertTrue(projectPerms.canAnnotate());
        Assert.assertTrue(projectPerms.canLink());
        Assert.assertTrue(projectPerms.canDelete());
        Assert.assertTrue(projectPerms.canChgrp());
        Assert.assertTrue(projectPerms.canChown());
    }

    /**
     * Test that {@link omero.api.IQueryPrx#projection(String, omero.sys.Parameters)} returns object permissions reporting that the
     * <tt>root</tt> user <q>can</q> do everything.
     * @throws Exception unexpected
     */
    @Test
    public void testRootCanPermissionsByProjection() throws Exception {
        final EventContext normalUser = newUserAndGroup("rwr---");
        final long projectId = iUpdate.saveAndReturnObject(mmFactory.simpleProject()).getId().getValue();
        logRootIntoGroup(normalUser.groupId);
        final Map<String, RType> queriedMap = ((RMap) iQuery.projection(
                "SELECT new map(project AS project_details_permissions) FROM Project AS project WHERE project.id = :id",
                new ParametersI().addId(projectId)).get(0).get(0)).getValue();
        final Map<String, RType> projectPermsMap = ((RMap) queriedMap.get("project_details_permissions")).getValue();
        Assert.assertTrue(((RBool) projectPermsMap.get("canEdit")).getValue());
        Assert.assertTrue(((RBool) projectPermsMap.get("canAnnotate")).getValue());
        Assert.assertTrue(((RBool) projectPermsMap.get("canLink")).getValue());
        Assert.assertTrue(((RBool) projectPermsMap.get("canDelete")).getValue());
        Assert.assertTrue(((RBool) projectPermsMap.get("canChgrp")).getValue());
        Assert.assertTrue(((RBool) projectPermsMap.get("canChown")).getValue());
    }

    /**
     * Test that an instance of the given class can be annotated only if the instance's {@link Permissions#canAnnotate()} is true.
     * @param annotateeClass the class of which to try annotating an instance
     * @throws Exception unexpected
     */
    @Test(dataProvider = "annotation classes", groups = "broken")
    public void testCanAnnotateConsistency(Class<? extends IObject> annotateeClass)
            throws Exception {
        final Parameters params = new ParametersI().page(0, 1);
        final List<IObject> toAnnotates = iQuery.findAllByQuery("FROM " + annotateeClass.getSimpleName(), params);
        if (CollectionUtils.isEmpty(toAnnotates)) {
            throw new SkipException("nothing to annotate");
        }
        final IObject toAnnotate = toAnnotates.get(0);
        final boolean isExpectSuccess = toAnnotate.getDetails().getPermissions().canAnnotate();
        final BooleanAnnotation annotation = new BooleanAnnotationI();
        annotation.setBoolValue(omero.rtypes.rbool(false));
        IObject link = mmFactory.createAnnotationLink(toAnnotate.proxy(), annotation);
        try {
            link = iUpdate.saveAndReturnObject(link);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        } finally {
            if (link.getId() != null) {
                doChange(Requests.delete().target(link).build());
            }
        }
    }

    /**
     * @return test cases for {@link #testCanAnnotateConsistency(Class)}
     */
    @DataProvider(name = "annotation classes") Object[][] provideSystemClasses() {
        return new Object[][] {
            new Object[] {Experimenter.class},
            new Object[] {ExperimenterGroup.class},
            new Object[] {Namespace.class},
            new Object[] {Session.class}};
    }
}
