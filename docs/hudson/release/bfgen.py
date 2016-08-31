#!/usr/bin/env python
# -*- coding: utf-8 -*-

import github
import subprocess
from doc_generator import *

fingerprint_url = "http://hudson.openmicroscopy.org.uk/fingerprint"
daily_url = "http://hudson.openmicroscopy.org.uk/job/BIOFORMATS-daily/lastSuccessfulBuild/artifact/artifacts"
trunk_url = "http://hudson.openmicroscopy.org.uk/job/BIOFORMATS-trunk/lastSuccessfulBuild/artifact/artifacts"


def usage():
    print "bfgen.py version"
    sys.exit(1)

try:
    version = sys.argv[1]
except:
    usage()

repl = {"@VERSION@": version,
        "@MONTHYEAR@": datetime.datetime.now().strftime("%b %Y")}

# (venv)jmoore@necromancer /var/www/cvs.openmicroscopy.org.uk/snapshots/bioformats/4.4.4 $ openssl md5 *
MD5s = """
MD5(bftools.zip)= ff6a326bff7c687a3ae5124e5088ce06
MD5(bio-formats.jar)= dffc404bee2ab699a642bf64ae6b1647
MD5(jai_imageio.jar)= df61b168faa6f4b017b74d6f77cc0b6a
MD5(loci-common.jar)= 4999039c4ab6894ad4baa5483212dedd
MD5(loci-testing-framework.jar)= 0b93694decdd0e3ad88e8aa63b923fd0
MD5(loci_plugins.jar)= 1abdb91e1529edf9912ab6118fe43284
MD5(loci_tools.jar)= c1be9cb77ef9ef8c35c31a7d4b67d355
MD5(lwf-stubs.jar)= 7dd5751b575a814489e24f4d3a1df230
MD5(mdbtools-java.jar)= 487b162b5b4a0c121e3973eecc887cad
MD5(metakit.jar)= 0610a135cb5476fe3830e5c8567a6c58
MD5(ome-editor.jar)= e21ad42f4e4ba9902ca88ef2e1fb5e10
MD5(ome-io.jar)= 16fc245bef5e451ee9861504f155f42b
MD5(ome-xml.jar)= 28168ab9580bf9a24ed6a9ec02468b1c
MD5(ome_plugins.jar)= ae20831ad75c8fb7105be40190eb8545
MD5(ome_tools.jar)= 39da236aa47e6f3447e01dac7f9af43e
MD5(poi-loci.jar)= 90372e92977db2a8d50c43e6ef0bb39e
MD5(scifio.jar)= f25a2e7ac7af8c5513daa0f03ac74dc2
"""
MD5s = [x.split(" ")[1] for x in MD5s.split("\n") if x.strip()]


# Creating Github instance
try:
    p = subprocess.Popen("git","config","--get","github.token", stdout = subprocess.PIPE)
    rc = p.wait()
    if rc:
        raise Exception("rc=%s" % rc)
    token = p.communicate()
except Exception:
    token = None

gh = github.Github(token)
org = gh.get_organization("openmicroscopy")
repo = org.get_repo("openmicroscopy")
for tag in repo.get_tags():
    if tag.name == ("v.%s" % version):
        break
repl["@SHA1_FULL@"] = tag.commit.sha
repl["@SHA1_SHORT@"] = tag.commit.sha[0:10]
if "STAGING" in os.environ:
    repl["@DOC_URL@"] = "https://www.openmicroscopy.org/site/support/bio-formats-staging"
else:
    repl["@DOC_URL@"] = "https://www.openmicroscopy.org/site/support/bio-formats"
repl["@PDF_URL@"] = repl["@DOC_URL@"] + "/Bio-Formats-%s.pdf" % version

if "SNAPSHOT_PATH" in os.environ:
    SNAPSHOT_PATH =  os.environ.get('SNAPSHOT_PATH')
else:
    SNAPSHOT_PATH = "/var/www/cvs.openmicroscopy.org.uk/snapshots/bioformats/"
SNAPSHOT_URL = "http://cvs.openmicroscopy.org.uk/snapshots/bioformats/"
repl["@SNAPSHOT_URL@"] = SNAPSHOT_URL


for x in ["bio-formats.jar", "scifio.jar", "bftools.zip",
         "ome_tools.jar", "ome-io.jar", "ome-xml.jar", "ome_plugins.jar", "ome-editor.jar",
         "poi-loci.jar", "jai_imageio.jar", "lwf-stubs.jar", "mdbtools-java.jar", "metakit.jar",
         "loci-common.jar", "loci_tools.jar", "loci_plugins.jar", "loci-testing-framework.jar"]:

    find_pkg(repl, fingerprint_url, SNAPSHOT_PATH, SNAPSHOT_URL, \
            x, "../bioformats/@VERSION@/%s" % x, MD5s)

    repl["@DAILY_%s@" % x] = "%s/%s" % (daily_url, x)
    repl["@TRUNK_%s@" % x] = "%s/%s" % (trunk_url, x)

for line in fileinput.input(["bftmpl.txt"]):
    print repl_all(repl, line, check_http=True),
