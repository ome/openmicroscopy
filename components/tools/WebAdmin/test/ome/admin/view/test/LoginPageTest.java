package ome.admin.view.test;

import net.sourceforge.jwebunit.junit.WebTestCase;
import net.sourceforge.jwebunit.util.TestingEngineRegistry;

public class LoginPageTest extends WebTestCase {

	public void setUp() throws Exception {
		setTestingEngineKey(TestingEngineRegistry.TESTING_ENGINE_HTMLUNIT);
		getTestContext().setBaseUrl("http://localhost:8080/WebAdmin");
	}

	/**
	 * test login form
	 */
	public void testLoginForm() {
		beginAt("/login.jsf");
		assertFormPresent("loginForm");
		assertSubmitButtonPresent("loginForm:submit");
		assertFormElementPresent("loginForm:server");
		assertFormElementEquals("loginForm:server", "localhost");
		assertFormElementPresent("loginForm:port");
		assertFormElementEquals("loginForm:port", "1099");
		setTextField("loginForm:username", "root");
		setTextField("loginForm:password", "ome");
		submit("loginForm:submit");
	}
	
	/**
	 * test login form for wrong login param
	 */
	public void testLoginForm1() {
		beginAt("/login.jsf");
		assertFormPresent("loginForm");
		assertSubmitButtonPresent("loginForm:submit");
		assertFormElementPresent("loginForm:server");
		assertFormElementEquals("loginForm:server", "localhost");
		assertFormElementPresent("loginForm:port");
		assertFormElementEquals("loginForm:port", "1099");
		setTextField("loginForm:username", "root");
		setTextField("loginForm:password", "aaa");
		submit("loginForm:submit");
		
        assertTextPresent("Invalid Login Params: Authentication failure");
	}
	
}
