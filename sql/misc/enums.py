#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Calculated via:
# grep enum components/model/resources/mappings/* | grep "id=" | perl -pe
# 's/^.*?\".*[.](.*?)\".*?$/$1/' | sort >> sql/misc/enums.py
ENUMS = """
AcquisitionMode
ArcType
Binning
ContrastMethod
Correction
DetectorType
DimensionOrder
EventType
EventType
ExperimentType
Family
FilamentType
FilterType
Format
Illumination
Immersion
JobStatus
LaserMedium
LaserType
Medium
MicrobeamManipulationType
MicroscopeType
PhotometricInterpretation
PixelsType
Pulse
RenderingModel
""".split()
ENUMS.sort()

import difflib
import psycopg2
import sys


def connect(db1, user, password):
    conn1 = psycopg2.connect(
        "host='localhost' dbname='%s' user='%s' password='%s'"
        % (db1, user, password))
    return conn1


def list(conn, table):
    cur = conn.cursor()
    cur.execute("select * from pg_tables where schemaname='public' and"
                " tablename ilike '%s'" % table)
    if len(cur.fetchall()) == 0:
        return [], True
    else:
        cur.execute('select value from %s' % table)
        return [x[0] for x in cur.fetchall()], False


def compare(conn1, conn2, table):
    l1, l1_deleted = list(conn1, table)
    l1.sort()
    l2, l2_deleted = list(conn2, table)
    l2.sort()

    output = []
    matcher = difflib.SequenceMatcher(None, l1, l2)
    for tag, i1, i2, j1, j2 in matcher.get_opcodes():
        if tag != "equal":
            output.append("  %7s l1[%d:%d] (%s) l2[%d:%d] (%s)"
                          % (tag, i1, i2, l1[i1:i2], j1, j2, l2[j1:j2]))
    if len(output) > 0:
        print "%s : ================== " % table
        if l1_deleted:
            print " ** ADDED ** "
        elif l2_deleted:
            print " ** DELETED ** "
        for l in output:
            print l

if __name__ == "__main__":
    if len(sys.argv) != 5:
        print "Usage: enums.py olddb newdb username password"
    else:
        a = sys.argv
        d1 = a[1]
        d2 = a[2]
        u = a[3]
        p = a[4]
        conn1 = connect(d1, u, p)
        print "From: %s" % conn1
        conn2 = connect(d2, u, p)
        print "To:   %s" % conn2

        for enum in ENUMS:
            compare(conn1, conn2, enum)
