% Copyright (C) 2011-2013 University of Dundee & Open Microscopy Environment.
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


try
    % Connect to a server
    % Use the ice.config file defined in the path
    client = loadOmero();
    hostname = char(client.getProperty('omero.host'));
    fprintf(1, 'Created connection to %s\n', hostname);
    
    % Alternate ways to create clients
    % client = loadOmero(hostname);
    % client = loadOmero('path/to/ice.config');
    
    % Information to edit
    username = char(client.getProperty('omero.user'));
    password = char(client.getProperty('omero.pass'));
    
    % Create OMERO session
    session = client.createSession(username, password);
    adminService = session.getAdminService();
    
    % Retrieve the information of the user
    userId = adminService.getEventContext().userId;
    userName = char(adminService.getEventContext().userName);
    
    % The group the user is currently logged in i.e. his/her default group
    groupId = adminService.getEventContext().groupId;
    groupName = char(adminService.getEventContext().groupName);
    
    fprintf(1, 'Created session for user %s (id: %g) using group %s (id: %g)\n',...
        userName, userId, groupName, groupId);
    
    %necessary to keep the proxy alive. part of the omero-package
    disp('Keep session alive');
    t = omeroKeepAlive(client);
    stop(t);
    delete(t);
    
    % List groups the user is member of
    user = adminService.getExperimenter(userId);
    groupIds = toMatlabList(adminService.getMemberOfGroupIds(user), 'double');
    
    % switch between groups the user is member of
    for groupId = groupIds'
        group = adminService.getGroup(groupId);
        fprintf(1, 'Switching to group %s (id: %g)\n',...
            char(group.getName().getValue()), groupId);
        session.setSecurityContext(group);
        
        % Check the current group has been switched
        assert(groupId == adminService.getEventContext().groupId);
    end
    
catch err
    client.closeSession();
    throw(err);
end

% Close the session
client.closeSession();
