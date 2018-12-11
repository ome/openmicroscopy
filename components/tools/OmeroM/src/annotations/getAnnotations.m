function annotations = getAnnotations(session, ids, type, varargin)
% GETANNOTATIONS Retrieve annotations from a given type from the OMERO server
%
%   annotations = getAnnotations(session, ids, type) returns all the 
%   annotations identified by the input ids of the specified type.
%
%   Examples:
%
%      annotations = getAnnotations(session, ids, type);
%      annotations = getAnnotations(session, ids, type, 'group', groupId);
%        returns annotations from the input group specfied by groupId.
%
% INPUT ARGUMENTS
% session     an omero.api.ServiceFactoryPrxHelper Java object
%
% ids         a positive integer | a vector of positive integers | []
%             ID(s) of the annotations you want to retrieve.
%
% type        'comment' | 'double' | 'file' | 'long' | 'map' | 'tag' | ...
%             'timestamp' | 'xml'
%             Specifies the type of annotation
%             defined by getAnnotationTypes.m
%
%
% OPTIONAL PARAMETER/VALUE PAIRS
%
% 'group'     groupId
%             (Optional) ID of group.
%
% OUTPUT ARGUMENTS
% annotations Java objects of the following classes:
%             omero.model.CommentAnnotationI | omero.model.DoubleAnnotationI | 
%             omero.model.FileAnnotationI | omero.model.LongAnnotationI | 
%             omero.model.MapAnnotationI | omero.model.TagAnnotationI |
%             omero.model.TimestampAnnotationI | omero.model.XmlAnnotationI
%
% See also: GETANNOTATIONTYPES, GETDOUBLEANNOTATIONS, GETCOMMENTANNOTATIONS,
% GETFILEANNOTATIONS, GETLONGANNOTATIONS, GETTAGANNOTATIONS,
% GETTIMESTAMPANNOTATIONS, GETXMLANNOTATIONS, GETOBJECTANNOTATIONS

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
ip.addRequired('ids', @(x) isvector(x) || ~isempty(x));
ip.addRequired('type', @(x) ischar(x) && ismember(x, annotationNames));
ip.addParameter('group', [], @(x) isscalar(x) && isnumeric(x));
ip.parse(session, ids, type, varargin{:});

annotationType = annotationTypes(strcmp(type, annotationNames));

% Create list of annotations identifiers to load
ids = toJavaList(ip.Results.ids, 'java.lang.Long');

% Create container service to load annotations
context = java.util.HashMap;
if ~isempty(ip.Results.group)
    context.put(...
        'omero.group', java.lang.String(num2str(ip.Results.group)));
else
    context.put('omero.group', '-1');
end
service = session.getMetadataService();
annotationList = service.loadAnnotation(ids, context);

% Filter annotation list by annotation class
for i = annotationList.size - 1 : -1 : 0
    if ~isa(annotationList.get(i), annotationType.class)
        annotationList.remove(i);
    end
end

% Convert java.util.ArrayList into Matlab arrays
annotations = toMatlabList(annotationList);