/*
 * $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration.delete;

import integration.AbstractTest;
import integration.DeleteServiceTest;
import omero.api.delete.DeleteCommand;
import omero.model.Annotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.Plate;
import omero.model.Project;
import omero.model.ProjectI;
import omero.model.Roi;
import omero.model.Screen;
import omero.model.ScreenI;

import org.testng.annotations.Test;

/**
 * Tests for deleting hierarchies and the effects that that should have under
 * double- and multiple-linking.
 *
 * @see ticket:3031
 * @see ticket:2994
 * @since 4.2.1
 */
@Test(groups = { "delete", "integration", "ticket:2615" })
public class HierarchyDeleteTest extends AbstractTest {

    private final static omero.RString t3031 = omero.rtypes.rstring("#3031");

    /**
     * Test to delete a dataset containing an image also contained in another 
     * dataset. The second dataset and the image should not be deleted.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:3031")
    public void testDeletingDataset() throws Exception {

        newUserAndGroup("rwrw--");

        Dataset ds1 = new DatasetI();
        ds1.setName(t3031);

        Dataset ds2 = new DatasetI();
        ds2.setName(t3031);

        Image i = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
        i.unload();

        ds1.linkImage(i);
        ds1 = (Dataset) iUpdate.saveAndReturnObject(ds1);
        ds2.linkImage(i);
        ds2 = (Dataset) iUpdate.saveAndReturnObject(ds2);

        delete(client, new DeleteCommand(DeleteServiceTest.REF_DATASET, 
        		ds2.getId().getValue(), null));

        assertDoesNotExist(ds2);
        assertExists(ds1);
        assertExists(i);
    }

    /**
     * Test to delete a dataset containing an image also contained in another 
     * dataset. The second dataset and the image with an annotation
     * should not be deleted.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:3031")
    public void testDeletingDatasetDoesntRemoveImageAnnotation() throws Exception {

        newUserAndGroup("rwrw--");

        Dataset ds1 = new DatasetI();
        ds1.setName(t3031);

        Dataset ds2 = new DatasetI();
        ds2.setName(t3031);

        Image i = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
        i.unload();

        ds1.linkImage(i);
        ds1 = (Dataset) iUpdate.saveAndReturnObject(ds1);
        ds2.linkImage(i);
        ds2 = (Dataset) iUpdate.saveAndReturnObject(ds2);

        Annotation a = (Annotation) iUpdate.saveAndReturnObject(
        		new CommentAnnotationI());

        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.setChild((Annotation) a.proxy());
        link.setParent((Image) i.proxy());
        iUpdate.saveAndReturnObject(link);
        
        delete(client, new DeleteCommand(DeleteServiceTest.REF_DATASET, 
        		ds2.getId().getValue(), null));

        assertDoesNotExist(ds2);
        assertExists(ds1);
        assertExists(i);
        assertExists(a);
    }

    /**
     * Test to delete a dataset containing an image also contained in another 
     * dataset. The second dataset and the image with ROI
     * should not be deleted.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = {"ticket:3031", "ticket:3032"})
    public void testDeletingDatasetDoesntRemoveImageRoi() throws Exception {

        newUserAndGroup("rwrw--");

        Dataset ds1 = new DatasetI();
        ds1.setName(t3031);

        Dataset ds2 = new DatasetI();
        ds2.setName(t3031);

        Image i = (Image) iUpdate.saveAndReturnObject(
        		mmFactory.createImageWithRoi());
        Roi roi = i.copyRois().get(0);
        i.unload();

        ds1.linkImage(i);
        ds1 = (Dataset) iUpdate.saveAndReturnObject(ds1);
        ds2.linkImage(i);
        ds2 = (Dataset) iUpdate.saveAndReturnObject(ds2);

        delete(client, new DeleteCommand(DeleteServiceTest.REF_DATASET, 
        		ds2.getId().getValue(),  null));

        assertDoesNotExist(ds2);
        assertExists(ds1);
        assertExists(i);
        assertExists(roi);
    }

    /**
     * Test to delete a project containing a dataset also contained in another 
     * project. The second project and the dataset should not be deleted.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:3031")
    public void testDeletingProject() throws Exception {

        newUserAndGroup("rwrw--");

        Project p1 = new ProjectI();
        p1.setName(t3031);

        Project p2 = new ProjectI();
        p2.setName(t3031);

        Dataset d = new DatasetI();
        d.setName(t3031);
        d = (Dataset) iUpdate.saveAndReturnObject(d);
        d.unload();

        p1.linkDataset(d);
        p1 = (Project) iUpdate.saveAndReturnObject(p1);
        p2.linkDataset(d);
        p2 = (Project) iUpdate.saveAndReturnObject(p2);

        delete(client, new DeleteCommand(DeleteServiceTest.REF_PROJECT, 
        		p2.getId().getValue(), null));

        assertDoesNotExist(p2);
        assertExists(p1);
        assertExists(d);
    }

    /**
     * Test to delete a screen containing a plate also contained in another 
     * screen. The second screen and the plate should not be deleted.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:3031")
    public void testDeletingScreen() throws Exception {

        newUserAndGroup("rwrw--");

        Screen s1 = new ScreenI();
        s1.setName(t3031);

        Screen s2 = new ScreenI();
        s2.setName(t3031);

        Plate p = mmFactory.createPlate(1, 1, 1, 1, false);
        p = (Plate) iUpdate.saveAndReturnObject(p);
        p.unload();

        s1.linkPlate(p);
        s1 = (Screen) iUpdate.saveAndReturnObject(s1);

        s2.linkPlate(p);
        s2 = (Screen) iUpdate.saveAndReturnObject(s2);

        delete(client, new DeleteCommand(DeleteServiceTest.REF_SCREEN, 
        		s2.getId().getValue(),
                null));

        assertDoesNotExist(s2);
        assertExists(s1);
        assertExists(p);
    }

}
