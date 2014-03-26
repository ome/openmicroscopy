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
guavajdk5_jar = fullfile(libpath, 'guava-jdk5.jar');
jarList = {guavajdk5_jar};

% For MATLAB versions 7.13 (R2011b) and above, the sl4j-api.jar and
% sl4j-log4j12.jar JARs are shipped with the external Java libraries of
% MATLAB under $matlabroot/java/jarext/jxbrowser.
if verLessThan('MATLAB', '7.13')
    slf4j_api_jar = fullfile(libpath, 'slf4j-api.jar');
    slf4j_log4j12_jar = fullfile(libpath, 'slf4j-log4j12.jar');
    jarList = horzcat(jarList, {slf4j_api_jar, slf4j_log4j12_jar});
end

% Finally add omero_client.jar
omero_client_jar = fullfile(libpath, 'omero_client.jar');
jarList = horzcat(jarList, {omero_client_jar});

end
