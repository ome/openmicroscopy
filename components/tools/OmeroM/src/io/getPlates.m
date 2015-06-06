function plates = getPlates(session, varargin)
% GETPLATES Retrieve plate objects from the OMERO server
%
%   plates = getPlates(session) returns all the plates owned by the
%   session user in the context of the session group.
%
%   plates = getPlates(session, ids) returns all the plates identified by
%   the input ids in the context of the session group.
%
%   plates = getPlates(..., 'owner', owner) specifies the owner of the
%   plates. A value of -1 implies plates are returned independently of
%   the owner.
%
%   plates = getPlates(..., 'group', groupId) specifies the group
%   context for the plates. A value of -1 means plates are returned
%   across groups.
%
%   Examples:
%
%      plates = getPlates(session);
%      plates = getPlates(session, 'owner', ownerId);
%      plates = getPlates(session, 'group', groupId);
%      plates = getPlates(session, ids);
%      plates = getPlates(session, ids, 'owner', ownerId);
%      plates = getPlates(session, ids, 'group', groupId);
%
% See also: GETOBJECTS, GETSCREENS, GETIMAGES

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

% Delegate unmatched arguments check to getObjects function
unmatchedArgs =[fieldnames(ip.Unmatched)'; struct2cell(ip.Unmatched)'];
plates = getObjects(session, 'plate', ip.Results.ids, unmatchedArgs{:});