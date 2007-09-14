/*   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.fixtures;

import java.util.HashMap;
import java.util.Map;

import ome.api.local.LocalAdmin;
import ome.security.MethodSecurity;
import ome.security.SecuritySystem;
import ome.services.blitz.Main;
import ome.services.blitz.Router;
import ome.services.blitz.client.IceServiceFactory;
import ome.system.OmeroContext;
import ome.system.Roles;
import ome.tools.spring.StubBeanPostProcessor;
import omeis.providers.re.RenderingEngine;
import omero.api.ServiceFactoryPrx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * Note: Using the {@link Router} wrapper class can cause processes to
 * be orphaned on the OS.
 */
public class BlitzServerFixture extends MockObjectTestCase {

	private static final Log log = LogFactory.getLog(BlitzServerFixture.class);

	private static final Map<String, Object> STUBS = StubBeanPostProcessor.stubs;

	private static final Map<String, Mock> MOCKS = new HashMap<String, Mock>();
	
	public void addMock(String name, Class iface) {
		MOCKS.put(name, mock(iface));
		STUBS.put(name, MOCKS.get(name).proxy());
	}
	
	public Mock getMock(String name) {
		return MOCKS.get(name);
	}
	
	public Object getStub(String name) {
		return STUBS.get(name);
	}
	
	// Name used to look up the test context.
	static String DEFAULT = "OMERO.blitz.test"; 
	String name;
	Thread t;
	Main m;
	Router r;
	IceServiceFactory ice;

	int sessionTimeout = 30, serviceTimeout = 10;

	// Keys for the mocks that are known to be needed
	String adm = "internal:ome.api.IAdmin", ss = "securitySystem",
	re = "managed:omeis.providers.re.RenderingEngine", 
	ms = "methodSecurity";
	public Mock getAdmin() { return MOCKS.get(adm); }
	public Mock getSecSystem() { return MOCKS.get(ss); }
	public Mock getRndEngine() { return MOCKS.get(re); }
	public Mock getMethodSecurity() { return MOCKS.get(ms); }
	
	public void setSessionTimeout(int st) {
		sessionTimeout = st;
	}

	public void setServiceTimeout(int st) {
		serviceTimeout = st;
	}

	public BlitzServerFixture() { this(DEFAULT);}
	public BlitzServerFixture(String contextName) { 
		this.name = contextName; 
		// Stubs will be replaced by the bean post processor
		addMock(adm, LocalAdmin.class);
		addMock(ss, SecuritySystem.class);
		addMock(re, RenderingEngine.class);
		addMock(ms, MethodSecurity.class);
	}
	
	public void startServer() {
		// Set property before the OmeroContext is created
		System.setProperty("omero.blitz.cache.timeToIdle", "" + serviceTimeout);

		m = new Main(name);
		t = new Thread(m);
		r = new Router();
		r.setTimeout(sessionTimeout);
		r.allowAdministration();
		m.setRouter(r);
		t.start();
		assertTrue("Startup must succeed", m.waitForStartup());

		log
				.warn("\n"
						+ "============================================================\n"
						+ "Use ONLY Ctrl-C or 'q' in the console to cancel this process\n"
						+ "============================================================");
	}

	public void prepareLogin() {
		getMock(adm).expects(once()).method("checkPassword").will(
				returnValue(true));
		getMock(ss).expects(once()).method("getSecurityRoles").will(
				returnValue(new Roles()));
	}

	public ServiceFactoryPrx createSession() throws Exception {
		prepareLogin();
		ice = new IceServiceFactory(null, null, null);
		ice.createSession();
		ServiceFactoryPrx session = ice.getProxy();
		return session;
	}

	public void methodCall() throws Exception {
		getMock(ms).expects(once()).method("isActive").will(returnValue(true));
		getMock(ms).expects(once()).method("checkMethod");
		getMock(ss).expects(once()).method("login");
		getMock(ss).expects(once()).method("logout");
		getMock(re).expects(once()).method("close");
	}
	
	public void destroySession() throws Exception {
		ice.destroy();
	}

	public void tearDown() throws Exception {
		try {
			super.tearDown();
		} finally {
			Ice.Communicator ic = (Ice.Communicator) OmeroContext.getInstance(
				name).getBean("Ice.Communicator");
			m.stop();
		}
	}

}