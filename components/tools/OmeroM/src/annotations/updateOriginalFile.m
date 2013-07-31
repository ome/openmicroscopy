function originalFile = updateOriginalFile(session, originalFile, filePath)
% UPDATEORIGINALFILE Update a file on the OMERO server with new content
%
%    originalFile = updateOriginalFile(session, originalFile, filePath)
%    updates the content of the input original file using the content of
%    the file specified by filePath
%
%    Examples:
%
%        originalFile = updateOriginalFile(session, originalFile, filePath)
%        originalFile = updateOriginalFile(session, fa.getFile(), filePath)

%
% See also: createFileAnnotation

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
ip.addRequired('originalFile', @(x) isa(x, 'omero.model.OriginalFile'));
ip.addRequired('filePath', @(x) exist(x, 'file') == 2);
ip.parse(session, originalFile, filePath);

% Read file absolute path and properties
[~, f] = fileattrib(filePath);
absolutePath = f.Name;
[path, name, ext] = fileparts(absolutePath);
fileLength = length(java.io.File(absolutePath));

% Create original file
originalFile.setName(rstring([name ext]));
originalFile.setPath(rstring(path));
originalFile.setSize(rlong(fileLength));

% Update the originalFile object
updateService = session.getUpdateService();
originalFile = updateService.saveAndReturnObject(originalFile);

% Initialize provider to compute client-side checksum
checksumProviderFactory = ome.util.checksum.ChecksumProviderFactoryImpl;
sha1= ome.util.checksum.ChecksumType.SHA1;
hasher = checksumProviderFactory.getProvider(sha1);

% Initialize the service to load the raw data
rawFileStore = session.createRawFileStore();
rawFileStore.setFileId(originalFile.getId().getValue());

%code for small file.
fid = fopen(absolutePath);
byteArray = fread(fid,[1, fileLength], 'uint8');
rawFileStore.write(byteArray, 0, fileLength);
rawFileStore.truncate(fileLength);
hasher.putBytes(byteArray);
fclose(fid);

% Save and close the service
originalFile = rawFileStore.save();
rawFileStore.close();

% Compare checksums of file client-side verus server-side
clientHash = char(hasher.checksumAsString());
serverHash = char(originalFile.getHash().getValue());
msg = 'File checksum mismatch on upload: %s (client has %s, server has %s)';
assert(isequal(clientHash, serverHash), msg, filePath, clientHash, serverHash);
