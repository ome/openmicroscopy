/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2018 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package training;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;

import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.facility.AdminFacility;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.DataManagerFacility;
import omero.log.SimpleLogger;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;
import omero.gateway.model.TagAnnotationData;

/** 
 * Sample code showing how to use sudo.
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class Sudo 
{

    //The value used if the configuration file is not used. To edit*/
    /** The server address.*/
    private static String hostName = "serverName";

    /** The username.*/
    private static String userName = "userName";

    /** The password.*/
    private static String password = "password";

    /** Information to edit.*/
    private static long imageId = 1;
    //end edit

    private Gateway gateway;

    private ExperimenterData user;
    
    private SecurityContext ctx;

    private String sudoUserName = UUID.randomUUID().toString().substring(0, 8);
    
    /**
     * start-code
     */

    private void sudoLoadTagDatasets() throws Exception {
     AdminFacility admin = gateway.getFacility(AdminFacility.class);

     // Look up the experimenter to sudo for
     ExperimenterData sudoUser = admin.lookupExperimenter(ctx, sudoUserName);

     // Create a SecurityContext for this user within the user's default group
     // and set the 'sudo' flag (i.e. all operations using this context will
     // be performed as this user)
     SecurityContext sudoCtx = new SecurityContext(sudoUser.getGroupId());
     sudoCtx.setExperimenter(sudoUser);
     sudoCtx.sudo();

     // Get the sudouser's datasets
     BrowseFacility browse = gateway.getFacility(BrowseFacility.class);
     Collection<DatasetData> datasets = browse.getDatasets(sudoCtx, sudoUser.getId());
     Iterator<DatasetData> i = datasets.iterator();
     DatasetData first = null;
     while (i.hasNext()) {
         DatasetData dataset = i.next();
         System.out.println(dataset.getName()+" ("+dataset.getId()+")");

         if (first == null)
             first = dataset;
     }

     // Add a tag to the first dataset on behalf of the sudouser (i.e. the sudouser will be
     // the owner of tag).
     DataManagerFacility dm = gateway.getFacility(DataManagerFacility.class);
     TagAnnotationData sudoUserTag = new TagAnnotationData(sudoUserName+"'s tag");
     dm.attachAnnotation(sudoCtx, sudoUserTag, first);
     System.out.println("Added '"+sudoUserTag.getContentAsString()+"' "
             + "to dataset "+first.getName()+" on behalf of "+sudoUserName);

     // Add a tag to the same dataset as logged in user (i. e. the logged in user will be
     // the owner of the tag). Note: This only works in a read-annotate group where the
     // logged in user is allowed to annotate the sudouser's data, or the logged in user has
     // write permission.
     TagAnnotationData adminTag = new TagAnnotationData(user.getUserName()+"'s tag");
     // Have to use a SecurityContext for the correct group, otherwise this would fail
     // with a security violation
     SecurityContext groupContext = new SecurityContext(sudoUser.getGroupId());
     dm.attachAnnotation(groupContext, adminTag, first);
     System.out.println("Added '"+adminTag.getContentAsString()+"'"
             + " to dataset "+first.getName()+" as admin.");
    }
    
    /**
     * end-code
     */
    /**
     * Connects and invokes the various methods.
     * @param args The login credentials.
     */
    
    /**
     * Create a test user we can sudo for
     * 
     * @throws Exception
     */
    private void prepareSudoUser() throws Exception {
        AdminFacility admin = gateway.getFacility(AdminFacility.class);
        GroupData g = new GroupData();
        g.setName(UUID.randomUUID().toString().substring(0, 8));
        g = admin.createGroup(ctx, g, null, GroupData.PERMISSIONS_GROUP_READ);
        ExperimenterData exp = new ExperimenterData();
        exp.setFirstName("Test");
        exp.setLastName("Blup");
        ExperimenterData sudoUser = admin.createExperimenter(ctx, exp,
                sudoUserName, "test", Collections.singletonList(g), false,
                false);

        SecurityContext sudoCtx = new SecurityContext(g.getId());
        sudoCtx.setExperimenter(sudoUser);
        sudoCtx.sudo();

        DatasetData ds = new DatasetData();
        ds.setName("Test_Dataset");
        DataManagerFacility df = gateway.getFacility(DataManagerFacility.class);
        df.createDataset(sudoCtx, ds, null);
    }
    
    Sudo(String[] args)
    {   
        LoginCredentials cred = new LoginCredentials(args);

        gateway = new Gateway(new SimpleLogger());

        try {
            user = gateway.connect(cred);
            ctx = new SecurityContext(user.getGroupId());
            
            prepareSudoUser();
            
            sudoLoadTagDatasets();
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                gateway.disconnect(); // Be sure to disconnect
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Runs the script without configuration options.
     *
     * @param args The login credentials.
     */
    public static void main(String[] args)
    {
        if (args == null || args.length == 0)
            args = new String[] { "--omero.host=" + hostName,
                "--omero.user=" + userName, "--omero.pass=" + password };
        
        new Sudo(args);
        System.exit(0);
    }

}
