function [store, pixels] = getRawPixelsStore(session, image)
% GETRAWPIXELSSTORE Initialize the raw pixel store service for an input image
%
%   [store, pixels] = getRawPixelsStore(session, image) creates a raw
%   pixels store, retrieves the pixels from the input image and set the
%   pixels identifier of the store.
%
%   [store, pixels] = getRawPixelsStore(session, imageID) retrieve a single
%   image from the server, creates a raw pixels store, retrieves the pixels
%   from the input image and set the pixels identifier of the store.
%
%   Examples:
%
%      store = getRawPixelsStore(session, image);
%      store = getRawPixelsStore(session, imageID);
%
% See also: GETPLANE, GETSTACK, GETTILE

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

% Create container service to load raw pixels
context = java.util.HashMap;
group = image.getDetails().getGroup().getId().getValue();
context.put('omero.group', num2str(group));
store = session.createRawPixelsStore();
store.setPixelsId(pixels.getId().getValue(), false, context);