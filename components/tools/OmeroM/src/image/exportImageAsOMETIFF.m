function exportImageAsOMETIFF(session, image, imagePath)
% EXPORTIMAGEASOMETIFF downloads an OMERO image as an OME-TIFF file
%
%   exportImageAsOMETIFF(session, image, imagePath) exports the input image
%   as an OME-TIFF file.
%
%   exportImageAsOMETIFF(session, imageID, imagePath) exports the image
%   specified by the input identifier as an OME-TIFF file.
%
%   Examples:
%
%      exportImageAsOMETIFF(session, image, imagePath)
%      exportImageAsOMETIFF(session, imageID, imagePath)
%
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

INC = 1024 * 1024; % Maximum size that can be run at once

% Input check
ip = inputParser;
ip.addRequired('image', @(x) isa(x, 'omero.model.ImageI') || isscalar(x));
ip.addRequired('imagePath', @ischar);
ip.parse(image, imagePath);

% Get the pixels from the image
if isa(image, 'omero.model.ImageI'),
    imageID = image.getId().getValue;
else
    imageID = image;
end

% Create exporter service
store = session.createExporter;

% Set the image identifier
context = java.util.HashMap;
context.put('omero.group', '-1');
store.addImage(imageID);
size = store.generateTiff(context);

% Open image file in write access
fid = fopen(imagePath, 'w');
fprintf(1, 'Downloading image %d to %s', imageID, imagePath);

% Read data in increments of size INC
n = fix(size/INC);
for i = 1 : n
    fwrite(fid, store.read((i-1) * INC, INC), 'int8');
    if mod(i, 72) == 1, fprintf('\n    '); end
    fprintf('.');
end

% Read last block of data
if mod(size, INC) ~= 0
    fwrite(fid, store.read(n * INC, size - n* INC), 'int8');
end
fprintf('\n');
fclose(fid);

% Close the file store
store.close()

end