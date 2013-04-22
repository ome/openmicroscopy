function screens = getScreens(session, varargin)
% GETSCREENS Retrieve screen objects from the OMERO server
%
%   screens = getScreens(session) returns all the screens owned by the
%   session user in the context of the session group. By default,
%   getScreens loads all the plates attached to the screens. This may have
%   consequences in terms of loading time depending on the number of
%   screens to load and plates attached to them.
%
%   screens = getScreens(session, ids) returns all the screens identified
%   by the input ids in the context of the session group.
%
%   screens = getScreens(session, 'owner', owner) returns all the screens
%   owned by the input owner in the context of the session group.
%
%   screens = getScreens(session, ids, 'owner', owner) returns all the
%   screens identified by the input ids owned by the input user in the
%   context of the session group.
%
%   Examples:
%
%      screens = getScreens(session);
%      screens = getScreens(session, 'owner', ownerId);
%      screens = getScreens(session, ids);
%      screens = getScreens(session, ids, 'owner', ownerId);
%
% See also: GETOBJECTS, GETPLATES, GETIMAGES

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
ip.addOptional('ids', [], @(x) isempty(x) || (isvector(x) && isnumeric(x)));
ip.KeepUnmatched = true;
ip.parse(varargin{:});

% Delegate unmatched arguments check to getObjects function
unmatchedArgs =[fieldnames(ip.Unmatched)' struct2cell(ip.Unmatched)'];
screens = getObjects(session, 'screen', ip.Results.ids, unmatchedArgs{:});