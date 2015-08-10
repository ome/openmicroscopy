function [screens, plates] = getScreens(session, varargin)
% GETSCREENS Retrieve screen objects from the OMERO server
%
%   screens = getScreens(session) returns all the screens owned by the
%   session user in the context of the session group. By default,
%   getScreens loads all the plates attached to the screens. This may have
%   consequences in terms of loading time depending on the number of
%   screens to load and plates attached to them.
%
%   screens = getScreens(session, ids) returns all the screens identified
%   by the input ids independently of the owner across groups.
%
%   screens = getScreens(..., 'owner', owner) specifies the owner of the
%   screens. A value of -1 implies screens are returned independently of
%   the owner.
%
%   screens = getScreens(..., 'group', groupId) specifies the group
%   context for the screens. A value of -1 means screens are returned
%   across groups.
%
%   [screens, plates] = getScreens(session, [],...) returns all the
%   orphaned platest in addition to all the projects.
%
%   Examples:
%
%      screens = getScreens(session);
%      screens = getScreens(session, 'owner', ownerId);
%      screens = getScreens(session, 'group', groupId);
%      screens = getScreens(session, ids);
%      screens = getScreens(session, ids, 'owner', ownerId);
%      screens = getScreens(session, ids, 'group', groupId);
%      [screens, plates] = getScreens(session, []);
%
% See also: GETOBJECTS, GETPLATES, GETIMAGES

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
ip.KeepUnmatched = true;
ip.parse(varargin{:});

parameters = omero.sys.ParametersI();

% If more than one output arguments, set orphan
if nargout > 1, parameters.orphan(); end

% Delegate unmatched arguments check to getObjects function
unmatchedArgs =[fieldnames(ip.Unmatched)'; struct2cell(ip.Unmatched)'];
[screens, plates] = getObjects(session, 'screen', ip.Results.ids,...
    parameters, unmatchedArgs{:});
