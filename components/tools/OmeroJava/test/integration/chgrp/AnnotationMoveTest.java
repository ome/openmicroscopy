/*
 * $Id$
 *
 * Copyright 2006-2011 University of Dundee. All rights reserved.
 * Use is subject to license terms supplied in LICENSE.txt
 */
package integration.chgrp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import omero.cmd.Chgrp;
import omero.model.Annotation;
import omero.model.ExperimenterGroup;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.testng.annotations.Test;

import integration.AbstractTest;
import integration.DeleteServiceTest;

/**
 * Move annotated objects. Annotation.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class AnnotationMoveTest
	extends AbstractTest
{

	/**
     * Test to move an image with annotation.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveImageWithNonSharableAnnotation()
	throws Exception
    {
	String perms = "rw----";
	EventContext ctx = newUserAndGroup(perms);
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);

	Image img = (Image) iUpdate.saveAndReturnObject(
			mmFactory.createImage());
	//Annotate the image.
	List<Long> annotationIds = createNonSharableAnnotation(img, null);
	//now move the image.
	long id = img.getId().getValue();
	doChange(new Chgrp(ctx.sessionUuid, DeleteServiceTest.REF_IMAGE, id,
			null, g.getId().getValue()));
	ParametersI param = new ParametersI();
	param.addId(id);
	StringBuilder sb = new StringBuilder();
	sb.append("select i from Image i ");
	sb.append("where i.id = :id");
	assertNull(iQuery.findByQuery(sb.toString(), param));

	param = new ParametersI();
	param.addIds(annotationIds);
	sb = new StringBuilder();
	sb.append("select i from Annotation i ");
	sb.append("where i.id in (:ids)");
	assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

	loginUser(g);
	assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(),
			annotationIds.size());
    }

	/**
     * Test to move an image with annotation that can be shared but we do not
     * keep them.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveImageWithSharableAnnotation()
	throws Exception
    {
	String perms = "rw----";
	EventContext ctx = newUserAndGroup(perms);
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);

	Image img = (Image) iUpdate.saveAndReturnObject(
			mmFactory.createImage());
	//Annotate the image.
	List<Long> annotationIds = createSharableAnnotation(img, null);
	//now move the image.
	long id = img.getId().getValue();
	doChange(new Chgrp(ctx.sessionUuid, DeleteServiceTest.REF_IMAGE, id,
			null, g.getId().getValue()));
	ParametersI param = new ParametersI();
	param.addId(id);
	StringBuilder sb = new StringBuilder();
	sb.append("select i from Image i ");
	sb.append("where i.id = :id");
	assertNull(iQuery.findByQuery(sb.toString(), param));

	param = new ParametersI();
	param.addIds(annotationIds);
	sb = new StringBuilder();
	sb.append("select i from Annotation i ");
	sb.append("where i.id in (:ids)");
	assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

	loginUser(g);
	assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(),
			annotationIds.size());
    }

    /**
     * Test to move an image with annotation that can be shared but we
     * keep them.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveImageWithSharableAnnotationToKeep()
	throws Exception
    {
	String perms = "rwrw--";
	EventContext ctx = newUserAndGroup(perms);
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);

	Image img = (Image) iUpdate.saveAndReturnObject(
			mmFactory.createImage());
	//Annotate the image.
	List<Long> annotationIds = createSharableAnnotation(img, null);
	//now move the image.
	long id = img.getId().getValue();
	doChange(new Chgrp(ctx.sessionUuid, DeleteServiceTest.REF_IMAGE, id,
			DeleteServiceTest.SHARABLE_TO_KEEP, g.getId().getValue()));
	ParametersI param = new ParametersI();
	param.addId(id);
	StringBuilder sb = new StringBuilder();
	sb.append("select i from Image i ");
	sb.append("where i.id = :id");
	assertNull(iQuery.findByQuery(sb.toString(), param));

	param = new ParametersI();
	param.addIds(annotationIds);
	sb = new StringBuilder();
	sb.append("select i from Annotation i ");
	sb.append("where i.id in (:ids)");
	assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(),
			annotationIds.size());

	loginUser(g);
	assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);
    }

    /**
     * Test to move an image with annotation.
     * Context:
     *  - 2 users annotate the image with non sharable annotations.
     *  - Owner move the image to a private group.
     *  - The annotation of the second user should be deleted and not moved
     *  to the destination group.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveImageAnnotatedByUsersDestinationGroupRW()
	throws Exception
    {
	String permsDestination = "rw----";
	EventContext ctx = newUserAndGroup("rwrw--");
	Image img = (Image) iUpdate.saveAndReturnObject(
			mmFactory.createImage());

	long id = img.getId().getValue();
	List<Long> annotationIdsUser1 = createNonSharableAnnotation(img, null);

	omero.client clientUser1 = disconnect();
	//Add a user to that group
	EventContext ctx2 = newUserInGroup(ctx);
	init(ctx2);
	List<Long> annotationIdsUser2 = createNonSharableAnnotation(img, null);
	omero.client clientUser2 = disconnect();

	List<Long> users = new ArrayList<Long>();
	users.add(ctx.userId);
	users.add(ctx2.userId);

	//Create a private group with the 2 users.
	ExperimenterGroup g = newGroupAddUser(permsDestination, users);

	//reconnect as user1
	init(clientUser1);
	//now move the image.

	doChange(new Chgrp(ctx.sessionUuid, DeleteServiceTest.REF_IMAGE, id,
			null, g.getId().getValue()));

	//Annotation of user1 should be removed
	ParametersI param = new ParametersI();
	param.addIds(annotationIdsUser1);
	StringBuilder sb = new StringBuilder();
	sb.append("select i from Annotation i ");
	sb.append("where i.id in (:ids)");
	assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

	//Annotation of user2 should be gone
	param = new ParametersI();
	param.addIds(annotationIdsUser2);
	assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

	loginUser(g);
	param = new ParametersI();
	param.addIds(annotationIdsUser1);
	assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(),
			annotationIdsUser1.size());
	//Annotation of user2 should not be moved b/c group is private
	disconnect();
	init(clientUser2);
	loginUser(g);
	param = new ParametersI();
	param.addIds(annotationIdsUser2);
	assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);
    }

    /**
     * Test to move an image with annotation.
     * Context:
     *  - 2 users annotate the image with non sharable annotations.
     *  - Owner move the image to a RWR--- group.
     *  - The annotation of the second user should be deleted and not moved
     *  to the destination group.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveImageAnnotatedByUsersDestinationGroupRWR()
	throws Exception
    {
	String permsDestination = "rwr---";
	EventContext ctx = newUserAndGroup("rwrw--");
	Image img = (Image) iUpdate.saveAndReturnObject(
			mmFactory.createImage());

	long id = img.getId().getValue();
	List<Long> annotationIdsUser1 = createNonSharableAnnotation(img, null);

	omero.client clientUser1 = disconnect();
	//Add a user to that group
	EventContext ctx2 = newUserInGroup(ctx);
	init(ctx2);
	List<Long> annotationIdsUser2 = createNonSharableAnnotation(img, null);
	disconnect();

	List<Long> users = new ArrayList<Long>();
	users.add(ctx.userId);
	users.add(ctx2.userId);

	//Create a private group with the 2 users.
	ExperimenterGroup g = newGroupAddUser(permsDestination, users);

	//reconnect as user1
	init(clientUser1);
	//now move the image.

	doChange(new Chgrp(ctx.sessionUuid, DeleteServiceTest.REF_IMAGE, id,
			null, g.getId().getValue()));

	//Annotation of user1 should be removed
	ParametersI param = new ParametersI();
	param.addIds(annotationIdsUser1);
	StringBuilder sb = new StringBuilder();
	sb.append("select i from Annotation i ");
	sb.append("where i.id in (:ids)");
	assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

	//Annotation of user2 should be gone
	param = new ParametersI();
	param.addIds(annotationIdsUser2);
	assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

	loginUser(g);
	param = new ParametersI();
	param.addIds(annotationIdsUser1);
	assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(),
			annotationIdsUser1.size());
	//Annotation of user2 should not be moved b/c destination group is
	//RWR---
	param = new ParametersI();
	param.addIds(annotationIdsUser2);
	assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);
    }

    /**
     * Test to move an image with annotation.
     * Context:
     *  - 2 users annotate the image with non sharable annotations.
     *  - Owner move the image to a RWRW-- group.
     *  - The annotation of the second user should be moved
     *  to the destination group.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveImageAnnotatedByUsersDestinationGroupRWRW()
	throws Exception
    {
	String permsDestination = "rwrw--";
	EventContext ctx = newUserAndGroup("rwrw--");
	Image img = (Image) iUpdate.saveAndReturnObject(
			mmFactory.createImage());

	long id = img.getId().getValue();
	List<Long> annotationIdsUser1 = createNonSharableAnnotation(img, null);

	omero.client clientUser1 = disconnect();
	//Add a user to that group
	EventContext ctx2 = newUserInGroup(ctx);
	init(ctx2);
	List<Long> annotationIdsUser2 = createNonSharableAnnotation(img, null);
	disconnect();

	List<Long> users = new ArrayList<Long>();
	users.add(ctx.userId);
	users.add(ctx2.userId);

	//Create a private group with the 2 users.
	ExperimenterGroup g = newGroupAddUser(permsDestination, users);

	//reconnect as user1
	init(clientUser1);
	//now move the image.

	doChange(new Chgrp(ctx.sessionUuid, DeleteServiceTest.REF_IMAGE, id,
			null, g.getId().getValue()));

	//Annotation of user1 should be removed
	ParametersI param = new ParametersI();
	param.addIds(annotationIdsUser1);
	StringBuilder sb = new StringBuilder();
	sb.append("select i from Annotation i ");
	sb.append("where i.id in (:ids)");
	assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

	//Annotation of user2 should be gone
	param = new ParametersI();
	param.addIds(annotationIdsUser2);
	assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

	loginUser(g);
	param = new ParametersI();
	param.addIds(annotationIdsUser1);
	assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(),
			annotationIdsUser1.size());
	//Annotation of user2 should be moved b/c group is RWRW and the user
	// is a member of the destination group
	param = new ParametersI();
	param.addIds(annotationIdsUser2);
	assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(),
			annotationIdsUser2.size());
    }

    /**
     * Test to move an image with annotation.
     * Context:
     *  - 2 users annotate the image with non sharable annotations.
     *  - Owner move the image to a RWRW-- group.
     *  - The annotation of the second user should be deleted and not moved
     *  to the destination group b/c the second user is not a member of the
     *  destination group.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveImageAnnotatedByUsersOneNotInDestinationGroupDestinationGroupRWRW()
	throws Exception
    {
	String permsDestination = "rwrw--";
	EventContext ctx = newUserAndGroup("rwrw--");
	Image img = (Image) iUpdate.saveAndReturnObject(
			mmFactory.createImage());

	long id = img.getId().getValue();
	List<Long> annotationIdsUser1 = createNonSharableAnnotation(img, null);

	omero.client clientUser1 = disconnect();
	//Add a user to that group
	EventContext ctx2 = newUserInGroup(ctx);
	init(ctx2);
	List<Long> annotationIdsUser2 = createNonSharableAnnotation(img, null);
	omero.client clientUser2 = disconnect();

	//Create a private group with the 2 users.
	ExperimenterGroup g = newGroupAddUser(permsDestination, ctx.userId);

	//reconnect as user1
	init(clientUser1);
	//now move the image.

	doChange(new Chgrp(ctx.sessionUuid, DeleteServiceTest.REF_IMAGE, id,
			null, g.getId().getValue()));

	//Annotation of user1 should be removed
	ParametersI param = new ParametersI();
	param.addIds(annotationIdsUser1);
	StringBuilder sb = new StringBuilder();
	sb.append("select i from Annotation i ");
	sb.append("where i.id in (:ids)");
	assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

	//Annotation of user2 should be gone
	param = new ParametersI();
	param.addIds(annotationIdsUser2);
	assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

	loginUser(g);
	param = new ParametersI();
	param.addIds(annotationIdsUser1);
	assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(),
			annotationIdsUser1.size());
	//Annotation of user2 should not be in the group b/c the user
	// is not a member of the destination group
	param = new ParametersI();
	param.addIds(annotationIdsUser2);
	assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);
    }


    /**
     * Test to move a tagged image, the tag is also used to tag another image.
     * The image will be moved and the tag.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveTaggedImage()
	throws Exception
    {
	String perms = "rw----";
	EventContext ctx = newUserAndGroup(perms);
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);

	Image img1 = (Image) iUpdate.saveAndReturnObject(
			mmFactory.createImage());
	Image img2 = (Image) iUpdate.saveAndReturnObject(
			mmFactory.createImage());
	//
	TagAnnotation c = new TagAnnotationI();
	c.setTextValue(omero.rtypes.rstring("tag"));
	c = (TagAnnotation) iUpdate.saveAndReturnObject(c);
	List<IObject> links = new ArrayList<IObject>();
	//Tag the 2 images
	ImageAnnotationLink link = new ImageAnnotationLinkI();
		link.setChild(new TagAnnotationI(c.getId().getValue(), false));
		link.setParent(img1);
		links.add(link);

		link = new ImageAnnotationLinkI();
		link.setChild(new TagAnnotationI(c.getId().getValue(), false));
		link.setParent(img2);
		links.add(link);
		iUpdate.saveAndReturnArray(links);

		long id = img1.getId().getValue();
		doChange(new Chgrp(ctx.sessionUuid, DeleteServiceTest.REF_IMAGE, id,
				DeleteServiceTest.SHARABLE_TO_KEEP, g.getId().getValue()));

		ParametersI param = new ParametersI();
	param.addId(c.getId().getValue());
		StringBuilder sb = new StringBuilder();
	sb.append("select i from Annotation i ");
	sb.append("where i.id = :id");
	assertNotNull(iQuery.findByQuery(sb.toString(), param));

	loginUser(g);
	param = new ParametersI();
	param.addId(id);
	sb = new StringBuilder();
	sb.append("select i from ImageAnnotationLink i ");
	sb.append("where i.parent.id = :id");
	List<IObject> results = iQuery.findAllByQuery(sb.toString(), param);

	assertTrue(results.size() > 0);
	Iterator<IObject> j = results.iterator();
	while (j.hasNext()) {
			link = (ImageAnnotationLink) j.next();
			Annotation a = link.getChild();
			if (a instanceof TagAnnotation) {
				assertEquals(((TagAnnotation) a).getTextValue().getValue(),
						c.getTextValue().getValue());
			}
		}

    }

}
