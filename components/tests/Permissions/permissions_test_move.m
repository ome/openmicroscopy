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

function [totvec1,resvec,detailed_result] = permissions_test_move()

%Init
clear all;close all;

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

%Iterate through all the users
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
        
        %Iterate through all target groups
        for i=1:groupno-1
            
            groups = exp1.getGroupExperimenterMap(i);
            g =groups.getParent;
            perms = char(g.getDetails().getPermissions());
            targetgroup = g.getId.getValue();
            
            images=getImages(session);
            if isempty(images)
                continue
                testimagpath='/Users/bramalingam/Desktop/Screen Shot 2014-06-16 at 10.15.59.png'; %#ok<*UNRCH>
                upload_image(testimagpath,session,username,password,ProjectName,DatasetName,host,importopt);
                images=getImages(session);
            end
            
            %Load fileset
            filesetId = images(1).getFileset.getId().getValue;
            query = ['select obj from Fileset obj '...
                'left outer join fetch obj.images as image '...
                'left outer join fetch obj.usedFiles as usedFile '...
                'join fetch usedFile.originalFile '...
                'left outer join fetch image.wellSamples as wlinks ' ...
                'left outer join fetch image.datasetLinks as dlinks '...
                'where obj.id = ' num2str(filesetId)];
            
            fileset = session.getQueryService().findAllByQuery(query, []);
            fileset = toMatlabList(fileset);
            fsImages = toMatlabList(fileset.copyImages());
            
            
            %Checkpoint for the kind of image
            Imtype = fsImages(1).sizeOfWellSamples;
            
            %extract Image annotations before move
            annvec=[];
            for m=1:numel(fsImages)
                imageId=fsImages(m).getId().getValue();
                ann(1)=double(~isempty(getImageFileAnnotations(session,imageId)));
                ann(2)=double(~isempty(getImageTagAnnotations(session,imageId)));
                ann(3)=double(~isempty(getImageCommentAnnotations(session,imageId)));
                ann(4)=double(~isempty(getImageXmlAnnotations(session,imageId)));
                annvec=[annvec ; ann];
            end
            ann=sum(annvec,1);
            
            %Prepare request
            list = ArrayList();
            %Add to list
            if Imtype == 1 %Do this if the given image is part of a screen
                query = ['select obj from Image obj '...
                    'left outer join fetch obj.wellSamples as wlinks '...
                    'join fetch wlinks.well as pId '...
                    'where obj.id = ' num2str(images(1).getId.getValue)];
                
                fileset = session.getQueryService().findAllByQuery(query, []);
                fileset = toMatlabList(fileset);
                wellSamples=toMatlabList(fileset.copyWellSamples);
                plateId=wellSamples.getPlateAcquisition.getId.getValue;
                list.add(Chgrp('/Plate', plateId, [], targetgroup));
                continue
            else
                list.add(Chgrp('/Fileset', filesetId, [], targetgroup));
            end
            all = DoAll();
            all.requests = list;
            
            %Submit request for move
            handle1 = session.submit(all);
            cb = CmdCallbackI(client, handle1);
            cb.loop(10 * all.requests.size, scalingFactor *  numel(fsImages) * 100);
            
            
            %Extract Image annotations post move
            annvec=[];
            for m=1:numel(fsImages)
                imageId=fsImages(m).getId().getValue();
                ann1(1)=double(~isempty(getImageFileAnnotations(session,imageId)));
                ann1(2)=double(~isempty(getImageTagAnnotations(session,imageId)));
                ann1(3)=double(~isempty(getImageCommentAnnotations(session,imageId)));
                ann1(4)=double(~isempty(getImageXmlAnnotations(session,imageId)));
                annvec=[annvec ; ann1];
            end
            ann1=sum(annvec,1);
            
            checkuserintgtgroup=double(ismember(targetgroup,usrgroups));
            detailed_result = [username '-->' char(groupname) '(' currgroup ')' '>' char(g.getName.getValue) '(' perms ')' '[image_name:' char(images(1).getName.getValue) ']---->' char(cb.getResponse)];
            movevec = [movevec ; detailed_result]; %#ok<*AGROW>
            
            %Check response of the callback object
            checkpoint = findstr('DoAllRsp',char(cb.getResponse)); %#ok<FSTR>
            if ~isempty(checkpoint)
                ResultofImageMove=1;
            else
                ResultofImageMove=-1;
            end
            resvec = [resvec ; num2str(ann) '--->' num2str(ann1) '--->' num2str([checkuserintgtgroup ResultofImageMove])];
            
            %Check if the annotations have moved properly or not
            checkval=strcmp(num2str(ann),num2str(ann1));
            if checkval==0
                checkval=-1;
            else
                checkval=1;
            end
            
            totvec = [totvec ; num2str(sum(ann)) num2str(checkval) '--->' num2str([checkuserintgtgroup ResultofImageMove])];
            
            disp(detailed_result)
            totalmoves = groupno * length(groupids) *allusers.size;
            totvec1=[totvec1 ; sum(ann) checkval checkuserintgtgroup ResultofImageMove];
            disp([cntr j l i totalmoves])
            cntr=cntr+1;
            
        end
    end
end

%Close session
client.closeSession();

