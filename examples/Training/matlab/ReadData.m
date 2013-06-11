% Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
% All rights reversed.
% Use is subject to license terms supplied in LICENSE.txt

% Information to edit
datasetId = 2651;
imageId = 27544;
plateId = 1007;
% Load Data
try
    [client, session] = connect();
    
    % Retrieve the project(s) owned by user currently logged in.
    % If a project contains datasets, the datasets will automatically be
    % loaded.
    projects = getProjects(session);
    for i = 1 : numel(projects),
        datasets = toMatlabList(projects(i).linkedDatasetList);
        for j = 1 : numel(datasets),
            %only if param.leaves() has been set.
            images = toMatlabList(datasets(j).linkedImageList);
            %for k = 1 : numel(images),
            %    imageId = image.getId().getValue();
            %end
        end
    end
    
    % Retrieve the dataset(s) owned by the user currently logged in.
    datasets = getDatasets(session);
    
    % Retrieve the images contained in a given dataset.
    dataset = getDatasets(session, datasetId);
    assert(~isempty(dataset), 'OMERO:ReadData', 'Dataset Id not valid');
    images = toMatlabList(dataset.linkedImageList); % The images in the dataset.
    
    % Retrieve an image if the identifier is known.
    image = getImages(session, imageId);
    proxy = session.getContainerService();
    assert(~isempty(image), 'OMERO:ReadData', 'Image Id not valid');
    
    % Access information about the image for example to draw it.
    
    % The model is a follow: Image-Pixels i.e. to access valuable data about
    % the image you need to use the pixels object.
    % We now only support one set of pixels per image (it used to be more!).
    
    pixels = image.getPrimaryPixels();
    sizeZ = pixels.getSizeZ().getValue(); % The number of z-sections.
    sizeT = pixels.getSizeT().getValue(); % The number of timepoints.
    sizeC = pixels.getSizeC().getValue(); % The number of channels.
    sizeX = pixels.getSizeX().getValue(); % The number of pixels along the X-axis.
    sizeY = pixels.getSizeY().getValue(); % The number of pixels along the Y-axis.
    
    % Retrieve Screening data owned by the user currently logged in.
    
    % There is no explicit method in the gateway exposed to retrieve screening data
    % (to learn about the model go to ScreenPlateWell) but you can use the ContainerService to
    % load the data, you can use the method `findAllByQuery`.
    
    % load Screen and plate owned by the user currently logged in
    screens = getScreens(session);
    
    for i = 1 : numel(screens),
        plates = toMatlabList(screens(i).linkedPlateList);
        for j = 1 : numel(plates),
            plateAcquisitions = toMatlabList(plates(j).copyPlateAcquisitions());
            for k = 1 : numel(plateAcquisitions),
                pa = plateAcquisitions(k);
            end
        end
    end
    
    % Retrieve Wells within a Plate, see ScreenPlateWell.
    
    % Given a plate ID, load the wells.
    % You will have to use the findAllByQuery method.
    
    wellList = session.getQueryService().findAllByQuery(['select well from Well as well left outer join fetch well.plate as pt left outer join fetch well.wellSamples as ws left outer join fetch ws.plateAcquisition as pa left outer join fetch ws.image as img left outer join fetch img.pixels as pix left outer join fetch pix.pixelsType as pt where well.plate.id =  ', num2str(plateId)], []);
    for j = 0:wellList.size()-1,
        well = wellList.get(j);
        wellsSampleList = well.copyWellSamples();
        well.getId().getValue()
        for i = 0:wellsSampleList.size()-1,
            ws = wellsSampleList.get(i);
            ws.getId().getValue();
            pa = ws.getPlateAcquisition();
            pa.getId().getValue();
        end
    end
catch err
    disp(err.message);
end


% close the session
client.closeSession();
