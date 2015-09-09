function screen = createScreen(session, name, varargin)
% CREATESCREEN Create a new screen and uploads it onto the OMERO server
%
%   screen = createScreen(session, name) creates a new screen with the
%   input name, uploads it onto the server and returns the loaded screen.
%
%   screen = createScreen(session, name, 'group', groupId) specifies the
%   group context in which the screen should be created.
%
%   Examples:
%
%      % Create a new screen in the context of the current session group
%      screen = createScreen(session, 'my-screen')
%      % Create a new screen in the context of the specified group
%      screen = createScreen(session, 'my-screen', 'group', groupId)
%
% See also: CREATEOBJECT, CREATEPLATE

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
screen = createObject(session, 'screen', name, varargin{:});

end
