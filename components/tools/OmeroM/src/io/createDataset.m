function dataset = createDataset(session, name, varargin)
% CREATEDATASET Create a new dataset and uploads it onto the OMERO server
%
%   dataset = createDataset(session, name) create a new dataset with the
%   input name, uploads it onto the server and returns the loaded dataset.
%
%   dataset = createDataset(session, name, project) also links the dataset
%   to the input project.
%
%   dataset = createDataset(session, name, projectId) also links the
%   dataset to the project specified by the input identifier.
%
%   dataset = createDataset(..., 'group', groupId) specifies the group
%   context in which the dataset should be created.
%
%   Examples:
%
%      dataset = createDataset(session, 'my-dataset');
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

% Delegate object creation
dataset = createObject(session, 'dataset', name, varargin{:});

% Check if optional project is passed
isValidProject = @(x) isscalar(x) && ...
    (isnumeric(x) || isa(x, 'omero.model.ProjectI'));
ip = inputParser;
ip.addOptional('project', [], isValidProject);
ip.parse(varargin{:});

if ~isempty(ip.Results.project)
    % Check project object
    if isnumeric(ip.Results.project)
        project = getProjects(session, ip.Results.project);
        assert(~isempty(project), 'Cannot find project %g', ip.Results.project);
    else
        project = ip.Results.project;
    end
    
    % Create project/dataset link
    link = omero.model.ProjectDatasetLinkI();
    link.setParent(project);
    link.setChild(dataset);
    session.getUpdateService().saveAndReturnObject(link);
    
    % Retrieve fully loaded dataset
    dataset = getDatasets(session, dataset.getId().getValue());
end

end
