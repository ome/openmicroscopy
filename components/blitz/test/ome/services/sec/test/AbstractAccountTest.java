/*
 *   $Id: AbstractAccountTest.java 2135 2008-02-07 11:21:08Z jmoore $
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sec.test;

import java.util.UUID;

import static omero.rtypes.*;
import omero.ServerError;
import omero.client;
import omero.api.IAdminPrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Experimenter;
import omero.model.ExperimenterI;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;

import org.springframework.dao.EmptyResultDataAccessException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

@Test(enabled=false, groups = { "broken", "client", "integration", "security", "ticket:181",
        "ticket:199", "password" })
public class AbstractAccountTest extends AbstractSecurityTest {

    protected static final String OME_HASH = "vvFwuczAmpyoRC0Nsv8FCw==";

    protected ExperimenterGroupI userGrp = new ExperimenterGroupI(1L, false),
            sysGrp = new ExperimenterGroupI(0L, false);

    protected Experimenter root, sudo;

    protected String sudo_name;

    protected String sudo_id;

    // ~ Testng Adapter
    // =========================================================================
    @BeforeClass
    public void sudoCanLoginWith_ome() throws Exception {
        init();
        root = (Experimenter) rootQuery.get("Experimenter", 0L);
        sudo = (Experimenter) createNewSystemUser(rootAdmin);
        sudo_name = sudo.getOmeName().getValue();
        resetPasswordTo_ome(sudo);
        assertCanLogin(sudo_name, "ome");
        assertCannotLogin(sudo_name, "bob");
    }

    // ~ Helpers
    // =========================================================================

    protected Experimenter createNewSystemUser(IAdminPrx iAdmin) {

        Experimenter e = new ExperimenterI();
        e.setOmeName(rstring(UUID.randomUUID().toString()));
        e.setFirstName(rstring("ticket:181"));
        e.setLastName(rstring("ticket:181"));
        long id;
        try
        {
            id = iAdmin.createSystemUser(e);
            return iAdmin.getExperimenter(id);
        } catch (ServerError e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
    }
    
    protected Experimenter createNewUser(IUpdatePrx iUpdate) {
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(rstring(UUID.randomUUID().toString()));
        try
        {
            g = (ExperimenterGroup) iUpdate.saveAndReturnObject(g);
            g.unload();
            Experimenter e = new ExperimenterI();
            e.setOmeName(rstring(UUID.randomUUID().toString()));
            e.setFirstName(rstring("ticket:181"));
            e.setLastName(rstring("ticket:181"));
            e.linkExperimenterGroup(g);
            e.linkExperimenterGroup(userGrp);
            return (Experimenter) iUpdate.saveAndReturnObject(e);
        } catch (ServerError e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
    }

    protected Experimenter createNewUser(IAdminPrx iAdmin) {
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(rstring(UUID.randomUUID().toString()));
        try
        {
            iAdmin.createGroup(g);
            Experimenter e = new ExperimenterI();
            e.setOmeName(rstring(UUID.randomUUID().toString()));
            e.setFirstName(rstring("ticket:181"));
            e.setLastName(rstring("ticket:181"));
            long id;
            id = iAdmin.createUser(e, g.getName().getValue());
            return iAdmin.getExperimenter(id);
        } catch (ServerError e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }

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
        resetPasswordTo_ome(e.getId().getValue());
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
        return count;
    }

    protected void removePasswordEntry(Experimenter e) throws Exception {
        int count = jdbc.update(
                "delete from password where experimenter_id = ?", e.getId());
        dataSource.getConnection().commit();
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
        client c = new client("system", 4064);
        try {
            c.createSession(name, password).getQueryService().get("Experimenter", 0L);
            if (!works) {
                fail("Login should not have succeeded:" + name + ":" + password);
            }
        } catch (Exception e) {
            if (works) {
                throw new RuntimeException(e);
            }
        }

    }

    protected IAdminPrx getSudoAdmin(String password) {
        client c = new client("system", 4063);
        ServiceFactoryPrx sf;
        try
        {
            sf = c.createSession(sudo_name, password);
            return sf.getAdminService();
        } catch (CannotCreateSessionException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        } catch (PermissionDeniedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        } catch (ServerError e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
    }

    protected IQueryPrx getSudoQuery(String password) {
        client c = new client("system", 4063);
        ServiceFactoryPrx sf;
        try
        {
            sf = c.createSession(sudo_name, password);
            return sf.getQueryService();
        } catch (CannotCreateSessionException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        } catch (PermissionDeniedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        } catch (ServerError e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
    }

    protected IUpdatePrx getSudoUpdate(String password) {
        client c = new client("system", 4063);
        ServiceFactoryPrx sf;
        try
        {
            sf = c.createSession(sudo_name, password);
            return sf.getUpdateService();
        } catch (CannotCreateSessionException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        } catch (PermissionDeniedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        } catch (ServerError e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
    }
}
