#!/usr/bin/env python
#
# OMERO Registry Database Count
# Copyright 2007 Glencoe Software, Inc.  All Rights Reserved.
#

import db

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

	os_map = {}
	osd_map = {}
	jdk_map = {}
	total = 0
	for ip in c:
		print "IP: %16s  Starts: %6s" % (ip[0], ip[1])

		os_key = ip[2]
		osd_key = "%s %s %s" %(ip[2],ip[3],ip[4])
		jdk_key = ip[6]

		if not os_map.has_key(os_key):
			os_map[os_key]=0
		if not osd_map.has_key(osd_key):
			osd_map[osd_key]=0
		if not jdk_map.has_key(jdk_key):
			jdk_map[jdk_key]=0

		os_map[os_key] = 1 + os_map[os_key]
		osd_map[osd_key] = 1 + osd_map[osd_key]
		jdk_map[jdk_key] = 1 + jdk_map[jdk_key]
		total+=1
	print "=" * 40
	print "Total:",total
	print "=" * 40

	print "\nOperating System"
	for os_key in os_map.iterkeys():
		print "%34s = %9s ( %5.2f%% )" % (os_key, os_map[os_key], 100.0*os_map[os_key]/total)
	
	print "\nOperating System (Detailed)"
	for osd_key in osd_map.iterkeys():
		print "%34s = %9s ( %5.2f%% )" % (osd_key, osd_map[osd_key], 100.0*osd_map[osd_key]/total)
	
	print "\nJava version"
	for jdk_key in jdk_map.iterkeys():
		print "%34s = %9s ( %5.2f%% )" % (jdk_key, jdk_map[jdk_key], 100.0*jdk_map[jdk_key]/total)


accessdb = db.accessdb()
try:	
	c = accessdb.conn.cursor()

	ndays = """SELECT date(time), count(date(time))
	            FROM hit
                    WHERE ip not like '10.%%' and ip != '127.0.0.1'
		     AND agent like '%s' group by date(time) order by date(time) desc limit 30"""
	lastNDays("Last 30 days:          ", ndays % "%")
	lastNDays("Last 30 days (server): ", ndays % "OMERO.server")
	lastNDays("Last 30 days (insight):", ndays % "OMERO.insight")
	lastNDays("Last 30 days (editor): ", ndays % "OMERO.editor")
	lastNDays("Last 30 days (importer): ", ndays % "OMERO.importer")

	allip = """SELECT ip, count(ip), osname, osarch, osversion, vmvendor, vmruntime
	             FROM hit
                    WHERE ip not like '10.%%' and ip != '127.0.0.1'
		      AND agent like '%s'
		 GROUP BY ip order by count(ip) desc"""
	allIps("All IPs:          ", allip % "%")
	allIps("All IPs (server): ", allip % "OMERO.server")
	allIps("All IPs (insight):", allip % "OMERO.insight")
	allIps("All Ips (editor): ", allip % "OMERO.editor")
	allIps("All Ips (importer): ", allip % "OMERO.importer")

        weeks = [ {"editor":0,"importer":0,"insight":0,"server":0} for i in range(0,52) ]
        perweek = """ 
	           SELECT strftime('%%W', date(time)), count(ip)
                          FROM hit
                         WHERE ip not like '10.%%' and ip != '127.0.0.1'
                           AND agent like 'OMERO.%s'
                      GROUP BY agent, strftime('%%W', date(time)) """
        def perweekFor(app):
                c.execute(perweek % app)
                for week in c:
                        idx = int(week[0])
                        val = int(week[1])
                        weeks[idx][app] = val

	def total(app):
		total = 0
		for idx in range(0,52):
			total = total + weeks[idx][app]
		return total

        perweekFor("editor")
        perweekFor("importer")
        perweekFor("insight")
        perweekFor("server")

	print ""
	print "WEEKLY STARTS PER APPLICATION"
	print "="*100
        print "WEEK    \tEDITOR  \tIMPORTER\tINSIGHT \tSERVER  "
        for idx in range(0,52):
                print "%8s\t%8s\t%8s\t%8s\t%8s" % (idx, weeks[idx]["editor"], weeks[idx]["importer"], weeks[idx]["insight"], weeks[idx]["server"])
	print    "TOTAL   \t%8s\t%8s\t%8s\t%8s" % (total("editor"), total("importer"), total("insight"), total("server"))

finally:
	accessdb.close()

