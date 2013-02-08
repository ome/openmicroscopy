function [mat] = toMatrix(binaryData, pixels)
% TOMATRIX convert the binaryData from the server to a matlab matrix.
%
% Input
%       binaryData: The data obtained using store.getPlane(...)
%
%       pixels: the pixels object of the image the plane came from.
%
% Output
%       mat: a matrix of size m x n or m x n x l
%            where m, n and l correspond to the x,y and z-dimensions of the
%            pixels
%

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
ip.addRequired('binaryData', @(x) isnumeric(x) && isvector(x));
ip.addRequired('pixels', @(x) isa(x, 'omero.model.PixelsI'));
ip.parse(binaryData, pixels);

% Cast the binary data into the appropriate format
type = char(pixels.getPixelsType().getValue().getValue());
if strcmp(type,'float'), type = 'single'; end
a = typecast(binaryData, type);

% Check binary and pixels dimensions
nElements = numel(a);
sizeX= pixels.getSizeX().getValue();
sizeY= pixels.getSizeY().getValue();
sizeZ= pixels.getSizeZ().getValue();
planeSize = sizeX * sizeY;
stackSize = sizeX * sizeY * sizeZ;
assert(ismember(nElements, [planeSize, stackSize]), ...
    'OMERO:toMatrix:dimSize',...
    'Length of binary data does not match the pixel dimensions');

% Reshape linear binary data
if nElements == planeSize
    b = reshape(a, sizeX, sizeY); % Convert a plane into a matrix
else
    b = reshape(a, sizeX, sizeY, sizeZ); % Convert a stack into a matrix
end

mat = swapbytes(b);