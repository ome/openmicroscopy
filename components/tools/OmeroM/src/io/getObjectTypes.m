function types = getObjectTypes()
% GETOBJECTTYPES Return a dictionary of OMERO objects types
%
%   types = getObjectTypes() returns a dictionary of OMERO object types
%   organized as an array of structures with three fields: name, class and
%   Iobject.
%
% See also: GETOBJECTS

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

types(1).name = 'project';
types(1).class = 'omero.model.Project';
types(1).Iobject = @omero.model.ProjectI;
types(2).name = 'dataset';
types(2).class = 'omero.model.Dataset';
types(2).Iobject = @omero.model.DatasetI;
types(3).name = 'image';
types(3).class = 'omero.model.Image';
types(3).Iobject = @omero.model.ImageI;
types(4).name = 'screen';
types(4).class = 'omero.model.Screen';
types(4).Iobject = @omero.model.ScreenI;
types(5).name = 'plate';
types(5).class = 'omero.model.Plate';
types(5).Iobject = @omero.model.PlateI;
types(6).name = 'plateacquisition';
types(6).class = 'omero.model.PlateAcquisition';
types(6).Iobject = @omero.model.PlateAcquisitionI;