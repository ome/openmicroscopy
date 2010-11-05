#!/usr/bin/env python
#
# $Id$
#
# Copyright 2010 Glencoe Software, Inc. All rights reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# Hudson launcher script which properly launches the script
# on the right system. This is used by most jobs via:
#
#   cd src
#   cd docs
#   cd hudson
#   python launcher.py
#
# which will:
#
#   * download <BRANCH>.log from hudson
#   * create target/hudson.log
#   * run sh docs/hudson/OMERO-<BRANCH>-<COMPONENT>.sh
#      or docs\hudson\OMERO-<BRANCH>-<COMPONENT>.bat
#

import os
import re
import sys
import urllib
import platform
import subprocess


LOG_URL = "http://hudson.openmicroscopy.org.uk/job/OMERO-%(BRANCH)s/lastSuccessfulBuild/artifact/src/target/%(BRANCH)s"
JOB_NAME_STR = "^OMERO-([^-]+)-(.*?)/(.*)$"
JOB_NAME_REG = re.compile(JOB_NAME_STR)


if __name__ == "__main__":

    #
    # FIND JOB NAME
    #
    job_name = os.environ["JOB_NAME"]
    m = JOB_NAME_REG.match(job_name)
    if not m:
        print "Bad job name: %s doesn't match %r" % (job_name, JOB_NAME_STR)
        sys.exit(1)

    branch = m.group(1)
    axises = m.group(3)
    values = {}
    for axis in axises.split(","):
        parts = axis.split("=")
        values[parts[0]] = parts[1]
    job = values["component"]
    label = values["label"]

    top = os.path.join(os.pardir, os.pardir)
    target = os.path.join(top, "target")
    if not os.path.exists(target):
        os.makedirs(target)
    build_log = os.path.join(target, "%s.log" % branch)
    hudson_log = os.path.join(target, "hudson.log")


    #
    # LOG FILES
    #
    url = urllib.urlopen(LOG_URL % {"BRANCH": branch})
    build_log_text = url.read()
    url.close()

    f = open(build_log, "w")
    f.write(build_log_text)
    f.close()

    f = open(hudson_log, "w")
    for key in sorted(os.environ):
        f.write("%s=%s\n" % (key, os.environ[key]))
    f.close


    #
    # BUILD COMMAND
    #
    path = os.path.join("docs", "hudson")
    base = "OMERO-%s" % job
    if "Windows" == platform.system():
        name = base + ".bat"
        cmd = []
    else:
        name = base + ".sh"
        cmd = ["sh"]
    path = os.path.join(path, name)
    cmd.append(path)

    #
    # RUN
    #
    print "Launching", " ".join(cmd)
    print "="*60
    popen = subprocess.Popen(cmd,\
        cwd = top,
        env = os.environ)
    rcode = popen.wait()
    if rcode != 0:
        print "="*60
        print "Build failed with rcode=%s" % rcode
    sys.exit(rcode)

