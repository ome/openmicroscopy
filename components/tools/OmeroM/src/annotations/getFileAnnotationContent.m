function getFileAnnotationContent(session, fileAnnotation, path)
% GETFILEANNOTATIONCONTENT Reads the file content of a file annotation
%
%    getFileAnnotationContent(session, fileAnnotation, path) reads the file
%    content of the input file annotation and saves it to the file
%    specified by the input path.
%
%    getFileAnnotationContent(session, faid, path) reads the file content
%    of the file annotation specified by the input identifier and saves it
%    to the file specified by the input path.
%
%    Examples:
%
%        getFileAnnotationContent(session, fileAnnotation, path)
%        getFileAnnotationContent(session, faid, path)
%
% See also: GETFILEANNOTATIONS

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
isLoadedFA = @(x) isa(x, 'omero.model.FileAnnotationI') && x.isLoaded();
ip = inputParser;
ip.addRequired('fileAnnotation', @(x) isLoadedFA(x) || isscalar(x));
ip.addRequired('path', @ischar);
ip.addParamValue('group', [], @(x) isscalar(x) && isnumeric(x));
ip.parse(fileAnnotation, path);

context = java.util.HashMap;
context.put('omero.group', '-1');

if ~isa(fileAnnotation, 'omero.model.FileAnnotationI'),
    % Load the file annotation from the server
    faID = ip.Results.fileAnnotation;
    fileAnnotation = getFileAnnotations(session, faID);
    assert(isLoadedFA(fileAnnotation),...
        'Could not load the file annotation: %u', faID);
end

% Initialize raw file store
store = session.createRawFileStore();

% Set file annotation id
file = fileAnnotation.getFile();
store.setFileId(file.getId().getValue(), context);

% Read data and cast into int8
fid = fopen(path, 'w');
fwrite(fid, store.read(0, file.getSize().getValue()), 'int8');
fclose(fid);

% Close the file store
store.close()