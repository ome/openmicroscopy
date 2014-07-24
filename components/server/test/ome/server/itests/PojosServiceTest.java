/*
 * ome.server.itests.PojosServiceTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import ome.api.IContainer;
import ome.conditions.ApiUsageException;
import ome.model.ILink;
import ome.model.IObject;
import ome.model.annotations.CommentAnnotation;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.screen.Plate;
import ome.model.screen.Screen;
import ome.parameters.Parameters;
import ome.testing.OMEData;
import ome.testing.ObjectFactory;
import ome.util.CBlock;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class PojosServiceTest extends AbstractManagedContextTest {

    protected IContainer iContainer;

    protected OMEData data;

    @BeforeClass
    protected void setup() throws Exception {
        DataSource dataSource = (DataSource) applicationContext
                .getBean("dataSource");
        data = new OMEData();
        data.setDataSource(dataSource);
        iContainer = factory.getContainerService();
    }

    @Test
    public void test_unannotated_Event_version() throws Exception {
        ILink link = createLinkedCommentAnnotation();
        iContainer.deleteDataObject(link, null);

    }

    @Test(groups = "ticket:318")
    public void testLoadHiearchiesHandlesNullRootNodeIds() throws Exception {
        Parameters po;

        try {
            iContainer.loadContainerHierarchy(Project.class, null, null);
            fail("Should throw ApiUsage.");
        } catch (ApiUsageException aue) {
            // ok
        }
        po = new Parameters().exp(0L);
        iContainer.loadContainerHierarchy(Project.class, null, po);

        po = new Parameters().grp(0L);
        iContainer.loadContainerHierarchy(Project.class, null, po);

    }

    @Test(groups = "ticket:657")
    public void testAnnotationsStillCounted() throws Exception {
        Dataset d = new Dataset();
        d.setName("ticket:657");
        CommentAnnotation da = new CommentAnnotation();
        da.setTextValue("ticket:657");
        da.setNs("");
        d.linkAnnotation(da);
        Image i = new_Image();
        i.setName("ticket:657");
        i.linkDataset(d);
        CommentAnnotation ia = new CommentAnnotation();
        ia.setTextValue("ticket:657");
        ia.setNs("");
        i.linkAnnotation(ia);

        d = iUpdate.saveAndReturnObject(d);

        Set<Image> list = iContainer.getImages(Dataset.class, Collections
                .singleton(d.getId()), null);

        i = list.iterator().next();
        // d = i.linkedDatasetList().get(0);
        // No longer joined

        long self = this.iAdmin.getEventContext().getCurrentUserId();
        // Aren't returned now by getImages
        // assertTrue(d.getAnnotationLinksCountPerOwner() != null);
        // assertTrue(d.getAnnotationLinksCountPerOwner().get(self).equals(1L));
        assertTrue(i.getAnnotationLinksCountPerOwner() != null);
        assertTrue(i.getAnnotationLinksCountPerOwner().get(self).equals(1L));

    }

    @Test(groups = "ticket:651")
    public void testIntervalInParameters() {

        // Previously this was throwing an OOM
        Long userID = loginNewUser().getId();

        // create
        Dataset ds = new Dataset();
        ds.setName("ticket:651");
        Image im = new_Image();
        im.setName("ticket:651");
        Pixels pi = ObjectFactory.createPixelGraph(null);
        im.addPixels(pi);
        ds.linkImage(im);
        ds = iUpdate.saveAndReturnObject(ds);

        // test
        Timestamp startTime = getDate("before");
        Timestamp endTime = getDate("after");

        Parameters options = new Parameters();
        options.exp(userID);
        options.startTime(startTime);
        options.endTime(endTime);

        iContainer.getImagesByOptions(options);
        iContainer.getImages(Dataset.class, Collections.singleton(ds.getId()),
                options);
    }

    @Test(groups = "ticket:1018")
    public void testIntervalPojoMethodsReturnsCounts() {

        // Previously this was throwing an OOM
        Long userID = loginNewUser().getId();

        // create
        Dataset ds = new Dataset();
        ds.setName("ticket:1018");
        Image im = new_Image();
        im.setName("ticket:1018");
        Pixels pi = ObjectFactory.createPixelGraph(null);
        im.addPixels(pi);
        ds.linkImage(im);
        ds = iUpdate.saveAndReturnObject(ds);

        // test
        Timestamp startTime = getDate("before");
        Timestamp endTime = getDate("after");

        Parameters options = new Parameters();
        options.exp(userID);
        options.startTime(startTime);
        options.endTime(endTime);

        Set<Image> images = iContainer.getImagesByOptions(options);
        Image i = images.iterator().next();
        assertTrue(i.getAnnotationLinksCountPerOwner() != null);

    }

    @Test
    public void testSPLoadHierarchy() throws Exception {

        loginNewUser();

        Screen s = new Screen();
        s.setName("screen 1");
        
        Plate p1 = new Plate();
        p1.setName("plate 1");
        s.linkPlate(p1);
        Plate p2 = new Plate();
        p2.setName("plate 2");

        s = iUpdate.saveAndReturnObject(s);
        p2 = iUpdate.saveAndReturnObject(p2);
        
        Parameters options = new Parameters();
        
        //no orphan
        Set screens = iContainer.loadContainerHierarchy(Screen.class, 
        		new HashSet(), options);

        assertEquals(1, screens.size());
        
        Iterator i = screens.iterator();
        Screen screen;
        List<Plate> plates;
        Iterator j;
        Plate plate;
        while (i.hasNext()) {
        	screen = (Screen) i.next();
		assertEquals(screen.getId(), s.getId());
        	plates = screen.linkedPlateList();
        	assertTrue(plates.size() == 1);
        	j = plates.iterator();
        	while (j.hasNext()) {
        		plate = (Plate) j.next();
			assertEquals(plate.getId(), p1.getId());
			}
		}

        //orphan
        options.orphan();
        screens = iContainer.loadContainerHierarchy(Screen.class, 
        		new HashSet(), options);
        assertTrue(screens.size() == 2);
        i = screens.iterator();
        IObject object;
        while (i.hasNext()) {
        	object = (IObject) i.next();
        	if (object instanceof Screen) {
        		assertTrue(object.getId() == s.getId());
        	} else if (object instanceof Plate) {
        		assertTrue(object.getId() == p2.getId());
        	}
		}
    }
    
    // ~ Helpers
    // =========================================================================

    private Timestamp getDate(String arg) {
        Calendar cal = Calendar.getInstance();
        cal.setLenient(true);
        cal.setTime(new Date());
        if (arg.equals("after")) {
            cal.add(Calendar.DATE, +1);
        }
        if (arg.equals("before")) {
            cal.add(Calendar.DATE, -1);
        }
        Date yesterday = cal.getTime();
        SimpleDateFormat currentDate = new SimpleDateFormat(
                "yyyy-MM-dd hh:mm:ss");
        Timestamp time = Timestamp.valueOf(currentDate.format(yesterday));
        return (time);
    }

    private ILink createLinkedCommentAnnotation() {
        CommentAnnotation da = new CommentAnnotation();
        Dataset ds = new Dataset();
        Project p = new Project();

        p.setName("uEv");
        p.linkDataset(ds);
        ds.setName("uEv");
        da.setNs("");
        da.setTextValue("uEv");
        ds.linkAnnotation(da);
        ds = iContainer.createDataObject(ds, null);
        return ds.collectAnnotationLinks((CBlock<ILink>) null).iterator()
                .next();
    }
    

    private Image new_Image() {
        Image i = new Image();
        return i;
    }


}
