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
#   * create hudson.log
#   * run sh docs/hudson/OMERO-<BRANCH>-<COMPONENT>.sh
#      or    docs\hudson\OMERO-<BRANCH>-<COMPONENT>.bat
#

import os
import re
import sys
import urllib
import platform
import subprocess
import exceptions


LOG_URL = "http://hudson.openmicroscopy.org.uk/job/OMERO-%(BRANCH)s/lastSuccessfulBuild/artifact/src/target/%(BRANCH)s.log"
JOB_NAME_STR = "^OMERO-([^-]+)-(.*?)(/(.*))?$"
JOB_NAME_REG = re.compile(JOB_NAME_STR)


class ConfigOpener(urllib.FancyURLopener):
    def http_error_default(self, url, fp, errcode, errmsg, headers):
        if errcode and errcode > 400:
             raise exceptions.Exception("Error loading %s: %s" % (url, errcode))


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
    build = m.group(2)
    axises = m.group(4)
    if axises:
        values = {}
        for axis in axises.split(","):
            parts = axis.split("=")
            values[parts[0]] = parts[1]
        job = values["component"]
        label = values["label"]
    else:
        job = build

    #
    # SETUP
    #
    os.chdir("..") # docs
    os.chdir("..") # OMERO_HOME
    top = os.path.abspath(".")
    build_log = os.path.join(top, "%s.log" % branch)
    hudson_log = os.path.join(top, "hudson.log")
    config_file = os.path.join(top, "%s.config" % branch)


    #
    # LOG FILES
    #
    log_url = LOG_URL % {"BRANCH": branch}
    print "Loading %s ..." % log_url
    url = urllib.urlopen(log_url)
    build_log_text = url.read()
    url.close()

    f = open(build_log, "w")
    for line in build_log_text.split("\n"):
        f.write(line)
        f.write("\n")
        # Also import the file into the environment
        line = line.strip()
        parts = line.split("=")
        if parts and parts[0]:
            k = str(parts[0])
            try:
                v = str(parts[1])
                os.environ[k] = v
            except:
                os.environ[k] = ""
    f.close()

    f = open(hudson_log, "w")
    for key in sorted(os.environ):
        f.write("%s=%s\n" % (key, os.environ[key]))
    f.close

    #
    # CONFIG FILE
    # -----------
    # If this is not the "start" job, then download
    # the <BRANCH>.config file created by start in
    # order to access the server.
    #
    if axises and job != "start":
        build_url = os.environ["BUILD_URL"]
        build_url = build_url.replace("component=%s" % job, "component=start")
        # These jobs don't have their own
        # "start" component, so let them use
        # the "linux" label.
        if label == "macosx" or label == "matlab":
            build_url = build_url.replace("label=%s" % label, "label=linux")
        build_url = "%s/%s" % (build_url, "artifact/src/%s.config" % branch)
        if os.path.exists(config_file):
            print "Removing %s ..." % config_file
            os.remove(config_file)
        print "Downloading %s ... " % build_url
        ConfigOpener().retrieve(build_url, filename=config_file)
        os.environ["ICE_CONFIG"] = config_file


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
    popen = subprocess.Popen(cmd, env = os.environ)
    rcode = popen.wait()
    if rcode != 0:
        print "="*60
        print "Build failed with rcode=%s" % rcode
    sys.exit(rcode)

