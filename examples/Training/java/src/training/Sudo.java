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

    private String sudoUsername;
    
    private String lightAdminUsername;
    
    /**
     * start-code
     */

    private void sudoLoadTagDatasets() throws Exception {
     AdminFacility admin = gateway.getFacility(AdminFacility.class);

     // Look up the experimenter to sudo for
     ExperimenterData sudoUser = admin.lookupExperimenter(ctx, sudoUsername);

     // Create a SecurityContext for this user within the user's default group
     // and set the 'sudo' flag (i.e. all operations using this context will
     // be performed as this user)
     SecurityContext sudoCtx = new SecurityContext(sudoUser.getGroupId());
     sudoCtx.setExperimenter(sudoUser);
     sudoCtx.sudo();

     // Get a sudouser's dataset (assume the user has at least one dataset)
     BrowseFacility browse = gateway.getFacility(BrowseFacility.class);
     Collection<DatasetData> datasets = browse.getDatasets(sudoCtx, sudoUser.getId());
     DatasetData sudoDataset = datasets.iterator().next();

     // Add a tag to the dataset on behalf of the sudouser (i.e. the sudouser will be
     // the owner of tag).
     DataManagerFacility dm = gateway.getFacility(DataManagerFacility.class);
     TagAnnotationData sudoUserTag = new TagAnnotationData(sudoUsername+"'s tag");
     dm.attachAnnotation(sudoCtx, sudoUserTag, sudoDataset);
     System.out.println("Added '"+sudoUserTag.getContentAsString()+"' "
             + "to dataset "+sudoDataset.getName()+" on behalf of "+sudoUsername);

     // Add a tag to the same dataset as logged in user (i. e. the logged in user will be
     // the owner of the tag). Note: This only works in a read-annotate group where the
     // logged in user is allowed to annotate the sudouser's data, or the logged in user has
     // write permission.
     TagAnnotationData adminTag = new TagAnnotationData(user.getUserName()+"'s tag");
     // Have to use a SecurityContext for the correct group, otherwise this would fail
     // with a security violation
     SecurityContext groupContext = new SecurityContext(sudoUser.getGroupId());
     dm.attachAnnotation(groupContext, adminTag, sudoDataset);
     System.out.println("Added '"+adminTag.getContentAsString()+"'"
             + " to dataset "+sudoDataset.getName()+" as admin.");
    }
    
    /**
     * end-code
     */
    /**
     * Connects and invokes the various methods.
     * @param args The login credentials.
     */

    Sudo(String[] args, String lightAdminUsername, String sudoUsername)
    {   
        this.lightAdminUsername = lightAdminUsername;
        this.sudoUsername = sudoUsername;
        
        // Login as light admin user
        for(int i=0; i<args.length; i++) {
            if(args[i].startsWith("--omero.user")) 
                args[i] = "--omero.user="+lightAdminUsername;
        }
        
        LoginCredentials cred = new LoginCredentials(args);

        gateway = new Gateway(new SimpleLogger());

        try {
            user = gateway.connect(cred);
            System.out.println("Logged in as "+user.getUserName());
            ctx = new SecurityContext(user.getGroupId());
            
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
        
        String sudoUsername = "";
        String lightAdminUsername = "";
        
        new Sudo(args, lightAdminUsername, sudoUsername);
        System.exit(0);
    }

}
