function [client, session] = connectOmero(varargin)
% CONECTOMERO Create an OMERO connection to a server
%
%   client = connectOmero() uses the properties in the ice.config file at
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
%   [client, session] = loadOmero(config_file) also creates a session
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

% Create client using constructor
if nargin > 0
    % Call constructor passing arguments
    client = javaObject('omero.client', varargin{:});
else
    % Try to find a valid configuration file and use it to create an initial
    % omero_client object.
    %
    % Either first in the ICE_CONFIG environment variable
    ice_config = getenv('ICE_CONFIG');
    if isempty(ice_config)
        % Then in the current directory.
        ice_config = which('ice.config');
        if isempty(ice_config) && exist(fullfile(findOmero, 'ice.config'), 'file')==2
            ice_config = fullfile(findOmero, 'ice.config');
        end
    else
        % Clearing the ice_config now, since it is available
        % in the environment, and Ice will pick it up
        % (assuming it was set before MATLAB started)
        ice_config = '';
    end
    
    assert(~isempty(ice_config),'No ice config found');
    
    % If no properties in varargins but ice_config set
    % then ice_config is not set. This is difficult to
    % handle because we don't know what's in the varargin
    % in order to pick the proper constructor (ticket:6892)
    
    ice_config_list=javaArray('java.io.File',1);
    ice_config_list(1)=java.io.File(ice_config);
    client = javaObject('omero.client', ice_config_list);
end

if (nargout <2), return; end
session = client.createSession();