package ome.server.utests.sec;

import java.util.concurrent.Callable;

import junit.framework.TestCase;

import ome.api.IQuery;
import ome.logic.QueryImpl;
import ome.security.BasicSecuritySystem;
import ome.security.JBossLoginModule;
import ome.security.SecuritySystem;
import ome.services.util.ServiceHandler;
import ome.system.EventContext;
import ome.system.ServiceFactory;
import ome.system.SimpleEventContext;
import ome.testing.MockServiceFactory;

import org.jmock.MockObjectTestCase;
import org.springframework.aop.framework.ProxyFactory;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

public class SecuritySystemTest extends MockObjectTestCase {

	ServiceFactory sf;
	EventContext ec;
	SecuritySystem secSys; 
	
    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception
    {
        super.setUp();
        
        ec = new SimpleEventContext();
        sf = new MockServiceFactory();
        secSys = new BasicSecuritySystem(sf,ec);
    }
    
    @Configuration(afterTestMethod = true)
    protected void tearDown() throws Exception
    {
        super.verify();
        super.tearDown();
    }
	
	@Test
	public void testSetCurrentDetails() throws Exception {
		secSys.setCurrentDetails();
	}
	
	@Test
	public void testTokenFunctionality() throws Exception {
		fail("who can do what");
	}
	
}
