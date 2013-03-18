function [mat] = toMatrix(binaryData, pixels,varargin)
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

% Parse optional size dimension
ip = inputParser;
sizeX = pixels.getSizeX().getValue();
sizeY = pixels.getSizeY().getValue();
sizeZ = pixels.getSizeZ().getValue();
defaultSize = [sizeX, sizeY, sizeZ];
sizeValidator = @(x) isvector(x) && all(round(x)==x) && all(x <= defaultSize(1:numel(x)));
ip.addOptional('size', defaultSize, sizeValidator);
ip.parse(varargin{:});

% Cast the binary data into the appropriate format
type = char(pixels.getPixelsType().getValue().getValue());
if strcmp(type,'float'), type = 'single'; end
a = typecast(binaryData, type);

% Check binary and pixels dimensions
nElements = numel(a);
nProd = cumprod(ip.Results.size);
assert(ismember(nElements, nProd), ...
    'OMERO:toMatrix:dimSize',...
    'Length of binary data does not match the input dimensions');

% Reshape linear binary data
iSize = find(nElements == nProd, 1);
b = reshape(a, ip.Results.size(1:iSize));

mat = swapbytes(b);