#!/usr/bin/env python
#
# OMERO Registry Database Count
# Copyright 2007 Glencoe Software, Inc.  All Rights Reserved.
#

import db

def filter(ip, value):
	return ip[0].startswith(value)

def lastNDays(title, query):
	print "\n"
	c.execute(query)
	print title,"="*34

	for ip in c:
		print "%10s  Count: %5s" % (ip[0], ip[1])

def allIps(title, query):
	print "\n"
	c.execute(query)
	print title,"="*34

	jdk_map = {}
	os_map = {}
	total = 0
	for ip in c:
		if filter(ip, "10."): continue
		if filter(ip, "127.0.0.1"): continue
		if filter(ip, "192.168."): continue
		print "IP: %16s  Starts: %6s" % (ip[0], ip[1])
		os_key = ip[2]
		jdk_key = ip[6]

		if not os_map.has_key(os_key):
			os_map[os_key]=0
		if not jdk_map.has_key(jdk_key):
			jdk_map[jdk_key]=0

		os_map[os_key] = 1 + os_map[os_key]
		jdk_map[jdk_key] = 1 + jdk_map[jdk_key]
		total+=1
	print "=" * 40
	print "Total:",total
	print "=" * 40

	print "\nOperating System"
	for os_key in os_map.iterkeys():
		print "%24s = %9s ( %5.2f%% )" % (os_key, os_map[os_key], 100.0*os_map[os_key]/total)
	
	print "\nJava version"
	for jdk_key in jdk_map.iterkeys():
		print "%24s = %9s ( %5.2f%% )" % (jdk_key, jdk_map[jdk_key], 100.0*jdk_map[jdk_key]/total)


accessdb = db.accessdb()
try:	
	c = accessdb.conn.cursor()

	ndays = "select date(time), count(date(time)) from hit where agent like '%s' group by date(time) order by date(time) desc limit 30"
	lastNDays("Last 30 days:          ", ndays % "%")
	lastNDays("Last 30 days (server): ", ndays % "OMERO.server")
	lastNDays("Last 30 days (insight):", ndays % "OMERO.insight")
	lastNDays("Last 30 days (editor): ", ndays % "OMERO.editor")
	lastNDays("Last 30 days (importer): ", ndays % "OMERO.importer")

	allip = "SELECT ip, count(ip), osname, osarch, osversion, vmvendor, vmruntime from hit where agent like '%s' group by ip order by count(ip) desc"
	allIps("All IPs:          ", allip % "%")
	allIps("All IPs (server): ", allip % "OMERO.server")
	allIps("All IPs (insight):", allip % "OMERO.insight")
	allIps("All Ips (editor): ", allip % "OMERO.editor")
	allIps("All Ips (importer): ", allip % "OMERO.importer")

finally:
	accessdb.close()

