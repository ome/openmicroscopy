function projects = getProjects(session, varargin)
% GETPROJECTS Retrieve project objects from the server
%
%   projects = getProjects(session) returns all the projects owned by the
%   session user in the context of the session group.
%
%   projects = getProjects(session, ids) returns all the projects
%   identified by the input ids owned by the session user in the context of
%   the session group.
%
%   By default, getProjects() loads the entire projects/datasets/images
%   graph. This may have consequences in terms of loading time depending on
%   the number of projects to load and datasets/images in the graph.
%
%   Examples:
%
%      projects = getProjects(session);
%      projects = getProjects(session, ids);
%
% See also: GETOBJECTS, GETDATASETS, GETIMAGES

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

% Input check
ip = inputParser;
ip.addOptional('ids', [], @isvector);
ip.parse(varargin{:});

% Indicate to load the Project/Dataset/Images graph
parameters = omero.sys.ParametersI();
parameters.leaves();

projects = getObjects(session, 'project', ip.Results.ids, parameters);