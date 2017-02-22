% Copyright (C) 2011-2014 University of Dundee & Open Microscopy Environment.
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
% GNU General Public License f4or more details.
%
% You should have received a copy of the GNU General Public License along
% with this program; if not, write to the Free Software Foundation, Inc.,
% 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
% ROIs

% To learn about the model see  http://www.ome-xml.org/wiki/ROI/2010-04.
% Note that annotation can be linked to ROI.
try
    %%
    % start-code
    %%
    
    % Initialize a client and a session using the ice.config file
    % See ConnectToOMERO for alternative ways to initialize a session
    [client, session] = loadOmero();
    p = parseOmeroProperties(client);
    eventContext = session.getAdminService().getEventContext();
    fprintf(1, 'Created connection to %s\n', p.hostname);
    msg = 'Created session for user %s (id: %g) using group %s (id: %g)\n';
    fprintf(1, msg, char(eventContext.userName), eventContext.userId,...
        char(eventContext.groupName), eventContext.groupId);
    
    % Information to edit
    imageId = p.imageid;
    
    % Initialize table variables
    name = char(java.util.UUID.randomUUID());
    columns = javaArray('omero.grid.Column', 2);
    columns(1) = omero.grid.LongColumn('Uid', 'testLong', []);
    valuesString = javaArray('java.lang.String', 1);
    columns(2) = omero.grid.StringColumn('MyStringColumn', '', 64, valuesString);
    
    % Create a new table.
    table = session.sharedResources().newTable(1, name);
    
    % Initialize the table
    table.initialize(columns);
    % Add data to the table.
    data = javaArray('omero.grid.Column', 2);
    data(1) = omero.grid.LongColumn('Uid', 'test Long', [2]);
    valuesString = javaArray('java.lang.String', 1);
    valuesString(1) = java.lang.String('add');
    data(2) = omero.grid.StringColumn('MyStringColumn', '', 64, valuesString);
    table.addData(data);
    file = table.getOriginalFile(); % if you need to interact with the table
    
    of = omero.model.OriginalFileI(file.getId(), false);
    tablePrx = session.sharedResources().openTable(of);
    
    % Read headers
    headers = tablePrx.getHeaders();
    for i=1:size(headers, 1),
        headers(i).name; % name of the header
        % Do something
    end
    
    % Depending on the size of table, you may only want to read some blocks.
    cols = [0:size(headers, 1)-1]; % The number of columns you wish to read.
    rows = [0:tablePrx.getNumberOfRows()-1]; % The number of rows you wish to read.
    data = tablePrx.slice(cols, rows); % Read the data.
    c = data.columns;
    for i=1:size(c),
        column = c(i);
        % Do something
    end
    tablePrx.close(); % Important to close when done.
    
catch err
    client.closeSession();
    throw(err);
end

% Close the session
client.closeSession();