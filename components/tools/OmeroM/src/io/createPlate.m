function plate = createPlate(session, name, varargin)
% CREATEPLATE Create a new plate and uploads it onto the OMERO server
%
%   plate = createPlate(session, name) create a new plate with the
%   input name, uploads it onto the server and returns the loaded plate.
%
%   plate = createPlate(session, name, screen) also links the plate
%   to the input screen.
%
%   plate = createPlate(session, name, screenId) also links the plate to
%   the screen specified by the input identifier.
%
%   plate = createPlate(..., 'group', groupId) specifies the group
%   context in which the plate should be created.
%
%   Examples:
%
%      % Create a plate in the context of the current session group
%      plate = createPlate(session, 'my-plate');
%      % Create a plate in the context of the current session group and
%      % links it to a screen
%      plate = createPlate(session, 'my-plate', screen);
%      plate = createPlate(session, 'my-plate', screenId);
%      % Create a plate in the context of the specified group
%      plate = createPlate(session, 'my-plate', 'group', groupId);
%
% See also: CREATEOBJECT, CREATESCREEN

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
plate = createObject(session, 'plate', name, varargin);

% Check if optional project is passed
isValidScreen = @(x) isscalar(x) && ...
    (isnumeric(x) || isa(x, 'omero.model.ScreenI'));
ip = inputParser;
ip.addOptional('screen', [], isValidScreen);
ip.parse(varargin{:});

if ~isempty(ip.Results.screen)
    % Check project object
    if isnumeric(ip.Results.screen)
        screen = getScreens(session, ip.Results.screen);
        assert(~isempty(screen), 'Cannot find screen %g', ip.Results.screen);
    else
        screen = ip.Results.screen;
    end
    
    % Create project/dataset link
    link = omero.model.ScreenPlateLinkI();
    link.setParent(screen);
    link.setChild(plate);
    session.getUpdateService().saveAndReturnObject(link);
    
    % Retrieve fully loaded plate
    plate = getPlates(session, plate.getId().getValue());
end

end
