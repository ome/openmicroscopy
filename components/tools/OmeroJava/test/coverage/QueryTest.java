/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package coverage;

import java.util.HashMap;
import java.util.List;

import static omero.rtypes.*;
import omero.ApiUsageException;
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
        i.setName(rstring("foo"));

        i = (ImageI) up.saveAndReturnObject(i);

        ImageI t;

        t = (ImageI) qu.get("Image", i.getId().getValue());
        t = (ImageI) qu.get("ImageI", i.getId().getValue());
        t = (ImageI) qu.get("omero.model.Image", i.getId().getValue());
        t = (ImageI) qu.get("omero.model.ImageI", i.getId().getValue());
        try {
            t = (ImageI) qu.get("ome.model.core.Image", i.getId().getValue());
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
        i.setName(rstring("foo"));

        i = (ImageI) up.saveAndReturnObject(i);

        ImageI t;

        t = (ImageI) qu.find("Image", i.getId().getValue());
        assertNotNull(t);
        t = (ImageI) qu.find("Image", -1);
        assertNull(t);
    }

    @Test
    public void testFindAll() throws Exception {

        IQueryPrx qu = ice.getServiceFactory().getQueryService();
        Filter f = new Filter();
        f.limit = rint(1);
        List<ExperimenterI> l = ExperimenterI.cast(qu
                .findAll("Experimenter", f));
        assertTrue(l.size() == 1);
        assertNotNull(l.get(0).getOmeName());

    }

    @SuppressWarnings("unchecked")
    <T> List<T> castList(List list) {
        return list;
    }

    @Test
    public void testFindAllByExample() throws Exception {

        IQueryPrx qu = ice.getServiceFactory().getQueryService();
        ExperimenterI ex = new ExperimenterI();
        ex.setOmeName(rstring("root"));
        List<ExperimenterI> l = ExperimenterI.cast(qu
                .findAllByExample(ex, null));
        assertTrue(l.size() == 1);
        assertTrue(l.get(0).getOmeName().getValue().equals("root"));
        assertTrue(l.get(0).getId().getValue() == 0L);

    }

    @Test
    public void testFindAllByQuery() throws Exception {

        IQueryPrx qu = ice.getServiceFactory().getQueryService();
        String str = "select e from Experimenter e where e.omeName = :name";
        Parameters p = new Parameters();
        p.theFilter = new Filter();
        p.theFilter.limit = rint(1);
        p.map = new HashMap();
        p.map.put("name", rstring("root"));
        List<ExperimenterI> l = ExperimenterI.cast(qu.findAllByQuery(str, p));
        assertTrue(l.size() == 1);
        assertTrue(l.get(0).getOmeName().getValue().equals("root"));
        assertTrue(l.get(0).getId().getValue() == 0L);

    }

    @Test
    public void testFindAllByString() throws Exception {

        IQueryPrx qu = ice.getServiceFactory().getQueryService();
        List<ExperimenterI> l = ExperimenterI.cast(qu.findAllByString(
                "Experimenter", "omeName", "root", true, null));
        assertTrue(l.size() == 1);
        assertTrue(l.get(0).getOmeName().getValue().equals("root"));
        assertTrue(l.get(0).getId().getValue() == 0L);

    }

    @Test
    public void testFindExample() throws Exception {

        IQueryPrx qu = ice.getServiceFactory().getQueryService();
        List<ExperimenterI> l = ExperimenterI.cast(qu.findAllByString(
                "Experimenter", "omeName", "root", true, null));
        assertTrue(l.size() == 1);
        assertTrue(l.get(0).getOmeName().getValue().equals("root"));
        assertTrue(l.get(0).getId().getValue() == 0L);

    }
}
