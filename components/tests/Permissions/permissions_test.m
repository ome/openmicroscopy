%Author : Balaji
%Permissions test does the following:
% i) Logs in as root user
% ii) Creates Groups(of varying permissions) and users for the test (iteratively).
% iii) Adds the root user to all the groups.
% iv) Extracts all the groups/groupids the root user is part of. (Iteratively marks them as the target group for the moves).
% v) Creates a session for each of the new users (created during the script) and tries moving images to each of the target groups root user is part of.
%     -- If there are no images within the user account, a dummy image is uploaded to the user's group and moved.
% vi) Groups created : 'rwra--','rw----','rwr---'
% vii) Moves done : Iteratively moves images between each of these groups, and also moves images from the root user to each of these groups.  
    
clear all;close all;

%Help : http://www.openmicroscopy.org/community/viewtopic.php?f=6&t=2965
import omero.cmd.Chgrp;
import omero.cmd.DoAll
import omero.cmd.CmdCallbackI;

import omero.rtypes.rdouble;
import omero.rtypes.rint;

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
import omero.model.ExperimenterGroup;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.LogicalChannel;
import omero.model.OriginalFile;
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
% String perms = "rw----";

%Params
% host= 'localhost';  %Host address
% username = 'root username';  %Username for Insight
% password = 'root password'; %Password for Insight
ProjectName = 'Permissions';
DatasetName = 'Test';
importopt = 2;
tempnumber=135;

%Load Omero
[client session] = loadOmero('ice.config'); %The ice config should consist of the root username/password.
% session = client.createSession(username, password);
client.enableKeepAlive(60);
scalingFactor=500;

perm_table={'rwra--','rw----','rwr---'};
%Create Group
cntr=tempnumber;
omero.model.ExperimenterGroupI();
userGroup = omero.model.ExperimenterGroupI(1,false);%Put the user in root
groups=userGroup;

for i=1:3
    GroupName = (['Permission_tester' num2str(i+cntr)]);
    group = omero.model.ExperimenterGroupI();
    group.setName(rstring(GroupName));
    group.getDetails().setPermissions(omero.model.PermissionsI(perm_table{i}));
    newgroupid = session.getAdminService.createGroup(group);

    newGroup(i) = omero.model.ExperimenterGroupI(newgroupid, false);
    groups = ([groups newGroup(i)]);

    groups1=toJavaList(groups);

    %Create User
    omeroUsername = ['Random_test' num2str(i+cntr)];
    experimenter = omero.model.ExperimenterI();
    experimenter.setFirstName(rstring('user_'))
    experimenter.setLastName(rstring([num2str(i+cntr)]))
    experimenter.setOmeName(rstring(omeroUsername))
    session.getAdminService.createExperimenterWithPassword(experimenter, rstring('ome'), newGroup(i), groups1);

end
%Add root to all the groups1
session.getAdminService.addGroups(omero.model.ExperimenterI(session.getAdminService.getEventContext.userId(), false), groups1);

%Upload annotations to each of the users

%Upload tags to each of the users

%Create Iterative move
%Create commands to move and create the link in target
exp=session.getAdminService.lookupExperimenter(username);
groupno = exp.sizeOfGroupExperimenterMap();
cntr=tempnumber;
movevec={};
for j=1:4
        
    if j>1
        cntr=cntr+1;
        client.closeSession();
        %Session properties to iterate
        username = ['Random_test' num2str(cntr)];  %Username for Insight
        password = 'ome'; %Password for Insight
        
        %New session based on user
        session = client.createSession(username, password);
        client.enableKeepAlive(60);
        exp1=session.getAdminService.lookupExperimenter(username);
    end
    
    usrgroups=cell2mat(cell(session.getAdminService.getEventContext.memberOfGroups.toArray));
    currgroup = char(session.getAdminService.getEventContext.groupPermissions);
    
    for i=1:groupno-1
        groups = exp.getGroupExperimenterMap(i);
        if j==1
            exp1=exp;
        end
        
        g =groups.getParent;
        perms = char(g.getDetails().getPermissions());
        targetgroup = g.getId.getValue();
        
        list = ArrayList();
        images=getImages(session);
        if isempty(images)
            testimagpath='/Users/bramalingam/Desktop/Screen Shot 2014-06-16 at 10.15.59.png';
            upload_image(testimagpath,session,username,password,ProjectName,DatasetName,host,importopt);
            images=getImages(session);
        end
        
        %         list.add(Chgrp('/Fileset', images(1).getFileset.getId().getValue, [], g.getId().getValue));
        list.add(Chgrp('/Image', images(1).getId().getValue, [], targetgroup));
        
        %Prepare request
        all = DoAll();
        all.requests = list;
        
        handle1 = session.submit(all);
        cb = CmdCallbackI(client, handle1);
%         disp([char(images(1).getName.getValue) ':number of images in the fileset = ' num2str(images(1).getFileset.IMAGES.length)])
        cb.loop(10 * all.requests.length, scalingFactor * images(1).getFileset().USEDFILES.length * 10);
        %     cntr=cntr+1;
        
        movevec = [movevec ; [num2str(ismember(targetgroup,usrgroups)) '_' char(session.getAdminService.getEventContext.groupName) '(' currgroup ')' '>' char(g.getName.getValue) '(' perms ')' '[image_name:' char(images(1).getName.getValue) ']---->' char(cb.getResponse)]];
        
        disp(movevec(i,:))
    end
    
end
client.closeSession();

% session.getAdminService.createGroup(group);
% userId = session.getAdminService().getEventContext().userId;
% group= session.getAdminService().getGroup(session.getAdminService.getEventContext.groupId());
% groups = java.util.ArrayList();
% for i = 1:N, groups.add(group); end
% userid=[];
% groupid=[];

% clear all;close all;
% unloadOmero;
% cntr=tempnumber;
% %Upload images to each of the users
% for k=1:3
% 
%     %Params
%     host= 'localhost';  %Host address
%     username = ['Random_test' num2str(k+cntr)];  %Username for Insight
%     password = 'ome'; %Password for Insight
% 
%     %Load Omero
%     client = loadOmero(host);
%     session = client.createSession(username, password);
%     user1=session.getAdminService.lookupExperimenter(username);
%     g=user1.getGroups;
%     session.setSecurityContext(ExperimenterGroup(g(1).id, false));
% %     client.enableKeepAlive(60);
% 
% %     imagesc(magic(512));
% %     time1=clock;
% %     ImageName=[username '_' date '_' num2str(time1(4)) '_' num2str(time1(5)) '.jpeg'];
% %     saveas(gca,ImageName);
% %     path=[pwd '/' ImageName];
% 
%     %upload image
% %     upload_image(path,session,username,password,ProjectName,DatasetName,host);
%     client.closeSession();
% 
% end
