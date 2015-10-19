function thumbnail = getThumbnailByLongestSide(session, image, varargin)
% GETTHUMBNAILBYLONGESTSIDE Retrieve cache thumbnail from an image on the OMERO server
%
%   thumbnail = getThumbnailByLongestSide(session, image) returns the cache
%   thumbnail for the input image where the aspect ratio of the original
%   image is preserved.
%
%   thumbnail = getThumbnailByLongestSide(session, image, size) also sets
%   the size of the longest side of the retrieved thumbnail.
%
%   thumbnail = getThumbnailByLongestSide(session, imageID) returns the
%   cache thumbnail for the input image identifier where the aspect ratio
%   of the original image is preserved.
%
%   thumbnail = getThumbnailByLongestSide(session, imageID, size) also sets
%   the size of the longest side of the retrieved thumbnail.
%
%   Examples:
%
%      thumbnail = getThumbnailByLongestSide(session, image);
%      thumbnail = getThumbnailByLongestSide(session, image, size);
%      thumbnail = getThumbnailByLongestSide(session, imageID);
%      thumbnail = getThumbnailByLongestSide(session, imageID, size);
%
% See also: GETTHUMBNAIL, GETTHUMBNAILSET, GETTHUMBNAILBYLONGESTSIDESET

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
isValidImageInput = @(x) isscalar(x) && ...
    (isa(x, 'omero.model.ImageI') || isnumeric(x));
isValidThumbnailSize = @(x) isscalar(x) && x > 2 && round(x) == x;
ip = inputParser();
ip.addRequired('image', isValidImageInput);
ip.addOptional('size', [], isValidThumbnailSize);
ip.parse(image, varargin{:});

% Format input thumbnail size
size = ip.Results.size;
if ~isempty(size), size = rint(size); end

% Get the image if image identifier is input
if isnumeric(image),
    image = getImages(session, ip.Results.image);
    assert(numel(image) == 1, 'No image found with ID: %u', ip.Results.image);
end

% Create store to retrieve thumbnails and set pixels Id
context = java.util.HashMap;
group = image.getDetails().getGroup().getId().getValue();
context.put('omero.group', java.lang.String(num2str(group)));
store = session.createThumbnailStore();
store.setPixelsId(image.getPrimaryPixels().getId().getValue(), context);

% Retrieve thumbnail set
byteArray = store.getThumbnailByLongestSide(size, context);
store.close();

% Convert byteArray into Matlab image
stream = java.io.ByteArrayInputStream(byteArray);
image = javax.imageio.ImageIO.read(stream);
stream.close();
thumbnail = JavaImageToMatlab(image);