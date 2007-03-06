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
import ome.services.icy.client.IceServiceFactory;
import omero.ApiUsageException;
import omero.JInt;
import omero.JString;
import omero.RInt;
import omero.RString;
import omero.api.IConfigPrx;
import omero.api.IConfigPrxHelper;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.IUpdatePrxHelper;
import omero.api.RenderingEnginePrx;
import omero.constants.CONFIGSERVICE;
import omero.constants.UPDATESERVICE;
import omero.model.Experimenter;
import omero.model.ExperimenterI;
import omero.model.ImageI;
import omero.sys.Filter;
import omero.sys.Parameters;
import omero.sys.QueryParam;
import omero.sys.Type;

import org.testng.annotations.Test;

public class QueryTest extends IceTest {

    @Test
    public void testGet() throws Exception {

        IUpdatePrx up = ice.getUpdateService(null);
        IQueryPrx qu = ice.getQueryService(null);

        ImageI i = new ImageI();
        i.name = new RString(false, "foo");

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
 
        IUpdatePrx up = ice.getUpdateService(null);
        IQueryPrx qu = ice.getQueryService(null);

        ImageI i = new ImageI();
        i.name = new RString(false, "foo");

        i = (ImageI) up.saveAndReturnObject(i);

        ImageI t;

        t = (ImageI) qu.find("Image", i.id.val);
        assertNotNull(t);
        t = (ImageI) qu.find("Image", -1);
        assertNull(t);
    }
    
    @Test
    public void testFindAll() throws Exception {
        
        IQueryPrx qu = ice.getQueryService(null);
        Filter f = new Filter();
        f.limit = new JInt(1);
        List<Experimenter> l = qu.findAll("Experimenter", f);
        assertTrue(l.size()==1);
        assertNotNull(l.get(0).omeName);

    }
    
    @Test
    public void testFindAllByExample() throws Exception {
        
        IQueryPrx qu = ice.getQueryService(null);
        ExperimenterI ex = new ExperimenterI();
        ex.omeName = new JString("root");
        List<ExperimenterI> l = qu.findAllByExample(ex, null);
        assertTrue(l.size() == 1);
        assertTrue(l.get(0).omeName.val.equals("root"));
        assertTrue(l.get(0).id.val == 0L);
        
    }
    
    @Test
    public void testFindAllByQuery() throws Exception {
        
        IQueryPrx qu = ice.getQueryService(null);
        String str = "select e from Experimenter e where e.omeName = :name";
        Parameters p = new Parameters();
        p.f = new Filter();
        p.f.limit = new JInt(1);
        p.p = new HashMap();
        QueryParam name = new QueryParam();
        name.name = "name";
        name.stringVal = "root";
        name.paramType = Type.stringType;
        p.p.put("name", name);
        List<ExperimenterI> l = qu.findAllByQuery(str,p);
        assertTrue(l.size() == 1);
        assertTrue(l.get(0).omeName.val.equals("root"));
        assertTrue(l.get(0).id.val == 0L);
        
    }

    @Test
    public void testFindAllByString() throws Exception {
        
        IQueryPrx qu = ice.getQueryService(null);
        List<ExperimenterI> l = qu.findAllByString("Experimenter", "omeName", "root", true, null);
        assertTrue(l.size() == 1);
        assertTrue(l.get(0).omeName.val.equals("root"));
        assertTrue(l.get(0).id.val == 0L);
        
    }
    
    @Test
    public void testFindExample() throws Exception {
        
        IQueryPrx qu = ice.getQueryService(null);
        List<ExperimenterI> l = qu.findAllByString("Experimenter", "omeName", "root", true, null);
        assertTrue(l.size() == 1);
        assertTrue(l.get(0).omeName.val.equals("root"));
        assertTrue(l.get(0).id.val == 0L);
        
    }
}
