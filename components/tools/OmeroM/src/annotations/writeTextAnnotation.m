function ann = writeTextAnnotation(session, type, text, varargin)
% WRITETEXTANNOTATION Create and upload a text annotation onto OMERO
%
%    ann = writeTextAnnotation(session, type, text) creates and uploads an
%    annotation of input type and input test owned by the session user.
%
%    ann = writeTextAnnotation(session, type, text, 'description',
%    description) also set the description of the annotation.
%
%    ann = writeTextAnnotation(session, type, text, 'namespace', namespace)
%    also sets the namespace of the annotation.
%
%    ann = writeTextAnnotation(session, type, text, 'group', groupid)
%    sets the group.
%
%    Examples:
%
%        ann = writeTextAnnotation(session, type, text)
%        ann = writeTextAnnotation(session, type, text, 'description',
%        description)
%        ann = writeTextAnnotation(session, type, text, 'namespace',
%        namespace)
%
% See also: WRITECOMMENTANNOTATION, WRITEDOUBLEANNOTATION,
% WRITEFILEANNOTATION, WRITETAGANNOTATION, WRITETIMESTAMPANNOTATION,
% WRITEXMLANNOTATION

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
annotationTypes = getAnnotationTypes();
annotationNames = {annotationTypes.name};
ip = inputParser;
ip.addRequired('session');
ip.addRequired('type', @(x) ischar(x) && ismember(x, annotationNames));
ip.addRequired('text', @ischar);
ip.addParamValue('description', '', @ischar);
ip.addParamValue('namespace', '', @ischar);
ip.addParamValue('group', [], @(x) isscalar(x) && isnumeric(x));
ip.parse(session, type, text, varargin{:});

% Create text annotation of input type
ann = annotationTypes(strcmp(type, annotationNames)).Iobject();

% Set annotation properties
ann.setTextValue(rstring(ip.Results.text));
if ~isempty(ip.Results.description),
    ann.setDescription(rstring(ip.Results.description));
end
if ~isempty(ip.Results.namespace),
    ann.setNs(rstring(ip.Results.namespace));
end

% Upload and return the annotation
context = java.util.HashMap;
if ~isempty(ip.Results.group)
    context.put(...
        'omero.group', java.lang.String(num2str(ip.Results.group)));
end
ann = session.getUpdateService().saveAndReturnObject(ann, context);
