#!/usr/bin/env python

# Calculated via:
# grep enum components/model/resources/mappings/* | grep "id=" | perl -pe 's/^.*?\".*[.](.*?)\".*?$/$1/' | sort >> sql/misc/enums.py
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
import psycopg
import sys

def connect(db1, user, password):
  conn1 = psycopg.connect("host='localhost' dbname='%s' user='%s' password='%s'" % (db1,user,password))
  return conn1

def list(conn, table):
   cur = conn.cursor()
   cur.execute('select value from %s' % (table,))
   return [x[0] for x in cur.fetchall()]

def compare(conn1, conn2, table):
  l1 = list(conn1,table); l1.sort()
  l2 = list(conn2,table); l2.sort()

  output = []
  matcher = difflib.SequenceMatcher(None, l1, l2)
  for tag, i1, i2, j1, j2 in matcher.get_opcodes():
    if tag != "equal":
      output.append("  %7s l1[%d:%d] (%s) l2[%d:%d] (%s)" % (tag, i1, i2, l1[i1:i2], j1, j2, l2[j1:j2]))
  if len(output) > 0:
    print "%s : ================== " % table
    for l in output:
      print l

if __name__ == "__main__":
  if len(sys.argv) != 5:
    print "Usage: enums.py olddb newdb username password"
  else:
    a  = sys.argv
    d1 = a[1]
    d2 = a[2]
    u  = a[3]
    p  = a[4]
    conn1 = connect(d1,u,p)
    conn2 = connect(d2,u,p)

    for enum in ENUMS:
      compare(conn1, conn2, enum)

