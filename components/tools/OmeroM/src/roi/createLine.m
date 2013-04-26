function line = createLine(x, y)
% CREATELINE Create a Line shape object from a set of input vectors
%
%
%   Example:
%
%      p = createLine([10 15], [10 20]);
%
% See also: CREATEPOLYLINE, SETSHAPECOORDINATES

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
validator = @(x) isvector(x) && numel(x) == 2;
ip = inputParser;
ip.addRequired('x', validator);
ip.addRequired('y', validator);
ip.parse(x, y);

% Create Line shape
line = omero.model.LineI;
line.setX1(rdouble(x(1)));
line.setX2(rdouble(x(2)));
line.setY1(rdouble(y(1)));
line.setY2(rdouble(y(2)));