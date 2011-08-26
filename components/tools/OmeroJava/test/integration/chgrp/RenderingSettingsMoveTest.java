/*
 * $Id$
 *
 * Copyright 2006-2011 University of Dundee. All rights reserved.
 * Use is subject to license terms supplied in LICENSE.txt
 */
package integration.chgrp;

import integration.AbstractTest;
import integration.DeleteServiceTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import omero.api.IRenderingSettingsPrx;
import omero.cmd.Chgrp;
import omero.model.ExperimenterGroup;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.RenderingDef;
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.testng.annotations.Test;



/**
 * Move image with rendering settings.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class RenderingSettingsMoveTest
	extends AbstractTest
{

	/**
     * Test to move an image w/o pixels between 2 private groups.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveImageWithRenderingSettings()
	throws Exception
    {
	String perms = "rw----";
	EventContext ctx = newUserAndGroup(perms);
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);

	Image img = mmFactory.createImage();
	img = (Image) iUpdate.saveAndReturnObject(img);
	Pixels pixels = img.getPrimaryPixels();
	//method already tested in RenderingSettingsServiceTest
	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
	prx.setOriginalSettingsInSet(Pixels.class.getName(),
			Arrays.asList(pixels.getId().getValue()));
	//check if we have settings now.
	ParametersI param = new ParametersI();
	param.addLong("pid", pixels.getId().getValue());
	String sql = "select rdef from RenderingDef as rdef " +
			"where rdef.pixels.id = :pid";
	List<IObject> settings = iQuery.findAllByQuery(sql, param);
	//now delete the image
	assertTrue(settings.size() > 0);


	long id = img.getId().getValue();
	//Move the image
	doChange(new Chgrp(ctx.sessionUuid, DeleteServiceTest.REF_IMAGE, id,
			null, g.getId().getValue()));

	//check if the settings have been deleted.
	Iterator<IObject> i = settings.iterator();
	IObject o;
	while (i.hasNext()) {
			o = i.next();
			param = new ParametersI();
			param.addId(o.getId().getValue());
			sql = "select rdef from RenderingDef as rdef " +
			"where rdef.id = :id";
			assertNull(iQuery.findByQuery(sql, param));
		}
	//Log in to other group
	loginUser(g);
	id = img.getId().getValue();
	param = new ParametersI();
	param.addId(id);

	StringBuilder sb = new StringBuilder();
	sb.append("select i from Image i ");
	sb.append("where i.id = :id");
	assertNotNull(iQuery.findByQuery(sb.toString(), param));
	i = settings.iterator();
	while (i.hasNext()) {
			o = i.next();
			param = new ParametersI();
			param.addId(o.getId().getValue());
			sql = "select rdef from RenderingDef as rdef " +
			"where rdef.id = :id";
			assertNotNull(iQuery.findByQuery(sql, param));
		}
    }

    /**
     * Test to move an image with rendering settings from 2 users.
     * Context:
     *  - 2 users view the image.
     *  - Owner move the image to a RW---- group.
     *  - The settings of the second user should not be moved
     *    to the destination group.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveImageWithRenderingSettingsRW()
	throws Exception
    {
	//This currently leads to a security violation
	//The owner should still be able to move the image even if somebody
	//looks at the image.
	String permsDestination = "rw----";
	EventContext ctx = newUserAndGroup("rwr---");

	Image img = mmFactory.createImage();
	img = (Image) iUpdate.saveAndReturnObject(img);
	Pixels pixels = img.getPrimaryPixels();
	//method already tested in RenderingSettingsServiceTest
	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
	long id = img.getId().getValue();
	prx.setOriginalSettingsInSet(Pixels.class.getName(),
			Arrays.asList(pixels.getId().getValue()));
	//check if we have settings now.
	ParametersI param = new ParametersI();
	param.addLong("pid", pixels.getId().getValue());
	String sql = "select rdef from RenderingDef as rdef " +
			"where rdef.pixels.id = :pid";
	List<IObject> settingsUser1 = iQuery.findAllByQuery(sql, param);
	assertTrue(settingsUser1.size() > 0);

	omero.client clientUser1 = disconnect();
	//Add a user to that group
	EventContext ctx2 = newUserInGroup(ctx);
	init(ctx2);
	prx = factory.getRenderingSettingsService();
	prx.setOriginalSettingsInSet(Pixels.class.getName(),
			Arrays.asList(pixels.getId().getValue()));
	List<IObject> settingsUser2 = iQuery.findAllByQuery(sql, param);
	assertTrue(settingsUser2.size() > 0);
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


	//check if the settings have been deleted.
	Iterator<IObject> i = settingsUser1.iterator();
	IObject o;
	while (i.hasNext()) {
			o = i.next();
			param = new ParametersI();
			param.addId(o.getId().getValue());
			sql = "select rdef from RenderingDef as rdef " +
			"where rdef.id = :id";
			assertNull(iQuery.findByQuery(sql, param));
		}

	i = settingsUser2.iterator();
	while (i.hasNext()) {
			o = i.next();
			param = new ParametersI();
			param.addId(o.getId().getValue());
			sql = "select rdef from RenderingDef as rdef " +
			"where rdef.id = :id";
			assertNull(iQuery.findByQuery(sql, param));
		}
	//Log in to other group
	loginUser(g);
	param = new ParametersI();
	param.addId(id);

	StringBuilder sb = new StringBuilder();
	sb.append("select i from Image i ");
	sb.append("where i.id = :id");
	assertNotNull(iQuery.findByQuery(sb.toString(), param));
	i = settingsUser1.iterator();
	while (i.hasNext()) {
			o = i.next();
			param = new ParametersI();
			param.addId(o.getId().getValue());
			sql = "select rdef from RenderingDef as rdef " +
			"where rdef.id = :id";
			assertNotNull(iQuery.findByQuery(sql, param));
		}
	i = settingsUser2.iterator();
	while (i.hasNext()) {
			o = i.next();
			param = new ParametersI();
			param.addId(o.getId().getValue());
			sql = "select rdef from RenderingDef as rdef " +
			"where rdef.id = :id";
			assertNull(iQuery.findByQuery(sql, param));
		}
    }

    /**
     * Test to move an image with rendering settings from 2 users.
     * Context:
     *  - 2 users view the image.
     *  - Owner move the image to a RWR--- group.
     *  - The settings of the second user should be moved
     *    to the destination group.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveImageWithRenderingSettingsRWR()
	throws Exception
    {
	//This currently leads to the same security violation even if
	// permissions are different.
	//The owner should still be able to move the image even if somebody
	//looks at the image.
	String permsDestination = "rwr---";
	EventContext ctx = newUserAndGroup("rwr---");

	Image img = mmFactory.createImage();
	img = (Image) iUpdate.saveAndReturnObject(img);
	Pixels pixels = img.getPrimaryPixels();
	//method already tested in RenderingSettingsServiceTest
	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
	long id = img.getId().getValue();
	prx.setOriginalSettingsInSet(Pixels.class.getName(),
			Arrays.asList(pixels.getId().getValue()));
	//check if we have settings now.
	ParametersI param = new ParametersI();
	param.addLong("pid", pixels.getId().getValue());
	String sql = "select rdef from RenderingDef as rdef " +
			"where rdef.pixels.id = :pid";
	List<IObject> settingsUser1 = iQuery.findAllByQuery(sql, param);
	assertTrue(settingsUser1.size() > 0);

	omero.client clientUser1 = disconnect();
	//Add a user to that group
	EventContext ctx2 = newUserInGroup(ctx);
	init(ctx2);
	prx = factory.getRenderingSettingsService();
	prx.setOriginalSettingsInSet(Pixels.class.getName(),
			Arrays.asList(pixels.getId().getValue()));
	List<IObject> settingsUser2 = iQuery.findAllByQuery(sql, param);
	assertTrue(settingsUser2.size() > 0);
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


	//check if the settings have been deleted.
	Iterator<IObject> i = settingsUser1.iterator();
	IObject o;
	while (i.hasNext()) {
			o = i.next();
			param = new ParametersI();
			param.addId(o.getId().getValue());
			sql = "select rdef from RenderingDef as rdef " +
			"where rdef.id = :id";
			assertNull(iQuery.findByQuery(sql, param));
		}

	i = settingsUser2.iterator();
	while (i.hasNext()) {
			o = i.next();
			param = new ParametersI();
			param.addId(o.getId().getValue());
			sql = "select rdef from RenderingDef as rdef " +
			"where rdef.id = :id";
			assertNull(iQuery.findByQuery(sql, param));
		}
	//Log in to other group
	loginUser(g);
	param = new ParametersI();
	param.addId(id);

	StringBuilder sb = new StringBuilder();
	sb.append("select i from Image i ");
	sb.append("where i.id = :id");
	assertNotNull(iQuery.findByQuery(sb.toString(), param));
	i = settingsUser1.iterator();
	while (i.hasNext()) {
			o = i.next();
			param = new ParametersI();
			param.addId(o.getId().getValue());
			sql = "select rdef from RenderingDef as rdef " +
			"where rdef.id = :id";
			assertNotNull(iQuery.findByQuery(sql, param));
		}
	i = settingsUser2.iterator();
	while (i.hasNext()) {
			o = i.next();
			param = new ParametersI();
			param.addId(o.getId().getValue());
			sql = "select rdef from RenderingDef as rdef " +
			"where rdef.id = :id";
			assertNotNull(iQuery.findByQuery(sql, param));
		}
    }

}
