#!/usr/bin/env python
# -*- coding: utf-8 -*-


import sys
import fileinput

def usage():
    print "gen.py version directory"
    sys.exit(1)

try:
    version = sys.argv[1]
    directory = sys.argv[2]
except:
    usage()

for line in fileinput.input(["tmpl.txt"]):
    print line.replace("@VERSION@", version),

