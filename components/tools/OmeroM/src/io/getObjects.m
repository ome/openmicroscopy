function objects = getObjects(session, type, ids, varargin)
% GETOBJECTS Retrieve objects from a given type from the OMERO server
%
%   objects = getObjects(session, type, ids) returns all the objects of the
%   specified type, identified by the input ids in the context of the
%   session group.
%
%   objects = getObjects(session, type, ids, parameters) returns all the
%   objects of the specified type, identified by the input ids, owned by
%   the session user in the context of the session group using the supplied
%   loading parameters.
%
%   If the input ids is an empty array, only the objects belonging to the
%   session user are returned else all the readable objects belonging to
%   any user are returned.
%
%   objects = getObjects(session, type, ids, 'owner', ownerId) returns all
%   the objects of the specified type, identified by the input ids, owned
%   by the input owner in the context of the session group.
%
%   Examples:
%
%      objects = getObjects(session, type, ids);
%      objects = getObjects(session, type, ids, parameters);
%      objects = getObjects(session, type, ids, 'owner', ownerId);
%      objects = getObjects(session, type, ids, parameters, 'owner', ownerId);
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

% Check required input parameters
objectTypes = getObjectTypes();
objectNames = {objectTypes.name};
ip = inputParser;
ip.addRequired('session');
ip.addRequired('type', @(x) ischar(x) && ismember(x, objectNames));
ip.addRequired('ids', @(x) isempty(x) || (isvector(x) && isnumeric(x)));
ip.parse(session, type, ids);
objectType = objectTypes(strcmp(type, objectNames));

% Throw exception if type is plate acquisition
assert(~strcmp(objectType.class, 'omero.model.PlateAcquisition'),...
    ['Plate acquisitions are loaded together with plates. '...
    'Use getPlates() instead.']);

% Check optional input parameters
ip = inputParser;
ip.addOptional('parameters', omero.sys.ParametersI(),...
    @(x) isa(x, 'omero.sys.ParametersI'));
if isempty(ids),
    % If no input id, return the objects owned by the session user by default
    defaultOwner = session.getAdminService().getEventContext().userId;
else
    % If ids are specified, return the objects owned by any user by default
    defaultOwner = -1;
end
ip.addParamValue('owner', defaultOwner, @(x) isscalar(x) && isnumeric(x));
ip.parse(varargin{:});

% Use getImages function if retrieving images
if strcmp(type, 'image'),
    objects = getImages(session, ids, varargin{:});
    return
end

% Add the owner to the loading parameters
parameters = ip.Results.parameters;
parameters.exp(rlong(ip.Results.owner));

% Create list of objects to load
ids = toJavaList(ids, 'java.lang.Long');

% Create container service to load objects
proxy = session.getContainerService();
objectList = proxy.loadContainerHierarchy(objectType.class, ids, parameters);

% Convert java.util.ArrayList into Matlab arrays
objects = toMatlabList(objectList);