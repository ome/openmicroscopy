% Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
% All rights reversed.
% Use is subject to license terms supplied in LICENSE.txt

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
    disp(err.message);
end

% REMEMBER TO CLOSE SESSION
client.closeSession();
