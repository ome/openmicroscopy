function tile = getTile(session, image, z, c, t, x, y, w, h)
% GETTILE Retrieve tile from an image on the OMERO server
%
%   tile = getTile(session, image, z, c, t, x, y, w, h) returns the tile
%   from input image at the input z, c, t coordinates located at (x, y) and
%   of dimensions [w, h]
%
%   tile = getTile(session, image, z, c, t, x, y, w, h) returns the tile
%   from input image identifier at the input z, c, t coordinates located at
%   (x, y) and of dimensions [w, h]
%
%
%   Examples:
%
%      tile = getTile(session, image, z, c, t, x, y, w, h);
%      tile = getTile(session, image, z, c, t, x, y, w, h)
%
% See also: GETRAWPIXELSSTORE, GETPLANE, GETSTACK

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

% Initialize raw pixels store
[store, pixels] = getRawPixelsStore(session, image);

% Input check
ip = inputParser;
isposint = @(x) isnumeric(x) & x >= 0 & abs(round(x)) == x;
ip.addRequired('z', isposint);
ip.addRequired('c', isposint);
ip.addRequired('t', isposint);
ip.addRequired('x', isposint);
ip.addRequired('y', isposint);
ip.addRequired('w', isposint);
ip.addRequired('h', isposint);
ip.parse(z, c, t, x, y, w, h);

% Read tile
tile = store.getTile(z, c, t, x, y, w, h);
tile = toMatrix(tile, pixels, [w h])';