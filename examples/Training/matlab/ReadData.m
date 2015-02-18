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
    datasetId = p.datasetid;
    imageId = p.imageid;
    plateId = p.plateid;
    
    % Retrieve all the projects and orphaned datasets owned by session
    % owner.
    % If a project contains datasets, the datasets will automatically be
    % loaded but the images contained in the datasets are not loaded.
    disp('Listing projects and orphaned datasets')
    [projects, orphanedDatasets] = getProjects(session, [], false);
    fprintf(1, 'Found %g projects\n', numel(projects));
    for i = 1 : numel(projects),
        datasets = toMatlabList(projects(i).linkedDatasetList);
        fprintf(1, '  Project %g: %s (%g) - %g datasets\n', i,...
            char(projects(i).getName().getValue()),...
            projects(i).getId().getValue(), numel(datasets));
        for j = 1 : numel(datasets),
            fprintf(1, '    Dataset %g: %s (%d)\n',...
                j, char(datasets(j).getName().getValue()),...
                datasets(j).getId().getValue());
        end
    end
    fprintf(1, 'Found %g orphaned datasets\n', numel(orphanedDatasets));
    for j = 1 : numel(orphanedDatasets),
        fprintf(1, '  Orphaned dataset %g: %s (%d)\n',...
            j, char(orphanedDatasets(j).getName().getValue()),...
            orphanedDatasets(j).getId().getValue());
    end
    fprintf(1, '\n');
    
    % Retrieve all the unloaded datasets owned by the session owner.
    % If the datasets contain images, the images will no be loaded.
    disp('Listing datasets')
    datasets = getDatasets(session, [], false);
    fprintf(1, 'Found %g datasets\n', numel(datasets));
    fprintf(1, '\n');
    
    % Retrieve a dataset specified by an input identifier
    % If the dataset contains images, the images will be loaded
    disp('Reading dataset with loaded images');
    dataset = getDatasets(session, datasetId, true);
    assert(~isempty(dataset), 'OMERO:ReadData', 'Dataset Id not valid');
    images = toMatlabList(dataset.linkedImageList); % The images in the dataset.
    fprintf(1, 'Found %g images in dataset %g using getDatasets()\n',...
        numel(images), datasetId);
    
    % Retrieve all the images contained in a given dataset.
    images2 = getImages(session, 'dataset', datasetId);
    fprintf(1, 'Found %g images in dataset %g using getImages()\n',...
        numel(images2), datasetId);
    fprintf(1, '\n');
    
    % Retrieve an image if the identifier is known.
    fprintf(1, 'Reading image: %g\n', imageId);
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
    fprintf(1, '  SizeX: %g\n', sizeX);
    fprintf(1, '  SizeY: %g\n', sizeY);
    fprintf(1, '  SizeZ: %g\n', sizeZ);
    fprintf(1, '  SizeC: %g\n', sizeC);
    fprintf(1, '  SizeT: %g\n', sizeT);
    fprintf(1, '\n');
    
    % Retrieve Screening data owned by the user currently logged in.
    
    % There is no explicit method in the gateway exposed to retrieve screening data
    % (to learn about the model go to ScreenPlateWell) but you can use the ContainerService to
    % load the data, you can use the method `findAllByQuery`.
    
    % load Screen and plate owned by the user currently logged in
    disp('Listing screens and orphaned plates')
    [screens, orphanedPlates] = getScreens(session);
    fprintf(1, 'Found %g screens\n', numel(screens));
    for i = 1 : numel(screens),
        plates = toMatlabList(screens(i).linkedPlateList);
        fprintf(1, '  Screen %g: %s (%g) - %g plates\n', i,...
            char(screens(i).getName().getValue()),...
            screens(i).getId().getValue(), numel(plates));
        for j = 1 : numel(plates),
            plateAcquisitions = toMatlabList(plates(j).copyPlateAcquisitions());
            fprintf(1, '    Plate %g: %s (%d) - %g plate runs\n',...
                i, j, char(plates(j).getName().getValue()),...
                plates(j).getId().getValue(), numel(plateAcquisitions));
            for k = 1 : numel(plateAcquisitions),
                pa = plateAcquisitions(k);
            end
        end
    end
    fprintf(1, 'Found %g orphaned plates\n', numel(orphanedPlates));
    for i = 1 : numel(orphanedPlates),
        plateAcquisitions = toMatlabList(orphanedPlates(i).copyPlateAcquisitions());
        fprintf(1, '  Orphaned plate %g: %s (%g) - %g plate runs\n', i,...
            char(orphanedPlates(i).getName().getValue()),...
            orphanedPlates(i).getId().getValue(), numel(plateAcquisitions));
    end
    fprintf(1, '\n');
    
    % Retrieve Wells within a Plate, see ScreenPlateWell.
    
    % Given a plate ID, load the wells.
    % You will have to use the findAllByQuery method.
    fprintf(1, 'Listing wells for plate: %g\n ', plateId);
    wells = session.getQueryService().findAllByQuery(['select well from Well as well left outer join fetch well.plate as pt left outer join fetch well.wellSamples as ws left outer join fetch ws.plateAcquisition as pa left outer join fetch ws.image as img left outer join fetch img.pixels as pix left outer join fetch pix.pixelsType as pt where well.plate.id =  ', num2str(plateId)], []);
    wells = toMatlabList(wells);
    fprintf(1, 'Found %g wells\n ', numel(wells));
    
    for i = 1:numel(wells),
        wellSamples = toMatlabList(wells(i).copyWellSamples());
        fprintf(1, 'Well %g - Found %g well samples\n ', i, numel(wellSamples));
        for j = 1:numel(wellSamples),
            pa = wellSamples(j).getPlateAcquisition();
        end
    end
    
catch err
    client.closeSession();
    throw(err);
end

% Close the session
client.closeSession();
