% Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
% All rights reversed.
% Use is subject to license terms supplied in LICENSE.txt

% Information to edit
datasetId = java.lang.Long(2651);
imageId = java.lang.Long(27544);
plateId = 1007;
% Load Data
try
    [client, session] = connect();
    % Retrieve the project(s) owned by user currently logged in.
    % If a project contains datasets, the datasets will automatically be
    % loaded. 
    proxy = session.getContainerService();
    param = omero.sys.ParametersI();
    param.noLeaves();%indicate not to load the images
    userId = session.getAdminService().getEventContext().userId;
    param.exp(omero.rtypes.rlong(userId));
    projectsList = proxy.loadContainerHierarchy(omero.model.Project.class, [], param);
    for j = 0:projectsList.size()-1,
        p = projectsList.get(j);
        datasetsList = p.linkedDatasetList;
        for i = 0:datasetsList.size()-1,
            d = datasetsList.get(i);
            %only if param.leaves() has been set.
            %imageList = d.linkedImageList;
            %for k = 0:imageList.size()-1,
            %    image = imageList.get(k);
            %    image.getId().getValue();
            %end
        end
    end 

    % Retrieve the dataset(s) owned by the user currently logged in.

    param = omero.sys.ParametersI();
    param.leaves();%indicate to load the images
    userId = session.getAdminService().getEventContext().userId;
    param.exp(omero.rtypes.rlong(userId));
    projectsList = proxy.loadContainerHierarchy(omero.model.Dataset.class, [], param);

    % Retrieve the images contained in a given dataset.
    proxy = session.getContainerService();
    param = omero.sys.ParametersI();
    param.leaves(); % indicate to load the images.
    list = proxy.loadContainerHierarchy(omero.model.Dataset.class, java.util.Arrays.asList(datasetId), param);
    if (list.size == 0)
        exception = MException('OMERO:ReadData', 'Dataset Id not valid');
        throw(exception);
    end
    dataset = list.get(0);
    imageList = dataset.linkedImageList; % The images in the dataset.

    % Retrieve an image if the identifier is known.
    proxy = session.getContainerService();
    list = proxy.getImages(omero.model.Image.class, java.util.Arrays.asList(imageId), omero.sys.ParametersI());
    if (list.size == 0)
        exception = MException('OMERO:ReadData', 'Image Id not valid');
        throw(exception);
    end
    image = list.get(0);

    % Access information about the image for example to draw it.

    % The model is a follow: Image-Pixels i.e. to access valuable data about
    % the image you need to use the pixels object. 
    % We now only support one set of pixels per image (it used to be more!).

    pixelsList = image.copyPixels();
    for k = 0:pixelsList.size()-1,
       pixels = pixelsList.get(k);
       sizeZ = pixels.getSizeZ().getValue() % The number of z-sections.
       sizeT = pixels.getSizeT().getValue() % The number of timepoints.
       sizeC = pixels.getSizeC().getValue() % The number of channels.
       sizeX = pixels.getSizeX().getValue() % The number of pixels along the X-axis.
       sizeY = pixels.getSizeY().getValue() % The number of pixels along the Y-axis.
    end

    % Retrieve Screening data owned by the user currently logged in.

    % There is no explicit method in the gateway exposed to retrieve screening data 
    % (to learn about the model go to ScreenPlateWell) but you can use the ContainerService to 
    % load the data, you can use the method `findAllByQuery`. 

    % load Screen and plate owned by the user currently logged in
    proxy = session.getContainerService();
    userId = session.getAdminService().getEventContext().userId;
    param = omero.sys.ParametersI;
    param.exp(omero.rtypes.rlong(userId)); 

    screenList = proxy.loadContainerHierarchy(omero.model.Screen.class, [], param);
    for j = 0:screenList.size()-1,
      screen = screenList.get(j);
      platesList = screen.linkedPlateList;
      for i = 0:platesList.size()-1,
        plate = platesList.get(i);
        plateAcquisitionList = plate.copyPlateAcquisitions();
        for k = 0:plateAcquisitionList.size()-1,
          pa = plateAcquisitionList.get(i);
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
