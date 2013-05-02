#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# OMERO Registry Whois Database
# Copyright 2007 Glencoe Software, Inc.  All Rights Reserved.
#
try:
    from pysqlite2 import dbapi2 as sqlite
except:
    from sqlite3 import dbapi2 as sqlite

from pprint import pprint
import os, sys

class whoisdb:

    def __create__(self, dbname='whois.db'):
        conn = sqlite.connect(dbname)
        try:
            cursor = conn.cursor()
            cursor.execute('CREATE TABLE whois (id varchar(15) PRIMARY KEY, whois text)')
            conn.commit()
        finally:
            conn.close()

    def __init__(self, dbname='whois.db'):
        if not os.path.exists(dbname):
	    self.__create__(dbname)
        self.conn = sqlite.connect(dbname)

    def __close__(self):
        self.conn.close()

    def __iter__(self):
        return iter(self.conn)

    def __values__(self):
        c = self.conn.cursor()
	c.execute('SELECT whois FROM whois')
	return [ value[0] for value in c ]

    def get(self, ip):
        c = self.conn.execute('SELECT id, whois FROM whois WHERE id = ?', (ip,))
	rv = c.fetchone()
	c.close()
	return rv

    def set(self, ip, whois, commit = None):
        c = self.conn.cursor()
        c.execute('INSERT INTO whois VALUES (?,?)', (ip, whois))
	if commit:
       	    self.conn.commit()

    def update(self, ip, whois, commit = None):
        c = self.conn.cursor()
        c.execute('UPDATE whois SET whois = ? WHERE id = ?', (whois, ip))
	if commit:
       	    self.conn.commit()

    def missing(self):
       for line in sys.stdin:
           line = line.strip()
	   parts = line.split()
	   if self.get(parts[0]) == None:
	        if not parts[0].startswith("10."):
	   	    print parts[0]

    def lookup(self):
       from socket import gethostbyaddr as ghba
       from socket import herror
       for line in sys.stdin:
           line = line.strip()
	   parts = line.split()
	   ip = parts[0]
	   if ip.endswith(","):
	       ip = ip[:-1]
	   try:
	   	print "%s\t" % ip,
	   	print ghba(ip)[0]
	   except herror:
	        print "Error"

    def load(self):
       seen = {}
       for line in sys.stdin:
           line = line.strip()
	   parts = line.split("\t")
	   if not parts[1] == "\"\"":
	        if seen.has_key(parts[0]):
		    print "Already seen %s (%s) new value %s" % (parts[0],seen[parts[0]], parts[1])
		else:
	            seen[parts[0]] = parts[1]
		    try:
	   	        self.set(parts[0], parts[1])
		    except Exception, e:
		        print "Failed to insert %s (%s)" % (parts[0], e)
       self.conn.commit()

    def values(self):
        for each in self.__values__():
	    print each

    def report(self, level = "2", filter = "0"):
        lvl = int(level)
	flt = int(filter)
	fmt = ".".join( [ "%s" for i in range(lvl) ] )
        all = {}
        for each in self.__values__():
	    parts = each.split(".")
	    used = []
	    for i in range(lvl):
	        try:
	            used.append( parts[-1 * (i+1)] ) 
                except IndexError:
	    	    used.append(" ")
	    key = fmt % tuple(used)
	    if not all.has_key(key):
	       all[key] = 1
	    else:
	       all[key] += 1
	for k,v in all.items():
	    if v > flt:
	        print "%-64s\t%s" % (k,v)

    def correct(self):
        ips = set()
        for i in self:
	    if 0 <= i[1].find("Resolves correctly"):
	    	ips.add(i[0])
	for ip in ips:
	    t = self.get(ip)
	    m = t[1].split()
	    if m[2] != "correctly" or m[1] != "Resolves":
	        print "Failed"
	    else:
	        self.update(ip, m[0])
	self.conn.commit()

    def lower(self):
        for i in self:
	    self.update(i[0],i[1].lower())
	self.conn.commit()

class iter:

    def __init__(self, conn):
        self.cursor = conn.cursor()
        self.cursor.execute('SELECT id, whois FROM whois')

    def __iter__(self):
        return self.cursor

    def next(self):
        return self.cursor.next()

if __name__ == "__main__":
        db = whoisdb()
	try:
            if len(sys.argv) == 1:
                for ip in db:
                    print "%-15s\t%s" % (ip[0],ip[1])
	    else:
	        arg = list(sys.argv[1:])
	        cmd = arg.pop(0)
		getattr(db, cmd)(*arg)
	finally:
	    db.__close__()
	    
