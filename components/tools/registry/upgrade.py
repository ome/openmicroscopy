#!/usr/bin/env python
#
# OMERO Registry Database Count
# Copyright 2007 Glencoe Software, Inc.  All Rights Reserved.
#

import db

accessdb = db.accessdb()
try:
	c = accessdb.conn.cursor()

	from pysqlite2 import dbapi2 as sqlite

	def agent(t):
	    return eval(t)["user-agent"]

	accessdb.conn.create_function("uagent", 1, agent)
	c.execute('update hit set agent = uagent(headers)')
	accessdb.conn.commit()
finally:
	accessdb.close()

