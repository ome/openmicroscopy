function [client, session] = loadOmero(varargin)
% LOADOMERO Add OMERO.matlab to the MATLAB path and Java class path, and optionally login.
%
%   loadOmero()  specifies the directory of the current method as an
%   OMERO.matlab toolbox installation, and adds it to the path and the
%   dynamic javaclasspath. (If you have already specified an OMERO jar on
%   your static classpath via classpath.txt, it will take priority. Please
%   remove it to use loadOmero)
%
%   client = loadOmero() adds OMERO.matlab to the Matlab path and Java
%   class path and connects to a server using the properties specified by
%   the ice.config file at the root of the OMERO.matlab directory.
%
%   [client, session] = loadOmero() also creates a session on this server
%   using the properties specified in the ice.config file at the root of
%   the OMERO.matlab directory.
%
%   client = loadOmero(server) adds OMERO.matlab to the Matlab path and
%   Java class path and connects to the input server.
%
%   client = loadOmero(server, port) adds OMERO.matlab to the Matlab path
%   and Java class path and connects to the input server using the input
%   port number
%
%   client = loadOmero(properties) adds OMERO.matlab to the Matlab path
%   and Java class path and connects to a server using the value of
%   'omero.host' stored in the input properties of java.util.Properties.
%
%   [client, session] = loadOmero(properties) also creates a session using
%   the values of 'omero.user' and 'omero.pass' stored in the input
%   properties.
%
%   client = loadOmero(config_file) adds OMERO.matlab to the Matlab path
%   and Java class path and connects to the input server using the Ice
%   configuration file located in the input config_file path.
%
%   [client, session] = loadOmero(config_file) alsoe creates a session
%   using the configuration file located in the input config_file path.
%
% See also: CONNECTOMERO, UNLOADOMERO

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


% Check if "omero.client" is already on the classpath, if not
% then add the omero_client.jar to the javaclasspath.
if exist('omero.client','class') == 0
    
    disp('');
    disp('--------------------------');
    disp('OmeroMatlab Toolbox ');
    disp(omeroVersion);
    disp('--------------------------');
    disp('');
    
    % Add the omero_client and guava jar to the Java dynamic classpath
    % This will allow the import omero.* statement to pass
    % successfully.
    libpath = fullfile(findOmero, 'libs');
    omero_client_jar = fullfile(libpath, 'omero_client.jar');
    guava_jar = fullfile(libpath, 'guava.jar');
    javaaddpath(omero_client_jar);
    import omero.*;
    javaaddpath(guava_jar);
    
    % Also add the OmeroM directory and its subdirectories to the path
    % so that functions and demos are available even if the user changes
    % directories. See the unloadOmero function for how to remove these
    % values.
    addpath(genpath(findOmero)); % OmeroM and subdirectories
    
    % If it does exist, then check that there aren't more than one
    % version active.
else
    
    w = which('omeroVersion','-ALL');
    sz = size(w);
    sz = sz(1);
    if sz > 1
        warning('OMERO:loadOmero','More than one OMERO version found!');
        disp(char(w));
    end
    
end


% If one or more return values are specified, then load some useful
% objects and return them.

if nargout == 1,
    client = connectOmero(varargin{:});
elseif nargout == 2,
    [client, session] = connectOmero(varargin{:});
end