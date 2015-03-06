% Copyright (C) 2011-2014 University of Dundee & Open Microscopy Environment.
% All rights reserved.
%
% This program is free software; you can redistribute it and/or modify
% it under the terms of the GNU General Public License as published by
% the Free Software Foundation; either version 2 of the License, or
% (at your option) any later version.
%
% This program is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
% GNU General Public License for more details.
%
% You should have received a copy of the GNU General Public License along
% with this program; if not, write to the Free Software Foundation, Inc.,
% 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

% File annotation constants
filePath = 'mydata.txt';
fileContent = 'file annotation content';
newFileContent = [fileContent ' modified'];
fileMimeType = 'application/octet-stream';
fileDescription = 'file annotation description';
newFileDescription = [fileDescription ' modified'];
ns = 'examples.training.matlab';
fileOutputPath = 'mydataBack.txt';

hasAnnotation = @(x, y) ismember(x.getId().getValue(),...
    arrayfun(@(t) t.getId().getValue(), y));

try
    % Initialize a client and a session using the ice.config file
    % See ConnectToOMERO for alternative ways to initialize a session
    [client, session] = loadOmero();
    p = parseOmeroProperties(client);
    eventContext = session.getAdminService().getEventContext();
    fprintf(1, 'Created connection to %s\n', p.hostname);
    msg = 'Created session for user %s (id: %g) using group %s (id: %g)\n';
    fprintf(1, msg, char(eventContext.userName), eventContext.userId,...
        char(eventContext.groupName), eventContext.groupId);
    
    % Information to edit
    imageId = p.imageid;
    datasetId = p.datasetid;
    projectId = p.projectid;
    plateId = p.plateid;
    screenId = p.screenid;
    
    %% File Annotation
    disp('File annotation');
    % Create a local file
    fprintf(1, 'Creating local file with content: %s\n', fileContent);
    fid = fopen(filePath, 'w');
    fwrite(fid, fileContent);
    fclose(fid);
    
    % Create a file annotation
    fileAnnotation = writeFileAnnotation(session, filePath,...
        'mimetype', fileMimeType, 'description', fileDescription,...
        'namespace', ns);
    fprintf(1, 'Created file annotation %g\n',...
        fileAnnotation.getId().getValue());
    
    % Delete the local file
    delete(filePath);
    
    % Download the content of the file annotation
    fprintf(1, 'Reading content of file annotation %g\n',...
        fileAnnotation.getId().getValue());
    getFileAnnotationContent(session, fileAnnotation, fileOutputPath);
    
    % Read the downloaded content
    fid = fopen(fileOutputPath, 'r');
    readContent = fread(fid);
    fclose(fid);
    fprintf(1, 'File content: %s\n', readContent);
    
    % Delete the local file
    delete(fileOutputPath);
    
    % Update the local file
    fprintf(1, 'Creating local file with content: %s\n', newFileContent);
    fid = fopen(filePath, 'w');
    fwrite(fid, newFileContent);
    fclose(fid);
    
    % Update the file annotation on the server
    fileAnnotation = updateFileAnnotation(session, fileAnnotation, filePath,...
        'description', newFileDescription);
    delete(filePath);
    
    % Read the content of the updated file annotation
    fprintf(1, 'Reading content of updated file annotation %g\n',...
        fileAnnotation.getId().getValue());
    getFileAnnotationContent(session, fileAnnotation, fileOutputPath);
    fid = fopen(fileOutputPath, 'r');
    readContent = fread(fid);
    fclose(fid);
    fprintf(1, 'Updated file content: %s\n', readContent);
    delete(fileOutputPath);
    
    % Project - Annotation link
    fa = omero.model.FileAnnotationI(fileAnnotation.getId().getValue(), false);
    linkAnnotation(session, fa, 'project', projectId);
    fprintf(1, 'Linked file annotation to project %g\n', projectId);
    fprintf(1, 'Retrieving file annotations attached to project %g with namespace %s\n',...
        projectId, ns);
    fas = getProjectFileAnnotations(session, projectId, 'include', ns);
    assert(hasAnnotation(fa, fas), 'WriteData: Could not find annotation');
    
    % Dataset - Annotation link
    linkAnnotation(session, fa, 'dataset', datasetId);
    fprintf(1, 'Linked file annotation to project %g\n', datasetId);
    fprintf(1, 'Retrieving file annotations attached to dataset %g with namespace %s\n',...
        datasetId, ns);
    fas = getDatasetFileAnnotations(session, datasetId, 'include', ns);
    assert(hasAnnotation(fa, fas), 'WriteData: Could not find annotation');
    
    % Image - Annotation link
    linkAnnotation(session, fa, 'image', imageId);
    fprintf(1, 'Linked file annotation to image %g\n', imageId);
    fprintf(1, 'Retrieving file annotations attached to image %g with namespace %s\n',...
        imageId, ns);
    fas = getImageFileAnnotations(session, imageId, 'include', ns);
    assert(hasAnnotation(fa, fas), 'WriteData: Could not find annotation');
    
    % Plate - Annotation link
    linkAnnotation(session, fa, 'plate', plateId);
    fprintf(1, 'Linked file annotation to plate %g\n', plateId);
    fprintf(1, 'Retrieving file annotations attached to plate %g with namespace %s\n',...
        plateId, ns);
    fas = getPlateFileAnnotations(session, plateId, 'include', ns);
    assert(hasAnnotation(fa, fas), 'WriteData: Could not find annotation');
    
    % Screen - Annotation link
    linkAnnotation(session, fa, 'screen', screenId);
    fprintf(1, 'Linked file annotation to screen %g\n', screenId);
    fprintf(1, 'Retrieving file annotations attached to screen %g with namespace %s\n',...
        screenId, ns);
    fas = getScreenFileAnnotations(session, screenId, 'include', ns);
    assert(hasAnnotation(fa, fas), 'WriteData: Could not find annotation');
    
    %% Comment Annotation
    disp('Comment annotation');
    commentAnnotation = writeCommentAnnotation(session, 'comment',...
        'description', 'comment description', 'namespace', ns);
    fprintf(1, 'Created comment annotation %g\n',...
        commentAnnotation.getId().getValue());
    fprintf(1, 'Retrieving comment annotation %g\n',...
        commentAnnotation.getId().getValue());
    annotation = getCommentAnnotations(session,...
        commentAnnotation.getId().getValue());
    assert(~isempty(annotation), 'WriteData: Could not find annotation');
    
    % Project - Annotation link
    ca = omero.model.CommentAnnotationI(commentAnnotation.getId().getValue(), false);
    linkAnnotation(session, ca, 'project', projectId);
    fprintf(1, 'Linked comment annotation to project %g\n', projectId);
    fprintf(1, 'Retrieving comment annotations attached to project %g with namespace %s\n',...
        projectId, ns);
    cas = getProjectCommentAnnotations(session, projectId, 'include', ns);
    assert(hasAnnotation(ca, cas), 'WriteData: Could not find annotation');
    
    % Dataset - Annotation link
    linkAnnotation(session, ca, 'dataset', datasetId);
    fprintf(1, 'Linked comment annotation to project %g\n', datasetId);
    fprintf(1, 'Retrieving comment annotations attached to dataset %g with namespace %s\n',...
        datasetId, ns);
    cas = getDatasetCommentAnnotations(session, datasetId, 'include', ns);
    assert(hasAnnotation(ca, cas), 'WriteData: Could not find annotation');
    
    % Image - Annotation link
    linkAnnotation(session, ca, 'image', imageId);
    fprintf(1, 'Linked comment annotation to image %g\n', imageId);
    fprintf(1, 'Retrieving comment annotations attached to image %g with namespace %s\n',...
        imageId, ns);
    cas = getImageCommentAnnotations(session, imageId, 'include', ns);
    assert(hasAnnotation(ca, cas), 'WriteData: Could not find annotation');
    
    % Plate - Annotation link
    linkAnnotation(session, ca, 'plate', plateId);
    fprintf(1, 'Linked comment annotation to plate %g\n', plateId);
    fprintf(1, 'Retrieving comment annotations attached to plate %g with namespace %s\n',...
        plateId, ns);
    cas = getPlateCommentAnnotations(session, plateId, 'include', ns);
    assert(hasAnnotation(ca, cas), 'WriteData: Could not find annotation');
    
    % Screen - Annotation link
    linkAnnotation(session, ca, 'screen', screenId);
    fprintf(1, 'Linked comment annotation to screen %g\n', screenId);
    fprintf(1, 'Retrieving comment annotations attached to screen %g with namespace %s\n',...
        screenId, ns);
    cas = getScreenCommentAnnotations(session, screenId, 'include', ns);
    assert(hasAnnotation(ca, cas), 'WriteData: Could not find annotation');
    
    %% Double Annotation
    disp('Double annotation');
    doubleAnnotation = writeDoubleAnnotation(session, .5,...
        'description', 'double description', 'namespace', ns);
    fprintf(1, 'Created double annotation %g\n',...
        doubleAnnotation.getId().getValue());
    fprintf(1, 'Retrieving double annotation %g\n',...
        doubleAnnotation.getId().getValue());
    annotation = getDoubleAnnotations(session,...
        doubleAnnotation.getId().getValue());
    assert(~isempty(annotation), 'WriteData: Could not find annotation');
    
    % Project - Annotation link
    da = omero.model.DoubleAnnotationI(doubleAnnotation.getId().getValue(), false);
    linkAnnotation(session, da, 'project', projectId);
    fprintf(1, 'Linked double annotation to project %g\n', projectId);
    fprintf(1, 'Retrieving double annotations attached to project %g with namespace %s\n',...
        projectId, ns);
    das = getProjectDoubleAnnotations(session, projectId, 'include', ns);
    assert(hasAnnotation(da, das), 'WriteData: Could not find annotation');
    
    % Dataset - Annotation link
    linkAnnotation(session, da, 'dataset', datasetId);
    fprintf(1, 'Linked double annotation to project %g\n', datasetId);
    fprintf(1, 'Retrieving double annotations attached to dataset %g with namespace %s\n',...
        datasetId, ns);
    das = getDatasetDoubleAnnotations(session, datasetId, 'include', ns);
    assert(hasAnnotation(da, das), 'WriteData: Could not find annotation');
    
    % Image - Annotation link
    linkAnnotation(session, da, 'image', imageId);
    fprintf(1, 'Linked double annotation to image %g\n', imageId);
    fprintf(1, 'Retrieving double annotations attached to image %g with namespace %s\n',...
        imageId, ns);
    das = getImageDoubleAnnotations(session, imageId, 'include', ns);
    assert(hasAnnotation(da, das), 'WriteData: Could not find annotation');
    
    % Plate - Annotation link
    linkAnnotation(session, da, 'plate', plateId);
    fprintf(1, 'Linked double annotation to plate %g\n', plateId);
    fprintf(1, 'Retrieving double annotations attached to plate %g with namespace %s\n',...
        plateId, ns);
    das = getPlateDoubleAnnotations(session, plateId, 'include', ns);
    assert(hasAnnotation(da, das), 'WriteData: Could not find annotation');
    
    % Screen - Annotation link
    linkAnnotation(session, da, 'screen', screenId);
    fprintf(1, 'Linked double annotation to screen %g\n', screenId);
    fprintf(1, 'Retrieving double annotations attached to screen %g with namespace %s\n',...
        screenId, ns);
    das = getScreenDoubleAnnotations(session, screenId, 'include', ns);
    assert(hasAnnotation(da, das), 'WriteData: Could not find annotation');
    
    %% Long Annotation
    disp('Long annotation');
    longAnnotation = writeLongAnnotation(session, 1,...
        'description', 'long description', 'namespace', ns);
    fprintf(1, 'Created long annotation %g\n',...
        longAnnotation.getId().getValue());
    fprintf(1, 'Retrieving long annotation %g\n',...
        longAnnotation.getId().getValue());
    annotation = getLongAnnotations(session,...
        longAnnotation.getId().getValue());
    assert(~isempty(annotation), 'WriteData: Could not find annotation');
    
    % Project - Annotation link
    la = omero.model.LongAnnotationI(longAnnotation.getId().getValue(), false);
    linkAnnotation(session, la, 'project', projectId);
    fprintf(1, 'Linked long annotation to project %g\n', projectId);
    fprintf(1, 'Retrieving long annotations attached to project %g with namespace %s\n',...
        projectId, ns);
    las = getProjectLongAnnotations(session, projectId, 'include', ns);
    assert(hasAnnotation(la, las), 'WriteData: Could not find annotation');
    
    % Dataset - Annotation link
    linkAnnotation(session, la, 'dataset', datasetId);
    fprintf(1, 'Linked long annotation to project %g\n', datasetId);
    fprintf(1, 'Retrieving long annotations attached to dataset %g with namespace %s\n',...
        datasetId, ns);
    las = getDatasetLongAnnotations(session, datasetId, 'include', ns);
    assert(hasAnnotation(la, las), 'WriteData: Could not find annotation');
    
    % Image - Annotation link
    linkAnnotation(session, la, 'image', imageId);
    fprintf(1, 'Linked long annotation to image %g\n', imageId);
    fprintf(1, 'Retrieving long annotations attached to image %g with namespace %s\n',...
        imageId, ns);
    las = getImageLongAnnotations(session, imageId, 'include', ns);
    assert(hasAnnotation(la, las), 'WriteData: Could not find annotation');
    
    % Plate - Annotation link
    linkAnnotation(session, la, 'plate', plateId);
    fprintf(1, 'Linked long annotation to plate %g\n', plateId);
    fprintf(1, 'Retrieving long annotations attached to plate %g with namespace %s\n',...
        plateId, ns);
    las = getPlateLongAnnotations(session, plateId, 'include', ns);
    assert(hasAnnotation(la, las), 'WriteData: Could not find annotation');
    
    % Screen - Annotation link
    linkAnnotation(session, la, 'screen', screenId);
    fprintf(1, 'Linked long annotation to screen %g\n', screenId);
    fprintf(1, 'Retrieving long annotations attached to screen %g with namespace %s\n',...
        screenId, ns);
    las = getScreenLongAnnotations(session, screenId, 'include', ns);
    assert(hasAnnotation(la, las), 'WriteData: Could not find annotation');
 
    %% Map Annotation
    disp('Map annotation');
    mapAnnotation = writeMapAnnotation(session, 'key', 'value',...
        'description', 'map description', 'namespace', ns);
    fprintf(1, 'Created map annotation %g\n',...
        mapAnnotation.getId().getValue());
    fprintf(1, 'Retrieving map annotation %g\n',...
        mapAnnotation.getId().getValue());
    annotation = getAnnotations(session,...
        mapAnnotation.getId().getValue(), 'map');
    assert(~isempty(annotation), 'WriteData: Could not find annotation');
    
    % Project - Annotation link
    ma = omero.model.MapAnnotationI(mapAnnotation.getId().getValue(), false);
    linkAnnotation(session, ma, 'project', projectId);
    fprintf(1, 'Linked map annotation to project %g\n', projectId);
    fprintf(1, 'Retrieving map annotations attached to project %g with namespace %s\n',...
        projectId, ns);
    mas = getObjectAnnotations(session, 'map', 'project', projectId, 'include', ns);
    assert(hasAnnotation(ma, mas), 'WriteData: Could not find annotation');
    
    % Dataset - Annotation link
    linkAnnotation(session, ma, 'dataset', datasetId);
    fprintf(1, 'Linked map annotation to dataset %g\n', datasetId);
    fprintf(1, 'Retrieving map annotations attached to dataset %g with namespace %s\n',...
        datasetId, ns);
    mas = getObjectAnnotations(session, 'map', 'dataset', datasetId, 'include', ns);
    assert(hasAnnotation(ma, mas), 'WriteData: Could not find annotation');
    
    % Image - Annotation link
    linkAnnotation(session, ma, 'image', imageId);
    fprintf(1, 'Linked map annotation to image %g\n', imageId);
    fprintf(1, 'Retrieving map annotations attached to image %g with namespace %s\n',...
        imageId, ns);
    mas = getObjectAnnotations(session, 'map', 'image', imageId, 'include', ns);
    assert(hasAnnotation(ma, mas), 'WriteData: Could not find annotation');
    
    % Plate - Annotation link
    linkAnnotation(session, ma, 'plate', plateId);
    fprintf(1, 'Linked map annotation to plate %g\n', plateId);
    fprintf(1, 'Retrieving map annotations attached to plate %g with namespace %s\n',...
        plateId, ns);
    mas = getObjectAnnotations(session, 'map', 'plate', plateId, 'include', ns);
    assert(hasAnnotation(ma, mas), 'WriteData: Could not find annotation');
    
    % Screen - Annotation link
    linkAnnotation(session, ma, 'screen', screenId);
    fprintf(1, 'Linked map annotation to screen %g\n', screenId);
    fprintf(1, 'Retrieving map annotations attached to screen %g with namespace %s\n',...
        screenId, ns);
    mas = getObjectAnnotations(session, 'map', 'screen', screenId, 'include', ns);
    assert(hasAnnotation(ma, mas), 'WriteData: Could not find annotation');
 
    %% Tag Annotation
    disp('Tag annotation');
    tagAnnotation = writeTagAnnotation(session, 'tag value',...
        'description', 'tag description', 'namespace', ns);
    fprintf(1, 'Created tag annotation %g\n',...
        tagAnnotation.getId().getValue());
    fprintf(1, 'Retrieving tag annotation %g\n',...
        tagAnnotation.getId().getValue());
    annotation = getTagAnnotations(session,...
        tagAnnotation.getId().getValue());
    assert(~isempty(annotation), 'WriteData: Could not find annotation');
    
    % Project - Annotation link
    ta = omero.model.TagAnnotationI(tagAnnotation.getId().getValue(), false);
    linkAnnotation(session, ta, 'project', projectId);
    fprintf(1, 'Linked tag annotation to project %g\n', projectId);
    fprintf(1, 'Retrieving tag annotations attached to project %g with namespace %s\n',...
        projectId, ns);
    tas = getProjectTagAnnotations(session, projectId, 'include', ns);
    assert(hasAnnotation(ta, tas), 'WriteData: Could not find annotation');
    
    % Dataset - Annotation link
    linkAnnotation(session, ta, 'dataset', datasetId);
    fprintf(1, 'Linked tag annotation to project %g\n', datasetId);
    fprintf(1, 'Retrieving tag annotations attached to dataset %g with namespace %s\n',...
        datasetId, ns);
    tas = getDatasetTagAnnotations(session, datasetId, 'include', ns);
    assert(hasAnnotation(ta, tas), 'WriteData: Could not find annotation');
    
    % Image - Annotation link
    linkAnnotation(session, ta, 'image', imageId);
    fprintf(1, 'Linked tag annotation to image %g\n', imageId);
    fprintf(1, 'Retrieving tag annotations attached to image %g with namespace %s\n',...
        imageId, ns);
    tas = getImageTagAnnotations(session, imageId, 'include', ns);
    assert(hasAnnotation(ta, tas), 'WriteData: Could not find annotation');
    
    % Plate - Annotation link
    linkAnnotation(session, ta, 'plate', plateId);
    fprintf(1, 'Linked tag annotation to plate %g\n', plateId);
    fprintf(1, 'Retrieving tag annotations attached to plate %g with namespace %s\n',...
        plateId, ns);
    tas = getPlateTagAnnotations(session, plateId, 'include', ns);
    assert(hasAnnotation(ta, tas), 'WriteData: Could not find annotation');
    
    % Screen - Annotation link
    linkAnnotation(session, ta, 'screen', screenId);
    fprintf(1, 'Linked tag annotation to screen %g\n', screenId);
    fprintf(1, 'Retrieving tag annotations attached to screen %g with namespace %s\n',...
        screenId, ns);
    tas = getScreenTagAnnotations(session, screenId, 'include', ns);
    assert(hasAnnotation(ta, tas), 'WriteData: Could not find annotation');
    
    %% Timestamp Annotation
    disp('Timestamp annotation');
    timestampAnnotation = writeTimestampAnnotation(session, now,...
        'description', 'timestamp description', 'namespace', ns);
    fprintf(1, 'Created timestamp annotation %g\n',...
        timestampAnnotation.getId().getValue());
    fprintf(1, 'Retrieving timestamp annotation %g\n',...
        timestampAnnotation.getId().getValue());
    annotation = getTimestampAnnotations(session,...
        timestampAnnotation.getId().getValue());
    assert(~isempty(annotation), 'WriteData: Could not find annotation');
    
    % Project - Annotation link
    ta = omero.model.TimestampAnnotationI(timestampAnnotation.getId().getValue(), false);
    linkAnnotation(session, ta, 'project', projectId);
    fprintf(1, 'Linked timestamp annotation to project %g\n', projectId);
    fprintf(1, 'Retrieving timestamp annotations attached to project %g with namespace %s\n',...
        projectId, ns);
    tas = getProjectTimestampAnnotations(session, projectId, 'include', ns);
    assert(hasAnnotation(ta, tas), 'WriteData: Could not find annotation');
    
    % Dataset - Annotation link
    linkAnnotation(session, ta, 'dataset', datasetId);
    fprintf(1, 'Linked timestamp annotation to project %g\n', datasetId);
    fprintf(1, 'Retrieving timestamp annotations attached to dataset %g with namespace %s\n',...
        datasetId, ns);
    tas = getDatasetTimestampAnnotations(session, datasetId, 'include', ns);
    assert(hasAnnotation(ta, tas), 'WriteData: Could not find annotation');
    
    % Image - Annotation link
    linkAnnotation(session, ta, 'image', imageId);
    fprintf(1, 'Linked timestamp annotation to image %g\n', imageId);
    fprintf(1, 'Retrieving timestamp annotations attached to image %g with namespace %s\n',...
        imageId, ns);
    tas = getImageTimestampAnnotations(session, imageId, 'include', ns);
    assert(hasAnnotation(ta, tas), 'WriteData: Could not find annotation');
    
    % Plate - Annotation link
    linkAnnotation(session, ta, 'plate', plateId);
    fprintf(1, 'Linked timestamp annotation to plate %g\n', plateId);
    fprintf(1, 'Retrieving timestamp annotations attached to plate %g with namespace %s\n',...
        plateId, ns);
    tas = getPlateTimestampAnnotations(session, plateId, 'include', ns);
    assert(hasAnnotation(ta, tas), 'WriteData: Could not find annotation');
    
    % Screen - Annotation link
    linkAnnotation(session, ta, 'screen', screenId);
    fprintf(1, 'Linked timestamp annotation to screen %g\n', screenId);
    fprintf(1, 'Retrieving timestamp annotations attached to screen %g with namespace %s\n',...
        screenId, ns);
    tas = getScreenTimestampAnnotations(session, screenId, 'include', ns);
    assert(hasAnnotation(ta, tas), 'WriteData: Could not find annotation');
    
    %% XML Annotation
    disp('XML annotation');
    xmlAnnotation = writeXmlAnnotation(session, 'xml value',...
        'description', 'xml description', 'namespace', ns);
    fprintf(1, 'Created XML annotation %g\n',...
        xmlAnnotation.getId().getValue());
    fprintf(1, 'Retrieving XML annotation %g\n',...
        xmlAnnotation.getId().getValue());
    annotation = getXmlAnnotations(session,...
        xmlAnnotation.getId().getValue());
    assert(~isempty(annotation), 'WriteData: Could not find annotation');
    
    % Project - Annotation link
    xa = omero.model.XmlAnnotationI(xmlAnnotation.getId().getValue(), false);
    linkAnnotation(session, xa, 'project', projectId);
    fprintf(1, 'Linked XML annotation to project %g\n', projectId);
    fprintf(1, 'Retrieving XML annotations attached to project %g with namespace %s\n',...
        projectId, ns);
    xas = getProjectXmlAnnotations(session, projectId, 'include', ns);
    assert(hasAnnotation(xa, xas), 'WriteData: Could not find annotation');
    
    % Dataset - Annotation link
    linkAnnotation(session, xa, 'dataset', datasetId);
    fprintf(1, 'Linked XML annotation to project %g\n', datasetId);
    fprintf(1, 'Retrieving XML annotations attached to dataset %g with namespace %s\n',...
        datasetId, ns);
    xas = getDatasetXmlAnnotations(session, datasetId, 'include', ns);
    assert(hasAnnotation(xa, xas), 'WriteData: Could not find annotation');
    
    % Image - Annotation link
    linkAnnotation(session, xa, 'image', imageId);
    fprintf(1, 'Linked XML annotation to image %g\n', imageId);
    fprintf(1, 'Retrieving XML annotations attached to image %g with namespace %s\n',...
        imageId, ns);
    xas = getImageXmlAnnotations(session, imageId, 'include', ns);
    assert(hasAnnotation(xa, xas), 'WriteData: Could not find annotation');
    
    % Plate - Annotation link
    linkAnnotation(session, xa, 'plate', plateId);
    fprintf(1, 'Linked XML annotation to plate %g\n', plateId);
    fprintf(1, 'Retrieving XML annotations attached to plate %g with namespace %s\n',...
        plateId, ns);
    xas = getPlateXmlAnnotations(session, plateId, 'include', ns);
    assert(hasAnnotation(xa, xas), 'WriteData: Could not find annotation');
    
    % Screen - Annotation link
    linkAnnotation(session, xa, 'screen', screenId);
    fprintf(1, 'Linked XML annotation to screen %g\n', screenId);
    fprintf(1, 'Retrieving XML annotations attached to screen %g with namespace %s\n',...
        screenId, ns);
    xas = getScreenXmlAnnotations(session, screenId, 'include', ns);
    assert(hasAnnotation(xa, xas), 'WriteData: Could not find annotation');
    
catch err
    client.closeSession();
    throw(err);
end

% Close the session
client.closeSession();
