package integration;

import static org.testng.AssertJUnit.assertNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import omero.RLong;
import omero.RString;
import omero.RType;
import omero.api.IAdminPrx;
import omero.api.IContainerPrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Fileset;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImageI;
import omero.model.OriginalFile;
import omero.model.Permissions;
import omero.model.PermissionsI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.TermAnnotation;
import omero.model.TermAnnotationI;
import omero.sys.ParametersI;
import omero.sys.Roles;
import omero.cmd.Chgrp;
import omero.cmd.DoAll;
import omero.cmd.CmdCallbackI;
import omero.cmd.DoAllRsp;
import omero.cmd.ERR;
import omero.cmd.HandlePrx;
import omero.cmd.OK;
import omero.cmd.Request;
import omero.cmd.Response;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import pojos.GroupData;
import pojos.PermissionData;
import edu.emory.mathcs.backport.java.util.Arrays;

public class PermissionsTest_Matlab extends AbstractServerTest{

	//Permission table lists the types of groups that would be created (two copies of each)
	String[] perm_table = {"rwra--","rw----","rwr---","rwrw--"};
	String[] perm_type = {"Read-Annotate-","Private-","Read-Only-","Read-Write-"};
	String password = "ome";

	//Users created in the database(list obtained from Petr's manually created database, trying to duplicate the setup)
	String[] users={"member-all-1","member-all-2","member-all-3","member-all-4","member-all-5","member-all-6","member-all-7","owner","admin","member-one-ra","member-one-p","member-one-ro","member-one-rw"};

	/**
	 * Creates the permissions corresponding to the specified level.
	 *
	 * @param level The level to handle.
	 * @return
	 */
	private String getPermissions(int level)
	{
		switch (level) {
		case GroupData.PERMISSIONS_GROUP_READ:
			return "rwr---";
		case GroupData.PERMISSIONS_GROUP_READ_LINK:
			return "rwra--";
		case GroupData.PERMISSIONS_GROUP_READ_WRITE:
			return "rwrw--";
		case GroupData.PERMISSIONS_PUBLIC_READ:
			return "rwrwr-";
		}
		return "rw----"; //private group;
	}

	void setGroupUp() throws Exception {
		//super.setUp();
		omero.client client = newRootOmeroClient();
		client.enableKeepAlive(60);
		factory = client.getSession();

		List<ExperimenterGroup> groups1 = new ArrayList<ExperimenterGroup>();
		List<ExperimenterGroup> groups2 = new ArrayList<ExperimenterGroup>();

		//Create two copies of every group type mentioned above
		for( int i = 1 ; i <= 2 ; i++)
		{
			for( int j = 0 ; j < perm_table.length ; j++ )
			{
				String groupName = perm_type[j] + Integer.toString(i);

				ExperimenterGroup group = new ExperimenterGroupI();
				group.setName(omero.rtypes.rstring(groupName));
				final Permissions perms = new PermissionsI(perm_table[j]);
				group.getDetails().setPermissions(perms);

				IAdminPrx svc = factory.getAdminService();
				group = new ExperimenterGroupI(svc.createGroup(group), false);
				groups1.add(group);

				if (i==1)
				{
					groups2.add(group);
				}
			}
		}

		RString omeroPassword = omero.rtypes.rstring("ome");
		String Admin = "admin";
		int cntr = 0;
		//Create Users and add them to the respective groups
		ExperimenterGroup default_group;
		List<ExperimenterGroup> target_groups;

		Roles roles = factory.getAdminService().getSecurityRoles();
		ExperimenterGroup userGroup = new ExperimenterGroupI(roles.userGroupId, false);
		ExperimenterGroup system = new ExperimenterGroupI(roles.systemGroupId, false);

		for( int i = 0 ; i < users.length ; i++ )
		{
			target_groups = new ArrayList<ExperimenterGroup>();
			target_groups.add(userGroup);
			String omeroUsername = users[i];			
			Experimenter experimenter = createExperimenterI(omeroUsername, omeroUsername, omeroUsername);

			//Add admin user to system group
			if (omeroUsername.equalsIgnoreCase(Admin))
			{
				default_group = system;
				target_groups.addAll(groups1);
			}
			//Add the first 8 users to all groups
			else if (i<=7)
			{
				default_group =  groups1.get(0);
				target_groups.addAll(groups1);

			}
			//Add the last 4 users to one group alone
			else
			{
				default_group =  groups1.get(cntr);
				target_groups.add(groups2.get(cntr));
				cntr = cntr+1;
			}
			factory.getAdminService().createExperimenterWithPassword(experimenter, omeroPassword, default_group, target_groups);
		}
		client.closeSession();
	}


	void testImagesSetup() throws Exception{

		//		setGroupUp();
		//Iterate through all the users
		for ( int i=0; i<users.length ; i++)
		{

			omero.client client = new omero.client();
			client.closeSession();

			ServiceFactoryPrx session = client.createSession(users[i], "ome");
			client.enableKeepAlive(60);

			Experimenter user1 = session.getAdminService().lookupExperimenter(users[i]);
			List<Long> gids = session.getAdminService().getMemberOfGroupIds(user1);

			//Switch group context for the user (Iterate through all the groups, the user is part of)
			for ( int j=0 ; j< gids.size() ; j++)
			{
				ExperimenterGroupI group = new ExperimenterGroupI(gids.get(j), false);
				session.setSecurityContext(group);
				iUpdate = session.getUpdateService();
				mmFactory = new ModelMockFactory(session.getPixelsService());

				//Create 3 new Image Objects(with pixels) and attach it to the session
				for (int k=0 ; k<=10 ; k++)
				{
					Image img = (Image) iUpdate.saveAndReturnObject(mmFactory
							.simpleImage());
					assertNotNull(img);
				}

			}		
			client.closeSession();

		}
	}
	//Fetches imageids from all the users and stores them to an ArrayList
	List<Long[]> getAllImages() throws Exception{

		List<Long[]> idlist = new ArrayList<Long[]>();
		for (int j=0 ; j<users.length ; j++)
		{
			omero.client client = new omero.client();
			client.closeSession();

			ServiceFactoryPrx session = client.createSession(users[j], password);
			client.enableKeepAlive(60);

			Experimenter user1 = session.getAdminService().lookupExperimenter(users[j]);
			List<Long> gids = session.getAdminService().getMemberOfGroupIds(user1);
			for ( int k=0 ; k< gids.size() ; k++)
			{
				ExperimenterGroupI group = new ExperimenterGroupI(gids.get(k), false);
				session.setSecurityContext(group);

				ParametersI params = new omero.sys.ParametersI();
				params.exp(user1.getId());

				IContainerPrx proxy = session.getContainerService();
				List<Image> imageList = proxy.getUserImages(params);
				Long[] data;
				for (int i=0; i<imageList.size(); i++)
				{
					data = new Long[3];
					Image img = imageList.get(i);
					long id = img.getDetails().getGroup().getId().getValue();

					if (gids.get(k) == id) {
						data[0] = img.getId().getValue();
						data[1] = img.getDetails().getOwner().getId().getValue();
						data[2] = id;
						idlist.add(data);
					}
				}

			}
			client.closeSession();
		}
		return idlist;

	}


	//Iterate through all the users and try to annotate the images
	void annotateAllImages() throws Exception{

		//		testImagesSetup();
		List<Long[]> idlist = getAllImages();
		for (int j=0 ; j<users.length ; j++)
		{
			omero.client client = new omero.client();
			ServiceFactoryPrx session = client.createSession(users[j], password);
			String comment = users[j] + "_comment";
			String tag = users[j] + "_tag";

			IAdminPrx svc = session.getAdminService();
			Experimenter exp = svc.lookupExperimenter(users[j]);
			List<Long> usergroups = svc.getMemberOfGroupIds(exp);
			RLong expid = exp.getId();

			for ( int k=0 ; k< usergroups.size() ; k++)
			{
				ExperimenterGroup group = svc.getGroup(usergroups.get(k));
				session.setSecurityContext(new ExperimenterGroupI(group.getId().getValue(), false));

				PermissionData perms = new PermissionData(group.getDetails().getPermissions());
				String permsAsString = getPermissions(perms.getPermissionsLevel());
				final String[] subgroups= {perm_table[0], perm_table[3]};
				final List<?> perm_table1 = Arrays.asList(subgroups);

				iUpdate = session.getUpdateService();
				mmFactory = new ModelMockFactory(session.getPixelsService());

				//Create Tags
				List<Long> ids = new ArrayList<Long>();
				TagAnnotation c = new TagAnnotationI();
				c.setTextValue(omero.rtypes.rstring(tag));
				c = (TagAnnotation) iUpdate.saveAndReturnObject(c);
				ids.add(c.getId().getValue());

				//Create Comments
				TermAnnotation t = new TermAnnotationI();
				t.setTermValue(omero.rtypes.rstring(comment));
				t = (TermAnnotation) iUpdate.saveAndReturnObject(t);
				ids.add(t.getId().getValue());

				//Create File for FileAnnotation
				OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory
						.createOriginalFile());
				assertNotNull(of);
				FileAnnotation f = new FileAnnotationI();
				f.setFile(of);
				f = (FileAnnotation) iUpdate.saveAndReturnObject(f);
				ids.add(f.getId().getValue());

				for (int i=0; i<idlist.size() ; i++)
				{
					Long[] searchterm = idlist.get(i);
					Long groupid = searchterm[2];
					Long imageid = searchterm[0];
					Long ownerid = searchterm[1];

					if (groupid == group.getId().getValue() && (perm_table1.contains(permsAsString) || ownerid==expid.getValue())) 
					{
						List<IObject> links = new ArrayList<IObject>();
						//Create Links for Tags
						ImageAnnotationLink link = new ImageAnnotationLinkI();
						link.setChild(new TagAnnotationI(c.getId().getValue(), false));
						link.setParent(new ImageI(imageid, false));
						links.add(link);

						//Create Links for Comments
						link = new ImageAnnotationLinkI();
						link.setChild(new TermAnnotationI(t.getId().getValue(), false));
						link.setParent(new ImageI(imageid, false));
						links.add(link);

						//Create Links for Files
						link = new ImageAnnotationLinkI();
						link.setChild(new FileAnnotationI(f.getId().getValue(), false));
						link.setParent(new ImageI(imageid, false));
						links.add(link);
						iUpdate.saveAndReturnArray(links);	
					}

				}

			}
			client.closeSession();
		}
	}


	@DataProvider(name = "createData")
	public Object[][] createData() {
		List<TestParam> map = new ArrayList<TestParam>();
		Object[][] values = null;
		try {


			for (int j=0 ; j< 1 ; j++) //users.length
			{
				omero.client client = new omero.client("localhost");
				ServiceFactoryPrx session = client.createSession(users[j], password);

				Experimenter user1 = session.getAdminService().lookupExperimenter(users[j]);
				List<Long> gids = session.getAdminService().getMemberOfGroupIds(user1);
				for ( int k=0 ; k<gids.size() ; k++) //gids.size()
				{

					for ( int l=0 ; l < gids.size() ; l++) { //gids.size()

						ExperimenterGroupI group = new ExperimenterGroupI(gids.get(k), false);
						session.setSecurityContext(group);
						Long userid = session.getAdminService().getEventContext().userId;

						Long targetgroup = gids.get(l);
						Chgrp chgrp = new Chgrp();

						ParametersI params = new omero.sys.ParametersI();
						params.exp(omero.rtypes.rlong(userid));

						IContainerPrx proxy = session.getContainerService();
						List<Image> imageList1 = proxy.getUserImages(params);
						if (imageList1.size()==0){

							iUpdate = session.getUpdateService();
							mmFactory = new ModelMockFactory(session.getPixelsService());
							Image img = (Image) iUpdate.saveAndReturnObject(mmFactory
									.simpleImage());
							assertNotNull(img);
							imageList1 =  proxy.getUserImages(params);
						}                   

						Image img = imageList1.get(0);
						long imageid = img.getId().getValue();

						chgrp.id = imageid;
						chgrp.type = "/Image";
						chgrp.grp = targetgroup;
						map.add(new TestParam(chgrp, users[j], password, gids.get(k)));
					}

				}
				int index = 0;
				Iterator<TestParam> k = map.iterator();
				values = new Object[map.size()][1];
				while (k.hasNext()) {
					values[index][0] = k.next();
					index++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return values;

	}

	@Test(dataProvider="createData")
	public void test(TestParam param) throws Exception{
		String username = param.getUser();
		String passwd = param.getPass();
		Long sourcegroup = param.getsrcID();

		//create session and switch context to source group
		omero.client client = new omero.client("localhost");
		ServiceFactoryPrx session1 = client.createSession(username, passwd);

		ExperimenterGroupI group = new ExperimenterGroupI(sourcegroup, false);
		session1.setSecurityContext(group);
		DoAll all = new DoAll();
		List<Request> list1 = new ArrayList<Request>();
		list1.add(param.getChgrp());
		all.requests = list1;
		HandlePrx handle1 = session1.submit(all);

		long timeout_move = scalingFactor *  1 * 100;

		CmdCallbackI cb = new CmdCallbackI(client, handle1);
		cb.loop(10 * all.requests.size(), timeout_move);
		Response response  = cb.getResponse();
		if (response == null) {
			System.out.print("Failure");
			System.out.printf("%n");
		}
		if (response instanceof DoAllRsp) {

			List<Response> responses = ((DoAllRsp) response).responses;
			if (responses.size() == 1) {
				Response r = responses.get(0); 
				Assert.assertTrue(r instanceof OK, "move OK");
			}
		} else if (response instanceof ERR) {
			long targetgroup = param.getChgrp().grp;
			long imageid = param.getChgrp().id;
			Long userid = session1.getAdminService().getEventContext().userId;

			ExperimenterGroup group2 =  session1.getAdminService().getGroup(sourcegroup);
			PermissionData perms = new PermissionData(group2.getDetails().getPermissions());
			String permsAsString = getPermissions(perms.getPermissionsLevel());

			ExperimenterGroup group1 =  session1.getAdminService().getGroup(targetgroup);
			PermissionData perms1 = new PermissionData(group1.getDetails().getPermissions());
			String permsAsString1 = getPermissions(perms1.getPermissionsLevel());

			Assert.fail("Failure : User : " +userid + "tried moving image " + imageid + " from " + sourcegroup + "(" + permsAsString + ")" + " to " + targetgroup + "(" + permsAsString1 + ")");
			//                      
		}
	}


	Fileset fetchFileset(Long imageid,IContainerPrx proxy,ServiceFactoryPrx session) throws Exception{

		List<RType> imageids = new ArrayList<RType>(1);
		imageids.add(omero.rtypes.rlong(imageid));
		ParametersI param = new ParametersI();
		param.add("imageIds", omero.rtypes.rlist(imageids));
		String s = "select obj from Fileset as obj \n"
				+ "left outer join fetch obj.images as image \n"
				+ "left outer join fetch obj.usedFiles as usedFile \n"
				+ "join fetch usedFile.originalFile as f \n"
				+ "join fetch f.hasher \n"
				+ "where image.id in (:imageIds) ";

		String query = s;

		Fileset fileset = (Fileset) session.getQueryService().findByQuery(query, param);
		return fileset;
	}



	class TestParam {

		private Chgrp chgrp;

		private String user;
		private String password;
		private Long srcID;

		TestParam(Chgrp chgrp, String user, String password, Long srcID)
		{
			this.chgrp = chgrp;
			this.user = user;
			this.password = password;
			this.srcID = srcID;
		}

		Chgrp getChgrp() { return chgrp; }
		String getUser() { return user; }
		String getPass() { return password; }
		Long getsrcID() { return srcID; }
	}
}


