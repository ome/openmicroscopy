function images = getImages(session, varargin)
% GETIMAGES Retrieve image objects from the OMERO server
%
%   images = getImages(session) returns all the images owned by the session
%   user in the context of the session group.
%
%   images = getImages(session, ids) returns all the images identified by
%   the input ids owned by the session user in the context of the session
%   group.
%
%   images = getImages(session, ids, type) returns all the images contained
%   in the objects of input type identified by the input ids and owned by
%   the session user in the context of the session group.
%
%
%   Examples:
%
%      images = getImages(session);
%      images = getImages(session, ids);
%      images = getImages(session, [], 'project);
%      images = getImages(session, datasetIds, 'dataset');
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
types = {'image', 'dataset', 'project'};
ip = inputParser;
ip.addOptional('ids', [], @(x) isempty(x) || isvector(x));
ip.addOptional('type', 'image', @(x) ismember(x, types));
ip.parse(varargin{:});

if strcmp(ip.Results.type, 'image')
    % Add the current user id to the loading parameters
    parameters = omero.sys.ParametersI();
    userId = session.getAdminService().getEventContext().userId;
    parameters.exp(rlong(userId));
    
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
    % Get loaded projects/datasets 
    if strcmp(ip.Results.type, 'project')
        projects = getProjects(session, ip.Results.ids, true);
        datasetList = arrayfun(@(x) toMatlabList(x.linkedDatasetList), projects,...
            'UniformOutput', false);
        datasets = [datasetList{:}];
    elseif strcmp(ip.Results.type, 'dataset')
        datasets = getDatasets(session, ip.Results.ids, true);
    end
    
    % Reconstruct image lists from dataset array
    imageList = arrayfun(@(x) toMatlabList(x.linkedImageList), datasets,...
        'UniformOutput', false);
    images = [imageList{:}];
end