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
fileNamespace = 'examples.training.matlab';
newFileNamespace = [fileNamespace '.extended'];
fileOutputPath = 'mydataBack.txt';

% Tag annotation constants
tagName = 'example';
tagDescription = 'tag annotation example';

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
    projectId = p.projectid;
    
    % Load image
    fprintf(1, 'Reading image: %g\n', imageId);
    image = getImages(session, imageId);
    assert(~isempty(image), 'OMERO:WriteData', 'Image Id not valid');
    
    % Create a local file
    fprintf(1, 'Creating local file with content: %s\n', fileContent);
    fid = fopen(filePath, 'w');
    fwrite(fid, fileContent);
    fclose(fid);
    
    % Create a file annotation
    fprintf(1, 'Creating file annotation with namespace %s\n', fileNamespace);
    fa = writeFileAnnotation(session, filePath,...
        'mimetype', fileMimeType, 'description', fileDescription,...
        'namespace', fileNamespace);
    fprintf(1, 'Created file annotation %g ', fa.getId().getValue());
    
    % Link the image and the file annotation
    link = linkAnnotation(session, fa, 'image', imageId);
    fprintf(1, 'and linked it to image %g\n', imageId);
    
    % Delete the local file
    delete(filePath);
    
    % Load all the file annotations with a given namespace linked to the image
    fprintf(1, 'Reading file annotations attached to image %g with namespace %s\n',...
        imageId, fileNamespace);
    fas = getImageFileAnnotations(session, imageId,...
        'include', fileNamespace);
    fprintf(1, 'Found %g file annotation(s)\n', numel(fas));
    
    % Download the content of the file annotation
    fprintf(1, 'Reading content of file annotation %g\n',...
        fa.getId().getValue());
    getFileAnnotationContent(session, fa, fileOutputPath);
    
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
    fa = updateFileAnnotation(session, fa, filePath, 'description',...
        newFileDescription, 'namespace', newFileNamespace);
    delete(filePath);
    
    % Read the content of the updated file annotation
    fprintf(1, 'Reading content of updated file annotation %g\n',...
        fa.getId().getValue());
    getFileAnnotationContent(session, fa, fileOutputPath);
    fid = fopen(fileOutputPath, 'r');
    readContent = fread(fid);
    fclose(fid);
    fprintf(1, 'Updated file content: %s\n', readContent);
    delete(fileOutputPath);
    
    % Create a tag i.e. tag annotation and link it to an existing project.
    disp('Creating tag annotation');
    ta = writeTagAnnotation(session, tagName, 'description', tagDescription);
    fprintf(1, 'Created tag annotation %g ', ta.getId().getValue());
    
    % Link the image and the file annotation
    link = linkAnnotation(session, ta, 'project', projectId);
    fprintf(1, 'and linked it to project %g\n', projectId);
    
    % Load all the tag annotations linked to the project
    fprintf(1, 'Reading tag annotations attached to project %g\n', projectId);
    tas = getProjectTagAnnotations(session, projectId);
    fprintf(1, 'Found %g tag annotation(s)\n', numel(tas));
    
catch err
    client.closeSession();
    throw(err);
end

% Close the session
client.closeSession();
