package ome.server.utests.sec;

import java.util.concurrent.Callable;

import junit.framework.TestCase;

import ome.security.JBossLoginModule;

import org.testng.annotations.Test;

public class LoginModuleTest extends TestCase {

	@Test
	public void testNullPasswords() throws Exception {
		
		class Test 
		extends JBossLoginModule 
		implements Callable<Boolean> {
			public String password;
			public Test(){}
			public Test(String password){this.password=password;}
			public Boolean call() throws Exception {
				return this.validatePassword(null, password);
			}
		};
		
		assertFalse( new Test().call() );
		assertTrue( new Test("").call() );
		assertTrue( new Test("     ").call() );
		
	}
	
}
