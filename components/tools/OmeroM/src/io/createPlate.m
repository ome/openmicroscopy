function plate = createPlate(session, name, varargin)
% CREATEPLATE Create a new plate and uploads it onto the OMERO server
%
%   plate = createPlate(session, name) create a new plate with the
%   input name, uploads it onto the server and returns the loaded plate.
%
%   plate = createPlate(session, name, 'group', groupId) specifies the
%   group context in which the plate should be created.
%
%   plate = createPlate(session, name, screen) creates a plate and
%   links it to the input screen. The group context is specified by the
%   screen group.
%
%   plate = createPlate(session, name, screenId) creates a plate and
%   links it to the screen specified by the input identifier. The group
%   context is specified by the screen group.

%
%   Examples:
%
%      % Create a plate in the context of the current session group
%      plate = createPlate(session, 'my-plate');
%      % Create a plate in the context of the specified group
%      plate = createPlate(session, 'my-plate', 'group', groupId);
%      % Create a plate and link it to an existing screen
%      plate = createPlate(session, 'my-plate', screen);
%      plate = createPlate(session, 'my-plate', screenId);

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

% Input check
isValidScreen = @(x) isscalar(x) && ...
    (isnumeric(x) || isa(x, 'omero.model.ScreenI'));
ip = inputParser;
ip.addRequired('name', @ischar);
ip.addOptional('screen', [], isValidScreen);
ip.addParamValue('context', java.util.HashMap, @(x) isa(x, 'java.util.HashMap'));
ip.addParamValue('group', [], @(x) isempty(x) || (isscalar(x) && isnumeric(x)));
ip.parse(name, varargin{:});

if ~isempty(ip.Results.screen)
    % Retrieve the screen
    if isnumeric(ip.Results.screen)
        screen = getScreens(session, ip.Results.screen);
        assert(~isempty(screen), 'Cannot find screen %g', ip.Results.screen);
    else
        screen = ip.Results.screen;
    end

    % Determine group from the parent screen
    groupId = screen.getDetails().getGroup().getId().getValue();
    if ~isempty(ip.Results.group)
        if ~isempty(groupId)
            assert(isequal(groupId, ip.Results.group),...
                'Input group is different from the screen group.');
        end
        groupId = ip.Results.group;
    end

    % Create context for uploading
    context = ip.Results.context;
    context.put('omero.group', java.lang.String(num2str(groupId)));

    % Create plate object
    plate = omero.model.PlateI();
    plate.setName(rstring(name));

    % Create screen/plate link
    link = omero.model.ScreenPlateLinkI();
    link.setParent(omero.model.ScreenI(screen.getId().getValue(), false));
    link.setChild(plate);
    link = session.getUpdateService().saveAndReturnObject(link, context);

    % Return the plate
    plate = link.getChild();
else
    % Delegate object creation
    plate = createObject(session, 'plate', name,...
        'context', ip.Results.context, 'group', ip.Results.group);
end
