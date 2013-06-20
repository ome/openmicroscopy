% Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
% All rights reversed.
% Use is subject to license terms supplied in LICENSE.txt

try
    % Create a connection
    [client, session] = loadOmero();
    fprintf(1, 'Created connection to %s\n', char(client.getProperty('omero.host')));
    fprintf(1, 'Created session for user %s using group %s\n',...
        char(session.getAdminService().getEventContext().userName),...
        char(session.getAdminService().getEventContext().groupName));
    
    % Information to edit
    imageId = str2double(client.getProperty('image.id'));

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
        re.resetDefaults();
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
            figure(k+1);
            imshow(JavaImageToMatlab(image));
            %make all the channels active.
            for i = 0:sizeC,
               re.setActive(i, 1);
            end
        end
    end
    % All channels active and save the image as a JPEG.
    figure(pixels.getSizeC().getValue()+1);
    values = re.renderCompressed(pDef);
    stream = java.io.ByteArrayInputStream(values);
    image = javax.imageio.ImageIO.read(stream);
    stream.close();
    imshow(JavaImageToMatlab(image));

    %Close the rendering engine.
    re.close();
    
    % Load cache thumbnail
    disp('Loading the cache thumbnails');
    thumbnail = getThumbnail(session, imageId);
    figure; 
    imshow(thumbnail, []);
    
    % Load cache thumbnail and set the longest side
    thumbnail = getThumbnailByLongestSide(session, imageId, 96);
    figure; 
    imshow(thumbnail, []);
catch err
     disp(err.message);
end

% close the session
client.closeSession();