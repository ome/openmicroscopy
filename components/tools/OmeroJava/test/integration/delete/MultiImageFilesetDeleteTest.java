/*
 * $Id$
 *
 *   Copyright 2013 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration.delete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import integration.AbstractServerTest;
import integration.DeleteServiceTest;
import omero.cmd.Delete;
import omero.model.Annotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Fileset;
import omero.model.FilesetI;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.Plate;
import omero.model.Project;
import omero.model.ProjectI;
import omero.model.Roi;
import omero.model.Screen;
import omero.model.ScreenI;
import omero.sys.Parameters;
import omero.sys.ParametersI;

import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * Tests for deleting hierarchies and the effects that that should have under
 * double- and multiple-linking.
 *
 * @since 5.-
 */
@Test(groups = { "delete", "integration"})
public class MultiImageFilesetDeleteTest extends AbstractServerTest {

	
	/**
     * Test the delete of datasets hosting images composing a MIF.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteDatasetWithSharedImageFromMIF()
    	throws Exception 
    {
    	//first create a project
    	Image i1 = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.simpleImage());
    	Image i2 = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.simpleImage());

    	Fileset fileset = new FilesetI();
    	fileset.setTemplatePrefix(omero.rtypes.rstring("fake"));
    	fileset.addImage(i1);
    	fileset.addImage(i2);
    	fileset = (Fileset) iUpdate.saveAndReturnObject(fileset);
    	Assert.assertEquals(fileset.copyImages().size(), 2);
    	Dataset d1 = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	Dataset d2 = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	DatasetImageLink link = new DatasetImageLinkI();
    	link.setChild((Image) i1.proxy());
    	link.setParent((Dataset) d1.proxy());
    	iUpdate.saveAndReturnObject(link);
    	link = new DatasetImageLinkI();
    	link.setChild((Image) i2.proxy());
    	link.setParent((Dataset) d2.proxy());
    	iUpdate.saveAndReturnObject(link);
    	Delete[] commands = new Delete[2];
    	commands[0] = new Delete(DeleteServiceTest.REF_DATASET,
    			d1.getId().getValue(), null);
    	commands[1] = new Delete(DeleteServiceTest.REF_DATASET,
    			d2.getId().getValue(), null);
    	delete(client, commands);
    }

}
