#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# $Id$
#
# Copyright 2009 Glencoe Software, Inc. All rights reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# General build scripts.

import os
import sys
import subprocess

BUILD_PY = "-Dbuild.py=true"


def popen(args, stdin=None, stdout=subprocess.PIPE, stderr=subprocess.PIPE):
    copy = os.environ.copy()
    return subprocess.Popen(args,
                            env=copy,
                            stdin=stdin,
                            stdout=stdout,
                            stderr=stderr)


def execute(args):
    p = popen(args, stdout=sys.stdout, stderr=sys.stderr)
    rc = p.wait()
    if rc != 0:
        sys.exit(rc)


def notification(msg, prio):
    """
    Provides UI notification.
    """

    # May want to revert this to be OMERO_BUILD_NOTIFICATION, or whatever.
    if "OMERO_QUIET" in os.environ or sys.platform == "win32":
        return

    try:
        p = popen(["growlnotify", "-t", "OMERO Build Status", "-p",
                   str(prio)], stdin=subprocess.PIPE)
        p.communicate(msg)
        rc = p.wait()
        if rc != 0:
            pass  # growl didn't work
    except OSError:
        pass  # No growlnotify found, may want to use another tool


def java_omero(args):
    command = [find_java()]
    p = os.path.join(os.path.curdir, "lib", "log4j-build.xml")
    command.append("-Dlog4j.configuration=%s" % p)
    command.append(BUILD_PY)
    command.extend(calculate_memory_args())
    command.extend(["omero"])
    if isinstance(args, str):
        command.append(args)
    else:
        command.extend(args)
    execute(command)


def find_java():
    return "java"


def calculate_memory_args():
    return (
        "-Xmx600M",
        "-XX:MaxPermSize=256m",
        "-XX:+IgnoreUnrecognizedVMOptions"
    )


def handle_tools(args):
    _ = os.path.sep.join
    additions = []
    mappings = {
        "-top": _(["build.xml"]),
        "-cpp": _(["components", "tools", "OmeroCpp", "build.xml"]),
        "-fs": _(["components", "tools", "OmeroFS", "build.xml"]),
        "-java": _(["components", "tools", "OmeroJava", "build.xml"]),
        "-py": _(["components", "tools", "OmeroPy", "build.xml"]),
        "-web": _(["components", "tools", "OmeroWeb", "build.xml"]),
    }
    while len(args) > 0 and args[0] in mappings.keys()+["-perf"]:
        if args[0] == "-perf":
            args.pop(0)
            A = ["-listener",
                 "net.sf.antcontrib.perf.AntPerformanceListener"]
            additions.extend(A)
        elif args[0] in mappings.keys():
            F = mappings[args.pop(0)]
            A = ["-f", F]
            additions.extend(A)
    return additions + args


def handle_relative(args):
    """
    If no other specific file has been requested,
    then use whatever relative path is needed to
    specify build.xml in the local directory.

    Regardless, os.chdir is called to the top.
    """
    additions = []
    this = os.path.abspath(__file__)
    this_dir = os.path.abspath(os.path.join(this, os.pardir))
    cwd = os.path.abspath(os.getcwd())
    os.chdir(this_dir)
    if "-f" not in args:
        build_xml = os.path.join(cwd, "build.xml")
        if os.path.exists(build_xml):
            additions.append("-f")
            additions.append(build_xml)
    return additions + args

if __name__ == "__main__":
    #
    # use java_omero which will specially configure the build system.
    #
    args = list(sys.argv)
    args.pop(0)

    # Unset CLASSPATH, since this breaks the build
    if os.environ.get('CLASSPATH'):
        del os.environ['CLASSPATH']

    try:
        args = handle_tools(args)
        args = handle_relative(args)
        java_omero(args)
        notification(""" Finished: %s """ % " ".join(args), 0)
    except KeyboardInterrupt:
        sys.stderr.write("\nCancelled by user\n")
        sys.exit(2)
    except SystemExit, se:
        notification(""" Failed: %s """ % " ".join(args), 100)
        sys.exit(se.code)
