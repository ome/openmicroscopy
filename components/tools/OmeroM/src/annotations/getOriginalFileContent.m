function getOriginalFileContent(session, originalFile, path)
% GETORIGINALFILECONTENT Reads the file content of an OriginalFile obtained
% from a file annotation
%
%    getOriginalFileContent(session, originalFile, path) reads the file
%    content of the input originalfile and saves it to the file
%    specified by the input path.
%
%
%
% See also: GETFILEANNOTATIONCONTENT

% Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
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

if ~isa(originalFile, 'omero.model.OriginalFileI'),
    assert(isLoadedFA(fileAnnotation),...
       'Not an original file:');
end

context = java.util.HashMap;
context.put('omero.group', '-1');

% Initialize raw file store
store = session.createRawFileStore();

% Set file annotation id
store.setFileId(originalFile.getId().getValue(), context);

% Read data and cast into int8
fid = fopen(path, 'w');
byteArr  = store.read(0,originalFile.getSize().getValue());
fwrite(fid,byteArr,'*uint8');
fclose(fid);

% Close the file store
store.close();