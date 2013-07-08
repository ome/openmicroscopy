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

% Information to edit
fileToUpload= char('mydata.txt'); % file should exist.
generatedSha1 = char('pending');
fileMimeType = char('application/octet-stream');

description = char(java.util.UUID.randomUUID());
NAME_SPACE_TO_SET = char('imperial.training.demo');

% How to create a file annotation and link to an image.

% To attach a file to an object e.g. an image, few objects need to be created:
%  1. an `OriginalFile`
%  1. a `FileAnnotation`
%  1. a link between the `Image` and the `FileAnnotation`.

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

    
    iUpdate = session.getUpdateService(); % service used to write object
    % create the original file object.
    file = java.io.File(fileToUpload);
    name = file.getName();
    absolutePath = file.getAbsolutePath();
	path = absolutePath.substring(0, absolutePath.length()-name.length());
    
    
    originalFile = omero.model.OriginalFileI;
    originalFile.setName(omero.rtypes.rstring(name));
    originalFile.setPath(omero.rtypes.rstring(path));
    originalFile.setSize(omero.rtypes.rlong(file.length()));
    originalFile.setSha1(omero.rtypes.rstring(generatedSha1));
    originalFile.setMimetype(omero.rtypes.rstring(fileMimeType));
    % now we save the originalFile object
    originalFile = iUpdate.saveAndReturnObject(originalFile);
  
    % Initialize the service to load the raw data
    rawFileStore = session.createRawFileStore();
    rawFileStore.setFileId(originalFile.getId().getValue());
    % open file and read stream
    % works for small file
    fid = fopen(fileToUpload);
    byteArray = fread(fid,[1, file.length()], 'uint8');
    rawFileStore.write(byteArray, 0, file.length());
    fclose(fid);
    
    originalFile = rawFileStore.save();
    originalFile.getSize().getValue()
    % Important to close the service
    rawFileStore.close();
    % now we have an original File in DB and raw data uploaded.
    % We now need to link the Original file to the image using the File annotation object. That's the way to do it.
    fa = omero.model.FileAnnotationI;
    fa.setFile(originalFile);
    fa.setDescription(omero.rtypes.rstring(description)); % The description set above e.g. PointsModel
    fa.setNs(omero.rtypes.rstring(NAME_SPACE_TO_SET)) % The name space you have set to identify the file annotation.

    % save the file annotation.
    fa = iUpdate.saveAndReturnObject(fa);

    % now link the image and the annotation
    link = omero.model.ImageAnnotationLinkI;
    link.setChild(fa);
    link.setParent(image);
    % save the link back to the server.
    link = iUpdate.saveAndReturnObject(link);


    % Load all the annotations with a given namespace linked to the images.


    userId = session.getAdminService().getEventContext().userId;
    nsToInclude = java.util.ArrayList;
    nsToInclude.add(NAME_SPACE_TO_SET);
    nsToExclude = java.util.ArrayList;
    options = omero.sys.ParametersI;
    options.exp(omero.rtypes.rlong(userId)); %load the annotation for a given user.
    metadataService = session.getMetadataService();
    % retrieve the annotations linked to images, for datasets use: omero.model.Dataset.class
    annotations = metadataService.loadSpecifiedAnnotations(omero.model.Image.class, nsToInclude, nsToExclude, options);
    for j = 0:annotations.size()-1,
        annotations.get(j).getId().getValue();
    end

    % Read the attachment. First load the annotation, cf. above.

    % Let's call fa the file annotation
    originalFile = fa.getFile();
    rawFileStore = session.createRawFileStore();
    rawFileStore.setFileId(originalFile.getId().getValue());
    % read data
    
    fid = fopen('mydataBack.txt', 'w');
    fwrite(fid, rawFileStore.read(0, originalFile.getSize().getValue()), 'uint8');
    fclose(fid);
    type mydataBack.txt;
    
    % close when done
    rawFileStore.close();

    %Create a dataset and link it to an existing project.
    dataset = omero.model.DatasetI;
    dataset.setName(omero.rtypes.rstring(char('name dataset')));
    dataset.setDescription(omero.rtypes.rstring(char('description dataset')));

    %link dataset and project
    link = omero.model.ProjectDatasetLinkI;
    link.setChild(dataset);
    link.setParent(omero.model.ProjectI(projectId, false));

    session.getUpdateService().saveAndReturnObject(link);

    % Create a tag i.e. tag annotation and link it to an existing project.

    tag = omero.model.TagAnnotationI;
    tag.setTextValue(omero.rtypes.rstring(char('name tag')));
    tag.setDescription(omero.rtypes.rstring(char('description tag')));

    %link tag and project
    link = omero.model.ProjectAnnotationLinkI;
    link.setChild(tag);
    link.setParent(omero.model.ProjectI(projectId, false));

    session.getUpdateService().saveAndReturnObject(link);
    
catch err
    disp(err.message);
end

%Close the session
client.closeSession();