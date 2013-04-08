function thumbnailSet = getThumbnailSet(session, images, varargin)
% GETTHUMBNAILSET Retrieve a set of thumbnails from images on the OMERO server
%
%   thumbnailSet = getThumbnailSet(session, images) returns a set of cache
%   thumbnails from a series of input images.
%
%   thumbnailSet = getThumbnailSet(session, images, width, height) also 
%   sets the width and the height of the thumbnails.
%
%   thumbnailSet = getThumbnailSet(session, imageIDs) returns a set of
%   cache thumbnails from a series of input image identifiers.
%
%   thumbnailSet = getThumbnailSet(session, imageIDs,  width, height) also
%   sets the width and the height of the thumbnails.
%
%   Examples:
%
%      thumbnailSet = getThumbnailSet(session, images);
%      thumbnailSet = getThumbnailSet(session, images, width, height);
%      thumbnailSet = getThumbnailSet(session, imageIDs);
%      thumbnailSet = getThumbnailSet(session, imageIDs, width, height);
%
% See also: GETTHUMBNAIL, GETTHUMBNAILBYLONGESTSIDE,
% GETTHUMBNAILBYLONGESTSIDESET

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
isValidImagesInput = @(x) isvector(x) &&...
    (isa(x(1), 'omero.model.ImageI') || isnumeric(x));
isValidThumbnailSize = @(x) isscalar(x) && x > 2 && round(x) == x;
ip = inputParser();
ip.addRequired('images', isValidImagesInput);
ip.addOptional('width', [], isValidThumbnailSize);
ip.addOptional('height', [], isValidThumbnailSize);
ip.parse(images, varargin{:});

% Format input thumbnail dimensions
width = ip.Results.width;
height = ip.Results.height;
if ~isempty(width), width = rint(width); end
if ~isempty(height), height = rint(height); end

% Get the pixels from the image
if isnumeric(images),
    images = getImages(session, ip.Results.images);
    assert(~isempty(images), 'No image found with ID: %u', ip.Results.images);
end

% Create container service to load thumbnails
store = session.createThumbnailStore();

% Retrieve thumbnail set
imageIds = arrayfun(@(x) x.getId().getValue(), images);
pixelsIds = arrayfun(@(x) x.getPrimaryPixels().getId().getValue(), images);
thumbnailMap = store.getThumbnailSet(width, height,...
    toJavaList(pixelsIds, 'java.lang.Long'));
store.close();

% Fill cell array with thumbnailst
nThumbnails = thumbnailMap.size;
iterator = thumbnailMap.entrySet().iterator();
thumbnailSet(1 : nThumbnails) = struct('image', [], 'pixels', [], 'thumbnail', []);

for i = 1 : nThumbnails
    % Read pairs and store pixels and image ID
    pairs = iterator.next();
    thumbnailSet(i).pixels = pairs.getKey();
    thumbnailSet(i).image = imageIds(pixelsIds == pairs.getKey());
    
    % Convert byteArray into Matlab image
    stream = java.io.ByteArrayInputStream(pairs.getValue());
    image = javax.imageio.ImageIO.read(stream);
    stream.close();
    thumbnailSet(i).thumbnail = JavaImageToMatlab(image);
end