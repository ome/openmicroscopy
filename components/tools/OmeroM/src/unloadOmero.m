function unloadOmero(varargin)
% UNLOADOMERO Remove OMERO.matlab from the path and javaclasspath.
%
%   loadOmero() removes all OMERO resources from MATLAB path and Java
%   classpath silencing any warnings about the given paths not being
%   available.
%
% If loadOmero() was called from another directory, this method will not be
% able to remove these paths and the unloadOmero() method from that
% directory should be used. (In that case, the other unloadOmero should be
% on your path anyway; changing directories might suffice).
%
% This method also calls 'clear java' but does not try to silence messages.
% If Java objects still exist, e.g. an omero.client, a warning will pop-up
% listing the Java objects still in the workspace. Strange JVM/Classpath
% errors may occcur if you do not clear the variables and then 'clear java'
% again.
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

% Disabling warnings since we are taking actions
% that the user doesn't know nor care about. If
% these values are not on the path, then so be it.
JAVAWARNID='MATLAB:GENERAL:JAVARMPATH:NotFoundInPath';
PATHWARNID='MATLAB:rmpath:DirNotFound';
java_old=warning('query', JAVAWARNID);
path_old=warning('query', PATHWARNID);
warning('off', JAVAWARNID);
warning('off', PATHWARNID);

% Now, we clear any existing warning, so we can detect if there are any
% warnings on clear java.
w = lastwarn;
if ~strcmp(w,'')
    disp('  ');
    disp('=============================================================');
    disp(' Last warning is about to be cleared:                        ');
    disp(' ------------------------------------                        ');
    disp(w);
    disp('=============================================================');
    disp('  ');
end

% See comment in omeroKeepAlive.m
keep_alives = timerfind('Tag','omeroKeepAlive');
if size(keep_alives) > 0,
    disp('Stopping all omeroKeepAlive timers');
    for i=1:size(keep_alives),
        stop(keep_alives(i));
        delete(keep_alives(i));
    end
end

% List jars
libpath = fullfile(findOmero, 'libs');
omero_client_jar = fullfile(libpath, 'omero_client.jar');
guava_jar = fullfile(libpath, 'guava.jar');

try
    % Remove OMERO jars from Java class path
    javarmpath(omero_client_jar);
    javarmpath(guava_jar);
    [w, wid] = lastwarn;
    if ~strcmp(w, '') && ~isequal(wid, JAVAWARNID)
        disp('  ');
        disp('=============================================================== ');
        disp('While unloading OMERO, found java objects left in workspace.    ');
        disp('Please remove with ''clear <name>'' and then run ''unloadOmero''');
        disp('again.  Printing all objects...');
        disp('=============================================================== ');
        disp('  ');
        evalin('caller','whos');
        lastwarn('');
        
        % Closing sessions for all omero.client objects
        javaObjects  = evalin('caller','whos');
        clients = javaObjects(strcmp({javaObjects.class}, 'omero.client'));
        fprintf(1, 'Closing session(s) for %g found client(s):', numel(clients));
        for i = 1:numel(clients)
            fprintf(1, ' %s', clients(i).name);
            client_copy = evalin('caller', clients(i).name);
            client_copy.closeSession();
        end
        fprintf(1, '\n');
        return
    end
    clear('java');
catch ME
    warning(java_old.state, JAVAWARNID);
    throw ME;
end

try
    % Remove OMERO.matlab from Matlab path
    rmpath(genpath(findOmero))
catch ME
    warning(path_old.state, PATHWARNID);
    throw ME;
end
lastwarn('');
