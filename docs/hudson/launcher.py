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

import os
import re
import sys
import platform
import subprocess


JOB_NAME_STR = "^OMERO-([^-]+)-(.*)$"
JOB_NAME_REG = re.compile(JOB_NAME_STR)


if __name__ == "__main__":

    top = os.path.join(os.pardir, os.pardir)
    hudson_log = os.path.join(top, "target", "hudson.log")

    #
    # LOG PROPERTIES
    #
    f = open(hudson_log, "w")
    for key in sorted(os.environ):
        f.write("%s=%s\n" % (key, os.environ[key]))
    f.close

    #
    # FIND JOB NAME
    #
    job_name = os.environ["JOB_NAME"]
    m = JOB_NAME_REG.match(job_name)
    if not m:
        print "Bad job name: %s doesn't match %r" % (job_name, JOB_NAME_STR)
        sys.exit(1)
    else:
        job = m.group(2)

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

