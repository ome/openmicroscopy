function [projects, datasets] = getProjects(session, varargin)
% GETPROJECTS Retrieve project objects from the server
%
%   projects = getProjects(session) returns all the projects owned by the
%   session user in the context of the session group. By default,
%   getProjects loads the entire projects/datasets/images graph. This may
%   have consequences in terms of loading time depending on the images
%   contained in the projects' datasets.
%
%   projects = getProjects(session, ids) returns all the projects
%   identified by the input ids in the context of the session group.
%
%   projects = getProjects(session, ids, loaded) returns all the projects
%   identified by the input ids in the context of the session group. If
%   loaded is false, the images attached to the  datasets are not loaded.
%   Default: false.
%
%   projects = getProjects(session, 'owner', ownerId) returns all the
%   projects owned by the input owner in the context of the session group.
%
%   projects = getProjects(session, 'group', groupId) returns all the
%   projects owned by the session owner in the context of the input group.
%   A value of -1 for groupId means projects are returned for all groups.
%
%   projects = getProjects(session, ids, 'owner', ownerId) returns all the
%   projects identified by the input ids owned by the input owner in the
%   context of the session group.
%
%   projects = getProjects(session, ids, 'group', groupId) returns all the
%   projects identified by the input ids owned by the session owner in the
%   context of the input group. A value of -1 for groupId means projects
%   are returned for all groups.
%
%   [projects, datasets] = getProjects(session, [],...) returns all the
%   orphaned datasets in addition to all the projects.
%
%   Examples:
%
%      projects = getProjects(session);
%      projects = getProjects(session, 'owner', ownerId);
%      projects = getProjects(session, 'group', groupId);
%      projects = getProjects(session, ids);
%      projects = getProjects(session, ids, 'owner', ownerId);
%      projects = getProjects(session, ids, 'group', -1);
%      projects = getProjects(session, ids, true);
%      projects = getProjects(session, ids, true, 'owner', ownerId);
%      projects = getProjects(session, ids, true, 'group', -1);
%      [projects, datasets] = getProjects(session, [], false);
%
%
% See also: GETOBJECTS, GETDATASETS, GETIMAGES

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

% Input check
ip = inputParser;
ip.addOptional('ids', [], @(x) isempty(x) || (isvector(x) && isnumeric(x)));
ip.addOptional('loaded', false, @islogical);
ip.KeepUnmatched = true;
ip.parse(varargin{:});

parameters = omero.sys.ParametersI();
% Load the images attached to the datasets if loaded is True
if ip.Results.loaded, parameters.leaves(); end

% If more than one output arguments, set orphan
if nargout > 1, parameters.orphan(); end

% Delegate unmatched arguments check to getObjects function
unmatchedArgs =[fieldnames(ip.Unmatched)'; struct2cell(ip.Unmatched)'];
[projects, datasets] = getObjects(session, 'project', ip.Results.ids,...
    parameters, unmatchedArgs{:});
