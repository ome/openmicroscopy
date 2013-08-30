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

% Load Data
try
    disp('Creating connection');
    [client, session] = loadOmero();
    fprintf(1, 'Created session for  %s', char(client.getProperty('omero.host')));
    fprintf(1, ' for user %s',...
        char(session.getAdminService().getEventContext().userName));
    fprintf(1, ' using group %s\n',...
        char(session.getAdminService().getEventContext().groupName));
    
    % Information to edit
    datasetId = str2double(client.getProperty('dataset.id'));
    imageId = str2double(client.getProperty('image.id'));
    plateId = str2double(client.getProperty('plate.id'));
    
    % Retrieve the project(s) owned by user currently logged in.
    % If a project contains datasets, the datasets will automatically be
    % loaded.
    disp('Listing projects')
    projects = getProjects(session);
    fprintf(1, 'Found %g projects\n', numel(projects));
    for i = 1 : numel(projects),
        datasets = toMatlabList(projects(i).linkedDatasetList);
        fprintf(1, 'Project %g: found %g datasets\n', i, numel(datasets));
        for j = 1 : numel(datasets),
            %only if param.leaves() has been set.
            images = toMatlabList(datasets(j).linkedImageList);
            fprintf(1, 'Project %g - dataset %g : found %g images\n',...
                i, j, numel(images));
            %for k = 1 : numel(images),
            %    imageId = image.getId().getValue();
            %end
        end
    end
    fprintf(1, '\n');
    
    % Retrieve the dataset(s) owned by the user currently logged in.
    disp('Listing datasets')
    datasets = getDatasets(session);
    fprintf(1, 'Found %g datasets\n', numel(datasets));
    fprintf(1, '\n');
    
    % Retrieve the images contained in a given dataset.
    fprintf(1, 'Reading dataset: %g\n', datasetId);
    dataset = getDatasets(session, datasetId);
    assert(~isempty(dataset), 'OMERO:ReadData', 'Dataset Id not valid');
    images = toMatlabList(dataset.linkedImageList); % The images in the dataset.
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
    fprintf(1, 'sizeX: %g\n', sizeX);
    fprintf(1, 'sizeY: %g\n', sizeY);
    fprintf(1, 'sizeZ: %g\n', sizeZ);
    fprintf(1, 'sizeC: %g\n', sizeC);
    fprintf(1, 'sizeT: %g\n', sizeT);
    fprintf(1, '\n');
    
    % Retrieve Screening data owned by the user currently logged in.
    
    % There is no explicit method in the gateway exposed to retrieve screening data
    % (to learn about the model go to ScreenPlateWell) but you can use the ContainerService to
    % load the data, you can use the method `findAllByQuery`.
    
    % load Screen and plate owned by the user currently logged in
    disp('Listing screens')
    screens = getScreens(session);
    fprintf(1, 'Found %g screens\n', numel(screens));
    
    for i = 1 : numel(screens),
        plates = toMatlabList(screens(i).linkedPlateList);
        fprintf(1, 'Screen %g: found %g plates\n', i, numel(plates));
        for j = 1 : numel(plates),
            plateAcquisitions = toMatlabList(plates(j).copyPlateAcquisitions());
            fprintf(1, 'Screen %g - plate %g: found %g plate runs\n',...
                i, j, numel(plateAcquisitions));
            for k = 1 : numel(plateAcquisitions),
                pa = plateAcquisitions(k);
            end
        end
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
