function [client, session] = connect
% Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
% All rights reversed.
% Use is subject to license terms supplied in LICENSE.txt

username = 'yourName';
password = 'yourPassword';
server = 'yourServer';
% Connect to server

client = omero.client(server, 4064);
session = client.createSession(username, password);
%if we want the data transfer not to be encrypted
%client = client.createClient(false);
%session = client.getSession();