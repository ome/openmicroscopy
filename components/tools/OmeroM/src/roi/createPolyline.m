function polyline = createPolyline(x, y)
% CREATEPOLYLINE Create a Polyline shape object from a set of input vectors
%
%   POLYGON = CREATEPOLYLINE(X, Y) creates an open geometrical figure which
%   contour is defined by a series of points of coordinates (X, Y)
%
%   Example:
%
%      p = createPolyline([10 15], [10 20]);
%
% See also: CREATEPOLYGON, SETSHAPECOORDINATES

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
islongvector = @(x) isvector(x) && numel(x) >= 2;
ip = inputParser;
ip.addRequired('x', islongvector);
ip.addRequired('y', @(y) islongvector(y) && numel(y) == numel(x));
ip.parse(x, y);

% Create Polyline shape
polyline = omero.model.PolylineI;
points = sprintf('%g,%g ', [x(:)'; y(:)']);
polyline.setPoints(rstring(points));