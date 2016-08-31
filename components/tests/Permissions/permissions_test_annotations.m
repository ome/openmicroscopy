%Author : Balaji
%Permissions test does the following:
% i) Logs in as owner
% ii) Identifies all groups associated with owner
% iii) List them as target groups
% iv) Lists all the users part of the user group.
% v) Creates a session for each of the new users (iterates through each of their groups) and tries moving images to each of the target groups.
%     -- If there are no images within the user account, (a dummy image is uploaded to the user's group and moved / the iteration is skipped).
% vi) Checks the results of the move and appends them to the following
% result matrices : totvec,resvec, and movevec (each with increasing
% details of the move)
% vi) GIST : Iteratively moves images between groups for every
% individual user.

%Community help for this code that I used:
%Help : http://www.openmicroscopy.org/community/viewtopic.php?f=6&t=2965

function resvec = Permissions_Test_Annotations(type1)

% Check input
annotationTypes = getAnnotationTypes();
annotationNames = {annotationTypes.name};
ip = inputParser;
ip.addRequired('type1', @(x) ischar(x) && ismember(x, annotationNames));
ip.parse(Annotationtype,type1);

checktype=strcmp(type1,annoataionNames);
checktype=find(checktype==1);

%Import Packages

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
import omero.model.ExperimenterGroupI;
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

%Params
host = 'localhost';
username = 'owner';
password = 'ome';
ProjectName = 'Permissions';
DatasetName = 'Test';
importopt = 1;

%Load Omero
[client] = loadOmero(host); %The ice config should consist of the root username/password.
session = client.createSession(username,password);
client.enableKeepAlive(60);
scalingFactor=500;

%extract all users part of the userGroup
allusers=session.getAdminService.containedExperimenters(1);

%Since owner is part of all groups, this user is used to list all the
%available groups (mimics root user but is just a group owner of all the
%groups)
username = 'owner';
exp1 = session.getAdminService.lookupExperimenter(username);

%init empty arrays
movevec={};resvec={};totvec={};totvec1=[];
cntr=1;

%Upload Images to all users
%Iterate through all the users
images1=[];
for j=1:allusers.size
    
    client.closeSession();
    %Session properties to iterate
    username = char(allusers.get(j-1).getOmeName.getValue);
    if strcmp(username,'root');
        password = 'omero';
    else
        password = 'ome';
    end
    
    %New session based on user
    client = loadOmero(host);
    session = client.createSession(username, password);
    client.enableKeepAlive(60);
    exp1=session.getAdminService.lookupExperimenter(username);
    
    %Group details
    usrgroups=cell2mat(cell(session.getAdminService.getEventContext.memberOfGroups.toArray));
    
    %Extract all groups root user is part of
    exp=session.getAdminService.lookupExperimenter(username);
    groupids=cell2mat(cell(session.getAdminService.getMemberOfGroupIds(exp).toArray));
    groupno = exp.sizeOfGroupExperimenterMap();
    
    %Iterate through the groups of a given user
    for l=1:length(groupids)
        
        session.setSecurityContext(omero.model.ExperimenterGroupI(groupids(l),false));
        currgroup = char(session.getAdminService.getEventContext.groupPermissions);
        groupname = session.getAdminService.getEventContext.groupName;

        c1=getImages(session);
        for m=1:length(c1)
            images1=[images1 ; {username} {c1(m).getId.getValue} {char(groupname)} {groupids(l)} {exp.getId.getValue}];
            
        end
        disp([j l])
    end
    
    
end

group_user=cell2mat(images1(:,4:5));

resvec={};cntr=1;
%Iterate through all the users
for j=1:allusers.size
    %
    
    client.closeSession();
    %Session properties to iterate
    username = char(allusers.get(j-1).getOmeName.getValue);
    if strcmp(username,'root');
        password = 'omero';
    else
        password = 'ome';
    end
    
    comment=[username '_comment'];
    tag=[username '_tag'];
    filepath=[pwd '/Permissions_Test_Annotations.m'];
    xmlFileName = [pwd '/' username '_xml.xml'];
    
    %New session based on user
    client = loadOmero(host);
    session = client.createSession(username, password);
    client.enableKeepAlive(60);
    exp1=session.getAdminService.lookupExperimenter(username);
    
    %Group details
    usrgroups=cell2mat(cell(session.getAdminService.getEventContext.memberOfGroups.toArray));
    
    %Extract all groups root user is part of
    exp=session.getAdminService.lookupExperimenter(username);
    groupids=cell2mat(cell(session.getAdminService.getMemberOfGroupIds(exp).toArray));
    groupno = exp.sizeOfGroupExperimenterMap();
    
    %extract all the unique groups the user is part of
    userid=exp.getId.getValue;
    idx1=find(group_user(:,2)==userid);
    tempmat=images1(idx1,:);tempmat1=group_user(idx1,:);
    uniqgroups=unique(tempmat1(:,1));
    
    %Iterate through each of the groups and tag every image within the
    %group
    for i=1:length(uniqgroups)
        
        groupid=uniqgroups(i);
        session.setSecurityContext(omero.model.ExperimenterGroupI(groupid,false));
        idx2=find(group_user(:,1)==groupid);
        images2=cell2mat(images1(idx2,2));
        
        tempvec=images1(idx2,:);
        if checktype==1
            ta = writeTagAnnotation(session, tag);
        elseif checktype==2
            ta=writeFileAnnotation(session, filepath);
        elseif checktype==3
            ta=writeCommentAnnotation(session,comment);
        elseif checktype==4
            %Create a temporary matlab xml
            docNode = com.mathworks.xml.XMLUtils.createDocument('root_element');
            docRootNode = docNode.getDocumentElement;
            docRootNode.setAttribute('attr_name','attr_value');
            for ii=1:2
                thisElement = docNode.createElement('child_node');
                thisElement.appendChild...
                    (docNode.createTextNode(sprintf('%i',ii)));
                docRootNode.appendChild(thisElement);
            end
            docNode.appendChild(docNode.createComment('this is a comment'));
                     
            xmlwrite(xmlFileName,docNode);
            ta=writeXmlAnnotation(session,xml);
            delete(xmlFileName);
        end
        
        %Iterate through the groups of a given user
        for l=1:length(images2)
            
            try                %                 ca = writeCommentAnnotation(session, comment);
                %                 link1 = linkAnnotation(session, ca, 'image', images2(l));
                link2 = linkAnnotation(session, ta, 'image', images2(l));
                resvec=[resvec ; ['success' [username '-->' tempvec{l,1}] ' ' 'Groupname:' tempvec{l,3} num2str(tempvec{l,4}) num2str(tempvec{l,5})]];
            catch err
                resvec=[resvec ; [[username '-->' tempvec{l,1}] ' ' 'Groupname:' tempvec{l,3} num2str(tempvec{l,4}) num2str(tempvec{l,5}) ' Failed. ERROR : ' err.message]];
            end
            disp(resvec{l});
            
            disp([j l])
            
        end
    end
end

%Close session
client.closeSession();

