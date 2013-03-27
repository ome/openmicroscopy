#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# OMERO Registry Database Creation
# Copyright 2007 Glencoe Software, Inc.  All Rights Reserved.
#
try:
    from pysqlite2 import dbapi2 as sqlite
except ImportError:
    import sqlite3 as sqlite
from pprint import pprint
import os, sys

if len(sys.argv) > 1:
    dbname = sys.argv[1]
else:
    dbname = "sqlite.db"

conn = sqlite.connect(dbname)
try:
    cursor = conn.cursor()
    cursor.execute('CREATE TABLE ip  (id varchar(15) PRIMARY KEY, latitude float not null, longitude float not null)')
    cursor.execute("""CREATE TABLE hit (
        ip varchar(15),
        time timestamp not null,
        version text not null,
        poll text not null,
        vmvendor text not null,
        vmruntime text not null,
        osname text not null,
        osarch text not null,
        osversion text not null,
	headers text not null,
        agent text not null,
        other text not null)""")
    conn.commit()
finally:
    conn.close()
