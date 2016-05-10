function ellipse = createEllipse(x, y, radiusx, varargin)
% CREATEELLIPSE Create a ellipse shape object from a set of coordinates and radii
%
%   Examples:
%
%      ellipse = createEllipse(10, 10, 5);
%      ellipse = createEllipse(10, 10, 5, 8);
%
% See also: CREATERECTANGLE, SETSHAPECOORDINATES

% Copyright (C) 2013-2016 University of Dundee & Open Microscopy Environment.
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
isposscalar = @(x) isscalar(x) && x > 0;
ip = inputParser;
ip.addRequired('x', @isscalar);
ip.addRequired('y', @isscalar);
ip.addRequired('radiusx', isposscalar);
ip.addOptional('radiusy', radiusx, isposscalar);
ip.parse(x, y, radiusx, varargin{:});

% Create an Ellipse shape
ellipse = omero.model.EllipseI;
ellipse.setX(rdouble(x));
ellipse.setY(rdouble(y));
ellipse.setRadiusX(rdouble(radiusx));
ellipse.setRadiusY(rdouble(ip.Results.radiusy));
