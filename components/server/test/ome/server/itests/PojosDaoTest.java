/*
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import ome.api.IQuery;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.parameters.Parameters;
import ome.system.OmeroContext;
import ome.system.ServiceFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * tests for an up-and-coming pojos data access
 * "PojosQuerySourceTest"
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 2.0
 */
// FIXME
//TODO rename
@Test(groups = "integration")
public class PojosDaoTest extends TestCase {

    private static Logger log = LoggerFactory.getLogger(PojosDaoTest.class);

    protected OmeroContext applicationContext;

    @BeforeClass
    protected void onSetUp() throws Exception {
        this.applicationContext = OmeroContext.getManagedServerContext();
        _q = new ServiceFactory((OmeroContext) applicationContext)
                .getQueryService();
        po = new Parameters().exp(1L);
        ids = new HashSet<Integer>(Arrays.asList(new Integer[] { 1, 2, 3, 4, 5,
                6, 250, 253, 249, 258 }));
        m = new HashMap();
        m.put("id_list", ids);
        m.put("exp", po.getExperimenter());
    }

    IQuery _q;

    Set s;

    String q;

    Parameters po;

    Set<Integer> ids;

    Map m = new HashMap();

    String n;

    private void runLoad(String name, Class c) {
        // Class, Set<Container>, options
        // q = PojosQueryBuilder.buildLoadQuery(c,false,po.map());
        n = name;
        go();
    }

    @Test
    public void testLoadProject() {
        runLoad("Load_p", Project.class);
    }

    @Test
    public void testLoadDataset() {
        runLoad("Load_d", Dataset.class);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`

    private void runFind(String name, Class c) {
        // Class, Set<Image>, options
        // q = PojosQueryBuilder.buildFindQuery(c,po.map());
        n = name;
        go();
    }

    @Test
    public void testFindProject() {
        runFind("Find_p", Project.class);
    }

    @Test
    public void testFindDataset() {
        runFind("Find_d", Dataset.class);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`

    private void runAnn(String name, Class c) {
        // Class, Set<Container>, Map
        // q = PojosQueryBuilder.buildAnnsQuery(c,po.map());
        m.remove("exp"); // unused
        n = name;
        go();
    }

    @Test
    public void testDatasetAnn() {
        runAnn("ann_d", Dataset.class);
    }

    @Test
    public void testImageAnn() {
        runAnn("ann_i", Image.class);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`

    private void runGet(String name, Class c) {
        // Class, Set<Container>, Map
        // q = PojosQueryBuilder.buildGetQuery(c,po.map());
        n = name;
        go();
    }

    @Test
    public void testGetFromProject() {
        runGet("get_p", Project.class);
    }

    @Test
    public void testGetFromDataset() {
        runGet("get_p", Dataset.class);
    }

    // TODO how to run getUserImages
    @Test
    public void testGetUser() {
        m.remove("id_list");
        runGet("get_user", Image.class); // TODO make nicer; here
                                            // Image.class=>noIds in template
                                            // could just po.set("noIds")
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`

    private void runPaths(String name, String algorithm) {
        // Set<Image>, Algorithm options
        // q = PojosQueryBuilder.buildPathsQuery(algorithm,po.map());
        n = name;
        go();
    }

    @Test
    public void testPathsInc() {
        runPaths("inc_path", "INCLUSIVE");
    }

    @Test
    public void testPathsExc() {
        runPaths("exc_path", "EXCLUSIVE");
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`

    private void go() {
        log.info(String.format("%n1)NAME: %s%n2)QUERY: %s", n, q));
        // s = new HashSet(_q.queryListMap(q,m));
        log.info(String.format("%n3)RESULT: %s", s));
    }

}
