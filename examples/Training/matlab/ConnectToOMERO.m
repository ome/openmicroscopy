% Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
% All rights reversed.
% Use is subject to license terms supplied in LICENSE.txt

try
    [client, session] = connect();
    %necessary to keep the proxy alive. part of the omero-package
    t = omeroKeepAlive(client);
    % Retrieve the id of the user
    userId = session.getAdminService().getEventContext().userId

    % The group the user is currently logged in i.e. his/her default group
    groupId = session.getAdminService().getEventContext().groupId

    delete(t);

catch err
    disp(err.message);
end

% REMEMBER TO CLOSE SESSION
client.closeSession();
