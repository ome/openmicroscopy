function types = getObjectTypes()
% GETOBJECTTYPES Return a dictionary of OMERO object types
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

objects = ...
    {
    'project', 'omero.model.Project', @omero.model.ProjectI, '/Project';
    'dataset', 'omero.model.Dataset', @omero.model.DatasetI, '/Dataset';
    'image', 'omero.model.Image', @omero.model.ImageI, '/Image';
    'screen', 'omero.model.Screen', @omero.model.ScreenI, '/Screen';
    'plate', 'omero.model.Plate', @omero.model.PlateI, '/Plate';
    'plateacquisition', 'omero.model.PlateAcquisition',...
    @omero.model.PlateAcquisitionI, '/PlateAcquisition'
    };
fieldnames = {'name', 'class', 'Iobject', 'delete'};
types = cell2struct(objects', fieldnames);