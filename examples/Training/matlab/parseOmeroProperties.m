% Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
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

function props = parseOmeroProperties(client)

omeroProperties = client.getProperties().getPropertiesForPrefix('omero');
props.hostname = char(omeroProperties.get('omero.host'));
props.username = char(omeroProperties.get('omero.user'));
props.password = char(omeroProperties.get('omero.pass'));
props.port = str2double(omeroProperties.get('omero.port'));
props.imageid = str2double(omeroProperties.get('omero.imageid'));
props.datasetid = str2double(omeroProperties.get('omero.datasetid'));
props.plateid = str2double(omeroProperties.get('omero.plateid'));
props.projectid = str2double(omeroProperties.get('omero.projectid'));
props.screenid = str2double(omeroProperties.get('omero.screenid'));
