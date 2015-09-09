function dataset = createDataset(session, name, varargin)
% CREATEDATASET Create a new dataset and uploads it onto the OMERO server
%
%   dataset = createDataset(session, name) create a new dataset with the
%   input name, uploads it onto the server and returns the loaded dataset.
%
%   dataset = createDataset(session, name, 'group', groupId) specifies the
%   group context in which the dataset should be created.
%
%   dataset = createDataset(session, name, project) creates a dataset and
%   links it to the input project. The group context is specified by the
%   project group.
%
%   dataset = createDataset(session, name, projectId) creates a new dataset
%   and links it to the project specified by the input identifier. The
%   group context is specified by the project group.
%
%   Examples:
%
%      % Create a new dataset in the context of the current session group
%      dataset = createDataset(session, 'my-dataset');
%      % Create a new dataset in the context of the specified group
%      dataset = createDataset(session, 'my-dataset', 'group', groupId);
%      % Create a new dataset and link it to the specified project
%      dataset = createDataset(session, 'my-dataset', project);
%      dataset = createDataset(session, 'my-dataset', projectId);
%
% See also: CREATEOBJECT, CREATEPROJECT

% Copyright (C) 2013-2015 University of Dundee & Open Microscopy Environment.
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


% Check if optional project is passed
isValidProject = @(x) isscalar(x) && ...
    (isnumeric(x) || isa(x, 'omero.model.ProjectI'));
ip = inputParser;
ip.addRequired('name', @ischar);
ip.addOptional('project', [], isValidProject);
ip.addParamValue('context', java.util.HashMap, @(x) isa(x, 'java.util.HashMap'));
ip.addParamValue('group', [], @(x) isempty(x) || (isscalar(x) && isnumeric(x)));
ip.parse(name, varargin{:});

if ~isempty(ip.Results.project)
    % Retrieve loaded project
    if isnumeric(ip.Results.project)
        project = getProjects(session, ip.Results.project);
        assert(~isempty(project), 'Cannot find project %g', ip.Results.project);
    else
        project = ip.Results.project;
    end
    
    % Determine group from the parent project
    groupId = project.getDetails().getGroup().getId().getValue();
    if ~isempty(ip.Results.group)
        if ~isempty(groupId)
            assert(isequal(groupId, ip.Results.group),...
                'Input group is different from the project group.');
        end
        groupId = ip.Results.group;
    end
    
    % Create dataset object
    dataset = omero.model.DatasetI();
    dataset.setName(rstring(name));

    % Create context for uploading
    context = ip.Results.context;
    context.put('omero.group', java.lang.String(num2str(groupId)));

    % Create project/dataset link
    link = omero.model.ProjectDatasetLinkI();
    link.setParent(omero.model.ProjectI(project.getId().getValue(), false));
    link.setChild(dataset);
    link = session.getUpdateService().saveAndReturnObject(link, context);
    
    % Return the dataset
    dataset = link.getChild();
else
    % Delegate object creation
    dataset = createObject(session, 'dataset', name,...
        'context', ip.Results.context, 'group', ip.Results.group);
end

end
