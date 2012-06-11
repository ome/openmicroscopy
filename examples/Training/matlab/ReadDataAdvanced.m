% Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
% All rights reversed.
% Use is subject to license terms supplied in LICENSE.txt

% Information to edit
datasetName = char('myDataset');
tagNs = char('imperial.training.demo');
tagName = char('myTagName');

% Load Data
try
    [client, session] = connect();
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
    disp(err.message);
end


% close the session
client.closeSession();
