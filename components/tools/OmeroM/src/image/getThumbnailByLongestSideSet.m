function thumbnails = getThumbnailByLongestSideSet(session, images, varargin)
% GETTHUMBNAILBYLONGESTSIDESET Retrieve a set of thumbnails from images on the OMERO server
%
%   thumbnails = getThumbnailByLongestSideSet(session, images) returns a
%   set of thumbnails from a series of input images.
%
%   thumbnails = getThumbnailByLongestSideSet(session, images, size) also
%   sets the size of of the longest side..
%
%   thumbnails = getThumbnailByLongestSideSet(session, imageIDs) returns a
%   set of thumbnails from a series of input image identifiers.
%
%   thumbnails = getThumbnailByLongestSideSet(session, imageIDs,  size)
%   also sets the  size of of the longest side.
%
%   Examples:
%
%      thumbnails = getThumbnailByLongestSideSet(session, images);
%      thumbnails = getThumbnailByLongestSideSet(session, images, size);
%      thumbnails = getThumbnailByLongestSideSet(session, imageIDs);
%      thumbnails = getThumbnailByLongestSideSet(session, imageIDs, size);
%
% See also: GETTHUMBNAIL, GETTHUMBNAILBYLONGESTSIDE, GETTHUMBNAILSET

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
ip.addOptional('size', [], isValidThumbnailSize);
ip.parse(images, varargin{:});

% Format input thumbnail size
size = ip.Results.size;
if ~isempty(size), size = rint(size); end

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
thumbnailMap = store.getThumbnailByLongestSideSet(size, pixelsIds);
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