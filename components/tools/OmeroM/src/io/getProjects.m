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
%   projects = getProjects(session, ids, loaded) returns all the projects
%   identified by the input ids owned by the session user in the context of
%   the session group. If loaded is True, the images attached to the
%   datasets are loaded.
%
%   By default, getProjects() loads the entire projects/datasets/images
%   graph. This may have consequences in terms of loading time depending on
%   the images contained in the projects' datasets.
%
%   Examples:
%
%      projects = getProjects(session);
%      projects = getProjects(session, ids);
%      projects = getProjects(session, ids, false);
%      projects = getProjects(session, [], false);
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
ip.addOptional('ids', [], @(x) isempty(x) || isvector(x));
ip.addOptional('loaded', true, @islogical);
ip.parse(varargin{:});

parameters = omero.sys.ParametersI();
% Load the images attached to the datasets if loaded is True
if ip.Results.loaded, parameters.leaves(); end

projects = getObjects(session, ip.Results.ids, 'project', parameters);