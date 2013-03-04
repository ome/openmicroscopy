function objects = getObjects(session, type, ids)
% Retrieve objects from a given type

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
ip.parse(session, type, ids);
objectType = objectTypes(strcmp(type, objectNames));

% Create container service
proxy = session.getContainerService();

% Create parameters
parameters = omero.sys.ParametersI();

% Load the images if loading Projects or Datasets
if ismember(type, {'project', 'dataset'}), parameters.leaves(); end

% Read current user id from session
userId = session.getAdminService().getEventContext().userId;
parameters.exp(rlong(userId));

% Load objects
ids = toJavaList(ids, 'java.lang.Long');
if strcmp(type, 'image'),
    objectList = proxy.getImages(objectType.class, ids, parameters);
else
    objectList = proxy.loadContainerHierarchy(objectType.class, ids, parameters);
end

% Convert java.util.ArrayList into Matlab arrays
nObjects = objectList.size();
if nObjects >= 1
    % Initialize array
    objects(1 : nObjects) = objectType.Iobject();
    for i = 1 : nObjects
        objects(i) = objectList.get(i-1);
    end
else
    objects = [];
end