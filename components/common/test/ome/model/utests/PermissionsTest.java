package ome.model.utests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.testng.annotations.*;

import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Flag;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;

import junit.framework.TestCase;

import static ome.model.internal.Permissions.Role.*;
import static ome.model.internal.Permissions.Right.*;
import static ome.model.internal.Permissions.Flag.*;

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
		
		p.revokeAll( mask );
		assertFalse( p.isGranted(WORLD, WRITE) );
	
		mask = new Permissions( Permissions.EMPTY );
		mask.grant(USER,READ);
		assertTrue(mask.isGranted(USER, READ));
		
		p.grantAll(mask);
		assertTrue( p.isGranted(USER, READ));
	
	}
	
	// ~ Testing immutability
	// =========================================================================
	
	@Test
	@ExpectedExceptions(UnsupportedOperationException.class)
	public void testRevokeOnImmutable() throws Exception {
		Permissions.READ_ONLY.revoke(GROUP, READ);
	}
	
	@Test
	@ExpectedExceptions(UnsupportedOperationException.class)
	public void testGrantOnImmutable() throws Exception {
		Permissions.READ_ONLY.grant(GROUP, WRITE);
	}

	@Test
	@ExpectedExceptions(UnsupportedOperationException.class)
	public void testRevokeAllOnImmutable() throws Exception {
		Permissions.READ_ONLY.revokeAll(Permissions.GROUP_PRIVATE);
	}
	
	@Test
	@ExpectedExceptions(UnsupportedOperationException.class)
	public void testGrantAllOnImmutable() throws Exception {
		Permissions.READ_ONLY.grantAll(Permissions.GROUP_PRIVATE);
	}

	
	@Test
	public void testCopyConstructor() throws Exception {
		Permissions p1 = new Permissions();
		p1.revoke(GROUP, WRITE);
		assertFalse(p1.isGranted(GROUP, WRITE));
		Permissions p2 = new Permissions(p1);
		assertFalse(p2.isGranted(GROUP, WRITE));
		
		Permissions none = new Permissions(Permissions.EMPTY);
		assertFalse(none.isGranted(USER, READ));
		assertFalse(none.isGranted(USER, WRITE));
		assertFalse(none.isGranted(USER, USE));
		assertFalse(none.isGranted(GROUP, READ));
		assertFalse(none.isGranted(GROUP, WRITE));
		assertFalse(none.isGranted(GROUP, USE));
		assertFalse(none.isGranted(WORLD, READ));
		assertFalse(none.isGranted(WORLD, WRITE));
		assertFalse(none.isGranted(WORLD, USE));
		
	}
	
	// ~ Internal-state dependent tests
	// =========================================================================

	private static class Perms extends Permissions {
		public long toLong() { return super.getPerm1(); }
		@Override
		public Perms revoke(Role role, Right... rights) {
			return (Perms) super.revoke(role, rights);
		}
	}

	@Test
	public void testLongValues() throws Exception {
		Perms pp = new Perms();
		assertEquals(pp.toLong(),-1);
		pp.revoke(GROUP, WRITE);
		pp.revoke(WORLD, WRITE);
		assertEquals(pp.toLong(),-35);
	}

	@Test
	public void testToBits() throws Exception {
		bitCompare(USER, READ);
		bitCompare(USER, WRITE);
		bitCompare(USER, USE);
		bitCompare(GROUP, READ);
		bitCompare(GROUP, WRITE);
		bitCompare(GROUP, USE);
		bitCompare(WORLD, READ);
		bitCompare(WORLD, WRITE);
		bitCompare(WORLD, USE);
	}

	// ~ Equals && HashCode
	// =========================================================================
	// see https://trac.openmicroscopy.org.uk/omero/ticket/291
	
	@Test( groups = "ticket:291" )
	public void testEquals() throws Exception {
		Permissions t = new Permissions();
//		assertEquals(p,t);
		assertTrue(p.identical(t));
		
		p.revoke(GROUP, READ);
		t.revoke(GROUP, READ);
//		assertEquals(p,t);
		assertTrue(p.identical(t));
	}
	
	@Test( groups = "ticket:291" )
	public void testHashCode() throws Exception {
		Permissions t = new Permissions();
//		assertEquals(p.hashCode(),t.hashCode());
		assertFalse(p.hashCode() == t.hashCode());
		
		p.revoke(GROUP, WRITE);
		t.revoke(GROUP, WRITE);
//		assertEquals(p.hashCode(),t.hashCode());
		assertFalse(p.hashCode() == t.hashCode());
	}
	
	@Test( groups = "ticket:291" )
	public void testSameRights() throws Exception {
		Permissions t = new Permissions();
		p.revoke(GROUP, WRITE);
		t.revoke(GROUP, WRITE);
		assertTrue(p.sameRights(t));
	}

	// ~ Flags
	// =========================================================================
	
	@Test( groups = "ticket:182" )
	public void testFlags() throws Exception {
		Permissions t = new Permissions();
		assertFalse( t.isSet( SOFT ));
		assertFalse( t.isSet( LOCKED ));
		t.set( SOFT );
		t.set( LOCKED );
		assertTrue( t.isSet( SOFT ));
		assertTrue( t.isSet( LOCKED ));
	}
	
	@Test( groups = "ticket:215" )
	public void testCopyCtorCopiesFlags() throws Exception {
		p = new Permissions();
		assertFalse( p.isSet(LOCKED) );
		assertFalse( p.isSet(SOFT) );
		p.set( LOCKED );
		p.set( SOFT );
		Permissions t = new Permissions( p );
		assertTrue( t.isSet(LOCKED) );
		assertTrue( t.isSet(SOFT) );
		
	}
	
	// ~ Immutables
	// =========================================================================
	
	@Test
	public void testRandomSample() throws Exception 
	{
		assertFalse( Permissions.GROUP_READABLE.isGranted(WORLD, READ));
		assertTrue( Permissions.GROUP_READABLE.isGranted(GROUP, READ));
		assertTrue( Permissions.GROUP_READABLE.isGranted(USER, READ));
	}
	
	@Test
	public void testCompareWithString() throws Exception
	{
		assertTrue( Permissions.EMPTY.sameRights( 
				Permissions.parseString("______")));
		assertTrue( Permissions.DEFAULT.sameRights( 
				Permissions.parseString("rwr_r_")));
		assertTrue( Permissions.GROUP_IMMUTABLE.sameRights( 
				Permissions.parseString("r_r___")));
		assertTrue( Permissions.GROUP_PRIVATE.sameRights( 
				Permissions.parseString("rwrw__")));
		assertTrue( Permissions.GROUP_READABLE.sameRights( 
				Permissions.parseString("rwr___")));
		assertTrue( Permissions.GROUP_WRITEABLE.sameRights( 
				Permissions.parseString("rwrwr_")));
		assertTrue( Permissions.PUBLIC.sameRights( 
				Permissions.parseString("rwrwrw")));
		assertTrue( Permissions.READ_ONLY.sameRights( 
				Permissions.parseString("r_r_r_")));
		assertTrue( Permissions.USER_IMMUTABLE.sameRights( 
				Permissions.parseString("r_____")));
		assertTrue( Permissions.USER_PRIVATE.sameRights( 
				Permissions.parseString("rw____")));
		assertTrue( Permissions.USER_WRITEABLE.sameRights( 
				Permissions.parseString("rwr_r_")));
		assertTrue( Permissions.WORLD_IMMUTABLE.sameRights( 
				Permissions.parseString("r_r_r_")));
		assertTrue( Permissions.WORLD_WRITEABLE.sameRights( 
				Permissions.parseString("rwrwrw")));

	}
	
	@Test
	public void testDelegationFunctionsProperly() throws Exception 
	{
		Permissions.DEFAULT.toString();
		Permissions.DEFAULT.identical(Permissions.EMPTY);
		Permissions.DEFAULT.isGranted(GROUP, READ);
		Permissions.DEFAULT.isSet(LOCKED);
		Permissions.DEFAULT.sameRights(p);
		try { Permissions.DEFAULT.grant(GROUP, READ);}
		catch (UnsupportedOperationException uoe) {};
		try { Permissions.DEFAULT.grantAll(Permissions.EMPTY);}
		catch (UnsupportedOperationException uoe) {};
		try { Permissions.DEFAULT.revoke(GROUP, READ);}
		catch (UnsupportedOperationException uoe) {};
		try { Permissions.DEFAULT.revokeAll(Permissions.EMPTY);}
		catch (UnsupportedOperationException uoe) {};
		try { Permissions.DEFAULT.set(LOCKED);}
		catch (UnsupportedOperationException uoe) {};
		try { Permissions.DEFAULT.unSet(LOCKED);}
		catch (UnsupportedOperationException uoe) {};	
	}
	
	// ~ Serialization
	// =========================================================================
	
	@Test
	public void testImmutableSerialization() throws Exception 
	{
		byte[] ser = serialize(Permissions.DEFAULT);
		p = deserialize(ser);
		p.grant(GROUP, READ); // is this what we want?
	}
	
	// ~ Parse String
	// =========================================================================
	@Test
	public void testParseString() throws Exception 
	{
		p = Permissions.parseString("rwrwrw");
		assertTrue( p.isGranted( USER, READ ));
		assertTrue( p.isGranted( USER, WRITE ));
		assertTrue( p.isGranted( GROUP, READ ));
		assertTrue( p.isGranted( GROUP, WRITE ));
		assertTrue( p.isGranted( WORLD, READ ));
		assertTrue( p.isGranted( WORLD, WRITE ));
	
		p = Permissions.parseString("RWRWRW");
		assertTrue( p.isGranted( USER, READ ));
		assertTrue( p.isGranted( USER, WRITE ));
		assertTrue( p.isGranted( GROUP, READ ));
		assertTrue( p.isGranted( GROUP, WRITE ));
		assertTrue( p.isGranted( WORLD, READ ));
		assertTrue( p.isGranted( WORLD, WRITE ));
	
		p = Permissions.parseString("______");
		assertFalse( p.isGranted( USER, READ ));
		assertFalse( p.isGranted( USER, WRITE ));
		assertFalse( p.isGranted( GROUP, READ ));
		assertFalse( p.isGranted( GROUP, WRITE ));
		assertFalse( p.isGranted( WORLD, READ ));
		assertFalse( p.isGranted( WORLD, WRITE ));
	
	}
	// ~ Private helpers
	// ===========================================================================

	private void revokeGrantCheck(Role role, Right right) {
		p.revoke(role, right);
		assertFalse(p.isGranted(role, right));
		p.grant(role, right);
		assertTrue(p.isGranted(role, right));
	}
	
	private void bitCompare(Role role, Right right)
	{
		Perms pp = new Perms().revoke(role,right);
		long l = pp.toLong();
		System.out.println(l+":"+Long.toBinaryString(l));
		long bit = (long) Perms.bit(role, right);
		System.out.println(bit+":"+Long.toBinaryString(bit));
		assertTrue( (l ^ bit) == -1L);

	}
		
	private Permissions deserialize(byte[] stream) throws Exception
	{
		ByteArrayInputStream bais;
		ObjectInputStream ois;
		
		bais = new ByteArrayInputStream(stream);
		ois = new ObjectInputStream(bais);
		Permissions p = (Permissions) ois.readObject();
		ois.close();
		bais.close();
		return p;
	}
	
	private byte[] serialize(Permissions p) throws Exception
	{
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
