server = 'gretzky.openmicroscopy.org.uk';
user = 'root';
password = 'omero';
client = omero.client(server,4064);
session = client.createSession(user, password);
iUpdate = session.getUpdateService();

% Create a table

% create a table with 2 columns



name = char(java.util.UUID.randomUUID());
columns = javaArray('omero.grid.Column', 2);
columns(1) = omero.grid.LongColumn('Uid', 'testLong', [1]);
valuesString = javaArray('java.lang.String', 1);
valuesString(1) = java.lang.String('table');
columns(2) = omero.grid.StringColumn('MyStringColumn', '', 64, valuesString);
%create a new table.
table = session.sharedResources().newTable(1, name);
%initialize the table
table.initialize(columns);
%add data to the table.
data = javaArray('omero.grid.Column', 2);
data(1) = omero.grid.LongColumn('Uid', 'test Long', [2]);
valuesString = javaArray('java.lang.String', 1);
valuesString(1) = java.lang.String('add');
data(2) = omero.grid.StringColumn('MyStringColumn', '', 64, valuesString);
table.addData(data);
file = table.getOriginalFile(); % if you need to link the table to another object.

client.closeSession();