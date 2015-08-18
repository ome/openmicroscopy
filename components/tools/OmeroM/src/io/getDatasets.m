function datasets = getDatasets(session, varargin)
% GETDATASETS Retrieve dataset objects from the OMERO server
%
%   datasets = getDatasets(session) returns all the datasets owned by the
%   session user in the context of the session group.
%
%   datasets = getDatasets(session, ids) returns all the datasets
%   identified by the input ids independently of the owner across all groups.
%
%   datasets = getDatasets(..., loaded) also loads the images attached to the
%   datasets if loaded is true. Default: false.
%
%   datasets = getDatasets(..., 'owner', owner) specifies the owner of the
%   datasets. A value of -1 implies datasets are returned independently of
%   the owner.
%
%   datasets = getDatasets(..., 'group', groupId) specifies the group
%   context for the datasets. A value of -1 means datasets are returned
%   across groups.
%
%   Examples:
%
%      datasets = getDatasets(session);
%      datasets = getDatasets(session, true);
%      datasets = getDatasets(session, 'owner', ownerId);
%      datasets = getDatasets(session, 'group', groupId);
%      datasets = getDatasets(session, ids);
%      datasets = getDatasets(session, ids, true);
%      datasets = getDatasets(session, ids, 'owner', ownerId);
%      datasets = getDatasets(session, ids, 'group', groupId);
%
% See also: GETOBJECTS, GETPROJECTS, GETIMAGES


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

% Check input
ip = inputParser;
ip.addOptional('ids', [], @(x) isempty(x) || (isvector(x) && isnumeric(x)));
ip.addOptional('loaded', false, @islogical);
ip.KeepUnmatched = true;
ip.parse(varargin{:});

parameters = omero.sys.ParametersI();
% Load the images attached to the datasets if loaded is True
if ip.Results.loaded, parameters.leaves(); end

% Delegate unmatched arguments check to getObjects function
unmatchedArgs =[fieldnames(ip.Unmatched)'; struct2cell(ip.Unmatched)'];
datasets = getObjects(session, 'dataset', ip.Results.ids, parameters,...
    unmatchedArgs{:});