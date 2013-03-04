function shape = setShapeCoordinates(shape, z, c, t)
% SETSHAPECOORDINATES Set the Z, C and T values of any Shape object
%
%   Example:
%
%      setShapeCoordinates(shape, 0, 0, 0);
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

% Check input
ip = inputParser;
ip.addRequired('shape', @(x) isa(shape, 'omero.model.Shape'));
ip.addOptional('z', 0, @isscalar);
ip.addOptional('c', 0, @isscalar);
ip.addOptional('t', 0, @isscalar);
ip.parse(shape, z, c, t)

% Set shape coordinates
shape.setTheZ(rint(ip.Results.z));
shape.setTheC(rint(ip.Results.c));
shape.setTheT(rint(ip.Results.t));