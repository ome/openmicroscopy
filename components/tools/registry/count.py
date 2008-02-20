#!/usr/bin/env python
#
# OMERO Registry Database Count
# Copyright 2007 Glencoe Software, Inc.  All Rights Reserved.
#

import db

accessdb = db.accessdb()
try:
	c = accessdb.conn.cursor()
	c.execute('SELECT ip, count(ip) from hit group by ip order by count(ip) desc')
	print "Count:","="*34
	for ip in c:
		print ip
	print "=" * 40
finally:
	accessdb.close()

