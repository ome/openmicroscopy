/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.utests;

import org.jmock.MockObjectTestCase;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;

@Test(groups = { "unit", "spring" })
public class GlobalMulticasterTest extends MockObjectTestCase {

    @Test
    public void testTwoContexts() throws Throwable {

        ClassPathXmlApplicationContext ac1 = new ClassPathXmlApplicationContext(
                new String[] { "classpath:ome/services/messaging.xml",
                        "classpath:ome/services/utests/GlobalMulticasterTest1.xml" });
        ClassPathXmlApplicationContext ac2 = new ClassPathXmlApplicationContext(
                new String[] { "classpath:ome/services/messaging.xml",
                        "classpath:ome/services/utests/GlobalMulticasterTest2.xml" },
                ac1);
        ac2.publishEvent(new GMEvent(this));
        ac1.publishEvent(new GMEvent(this));

        GMBean one = (GMBean) ac1.getBean("one");
        GMBean two = (GMBean) ac2.getBean("two");

        assertEquals(3, one.invoked);
        assertEquals(3, two.invoked);

    }

    static class GMEvent extends ApplicationEvent {
        GMEvent(Object source) {
            super(source);
        }
    }

    static class GMBean implements ApplicationListener {

        public int invoked = 0;

        public void onApplicationEvent(ApplicationEvent event) {
            if (event instanceof GMEvent) {
                invoked++;
            }
        }

    }

}
