function plane = getPlane(varargin)
% GETPLANE Retrieve plane from an image on the OMERO server
%
%   plane = getPlane(session, image, z, c, t) returns the plane from input
%   image at the input z, c, t coordinates.
%
%   plane = getPlane(session, imageID, z, c, t) returns the plane from
%   input image identifier at the input z, c, t coordinates.
%
%   plane = getPlane(pixels, store, z, c, t) returns the plane from a
%   pixels object and an initialized pixels store at the input z, c, t
%   coordinates.
%
%   Examples:
%
%      images = getPlane(session, image, z, c, t);
%      images = getPlane(session, imageID, z, c, t);
%      images = getPlane(pixels, store, z, c, t);
%
%
% See also: GETRAWPIXELSSTORE, GETSTACK, GETTILE

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
narginchk(5, 5);
if ~isa(varargin{1}, 'omero.model.PixelsI')
    % Initialize raw pixels store
    [store, pixels] = getRawPixelsStore(varargin{1}, varargin{2});
else
    pixels = varargin{1};
    store = varargin{2};
    store.setPixelsId(pixels.getId().getValue(), false);
end

% Input check for z, c, t coordinates
ip = inputParser;
isposint = @(x) isnumeric(x) & x >= 0 & abs(round(x)) == x;
sizeZ = pixels.getSizeZ().getValue();
sizeC = pixels.getSizeC().getValue();
sizeT = pixels.getSizeT().getValue();
ip.addRequired('z', @(x) isposint(x) && x < sizeZ);
ip.addRequired('c', @(x) isposint(x) && x < sizeC);
ip.addRequired('t', @(x) isposint(x) && x < sizeT);
ip.parse(varargin{3:end});

% Read plane
plane = store.getPlane(ip.Results.z, ip.Results.c, ip.Results.t);
plane = toMatrix(plane, pixels)';

if ~isa(varargin{1}, 'omero.model.PixelsI')
    % Close the store if initialized from a session and image input
    store.close();
end
