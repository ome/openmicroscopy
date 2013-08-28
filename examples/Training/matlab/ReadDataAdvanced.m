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
% Information to edit
datasetName = char('myDataset');
tagNs = char('imperial.training.demo');
tagName = char('myTagName');

% Load Data
try
    disp('Creating connection');
    [client, session] = loadOmero();
    fprintf(1, 'Created session for  %s', char(client.getProperty('omero.host')));
    fprintf(1, ' for user %s',...
        char(session.getAdminService().getEventContext().userName));
    fprintf(1, ' using group %s\n',...
        char(session.getAdminService().getEventContext().groupName));
    
    iUpdate = session.getUpdateService();
    %First create datasets
    n = 2;
    for i= 0:n,
        d = omero.model.DatasetI;
        d.setName(omero.rtypes.rstring(datasetName));
        iUpdate.saveAndReturnObject(d);
    end
    
    % Create tag
    for i= 0:n,
        d = omero.model.TagAnnotationI;
        d.setNs(omero.rtypes.rstring(tagNs));
        d.setTextValue(omero.rtypes.rstring(tagName));
        iUpdate.saveAndReturnObject(d);
    end
    
    %Load the datasets by Name
    
    filter = omero.sys.Filter;
    filer.limit = omero.rtypes.rint(10);
    filer.offset = omero.rtypes.rint(10);
    
    proxy = session.getQueryService();
    results = proxy.findAllByString(omero.model.Dataset.class, 'name', datasetName, 1, filter);
    for i= 0:results.size-1,
        d = results.get(i);
        d.getName().getValue()
        d.getId().getValue()
    end
    
    %Load the tags by name space
    
    results = proxy.findAllByString(omero.model.TagAnnotation.class, 'ns', tagNs, 1, filter);
    for i= 0:results.size-1,
        tag = results.get(i);
        tag.getTextValue().getValue()
        tag.getId().getValue()
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
