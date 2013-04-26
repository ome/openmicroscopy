function shape = setShapeCoordinates(shape, varargin)
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
isvalidimension = @(x) (isscalar(x) && x >= 0) || isempty(x);
ip = inputParser;
ip.addRequired('shape', @(x) isa(shape, 'omero.model.Shape'));
ip.addOptional('z', [], isvalidimension);
ip.addOptional('c', [], isvalidimension);
ip.addOptional('t', [], isvalidimension);
ip.parse(shape, varargin{:})

% Set the Z-dimension
if ~isempty(ip.Results.z),
    shape.setTheZ(rint(ip.Results.z));
else
    shape.setTheZ([]);
end

% Set the C-dimension
if ~isempty(ip.Results.c),
    shape.setTheC(rint(ip.Results.c));
else
    shape.setTheC([]);
end

% Set the T-dimension
if ~isempty(ip.Results.t),
    shape.setTheT(rint(ip.Results.t));
else
    shape.setTheT([]);
end
