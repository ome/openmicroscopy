function project = createProject(session, name, varargin)
% CREATEPROJECT Create a new project and uploads it onto the OMERO server
%
%   project = createProject(session, name) creates a new project with the
%   input name, uploads it onto the server and returns the loaded project.
%
%   project = createProject(..., 'group', groupId) specifies the group
%   context in which the project should be created.
%
%   Examples:
%
%      % Creates a new project in the context of the current session group
%      project = createProject(session, projectName);
%      % Creates a new project in the context of the specified group
%      project = createProject(session, projectName, 'group', groupId);
%
% See also: CREATEOBJECT, CREATEDATASET

% Copyright (C) 2013-2015 University of Dundee & Open Microscopy Environment.
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

% Delegate object creation
project = createObject(session, 'project', name, varargin{:});

end
