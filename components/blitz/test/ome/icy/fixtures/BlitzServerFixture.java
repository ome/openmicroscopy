/*   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.fixtures;

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

	public Mock adminMock, secSysMock, reMock, msMock;

	Thread t;
	Main m;
	Router r;
	IceServiceFactory ice;

	int sessionTimeout = 30, serviceTimeout = 10;

	{
		adminMock = mock(LocalAdmin.class);
		secSysMock = mock(SecuritySystem.class);
		reMock = mock(RenderingEngine.class);
		msMock = mock(MethodSecurity.class);

		// Stubs will be replaced by the bean post processor
		STUBS.put("internal:ome.api.IAdmin", adminMock.proxy());
		STUBS.put("securitySystem", secSysMock.proxy());
		STUBS.put("managed:omeis.providers.re.RenderingEngine", reMock.proxy());
		STUBS.put("methodSecurity", msMock.proxy());
	}

	public void setSessionTimeout(int st) {
		sessionTimeout = st;
	}

	public void setServiceTimeout(int st) {
		serviceTimeout = st;
	}

	public void startServer() {
		// Set property before the OmeroContext is created
		System.setProperty("omero.blitz.cache.timeToIdle", "" + serviceTimeout);

		m = new Main("OMERO.blitz.test");
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
		adminMock.expects(once()).method("checkPassword").will(
				returnValue(true));
		secSysMock.expects(once()).method("getSecurityRoles").will(
				returnValue(new Roles()));
	}

	public ServiceFactoryPrx createSession() throws Exception {
		prepareLogin();
		ice = new IceServiceFactory(null, null, null);
		ice.createSession();
		ServiceFactoryPrx session = ice.getProxy();
		return session;
	}

	public void destroySession() throws Exception {
		ice.destroy();
	}

	public void tearDown() throws Exception {
		try {
			super.tearDown();
		} finally {
			Ice.Communicator ic = (Ice.Communicator) OmeroContext.getInstance(
				"OMERO.blitz.test").getBean("Ice.Communicator");
			m.stop();
		}
	}

}