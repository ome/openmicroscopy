/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.utests;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import ome.model.annotations.AnnotationAnnotationLink;
import ome.model.annotations.TextAnnotation;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.display.CodomainMapContext;
import ome.model.display.PlaneSlicingContext;
import ome.model.display.RenderingDef;
import ome.model.meta.Event;
import ome.util.builders.PojoOptions;
import omero.JArray;
import omero.JBool;
import omero.JClass;
import omero.JDouble;
import omero.JFloat;
import omero.JInt;
import omero.JList;
import omero.JLong;
import omero.JObject;
import omero.JSet;
import omero.JString;
import omero.JTime;
import omero.RBool;
import omero.RClass;
import omero.RDouble;
import omero.RFloat;
import omero.RInt;
import omero.RList;
import omero.RLong;
import omero.RObject;
import omero.RSet;
import omero.RString;
import omero.RTime;
import omero.RType;
import omero.constants.POJOEXPERIMENTER;
import omero.constants.POJOLEAVES;
import omero.model.AnnotationAnnotationLinkI;
import omero.model.DatasetI;
import omero.model.EventI;
import omero.model.IObject;
import omero.model.ImageI;
import omero.model.PlaneSlicingContextI;
import omero.model.ProjectDatasetLinkI;
import omero.model.ProjectI;
import omero.model.TextAnnotationI;
import omero.util.IceMapper;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

public class AdapterTest extends TestCase {

    Project p;

    Dataset d;

    Image i;

    Pixels pix;

    @Override
    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception {
        p = new Project();
        d = new Dataset();
        i = new Image();
        pix = new Pixels();
    }

    @Test
    public void test_simple() throws Exception {
        IceMapper mapper = new IceMapper();
        ProjectI p_remote = (ProjectI) mapper.map(p);
        Project p_test = (Project) mapper.reverse(p_remote);
    }

    @Test
    public void test_with_values() throws Exception {
        p.setName("test");
        IceMapper mapper = new IceMapper();
        ProjectI p_remote = (ProjectI) mapper.map(p);
        Project p_test = (Project) mapper.reverse(p_remote);
        assertTrue("test".equals(p_remote.getName()));
        assertTrue("test".equals(p_test.getName()));
    }

    @Test
    public void test_with_collections() throws Exception {
        p.linkDataset(d);
        IceMapper mapper = new IceMapper();
        ProjectI p_remote = (ProjectI) mapper.map(p);
        Project p_test = (Project) mapper.reverse(p_remote);
        assertTrue(p_remote.datasetLinks.size() == 1);
        assertTrue(p_test.sizeOfDatasetLinks() == 1);
    }

    @Test
    public void test_complex() throws Exception {
        p.linkDataset(d);
        d.linkImage(i);
        i.addPixels(pix);
        IceMapper mapper = new IceMapper();
        ProjectI p_remote = (ProjectI) mapper.map(p);
        Project p_test = (Project) mapper.reverse(p_remote);
        assertTrue(p_remote.datasetLinks.size() == 1);
        assertTrue(p_test.sizeOfDatasetLinks() == 1);
        ProjectDatasetLinkI pdl_remote = (ProjectDatasetLinkI) p_remote.datasetLinks
                .get(0);
        ProjectDatasetLink pdl_test = (ProjectDatasetLink) mapper
                .reverse(pdl_remote);
        assertTrue(pdl_remote.parent == p_remote);
        assertTrue(pdl_test.parent() != p.collectDatasetLinks(null).get(0));

        omero.model.Dataset d_remote = pdl_remote.child;
        assertTrue(d_remote.imageLinks.size() == 1);
        omero.model.DatasetImageLink dil_remote = d_remote.imageLinks.get(0);
        assertTrue(dil_remote.parent == d_remote);
        omero.model.Image i_remote = dil_remote.child;
        assertTrue(i_remote.pixels.size() == 1);
        omero.model.Pixels pix_remote = i_remote.pixels.get(0);
        assertTrue(pix_remote.image == i_remote);
    }

    @Test
    public void testInheritance() throws Exception {

        IceMapper mapper = new IceMapper();

        RenderingDef def = new RenderingDef();
        CodomainMapContext cmc = new PlaneSlicingContext();
        cmc.setRenderingDef(def);

        PlaneSlicingContextI cmc_remote = (PlaneSlicingContextI) mapper
                .map(cmc);
        CodomainMapContext cmc_test = (CodomainMapContext) mapper
                .reverse(cmc_remote);
    }

    @Test
    public void testUnloadedCollectionIsMappedUnloaded() throws Exception {

        IceMapper mapper = new IceMapper();

        Project p = new Project();
        p.putAt(Project.DATASETLINKS, null);
        assertTrue(p.sizeOfDatasetLinks() < 0);

        ProjectI p_remote = (ProjectI) mapper.map(p);
        assert (p_remote.datasetLinks != null);
    }

    @Test
    public void testUnloadedCollectionisReversedUnloaded() throws Exception {

        IceMapper mapper = new IceMapper();

        ProjectI p_remote = new ProjectI();
        p_remote.datasetLinksLoaded = false;

        Project p = (Project) mapper.reverse(p_remote);

        assert (p.sizeOfDatasetLinks() < 0);

    }

    @Test
    public void testUnloadedObjectisMappedUnloaded() throws Exception {

        IceMapper mapper = new IceMapper();

        TextAnnotation pa = new TextAnnotation();
        pa.addAnnotationAnnotationLink(new AnnotationAnnotationLink(1L, false));

        TextAnnotationI pa_remote = (TextAnnotationI) mapper.map(pa);
        assertFalse(pa_remote.annotationLinks.iterator().next().loaded);

    }

    @Test
    public void testUnloadedObjectIsReversedUnloaded() throws Exception {

        IceMapper mapper = new IceMapper();

        TextAnnotationI pa_remote = new TextAnnotationI();
        AnnotationAnnotationLinkI p_remote = new AnnotationAnnotationLinkI();
        p_remote.unload();
        pa_remote.addAnnotationAnnotationLink(p_remote);

        TextAnnotation pa = (TextAnnotation) mapper.reverse(pa_remote);
        assertFalse(pa.iterateAnnotationLinks().next().isLoaded());
    }

    @Test(groups = "ticket:684")
    public void testNoDuplicateObjectsWithListsInsteadOfSets() throws Exception {
        p.linkDataset(d);
        IceMapper mapper = new IceMapper();
        ProjectI p_remote = (ProjectI) mapper.map(p);
        DatasetI d_remote = (DatasetI) mapper.map(d);

        long p_sz = p_remote.sizeOfDatasetLinks();
        long d_sz = d_remote.sizeOfProjectLinks();

        assertTrue(d_sz + "!=1", d_sz == 1L);
        assertTrue(p_sz + "!=1", p_sz == 1L);
    }

    @Test
    public void testUnloadedCollectionsRemainUnloaded() throws Exception {
        p.putAt(Project.DATASETLINKS, null);
        assertTrue(p.sizeOfDatasetLinks() < 0);
        IceMapper mapper = new IceMapper();
        ProjectI p_remote = (ProjectI) mapper.map(p);
        assertFalse(p_remote.datasetLinksLoaded);

        // reverse
        p_remote = new ProjectI();
        p_remote.unloadDatasetLinks();
        assertFalse(p_remote.datasetLinksLoaded);
        mapper = new IceMapper();
        p = (Project) mapper.reverse(p_remote);
        assertTrue(p.sizeOfDatasetLinks() < 0);

        // and if we just forget to set unload?
        p_remote = new ProjectI();
        p_remote.datasetLinks = null;
        assertTrue(p_remote.datasetLinksLoaded);
        mapper = new IceMapper();
        p = (Project) mapper.reverse(p_remote);
        // This is why you should use the accessors!
        // assertTrue(p.sizeOfDatasetLinks()<0);

    }

    @Test
    public void testParameterMapAndPojoOptions() throws Exception {
        PojoOptions po = new PojoOptions();
        po.leaves();
        po.exp(1L);

        RList rl = new RList();
        rl.val = Arrays.<RType> asList(new JString("a"), new JString("b"));

        Map<String, RType> map = new HashMap<String, RType>();
        map.put(POJOLEAVES.value, new JBool(true));
        map.put(POJOEXPERIMENTER.value, new JLong(1L));

        IceMapper mapper = new IceMapper();
        Map reversed = mapper.reverse(map);
        Long l = (Long) reversed.get(POJOEXPERIMENTER.value);
        assertEquals(l, po.getExperimenter());
        Boolean b = (Boolean) reversed.get(POJOLEAVES.value);
        assertEquals(b, Boolean.valueOf(po.isLeaves()));
    }

    @Test
    public void testRTypes() throws Exception {
        IceMapper mapper = new IceMapper();
        // Nulls
        assertNull(mapper.fromRType((JString) null));
        assertNull(mapper.fromRType((RString) null));
        assertNull(mapper.fromRType((JBool) null));
        assertNull(mapper.fromRType((RBool) null));
        assertNull(mapper.fromRType((JInt) null));
        assertNull(mapper.fromRType((RInt) null));
        assertNull(mapper.fromRType((JLong) null));
        assertNull(mapper.fromRType((RLong) null));
        assertNull(mapper.fromRType((JDouble) null));
        assertNull(mapper.fromRType((RDouble) null));
        assertNull(mapper.fromRType((JClass) null));
        assertNull(mapper.fromRType((RClass) null));
        assertNull(mapper.fromRType((JFloat) null));
        assertNull(mapper.fromRType((RFloat) null));
        assertNull(mapper.fromRType((JObject) null));
        assertNull(mapper.fromRType((RObject) null));
        assertNull(mapper.convert((JTime) null));
        assertNull(mapper.convert((RTime) null));
        assertNull(mapper.fromRType((JList) null));
        assertNull(mapper.fromRType((RList) null));
        assertNull(mapper.fromRType((JSet) null));
        assertNull(mapper.fromRType((RSet) null));
        //
        assertEquals("a", mapper.fromRType(new JString("a")));
        assertEquals(1L, mapper.fromRType(new JLong(1L)));
        assertEquals(1, mapper.fromRType(new JInt(1)));
        assertEquals(1.0, mapper.fromRType(new JDouble(1.0)));
        assertEquals(1.0f, mapper.fromRType(new JFloat(1.0f)));
        assertEquals(true, mapper.fromRType(new JBool(true)));
        assertEquals(Image.class, mapper.fromRType(new JClass("Image")));
        IObject obj = new ImageI(1L, false);
        Image img = (Image) mapper.fromRType(new JObject(obj));
        assertEquals(img.getId(), Long.valueOf(obj.id.val));
        JTime time = new JTime(1L);
        Timestamp ts = mapper.convert(time);
        assertEquals(ts.getTime(), time.val);
        JArray jarr = new JArray(new JString("A"));
        String[] strings = (String[]) mapper.fromRType(jarr);
        assertTrue(strings[0].equals("A"));
        JList jlist = new JList(Arrays.<RType> asList(new JString("L")));
        List stringList = (List) mapper.fromRType(jlist);
        assertTrue(stringList.contains("L"));
        JSet jset = new JSet(new HashSet<RType>(Arrays
                .<RType> asList(new JString("S"))));
        Set stringSet = (Set) mapper.fromRType(jset);
        assertTrue(stringSet.contains("S"));
    }

    @Test
    public void testMapsAreProperlyDispatched() throws Exception {
        IceMapper mapper = new IceMapper();
        Map m = new HashMap();
        m.put("a", new JString("a"));
        Map reversed = mapper.reverse(m);
        assertTrue(reversed.get("a").equals("a"));
    }

    @Test(groups = "ticket:737")
    public void testEventsAndTimes() throws Exception {
        Event e = new Event();
        Timestamp t = new Timestamp(System.currentTimeMillis());
        e.setTime(t);
        IceMapper mapper = new IceMapper();
        EventI ei = (EventI) mapper.map(e);
        assertNotNull(ei.getTime());
    }
}
