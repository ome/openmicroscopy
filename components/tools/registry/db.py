#!/usr/bin/env python
#
# OMERO Registry Database
# Copyright 2007 Glencoe Software, Inc.  All Rights Reserved.
#
from pysqlite2 import dbapi2 as sqlite
from pprint import pprint
import os

class accessdb:

    def __init__(self, dbname='sqlite.db'):
        self.conn = sqlite.connect(dbname)

    def close(self):
        self.conn.close()

    def hit(self, ip, version, poll, vmvendor, vmruntime, osname, osarch, osversion, other):
        c = self.conn.cursor()
        c.execute('SELECT id from ip where id = ?',(ip,))
        if None == c.fetchone():
            # Now guaranteed to be in DB
            self.insertIp(ip)

        c = self.conn.cursor()
        c.execute('INSERT INTO hit VALUES (?,datetime(\'now\'),?,?,?,?,?,?,?,?)',\
            (ip, version, poll, vmvendor, vmruntime, osname, osarch, osversion, other))
        self.conn.commit()

    def insertIp(self, ipaddr):
        import GeoIP
        if not hasattr(GeoIP,"gi"):
            GeoIP.gi = GeoIP.open("GeoLiteCity.dat",GeoIP.GEOIP_STANDARD)

        try:
            gir = GeoIP.gi.record_by_addr(ipaddr)
            lat = gir["latitude"]
            lon = gir["longitude"]
        except:
            lat = "unknown"
            lon = "unknown"

        c = self.conn.cursor()
        c.execute('INSERT INTO ip VALUES (?, ?, ?)', (ipaddr, lat, lon))
        self.conn.commit()

    def __iter__(self):
        return iter(self.conn)


class iter:

    def __init__(self, conn):
        self.cursor = conn.cursor()
        self.cursor.execute('SELECT ip.latitude, ip.longitude, hit.* FROM IP ip, HIT hit where hit.ip = ip.id')

    def __iter__(self):
        return self.cursor

    def next(self):
        return self.cursor.next()

if __name__ == "__main__":
        import sys
        if len(sys.argv) == 1:
            db = accessdb()
            for hit in db:
                print hit
        else:
            dbname = sys.argv[1]
            db = accessdb(dbname)
            db.hit('127.0.0.1','TRUNK','unknown','apple','1.5','mac','i386','10.4','other')
            db.hit('134.36.65.49','TRUNK','unknown','gentoo','2007...','mac','i386','10.4','other')
            for hit in db:
                print hit


