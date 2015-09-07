function newobject = createObject(session, type, name, varargin)
% CREATEOBJECT Create a new object of input type and uploads it onto the OMERO server
%
%   newobject = createObject(session, type, name) create a new object of
%   input type with the input name, uploads it onto the server and returns
%   the loaded object.
%
%   newobject = createObject(..., 'group', groupId) specifies the group
%   context in which the object should be created.
%
%   Examples:
%
%      % Create an image object in the context of the session group
%      image = createObject(session, 'image', 'name');
%      % Create an image object in the specified group
%      image = createObject(session, 'image', 'name', 'group', groupId);
%
% See also: CREATEIMAGE, CREATEPROJECT, CREATEDATASET, CREATEPLATE,
% CREATESCREEN

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

% Input check
objectTypes = getObjectTypes();
objectNames = {objectTypes.name};
ip = inputParser;
ip.addRequired('session');
ip.addRequired('type', @(x) ischar(x) && ismember(x, objectNames));
ip.addRequired('name', @ischar);
ip.addParamValue('context', java.util.HashMap, @(x) isa(x, 'java.util.HashMap'));
ip.addParamValue('group', [], @(x) isempty(x) || (isscalar(x) && isnumeric(x)));
ip.parse(session, type, name, varargin{:});

% Create new object and upload onto the server
context = ip.Results.context;
if ~isempty(ip.Results.group)
    context.put('omero.group', java.lang.String(num2str(ip.Results.group)));
end

newobject = objectTypes(strcmp(type, objectNames)).Iobject();
newobject.setName(rstring(ip.Results.name));
newobject = session.getUpdateService().saveAndReturnObject(newobject, context);

end
