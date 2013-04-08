function thumbnails = getThumbnailSet(session, images, varargin)
% GETTHUMBNAILSET Retrieve a set of thumbnails from images on the OMERO server
%
%   thumbnails = getThumbnailSet(session, images) returns a set of
%   thumbnails from a series of input images.
%
%   thumbnails = getThumbnailSet(session, images, width, height) also sets
%   the width and the height of the thumbnails.
%
%   thumbnails = getThumbnailSet(session, imageIDs) returns a set of
%   thumbnails from a series of input image identifiers.
%
%   thumbnails = getThumbnailSet(session, imageIDs,  width, height) also
%   sets the width and the height of the thumbnails.
%
%   Examples:
%
%      thumbnails = getThumbnailSet(session, images);
%      thumbnails = getThumbnailSet(session, images, width, height);
%      thumbnails = getThumbnailSet(session, imageIDs);
%      thumbnails = getThumbnailSet(session, imageIDs, width, height);
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
isValidThumbnailSize = @(x) isscalar(x) && x > 2 && round(x) == x;
ip = inputParser();
ip.addRequired('images', @(x) isa(x, 'omero.model.ImageI[]') || isvector(x));
ip.addOptional('width', [], isValidThumbnailSize);
ip.addOptional('height', [], isValidThumbnailSize);
ip.parse(images, varargin{:});

% Format input thumbnail dimensions
width = ip.Results.width;
height = ip.Results.height;
if ~isempty(width), width = rint(width); end
if ~isempty(height), height = rint(height); end

% Get the pixels from the image
if ~isa(images, 'omero.model.ImageI[]'),
    images = getImages(session, ip.Results.images);
    assert(~isempty(images), 'No image found with ID: %u', ip.Results.images);
end

% Create container service to load thumbnails
store = session.createThumbnailStore();

% Retrieve thumbnail set
pixelsIds = arrayfun(@(x) x.getPrimaryPixels().getId().getValue(), images);
pixelsIds = toJavaList(pixelsIds, 'java.lang.Long');
thumbnailMap = store.getThumbnailSet(width, height, pixelsIds);
store.close();

% Fill cell array with thumbnails
iterator = thumbnailMap.values().iterator();
thumbnails = cell(1, thumbnailMap.size);
i = 0;
while iterator.hasNext()
    i = i + 1;
    % Convert byteArray into Matlab image
    stream = java.io.ByteArrayInputStream(iterator.next());
    image = javax.imageio.ImageIO.read(stream);
    stream.close();
    thumbnails{i} = JavaImageToMatlab(image);
end