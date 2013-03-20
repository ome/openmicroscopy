function [mat] = toMatrix(binaryData, pixels, varargin)
% TOMATRIX Format binary data retrieved from the OMERO server into a matrix.
%
%   mat = toMatrix(binaryData, pixels) converts the linear binary data
%   obtained from the input pixels (using the raw pixels store) into a
%   matrix. The input data can be a plane or a stack and its dimensions are
%   checked against the pixels dimensions. The output matrix dimensions are
%   either [sizeX sizeY] or [sizeX sizeY sizeZ].
%
%   mat = toMatrix(binaryData, pixels, size) converts the linear binary
%   data obtained from the input pixels (using the raw pixels store) into a
%   matrix of input size. This allows to format tiles or hypercubes
%   retrieved from the server into matrices.
%
%   Examples:
%
%      mat = toMatrix(binaryData, pixels);
%      mat = toMatrix(binaryData, pixels, size);;

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
assert(ismember(nElements, nProd), 'OMERO:toMatrix:sizeMismatch',...
    'Length of binary data does not match the input dimensions');

if nElements > 1,
    % Reshape linear binary data
    iSize = find(nElements == nProd, 1);
    a = reshape(a, ip.Results.size(1:iSize));
end
mat = swapbytes(a);