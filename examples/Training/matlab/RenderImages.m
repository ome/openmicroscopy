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

try
    % Create a connection
    [client, session] = loadOmero();
    p = parseOmeroProperties(client);
    eventContext = session.getAdminService().getEventContext();
    fprintf(1, 'Created connection to %s\n', p.hostname);
    msg = 'Created session for user %s (id: %g) using group %s (id: %g)\n';
    fprintf(1, msg, char(eventContext.userName), eventContext.userId,...
        char(eventContext.groupName), eventContext.groupId);
  
    % Information to edit
    imageId = p.imageid;
    
    % Load image acquisition data.
    fprintf(1, 'Reading image: %g\n', imageId);
    image = getImages(session, imageId);
    assert(~isempty(image), 'OMERO:RenderImages', 'Image Id not valid');
    pixels = image.getPrimaryPixels();
    pixelsId = pixels.getId().getValue();
    
    % Rendering
    
    % Follow an example indicating, how to create a rendering engine.
    disp('Rendering the image');
    % Create rendering engine.
    re = session.createRenderingEngine();
    re.lookupPixels(pixelsId);
    % Check if default required.
    if (~re.lookupRenderingDef(pixelsId))
        re.resetDefaultSettings(true);
        re.lookupRenderingDef(pixelsId);
    end
    % start the rendering engine
    re.load();
    
    % render a plane as compressed. Possible to render it uncompressed.
    pDef = omero.romio.PlaneDef;
    pDef.z = re.getDefaultZ();
    pDef.t = re.getDefaultT();
    pDef.slice = omero.romio.XY.value;
    %Create an input stream
    sizeC = pixels.getSizeC().getValue()-1;
    if (sizeC == 0)
        re.setActive(0, 1);
    else
        for k = 0:sizeC,
            re.setActive(k, 0);
            values = re.renderCompressed(pDef);
            stream = java.io.ByteArrayInputStream(values);
            image = javax.imageio.ImageIO.read(stream);
            stream.close();
            if feature('ShowFigureWindows')
                figure(k+1);
                imshow(JavaImageToMatlab(image));
            end
            %make all the channels active.
            for i = 0:sizeC,
                re.setActive(i, 1);
            end
        end
    end
    % All channels active and save the image as a JPEG.
    values = re.renderCompressed(pDef);
    stream = java.io.ByteArrayInputStream(values);
    image = javax.imageio.ImageIO.read(stream);
    stream.close();
    if feature('ShowFigureWindows')
        figure(pixels.getSizeC().getValue()+1);
        imshow(JavaImageToMatlab(image));
    end
    
    %Close the rendering engine.
    re.close();
    
    % Load cache thumbnail
    disp('Loading the cache thumbnail');
    thumbnail = getThumbnail(session, imageId);
    if feature('ShowFigureWindows')
        figure;
        imshow(thumbnail, []);
    end
    
    % Load cache thumbnail and set the longest side
    thumbnail = getThumbnailByLongestSide(session, imageId, 96);
    if feature('ShowFigureWindows')
        figure;
        imshow(thumbnail, []);
    end
    
catch err
    client.closeSession();
    throw(err);
end

% Close the session
client.closeSession();
