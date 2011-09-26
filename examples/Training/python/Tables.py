# import the libraries we need
import my_omero_config as conf
import omero
import omero.grid
from omero.gateway import BlitzGateway
# create a connection
conn = BlitzGateway(conf.USERNAME, conf.PASSWORD, host=conf.HOST, port=conf.PORT)
print conn.connect()
datasetId = 33

# create a name for the Original File (should be unique)
from random import random
name = "TablesDemo:%s" % str(random())


col1 = omero.grid.LongColumn('Uid', 'testLong', [])
col2 = omero.grid.StringColumn('MyStringColumnInit', '', 64, []);

columns = [col1, col2]

# create a new table.
repositoryId = 1
table = conn.c.sf.sharedResources().newTable(repositoryId, name)

# initialize the table
table.initialize(columns)

# add data to the table.
ids = [1,2,3,4,5,6,7,8,9,10]
strings = ["one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"]
data1 = omero.grid.LongColumn('Uid', 'test Long', ids);
data2 = omero.grid.StringColumn('MyStringColumn', '', 64, strings);
data = [data1, data2]
table.addData(data)

# get the table as an original file...
orig_file = table.getOriginalFile()
# ...so you can attach this data to an object. E.g. Dataset
fileAnn = omero.model.FileAnnotationI()
fileAnn.setFile(orig_file)
link = omero.model.DatasetAnnotationLinkI()
link.setParent(omero.model.DatasetI(datasetId, False))
link.setChild(fileAnn)
conn.getUpdateService().saveAndReturnObject(link)

# table API http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/slice2html/omero/grid/Table.html
openTable = conn.c.sf.sharedResources().openTable(orig_file)

print "Table Columns:"
for col in openTable.getHeaders():
    print "   ", col.name

rowCount = openTable.getNumberOfRows()
print "Row count:",rowCount

# Get data from every column of the specified rows

rowNumbers = [3,5,7]
print "\nGet All Data for rows: ", rowNumbers
data = openTable.readCoordinates(range(rowCount))
for col in data.columns:
    print "Data for Column: ", col.name
    for v in col.values:
        print "   ", v

# Get data from specified columns of specified rows
colNumbers = [1]
start = 3
stop = 7
print "\nGet Data for cols: ", colNumbers, " and between rows: ", start, "-", stop 
data = openTable.read(colNumbers, start, stop)
for col in data.columns:
    print "Data for Column: ", col.name
    for v in col.values:
        print "   ", v


queryRows = openTable.getWhereList("(Uid > 2) & (Uid <= 8)", variables={}, start=0, stop=rowCount, step=0)
data = openTable.readCoordinates(queryRows)
for col in data.columns:
    print "Query Results for Column: ", col.name
    for v in col.values:
        print "   ", v