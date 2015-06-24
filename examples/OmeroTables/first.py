#!/usr/bin/env python
# -*- coding: utf-8 -*-
import omero

c = omero.client()
s = c.createSession()
r = s.sharedResources()
m = r.repositories()
i = m.descriptions[0].id.val
t = r.newTable(i, "/example.h5")
l = omero.grid.LongColumn('name', 'desc', None)
t.initialize([l])
l.values = [1, 2, 3, 4]
t.addData([l])
ids = t.getWhereList('(name==1)', {}, 0, 0, 0)
data = t.readCoordinates(ids)
print data.columns[0].values[0]
data = t.slice([0], [0])
print data.columns[0].values[0]
