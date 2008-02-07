/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.itests.coverage;

import java.util.HashMap;
import java.util.List;

import ome.icy.model.itests.IceTest;
import omero.ApiUsageException;
import omero.JInt;
import omero.JString;
import omero.RString;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.model.ExperimenterI;
import omero.model.ImageI;
import omero.sys.Filter;
import omero.sys.Parameters;

import org.testng.annotations.Test;

public class QueryTest extends IceTest {

    @Test
    public void testGet() throws Exception {

        IUpdatePrx up = ice.getServiceFactory().getUpdateService();
        IQueryPrx qu = ice.getServiceFactory().getQueryService();

        ImageI i = new ImageI();
        i.name = new RString("foo");

        i = (ImageI) up.saveAndReturnObject(i);

        ImageI t;

        t = (ImageI) qu.get("Image", i.id.val);
        t = (ImageI) qu.get("ImageI", i.id.val);
        t = (ImageI) qu.get("omero.model.Image", i.id.val);
        t = (ImageI) qu.get("omero.model.ImageI", i.id.val);
        try {
            t = (ImageI) qu.get("ome.model.core.Image", i.id.val);
            fail("shouldn't work.");
        } catch (ApiUsageException e) {
            // ok
        }
    }

    @Test
    public void testFind() throws Exception {

        IUpdatePrx up = ice.getServiceFactory().getUpdateService();
        IQueryPrx qu = ice.getServiceFactory().getQueryService();

        ImageI i = new ImageI();
        i.name = new RString("foo");

        i = (ImageI) up.saveAndReturnObject(i);

        ImageI t;

        t = (ImageI) qu.find("Image", i.id.val);
        assertNotNull(t);
        t = (ImageI) qu.find("Image", -1);
        assertNull(t);
    }

    @Test
    public void testFindAll() throws Exception {

        IQueryPrx qu = ice.getServiceFactory().getQueryService();
        Filter f = new Filter();
        f.limit = new JInt(1);
        List<ExperimenterI> l = ExperimenterI.cast(qu
                .findAll("Experimenter", f));
        assertTrue(l.size() == 1);
        assertNotNull(l.get(0).omeName);

    }

    @SuppressWarnings("unchecked")
    <T> List<T> castList(List list) {
        return list;
    }

    @Test
    public void testFindAllByExample() throws Exception {

        IQueryPrx qu = ice.getServiceFactory().getQueryService();
        ExperimenterI ex = new ExperimenterI();
        ex.omeName = new JString("root");
        List<ExperimenterI> l = ExperimenterI.cast(qu
                .findAllByExample(ex, null));
        assertTrue(l.size() == 1);
        assertTrue(l.get(0).omeName.val.equals("root"));
        assertTrue(l.get(0).id.val == 0L);

    }

    @Test
    public void testFindAllByQuery() throws Exception {

        IQueryPrx qu = ice.getServiceFactory().getQueryService();
        String str = "select e from Experimenter e where e.omeName = :name";
        Parameters p = new Parameters();
        p.theFilter = new Filter();
        p.theFilter.limit = new JInt(1);
        p.map = new HashMap();
        p.map.put("name", new JString("root"));
        List<ExperimenterI> l = ExperimenterI.cast(qu.findAllByQuery(str, p));
        assertTrue(l.size() == 1);
        assertTrue(l.get(0).omeName.val.equals("root"));
        assertTrue(l.get(0).id.val == 0L);

    }

    @Test
    public void testFindAllByString() throws Exception {

        IQueryPrx qu = ice.getServiceFactory().getQueryService();
        List<ExperimenterI> l = ExperimenterI.cast(qu.findAllByString(
                "Experimenter", "omeName", "root", true, null));
        assertTrue(l.size() == 1);
        assertTrue(l.get(0).omeName.val.equals("root"));
        assertTrue(l.get(0).id.val == 0L);

    }

    @Test
    public void testFindExample() throws Exception {

        IQueryPrx qu = ice.getServiceFactory().getQueryService();
        List<ExperimenterI> l = ExperimenterI.cast(qu.findAllByString(
                "Experimenter", "omeName", "root", true, null));
        assertTrue(l.size() == 1);
        assertTrue(l.get(0).omeName.val.equals("root"));
        assertTrue(l.get(0).id.val == 0L);

    }
}
