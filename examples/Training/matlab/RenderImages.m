% Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
% All rights reversed.
% Use is subject to license terms supplied in LICENSE.txt

% information to edit.
imageId = java.lang.Long(27544);

try
    [client, session] = Connect();

    imagename = tempname();
    proxy = session.getContainerService();
    list = proxy.getImages(omero.model.Image.class, java.util.Arrays.asList(imageId), omero.sys.ParametersI());
    if (list.size == 0)
        exception = MException('OMERO:RenderImages', 'Image Id not valid');
        throw(exception);
    end
    image = list.get(0);
    pixelsList = image.copyPixels();
    pixels = pixelsList.get(0);

    % Rendering 

    % Follow an example indicating, how to create a rendering engine.

    % Create rendering engine.
    pixelsId = pixels.getId().getValue(); % see Load data section
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
    
    %save to Jpeg
    %file = [imagename '.jpg'];
    %fid = fopen(file, 'wb');
    %fwrite(fid, values, 'int8');
    %fclose(fid);
    %delete(file);

    %Close the rendering engine.
    re.close();
    
    %Load thumbnails.
    store = session.createThumbnailStore();
    map = store.getThumbnailByLongestSideSet(omero.rtypes.rint(96), java.util.Arrays.asList(java.lang.Long(pixelsId)));
    %Display the thumbnail;
    collection = map.values();
    i = collection.iterator();
    while (i.hasNext())
        figure(100);
        stream = java.io.ByteArrayInputStream(i.next());
        image = javax.imageio.ImageIO.read(stream);
        stream.close();
        imshow(JavaImageToMatlab(image));
    end
    store.close();
catch err
     disp(err.message);
end

% close the session
client.closeSession();