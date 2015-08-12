function originalFile = updateOriginalFile(...
    session, originalFile, filePath, varargin)
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
% See also: writeFileAnnotation, updateFileAnnotation

% Copyright (C) 2013-2014 University of Dundee & Open Microscopy Environment.
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
ip.addParamValue('group', [], @(x) isscalar(x) && isnumeric(x));
ip.KeepUnmatched = true;
ip.parse(session, originalFile, filePath, varargin{:});

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
context = java.util.HashMap;
% This method is used to update OriginalFiles that may or may yet NOT exist
% on the server, hence:
% Check if the Annotation exists on the server
try
    group = fileAnnotation.getDetails().getGroup().getId().getValue();
    context.put('omero.group', num2str(group));
catch
end
% If exists only client side accept omero.group parameter
if ~context.containsKey('omero.group') && ~isempty(ip.Results.group)
    context.put(...
        'omero.group', java.lang.String(num2str(ip.Results.group)));
end
updateService = session.getUpdateService();
originalFile = updateService.saveAndReturnObject(originalFile, context);

% Initialize provider to compute client-side checksum
checksumProviderFactory = ome.util.checksum.ChecksumProviderFactoryImpl;
sha1= ome.util.checksum.ChecksumType.SHA1;
hasher = checksumProviderFactory.getProvider(sha1);

% Initialize the service to load the raw data
rawFileStore = session.createRawFileStore();
rawFileStore.setFileId(originalFile.getId().getValue(), context);

%code for large files as well.
lengthvec=262144;
if fileLength<=lengthvec
    lengthvec=fileLength;
end

fileLength1=(1:lengthvec:fileLength);
for i=1:length(fileLength1)
    
    
    filestart1=fileLength1(i);
    if i==length(fileLength1)
        filestop1=fileLength;
    else
        filestop1=fileLength1(i+1)-1;
    end
    
    fid = fopen(f.Name);
    fseek(fid,filestart1-1,'bof');
    
    byteArray = fread(fid,[1, length(filestart1:filestop1)], 'uint8');%include skip bytes in every loop
    rawFileStore.write(byteArray, (filestart1-1), length(byteArray));
    fclose(fid);
    hasher.putBytes(byteArray, 0, length(byteArray));
    
    
end

% Truncate the file to fileLength in case a smaller file is uploaded
rawFileStore.truncate(fileLength);

% Save and close the service
originalFile = rawFileStore.save(context);
rawFileStore.close();

% Compare checksums of file client-side verus server-side
clientHash = char(hasher.checksumAsString());
serverHash = char(originalFile.getHash().getValue());
msg = 'File checksum mismatch on upload: %s (client has %s, server has %s)';
assert(isequal(clientHash, serverHash), msg, filePath, clientHash, serverHash);
