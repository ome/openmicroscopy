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
    datasetName = 'myDataset';
    tagNs = 'training.ns';
    tagName = 'myTagName';

    %First create datasets
    n = 2;
    for i = 1 : n,
        createDataset(session, datasetName);
    end
    
    % Create tag
    for i = 1 : n,
        writeTagAnnotation(session, tagName, 'namespace', tagNs);
    end
    
    % Create query filter
    filter = omero.sys.Filter();
    filter.limit = rint(10);
    
    % Query datasets by name
    queryService = session.getQueryService();
    results = queryService.findAllByString('omero.model.Dataset',...
        'name', datasetName, 1, filter);
    datasets = toMatlabList(results);
    fprintf('Found %g datasets named %s\n', numel(datasets), datasetName);
    for i = 1 : numel(datasets)
        fprintf('  Dataset %s (id: %g)\n',...
            char(datasets(i).getName().getValue()),...
            datasets(i).getId().getValue());
    end
    
    % Query tags by namespace
    results = queryService.findAllByString('omero.model.TagAnnotation',...
        'ns', tagNs, 1, filter);
    tags = toMatlabList(results);
    fprintf('Found %g tags with namespace %s\n', numel(tags), tagNs);
    for i = 1 : numel(tags)
        fprintf('  Tag %s with namespace %s (id: %g)\n',...
            char(tags(i).getTextValue().getValue()),...
            char(tags(i).getNs().getValue()),...
            tags(i).getId().getValue());
    end
    
    % Retrieve the project(s) owned by user currently logged in and load the orphaned datasets.
    % i.e. datasets not contained in a project.
    % If a project contains datasets, the datasets will automatically be
    % loaded.
    proxy = session.getContainerService();
    param = omero.sys.ParametersI();
    param.orphan();
    param.noLeaves();%indicate not to load the images
    userId = session.getAdminService().getEventContext().userId;
    param.exp(omero.rtypes.rlong(userId));
    results = proxy.loadContainerHierarchy(omero.model.Project.class, [], param);
    for j = 0:results.size()-1,
        value = results.get(j);
        if (isa(value, 'omero.model.Project'))
            % handle project
            p = value;
            p.linkedDatasetList();
        elseif (isa(value, 'omero.model.Dataset'))
            % handle dataset
            dataset = value;
            dataset.getName().getValue();
        end
    end
    
catch err
    client.closeSession();
    throw(err);
end

% Close the session
client.closeSession();
