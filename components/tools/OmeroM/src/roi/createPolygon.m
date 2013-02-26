function polygon = createPolygon(x, y)
% Create a Polygon shape object from a set of input vectors

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
ip = inputParser;
ip.addRequired('x', @isvector);
ip.addRequired('y', @isvector);
ip.parse(x, y);
assert(numel(x) == numel(y), 'x and y must have the same number of elements');
assert(numel(x) >= 2, 'x and y are scalars, use createPoint(x,y) instead');

% Create Polygon shape
polygon = omero.model.PolygonI;
points = sprintf('%g,%g ', x, y);
polygon.setPoints(rstring(points));