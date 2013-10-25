% Copyright (C) 2011-2013 University of Dundee & Open Microscopy Environment.
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
    % Create a connection
    [client, session] = loadOmero();
    fprintf(1, 'Created connection to %s\n', char(client.getProperty('omero.host')));
    fprintf(1, 'Created session for user %s using group %s\n',...
        char(session.getAdminService().getEventContext().userName),...
        char(session.getAdminService().getEventContext().groupName));
    
    % Information to edit
    imageId = str2double(client.getProperty('image.id'));
    projectId = str2double(client.getProperty('project.id'));
    
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
