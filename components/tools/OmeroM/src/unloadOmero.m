function unloadOmero(varargin)
% Remove OMERO.matlab from the path and javaclasspath.
%
% Remove's all OMERO resources from MATLAB path and javaclasspath
% silencing any warnings about the given paths not being available.
% If loadOmero was called from another directory, this method will
% not be able to remove the paths. Use the unloadOmero method from
% that directory. (In that case, the other unloadOmero should be on
% your path anyway; changing directories might suffice).
%
% This method also calls 'clear java' but does not try to silence
% messages. Any warnings show that objects are still in your work-
% space which hold on to Java objects, which therefore can't be
% cleaned. Strange JVM/Classpath errors may occcur if you do not
% clear the variables and then 'clear java' again.

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

try
    OmeroClient_Jar=fullfile(findOmero, 'libs', 'omero_client.jar');
    javarmpath(OmeroClient_Jar);
    rmpath(genpath(findOmero)) % OmeroM and subdirectories
    lastwarn(''); % We don't care about the clear path warnings.
    clear('java');
catch ME
    warning(java_old.state, JAVAWARNID);
    warning(path_old.state, PATHWARNID);
    throw ME;
end

w = lastwarn;
disp(w);
if ~strcmp(w,'')
    disp('  ');
    disp('=============================================================== ');
    disp('While unloading OMERO, found java objects left in workspace.    ');
    disp('Please remove with ''clear <name>'' and then run ''unloadOmero''');
    disp('again.  Printing all objects...');
    disp('=============================================================== ');
    disp('  ');
    evalin('caller','whos');
end
lastwarn('');
