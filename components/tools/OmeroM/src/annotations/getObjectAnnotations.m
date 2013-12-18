function annotations = getObjectAnnotations(session, annotationType, parentType, ids, varargin)
% GETOBJECTANNOTATIONS Retrieve annotations of a given type associated with an object
%
%    anns = getObjectAnnotations(session, annotationType, parentType, ids)
%    returns all annotations of type annotationType linked to the object of
%    type parentType and identifiers ids owned by the session user.
%
%    anns = getObjectAnnotations(session, annotationType, parentType,
%    parents) returns all annotations of type annotationType linked to the
%    input parent objects of type parentType owned by the session user.
%
%    anns = getObjectAnnotations(session, annotationType, parentType, ids,
%    'include', include) only returns annotations with the input namespace.
%
%    anns = getObjectAnnotations(session, annotationType, parentType, ids,
%    'exclude', exclude) excludes annotations with the input namespace.
%
%    Examples:
%
%        anns = getObjectAnnotations(session, annotationType, parentType,
%        ids)
%        anns = getObjectAnnotations(session, annotationType, parentType,
%        parents)
%        anns = getObjectAnnotations(session, annotationType, parentType,
%        ids, 'include', include)
%        anns = getObjectAnnotations(session, annotationType, parentType,
%        ids, 'exclude', exclude)
%
% See also: GETIMAGEFILEANNOTATIONS, GETIMAGETAGANNOTATIONS,
% GETIMAGECOMMENTANNOTATIONS

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
annotations = getAnnotationTypes();
objects = getObjectTypes();
ip = inputParser;
ip.addRequired('annotationType', @(x) ischar(x) && ismember(x, {annotations.name}));
ip.addRequired('parentType', @(x) ischar(x) && ismember(x, {objects.name}));
ip.addRequired('ids', @(x) isvector(x) || isempty(x));
ip.addParamValue('include', [], @(x) iscellstr(x) || ischar(x));
ip.addParamValue('exclude', [], @(x) iscellstr(x) || ischar(x));
ip.parse(annotationType, parentType, ids, varargin{:});

% Load existing file annotations
metadataService = session.getMetadataService();

% Convert input into java.util.ArrayList;
if ~isnumeric(ids),
    ids = arrayfun(@(x) x.getId().getValue(), ids);
end
ids = toJavaList(ids, 'java.lang.Long');
include = toJavaList(ip.Results.include, 'java.lang.String');
exclude = toJavaList(ip.Results.exclude, 'java.lang.String');

% Load the annotations of the session owner
userId = session.getAdminService().getEventContext().userId;
parameters = omero.sys.ParametersI;
parameters.exp(rlong(userId));

% Read annotations
object = objects(strcmp(parentType, {objects.name}));
annotation = annotations(strcmp(annotationType, {annotations.name}));
annotations = metadataService.loadSpecifiedAnnotationsLinkedTo(...
    annotation.class, include, exclude, object.class, ids, parameters);

% Aggregate all annotations into a java.util.ArrayList
annotationList = java.util.ArrayList();
i = annotations.values.iterator;
while (i.hasNext())
    j = i.next().iterator();
    while (j.hasNext())
        annotationList.add(j.next());
    end
end

% Convert java.util.ArrayList into a Matlab array
annotations = toMatlabList(annotationList);