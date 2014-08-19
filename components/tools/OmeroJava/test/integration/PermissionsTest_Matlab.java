package integration;

import omero.RString;
import omero.rtypes;
import omero.api.IAdminPrx;
import omero.cmd.Chgrp;
import omero.cmd.DoAll;
import omero.cmd.CmdCallbackI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import omero.grid.Column;
import omero.grid.LongColumn;
import omero.grid.TablePrx;
import omero.model.Channel;
import omero.model.Dataset;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.LogicalChannel;
import omero.model.OriginalFile;
import omero.model.Permissions;
import omero.model.PermissionsI;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.PlateAnnotationLink;
import omero.model.PlateAnnotationLinkI;
import omero.model.PlateI;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.Reagent;
import omero.model.Rect;
import omero.model.RectI;
import omero.model.Roi;
import omero.model.RoiAnnotationLink;
import omero.model.RoiAnnotationLinkI;
import omero.model.RoiI;
import omero.model.Screen;
import omero.model.ScreenPlateLink;
import omero.model.ScreenPlateLinkI;
import omero.model.Shape;
import omero.model.StatsInfo;
import omero.model.Well;
import omero.model.WellSample;
import omero.sys.EventContext;

public class PermissionsTest_Matlab extends AbstractServerTest{

	@SuppressWarnings("unchecked")
	void Database_Setup () {
		omero.client client = newRootOmeroClient();

		//Permission table lists the types of groups that would be created (two copies of each)
		String[] perm_table = {"rwra--","rw----","rwr---","rwrw--"};
		String[] perm_type = {"Read-Annotate-","Private-","Read-Only-","Read-Write-"};	

		//Users created in the database(list obtained from Petr's manually created database, trying to duplicate the setup)
		String[] users={"member-all-1","member-all-2","member-all-3","member-all-4","member-all-5","member-all-6","member-all-7","owner","admin","member-one-ra","member-one-p","member-one-ro","member-one-rw"};
		List<ExperimenterGroup> groups1 = new ArrayList<ExperimenterGroup>();
		List<ExperimenterGroup> groups2 = new ArrayList<ExperimenterGroup>();

		//Create two copies of every group type mentioned above
		for( int i = 1 ; i <= 2 ; i++)
		{
			for( int j = 0 ; j < perm_table.length ; j++ )
			{
				String groupName = perm_type[j] + Integer.toString(i);

				final ExperimenterGroup group = new ExperimenterGroupI();
				group.setName(omero.rtypes.rstring(groupName));
				final Permissions perms = new PermissionsI(perm_table[j]);
				group.getDetails().setPermissions(perms);

				IAdminPrx svc = root.getSession().getAdminService();
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
		for( int i = 0 ; i < users.length ; i++ )
		{
			String omeroUsername = users[i];			
			Experimenter experimenter = createExperimenterI(omeroUsername, omeroUsername, omeroUsername);

			//Add admin user to system group
			if (omeroUsername.equalsIgnoreCase(Admin))
			{
				long system_group=root.getSession().getAdminService().getEventContext().groupId;
				default_group = root.getSession().getAdminService().getGroup(system_group);
				target_groups = groups1;
			}
			//Add the first 8 users to all groups
			else if (i<=7)
			{
				default_group =  groups1.get(0);
				target_groups = groups1;
			}
			//Add the last 4 users to one group alone
			else
			{
				default_group =  groups1.get(0);
				target_groups = (List<ExperimenterGroup>) groups2.get(cntr);
				cntr = cntr+1;
			}
			
			root.getSession().getAdminService().createExperimenterWithPassword(experimenter, omeroPassword, default_group, target_groups);			
		}

	}

}


