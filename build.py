#!/usr/bin/env python
#
# $Id$
#
# Copyright 2009 Glencoe Software, Inc. All rights reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# General build scripts.

import sys
import subprocess

def build_hudson():
    """
    Top-level build called by hudson for testing the 
    """
    #
    # Cleaning to prevent strange hudson errors about
    # stale tests and general weirdness.
    #
    java_omero("clean")

    # Build & Test
    java_omero("build-all")
    java_omero("test-integration")

    #
    # Documentation and build reports
    #
    java_omero("release-javadoc")
    java_omero("release-findbugs")

    #
    # Prepare a distribution
    #
    "rm -f OMERO.server-build*.zip"
    java_omero("release-zip")

    # Install into the hudson repository
    java_omero("release-hudson")


def java_omero(args):
    command = [ find_java() ]
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
    """
    # Otherwise -Domero.version=buildBUILD_NUMBER
    return [] # Use default

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
        else:
            java_omero(args)
    except KeyboardInterrupt:
        sys.stderr.write("\nCancelled by user\n")
        sys.exit(2)
