function deleteScreens(session, varargin)
% DELETESCREENS Delete screens from the OMERO server
%
%   deleteScreens(session, ids) deletes all the screens identified by the
%   input ids. All annotations (tags, files...) linked to the screens will
%   either be deleted if not shared with other objects or unlinked if
%   shared with other objects.
%
%   Examples:
%
%      deleteScreens(session, ids);
%
% See also: DELETEOBJECTS, DELETEPLATES, DELETEPLATEACQUISITIONS

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
ip.addRequired('ids', @isvector);
ip.parse(varargin{:});

deleteObjects(session, ip.Results.ids, 'screen');