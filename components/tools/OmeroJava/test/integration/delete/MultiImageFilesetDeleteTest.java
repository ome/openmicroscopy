/*
 * $Id$
 *
 *   Copyright 2013 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration.delete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import integration.AbstractServerTest;
import integration.DeleteServiceTest;
import omero.cmd.Delete2;
import omero.cmd.DoAll;
import omero.cmd.Request;
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

import com.google.common.collect.ImmutableMap;

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
    	List<Request> commands = new ArrayList<Request>();
    	Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(d1.getId().getValue()));
        commands.add(dc);
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Dataset.class.getSimpleName(),
                Collections.singletonList(d2.getId().getValue()));
        commands.add(dc);
        DoAll all = new DoAll();
        all.requests = commands;
        doChange(client, factory, all, false, null);
    }

}
