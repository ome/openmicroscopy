function getFileAnnotationContent(session, fileAnnotation, path)
% GETFILEANNOTATIONCONTENT Reads the file content of a file annotation
%
%    getFileAnnotationContent(session, fileAnnotation, path) reads the file
%    content of the input file annotation and saves it in the files
%    specified by the input path.
%
%    Examples:
%
%        getFileAnnotationContent(session, fileAnnotation, path)
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
ip = inputParser;
ip.addRequired('fileAnnotation', @(x) isa(x, 'omero.model.FileAnnotationI'));
ip.addRequired('path', @ischar);
ip.parse(fileAnnotation, path);

% Initialize raw file store
store = session.createRawFileStore();

% Set file annotation id
file = fileAnnotation.getFile();
store.setFileId(file.getId().getValue());

% Read data and cast into int8
fid = fopen(path, 'w');
fwrite(fid, store.read(0, file.getSize().getValue()), 'int8');
fclose(fid);

% Close the file store
store.close()