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

% Raw Data access

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
    
    % First retrieve the image.
    image = getImages(session, imageId);
    assert(~isempty(image), 'OMERO:RawDataAccess', 'Image Id not valid');
    
    % you should only have one pixels set per image.
    fprintf(1, 'Reading image %g\n', image.getId.getValue);
    pixels = image.getPrimaryPixels();
    sizeX = pixels.getSizeX().getValue(); % The number of pixels along the X-axis.
    sizeY = pixels.getSizeY().getValue(); % The number of pixels along the Y-axis.
    sizeZ = pixels.getSizeZ().getValue(); % The number of z-sections.
    sizeC = pixels.getSizeC().getValue(); % The number of channels.
    sizeT = pixels.getSizeT().getValue(); % The number of timepoints.
    fprintf(1, 'Size X: %g\n', sizeX);
    fprintf(1, 'Size Y: %g\n', sizeY);
    fprintf(1, 'Size Z: %g\n', sizeZ);
    fprintf(1, 'Size C: %g\n', sizeC);
    fprintf(1, 'Size T: %g\n', sizeT);
    
    %% Planes
    % The following loop initializes a raw pixels store, reads the pixels
    % data and closes the store for each method call
    disp('Reading planes with raw pixels store re-initialization');
    tic
    for z = 0:sizeZ-1,
        for t = 0:sizeT-1,
            for c = 0:sizeC-1,
                fprintf(1, '  Plane Z: %g, C: %g, T: %g\n', z, c, t);
                plane = getPlane(session, image, z, c, t);
            end
        end
    end
    toc
    
    % The following loop initializes a raw pixels store which is re-used in
    % each method call. The store must be closed manually at the end of the
    % loop.
    disp('Reading planes with raw pixels store recycling');
    tic
    [store, pixels] = getRawPixelsStore(session, image);
    for z = 0:sizeZ-1,
        for t = 0:sizeT-1,
            for c = 0:sizeC-1,
                fprintf(1, '  Plane Z: %g, C: %g, T: %g\n', z, c, t);
                plane = getPlane(pixels, store, z, c, t);
            end
        end
    end
    store.close();
    toc
    
    %% Tiles
    % The following loop initializes a raw pixels store, reads the pixels
    % data and closes the store for each method call
    disp('Reading tiles with raw pixels store re-initialization');
    x = 0;
    y = 0;
    width = sizeX/2;
    height = sizeY/2;
    tic
    for z = 0:sizeZ-1,
        for t = 0:sizeT-1,
            for c = 0:sizeC-1,
                fprintf(1, '  Tile Z: %g, C: %g, T: %g\n', z, c, t);
                tile = getTile(session, image, z, c, t, x, y, width, height);
            end
        end
    end
    toc
    
    % The following loop initializes a raw pixels store which is re-used in
    % each method call. The store must be closed manually at the end of the
    % loop.
    disp('Reading tiles with raw pixels store recycling');
    tic
    [store, pixels] = getRawPixelsStore(session, image);
    for z = 0:sizeZ-1,
        for t = 0:sizeT-1,
            for c = 0:sizeC-1,
                fprintf(1, '  Tile Z: %g, C: %g, T: %g\n', z, c, t);
                plane = getTile(pixels, store, z, c, t, x, y, width, height);
            end
        end
    end
    store.close();
    toc

    %% Stacks
    % The following loop initializes a raw pixels store, reads the pixels
    % data and closes the store for each method call
    disp('Reading stacks with raw pixels store re-initialization');
    tic
    for t = 0:sizeT-1,
        for c = 0:sizeC-1,
            fprintf(1, '  Stack C: %g, T: %g\n', c, t);
            stack = getStack(session, image, c, t);
        end
    end
    toc
    
    % The following loop initializes a raw pixels store which is re-used in
    % each method call. The store must be closed manually at the end of the
    % loop.
    disp('Reading stacks with raw pixels store recycling');
    tic
    [store, pixels] = getRawPixelsStore(session, image);
    for t = 0:sizeT-1,
        for c = 0:sizeC-1,
            fprintf(1, '  Stack C: %g, T: %g\n', c, t);
            stack = getStack(pixels, store, c, t);
        end
    end
    store.close();
    toc
    
    %% Hypercube
    disp('Reading hypercube');

    %Create the store to load the stack
    [store, pixels] = getRawPixelsStore(session, image);
    
    % offset values in each dimension XYZCT
    offset = toJavaList(zeros(1,5), 'java.lang.Integer');
    
    % offset values in each dimension XYZCT
    size = toJavaList([sizeX/2 sizeY/2 sizeZ sizeC sizeT],...
        'java.lang.Integer');
    
    % indicate the step in each direction, step = 1, will return values at index 0, 1, 2.
    % step = 2, values at index 0, 2, 4 etc.
    step = toJavaList(ones(1,5), 'java.lang.Integer');
    
    % Retrieve the data
    store.getHypercube(offset, size, step);
    % close the store
    store.close();
    
catch err
    client.closeSession();
    throw(err);
end

% Close the session
client.closeSession();
