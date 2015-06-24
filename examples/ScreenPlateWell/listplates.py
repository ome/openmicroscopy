#!/usr/bin/env python
# -*- coding: utf-8 -*-
import omero
from omero_sys_ParametersI import ParametersI  # Temporary

c = omero.client()
s = c.createSession()
q = s.getQueryService()

QUERY = ("select p from Plate p left outer join fetch p.wells w"
         " left outer join fetch w.wellSamples s"
         " left outer join fetch s.image where p.id = :id")

filter = omero.sys.Filter()
plates = q.findAll('Plate', filter)


print 'Plate:'
for i in range(len(plates)):
    params = ParametersI()
    params.addId(plates[i].getId().getValue())
    plate = q.findByQuery(QUERY, params)
    print '    %d %s ' % (i, plate.getName().getValue())
    wells = {}

    for well in plate.copyWells():
        row = well.getRow() and well.getRow().getValue() or -1
        col = well.getColumn() and well.getColumn().getValue() or -1
        if row in wells:
            row_list = wells[row]
        else:
            row_list = []
            wells[row] = row_list
        row_list.append(col)
        row_list.sort()

    rows = list(wells.keys())
    rows.sort()

    for row in rows:
        msg = '        '
        col_list = list(wells[row])
        col_list.sort()
        for col in col_list:
            msg += '%2dx%2d ' % (row, col)
        print msg
