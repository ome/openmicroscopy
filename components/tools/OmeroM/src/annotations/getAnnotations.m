function annotations = getAnnotations(session, ids, type)
% GETANNOTATIONS Retrieve annotations from a given type from the OMERO server
%
%   annotations = getAnnotations(session, ids, type) returns all the 
%   annotations identified by the input ids of the specified type.
%
%   Examples:
%
%      annotations = getAnnotations(session, ids, type);
%
% See also: GETANNOTATIONTYPES, GETDOUBLEANNOTATIONS, GETCOMMENTANNOTATIONS,
% GETFILEANNOTATIONS, GETLONGANNOTATIONS, GETTAGANNOTATIONS,
% GETTIMESTAMPANNOTATIONS, GETXMLANNOTATIONS

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

% Check input
annotationTypes = getAnnotationTypes();
annotationNames = {annotationTypes.name};
ip = inputParser;
ip.addRequired('session');
ip.addRequired('ids', @(x) isvector(x) || isempty(x));
ip.addRequired('type', @(x) ischar(x) && ismember(x, annotationNames));
ip.parse(session, ids, type);
annotationType = annotationTypes(strcmp(type, annotationNames));

% Create list of annotations identifiers to load
ids = toJavaList(ip.Results.ids, 'java.lang.Long');

% Create container service to load annotations
service = session.getMetadataService();
annotationList = service.loadAnnotation(ids);

% Filter annotation list by annotation class
for i = annotationList.size - 1 : -1 : 0
    if ~isa(annotationList.get(i), annotationType.class)
        annotationList.remove(i);
    end
end

% Convert java.util.ArrayList into Matlab arrays
annotations = toMatlabList(annotationList);