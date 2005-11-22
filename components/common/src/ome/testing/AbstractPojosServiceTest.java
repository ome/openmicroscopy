/*
 * ome.testing.AbstractPojosServiceTest
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package ome.testing;

// Java imports
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

// Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

// Application-internal dependencies
import ome.api.Pojos;
import ome.model.Category;
import ome.model.CategoryGroup;
import ome.model.Dataset;
import ome.model.Image;
import ome.model.Project;
import ome.util.Utils;
import ome.util.builders.PojoOptions;

/**
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 2.0
 */
public abstract class AbstractPojosServiceTest
        extends AbstractDependencyInjectionSpringContextTests
{

    protected static Log log = LogFactory
                                     .getLog(AbstractPojosServiceTest.class);

    protected Pojos      psrv;

    public void setPojos(Pojos service)
    {
        psrv = service;
    }

    protected OMEData data;

    public void setData(OMEData omeData)
    {
        data = omeData;
    }

    protected Set         s;

    protected PojoOptions po;

    protected Set         ids;

    protected void onSetUp() throws Exception
    {
        po = new PojoOptions().exp(new Integer(1));
    }

    public void testLoadProject()
    {
        ids = new HashSet(data.getMax("Project.ids",2));
        log("LOADP", psrv.loadContainerHierarchy(Project.class, ids, po.map()));
    }

    public void testLoadDataset()
    {
        ids = new HashSet(data.getMax("Dataset.ids",2));
        log("LOADD", psrv.loadContainerHierarchy(Dataset.class, ids, po.map()));
    }

    public void testLoadCG()
    {
        ids = new HashSet(data.getMax("CategoryGroup.ids",2));
        log("LOADCG", psrv.loadContainerHierarchy(CategoryGroup.class, ids, po
                .map()));
    }

    public void testLoadC()
    {
        ids = new HashSet(data.getMax("Category.ids",2));
        log("Load_c", psrv
                .loadContainerHierarchy(Category.class, ids, po.map()));
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`

    public void testFindProject()
    {
        ids = new HashSet(data.getMax("Image.ids",2));
        log("find_p", psrv.findContainerHierarchies(Project.class, ids, po
                .map()));
    }

    public void testFindCG()
    {
        ids = new HashSet(data.getMax("Image.ids",2));
        log("find_cg", psrv.findContainerHierarchies(CategoryGroup.class, ids,
                po.map()));
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`

    public void testDatasetAnn()
    {
        ids = new HashSet(data.getMax("Dataset.Annotated.ids",2));
        log("d_ann", psrv.findAnnotations(Dataset.class, ids, po.map()));
    }

    public void testImageAnn()
    {
        ids = new HashSet(data.getMax("Image.Annotated.ids",2));
        log("i_ann", psrv.findAnnotations(Image.class, ids, po.map()));
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`

    public void testGetFromProject()
    {
        ids = new HashSet(data.getMax("Project.ids",2));
        log("get_p", psrv.getImages(Project.class, ids, po.map()));
    }

    public void testGetFromDataset()
    {
        ids = new HashSet(data.getMax("Dataset.ids",2));
        log("get_d", psrv.getImages(Dataset.class, ids, po.map()));
    }

    public void testGetFromCg()
    {
        ids = new HashSet(data.getMax("CategoryGroup.ids",2));
        log("get_cg", psrv.getImages(CategoryGroup.class, ids, po.map()));
    }

    public void testGetFromCat()
    {
        ids = new HashSet(data.getMax("Category.ids",2));
        log("get_c", psrv.getImages(Category.class, ids, po.map()));
    }

    // TODO how to run getUserImages
    public void testGetUser()
    {
        log("get_image", psrv.getUserImages(po.map()));
    }

    public void testGetUserDetails()
    {
        Set names = new HashSet(Arrays.asList(new String[] { "josh", "jason",
                "chris", "callan", "jmoore", "jswedlow", "jburel" }));
        log("get_user", psrv.getUserDetails(names, po.map()));
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`

    public void testPathsInc()
    {
        ids = new HashSet(data.getMax("Image.ids",2));
        log("path_inc", psrv.findCGCPaths(ids, 0, po.map()));
    }

    public void testPathsExc()
    {
        ids = new HashSet(data.getMax("Image.ids",2));
        log("path_exc", psrv.findCGCPaths(ids, 1, po.map()));
    }

    public void testPathsFAIL()
    {
        ids = new HashSet(data.getMax("Image.ids",2));
        try
        {
            log("path_exc", psrv.findCGCPaths(ids, 2, po.map()));
            fail(" no algorithm 2 !!!");
        } catch (IllegalArgumentException iae)
        {
            // do nothing.
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`

    private void log(String name, Object result)
    {
        log.info("1)NAME: " + name + "2)RESULT: " + result);
    }

    String nullObj   = "This should get us nothing.";

    String emptyColl = "This collection should be empty.";

    String nonNull   = "We should get something back";

    /** Each method should return a null or an empty set as appropriate */
    public void testNulls()
    {
        Set test = new HashSet();
        test.add(new Integer(-1)); // Non-existence set of ids
        Integer nonExp = new Integer(-1); // Non-existence experimenter ID
        PojoOptions po = new PojoOptions().exp(nonExp);
        //
        assertTrue(emptyColl, psrv.findAnnotations(Dataset.class, test, null)
                .size() == 0);
        assertTrue(emptyColl, psrv.findAnnotations(Dataset.class,
                new HashSet(), null).size() == 0);
        //
        assertTrue(emptyColl, psrv.findAnnotations(Dataset.class, test,
                po.map()).size() == 0);
        assertTrue(emptyColl, psrv.findAnnotations(Dataset.class,
                new HashSet(), po.map()).size() == 0);
        //
        assertTrue(emptyColl, psrv.findAnnotations(Image.class, test, null)
                .size() == 0);
        assertTrue(emptyColl, psrv.findAnnotations(Image.class, new HashSet(),
                null).size() == 0);
        //
        assertTrue(emptyColl, psrv.findAnnotations(Image.class, test, po.map())
                .size() == 0);
        assertTrue(emptyColl, psrv.findAnnotations(Image.class, new HashSet(),
                po.map()).size() == 0);
        //
        assertTrue(emptyColl, psrv.findContainerHierarchies(Project.class,
                test, null).size() == 0);
        assertTrue(emptyColl, psrv.findContainerHierarchies(Project.class,
                new HashSet(), null).size() == 0);
        //
        assertTrue(emptyColl, psrv.findContainerHierarchies(
                CategoryGroup.class, test, null).size() == 0);
        assertTrue(emptyColl, psrv.findContainerHierarchies(
                CategoryGroup.class, new HashSet(), null).size() == 0);
        //
        assertTrue(emptyColl, psrv.loadContainerHierarchy(CategoryGroup.class,
                test, null).size()==0);
        assertTrue(emptyColl, psrv.loadContainerHierarchy(Category.class, test,
                null).size()==0);
        assertTrue(emptyColl, psrv.loadContainerHierarchy(CategoryGroup.class,
                test, po.map()).size()==0);
        assertTrue(emptyColl, psrv.loadContainerHierarchy(Category.class, test,
                po.map()).size()==0);
        //
        assertTrue(emptyColl, psrv.loadContainerHierarchy(Project.class, test,
                null).size()==0);
        assertTrue(emptyColl, psrv.loadContainerHierarchy(Dataset.class, test,
                null).size()==0);
        assertTrue(emptyColl, psrv.loadContainerHierarchy(Project.class, test, po
                .map()).size()==0);
        assertTrue(emptyColl, psrv.loadContainerHierarchy(Dataset.class, test, po
                .map()).size()==0);
        //
        assertTrue(emptyColl, psrv.findCGCPaths(test, 0, null).size() == 0);
        assertTrue(emptyColl,
                psrv.findCGCPaths(new HashSet(), 0, null).size() == 0);
        // TODO The Logic here is reversed!
        // assertTrue(emptyColl,psrv.findCGCPaths(test,false).size()==0);
        assertTrue(emptyColl, psrv.findCGCPaths(test, 1, null).size() == 0);
        assertTrue(emptyColl,
                psrv.findCGCPaths(new HashSet(), 1, null).size() == 0);

    }

    public void testContainedImages()
    {
        Set imgsPDI = new HashSet(data.getMax("Image.ids", 10));

        // Something
        Set result = (Set) psrv.findContainerHierarchies(Project.class,
                imgsPDI, null);
        assertTrue(nonNull, result != null && result.size() != 0);
        // Not too much
        Set test = Utils.getImagesinPDI(result);
        assertTrue("There should only be as many images " + test.size()
                + " as in the data.imagesPDI (" + imgsPDI.size() + ").", test
                .size() == imgsPDI.size());
    }

    public void testDuplicateImages() {
        Set imgsPDI = new HashSet(data.getMax("Image.ids",10));
        Set result = psrv.findContainerHierarchies(Project.class,imgsPDI,null);
        Set test = Utils.getImagesinPDI(result);
        assertTrue("Images in should eq. images out",imgsPDI.size()==test.size());
        
        Set noDupesPlease = new HashSet(); 
        for (Iterator i = test.iterator(); i.hasNext();) {
            Image img = (Image) i.next();
            if (noDupesPlease.contains(img.getImageId())) 
                fail("But also the IDs should be unique!");
            noDupesPlease.add(img.getImageId());
        }
        
    }
    
    public void testPathCalls()
    {
        Set imgs = new HashSet(data.getMax("Image.ids", 1));
        Set con = psrv.findCGCPaths(imgs, 0, null);
        Set non = psrv.findCGCPaths(imgs, 1, null);
        for (Iterator itNon = non.iterator(); itNon.hasNext();)
        {
            CategoryGroup cg = (CategoryGroup) itNon.next();
            for (Iterator itCon = con.iterator(); itCon.hasNext();)
            {
                CategoryGroup cg2 = (CategoryGroup) itCon.next();
                assertTrue(
                        "No contained paths may be contained and not-contained: "+cg2path(cg2),
                        cg.getAttributeId() != cg2.getAttributeId());

            }
        }
    }

    String cg2path(CategoryGroup cg)
    {
        StringBuilder sb = new StringBuilder();
        Integer cgId = cg.getAttributeId();
        for (Iterator it = cg.getCategories().iterator(); it.hasNext();)
        {
            Category c = (Category) it.next();
            Integer cId = c.getAttributeId();
            sb.append("/" + cgId + "/" + cId + "\n");
        }
        return sb.toString();
    }

}
