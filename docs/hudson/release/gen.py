#!/usr/bin/env python
# -*- coding: utf-8 -*-


import os
import sys
import glob
import hashlib
import httplib
import datetime
import urlparse
import fileinput


# For calculating tags
import github

from doc_generator import *


fingerprint_url = "http://hudson.openmicroscopy.org.uk/fingerprint"
MD5s = """
MD5(4.4.5/OMERO-4.4.5.pdf)= d92d4bd40e2defb328756780c77a633e
MD5(4.4.6/OMERO-4.4.6.pdf)= 03aace065d939bb7a47a13b47cf4cc70
"""
MD5s = [x.split(" ")[1] for x in MD5s.split("\n") if x.strip()]



def usage():
    print "gen.py version build"
    sys.exit(1)

try:
    version = sys.argv[1]
    build = sys.argv[2]
except:
    usage()

repl = {"@VERSION@": version,
        "@BUILD@": build,
        "@MONTHYEAR@": datetime.datetime.now().strftime("%b %Y")}


gh = github.Github()
org = gh.get_organization("openmicroscopy")
repo = org.get_repo("openmicroscopy")
for tag in repo.get_tags():
    if tag.name == ("v.%s" % version):
        break
repl["@SHA1_FULL@"] = tag.commit.sha
repl["@SHA1_SHORT@"] = tag.commit.sha[0:10]
if "STAGING" in os.environ:
    repl["@DOC_URL@"] = "https://www.openmicroscopy.org/site/support/omero4-staging"
else:
    repl["@DOC_URL@"] = "https://www.openmicroscopy.org/site/support/omero4"

if "SNAPSHOT_PATH" in os.environ:
    SNAPSHOT_PATH =  os.environ.get('SNAPSHOT_PATH')
else:
    SNAPSHOT_PATH = "/var/www/cvs.openmicroscopy.org.uk/snapshots/omero/"
SNAPSHOT_URL = "http://cvs.openmicroscopy.org.uk/snapshots/omero/"


for x, y in (
    ("LINUX_CLIENTS", "@VERSION@/OMERO.clients-@VERSION@-ice33-@BUILD@.linux.zip"),
    ("MAC_CLIENTS", "@VERSION@/OMERO.clients-@VERSION@-ice33-@BUILD@.mac.zip"),
    ("WIN_CLIENTS", "@VERSION@/OMERO.clients-@VERSION@-ice33-@BUILD@.win.zip"),
    ("IJ_CLIENTS", "@VERSION@/OMERO.insight-ij-@VERSION@-ice33-@BUILD@.zip"),
    ("MATLAB_CLIENTS", "@VERSION@/OMERO.matlab-@VERSION@-ice33-@BUILD@.zip"),
    ("SERVER33", "@VERSION@/OMERO.server-@VERSION@-ice33-@BUILD@.zip"),
    ("SERVER34", "@VERSION@/OMERO.server-@VERSION@-ice34-@BUILD@.zip"),
    ("DOCS", "@VERSION@/OMERO.docs-@VERSION@-ice33-@BUILD@.zip"),
    ("VM", "virtualbox/omero-vm-@VERSION@.ova"),
    ("DOC", "@VERSION@/OMERO-@VERSION@.pdf")):

    find_pkg(repl, fingerprint_url, SNAPSHOT_PATH, SNAPSHOT_URL, x, y, MD5s)


for line in fileinput.input(["tmpl.txt"]):
    print repl_all(repl, line, check_http=True),
