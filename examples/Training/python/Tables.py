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
import os
import omero.grid
from omero.gateway import BlitzGateway
from omero.util.populate_metadata import ParsingContext
from Parse_OMERO_Properties import USERNAME, PASSWORD, HOST, PORT
from Parse_OMERO_Properties import datasetId, plateId

#
# .. _python_omero_tables_code_samples:

"""
start-code
"""

# Create a connection
# ===================
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()

# Create a name for the Original File (should be unique)
# ======================================================
from random import random
table_name = "TablesDemo:%s" % str(random())
col1 = omero.grid.LongColumn('Uid', 'testLong', [])
col2 = omero.grid.StringColumn('MyStringColumnInit', '', 64, [])
columns = [col1, col2]


# Create and initialize a new table
# =================================
repositoryId = 1
table = conn.c.sf.sharedResources().newTable(repositoryId, table_name)
table.initialize(columns)


# Add data to the table
# =====================
ids = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
strings = ["one", "two", "three", "four", "five",
           "six", "seven", "eight", "nine", "ten"]
data1 = omero.grid.LongColumn('Uid', 'test Long', ids)
data2 = omero.grid.StringColumn('MyStringColumn', '', 64, strings)
data = [data1, data2]
table.addData(data)
table.close()           # when we are done, close.


# Get the table as an original file
# =================================
orig_file = table.getOriginalFile()
orig_file_id = orig_file.id.val
# ...so you can attach this data to an object e.g. Dataset
file_ann = omero.model.FileAnnotationI()
# use unloaded OriginalFileI
file_ann.setFile(omero.model.OriginalFileI(orig_file_id, False))
file_ann = conn.getUpdateService().saveAndReturnObject(file_ann)
link = omero.model.DatasetAnnotationLinkI()
link.setParent(omero.model.DatasetI(datasetId, False))
link.setChild(omero.model.FileAnnotationI(file_ann.getId().getValue(), False))
conn.getUpdateService().saveAndReturnObject(link)


# Table API
# =========
# .. seealso:: :javadoc:` OMERO Tables <slice2html/omero/grid/Table.html>`

open_table = conn.c.sf.sharedResources().openTable(orig_file)
print "Table Columns:"
for col in open_table.getHeaders():
    print "   ", col.name
rowCount = open_table.getNumberOfRows()
print "Row count:", rowCount


# Get data from every column of the specified rows
# ================================================
row_numbers = [3, 5, 7]
print "\nGet All Data for rows: ", row_numbers
data = open_table.readCoordinates(range(rowCount))
for col in data.columns:
    print "Data for Column: ", col.name
    for v in col.values:
        print "   ", v


# Get data from specified columns of specified rows
# =================================================
col_numbers = [1]
start = 3
stop = 7
print "\nGet Data for cols: ", col_numbers,\
    " and between rows: ", start, "-", stop
data = open_table.read(col_numbers, start, stop)
for col in data.columns:
    print "Data for Column: ", col.name
    for v in col.values:
        print "   ", v


# Query the table for rows where the 'Uid' is in a particular range
# =================================================================
query_rows = open_table.getWhereList(
    "(Uid > 2) & (Uid <= 8)", variables={}, start=0, stop=rowCount, step=0)
data = open_table.readCoordinates(query_rows)
for col in data.columns:
    print "Query Results for Column: ", col.name
    for v in col.values:
        print "   ", v
open_table.close()           # we're done


# In future, to get the table back from Original File
# ===================================================
orig_table_file = conn.getObject(
    "OriginalFile", attributes={'name': table_name})    # if name is unique
saved_table = conn.c.sf.sharedResources().openTable(orig_table_file._obj)
print "Opened table with row-count:", saved_table.getNumberOfRows()
saved_table.close()

# Populate a table on a Plate from a csv file
# ===========================================
col_names = "Well, Well Type, Concentration\n"
csv_lines = [
    col_names,
    "A1, Control, 0\n",
    "A2, Treatment, 5\n",
    "A3, Treatment, 10\n"]
with open('data.csv', 'w') as csv_data:
    csv_data.writelines(csv_lines)
plate = conn.getObject("Plate", plateId)
target_object = plate._obj
client = conn.c
ctx = ParsingContext(client, target_object, 'data.csv')
ctx.parse()
ctx.write_to_omero()
os.remove('data.csv')


# Close connection
# ================
# When you are done, close the session to free up server resources.
conn.close()
