function link = linkAnnotation(session, annotation, parentType, parentId)
% LINKANNOTATION Link an annotation to an object on the OMERO server
%
%    fa = linkAnnotation(session, annotation, parentType, parentId) creates
%    a link between the input annotation to the object of the input type
%    specified by the input identifier and owned by the session user.
%
%    Examples:
%
%        link = linkAnnotation(session, annotation, parentType, parentId)
%
% See also:

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
ip.addRequired('annotation', @isscalar);
ip.addRequired('parentType', @(x) ischar(x) && ismember(x, objectNames));
ip.addRequired('parentId', @(x) isempty(x) || (isvector(x) && isnumeric(x)));
ip.parse(session, annotation, parentType, parentId);
objectType = objectTypes(strcmp(parentType, objectNames));

% Get the parent object
if isnumeric(parentId),
    parent = getObjects(session, parentType, parentId);
    assert(~isempty(parent), 'No %s with id %g found', parentType, parentId);
else
    parent = parentId;
end

% Create object annotation link
context = java.util.HashMap;
group = parent.getDetails().getGroup().getId().getValue();
context.put('omero.group', num2str(group));

link = objectType.annotationLink();
link.setParent(parent)
link.setChild(annotation);
link = session.getUpdateService().saveAndReturnObject(link, context);
