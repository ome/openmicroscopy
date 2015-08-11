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
    projectId = p.projectid;
    datasetId = p.datasetid;
    imageId = p.imageid;
    plateId = p.plateid;

    print_object = @(x) fprintf(1, '  %s (id: %d, owner: %d, group: %d)\n',...
        char(x.getName().getValue()), x.getId().getValue(),...
        x.getDetails().getOwner().getId().getValue(),...
        x.getDetails().getGroup().getId().getValue());

    %% Projects
    % Retrieve all the projects and orphaned datasets owned by session
    % owner in the current context
    % If a project contains datasets, the datasets will automatically be
    % loaded but the images contained in the datasets are not loaded.
    disp(['Retrieving projects and orphaned datasets owned '...
        'by the session user in the current group']);
    [projects, orphanedDatasets] = getProjects(session);
    fprintf(1, '  Found %g projects\n', numel(projects));
    for i = 1 : numel(projects),
        print_object(projects(i));
        datasets = toMatlabList(projects(i).linkedDatasetList);
        for j = 1 : numel(datasets),
            fprintf(1, '  ');
            print_object(datasets(j));
        end
    end
    fprintf(1, '  Found %g orphaned datasets\n', numel(orphanedDatasets));
    for j = 1 : numel(orphanedDatasets),
        print_object(orphanedDatasets(j));
    end
    fprintf(1, '\n');

    % Retrieve all the unloaded projects owned by the session owner across
    % groups
    disp('Retrieving projects owned by the session user across groups')
    projects = getProjects(session, 'group', -1);
    fprintf(1, '  Found %g projects\n', numel(projects));
    for i = 1 : numel(projects),
        print_object(projects(i));
    end
    fprintf(1, '\n');

    % Retrieve all the unloaded projects owned by the session owner across
    % groups
    disp('Retrieving projects across by any user in the current group')
    projects = getProjects(session, 'owner', -1);
    fprintf(1, '  Found %g projects\n', numel(projects));
    for i = 1 : numel(projects),
        print_object(projects(i));
    end
    fprintf(1, '\n');

    % Retrieve a loaded project specified by an input identifier
    % If the dataset contains images, the images will be loaded
    fprintf(1, 'Reading project %g with loaded images\n', projectId);
    project = getProjects(session, projectId, true);
    assert(~isempty(project), 'OMERO:ReadData', 'Project Id not valid');
    datasets = toMatlabList(project.linkedDatasetList);
    for i = 1 : numel(datasets),
        print_object(datasets(i));
        images = toMatlabList(datasets(i).linkedImageList);
        for j = 1 : numel(images)
            fprintf(1, '  ');
            print_object(images(j));
        end
    end
    fprintf(1, '\n');

    %% Datasets
    % Retrieve all the unloaded datasets owned by the session owner.
    % If the datasets contain images, the images will not be loaded.
    disp('Listing datasets owned by the session user');
    allDatasets = getDatasets(session);
    fprintf(1, '  Found %g datasets\n', numel(allDatasets));
    for i = 1 : numel(allDatasets),
        print_object(allDatasets(i));
    end
    fprintf(1, '\n');

    % Retrieve all the unloaded datasets owned by the session owner across
    % all groups
    disp('Retrieving dataset owned by the session user across all groups')
    allDatasetsAllGroups = getDatasets(session, 'group', -1);
    fprintf(1, '  Found %g datasets\n', numel(allDatasetsAllGroups));
    for i = 1 : numel(allDatasetsAllGroups),
        print_object(allDatasetsAllGroups(i));
    end
    fprintf(1, '\n');

    % Retrieve a loaded dataset specified by an input identifier
    % If the dataset contains images, the images will be loaded
    fprintf(1, 'Retrieving dataset %g with loaded images\n', datasetId);
    dataset = getDatasets(session, datasetId, true);
    assert(~isempty(dataset), 'OMERO:ReadData', 'Dataset Id not valid');
    images = toMatlabList(dataset.linkedImageList); % The images in the dataset.
    fprintf(1, '  Found %g images\n', numel(images));
    for i = 1 : numel(images),
        print_object(images(i));
    end
    fprintf(1, '\n');

    %% Images
    % Retrieve all the images owned by the session user.
    disp('Retrieving images owned by the session user in the current group')
    allImages = getImages(session);
    fprintf(1, ' Found %g images\n', numel(allImages));
    for i = 1 : numel(allImages),
        print_object(allImages(i));
    end
    fprintf(1, '\n');

    % Retrieve all the images owned by the session user across all groups
    disp('Retrieving images owned by the session user across groups')
    allImagesAllGroups = getImages(session, 'group', -1);
    fprintf(1, '  Found %g images\n', numel(allImagesAllGroups));
    for i = 1 : numel(allImagesAllGroups),
        print_object(allImagesAllGroups(i));
    end
    fprintf(1, '\n');

    % Retrieve all the images contained in a given dataset.
    fprintf(1, 'Retrieving images contained in dataset %g\n', datasetId);
    images2 = getImages(session, 'dataset', datasetId);
    fprintf(1, '  Found %g images\n', numel(images2));
    for i = 1 : numel(images2),
        print_object(images2(i));
    end
    fprintf(1, '\n');

    % Retrieve all the images contained in a given dataset.
    fprintf(1, 'Retrieving images contained in project %g\n', projectId);
    images3 = getImages(session, 'project', projectId);
    fprintf(1, '  Found %g images\n', numel(images3));
    for i = 1 : numel(images3),
        print_object(images3(i));
    end
    fprintf(1, '\n');

    % Retrieve an image if the identifier is known.
    fprintf(1, 'Retrieving image: %g\n', imageId);
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
    
    %% Screens
    % Retrieve Screening data owned by the user currently logged in.
    
    % There is no explicit method in the gateway exposed to retrieve screening data
    % (to learn about the model go to ScreenPlateWell) but you can use the ContainerService to
    % load the data, you can use the method `findAllByQuery`.

    % load Screen and plate owned by the user currently logged in
    disp('Retrieving all screens and orphaned plates owned by the session user')
    [screens, orphanedPlates] = getScreens(session);
    fprintf(1, '  Found %g screens\n', numel(screens));
    for i = 1 : numel(screens),
        print_object(screens(i));
        plates = toMatlabList(screens(i).linkedPlateList);
        for j = 1 : numel(plates),
            fprintf(1, '  ');
            print_object(plates(j));
            plateAcquisitions = toMatlabList(plates(j).copyPlateAcquisitions());
            for k = 1 : numel(plateAcquisitions),
                fprintf(1, '    ');
                print_object(plateAcquisitions(k));
            end
        end
    end
    fprintf(1, '  Found %g orphaned plates\n', numel(orphanedPlates));
    for i = 1 : numel(orphanedPlates),
        print_object(orphanedPlates(i));
        plateAcquisitions = toMatlabList(orphanedPlates(i).copyPlateAcquisitions());
        for j = 1 : numel(plateAcquisitions),
            fprintf(1, '  ');
            print_object(plateAcquisitions(j));
        end
    end
    fprintf(1, '\n');

    % Retrieve all the images owned by the session user across all groups
    disp('Retrieving all screens owned by the session user across all groups')
    allScreensAllGroups = getScreens(session, 'group', -1);
    fprintf(1, '  Found %g screens\n', numel(allScreensAllGroups));
    for i = 1 : numel(allScreensAllGroups),
        print_object(allScreensAllGroups(i));
    end
    fprintf(1, '\n');

    %% Plates
    % Retrieve all the plates owned by the session user.
    disp('Listing plates owned by the session user');
    allPlates = getPlates(session);
    fprintf(1, '  Found %g plates\n', numel(allPlates));
    for i = 1 : numel(allPlates),
        print_object(allPlates(i));
    end
    fprintf(1, '\n');

    % Retrieve all the pldates owned by the session user across all groups
    disp('Retrieving plates owned by the session user across all groups')
    allPlatesAllGroups = getPlates(session, 'group', -1);
    fprintf(1, '  Found %g plates\n', numel(allPlatesAllGroups));
    for i = 1 : numel(allPlatesAllGroups),
        print_object(allPlatesAllGroups(i));
    end
    fprintf(1, '\n');

    % Retrieve Wells within a Plate, see ScreenPlateWell.

    % Given a plate ID, load the wells.
    % You will have to use the findAllByQuery method.
    fprintf(1, 'Listing wells for plate: %g\n ', plateId);
    wells = session.getQueryService().findAllByQuery(['select well from Well as well left outer join fetch well.plate as pt left outer join fetch well.wellSamples as ws left outer join fetch ws.plateAcquisition as pa left outer join fetch ws.image as img left outer join fetch img.pixels as pix left outer join fetch pix.pixelsType as pt where well.plate.id =  ', num2str(plateId)], []);
    wells = toMatlabList(wells);
    fprintf(1, '  Found %g wells\n ', numel(wells));
    
    for i = 1:numel(wells),
        wellSamples = toMatlabList(wells(i).copyWellSamples());
        fprintf(1, '  Well %g - Found %g well samples\n ', i, numel(wellSamples));
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
