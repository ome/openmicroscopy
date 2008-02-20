#!/usr/bin/env python
#
# OMERO Registry Database
# Copyright 2007 Glencoe Software, Inc.  All Rights Reserved.
#

import db, GeoIP

def doIp(db, ipaddr):
        if not hasattr(GeoIP,"gi"):
            GeoIP.gi = GeoIP.open("GeoLiteCity.dat",GeoIP.GEOIP_STANDARD)

        try:
            print ipaddr,
            gir = GeoIP.gi.record_by_addr(ipaddr)
            lat = gir["latitude"]
            lon = gir["longitude"]
	    print lat, lon
        except:
	    print "unknown"
            lat = "unknown"
            lon = "unknown"

	c2 = db.conn.cursor()
	c2.execute("UPDATE ip SET latitude = ?, longitude = ? WHERE id = ?", (lat, lon, ipaddr))

try:
	db = db.accessdb()
	c  = db.conn.cursor()
	c.execute('SELECT id FROM ip')
	for ip in c:
		doIp(db, ip[0])
	db.conn.commit()
finally:
	db.close()

