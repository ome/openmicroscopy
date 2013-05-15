/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sessions;

import junit.framework.TestCase;
import ome.model.meta.Experimenter;
import ome.server.itests.ManagedContextFixture;
import ome.services.messages.stats.ObjectsReadStatsMessage;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4
 */
@Test(groups = "integration")
public class SessionStatsIntegrationTest extends TestCase {

    ManagedContextFixture fixture;
    
    @BeforeTest
    public void setup() {
        fixture = new ManagedContextFixture();
        fixture.loginNewUserDefaultGroup();
    }
    
    @Test
    public void testTooManyLoads() {
        final boolean[] called = new boolean[]{false};
        ApplicationEventMulticaster mc = (ApplicationEventMulticaster) fixture.ctx.getBean("applicationEventMulticaster");
        mc.addApplicationListener(new ApplicationListener(){
            public void onApplicationEvent(ApplicationEvent arg0) {
                if (arg0 instanceof ObjectsReadStatsMessage) {
                    called[0] = true;
                }
            }});
        for (int i = 0; i < 10001; i++) {
            fixture.managedSf.getQueryService().get(Experimenter.class, 1L);
        }
        assertTrue(called[0]);
    }

}
