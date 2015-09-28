function [objects, orphans] = getObjects(session, type, ids, varargin)
% GETOBJECTS Retrieve objects from a given type from the OMERO server
%
%   objects = getObjects(session, type, ids) returns all the objects of the
%   specified type, identified by the input ids.
%   If ids is an empty array, only the objects belonging to the session
%   owner in the context of the current group are returned. If ids is non
%   empty, all objects are returned independently of the owner or the group.
%
%   objects = getObjects(session, type, ids, parameters) returns all the
%   objects of the specified type, identified by the input ids, owned by
%   the session user in the context of the session group using the supplied
%   loading parameters.
%
%   objects = getObjects(..., 'owner', owner) specifies the owner of the
%   objects. A value of -1 implies objects are returned independently of
%   the owner.
%
%   objects = getObjects(..., 'group', groupId) specifies the group
%   context for the objects. A value of -1 means objects are returned
%   across groups.
%
%   [objects, orphans] = getObjects(session, type, [],...) returns all the
%   orphans in addition to all the queried objects.
%
%   Examples:
%
%      % Retrieve objects by type and ids
%      objects = getObjects(session, type, ids);
%      % Retrieve objects with a custom set of parameters
%      objects = getObjects(session, type, ids, parameters);
%      % Retrieve objects owned by a given user
%      objects = getObjects(session, type, ids, 'owner', ownerId);
%      % Retrieve objects owned by the session user across all groups
%      objects = getObjects(session, type, ids, 'group', groupId);
%      objects = getObjects(session, type, ids, parameters, 'owner', ownerId);
%      [objects, orphans] = getObjects(session, type, [])
%
% See also: GETOBJECTTYPES

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
defaultParameters = omero.sys.ParametersI();
defaultContext = java.util.HashMap;
if isempty(ids),
    % If no input id, return the objects owned by the session user in the
    % current context
    defaultOwner = session.getAdminService().getEventContext().userId;
else
    % If ids are specified, return the objects owned by any user across
    % groups
    defaultOwner = -1;
    defaultContext.put('omero.group', '-1');
end
ip = inputParser;
ip.addOptional('parameters', defaultParameters, @(x) isa(x, 'omero.sys.ParametersI'));
ip.addParamValue('owner', defaultOwner, @(x) isscalar(x) && isnumeric(x));
ip.addParamValue('context', defaultContext, @(x) isa(x, 'java.util.HashMap'));
ip.addParamValue('group', [], @(x) isscalar(x) && isnumeric(x));
ip.parse(varargin{:});

% Use getImages function if retrieving images
if strcmp(type, 'image'),
    objects = getImages(session, ids, varargin{:});
    return
end

% Add the owner to the loading parameters
parameters = ip.Results.parameters;
if ~isempty(ip.Results.owner)
    parameters.exp(rlong(ip.Results.owner));
end

% Create list of objects to load
ids = toJavaList(ids, 'java.lang.Long');

% Create container service to load objects
proxy = session.getContainerService();
context = ip.Results.context;
if ~isempty(ip.Results.group)
    context.put('omero.group', num2str(ip.Results.group));
end
objectList = proxy.loadContainerHierarchy(objectType.class, ids, parameters, context);

% If orphans are loaded split the lists into two: objects and orphans
orphanList = java.util.ArrayList();
if ~isempty(parameters.getOrphan()) && parameters.getOrphan().getValue()
    for i = objectList.size() - 1 : -1  : 0,
        if ~isa(objectList.get(i), objectType.class);
            orphanList.add(objectList.get(i));
            objectList.remove(i);
        end
    end
end

% Convert java.util.ArrayList into Matlab arrays
objects = toMatlabList(objectList);
orphans = toMatlabList(orphanList);
