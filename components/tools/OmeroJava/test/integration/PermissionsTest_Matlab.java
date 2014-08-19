package integration;

import java.util.ArrayList;
import java.util.List;

import omero.RString;
import omero.api.IAdminPrx;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.Permissions;
import omero.model.PermissionsI;

import org.testng.annotations.Test;

import edu.emory.mathcs.backport.java.util.Arrays;

public class PermissionsTest_Matlab extends AbstractServerTest{

	@SuppressWarnings("unchecked")
	@Test
	void Database_Setup () throws Exception{
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

				ExperimenterGroup group = new ExperimenterGroupI();
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
				default_group =  groups1.get(cntr);				
				target_groups = (List<ExperimenterGroup>) Arrays.asList(new Object[] {groups2.get(cntr)});
				cntr = cntr+1;
			}
			
			root.getSession().getAdminService().createExperimenterWithPassword(experimenter, omeroPassword, default_group, target_groups);			
		}

	}

}


