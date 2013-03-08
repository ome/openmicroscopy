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
%   Examples:
%
%      images = getImages(session);
%      images = getImages(session, ids);
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
ip = inputParser;
ip.addOptional('ids', [], @(x) isempty(x) || isvector(x));
ip.parse(varargin{:});

% Add the current user id to the loading parameters
parameters = omero.sys.ParametersI();
userId = session.getAdminService().getEventContext().userId;
parameters.exp(rlong(userId));

% Create container service to load objects
proxy = session.getContainerService();
if ~isempty(ip.Results.ids),
    ids = toJavaList(ip.Results.ids, 'java.lang.Long');
    imageList = proxy.getImages('omero.model.Image', ids, parameters);
else
    imageList = proxy.getUserImages(parameters);
end

% Convert java.util.ArrayList into Matlab arrays
images = toMatlabList(imageList);