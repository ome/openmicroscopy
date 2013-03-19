function plane = getPlane(session, image, z, c, t)
% GETPLANE Retrieve plane from an image on the OMERO server
%
%   plane = getPlane(session, image, z, c, t) returns the plane from input
%   image at the input z, c, t coordinates.
%
%   plane = getPlane(session, imageID, z, c, t) returns the plane from
%   input image identifier at the input z, c, t coordinates.
%
%   Examples:
%
%      images = getPlane(session, image, z, c, t);
%      images = getPlane(session, imageID, z, c, t);
%
%
% See also: GETRAWPIXELSSTORE, GETSTACK, GETTILE

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
sizeZ = pixels.getSizeZ().getValue();
sizeC = pixels.getSizeC().getValue();
sizeT = pixels.getSizeT().getValue();

% Input check
ip = inputParser;
isposint = @(x) isnumeric(x) & x >= 0 & abs(round(x)) == x;
ip.addRequired('z', @(x) isposint(x) && x < sizeZ);
ip.addRequired('c', @(x) isposint(x) && x < sizeC);
ip.addRequired('t', @(x) isposint(x) && x < sizeT);
ip.parse(z, c, t);

% Read plane
plane = store.getPlane(z, c, t);
plane = toMatrix(plane, pixels)';

% Close the store
store.close();