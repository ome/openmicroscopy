/*
 * Created on May 23, 2005
*/
package org.ome.tests.client;

import java.util.Set;

import junit.framework.TestCase;

/**
 * @author josh
 */
public class UtilsTest extends TestCase {

    public void testGetObjectVoidMethods() {
        String[] names = Utils.getObjectVoidMethods(OmeroPercentTest.class);
    }

}
