/*
 * ome.formats.testclient.ExampleUnitTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.testclient;

import java.io.File;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ResourceUtils;
import org.testng.annotations.Test;

import ome.formats.OMEROMetadataStore;
import ome.formats.importer.ImportFixture;
import ome.formats.importer.ImportLibrary;
import ome.model.containers.Dataset;
import ome.system.Login;
import ome.system.ServiceFactory;

import junit.framework.TestCase;

@Test
public class ExampleUnitTest extends TestCase {

    private final static Log log = LogFactory.getLog(ExampleUnitTest.class);

    public void test() throws Exception {
    }

}
