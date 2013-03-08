function objects = getObjects(session, type, ids, varargin)
% GETOBJECTS Retrieve objects from a given type from the OMERO server
%
%   objects = getObjects(session, type, ids) returns all the objects of the
%   specified type, identified by the input ids and owned by the session
%   user in the context of the session group.
%
%   objects = getObjects(session, type, ids, parameters) returns all the
%   objects of the specified type, identified by the input ids, owned by
%   the session user in the context of the session group using the supplied
%   loading parameters.
%
%   Examples:
%
%      objects = getObjects(session, type, ids);
%      objects = getObjects(session, type, ids, parameters);
%
% See also: GETOBJECTTYPES


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
objectTypes = getObjectTypes();
objectNames = {objectTypes.name};
ip = inputParser;
ip.addRequired('session');
ip.addRequired('type', @(x) ischar(x) && ismember(x, objectNames));
ip.addRequired('ids', @(x) isvector(x) || isempty(x));
ip.addOptional('parameters', omero.sys.ParametersI(),...
    @(x) isa(x, 'omero.sys.ParametersI'));
ip.parse(session, type, ids, varargin{:});
objectType = objectTypes(strcmp(type, objectNames));

% Use getImages function if retrieving images
if strcmp(type, 'image'),
    objects = getImages(session, ids);
    return
end

% Add the current user id to the loading parameters
parameters = ip.Results.parameters;
userId = session.getAdminService().getEventContext().userId;
parameters.exp(rlong(userId));

% Create list of objects to load
ids = toJavaList(ip.Results.ids, 'java.lang.Long');

% Create container service to load objects
proxy = session.getContainerService();
objectList = proxy.loadContainerHierarchy(objectType.class, ids, parameters);

% Convert java.util.ArrayList into Matlab arrays
objects = toMatlabList(objectList);