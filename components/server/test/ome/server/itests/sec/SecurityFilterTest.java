/*
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.itests.sec;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import ome.annotations.RolesAllowed;
import ome.api.IQuery;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.ExperimenterAnnotationLink;
import ome.model.annotations.FileAnnotation;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.model.meta.Experimenter;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.security.AdminAction;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.query.Definitions;
import ome.services.query.Query;
import ome.services.query.QueryParameterDef;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

@Test(groups = { "ticket:117", "security", "filter" })
public class SecurityFilterTest extends AbstractManagedContextTest {

    static String ticket117 = "ticket:117";

    Executor ex;

    @Test
    public void testFilterDisallowsRead() throws Exception {

        Image i;

        Experimenter e = loginNewUser(Permissions.PRIVATE);
        i = createImage();

        loginNewUserInOtherUsersGroup(e);
        assertCannotReadImage(i);

        loginUserKeepGroup(e);
        assertCanReadImage(i);
    }

    @Test
    public void testRootCanReadAll() throws Exception {

        Image i;

        Experimenter e = loginNewUser(Permissions.PRIVATE);
        i = createImage();
        assertCanReadImage(i);

        loginNewUserInOtherUsersGroup(e);
        assertCannotReadImage(i);

        loginRootKeepGroup();
        assertCanReadImage(i);
    }

    @Test
    public void testRunAsAdminCanReadAll() throws Exception {

        final Image i;
        final Dataset d;

        Experimenter e = loginNewUser(Permissions.PRIVATE);
        d = createDataset();
        i = createImage();

        loginNewUserInOtherUsersGroup(e);
        assertCannotReadImage(d, i);

        final SecurityFilterTest test = this;
        String uuid = (String) applicationContext.getBean("uuid");

        ex = (Executor) applicationContext.getBean("executor");
        ex.execute(new Principal(uuid), new Executor.SimpleWork(this, "run as admin") {
		@RolesAllowed("user")
		@Transactional(readOnly=true)
		    public Object doWork(org.hibernate.Session session, final ServiceFactory sf) {
		    securitySystem.runAsAdmin(new AdminAction() {
			    public void runAsAdmin() {
			        assertCanReadImage(sf.getQueryService(), i);
			    }
			});
		    return null;
		}
	    });

        loginUserKeepGroup(e);
        assertCanReadImage(d, i);
    }

    @Test
    public void testGroupReadable() throws Exception {

        Image i;

        Experimenter e = loginNewUser(Permissions.COLLAB_READONLY);
        i = createImage();
        assertCanReadImage(i);

        loginNewUserInOtherUsersGroup(e);
        assertCanReadImage(i);

        loginNewUser();
        assertCannotReadImage(i);

    }

    @Test
    public void testGroupLeadersCanReadAllInGroup() throws Exception {
        Image i;

        // as non-PI create an image..
        Experimenter e = loginNewUser(Permissions.PRIVATE);
        i = createImage();
        assertCanReadImage(i);

        // others in group can't read
        loginNewUserInOtherUsersGroup(e);
        assertCannotReadImage(i);

        // but PI can
        Experimenter pi = loginNewUserInOtherUsersGroup(e);
        loginRootKeepGroup();
        iAdmin.setGroupOwner(currentGroup(), pi);
        loginUserKeepGroup(pi);
        assertCanReadImage(i);
    }

    @Test(groups = "broken") /* not supported currently */
    public void testUserCanHideFromSelf() throws Exception {
        Image i;

        // create an image with no permissions
        Permissions unreadable = new Permissions(Permissions.USER_IMMUTABLE)
            .revoke(Role.USER, Right.READ);
        loginNewUser(unreadable);
        i = createImage();
        assertCannotReadImage(i);

    }

    @Test
    public void testFilterDoesntHinderOuterJoins() throws Exception {

    }

    @Test
    public void testWorldReadable() throws Exception {

    }

    @Test
    public void testStatefulServicesFollowSameContract() throws Exception {

    }

    @Test /* doesn't reproduce */
    public void testTicket663() {

        Experimenter e = loginNewUser(Permissions.PRIVATE);
        Project p = new Project("663");
        Dataset d = new Dataset("663");
        p.linkDataset(d);
        iUpdate.saveObject(p);

        long uid = iAdmin.getEventContext().getCurrentUserId();
        ome.parameters.Filter filter = new ome.parameters.Filter().owner(uid);
        ome.parameters.Parameters params = new ome.parameters.Parameters(filter);

        findAllProjects(params);

        loginUserInNewGroup(e);

        findAllProjects(params);

    }

    private void findAllProjects(ome.parameters.Parameters params) {
        iQuery.findAllByQuery("select p from Project p" +
                " left outer join fetch p.datasetLinks l"+
                " left outer join fetch l.child d", params);
    }

    @Test
    public void testTicket1798() throws Exception {
        Experimenter e = loginNewUser();

        // Create an annotation like a user photo in #1798
        long uid = iAdmin.getEventContext().getCurrentUserId();
        FileAnnotation fa = new FileAnnotation();
        ExperimenterAnnotationLink link = new ExperimenterAnnotationLink();
        link.link(new Experimenter(uid, false), fa);
        iUpdate.saveObject(link);

        loadUserFileAnnotations(uid);

        // Now login to another group and see what happens
        loginUserInNewGroup(e);
        loadUserFileAnnotations(uid);
    }

    private void loadUserFileAnnotations(long uid) {
        Map<Long, Set<Annotation>> map = iMetadata.loadAnnotations(
                Experimenter.class, java.util.Collections.singleton(uid),
                Collections.singleton("FileAnnotation"), null, null);
    }


    // ~ Helpers
    // =========================================================================

    private Image createImage() {
        Image img = new Image();
        img.setName(ticket117 + ":" + UUID.randomUUID().toString());
        return createObject(img);
    }

    private Dataset createDataset() {
        Dataset ds = new Dataset();
        ds.setName(ticket117 + ":" + UUID.randomUUID().toString());
        return createObject(ds);
    }

    private <T extends IObject> T createObject(T obj) {
        obj = factory.getUpdateService().saveAndReturnObject(obj);
        return obj;
    }

    private <T extends IObject> void assertCannotReadImage(T... ts) {

        T test;
        for (T t : ts) {
            test = getAsString(t);
            assertNull(t + "==null", test);

            test = getByCriteria(t);
            assertNull(t + "==null", test);
        }

    }

    private <T extends IObject> void assertCanReadImage(T... ts) {
        assertCanReadImage(iQuery, ts);
    }

    private <T extends IObject> void assertCanReadImage(IQuery q, T... ts) {
        T test;
        for (T t : ts) {
            test = getAsString(q, t);
            assertNotNull(t + "!=null", test);

            test = getByCriteria(q, t);
            assertNotNull(t + "!=null", test);
        }

    }

    private <T extends IObject> T getAsString(IQuery q, T obj) {
        return (T) iQuery.findByString(obj.getClass(), "name", ByNameQuery
                .name(obj));
    }

    private <T extends IObject> T getByCriteria(IQuery q, T obj) {
        return (T) iQuery.execute(new ByNameQuery(obj));
    }

    private <T extends IObject> T getAsString(T obj) {
        return getAsString(iQuery, obj);
    }

    private <T extends IObject> T getByCriteria(T obj) {
        return getByCriteria(iQuery, obj);
    }
}

class ByNameQuery extends Query {

    static Definitions defs = new Definitions(new QueryParameterDef("name",
            String.class, false));

    Object obj;

    public <T extends IObject> ByNameQuery(T obj) {
        super(defs, new Parameters(new Filter().unique()).addString("name",
                name(obj)));
        this.obj = obj;
    }

    @Override
    protected void buildQuery(Session session) throws HibernateException,
            SQLException {
        Criteria c = session.createCriteria(obj.getClass());
        c.add(Restrictions.eq("name", value("name")));
        setCriteria(c);
    }

    static String name(Object object) {
        try {
            Field f = object.getClass().getDeclaredField("name");
            f.setAccessible(true);
            return (String) f.get(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
