function fileAnnotation = updateFileAnnotation(session, fileAnnotation, filePath, varargin)
% UPDATEFILEANNOTATION Update a file annotation on the OMERO server with new content
%
%    fileAnnotation = updateFileAnnotation(session, fileAnnotation,
%    filePath) updates the file content of a file annotation using the
%    content of the file on disk specified by filePath.
%
%    fileAnnotation = updateFileAnnotation(session, fileAnnotation,
%    filePath, 'description', description) also updates the description of
%    the file annotation.
%
%    fileAnnotation = updateFileAnnotation(session, fileAnnotation,
%    filePath, 'namespace', namespace) also updates the namespace of
%    the file annotation.
%
%    Examples:
%
%        updateFileAnnotation(session, fileAnnotation, filePath)
%
%        fileAnnotation = updateFileAnnotation(session, fileAnnotation,
%        filePath, 'description', 'new decription');
%
%        fileAnnotation = updateFileAnnotation(session, fileAnnotation,
%        filePath, 'namespace', 'new.namespace');
%
% See also: writeFileAnnotation, updateOriginalFile

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
ip.addRequired('session');
ip.addRequired('fileAnnotation', @(x) isa(x, 'omero.model.FileAnnotationI'));
ip.addRequired('filePath', @(x) exist(x, 'file') == 2);
ip.addParamValue('namespace', '', @ischar);
ip.addParamValue('description', '', @ischar);
ip.addParamValue('group', [], @(x) isscalar(x) && isnumeric(x));
ip.parse(session, fileAnnotation, filePath, varargin{:});

context = java.util.HashMap;
% Check if the Annotation exists on the server
try
    group = fileAnnotation.getDetails().getGroup().getId().getValue();
    context.put('omero.group', num2str(group));
catch
end
% In case the FileAnnotation does not yet exist on the server:
if ~context.containsKey('omero.group') && ~isempty(ip.Results.group)
    context.put(...
        'omero.group', java.lang.String(num2str(ip.Results.group)));
end

if ~isempty(ip.Results.description)
    % Update the description
    fileAnnotation.setDescription(rstring(ip.Results.description));
end

if ~isempty(ip.Results.namespace),
    % Update the namespace
    fileAnnotation.setNs(rstring(ip.Results.namespace))
end

if ~isempty(ip.Results.description) || ~isempty(ip.Results.namespace),
    % Save the file annotation
    fileAnnotation = session.getUpdateService().saveAndReturnObject(...
        fileAnnotation, context);
end

% Update the original file with the new content
updateOriginalFile(session, fileAnnotation.getFile(), filePath, varargin);
