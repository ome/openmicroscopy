function fa = writeFileAnnotation(session, filePath, varargin)
% WRITEFILEANNOTATION Upload a file and create a file annotation onto OMERO
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
%    fa = writeFileAnnotation(session, filePath, 'group', groupid)
%    sets the group.
%
%    Examples:
%
%        fa = writeFileAnnotation(session, filePath)
%        fa = writeFileAnnotation(session, filePath, 'mimetype', mimetype)
%        fa = writeFileAnnotation(session, filePath, 'description',
%        description)
%
% See also: WRITECOMMENTANNOTATION, WRITEDOUBLEANNOTATION,
% WRITELONGANNOTATION, WRITETAGANNOTATION, WRITETEXTANNOTATION,
% WRITETIMESTAMPANNOTATION, WRITEXMLANNOTATION, UPDATEORIGINALFILE

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
ip = inputParser;
ip.addRequired('session');
ip.addRequired('filePath', @(x) exist(x, 'file') == 2);
ip.addParamValue('mimetype', '', @ischar);
ip.addParamValue('namespace', '', @ischar);
ip.addParamValue('description', '', @ischar);
ip.addParamValue('group', [], @(x) isscalar(x) && isnumeric(x));
ip.parse(session, filePath, varargin{:});

% Create original file
originalFile = omero.model.OriginalFileI;
originalFile.setHasher(omero.model.ChecksumAlgorithmI());
originalFile.getHasher.setValue(rstring('SHA1-160'));

if ~isempty(ip.Results.mimetype),
    originalFile.setMimetype(rstring(ip.Results.mimetype));
end

originalFile = updateOriginalFile(...
    session, originalFile, filePath, varargin{:});

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
context = java.util.HashMap;
if ~isempty(ip.Results.group)
    context.put(...
        'omero.group', java.lang.String(num2str(ip.Results.group)));
end
fa = session.getUpdateService().saveAndReturnObject(fa, context);
