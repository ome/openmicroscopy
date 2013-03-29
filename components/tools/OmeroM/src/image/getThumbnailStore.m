function [store, pixels] = getThumbnailStore(session, image)
% GETTHUMBNAILSTORE Initialize the thumbnail store service for an input image
%
%   [store, pixels] = getThumbnailStore(session, image) creates a thumbnail
%   store, retrieves the pixels from the input image and set the pixels 
%   identifier of the store.
%
%   [store, pixels] = getThumbnailStore(session, imageID) retrieve a single
%   image from the server, creates a thumbnail store, retrieves the pixels
%   from the input image and set the pixels identifier of the store.
%
%   Examples:
%
%      store = getThumbnailStore(session, image);
%      store = getThumbnailStore(session, imageID);
%
% See also: GETRAWPIXELSSTORE, GETTHUMBNAIL, GETTHUMBNAILBYLONGESTSIDE

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
ip.addRequired('image', @(x) isa(x, 'omero.model.ImageI') || isscalar(x));
ip.parse(image);

% Get the pixels from the image
if ~isa(image, 'omero.model.ImageI'),
    images = getImages(session, ip.Results.image);
    assert(numel(images) == 1, 'No image found with ID: %u', ip.Results.image);
    image = images(1);
end
pixels = image.getPrimaryPixels();

% Create container service to load thumbnails
store = session.createThumbnailStore();
store.setPixelsId(pixels.getId().getValue());