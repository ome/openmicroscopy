function rectangle = createRectangle(x, y, w, h)
% CREATERECTANGLE Create a rectangle shape object from a set of coordinates and dimensions
%
%   Examples:
%
%      rectangle = createRectangle(10, 10, 5, 8);
%
% See also: CREATEELLIPSE, SETSHAPECOORDINATES

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

% Check input
ispositivescalar = @(x) isscalar(x) && x > 0;
ip = inputParser;
ip.addRequired('x', @isscalar);
ip.addRequired('y', @isscalar);
ip.addRequired('w', ispositivescalar);
ip.addRequired('h', ispositivescalar);
ip.parse(x, y, w, h)

% Create a Rectangle shape
rectangle = omero.model.RectangleI;
rectangle.setX(rdouble(x));
rectangle.setY(rdouble(y));
rectangle.setWidth(rdouble(w));
rectangle.setHeight(rdouble(h));