package ome.model.utests;

import org.testng.annotations.*;

import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;

import junit.framework.TestCase;

import static ome.model.internal.Permissions.Role.*;
import static ome.model.internal.Permissions.Right.*;

public class PermissionsTest extends TestCase {

	Permissions p;

	@Override
	@Configuration(beforeTestMethod = true)
	protected void setUp() throws Exception {
		super.setUp();
		p = new Permissions();
	}

	@Test
	public void testSimplePermissionsAllowsEverything() throws Exception {
		assertTrue(p.isGranted(USER, READ));
		assertTrue(p.isGranted(USER, WRITE));
		assertTrue(p.isGranted(USER, USE));
		assertTrue(p.isGranted(GROUP, READ));
		assertTrue(p.isGranted(GROUP, WRITE));
		assertTrue(p.isGranted(GROUP, USE));
		assertTrue(p.isGranted(WORLD, READ));
		assertTrue(p.isGranted(WORLD, WRITE));
		assertTrue(p.isGranted(WORLD, USE));
		// assertTrue( p.isGranted(ALL, READ) );
		// assertTrue( p.isGranted(ALL, WRITE) );
		// assertTrue( p.isGranted(ALL, USE) );
	}

	@Test
	public void testGrantAndRevokePermissions() throws Exception {
		revokeGrantCheck(USER, READ);
		revokeGrantCheck(USER, WRITE);
		revokeGrantCheck(USER, USE);
		revokeGrantCheck(GROUP, READ);
		revokeGrantCheck(GROUP, WRITE);
		revokeGrantCheck(GROUP, USE);
		revokeGrantCheck(WORLD, READ);
		revokeGrantCheck(WORLD, WRITE);
		revokeGrantCheck(WORLD, USE);
	}

	@Test
	public void testNullRightsAreOk() throws Exception {
		String before;
		
		before = p.toString();
		p.grant(USER,(Right[]) null);
		assertEquals(before,p.toString());
		
		before = p.toString();
		p.grant(USER);
		assertEquals(before,p.toString());
		
		before = p.toString();
		p.revoke(USER,(Right[]) null);
		assertEquals(before,p.toString());
		
		before = p.toString();
		p.revoke(USER);
		assertEquals(before,p.toString());
	}
	
	@Test
	public void testVarArgs() throws Exception {
		p.revoke(GROUP, USE,READ,WRITE);
		assertFalse( p.isGranted(GROUP, READ));
		assertFalse( p.isGranted(GROUP, WRITE));
		assertFalse( p.isGranted(GROUP, USE));
	}
	
	@Test
	public void testApplyMask() throws Exception {
		Permissions mask = new Permissions();
		mask.revoke(WORLD, WRITE);
		assertFalse(mask.isGranted(WORLD, WRITE));
		
		p.applyMask( mask );
		assertFalse( p.isGranted(WORLD, WRITE) );
	}
	
	@Test
	public void testLongValues() throws Exception {
		Perms pp = new Perms();
		assertEquals(pp.toLong(),-1);
		pp.revoke(GROUP, WRITE);
		pp.revoke(WORLD, WRITE);
		assertEquals(pp.toLong(),-37);
	}
	
	private static class Perms extends Permissions {
		public long toLong() { return super.getPerm1(); }
	}

	// ~ Private helpers
	// ===========================================================================

	private void revokeGrantCheck(Role role, Right right) {
		p.revoke(role, right);
		assertFalse(p.isGranted(role, right));
		p.grant(role, right);
		assertTrue(p.isGranted(role, right));
	}

}
