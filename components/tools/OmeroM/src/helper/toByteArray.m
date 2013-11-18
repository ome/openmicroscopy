function byteArray = toByteArray(matrix, pixels)
% TOBYTESTREAM Format matrix into a byte stream to be uploaded on to an OMERO server
%
%   byteArray = toByteArray(matrix, pixels) converts an input matrix of
%   dimensions matching the size of the input pixels into a  byte array
%   that can be uploaded onto the OMERO server using the rawPixelsStore.
%   The input matrix dimensions must be [sizeX sizeY].
%
%   Examples:
%
%      byteArray = toMatrix(matrix, pixels);

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
ip.addRequired('matrix', @(x) isnumeric(x) && ismatrix(x));
ip.addRequired('pixels', @(x) isa(x, 'omero.model.PixelsI'));
ip.parse(matrix, pixels);

% Check input matrix dimensions
sizeX = pixels.getSizeX().getValue();
sizeY = pixels.getSizeY().getValue();
assert(isequal(size(matrix), [sizeX, sizeY]),...
    'OMERO:byteArray:sizeMismatch',...
    'Size of  input matrix  does not match the pixels dimensions');

% Check input matrix pixel type
type = char(pixels.getPixelsType().getValue().getValue());
if strcmp(type,'float'), type = 'single'; end
assert(isa(matrix, type),...
    'OMERO:byteArray:typeMismatch',...
    'Type of input matrix does not match the pixels type');

% Convert matrix into byte array
byteArray = reshape(matrix, sizeX * sizeY, 1 );
byteArray = swapbytes(byteArray);
byteArray = typecast(byteArray, 'int8');
