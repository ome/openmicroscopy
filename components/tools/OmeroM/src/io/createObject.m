function newobject = createObject(session, type, name)
% CREATEOBJECT Create a new object of input type and uploads it onto the OMERO server
%
%   newobject = createObject(session, type, name) create a new object of
%   input type with the input name, uploads it onto the server and returns
%   the loaded object.
%
%   Examples:
%
%      image = createObject(session, 'image', 'my-image')
%
% See also: CREATEIMAGE, CREATEPROJECT, CREATEDATASET

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
objectTypes = getObjectTypes();
objectNames = {objectTypes.name};
ip = inputParser;
ip.addRequired('session');
ip.addRequired('type', @(x) ischar(x) && ismember(x, objectNames));
ip.addRequired('name', @ischar);
ip.parse(session, type, name);

% Create new object and upload onto the server
newobject = objectTypes(strcmp(type, objectNames)).Iobject();
newobject.setName(rstring(ip.Results.name));
newobject = session.getUpdateService().saveAndReturnObject(newobject);

end
