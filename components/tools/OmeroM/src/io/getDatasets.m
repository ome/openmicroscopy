function datasets = getDatasets(session, varargin)
% GETDATASETS Retrieve dataset objects from the OMERO server
%
%   datasets = getDatasets(session) returns all the datasets owned by the
%   session user in the context of the session group.
%
%   datasets = getDatasets(session, ids) returns all the datasets
%   identified by the input ids owned by the session user in the context of
%   the session group.
%
%   datasets = getDatasets(session, ids, loaded) returns all the datasets
%   identified by the input ids owned by the session user in the context of
%   the session group. If loaded is True, the images attached to the
%   datasets are loaded.
%
%   By default, getDatasets() loads all the images attached to the datasets.
%   This may have consequences in terms of loading time depending on the
%   number of images in the datasets.
%
%   datasets = getDatasets(session, ids, 'owner', owner) returns all the
%   datasets identified by the input ids owned by the input user in the
%   context of the session group.
%
%   Examples:
%
%      datasets = getDatasets(session);
%      datasets = getDatasets(session, ids);
%      datasets = getDatasets(session, ids, false);
%      datasets = getDatasets(session, 'owner', ownerId);
%      datasets = getDatasets(session, ids, 'owner', ownerId);
%      datasets = getDatasets(session, ids, false, 'owner', ownerId);
%
% See also: GETOBJECTS, GETPROJECTS, GETIMAGES


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

% Check input
userId = session.getAdminService().getEventContext().userId;
ip = inputParser;
ip.addOptional('ids', [], @(x) isempty(x) || (isvector(x) && isnumeric(x)));
ip.addOptional('loaded', true, @islogical);
ip.addParamValue('owner', userId, @isscalar);
ip.parse(varargin{:});

parameters = omero.sys.ParametersI();
% Load the images attached to the datasets if loaded is True
if ip.Results.loaded, parameters.leaves(); end

datasets = getObjects(session, ip.Results.ids, 'dataset', parameters,...
    'owner', ip.Results.owner);