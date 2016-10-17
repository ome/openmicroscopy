/*
 *   Copyright 2006-2016 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.utests;

import static omero.rtypes.rarray;
import static omero.rtypes.rbool;
import static omero.rtypes.rclass;
import static omero.rtypes.rdouble;
import static omero.rtypes.rfloat;
import static omero.rtypes.rint;
import static omero.rtypes.rlist;
import static omero.rtypes.rlong;
import static omero.rtypes.robject;
import static omero.rtypes.rset;
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import ome.model.annotations.AnnotationAnnotationLink;
import ome.model.annotations.CommentAnnotation;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.display.CodomainMapContext;
import ome.model.display.ChannelBinding;
import ome.model.display.ReverseIntensityContext;
import ome.model.meta.Event;
import ome.parameters.Parameters;
import omero.RArray;
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
import omero.model.AnnotationAnnotationLinkI;
import omero.model.CommentAnnotationI;
import omero.model.DatasetI;
import omero.model.EventI;
import omero.model.IObject;
import omero.model.ImageI;
import omero.model.PlaneSlicingContextI;
import omero.model.ProjectDatasetLinkI;
import omero.model.ProjectI;
import omero.model.ReverseIntensityContextI;
import omero.sys.ParametersI;
import omero.util.IceMapper;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AdapterTest extends TestCase {

    Project p;

    Dataset d;

    Image i;

    Pixels pix;

    @Override
    @BeforeMethod
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
        assertTrue(p_remote.copyDatasetLinks().size() == 1);
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
        assertTrue(p_remote.copyDatasetLinks().size() == 1);
        assertTrue(p_test.sizeOfDatasetLinks() == 1);
        ProjectDatasetLinkI pdl_remote = (ProjectDatasetLinkI) p_remote
                .copyDatasetLinks().get(0);
        ProjectDatasetLink pdl_test = (ProjectDatasetLink) mapper
                .reverse(pdl_remote);
        assertTrue(pdl_remote.getParent() == p_remote);
        assertTrue(pdl_test.parent() != p.collectDatasetLinks(null).get(0));

        omero.model.Dataset d_remote = pdl_remote.getChild();
        assertTrue(d_remote.sizeOfImageLinks() == 1);
        omero.model.DatasetImageLink dil_remote = d_remote.copyImageLinks()
                .get(0);
        assertTrue(dil_remote.getParent() == d_remote);
        omero.model.Image i_remote = dil_remote.getChild();
        assertTrue(i_remote.sizeOfPixels() == 1);
        omero.model.Pixels pix_remote = i_remote.copyPixels().get(0);
        assertTrue(pix_remote.getImage() == i_remote);
    }

    @Test
    public void testInheritance() throws Exception {

        IceMapper mapper = new IceMapper();

        ChannelBinding def = new ChannelBinding();
        CodomainMapContext cmc = new ReverseIntensityContext();
        cmc.setChannelBinding(def);

        ReverseIntensityContextI cmc_remote = (ReverseIntensityContextI) mapper
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
        assert (p_remote.sizeOfDatasetLinks() > 0);
    }

    @Test
    public void testUnloadedCollectionisReversedUnloaded() throws Exception {

        IceMapper mapper = new IceMapper();

        ProjectI p_remote = new ProjectI();
        p_remote.unloadDatasetLinks();

        Project p = (Project) mapper.reverse(p_remote);

        assert (p.sizeOfDatasetLinks() < 0);

    }

    @Test
    public void testUnloadedObjectisMappedUnloaded() throws Exception {

        IceMapper mapper = new IceMapper();

        CommentAnnotation pa = new CommentAnnotation();
        pa.addAnnotationAnnotationLink(new AnnotationAnnotationLink(1L, false));

        CommentAnnotationI pa_remote = (CommentAnnotationI) mapper.map(pa);
        assertFalse(pa_remote.copyAnnotationLinks().get(0).isLoaded());

    }

    @Test
    public void testUnloadedObjectIsReversedUnloaded() throws Exception {

        IceMapper mapper = new IceMapper();

        CommentAnnotationI pa_remote = new CommentAnnotationI();
        AnnotationAnnotationLinkI p_remote = new AnnotationAnnotationLinkI();
        p_remote.unload();
        pa_remote.addAnnotationAnnotationLink(p_remote);

        CommentAnnotation pa = (CommentAnnotation) mapper.reverse(pa_remote);
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
        assertFalse(p_remote.sizeOfDatasetLinks() > 0);

        // reverse
        p_remote = new ProjectI();
        p_remote.unloadDatasetLinks();
        assertFalse(p_remote.sizeOfDatasetLinks() > 0);
        mapper = new IceMapper();
        p = (Project) mapper.reverse(p_remote);
        assertTrue(p.sizeOfDatasetLinks() < 0);

    }

    @Test
    public void testParameterMapAndPojoOptions() throws Exception {
        Parameters po = new Parameters();
        po.leaves();
        po.exp(1L);

        RList rl = rlist(
                Arrays.<RType> asList(rstring("a"), rstring("b")));

        ParametersI p = new ParametersI();
        p.leaves();
        p.exp(rlong(1L));
        Map<String, RType> map = new HashMap<String, RType>();
        map.put("c", rbool(true));
        map.put("d", rlong(1L));

        IceMapper mapper = new IceMapper();
        Map reversed = mapper.reverse(map);
        Long l = (Long) reversed.get("d");
        assertEquals(l, po.getExperimenter());
        Boolean b = (Boolean) reversed.get("c");
        assertEquals(b, Boolean.valueOf(po.isLeaves()));
    }

    @Test
    public void testRTypes() throws Exception {
        IceMapper mapper = new IceMapper();
        // Nulls
        assertNull(mapper.fromRType((RString) null));
        assertNull(mapper.fromRType((RBool) null));
        assertNull(mapper.fromRType((RInt) null));
        assertNull(mapper.fromRType((RLong) null));
        assertNull(mapper.fromRType((RDouble) null));
        assertNull(mapper.fromRType((RClass) null));
        assertNull(mapper.fromRType((RFloat) null));
        assertNull(mapper.fromRType((RObject) null));
        assertNull(mapper.convert((RTime) null));
        assertNull(mapper.fromRType((RList) null));
        assertNull(mapper.fromRType((RSet) null));
        //
        assertEquals("a", mapper.fromRType(rstring("a")));
        assertEquals(1L, mapper.fromRType(rlong(1L)));
        assertEquals(1, mapper.fromRType(rint(1)));
        assertEquals(1.0, mapper.fromRType(rdouble(1.0)));
        assertEquals(1.0f, mapper.fromRType(rfloat(1.0f)));
        assertEquals(true, mapper.fromRType(rbool(true)));
        assertEquals(Image.class, mapper.fromRType(rclass("Image")));
        IObject obj = new ImageI(1L, false);
        Image img = (Image) mapper.fromRType(robject(obj));
        assertEquals(img.getId(), Long.valueOf(obj.getId().getValue()));
        RTime time = rtime(1L);
        Timestamp ts = mapper.convert(time);
        assertEquals(ts.getTime(), time.getValue());
        RArray jarr = rarray(rstring("A"));
        String[] strings = (String[]) mapper.fromRType(jarr);
        assertTrue(strings[0].equals("A"));
        RList jlist = rlist(Arrays.<RType> asList(rstring("L")));
        List stringList = (List) mapper.fromRType(jlist);
        assertTrue(stringList.contains("L"));
        RSet jset = rset(new HashSet<RType>(Arrays
                .<RType> asList(rstring("S"))));
        Set stringSet = (Set) mapper.fromRType(jset);
        assertTrue(stringSet.contains("S"));
    }

    @Test
    public void testMapsAreProperlyDispatched() throws Exception {
        IceMapper mapper = new IceMapper();
        Map m = new HashMap();
        m.put("a", rstring("a"));
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
