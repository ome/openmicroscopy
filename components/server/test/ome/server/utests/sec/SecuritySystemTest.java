/*
 *   Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.utests.sec;

import static ome.model.internal.Permissions.Right.READ;
import static ome.model.internal.Permissions.Role.GROUP;
import static ome.model.internal.Permissions.Role.WORLD;

import java.util.List;

import org.hibernate.Filter;
import org.hibernate.Session;
import org.jmock.Mock;
import org.jmock.core.Invocation;
import org.jmock.core.Stub;
import org.jmock.core.stub.DefaultResultStub;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.testng.annotations.Test;

import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.model.IEnum;
import ome.model.IObject;
import ome.model.core.Image;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.meta.Event;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.AdminAction;
import ome.security.SecureAction;
import ome.security.basic.OneGroupSecurityFilter;

@Test
public class SecuritySystemTest extends AbstractBasicSecuritySystemTest {

    /*
     * Test method for 'ome.security.SecuritySystem.isReady()'
     */
    public void testIsReady() {
        prepareMocksWithUserDetails(false);

        assertFalse(sec.isReady());
        sec.loadEventContext(false);
        assertTrue(sec.isReady());
        sec.invalidateEventContext();
        assertFalse(sec.isReady());

        // don't need ready sec.sys.
        sec.isReady();
        sec.isSystemType(null);
        aclVoter.allowLoad(null, user.getClass(), Details.create(), 1L);
        sec.getSecurityRoles();
        sf.mockQuery.expects(atLeastOnce()).method("contains").will(
                returnValue(true));
        sec.doAction(new SecureAction() {
            public <T extends IObject> T updateObject(T... obj) {
                return null;
            };
        }, user);
        sec.copyToken(user, user);
        sec.invalidateEventContext();
        sec.isReady();
        sec.disable("foo");
        sec.enable();
        sec.isDisabled("");

        // need ready sec.sys
        try {
            sec.enableReadFilter(null);
            fail("Should throw ApiUsage");
        } catch (ApiUsageException api) {
        }
        ;
        // See documentation in method. No longer throws
        // try {
        // sec.disableReadFilter(null);
        // fail("Should throw ApiUsage");
        // } catch (ApiUsageException api) {
        // }
        // ;
        try {
            sec.newTransientDetails(null);
            fail("Should throw ApiUsage");
        } catch (ApiUsageException api) {
        }
        ;
        try {
            sec.checkManagedDetails(null, null);
            fail("Should throw ApiUsage");
        } catch (ApiUsageException api) {
        }
        ;
        try {
            // Requires ready
            prepareMocksWithUserDetails(false);
            assertFalse(sec.isReady());
            sec.loadEventContext(false);

            sec.addLog(null, Image.class, 1L);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        }
        ;

        // aclVoter doesn't participate in isReady()

    }

    /*
     * Test method for 'ome.security.SecuritySystem.isSystemType(Class<?
     * extends IObject>)'
     *
     * ticket:1784 - "system" group contents are system types
     */
    public void testIsSystemType() {
        assertTrue(sec.isSystemType(Experimenter.class));
        assertTrue(sec.isSystemType(ExperimenterGroup.class));
        assertTrue(sec.isSystemType(Event.class));
        assertTrue(sec.isSystemType(EventLog.class));
        assertTrue(sec.isSystemType(IEnum.class));
        assertFalse(sec.isSystemType(Image.class));
        // TODO what else
    }

    /*
     * Test method for 'ome.security.SecuritySystem.enableReadFilter(Object)'
     * Test method for 'ome.security.SecuritySystem.disableReadFilter(Object)'
     */
    @Test(enabled = false) // Basically is just testing the implementation.
    public void testEnableAndDisableReadFilter() {
        prepareMocksWithUserDetails(false);

        Mock mockFilter = mock(Filter.class);
        Filter f = (Filter) mockFilter.proxy();
        mockFilter.expects(once()).method("setParameter").with(
                eq(OneGroupSecurityFilter.is_adminorpi), eq(Boolean.FALSE)).will(
                returnValue(f));
        mockFilter.expects(once()).method("setParameter").with(
                eq(OneGroupSecurityFilter.current_user), eq(user.getId())).will(
                returnValue(f));

        Mock mockSession = mock(Session.class);
        mockSession.expects(once()).method("enableFilter").with(
                eq("securityFilter")).will(returnValue(f));
        mockSession.expects(once()).method("disableFilter").with(
                eq("securityFilter"));
        Session s = (Session) mockSession.proxy();

        sec.loadEventContext(false);
        sec.enableReadFilter(s);
        sec.disableReadFilter(s);
        sec.invalidateEventContext();

    }

    /*
     * Test method for 'ome.security.SecuritySystem.addLog(String, Class, Long)'
     */
    public void testAddLog() {
        prepareMocksWithUserDetails(true);
        sec.loadEventContext(true);
        assertTrue(sec.getLogs().size() == 0);
        sec.addLog("SHOULDN'T BE ADDED", Event.class, 1L);
        assertTrue(sec.getLogs().size() == 0);
        sec.addLog("SHOULD BE ADDED", Image.class, 2L);

        // ticket:328
        assertTrue(sec.getLogs().size() == 1);
        EventLog onlyLog = sec.getLogs().get(0);
        assertEquals(onlyLog.getAction(), "SHOULD BE ADDED");
        assertEquals(onlyLog.getEntityType(), Image.class.getName());
        assertEquals(onlyLog.getEntityId(), new Long(2L));
        sec.invalidateEventContext();
    }

    /*
     * Test method for 'ome.security.SecuritySystem.getCreationEvent()'
     */
    public void testGetCurrentEvent() {
        prepareMocksWithUserDetails(false);
        sec.loadEventContext(false);
        assertSame(cd.getEvent(), event);
        sec.invalidateEventContext();
    }

    /*
     * Test method for 'ome.security.SecuritySystem.clearCurrentDetails()'
     */
    public void testClearCurrentDetails() {
        prepareMocksWithUserDetails(false);
        assertFalse(sec.isReady());
        sec.loadEventContext(false);
        assertTrue(sec.isReady());
        sec.invalidateEventContext();
        assertFalse(sec.isReady());
    }

    /*
     * Test method for 'ome.security.SecuritySystem.setCurrentDetails()'
     */
    public void testSetCurrentDetails() {
        prepareMocksWithUserDetails(false);

        sec.loadEventContext(false);
        assertEquals(user.getId(), cd.getOwner().getId());
        assertEquals(event.getId(), cd.getEvent().getId());
        assertEquals(group.getId(), cd.getGroup().getId());
        assertTrue(sec.isReady());
        sec.invalidateEventContext();
    }

    @Test
    public void testNullChecksOnAllMethods() throws Exception {
        prepareMocksWithRootDetails(false);
        sec.loadEventContext(false);

        // can handle nulls
        sec.isSystemType(null);
        sec.copyToken(null, null);
        sec.enable((java.lang.String[])null);

        // uses Springs assert
        try {
            aclVoter.allowLoad(null, null, null, 1L);
            fail("Should throw IllegalArg");
        } catch (IllegalArgumentException iae) {
        }
        ;
        try {
            aclVoter.allowCreation(null);
            fail("Should throw IllegalArg");
        } catch (IllegalArgumentException iae) {
        }
        ;
        try {
            aclVoter.allowUpdate(null, null);
            fail("Should throw IllegalArg");
        } catch (IllegalArgumentException iae) {
        }
        ;
        try {
            aclVoter.allowDelete(null, null);
            fail("Should throw IllegalArg");
        } catch (IllegalArgumentException iae) {
        }
        ;
        try {
            sec.doAction(null, (IObject[])null);
            fail("Should throw IllegalArg");
        } catch (IllegalArgumentException iae) {
        }
        ;
        try {
            sec.addLog(null, null, null);
            fail("Should throw IllegalArg");
        } catch (IllegalArgumentException iae) {
        }
        ;
        try {
            aclVoter.throwLoadViolation(null);
            fail("Should throw IllegalArg");
        } catch (IllegalArgumentException iae) {
        }
        ;
        try {
            aclVoter.throwCreationViolation(null);
            fail("Should throw IllegalArg");
        } catch (IllegalArgumentException iae) {
        }
        ;
        try {
            aclVoter.throwUpdateViolation(null);
            fail("Should throw IllegalArg");
        } catch (IllegalArgumentException iae) {
        }
        ;
        try {
            aclVoter.throwDeleteViolation(null);
            fail("Should throw IllegalArg");
        } catch (IllegalArgumentException iae) {
        }
        ;

        // api usage
        try {
            sec.enableReadFilter(null);
            fail("Should throw ApiUsage");
        } catch (ApiUsageException api) {
        }
        ;
        // See documentation in method. No longer throws.
        // try {
        // sec.disableReadFilter(null);
        // fail("Should throw ApiUsage");
        // } catch (ApiUsageException api) {
        // }
        ;
        try {
            sec.newTransientDetails(null);
            fail("Should throw ApiUsage");
        } catch (ApiUsageException api) {
        }
        ;
        try {
            sec.checkManagedDetails(null, null);
            fail("Should throw ApiUsage");
        } catch (ApiUsageException api) {
        }
        ;
        try {
            sec.isDisabled(null);
            fail("Should throw ApiUsage");
        } catch (ApiUsageException api) {
        }
        ;
        try {
            sec.disable((java.lang.String[])null);
            fail("Should throw ApiUSage");
        } catch (ApiUsageException api) {
        }
        ;

    }

    @Test
    public void testIsSystemGroup() throws Exception {
        prepareMocksWithRootDetails(true);
        sec.loadEventContext(true);
        assertTrue(sec.getSecurityRoles().isSystemGroup(group));
        sec.invalidateEventContext();
    }

    @Test
    public void testLeaderOfGroups() throws Exception {
        prepareMocksWithUserDetails(false);
        sec.loadEventContext(false);
        assertEquals(cd.getCurrentEventContext().getLeaderOfGroupsList(),
                leaderOfGroups);
        sec.invalidateEventContext();
    }

    @Test
    public void testDisblingSubSystems() throws Exception {
        prepareMocksWithUserDetails(false);
        sec.loadEventContext(false);
        assertFalse(sec.isDisabled("foo"));
        sec.disable("foo");
        assertTrue(sec.isDisabled("foo"));
        sec.enable("foo");
        assertFalse(sec.isDisabled("foo"));
        sec.disable("foo");
        assertTrue(sec.isDisabled("foo"));
        sec.enable();
        assertFalse(sec.isDisabled("foo"));
        sec.invalidateEventContext();
    }

    // ~ CAN USE MORE WORK
    // =========================================================================

    /*
     * Test method for 'ome.security.SecuritySystem.allowCreation(IObject)'
     */
    public void testAllowCreation() {

        Experimenter e = new Experimenter();
        Image i = new Image();

        prepareMocksWithUserDetails(false);

        // 1. not system type
        sec.loadEventContext(false);
        assertFalse(aclVoter.allowCreation(e));
        assertTrue(aclVoter.allowCreation(i));
        sec.invalidateEventContext();

        // 2. is privileged
        SecureAction checkAllowCreate = new SecureAction() {
            public <T extends IObject> T updateObject(T... objs) {
                assertTrue(aclVoter.allowCreation(objs[0]));
                return null;
            }
        };
        sec.doAction(checkAllowCreate, e);
        sec.doAction(checkAllowCreate, i);

        // 3. user is admin.
        prepareMocksWithRootDetails(false);
        sec.loadEventContext(false);
        assertTrue(aclVoter.allowCreation(e));
        assertTrue(aclVoter.allowCreation(i));
        sec.invalidateEventContext();

    }

    /*
     * Test method for 'ome.security.SecuritySystem.allowUpdate(IObject)'
     */
    public void testAllowUpdate() {

        Experimenter e = new Experimenter();
        Image i = new Image();
        Details d = Details.create();
        d.setPermissions(new Permissions());

        prepareMocksWithUserDetails(false);

        // BASICS

        // 1. not system type
        sec.loadEventContext(false);
        assertFalse(aclVoter.allowUpdate(e, d));
        assertTrue(aclVoter.allowUpdate(i, d));
        sec.invalidateEventContext();

        // 2. is privileged
        SecureAction checkAllowCreate = new SecureAction() {
            public <T extends IObject> T updateObject(T... objs) {
                assertTrue(aclVoter.allowUpdate(objs[0], objs[0].getDetails()));
                return null;
            }
        };
        sec.doAction(checkAllowCreate, e);
        sec.doAction(checkAllowCreate, i);

        // 3. user is admin.
        prepareMocksWithRootDetails(false);
        sec.loadEventContext(false);
        assertTrue(aclVoter.allowUpdate(e, e.getDetails()));
        assertTrue(aclVoter.allowUpdate(i, i.getDetails()));
        sec.invalidateEventContext();

        // PERMISSIONS BASED
        prepareMocksWithUserDetails(false);
        sec.loadEventContext(false);

        // different owner but all permissions
        i = new Image(2L, true);
        i.getDetails().setOwner(new Experimenter(2L, false));
        i.getDetails().setGroup(new ExperimenterGroup(2L, false));
        i.getDetails().setPermissions(new Permissions());
        assertTrue(aclVoter.allowUpdate(i, i.getDetails()));

        // now lower permissions
        prepareMocksWithUserDetails(false, Permissions.READ_ONLY);
        sec.loadEventContext(false);
        assertFalse(aclVoter.allowUpdate(i, i.getDetails()));

    }

    /*
     * Test method for 'ome.security.SecuritySystem.allowDelete(IObject)'
     */
    public void testAllowDelete() {
        Experimenter e = new Experimenter();
        Image i = new Image();
        Details d = Details.create();
        d.setPermissions(new Permissions());

        prepareMocksWithUserDetails(false);

        // 1. not system type
        sec.loadEventContext(false);
        assertFalse(aclVoter.allowDelete(e, d));
        assertTrue(aclVoter.allowDelete(i, d));
        sec.invalidateEventContext();

        // 2. is privileged
        SecureAction checkAllowCreate = new SecureAction() {
            public <T extends IObject> T updateObject(T... objs) {
                assertTrue(aclVoter.allowDelete(objs[0], objs[0].getDetails()));
                return null;
            }
        };
        sec.doAction(checkAllowCreate, e);
        sec.doAction(checkAllowCreate, i);

        // 3. user is admin.
        prepareMocksWithRootDetails(false);
        sec.loadEventContext(false);
        assertTrue(aclVoter.allowDelete(e, e.getDetails()));
        assertTrue(aclVoter.allowDelete(i, i.getDetails()));
        sec.invalidateEventContext();

        // PERMISSIONS BASED
        prepareMocksWithUserDetails(false, Permissions.WORLD_WRITEABLE);
        sec.loadEventContext(false);

        // different owner but all permissions
        i = new Image(2L, true);
        i.getDetails().setOwner(new Experimenter(2L, false));
        i.getDetails().setGroup(new ExperimenterGroup(2L, false));
        i.getDetails().setPermissions(new Permissions());
        assertTrue(aclVoter.allowDelete(i, i.getDetails()));

        // now lower permissions
        prepareMocksWithUserDetails(false, Permissions.READ_ONLY);
        sec.loadEventContext(false);
        assertFalse(aclVoter.allowDelete(i, i.getDetails()));

        sec.invalidateEventContext();
    }

    /*
     * Test method for 'ome.security.SecuritySystem.allowLoad(IObject)'
     */
    public void testAllowLoad() {

        prepareMocksWithUserDetails(false, Permissions.PUBLIC);
        sec.loadEventContext(false);

        Details d = Details.create();
        d.setOwner(new Experimenter(2L, false));
        d.setGroup(new ExperimenterGroup(2L, false)); // in same group
        d.setPermissions(new Permissions());

        assertTrue(aclVoter.allowLoad(null, Image.class, d, 1L));

        prepareMocksWithUserDetails(false, Permissions.PRIVATE);
        sec.loadEventContext(false);
        assertFalse(aclVoter.allowLoad(null, Image.class, d, 1L));

        sec.invalidateEventContext();

    }

    /*
     * Test method for 'ome.security.SecuritySystem.transientDetails(IObject)'
     */
    public void testTransientDetails() {
        prepareMocksWithUserDetails(false);
        sec.loadEventContext(false);

        Permissions p = new Permissions();
        Image i = new Image();

        // setting permissions
        i.getDetails().setOwner(new Experimenter(1L, false));
        i.getDetails().setPermissions(p);
        Details test = sec.newTransientDetails(i);
        assertEquals(p, test.getPermissions());
        assertEquals(test.getOwner().getId(), user.getId());
        assertEquals(test.getGroup().getId(), group.getId());

        // can't change that value
        i.getDetails().setOwner(new Experimenter(3L, false));
        i.getDetails().setPermissions(p);
        try {
            sec.newTransientDetails(i);
            fail("should throw sec. vio.");
        } catch (SecurityViolation sv) {
        }

        sec.invalidateEventContext();

    }

    /*
     * Test method for 'ome.security.SecuritySystem.managedDetails(IObject,
     * Details)'
     */
    public void testManagedDetails() {
        prepareMocksWithUserDetails(false);
        sec.loadEventContext(false);

        Permissions p = new Permissions();
        Image i = new Image(1L, true);

        Details oldDetails = Details.create();
        oldDetails.setOwner(user);
        oldDetails.setGroup(group);
        oldDetails.setCreationEvent(event);
        oldDetails.setPermissions(new Permissions());

        // setting permissions
        i.getDetails().setOwner(new Experimenter(1L, false));
        i.getDetails().setGroup(new ExperimenterGroup(2L, false));
        i.getDetails().setCreationEvent(new Event(1L, false));
        i.getDetails().setPermissions(p);
        Details test = sec.checkManagedDetails(i, oldDetails);
        assertTrue(p.sameRights(test.getPermissions()));
        assertEquals(test.getOwner().getId(), user.getId());
        assertEquals(test.getGroup().getId(), group.getId());

        // can't change that value
        i.getDetails().setOwner(new Experimenter(3L, false));
        i.getDetails().setPermissions(p);
        try {
            sec.checkManagedDetails(i, oldDetails);
            fail("should throw sec. vio.");
        } catch (SecurityViolation sv) {
        }

        sec.invalidateEventContext();

    }

    @Test
    public void testRunAsAdmin() {
        prepareMocksWithUserDetails(false);
        sec.loadEventContext(false);

        assertFalse(cd.getCurrentEventContext().isCurrentUserAdmin());

        Mock mockFilter = mock(Filter.class);
        final Filter filter = (Filter) mockFilter.proxy();
        mockFilter.setDefaultStub(new DefaultResultStub());
        Mock mockSession = mock(Session.class);
        final Session session = (Session) mockSession.proxy();
        mockSession.expects(atLeastOnce()).method("enableFilter").will(
                returnValue(filter));
        sf.mockQuery.expects(once()).method("execute").will(new Stub() {
            public Object invoke(Invocation arg0) throws Throwable {
                ((HibernateCallback) arg0.parameterValues.get(0))
                        .doInHibernate(session);
                return null;
            }

            public StringBuffer describeTo(StringBuffer arg0) {
                return arg0.append("call doInHibernate");
            }

        });
        AdminAction action = new AdminAction() {
            public void runAsAdmin() {
                assertTrue(cd.getCurrentEventContext().isCurrentUserAdmin());
            }
        };
        sec.runAsAdmin(action);

        assertFalse(cd.getCurrentEventContext().isCurrentUserAdmin());

    }

    @Test
    public void testDoAction() {
        prepareMocksWithUserDetails(false);
        sec.loadEventContext(false);
        try {
            sec.doAction(new SecureAction() {
                public <T extends IObject> T updateObject(T... objs) {
                    fail("implement");
                    return null;
                }
            });
            fail("Where's the IllegalArgumentEx?");
        } catch (IllegalArgumentException iae) {
            // ok
        }
    }

    @Test
    public void testTokenFunctionality() throws Exception {

        Image i = new Image();
        assertFalse(sec.hasPrivilegedToken(i));

        prepareMocksWithUserDetails(false);
        sec.loadEventContext(false);
        sec.doAction(new SecureAction() {
            public <T extends IObject> T updateObject(T... objs) {
                assertTrue(sec.hasPrivilegedToken(objs[0]));
                Image test = new Image();
                sec.copyToken(objs[0], test);
                assertTrue(sec.hasPrivilegedToken(test));
                return null;
            }
        }, i);

    }

    @Test
    public void testLeaderAndMemberOfGroupsProperlyFilled() throws Exception {
        prepareMocksWithUserDetails(false);
        sec.loadEventContext(false);

        List<Long> l;

        l = sec.getEventContext().getLeaderOfGroupsList();
        assertTrue(l.containsAll(leaderOfGroups));
        assertTrue(leaderOfGroups.containsAll(l));

        l = sec.getEventContext().getMemberOfGroupsList();
        assertTrue(l.containsAll(memberOfGroups));
        assertTrue(memberOfGroups.containsAll(l));

    }

    @Test
    public void testReadOnlyFunctionality() throws Exception {
        prepareMocksWithUserDetails(true);
        sec.loadEventContext(true);
        assertTrue(sec.getEventContext().isReadOnly());
        assertNull(sec.getEventContext().getCurrentEventId());
    }

}
