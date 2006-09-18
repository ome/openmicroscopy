package ome.server.itests;

import java.util.UUID;

import ome.api.IConfig;

import org.testng.annotations.*;

/**
 * simple server-side test of the ome.api.IConfig service.
 * 
 * Also used as the main developer example for developing (stateless/client-side) 
 * tests. See source code documentation for more.
 *
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @since   3.0-M3
 */
/* Developer notes:
 * ---------------
 * The "integration" group is important for having tests run properly. 
 * It's not actually needed here because it's also defined on the super class. 
 * The "ticket:306" group works as a pointer to why certain tests were created, 
 * allowing for acceptance test-like functionality (TBD).
 * 
 * It should be noted that server side integration tests DO NOT test the 
 * application server in any way. You are using the server-side code as a 
 * library at that point and no deployment has been made. (This may change 
 * if we start to use an embedded container like the one available from JBoss 
 * for testing, in which case there should be an AbstractEmbeddedContextTest)
 * 
 *  Also to explain, the AbstractInternalContextTest which is a close parallel
 *  to AbstractManagedContextTest is partially deprecated and should only 
 *  really be used when testing specific parts of the internal plumbing of
 *  Omero, Spring, or Hibernate, such as flushing or transactions.
 */
@Test( groups = {"ticket:306", "config","integration"} )
public class ConfigTest extends AbstractManagedContextTest
{

	/* Developer notes:
	 * ---------------
	 * The name of the method is unimportant for testng. We continue to use 
	 * test<name> for the possibility of having the tests also run onJunit 3.8. 
	 */
	@Test
	public void testConfigGetServerTime() throws Exception {
		
		/* Developer notes:
		 * ---------------
		 * An iConfig instance has also be set on the super class as has been 
		 * done with almost all our interfaces. It is reset before each method
		 * call
		 */
		IConfig test = factory.getConfigService();
		
		assertNotNull( test.getServerTime() );
	}
	
	@Test
	public void testConfigGetDBTime() throws Exception {
		/* Developer notes:
		 * ---------------
		 * Using the instance variable
		 */
		assertNotNull( iConfig.getDatabaseTime() );
	}
	
	@Test
	public void testGetMissingConfigValue() throws Exception {

		/* Developer notes:
		 * ---------------
		 * HIGHLY unlikely that the UUID string will be available
		 */ 
		String value = iConfig.getConfigValue( UUID.randomUUID().toString() );
		assertNull( value );
		
	}
	
	/* Developer notes:
	 * ---------------
	 * Tests that should not be run, either because they're not yet finished, or 
	 * because they're waiting on a known issue, can be added to the "ignore" or
	 * "broken" group or marked "enabled = false"
	 */
	@Test( groups = "ignore" )
	public void testThisTestUsuallyWontBeCalled() throws Exception {
		fail("This usually won't be called. You're probably using " +
				"the developer test suite; don't worry about this.");
	}
	
	@Test( enabled = false )
	public void testThisTestWontBeCalled() throws Exception {
		fail("Error in TestNG!");
	}
	
    
}
