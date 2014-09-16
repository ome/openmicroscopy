function tile = getTile(varargin)
% GETTILE Retrieve tile from an image on the OMERO server
%
%   tile = getTile(session, image, z, c, t, x, y, w, h) returns the tile
%   from input image at the input z, c, t coordinates located at (x, y) and
%   of dimensions [w, h].
%
%   tile = getTile(session, imageID, z, c, t, x, y, w, h) returns the tile
%   from input image identifier at the input z, c, t coordinates located at
%   (x, y) and of dimensions [w, h].
%
%   tile = getTile(pixels, store, z, c, t, x, y, w, h) returns the tile
%   from a pixels object and an initialized pixels store at the input z, c,
%   t coordinates located at (x, y) and of dimensions [w, h].
%
%
%   Examples:
%
%      tile = getTile(session, image, z, c, t, x, y, w, h);
%      tile = getTile(session, imageID, z, c, t, x, y, w, h);
%      tile = getTile(pixels, store, z, c, t, x, y, w, h);
%
% See also: GETRAWPIXELSSTORE, GETPLANE, GETSTACK

% Copyright (C) 2013-2014 University of Dundee & Open Microscopy Environment.
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

% Check number of arguments and retrieve store and pixels
narginchk(10, 10);
if ~isa(varargin{1}, 'omero.model.PixelsI')
    % Initialize raw pixels store
    [store, pixels] = getRawPixelsStore(varargin{1}, varargin{2});
else
    pixels = varargin{1};
    store = varargin{2};
    store.setPixelsId(pixels.getId().getValue(), false);
end

% Initialize raw pixels store
sizeX = pixels.getSizeX().getValue();
sizeY = pixels.getSizeY().getValue();
sizeZ = pixels.getSizeZ().getValue();
sizeC = pixels.getSizeC().getValue();
sizeT = pixels.getSizeT().getValue();

% Input check
ip = inputParser;
isposint = @(x) isnumeric(x) & x >= 0 & abs(round(x)) == x;
ip.addRequired('z', @(x) isposint(x) && x < sizeZ);
ip.addRequired('c', @(x) isposint(x) && x < sizeC);
ip.addRequired('t', @(x) isposint(x) && x < sizeT);
ip.addRequired('x', @(t) isposint(t) && t < sizeX);
ip.addRequired('y', @(t) isposint(t) && t < sizeY);
ip.addRequired('w', @(t) isposint(t) && (x + t) <= sizeX);
ip.addRequired('h', @(t) isposint(t) && (y + t) <= sizeY);
ip.parse(varargin{3:end});

% Read tile
tile = store.getTile(ip.Results.z, ip.Results.c, ip.Results.t,...
    ip.Results.x, ip.Results.y, ip.Results.w, ip.Results.h);
tile = toMatrix(tile, pixels, [w h])';


if ~isa(varargin{1}, 'omero.model.PixelsI')
    % Close the store if initialized from a session and image input
    store.close();
end
