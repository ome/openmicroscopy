% Copyright (C) 2011-2015 University of Dundee & Open Microscopy Environment.
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
fileContent = 'file annotation current session group';
fileContent2 = 'file annotation different group';
newFileContent = [fileContent ' modified'];
newFileContent2 = [fileContent2 ' modified'];
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
    group2 = p.group2;
    groupId = session.getAdminService().lookupGroup('training_group-2').getId().getValue();

    print_object = @(x) fprintf(1, '  %s (id: %d, owner: %d, group: %d)\n',...
        char(x.getName().getValue()), x.getId().getValue(),...
        x.getDetails().getOwner().getId().getValue(),...
        x.getDetails().getGroup().getId().getValue());


    %% P/D/I
    % Create a project/dataset/image
    disp('Creating projects');
    project1 = createProject(session, 'project-1');
    project2 = createProject(session, 'project-1', 'group', groupId);
    print_object(project1);
    print_object(project2);
    disp('Creating datasets linked to projects');
    projectId1 = project1.getId().getValue();
    projectId2 = project2.getId().getValue();
    dataset1 = createDataset(session, 'dataset-1', project1);
    dataset2 = createDataset(session, 'dataset-2', projectId1);
    dataset3 = createDataset(session, 'dataset-1', project2);
    dataset4 = createDataset(session, 'dataset-1', projectId2);
    print_object(dataset1);
    print_object(dataset2);
    print_object(dataset3);
    print_object(dataset4);
    disp('Creating orphaned datasets');
    dataset5 = createDataset(session, 'orphaned dataset-1');
    dataset6 = createDataset(session, 'orphaned dataset-2', 'group', groupId);
    print_object(dataset5)
    print_object(dataset6)
    datasetId1 = dataset1.getId().getValue();
    datasetId2 = dataset2.getId().getValue();

    disp('Creating screens');
    screen1 = createScreen(session, 'screen-1');
    screen2 = createScreen(session, 'screen-1', 'group', groupId);
    print_object(screen1);
    print_object(screen2);
    disp('Creating plates linked to screens');
    screenId1 = screen1.getId().getValue();
    screenId2 = screen2.getId().getValue();
    plate1 = createPlate(session, 'plate-1', screen1);
    plate2 = createPlate(session, 'plate-2', screenId1);
    plate3 = createPlate(session, 'plate-1', screen2);
    plate4 = createPlate(session, 'plate-2', screenId2);
    print_object(plate1);
    print_object(plate2);
    print_object(plate3);
    print_object(plate4);
    disp('Creating orphaned plates');
    plate5 = createDataset(session, 'orphaned plate-1');
    plate6 = createDataset(session, 'orphaned plate-2', 'group', groupId);
    print_object(plate5)
    print_object(plate6)
    plateId1 = plate1.getId().getValue();

    image1 = createObject(session, 'image', 'image-1');
    image2 = createObject(session, 'image', 'image-1', 'group', groupId);
    imageId1 = image1.getId().getValue();

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
    fprintf(1, 'Created file annotation %g (group: %g)\n',...
        fileAnnotation.getId().getValue(),...
        fileAnnotation.getDetails().getGroup().getId().getValue());
    
    fid = fopen(filePath, 'w');
    fwrite(fid, fileContent2);
    fclose(fid);
    fileAnnotation2 = writeFileAnnotation(session, filePath,...
        'mimetype', fileMimeType, 'description', fileDescription,...
        'namespace', ns, 'group', groupId);
    fprintf(1, 'Created file annotation %g (group: %g)\n',...
        fileAnnotation2.getId().getValue(),...
        fileAnnotation2.getDetails().getGroup().getId().getValue());
    % Delete the local file
    delete(filePath);
    
    % Download and read the content of the file annotation
    getFileAnnotationContent(session, fileAnnotation, fileOutputPath);
    fid = fopen(fileOutputPath, 'r');
    readContent = fread(fid);
    fclose(fid);
    fprintf(1, 'Reading content of file annotation %g: %s\n',...
        fileAnnotation.getId().getValue(), readContent);
    delete(fileOutputPath);
    
    % Download and read the content of the file annotation
    getFileAnnotationContent(session, fileAnnotation2, fileOutputPath);
    fid = fopen(fileOutputPath, 'r');
    readContent = fread(fid);
    fclose(fid);
    fprintf(1, 'Reading content of file annotation %g: %s\n',...
        fileAnnotation2.getId().getValue(), readContent);
    delete(fileOutputPath);
    
    % Update the file annotation on the server
    disp('Updating file annotations with new content');
    fid = fopen(filePath, 'w');
    fwrite(fid, newFileContent);
    fclose(fid);
    fileAnnotation = updateFileAnnotation(session, fileAnnotation, filePath,...
        'description', newFileDescription);
    fid = fopen(filePath, 'w');
    fwrite(fid, newFileContent2);
    fclose(fid);
    fileAnnotation2 = updateFileAnnotation(session, fileAnnotation2, filePath,...
        'description', newFileDescription);
    delete(filePath);
    
    % Read the content of the updated file annotation
    getFileAnnotationContent(session, fileAnnotation, fileOutputPath);
    fid = fopen(fileOutputPath, 'r');
    readContent = fread(fid);
    fclose(fid);
    fprintf(1, 'Reading content of updated file annotation %g: %s\n',...
        fileAnnotation.getId().getValue(), readContent);
    delete(fileOutputPath);

    getFileAnnotationContent(session, fileAnnotation2, fileOutputPath);
    fid = fopen(fileOutputPath, 'r');
    readContent = fread(fid);
    fclose(fid);
    fprintf(1, 'Reading content of updated file annotation %g: %s\n',...
        fileAnnotation2.getId().getValue(), readContent);
    delete(fileOutputPath);

    % Project - Annotation link
    fa = omero.model.FileAnnotationI(fileAnnotation.getId().getValue(), false);
    linkAnnotation(session, fa, 'project', projectId1);
    fprintf(1, 'Linked file annotation to project %g\n', projectId1);
    fprintf(1, 'Retrieving file annotations attached to project %g with namespace %s\n',...
        projectId1, ns);
    fas = getProjectFileAnnotations(session, projectId1, 'include', ns);
    assert(hasAnnotation(fa, fas), 'WriteData: Could not find annotation');

    % Dataset - Annotation link
    linkAnnotation(session, fa, 'dataset', datasetId1);
    fprintf(1, 'Linked file annotation to dataset %g\n', datasetId1);
    fprintf(1, 'Retrieving file annotations attached to dataset %g with namespace %s\n',...
        datasetId1, ns);
    fas = getDatasetFileAnnotations(session, datasetId1, 'include', ns);
    assert(hasAnnotation(fa, fas), 'WriteData: Could not find annotation');

    % Image - Annotation link
    linkAnnotation(session, fa, 'image', imageId1);
    fprintf(1, 'Linked file annotation to image %g\n', imageId1);
    fprintf(1, 'Retrieving file annotations attached to image %g with namespace %s\n',...
        imageId1, ns);
    fas = getImageFileAnnotations(session, imageId1, 'include', ns);
    assert(hasAnnotation(fa, fas), 'WriteData: Could not find annotation');

    % Plate - Annotation link
    linkAnnotation(session, fa, 'plate', plateId1);
    fprintf(1, 'Linked file annotation to plate %g\n', plateId1);
    fprintf(1, 'Retrieving file annotations attached to plate %g with namespace %s\n',...
        plateId1, ns);
    fas = getPlateFileAnnotations(session, plateId1, 'include', ns);
    assert(hasAnnotation(fa, fas), 'WriteData: Could not find annotation');

    % Screen - Annotation link
    linkAnnotation(session, fa, 'screen', screenId1);
    fprintf(1, 'Linked file annotation to screen %g\n', screenId1);
    fprintf(1, 'Retrieving file annotations attached to screen %g with namespace %s\n',...
        screenId1, ns);
    fas = getScreenFileAnnotations(session, screenId1, 'include', ns);
    assert(hasAnnotation(fa, fas), 'WriteData: Could not find annotation');

    %% Comment Annotation
    disp('Comment annotation');

    disp('Creating comment annotations');
    commentAnnotation1 = writeCommentAnnotation(session, 'comment',...
        'description', 'comment description', 'namespace', ns);
    commentAnnotation2 = writeCommentAnnotation(session, 'comment',...
        'description', 'comment description', 'namespace', ns, 'group', groupId);
    print_long = @(x) fprintf(1, '  %s (id: %d, owner: %d, group: %d)\n',...
        char(x.getTextValue().getValue()), x.getId().getValue(),...
        x.getDetails().getOwner().getId().getValue(),...
        x.getDetails().getGroup().getId().getValue());
    print_long(commentAnnotation1);
    print_long(commentAnnotation2);

    disp('Retrieving comment annotations');
    annotation = getCommentAnnotations(session,...
        commentAnnotation1.getId().getValue());
    assert(~isempty(annotation), 'WriteData: Could not find annotation');

    % Project - Annotation link
    ca = omero.model.CommentAnnotationI(commentAnnotation1.getId().getValue(), false);
    linkAnnotation(session, ca, 'project', projectId1);
    fprintf(1, 'Linked comment annotation to project %g\n', projectId1);
    fprintf(1, 'Retrieving comment annotations attached to project %g with namespace %s\n',...
        projectId1, ns);
    cas = getProjectCommentAnnotations(session, projectId1, 'include', ns);
    assert(hasAnnotation(ca, cas), 'WriteData: Could not find annotation');
    
    % Dataset - Annotation link
    linkAnnotation(session, ca, 'dataset', datasetId1);
    fprintf(1, 'Linked comment annotation to dataset %g\n', datasetId1);
    fprintf(1, 'Retrieving comment annotations attached to dataset %g with namespace %s\n',...
        datasetId1, ns);
    cas = getDatasetCommentAnnotations(session, datasetId1, 'include', ns);
    assert(hasAnnotation(ca, cas), 'WriteData: Could not find annotation');
    
    % Image - Annotation link
    linkAnnotation(session, ca, 'image', imageId1);
    fprintf(1, 'Linked comment annotation to image %g\n', imageId1);
    fprintf(1, 'Retrieving comment annotations attached to image %g with namespace %s\n',...
        imageId1, ns);
    cas = getImageCommentAnnotations(session, imageId1, 'include', ns);
    assert(hasAnnotation(ca, cas), 'WriteData: Could not find annotation');
    
    % Plate - Annotation link
    linkAnnotation(session, ca, 'plate', plateId1);
    fprintf(1, 'Linked comment annotation to plate %g\n', plateId1);
    fprintf(1, 'Retrieving comment annotations attached to plate %g with namespace %s\n',...
        plateId1, ns);
    cas = getPlateCommentAnnotations(session, plateId1, 'include', ns);
    assert(hasAnnotation(ca, cas), 'WriteData: Could not find annotation');
    
    % Screen - Annotation link
    linkAnnotation(session, ca, 'screen', screenId1);
    fprintf(1, 'Linked comment annotation to screen %g\n', screenId1);
    fprintf(1, 'Retrieving comment annotations attached to screen %g with namespace %s\n',...
        screenId1, ns);
    cas = getScreenCommentAnnotations(session, screenId1, 'include', ns);
    assert(hasAnnotation(ca, cas), 'WriteData: Could not find annotation');

    %% Double Annotation
    disp('Double annotation');

    disp('Creating double annotations');
    doubleAnnotation1 = writeDoubleAnnotation(session, .5,...
        'description', 'double description', 'namespace', ns);
    doubleAnnotation2 = writeDoubleAnnotation(session, .5,...
        'description', 'double description', 'namespace', ns, 'group', groupId);
    print_long = @(x) fprintf(1, '  %g (id: %d, owner: %d, group: %d)\n',...
        x.getDoubleValue().getValue(), x.getId().getValue(),...
        x.getDetails().getOwner().getId().getValue(),...
        x.getDetails().getGroup().getId().getValue());
    print_long(doubleAnnotation1);
    print_long(doubleAnnotation2);

    disp('Retrieving double annotations');
    annotation = getDoubleAnnotations(session,...
        doubleAnnotation1.getId().getValue());
    assert(~isempty(annotation), 'WriteData: Could not find annotation');

    % Project - Annotation link
    da = omero.model.DoubleAnnotationI(doubleAnnotation1.getId().getValue(), false);
    linkAnnotation(session, da, 'project', projectId1);
    fprintf(1, 'Linked double annotation to project %g\n', projectId1);
    fprintf(1, 'Retrieving double annotations attached to project %g with namespace %s\n',...
        projectId1, ns);
    das = getProjectDoubleAnnotations(session, projectId1, 'include', ns);
    assert(hasAnnotation(da, das), 'WriteData: Could not find annotation');
    
    % Dataset - Annotation link
    linkAnnotation(session, da, 'dataset', datasetId1);
    fprintf(1, 'Linked double annotation to dataset %g\n', datasetId1);
    fprintf(1, 'Retrieving double annotations attached to dataset %g with namespace %s\n',...
        datasetId1, ns);
    das = getDatasetDoubleAnnotations(session, datasetId1, 'include', ns);
    assert(hasAnnotation(da, das), 'WriteData: Could not find annotation');
    
    % Image - Annotation link
    linkAnnotation(session, da, 'image', imageId1);
    fprintf(1, 'Linked double annotation to image %g\n', imageId1);
    fprintf(1, 'Retrieving double annotations attached to image %g with namespace %s\n',...
        imageId1, ns);
    das = getImageDoubleAnnotations(session, imageId1, 'include', ns);
    assert(hasAnnotation(da, das), 'WriteData: Could not find annotation');
    
    % Plate - Annotation link
    linkAnnotation(session, da, 'plate', plateId1);
    fprintf(1, 'Linked double annotation to plate %g\n', plateId1);
    fprintf(1, 'Retrieving double annotations attached to plate %g with namespace %s\n',...
        plateId1, ns);
    das = getPlateDoubleAnnotations(session, plateId1, 'include', ns);
    assert(hasAnnotation(da, das), 'WriteData: Could not find annotation');
    
    % Screen - Annotation link
    linkAnnotation(session, da, 'screen', screenId1);
    fprintf(1, 'Linked double annotation to screen %g\n', screenId1);
    fprintf(1, 'Retrieving double annotations attached to screen %g with namespace %s\n',...
        screenId1, ns);
    das = getScreenDoubleAnnotations(session, screenId1, 'include', ns);
    assert(hasAnnotation(da, das), 'WriteData: Could not find annotation');

    %% Long Annotation
    disp('Long annotation');

    disp('Creating long annotations');
    longAnnotation1 = writeLongAnnotation(session, 1,...
        'description', 'long description', 'namespace', ns);
    longAnnotation2 = writeLongAnnotation(session, 1,...
        'description', 'long description', 'namespace', ns, 'group', groupId);
    print_long = @(x) fprintf(1, '  %g (id: %d, owner: %d, group: %d)\n',...
        char(x.getLongValue().getValue()), x.getId().getValue(),...
        x.getDetails().getOwner().getId().getValue(),...
        x.getDetails().getGroup().getId().getValue());
    print_long(longAnnotation1);
    print_long(longAnnotation2);

    disp('Retrieving long annotations');
    annotation = getLongAnnotations(session,...
        longAnnotation1.getId().getValue());
    assert(~isempty(annotation), 'WriteData: Could not find annotation');

    % Project - Annotation link
    la = omero.model.LongAnnotationI(longAnnotation1.getId().getValue(), false);
    linkAnnotation(session, la, 'project', projectId1);
    fprintf(1, 'Linked long annotation to project %g\n', projectId1);
    fprintf(1, 'Retrieving long annotations attached to project %g with namespace %s\n',...
        projectId1, ns);
    las = getProjectLongAnnotations(session, projectId1, 'include', ns);
    assert(hasAnnotation(la, las), 'WriteData: Could not find annotation');

    % Dataset - Annotation link
    linkAnnotation(session, la, 'dataset', datasetId1);
    fprintf(1, 'Linked long annotation to dataset %g\n', datasetId1);
    fprintf(1, 'Retrieving long annotations attached to dataset %g with namespace %s\n',...
        datasetId1, ns);
    las = getDatasetLongAnnotations(session, datasetId1, 'include', ns);
    assert(hasAnnotation(la, las), 'WriteData: Could not find annotation');

    % Image - Annotation link
    linkAnnotation(session, la, 'image', imageId1);
    fprintf(1, 'Linked long annotation to image %g\n', imageId1);
    fprintf(1, 'Retrieving long annotations attached to image %g with namespace %s\n',...
        imageId1, ns);
    las = getImageLongAnnotations(session, imageId1, 'include', ns);
    assert(hasAnnotation(la, las), 'WriteData: Could not find annotation');

    % Plate - Annotation link
    linkAnnotation(session, la, 'plate', plateId1);
    fprintf(1, 'Linked long annotation to plate %g\n', plateId1);
    fprintf(1, 'Retrieving long annotations attached to plate %g with namespace %s\n',...
        plateId1, ns);
    las = getPlateLongAnnotations(session, plateId1, 'include', ns);
    assert(hasAnnotation(la, las), 'WriteData: Could not find annotation');

    % Screen - Annotation link
    linkAnnotation(session, la, 'screen', screenId1);
    fprintf(1, 'Linked long annotation to screen %g\n', screenId1);
    fprintf(1, 'Retrieving long annotations attached to screen %g with namespace %s\n',...
        screenId1, ns);
    las = getScreenLongAnnotations(session, screenId1, 'include', ns);
    assert(hasAnnotation(la, las), 'WriteData: Could not find annotation');

    %% Map Annotation
    disp('Map annotation');

    disp('Creating map annotations');
    mapAnnotation1 = writeMapAnnotation(session, 'key', 'value',...
        'description', 'map description', 'namespace', ns);
    mapAnnotation2 = writeMapAnnotation(session, 'key', 'value',...
        'description', 'map description', 'namespace', ns, 'group', groupId);
    print_map = @(x) fprintf(1, '  %s (id: %d, owner: %d, group: %d)\n',...
        char(x.getMapValueAsMap), x.getId().getValue(),...
        x.getDetails().getOwner().getId().getValue(),...
        x.getDetails().getGroup().getId().getValue());
    print_map(mapAnnotation1);
    print_map(mapAnnotation2);

    disp('Retrieving map annotations');
    annotation = getAnnotations(session,...
        mapAnnotation1.getId().getValue(), 'map');
    assert(~isempty(annotation), 'WriteData: Could not find annotation');

    % Project - Annotation link
    ma = omero.model.MapAnnotationI(mapAnnotation1.getId().getValue(), false);
    linkAnnotation(session, ma, 'project', projectId1);
    fprintf(1, 'Linked map annotation to project %g\n', projectId1);
    fprintf(1, 'Retrieving map annotations attached to project %g with namespace %s\n',...
        projectId1, ns);
    mas = getObjectAnnotations(session, 'map', 'project', projectId1, 'include', ns);
    assert(hasAnnotation(ma, mas), 'WriteData: Could not find annotation');

    % Dataset - Annotation link
    linkAnnotation(session, ma, 'dataset', datasetId1);
    fprintf(1, 'Linked map annotation to dataset %g\n', datasetId1);
    fprintf(1, 'Retrieving map annotations attached to dataset %g with namespace %s\n',...
        datasetId1, ns);
    mas = getObjectAnnotations(session, 'map', 'dataset', datasetId1, 'include', ns);
    assert(hasAnnotation(ma, mas), 'WriteData: Could not find annotation');

    % Image - Annotation link
    linkAnnotation(session, ma, 'image', imageId1);
    fprintf(1, 'Linked map annotation to image %g\n', imageId1);
    fprintf(1, 'Retrieving map annotations attached to image %g with namespace %s\n',...
        imageId1, ns);
    mas = getObjectAnnotations(session, 'map', 'image', imageId1, 'include', ns);
    assert(hasAnnotation(ma, mas), 'WriteData: Could not find annotation');

    % Plate - Annotation link
    linkAnnotation(session, ma, 'plate', plateId1);
    fprintf(1, 'Linked map annotation to plate %g\n', plateId1);
    fprintf(1, 'Retrieving map annotations attached to plate %g with namespace %s\n',...
        plateId1, ns);
    mas = getObjectAnnotations(session, 'map', 'plate', plateId1, 'include', ns);
    assert(hasAnnotation(ma, mas), 'WriteData: Could not find annotation');

    % Screen - Annotation link
    linkAnnotation(session, ma, 'screen', screenId1);
    fprintf(1, 'Linked map annotation to screen %g\n', screenId1);
    fprintf(1, 'Retrieving map annotations attached to screen %g with namespace %s\n',...
        screenId1, ns);
    mas = getObjectAnnotations(session, 'map', 'screen', screenId1, 'include', ns);
    assert(hasAnnotation(ma, mas), 'WriteData: Could not find annotation');

    %% Tag Annotation
    disp('Tag annotation');

    disp('Creating tag annotations');
    tagAnnotation1 = writeTagAnnotation(session, 'tag value',...
        'description', 'tag description', 'namespace', ns);
    tagAnnotation2 = writeTagAnnotation(session, 'tag value',...
        'description', 'tag description', 'namespace', ns, 'group', groupId);
    print_tag = @(x) fprintf(1, '  %s (id: %d, owner: %d, group: %d)\n',...
        char(x.getTextValue().getValue()), x.getId().getValue(),...
        x.getDetails().getOwner().getId().getValue(),...
        x.getDetails().getGroup().getId().getValue());
    print_tag(tagAnnotation1);
    print_tag(tagAnnotation2);

    disp('Retrieving tag annotations');
    annotation = getTagAnnotations(session,...
        tagAnnotation1.getId().getValue());
    assert(~isempty(annotation), 'WriteData: Could not find annotation');

    % Project - Annotation link
    ta = omero.model.TagAnnotationI(tagAnnotation1.getId().getValue(), false);
    linkAnnotation(session, ta, 'project', projectId1);
    fprintf(1, 'Linked tag annotation to project %g\n', projectId1);
    fprintf(1, 'Retrieving tag annotations attached to project %g with namespace %s\n',...
        projectId1, ns);
    tas = getProjectTagAnnotations(session, projectId1, 'include', ns);
    assert(hasAnnotation(ta, tas), 'WriteData: Could not find annotation');
    
    % Dataset - Annotation link
    linkAnnotation(session, ta, 'dataset', datasetId1);
    fprintf(1, 'Linked tag annotation to dataset %g\n', datasetId1);
    fprintf(1, 'Retrieving tag annotations attached to dataset %g with namespace %s\n',...
        datasetId1, ns);
    tas = getDatasetTagAnnotations(session, datasetId1, 'include', ns);
    assert(hasAnnotation(ta, tas), 'WriteData: Could not find annotation');
    
    % Image - Annotation link
    linkAnnotation(session, ta, 'image', imageId1);
    fprintf(1, 'Linked tag annotation to image %g\n', imageId1);
    fprintf(1, 'Retrieving tag annotations attached to image %g with namespace %s\n',...
        imageId1, ns);
    tas = getImageTagAnnotations(session, imageId1, 'include', ns);
    assert(hasAnnotation(ta, tas), 'WriteData: Could not find annotation');
    
    % Plate - Annotation link
    linkAnnotation(session, ta, 'plate', plateId1);
    fprintf(1, 'Linked tag annotation to plate %g\n', plateId1);
    fprintf(1, 'Retrieving tag annotations attached to plate %g with namespace %s\n',...
        plateId1, ns);
    tas = getPlateTagAnnotations(session, plateId1, 'include', ns);
    assert(hasAnnotation(ta, tas), 'WriteData: Could not find annotation');
    
    % Screen - Annotation link
    linkAnnotation(session, ta, 'screen', screenId1);
    fprintf(1, 'Linked tag annotation to screen %g\n', screenId1);
    fprintf(1, 'Retrieving tag annotations attached to screen %g with namespace %s\n',...
        screenId1, ns);
    tas = getScreenTagAnnotations(session, screenId1, 'include', ns);
    assert(hasAnnotation(ta, tas), 'WriteData: Could not find annotation');

    %% Timestamp Annotation
    disp('Timestamp annotation');

    disp('Creating timestamp annotations');
    timestampAnnotation1 = writeTimestampAnnotation(session, now,...
        'description', 'timestamp description', 'namespace', ns);
    timestampAnnotation2 = writeTimestampAnnotation(session, now,...
        'description', 'timestamp description', 'namespace', ns, 'group', groupId);
    print_ts = @(x) fprintf(1, '  %g (id: %d, owner: %d, group: %d)\n',...
        x.getTimeValue().getValue(), x.getId().getValue(),...
        x.getDetails().getOwner().getId().getValue(),...
        x.getDetails().getGroup().getId().getValue());
    print_ts(timestampAnnotation1);
    print_ts(timestampAnnotation2);

    disp('Retrieving timestamp annotations');
    annotation = getTimestampAnnotations(session,...
        timestampAnnotation1.getId().getValue());
    assert(~isempty(annotation), 'WriteData: Could not find annotation');

    % Project - Annotation link
    ta = omero.model.TimestampAnnotationI(timestampAnnotation1.getId().getValue(), false);
    linkAnnotation(session, ta, 'project', projectId1);
    fprintf(1, 'Linked timestamp annotation to project %g\n', projectId1);
    fprintf(1, 'Retrieving timestamp annotations attached to project %g with namespace %s\n',...
        projectId1, ns);
    tas = getProjectTimestampAnnotations(session, projectId1, 'include', ns);
    assert(hasAnnotation(ta, tas), 'WriteData: Could not find annotation');
    
    % Dataset - Annotation link
    linkAnnotation(session, ta, 'dataset', datasetId1);
    fprintf(1, 'Linked timestamp annotation to dataset %g\n', datasetId1);
    fprintf(1, 'Retrieving timestamp annotations attached to dataset %g with namespace %s\n',...
        datasetId1, ns);
    tas = getDatasetTimestampAnnotations(session, datasetId1, 'include', ns);
    assert(hasAnnotation(ta, tas), 'WriteData: Could not find annotation');
    
    % Image - Annotation link
    linkAnnotation(session, ta, 'image', imageId1);
    fprintf(1, 'Linked timestamp annotation to image %g\n', imageId1);
    fprintf(1, 'Retrieving timestamp annotations attached to image %g with namespace %s\n',...
        imageId1, ns);
    tas = getImageTimestampAnnotations(session, imageId1, 'include', ns);
    assert(hasAnnotation(ta, tas), 'WriteData: Could not find annotation');
    
    % Plate - Annotation link
    linkAnnotation(session, ta, 'plate', plateId1);
    fprintf(1, 'Linked timestamp annotation to plate %g\n', plateId1);
    fprintf(1, 'Retrieving timestamp annotations attached to plate %g with namespace %s\n',...
        plateId1, ns);
    tas = getPlateTimestampAnnotations(session, plateId1, 'include', ns);
    assert(hasAnnotation(ta, tas), 'WriteData: Could not find annotation');
    
    % Screen - Annotation link
    linkAnnotation(session, ta, 'screen', screenId1);
    fprintf(1, 'Linked timestamp annotation to screen %g\n', screenId1);
    fprintf(1, 'Retrieving timestamp annotations attached to screen %g with namespace %s\n',...
        screenId1, ns);
    tas = getScreenTimestampAnnotations(session, screenId1, 'include', ns);
    assert(hasAnnotation(ta, tas), 'WriteData: Could not find annotation');

    %% XML Annotation
    disp('XML annotation');

    disp('Creating XML annotations');
    xmlAnnotation1 = writeXmlAnnotation(session, 'xml value',...
        'description', 'xml description', 'namespace', ns);
    xmlAnnotation2 = writeXmlAnnotation(session, 'xml value',...
        'description', 'xml description', 'namespace', ns, 'group', groupId);
    print_timestamp = @(x) fprintf(1, '  %s (id: %d, owner: %d, group: %d)\n',...
        char(x.getTextValue().getValue()), x.getId().getValue(),...
        x.getDetails().getOwner().getId().getValue(),...
        x.getDetails().getGroup().getId().getValue());
    print_timestamp(xmlAnnotation1);
    print_timestamp(xmlAnnotation2);

    disp('Retrieving XML annotations');
    annotation = getXmlAnnotations(session,...
        xmlAnnotation1.getId().getValue());
    assert(~isempty(annotation), 'WriteData: Could not find annotation');

    % Project - Annotation link
    xa = omero.model.XmlAnnotationI(xmlAnnotation1.getId().getValue(), false);
    linkAnnotation(session, xa, 'project', projectId1);
    fprintf(1, 'Linked XML annotation to project %g\n', projectId1);
    fprintf(1, 'Retrieving XML annotations attached to project %g with namespace %s\n',...
        projectId1, ns);
    xas = getProjectXmlAnnotations(session, projectId1, 'include', ns);
    assert(hasAnnotation(xa, xas), 'WriteData: Could not find annotation');
    
    % Dataset - Annotation link
    linkAnnotation(session, xa, 'dataset', datasetId1);
    fprintf(1, 'Linked XML annotation to dataset %g\n', datasetId1);
    fprintf(1, 'Retrieving XML annotations attached to dataset %g with namespace %s\n',...
        datasetId1, ns);
    xas = getDatasetXmlAnnotations(session, datasetId1, 'include', ns);
    assert(hasAnnotation(xa, xas), 'WriteData: Could not find annotation');
    
    % Image - Annotation link
    linkAnnotation(session, xa, 'image', imageId1);
    fprintf(1, 'Linked XML annotation to image %g\n', imageId1);
    fprintf(1, 'Retrieving XML annotations attached to image %g with namespace %s\n',...
        imageId1, ns);
    xas = getImageXmlAnnotations(session, imageId1, 'include', ns);
    assert(hasAnnotation(xa, xas), 'WriteData: Could not find annotation');
    
    % Plate - Annotation link
    linkAnnotation(session, xa, 'plate', plateId1);
    fprintf(1, 'Linked XML annotation to plate %g\n', plateId1);
    fprintf(1, 'Retrieving XML annotations attached to plate %g with namespace %s\n',...
        plateId1, ns);
    xas = getPlateXmlAnnotations(session, plateId1, 'include', ns);
    assert(hasAnnotation(xa, xas), 'WriteData: Could not find annotation');
    
    % Screen - Annotation link
    linkAnnotation(session, xa, 'screen', screenId1);
    fprintf(1, 'Linked XML annotation to screen %g\n', screenId1);
    fprintf(1, 'Retrieving XML annotations attached to screen %g with namespace %s\n',...
        screenId1, ns);
    xas = getScreenXmlAnnotations(session, screenId1, 'include', ns);
    assert(hasAnnotation(xa, xas), 'WriteData: Could not find annotation');
    
catch err
    client.closeSession();
    throw(err);
end

% Close the session
client.closeSession();
