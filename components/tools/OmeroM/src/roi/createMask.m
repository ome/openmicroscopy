function mask = createMask(x, y, m)
% CREATEMASK Create a Mask shape object from input mask
%
%   Examples:
%
%      mask = createMask(ones(20, 30));
%      mask = createMask(10, 10, ones(20, 30));
%
% See also: SETSHAPECOORDINATES

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

if nargin == 1,
    mask = createMask(0, 0, x);
    return
end

% Check input
isvalidmaskinput = @(x) (isnumeric(x) || islogical(x)) && numel(x) > 0;
ip = inputParser;
ip.addRequired('x', @isscalar);
ip.addRequired('y', @isscalar);
ip.addRequired('m', isvalidmaskinput);
ip.parse(x, y, m);

% Create Mask shape
mask = omero.model.MaskI;
mask.setX(rdouble(x));
mask.setY(rdouble(y));
mask.setWidth(rdouble(size(m, 2)));
mask.setHeight(rdouble(size(m, 1)));

% Convert to array of bits
binary_vector = double(reshape(m', [], 1));
if mod(length(binary_vector), 8) ~= 0,
    binary_vector(end + 1 : end + 8 - mod(length(binary_vector), 8)) = 0;
end
byte_vector = reshape(binary_vector, 8, [])' * (2.^(7:-1:0))';
x_bytes = uint8(byte_vector);

mask.setBytes(x_bytes(:));
