function images = getImages(session, varargin)
% GETIMAGES Retrieve image objects from the OMERO server
%
%   images = getImages(session) returns all the images owned by the session
%   user in the context of the session group.
%
%   images = getImages(session, ids) returns all the images identified by
%   the input ids in the context of the session group.
%
%   images = getImages(session, 'owner', ownerId) returns all the images
%   owned by the input owner in the context of the session group.
%
%   images = getImages(session, 'group', ownerId) returns all the images
%   owned by the session owner in the context of the input group. A value
%   of -1 for groupId means images are returned for all groups.
%
%   images = getImages(session, ids, 'owner', ownerId) returns all the
%   images identified by the input ids and owned by the input owner in the
%   context of the session group.
%
%   images = getImages(session, ids, 'group', groupId) returns all the
%   images identified by the input ids owned by the session owner in the
%   context of the input group. A value of -1 for groupId means images
%   are returned for all groups.
%
%   images = getImages(session, 'project', projectIds) returns all the
%   images contained in the projects identified by the input ids in the
%   context of the session group.
%
%   images = getImages(session, 'dataset', datasetIds) returns all the
%   images contained in the datasets identified by the input ids in the
%   context of the session group.

%
%   Examples:
%
%      images = getImages(session);
%      images = getImages(session, 'owner', ownerId);
%      images = getImages(session, 'group', -1);
%      images = getImages(session, ids);
%      images = getImages(session, ids, 'owner', ownerId);
%      images = getImages(session, ids, 'group', -1);
%      images = getImages(session, 'project', projectIds);
%      images = getImages(session, 'dataset', projectIds);
%
% See also: GETOBJECTS, GETPROJECTS, GETDATASETS, GETPLATES

% Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
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

% Input check
isValidIds =  @(x) isempty(x) || (isvector(x) && isnumeric(x));
ip = inputParser;
ip.addRequired('session');
ip.addOptional('ids', [], isValidIds);
ip.addParamValue('project', [], isValidIds);
ip.addParamValue('dataset', [], isValidIds);
ip.KeepUnmatched = true;
ip.parse(session, varargin{:});

if isempty(ip.Results.project) && isempty(ip.Results.dataset)
    % Check optional input parameters
    if isempty(ip.Results.ids)
        % If no input id, return the objects owned by the session user by default
        defaultOwner = session.getAdminService().getEventContext().userId;
    else
        % If ids are specified, return the objects owned by any user by default
        defaultOwner = -1;
    end
    ip.addParamValue('owner', defaultOwner, @(x) isscalar(x) && isnumeric(x));
    ip.KeepUnmatched = false;
    ip.parse(session, varargin{:});

    % Add the owner user id to the loading parameters
    parameters = omero.sys.ParametersI();
    parameters.exp(rlong(ip.Results.owner));
    
    % Create container service to load objects
    proxy = session.getContainerService();
    
    if isempty(ip.Results.ids),
        % Load all images belonging to current session user
        imageList = proxy.getUserImages(parameters);
    else
        % Load all images specified by input ids
        ids = toJavaList(ip.Results.ids, 'java.lang.Long');
        imageList = proxy.getImages('omero.model.Image', ids, parameters);
    end
    
    % Convert java.util.ArrayList into Matlab arrays
    images = toMatlabList(imageList);
    
else
    % Check project and dataset are not set at the same time
    assert(isempty(ip.Results.project) || isempty(ip.Results.dataset),...
        ['Both project and dataset arguments cannot be specified '...
        'at the same time.']);

    % Get projects/datasets by Id
    if ~isempty(ip.Results.project)
        projects = getProjects(session, ip.Results.project, true);
        datasetList = arrayfun(@(x) toMatlabList(x.linkedDatasetList), projects,...
            'UniformOutput', false);
        datasets = [datasetList{:}];
    else
        datasets = getDatasets(session, ip.Results.dataset, true);
    end
    
    % Reconstruct image lists from dataset array
    imageList = arrayfun(@(x) toMatlabList(x.linkedImageList), datasets,...
        'UniformOutput', false);
    images = [imageList{:}];
end