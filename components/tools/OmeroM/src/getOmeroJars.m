function jarList = getOmeroJars()
% GETOMEROJARS List the JAR files required for the OMERO.matlab toolbox
%
%   jarList = getOmeroJars() return a cell array containing the paths to
%   the JAR files necessary to use all the functionalities of the
%   OMERO.matlab toolbox.
%
% See also: LOADOMERO, UNLOADOMERO

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
%   Detailed explanation goes here

% Retrieve path to the toolbox libraries
libpath = fullfile(findOmero(), 'libs');

% Create a cell array with the dependency jars
guava_jar = fullfile(libpath, 'guava.jar');
jarList = {guava_jar};

% For recent versions of MATLAB, some JAR dependencies are  shipped with
% the external Java libraries:
% * log4j.jar is under $matlabroot/java/jarext,
% * sl4j-api.jar and sl4j-log4j12.jar are under
%   $matlabroot/java/jarext/jxbrowser.
javaPath = javaclasspath('-all');
has_log4j = any(~cellfun(@isempty, regexp(javaPath, '.*log4j.jar$',...
    'match', 'once')));
has_slf4j = any(~cellfun(@isempty, regexp(javaPath, '.*slf4j-api.jar$',...
    'match', 'once')));

% Include log4j dependency if not present in MATLAB java classpath
if ~has_log4j
    log4j_jar = fullfile(libpath, 'log4j.jar');
    jarList = horzcat(jarList, {log4j_jar});
end

% Include slf4j dependencies if not present in MATLAB java classpath
if ~has_slf4j
    slf4j_api_jar = fullfile(libpath, 'slf4j-api.jar');
    slf4j_log4j12_jar = fullfile(libpath, 'slf4j-log4j12.jar');
    jarList = horzcat(jarList, {slf4j_api_jar, slf4j_log4j12_jar});
end

% Finally add omero_client.jar
omero_client_jar = fullfile(libpath, 'omero_client.jar');
jarList = horzcat(jarList, {omero_client_jar});

end
