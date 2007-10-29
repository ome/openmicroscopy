/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.client.itests.sec;

import ome.api.IAdmin;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.system.Login;
import ome.system.ServiceFactory;

import org.jboss.util.id.GUID;
import org.springframework.dao.EmptyResultDataAccessException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = { "client", "integration", "security", "ticket:181",
        "ticket:199", "password" })
public class AbstractAccountTest extends AbstractSecurityTest {

    protected static final String OME_HASH = "vvFwuczAmpyoRC0Nsv8FCw==";

    protected ExperimenterGroup userGrp = new ExperimenterGroup(1L, false),
            sysGrp = new ExperimenterGroup(0L, false);

    protected Experimenter root, sudo;

    protected String sudo_name;

    protected String sudo_id;

    // ~ Testng Adapter
    // =========================================================================
    @BeforeClass
    public void sudoCanLoginWith_ome() throws Exception {
        init();
        root = rootQuery.get(Experimenter.class, 0L);
        sudo = createNewSystemUser(rootAdmin);
        sudo_name = sudo.getOmeName();
        resetPasswordTo_ome(sudo);
        assertCanLogin(sudo_name, "ome");
        assertCannotLogin(sudo_name, "bob");
    }

    // ~ Helpers
    // =========================================================================

    protected Experimenter createNewUser(IUpdate iUpdate) {
        ExperimenterGroup g = new ExperimenterGroup();
        g.setName(GUID.asString());
        g = iUpdate.saveAndReturnObject(g);
        g.unload();
        Experimenter e = new Experimenter();
        e.setOmeName(new GUID().asString());
        e.setFirstName("ticket:181");
        e.setLastName("ticket:181");
        GroupExperimenterMap map = new GroupExperimenterMap();
        map.link(userGrp, e);
        GroupExperimenterMap def = new GroupExperimenterMap();
        def.link(g, e);
        def.setDefaultGroupLink(true);
        e.addGroupExperimenterMap(map, false);
        e.addGroupExperimenterMap(def, false);
        return iUpdate.saveAndReturnObject(e);
    }

    protected Experimenter createNewUser(IAdmin iAdmin) {
        ExperimenterGroup g = new ExperimenterGroup();
        g.setName(GUID.asString());
        iAdmin.createGroup(g);
        Experimenter e = new Experimenter();
        e.setOmeName(GUID.asString());
        e.setFirstName("ticket:181");
        e.setLastName("ticket:181");
        long id = iAdmin.createUser(e, g.getName());
        return iAdmin.getExperimenter(id);
    }

    protected Experimenter createNewSystemUser(IAdmin iAdmin) {

        Experimenter e = new Experimenter();
        e.setOmeName(GUID.asString());
        e.setFirstName("ticket:181");
        e.setLastName("ticket:181");
        long id = iAdmin.createSystemUser(e);
        return iAdmin.getExperimenter(id);
    }

    protected String getPasswordFromDb(Experimenter e) throws Exception {
        try {
            return jdbc.queryForObject(
                    "select hash from password " + "where experimenter_id = ?",
                    String.class, e.getId()).trim(); // TODO remove trim in
            // sync with
            // JBossLoginModule
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    protected void resetPasswordTo_ome(Experimenter e) throws Exception {
        resetPasswordTo_ome(e.getId());
    }

    protected void resetPasswordTo_ome(Long id) throws Exception {
        int count = jdbc.update(
                "update password set hash = ? where experimenter_id = ?",
                AbstractAccountTest.OME_HASH, id);

        if (count < 1) {

            count = jdbc.update("insert into password values (?,?)", id,
                    AbstractAccountTest.OME_HASH);
            assertTrue(count == 1);
        }
        dataSource.getConnection().commit();
        rootAdmin.synchronizeLoginCache();
    }

    protected int setPasswordtoEmptyString(Experimenter e) throws Exception {
        int count = jdbc.update(
                "update password set hash = ? where experimenter_id = ?", "", e
                        .getId());
        if (count < 1) {
            count = jdbc.update("insert into password values (?,?)", e.getId(),
                    "");
        }
        dataSource.getConnection().commit();
        rootAdmin.synchronizeLoginCache();
        return count;
    }

    protected void removePasswordEntry(Experimenter e) throws Exception {
        int count = jdbc.update(
                "delete from password where experimenter_id = ?", e.getId());
        dataSource.getConnection().commit();
        rootAdmin.synchronizeLoginCache();
    }

    protected void nullPasswordEntry(Experimenter e) throws Exception {
        int count = jdbc.update(
                "update password set hash = null where experimenter_id = ?", e
                        .getId());
        if (count < 1) {
            count = jdbc.update("insert into password values (?,null)", e
                    .getId());
        }
        dataSource.getConnection().commit();
        rootAdmin.synchronizeLoginCache();

        if (count < 1) {
            throw new RuntimeException("No row inserted during null entry.");
        }
    }

    protected void assertCanLogin(String name, String password) {
        assertLogin(name, password, true);
    }

    protected void assertCannotLogin(String name, String password) {
        assertLogin(name, password, false);
    }

    protected void assertLogin(String name, String password, boolean works) {
        rootAdmin.synchronizeLoginCache();
        try {
            new ServiceFactory(new Login(name, password)).getQueryService()
                    .get(Experimenter.class, 0L);
            if (!works) {
                fail("Login should not have succeeded:" + name + ":" + password);
            }
        } catch (Exception e) {
            if (works) {
                throw new RuntimeException(e);
            }
        }

    }

    protected IAdmin getSudoAdmin(String password) {
        return new ServiceFactory(new Login(sudo_name, password, "system",
                "Test")).getAdminService();
    }

    protected IQuery getSudoQuery(String password) {
        return new ServiceFactory(new Login(sudo_name, password, "system",
                "Test")).getQueryService();
    }

    protected IUpdate getSudoUpdate(String password) {
        return new ServiceFactory(new Login(sudo_name, password, "system",
                "Test")).getUpdateService();
    }

}
