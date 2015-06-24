function d = createObjectDictionary(names, classnames)
% CREATEOBJECTDICTIONARY Return a dictionary of OMERO object types
%
%   d = createObjectDictionary(names, classnames) returns a dictionary of
%   OMERO objects organized as an array of structures with four fields:
%   name, class, Iobject and delete.
%
% See also: GETOBJECTTYPES, GETANNOTATIONTYPES

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
ip.addRequired('names', @iscellstr);
nObjects = numel(names);
ip.addRequired('classnames', @(x) iscellstr(x) && numel(x) == nObjects);
ip.parse(names, classnames);

% Initialize structure array
d(1 : nObjects) = struct('name', '', 'class', '', 'Iobject', '', 'delete', '', 'delete2', '');
for  i = 1 : nObjects
    d(i).name = names{i};
    d(i).class = ['omero.model.' classnames{i}];
    d(i).Iobject = str2func([d(i).class 'I']);
    d(i).delete = ['/' classnames{i}];%@deprecated
    d(i).delete2 = classnames{i};
end