#!/usr/bin/env python
#
# $Id$
#
# Copyright 2009 Glencoe Software, Inc. All rights reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# General build scripts.

import os
import sys
import time
import subprocess

def notification(msg, prio):
    """
    Provides UI notification.
    """

    # May want to revert this to be OMERO_BUILD_NOTIFICATION, or whatever.
    if "OMERO_QUIET" in os.environ:
        return

    try:
        p = subprocess.Popen(["growlnotify","-t","OMERO Build Status","-p",str(prio)],\
            stdin=subprocess.PIPE,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
        p.communicate(msg)
        rc = p.wait()
        if rc != 0:
            pass # growl didn't work
    except OSError:
        pass # No growlnotify found, may want to use another tool

def build_hudson():
    """
    Top-level build called by hudson for testing all components,
    generating documentation, etc.
    """
    #
    # Cleaning to prevent strange hudson errors about
    # stale tests and general weirdness.
    #
    java_omero("clean")

    # Build & Test
    java_omero("build-all")
    java_omero("test-integration")
    java_omero("test-dist")

    #
    # Documentation and build reports
    #
    java_omero("release-docs")
    java_omero("release-findbugs")
    ## java_omero("release-jdepend") ## Doesn't yet work. Running from hudson

    #
    # Prepare a distribution
    #
    "rm -f OMERO.server-build*.zip"
    java_omero("release-zip")

    # Install into the hudson repository
    ## Disabling until 4.1 with more work
    ## on integration
    ##java_omero("release-hudson")


def java_omero(args):
    command = [ find_java() ]
    p = os.path.join( os.path.curdir, "lib", "log4j-build.xml")
    command.append("-Dlog4j.configuration=%s" % p)
    command.extend( calculate_memory_args() )
    command.extend(["omero"])
    command.extend(choose_omero_version())
    if isinstance(args,str):
        command.append(args)
    else:
        command.extend(args)
    execute(command)

def find_java():
    return "java"

def calculate_memory_args():
    return "-Xmx600M -Djavac.maxmem=600M -Djavadoc.maxmem=600M -XX:MaxPermSize=256m".split(" ")

def choose_omero_version():
    """
    Returns an array specifying the build parameter for
    ant. Returned as an array so that an empty value can
    be extended into the build command.

    If OMERO_BULID is set, then "-Domero.version=${omero-version}-${OMERO_BUILD}"
    otherwise nothing.
    """

    omero_build = os.environ.get("OMERO_BUILD", "")
    if omero_build:
        omero_build = "-%s" % omero_build

    command = [ find_java(), "omero","-q","version" ]
    try:
        p = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        omero_version,err = p.communicate()
        omero_version = omero_version.split()[1]
        return [ "-Domero.version=%s%s" % (omero_version, omero_build) ]
    except:
        print "Error getting version for OMERO_BUILD=%s" % omero_build
        print err
        sys.exit(rc)

def execute(args):
    rc = subprocess.call(args)
    if rc != 0:
	sys.exit(rc)


if __name__ == "__main__":
    #
    # If this is a hudson build, then call the special build_hudson
    # method. Otherwise, use java_omero which will specially configure
    # the build system.
    #
    args = list(sys.argv)
    args.pop(0)

    try:
        if len(args) > 0 and args[0] == "-hudson":
            build_hudson()
        elif len(args) > 0 and args[0] == "-perf":
            args.pop(0)
            A = "-listener net.sf.antcontrib.perf.AntPerformanceListener".split() + args
            java_omero(A)
        else:
            java_omero(args)
        notification(""" Finished: %s """ % " ".join(args), 0)
    except KeyboardInterrupt:
        sys.stderr.write("\nCancelled by user\n")
        sys.exit(2)
    except SystemExit, se:
        notification(""" Failed: %s """ % " ".join(args), 100)
        sys.exit(se.code)
