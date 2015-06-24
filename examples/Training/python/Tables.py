#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
#                    All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
FOR TRAINING PURPOSES ONLY!
"""

import omero
import omero.grid
from omero.gateway import BlitzGateway
from Parse_OMERO_Properties import USERNAME, PASSWORD, HOST, PORT
from Parse_OMERO_Properties import datasetId

#
# .. _python_omero_tables_code_samples:

# Create a connection
# =================================================================
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()

# Create a name for the Original File (should be unique)
# =================================================================
from random import random
tablename = "TablesDemo:%s" % str(random())

col1 = omero.grid.LongColumn('Uid', 'testLong', [])
col2 = omero.grid.StringColumn('MyStringColumnInit', '', 64, [])

columns = [col1, col2]


# Create and initialize a new table.
# =================================================================
repositoryId = 1
table = conn.c.sf.sharedResources().newTable(repositoryId, tablename)
table.initialize(columns)


# Add data to the table.
# =================================================================
ids = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
strings = ["one", "two", "three", "four", "five",
           "six", "seven", "eight", "nine", "ten"]
data1 = omero.grid.LongColumn('Uid', 'test Long', ids)
data2 = omero.grid.StringColumn('MyStringColumn', '', 64, strings)
data = [data1, data2]
table.addData(data)
table.close()           # when we are done, close.


# Get the table as an original file...
# =================================================================
orig_file = table.getOriginalFile()
orig_file_id = orig_file.id.val
# ...so you can attach this data to an object. E.g. Dataset
fileAnn = omero.model.FileAnnotationI()
# use unloaded OriginalFileI
fileAnn.setFile(omero.model.OriginalFileI(orig_file_id, False))
fileAnn = conn.getUpdateService().saveAndReturnObject(fileAnn)
link = omero.model.DatasetAnnotationLinkI()
link.setParent(omero.model.DatasetI(datasetId, False))
link.setChild(omero.model.FileAnnotationI(fileAnn.id.val, False))
conn.getUpdateService().saveAndReturnObject(link)


# Table API
# =================================================================
# .. seealso:: :javadoc:` OMERO Tables <slice2html/omero/grid/Table.html>`

openTable = conn.c.sf.sharedResources().openTable(orig_file)

print "Table Columns:"
for col in openTable.getHeaders():
    print "   ", col.name
rowCount = openTable.getNumberOfRows()
print "Row count:", rowCount


# Get data from every column of the specified rows
# =================================================================
rowNumbers = [3, 5, 7]
print "\nGet All Data for rows: ", rowNumbers
data = openTable.readCoordinates(range(rowCount))
for col in data.columns:
    print "Data for Column: ", col.name
    for v in col.values:
        print "   ", v


# Get data from specified columns of specified rows
# =================================================================
colNumbers = [1]
start = 3
stop = 7
print "\nGet Data for cols: ", colNumbers,\
    " and between rows: ", start, "-", stop

data = openTable.read(colNumbers, start, stop)
for col in data.columns:
    print "Data for Column: ", col.name
    for v in col.values:
        print "   ", v


# Query the table for rows where the 'Uid' is in a particular range
# =================================================================
queryRows = openTable.getWhereList(
    "(Uid > 2) & (Uid <= 8)", variables={}, start=0, stop=rowCount, step=0)
data = openTable.readCoordinates(queryRows)
for col in data.columns:
    print "Query Results for Column: ", col.name
    for v in col.values:
        print "   ", v
openTable.close()           # we're done


# In future, to get the table back from Original File
# =================================================================
orig_table_file = conn.getObject(
    "OriginalFile", attributes={'name': tablename})    # if name is unique
savedTable = conn.c.sf.sharedResources().openTable(orig_table_file._obj)
print "Opened table with row-count:", savedTable.getNumberOfRows()


# Close connection:
# =================================================================
# When you are done, close the session to free up server resources.
conn._closeSession()
