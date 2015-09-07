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
    % Retrieve the screen identifier
    if isnumeric(ip.Results.screen)
        screenId = ip.Results.screen;
    else
        screenId = ip.Results.screen.getId().getValue();
    end

    % Create plate object
    plate = omero.model.PlateI();
    plate.setName(rstring(name));

    % Create context for uploading
    context = ip.Results.context;
    if ~isempty(ip.Results.group)
        context.put('omero.group', java.lang.String(num2str(ip.Results.group)));
    end

    % Create screen/plate link
    link = omero.model.ScreenPlateLinkI();
    link.setParent(omero.model.ScreenI(screenId, false));
    link.setChild(plate);
    link = session.getUpdateService().saveAndReturnObject(link, context);

    % Return the plate
    plate = link.getChild();
else
    % Delegate object creation
    plate = createObject(session, 'plate', name,...
        'context', ip.Results.context, 'group', ip.Results.group);
end
