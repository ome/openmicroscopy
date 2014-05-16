% Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
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
    
    % Retrieve an image if the identifier is known.
    fprintf(1, 'Reading image: %g\n', imageId);
    image = getImages(session, imageId);
    assert(~isempty(image), 'OMERO:ReadData', 'Image Id not valid');
    
    % Access information about the fileset
    fileset = image.getFileset();
    filesetId = fileset.getId().getValue();
    fprintf(1, 'Image %g associated to Fileset %g\n', imageId, filesetId);
    
    % Retrieve Fileset
    query = ['select obj from Fileset obj '...
        'left outer join fetch obj.images as image '...
        'left outer join fetch obj.usedFiles as usedFile '...
        'join fetch usedFile.originalFile '...
        'where obj.id = ' num2str(filesetId)];
    fileset = session.getQueryService().findAllByQuery(query, []);
    fileset = toMatlabList(fileset);
    
    % List all images that are associated to this fileset
    fsImages = toMatlabList(fileset.copyImages());
    fprintf(1, 'Found %g Images associated to fileset: %g\n',...
        numel(fsImages), filesetId);
    for i = 1 : numel(fsImages)
        fprintf(1, '  Image %s (id: %g)\n',...
            char(fsImages(i).getName().getValue()),...
            fsImages(i).getId().getValue());
    end
    
    % List all original imported files
    usedFiles = toMatlabList(fileset.copyUsedFiles());
    fprintf(1, 'Found %g original files imported with fileset: %g\n',...
        numel(usedFiles), filesetId);
    for i = 1 : numel(usedFiles)
        oFile =  usedFiles(i).getOriginalFile();
        fprintf(1, '  Files %s (id: %g)\n',...
            char(oFile.getName().getValue()), oFile.getId().getValue());
    end
catch err
    client.closeSession();
    throw(err);
end

% Close the session
client.closeSession();
