function channels = loadChannels(session, image)
% LOADCHANNELS Retrieve channels associated to an image on the OMERO server
%
%   channels = loadChannels(session, image) loads and returns the channels
%   associated to the input image
%
%   channels = loadChannels(session, imageID) loads and returns the channels
%   associated to the image specified by the input identifier
%
%   Examples:
%
%      channels = loadChannels(session, image);
%      channels = loadChannels(session, imageID);
%
%
% See also:

% Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
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
    image = getImages(session, ip.Results.image);
    assert(isscalar(image), 'No image found with ID: %u', ip.Results.image);
end
pixels = image.getPrimaryPixels();

% Retrieve channels
context = java.util.HashMap;
group = image.getDetails().getGroup().getId().getValue();
context.put('omero.group', num2str(group));
pixelsService = session.getPixelsService();
pixels = pixelsService.retrievePixDescription(...
    pixels.getId.getValue, context);
channels = toMatlabList(pixels.copyChannels);
