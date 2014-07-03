function [client, session] = connectOmero(varargin)
% CONNECTOMERO Create an OMERO connection to a server
%
%   client = connectOmero() uses the properties in the ice.config file a
%   the root of the OMERO.matlab directory to create an client.
%
%   [client, session] = connectOmero() also uses the properties in the
%   ice.config file at  the root of the OMERO.matlab directory to create
%   a session on the server.
%
%   client = connectOmero(server) connects to the input server.
%
%   client = connectOmero(server, port) connects to the input server using
%   the input port number
%
%   client = connectOmero(properties) connects to a server specified by
%   'omero.host' in the input properties of type java.util.Properties.
%
%   [client, session] = connectOmero(properties) also creates a session
%   specified by 'omero.user' and 'omero.pass' in the input properties.
%
%   client = connectOmero(config_file) connects to a server using the Ice
%   configuration file located in the input path.
%
%   [client, session] = connectOmero(config_file) also creates a session
%   using the configuration file located in the input config_file path.
%
% See also: LOADOMERO

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

if nargin == 0
    % If no argument is supplied, look for a configuration file
    ice_config_file = getDefaultConfigFile();
    assert(isValidFile(ice_config_file), 'No valid configuration file found');
    % Create a connection arguments
    connArgs = {toJavaFileArray(ice_config_file)};
else
    % Test if configuration files or properties are passed
    isconfigfile = cellfun(@isValidFile, varargin);
    assert(all(isconfigfile) || all(~isconfigfile),...
        'Mixed connection inputs');
    if all(isconfigfile)
        connArgs = {toJavaFileArray(varargin{isconfigfile})};
    else
        connArgs = varargin;
    end
end

% Create client object
client = javaObject('omero.client', connArgs{:});
client.setAgent('OMERO.matlab');

if nargout > 1,
    % Create session
    session = client.createSession();
end

function ice_config_list = toJavaFileArray(varargin)

% Convert array of file paths into javaArray for client constructor
ice_config_list = javaArray('java.io.File', nargin);
for i = 1 : nargin
    % Get absolute file path
    [~, f] = fileattrib(varargin{i});
    configfilepath = f.Name;
    ice_config_list(i) = java.io.File(configfilepath);
end

function ice_config = getDefaultConfigFile()
% Return a valid configuration file to create an initial omero_client object

% Either in the ICE_CONFIG environment variable
ice_config = getenv('ICE_CONFIG');
if isValidFile(ice_config), return; end

% Then in the Matlab path
ice_config = which('ice.config');
if isValidFile(ice_config), return; end

% At the root of the OMERO.matlab toolbox
ice_config = fullfile(findOmero, 'ice.config');
if isValidFile(ice_config), return; end

% Clearing the ice_config now, since it is available
ice_config = '';

function status = isValidFile(path)
status = ischar(path) && exist(path,'file')==2;
