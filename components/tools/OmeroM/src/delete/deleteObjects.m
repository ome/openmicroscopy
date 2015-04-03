function deleteObjects(session, ids, type)
% DELETEOBJECTS Delete objects of a given type from the OMERO server
%
%   objects = deleteObjects(session, ids, type) returns all the objects of
%   the specified type, identified by the input ids. All annotations (tags,
%   files...) linked to the objects will either be deleted if not shared
%   with other objects or unlinked if shared with other objects.
%
%
%   Examples:
%
%      objects = deleteObjects(session, ids, type);
%
% See also: DELETEPROJECTS, DELETEDATASETS, DELETEIMAGES, DELETESCREENS,
% DELETEPLATES

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
ip.addRequired('ids', @isvector);
ip.addRequired('type', @(x) ischar(x) && ismember(x, objectNames));
ip.parse(session, ids, type);
objectType = objectTypes(strcmp(type, objectNames));

% Create a list of delete commands
idlist=java.util.ArrayList();
for i = 1 : numel(ids)
    idlist.add(java.lang.Long(ids(i)));
end
targetObj = java.util.Hashtable;
targetObj.put(objectType.delete,idlist);
deleteCommands = omero.cmd.Delete2(targetObj, [], false);

% Submit the delete commands
session.submit(deleteCommands);