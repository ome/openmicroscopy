function fa = writeFileAnnotation(session, filePath, varargin)
% WRITEFILEANNOTATION Upload a file and create a file annotation on the OMERO server
%
%    fa = writeFileAnnotation(session, filePath) uploads the file specified
%    by filePath and creates a file annotation owned by the session user.
%
%    fa = writeFileAnnotation(session, filePath, 'mimetype', mimetype) also
%    specifies the mimetype of the file.
%
%    fa = writeFileAnnotation(session, filePath, 'namespace', namespace)
%    also sets the namespace of the file annotation.
%
%    fa = writeFileAnnotation(session, filePath, 'description',
%    description) also sets the description of the file annotation.
%
%    Examples:
%
%        fa = writeFileAnnotation(session, filePath)
%        fa = writeFileAnnotation(session, filePath, 'mimetype', mimetype)
%        fa = writeFileAnnotation(session, filePath, 'description',
%        description)
%
% See also: WRITETEXTANNOTATION

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
ip.addRequired('filePath', @(x) exist(x, 'file') == 2);
ip.addParamValue('mimetype', '', @ischar);
ip.addParamValue('namespace', '', @ischar);
ip.addParamValue('description', '', @ischar);
ip.parse(session, filePath, varargin{:});

% Create java io File
[~, f] = fileattrib(filePath);
[path, name, ext] = fileparts(f.Name);
fileLength = length(java.io.File(filePath));

% Create original file
originalFile = omero.model.OriginalFileI;
originalFile.setName(rstring([name ext]));
originalFile.setPath(rstring(path));
originalFile.setSize(rlong(fileLength));
originalFile.setHasher(omero.model.ChecksumAlgorithmI());
originalFile.getHasher.setValue(rstring('SHA1-160'));

if ~isempty(ip.Results.mimetype),
    originalFile.setMimetype(rstring(ip.Results.mimetype));
end

% now we save the originalFile object
updateService = session.getUpdateService();
originalFile = updateService.saveAndReturnObject(originalFile);

% Initialize the service to load the raw data
rawFileStore = session.createRawFileStore();
rawFileStore.setFileId(originalFile.getId().getValue());

% Initialize provider to compute client-side checksum
checksumProviderFactory = ome.util.checksum.ChecksumProviderFactoryImpl;
sha1= ome.util.checksum.ChecksumType.SHA1;
hasher = checksumProviderFactory.getProvider(sha1);

%code for large files as well.
lengthvec=262144;
if fileLength<=lengthvec
    lengthvec=fileLength;
end

fileLength1=(1:lengthvec:fileLength);
resvec=[];
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

% Save and close the service
originalFile = rawFileStore.save();
rawFileStore.close();

% Compare checksums client-side and server-sid
clientHash = char(hasher.checksumAsString());
serverHash = char(originalFile.getHash().getValue());
msg = 'File checksum mismatch on upload: %s (client has %s, server has %s)';
assert(isequal(clientHash, serverHash), msg, f.Name, clientHash, serverHash);

% Create a file annotation
fa = omero.model.FileAnnotationI;
fa.setFile(originalFile);

if ~isempty(ip.Results.description),
    fa.setDescription(rstring(ip.Results.description));
end

if ~isempty(ip.Results.namespace),
    fa.setNs(rstring(ip.Results.namespace))
end

% Save the file annotation
fa = updateService.saveAndReturnObject(fa);
