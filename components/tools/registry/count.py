#!/usr/bin/env python
#
# OMERO Registry Database Count
# Copyright 2007 Glencoe Software, Inc.  All Rights Reserved.
#

import db

def os_info(title, query):
	print "\n"
	c.execute(query)

	print "="*40
	print title
	print "="*40

	os_map = {}
	osd_map = {}
	jdk_map = {}
	total = 0
	for ip in c:
		# No longer printing ips here, but rather later from csv
		# print "IP: %16s  Starts: %6s" % (ip[0], ip[1])

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
		total = total + 1

	print "\nOperating System"
	for os_key in os_map.iterkeys():
		print "%34s = %9s ( %5.2f%% )" % (os_key, os_map[os_key], 100.0*os_map[os_key]/total)
	
	print "\nOperating System (Detailed)"
	for osd_key in osd_map.iterkeys():
		print "%34s = %9s ( %5.2f%% )" % (osd_key, osd_map[osd_key], 100.0*osd_map[osd_key]/total)
	
	print "\nJava version"
	for jdk_key in jdk_map.iterkeys():
		print "%34s = %9s ( %5.2f%% )" % (jdk_key, jdk_map[jdk_key], 100.0*jdk_map[jdk_key]/total)



# This list should always be in sync with the csv method
applications = ["editor","importer","insight","server"]

def csv(title, col1, data, keys):
	print ""
	print title
	print "="*120
        print "%8s\t  EDITOR\tIMPORTER\t INSIGHT\t  SERVER\tTOTAL"% col1
	totals = [0,0,0,0]
        for idx in keys:
		values = []
		for m in [data["editor"], data["importer"], data["insight"], data["server"]]:
			try:
				values.append(int(m[idx]))
			except KeyError:
				values.append(0)
		for jdx in range(0,4):
			totals[jdx] = totals[jdx] + values[jdx]
		values.append( sum(values) )
		values.insert( 0, idx)
                print "%8s\t%8s\t%8s\t%8s\t%8s\t%8s" % tuple(values)
	totals.append(sum(totals))
	print    "TOTAL   \t%8s\t%8s\t%8s\t%8s\t%8s" % tuple(totals)
	
accessdb = db.accessdb()
try:
	c = accessdb.conn.cursor()

	#
	# LAST 30 DAYS
	#
	ndays = """SELECT date(time), count(date(time))
	            FROM hit
                    WHERE ip not like '10.%%' and ip != '127.0.0.1'
		     AND agent = 'OMERO.%s'
                GROUP BY date(time)
		ORDER BY date(time) desc limit 30"""
	m = {}
	def doDay(app):
		c.execute(ndays % app)
		m[app] = {}
		for day in c:
			m[app][day[0]] = day[1]
		return m

	for app in applications: doDay(app)
	title = "DAILY STARTS PER APPLICATION FOR LAST 30 DAYS"
	col1 = "DAY"
	# Making a set of all keys for when not all apps start on each day
	s = set()
	for app in applications: s.update( set(m[app].keys()) )
	keys = list(s)
	keys.sort()
	keys.reverse()
	csv(title, col1, m, keys)
	
	#
	# PERWEEK
	#
        weeks = {}
	for app in applications:
		weeks[app] = [ 0 for idx in range(0,52) ]
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
                        weeks[app][idx] = val

	for app in applications: perweekFor(app)
	title = "WEEKLY STARTS PER APPLICATION"
	col1 = "WEEK"
	csv(title, col1, weeks, range(0,52))

	#
	# IPS
	#
	allip = """SELECT ip, count(ip), osname, osarch, osversion, vmvendor, vmruntime
	             FROM hit
                    WHERE ip not like '10.%%' and ip != '127.0.0.1'
		      AND agent like 'OMERO.%s'
		 GROUP BY ip order by count(ip) desc"""
	ipcounts = {}
	ips = set()
	for app in applications: ipcounts[app] = {}
	def perip(app):
		c.execute(allip % app)	
		for ip in c:
			ips.add(ip[0])
			ipcounts[app][ip[0]] = int(ip[1])
	for app in applications: perip(app)
	ips = list(ips)
	def mysort(a,b):
		a = [int(i) for i in a.split(".")]
		b = [int(i) for i in b.split(".")]
		for i in range(0,4):
			t = cmp(a[i],b[i])
			if t != 0:
				return t
		return 0
	ips.sort(mysort)
	csv("STARTS PER IP ADDRESS", "IP", ipcounts, ips)
	
	#
	# Print os_info
	#
	os_info("OS Info (combined): ", allip % "%")
	for app in applications:
		os_info("OS INFO (%s)"%app, allip % app)

finally:
	accessdb.close()
