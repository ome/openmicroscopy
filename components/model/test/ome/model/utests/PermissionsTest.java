/*
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.model.utests;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Flag;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PermissionsTest {

    Permissions p;

    @BeforeMethod
    protected void setUp() throws Exception {
        p = new Permissions();
    }

    @Test
    public void testSimplePermissionsAllowsEverything() throws Exception {
        Assert.assertTrue(p.isGranted(Role.USER, Right.READ));
        Assert.assertTrue(p.isGranted(Role.USER, Right.WRITE));
        Assert.assertTrue(p.isGranted(Role.GROUP, Right.READ));
        Assert.assertTrue(p.isGranted(Role.GROUP, Right.ANNOTATE));
        Assert.assertTrue(p.isGranted(Role.GROUP, Right.WRITE));
        Assert.assertTrue(p.isGranted(Role.WORLD, Right.READ));
        Assert.assertTrue(p.isGranted(Role.WORLD, Right.ANNOTATE));
        Assert.assertTrue(p.isGranted(Role.WORLD, Right.WRITE));
        // assertTrue( p.isGranted(ALL, READ) );
        // assertTrue( p.isGranted(ALL, WRITE) );
        // assertTrue( p.isGranted(ALL, USE) );
    }

    @Test
    public void testGrantAndRevokePermissions() throws Exception {
        revokeGrantCheck(Role.USER, Right.READ);
        revokeGrantCheck(Role.USER, Right.ANNOTATE);
        revokeGrantCheck(Role.USER, Right.WRITE);
        revokeGrantCheck(Role.GROUP, Right.READ);
        revokeGrantCheck(Role.GROUP, Right.ANNOTATE);
        revokeGrantCheck(Role.GROUP, Right.WRITE);
        revokeGrantCheck(Role.WORLD, Right.READ);
        revokeGrantCheck(Role.WORLD, Right.ANNOTATE);
        revokeGrantCheck(Role.WORLD, Right.WRITE);
    }

    @Test
    public void testNullRightsAreOk() throws Exception {
        String before;

        before = p.toString();
        p.grant(Role.USER, (Right[]) null);
        Assert.assertEquals(p.toString(), before);

        before = p.toString();
        p.grant(Role.USER);
        Assert.assertEquals(p.toString(), before);

        before = p.toString();
        p.revoke(Role.USER, (Right[]) null);
        Assert.assertEquals(p.toString(), before);

        before = p.toString();
        p.revoke(Role.USER);
        Assert.assertEquals(p.toString(), before);
    }

    @Test
    public void testVarArgs() throws Exception {
        p.revoke(Role.GROUP, Right.READ, Right.WRITE);
        Assert.assertFalse(p.isGranted(Role.GROUP, Right.READ));
        Assert.assertFalse(p.isGranted(Role.GROUP, Right.WRITE));
    }

    @Test
    public void testApplyMask() throws Exception {
        Permissions mask = new Permissions();
        mask.revoke(Role.WORLD, Right.WRITE);
        Assert.assertFalse(mask.isGranted(Role.WORLD, Right.WRITE));

        p.revokeAll(mask);
        Assert.assertFalse(p.isGranted(Role.WORLD, Right.WRITE));

        mask = new Permissions(Permissions.EMPTY);
        mask.grant(Role.USER, Right.READ);
        Assert.assertTrue(mask.isGranted(Role.USER, Right.READ));

        p.grantAll(mask);
        Assert.assertTrue(p.isGranted(Role.USER, Right.READ));

    }

    // ~ Testing immutability
    // =========================================================================

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testRevokeOnImmutable() throws Exception {
        Permissions.READ_ONLY.revoke(Role.GROUP, Right.READ);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGrantOnImmutable() throws Exception {
        Permissions.READ_ONLY.grant(Role.GROUP, Right.WRITE);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testRevokeAllOnImmutable() throws Exception {
        Permissions.READ_ONLY.revokeAll(Permissions.GROUP_PRIVATE);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGrantAllOnImmutable() throws Exception {
        Permissions.READ_ONLY.grantAll(Permissions.GROUP_PRIVATE);
    }

    @Test
    public void testCopyConstructor() throws Exception {
        Permissions p1 = new Permissions();
        p1.revoke(Role.GROUP, Right.WRITE);
        Assert.assertFalse(p1.isGranted(Role.GROUP, Right.WRITE));
        Permissions p2 = new Permissions(p1);
        Assert.assertFalse(p2.isGranted(Role.GROUP, Right.WRITE));

        Permissions none = new Permissions(Permissions.EMPTY);
        Assert.assertFalse(none.isGranted(Role.USER, Right.READ));
        Assert.assertFalse(none.isGranted(Role.USER, Right.WRITE));
        Assert.assertFalse(none.isGranted(Role.GROUP, Right.READ));
        Assert.assertFalse(none.isGranted(Role.GROUP, Right.WRITE));
        Assert.assertFalse(none.isGranted(Role.WORLD, Right.READ));
        Assert.assertFalse(none.isGranted(Role.WORLD, Right.WRITE));

    }

    // ~ Internal-state dependent tests
    // =========================================================================

    private static class Perms extends Permissions {
        /**
         * 
         */
        private static final long serialVersionUID = -181330578094652586L;

        public long toLong() {
            return super.getPerm1();
        }

        @Override
        public Perms revoke(Role role, Right... rights) {
            return (Perms) super.revoke(role, rights);
        }
    }

    @Test
    public void testLongValues() throws Exception {
        Perms pp = new Perms();
        Assert.assertEquals(pp.toLong(), -1);
        pp.revoke(Role.GROUP, Right.WRITE);
        pp.revoke(Role.WORLD, Right.WRITE);
        Assert.assertEquals(pp.toLong(), -35);
    }

    @Test
    public void testToBits() throws Exception {
        bitCompare(Role.USER, Right.READ);
        bitCompare(Role.USER, Right.WRITE);
        bitCompare(Role.GROUP, Right.READ);
        bitCompare(Role.GROUP, Right.WRITE);
        bitCompare(Role.WORLD, Right.READ);
        bitCompare(Role.WORLD, Right.WRITE);
    }

    // ~ Equals && HashCode
    // =========================================================================
    // see http://trac.openmicroscopy.org.uk/ome/ticket/291

    @Test(groups = "ticket:291")
    public void testEquals() throws Exception {
        Permissions t = new Permissions();
        Assert.assertTrue(p.identical(t));

        p.revoke(Role.GROUP, Right.READ);
        t.revoke(Role.GROUP, Right.READ);
        Assert.assertTrue(p.identical(t));
    }

    @Test(groups = "ticket:291")
    public void testHashCode() throws Exception {
        Permissions t = new Permissions();
        Assert.assertFalse(p.hashCode() == t.hashCode());

        p.revoke(Role.GROUP, Right.WRITE);
        t.revoke(Role.GROUP, Right.WRITE);
        Assert.assertFalse(p.hashCode() == t.hashCode());
    }

    @Test(groups = "ticket:291")
    public void testSameRights() throws Exception {
        Permissions t = new Permissions();
        p.revoke(Role.GROUP, Right.WRITE);
        t.revoke(Role.GROUP, Right.WRITE);
        Assert.assertTrue(p.sameRights(t));
    }

    // ~ Flags
    // =========================================================================

    @Test(groups = "ticket:182")
    public void testFlags() throws Exception {
        Permissions t = new Permissions();
        Assert.assertFalse(t.isSet(Flag.UNUSED));
        t.set(Flag.UNUSED);
        Assert.assertTrue(t.isSet(Flag.UNUSED));
    }

    @Test(groups = "ticket:215")
    public void testCopyCtorCopiesFlags() throws Exception {
        p = new Permissions();
        Assert.assertFalse(p.isSet(Flag.UNUSED));
        p.set(Flag.UNUSED);
        Permissions t = new Permissions(p);
        Assert.assertTrue(t.isSet(Flag.UNUSED));

    }

    // ~ Immutables
    // =========================================================================

    @Test
    public void testRandomSample() throws Exception {
        Assert.assertFalse(Permissions.GROUP_READABLE.isGranted(Role.WORLD, Right.READ));
        Assert.assertTrue(Permissions.GROUP_READABLE.isGranted(Role.GROUP, Right.READ));
        Assert.assertTrue(Permissions.GROUP_READABLE.isGranted(Role.USER, Right.READ));
    }

    @Test
    public void testCompareWithString() throws Exception {
        Assert.assertTrue(Permissions.EMPTY.sameRights(Permissions
                .parseString("------")));
        Assert.assertTrue(Permissions.EMPTY.sameRights(Permissions
                .parseString("______")));
        Assert.assertTrue(Permissions.GROUP_IMMUTABLE.sameRights(Permissions
                .parseString("r_r___")));
        Assert.assertTrue(Permissions.GROUP_PRIVATE.sameRights(Permissions
                .parseString("rwrw__")));
        Assert.assertTrue(Permissions.GROUP_READABLE.sameRights(Permissions
                .parseString("rwr___")));
        Assert.assertTrue(Permissions.GROUP_WRITEABLE.sameRights(Permissions
                .parseString("rwrwr_")));
        Assert.assertTrue(Permissions.PUBLIC.sameRights(Permissions
                .parseString("rwrwrw")));
        Assert.assertTrue(Permissions.READ_ONLY.sameRights(Permissions
                .parseString("r_r_r_")));
        Assert.assertTrue(Permissions.USER_IMMUTABLE.sameRights(Permissions
                .parseString("r_____")));
        Assert.assertTrue(Permissions.USER_PRIVATE.sameRights(Permissions
                .parseString("rw____")));
        Assert.assertTrue(Permissions.USER_WRITEABLE.sameRights(Permissions
                .parseString("rwr_r_")));
        Assert.assertTrue(Permissions.WORLD_IMMUTABLE.sameRights(Permissions
                .parseString("r_r_r_")));
        Assert.assertTrue(Permissions.WORLD_WRITEABLE.sameRights(Permissions
                .parseString("rwrwrw")));

    }

    @Test
    public void testDelegationFunctionsProperly() throws Exception {
        Permissions.PUBLIC.toString();
        Permissions.PUBLIC.identical(Permissions.PUBLIC);
        Permissions.PUBLIC.isGranted(Role.GROUP, Right.READ);
        Permissions.PUBLIC.isSet(Flag.UNUSED);
        Permissions.PUBLIC.sameRights(p);
        try {
            Permissions.PUBLIC.grant(Role.GROUP, Right.READ);
        } catch (UnsupportedOperationException uoe) {
        }
        ;
        try {
            Permissions.PUBLIC.grantAll(Permissions.EMPTY);
        } catch (UnsupportedOperationException uoe) {
        }
        ;
        try {
            Permissions.PUBLIC.revoke(Role.GROUP, Right.READ);
        } catch (UnsupportedOperationException uoe) {
        }
        ;
        try {
            Permissions.PUBLIC.revokeAll(Permissions.EMPTY);
        } catch (UnsupportedOperationException uoe) {
        }
        ;
        try {
            Permissions.PUBLIC.set(Flag.UNUSED);
        } catch (UnsupportedOperationException uoe) {
        }
        ;
        try {
            Permissions.PUBLIC.unSet(Flag.UNUSED);
        } catch (UnsupportedOperationException uoe) {
        }
        ;
    }

    @Test
    public void testConstantsAreNotSoft() throws Exception {
        Assert.assertFalse(Permissions.EMPTY.isSet(Flag.UNUSED));
        Assert.assertFalse(Permissions.GROUP_IMMUTABLE.isSet(Flag.UNUSED));
        Assert.assertFalse(Permissions.GROUP_PRIVATE.isSet(Flag.UNUSED));
        Assert.assertFalse(Permissions.GROUP_READABLE.isSet(Flag.UNUSED));
        Assert.assertFalse(Permissions.GROUP_WRITEABLE.isSet(Flag.UNUSED));
        Assert.assertFalse(Permissions.PUBLIC.isSet(Flag.UNUSED));
        Assert.assertFalse(Permissions.READ_ONLY.isSet(Flag.UNUSED));
        Assert.assertFalse(Permissions.USER_IMMUTABLE.isSet(Flag.UNUSED));
        Assert.assertFalse(Permissions.USER_PRIVATE.isSet(Flag.UNUSED));
        Assert.assertFalse(Permissions.USER_WRITEABLE.isSet(Flag.UNUSED));
        Assert.assertFalse(Permissions.WORLD_IMMUTABLE.isSet(Flag.UNUSED));
        Assert.assertFalse(Permissions.WORLD_WRITEABLE.isSet(Flag.UNUSED));
    }

    // ~ Serialization
    // =========================================================================

    // grant() is unimplemented, until it is it will always raise this exception.
    @Test(groups = { "broken", "ticket:375" }, expectedExceptions = UnsupportedOperationException.class)
    public void testImmutableSerialization() throws Exception {
        byte[] ser = serialize(Permissions.PUBLIC);
        p = deserialize(ser);
        p.grant(Role.GROUP, Right.READ); // is this what we want?
    }

    // ~ Parse String
    // =========================================================================
    @Test
    public void testParseString() throws Exception {
        p = Permissions.parseString("rwrwrw");
        Assert.assertTrue(p.isGranted(Role.USER, Right.READ));
        Assert.assertTrue(p.isGranted(Role.USER, Right.ANNOTATE));
        Assert.assertTrue(p.isGranted(Role.USER, Right.WRITE));
        Assert.assertTrue(p.isGranted(Role.GROUP, Right.READ));
        Assert.assertTrue(p.isGranted(Role.GROUP, Right.ANNOTATE));
        Assert.assertTrue(p.isGranted(Role.GROUP, Right.WRITE));
        Assert.assertTrue(p.isGranted(Role.WORLD, Right.READ));
        Assert.assertTrue(p.isGranted(Role.WORLD, Right.ANNOTATE));
        Assert.assertTrue(p.isGranted(Role.WORLD, Right.WRITE));

        p = Permissions.parseString("RWRWRW");
        Assert.assertTrue(p.isGranted(Role.USER, Right.READ));
        Assert.assertTrue(p.isGranted(Role.USER, Right.ANNOTATE));
        Assert.assertTrue(p.isGranted(Role.USER, Right.WRITE));
        Assert.assertTrue(p.isGranted(Role.GROUP, Right.READ));
        Assert.assertTrue(p.isGranted(Role.GROUP, Right.ANNOTATE));
        Assert.assertTrue(p.isGranted(Role.GROUP, Right.WRITE));
        Assert.assertTrue(p.isGranted(Role.WORLD, Right.READ));
        Assert.assertTrue(p.isGranted(Role.WORLD, Right.ANNOTATE));
        Assert.assertTrue(p.isGranted(Role.WORLD, Right.WRITE));

        p = Permissions.parseString("______");
        Assert.assertFalse(p.isGranted(Role.USER, Right.READ));
        Assert.assertFalse(p.isGranted(Role.USER, Right.ANNOTATE));
        Assert.assertFalse(p.isGranted(Role.USER, Right.WRITE));
        Assert.assertFalse(p.isGranted(Role.GROUP, Right.READ));
        Assert.assertFalse(p.isGranted(Role.GROUP, Right.ANNOTATE));
        Assert.assertFalse(p.isGranted(Role.GROUP, Right.WRITE));
        Assert.assertFalse(p.isGranted(Role.WORLD, Right.READ));
        Assert.assertFalse(p.isGranted(Role.WORLD, Right.ANNOTATE));
        Assert.assertFalse(p.isGranted(Role.WORLD, Right.WRITE));

    }

    // ~ Private helpers
    // ===========================================================================

    private void revokeGrantCheck(Role role, Right right) {
        p.revoke(role, right);
        Assert.assertFalse(p.isGranted(role, right));
        p.grant(role, right);
        Assert.assertTrue(p.isGranted(role, right));
    }

    private void bitCompare(Role role, Right right) {
        Perms pp = new Perms().revoke(role, right);
        long l = pp.toLong();
        long bit = Perms.bit(role, right);
        Assert.assertTrue((l ^ bit) == -1L);

    }

    private Permissions deserialize(byte[] stream) throws Exception {
        ByteArrayInputStream bais;
        ObjectInputStream ois;

        bais = new ByteArrayInputStream(stream);
        ois = new ObjectInputStream(bais);
        Permissions p = (Permissions) ois.readObject();
        ois.close();
        bais.close();
        return p;
    }

    private byte[] serialize(Permissions p) throws Exception {
        ByteArrayOutputStream baos;
        ObjectOutputStream oos;

        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(baos);
        oos.writeObject(p);
        oos.close();
        byte[] retVal = baos.toByteArray();
        baos.close();
        return retVal;
    }
}
