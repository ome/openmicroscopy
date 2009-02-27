#
#   $Id$
#
#   Copyright 2008 Glencoe Software, Inc. All rights reserved.
#   Use is subject to license terms supplied in LICENSE.txt
#

import os, glob, exceptions, subprocess
from SCons.Script.SConscript import *
from SCons.SConf import *

#
# Global Directories
#
dir = os.path.abspath( os.path.dirname( __file__ ) )
top = os.path.abspath( os.path.join( dir, os.path.pardir, os.path.pardir ) )
slice_directory = os.path.abspath( os.path.join( top, "target", "Ice", "slice" ) )
blitz_resources = os.path.abspath( os.path.join( top, "components", "blitz", "resources") )
blitz_generated = os.path.abspath( os.path.join( top, "components", "blitz", "generated") )
tools_include = os.path.abspath( os.path.join( top, "components", "tools", "target", "include" ) )
tools_library = os.path.abspath( os.path.join( top, "components", "tools", "target", "lib" ) )
omerocpp_dir = os.path.abspath( os.path.join( top, "components", "tools", "OmeroCpp") )
# Relative
resources = os.path.abspath("resources")
generated = os.path.abspath("generated")
# Support ICE_HOME
if os.environ.has_key("ICE_HOME"):
    ice_home = os.path.abspath( os.environ["ICE_HOME"] )
else:
    ice_home = None

def jdep(DEPMAP, target):
    """
    Uses a map named "DEPMAP" to look map from Java file
    names which don't exist ("omero/API.ice" -> "omero/API.java")
    to real Globs ("omero/api/*.java")
    """
    try:
        return DEPMAP[target]
    except KeyError:
        return target

#
# Helpers
#

def common(dir = generated):
    """
    Necessary since output for C++ does not include directories.
    """
    return ["-I%s" % generated, "-I%s" % resources, "-I%s" % slice_directory, "--output-dir=%s" % dir]

def names(dir, ice):
    basename = os.path.basename(ice)[:-4]
    filename = '%s/%s' % (dir, basename)
    return (basename, filename)

def basenames(where, dir):
    for ice in glob.glob("%s/%s/*.ice" % (where, dir)):
        yield names(dir, ice)

#
# Define calls to slice2java, slice2cpp, and slice2py
#

def make_slice(command):
    def slice(target, source, env):
        args = command+[str(source[0].get_abspath())]
        rv = subprocess.call(args)
        if rv != 0:
            raise exceptions.Exception("%s returned %s" % (" ".join(args), rv))
    return slice

def slice_cpp(env, where, dir):
    command = ["slice2cpp", "--include-dir=%s"%dir] + common( "%s/%s" % (generated, dir) )
    actions = []
    for basename, filename in basenames(where, dir):
        c = env.Command(
            [filename + '.h', filename + '.cpp'],                 # target
            filename + '.ice',                                    # source
            make_slice(command),                                  # command
            chdir = where )                                       # dir
        actions.append( c )
    return actions

def slice_java(env, where, dir):
    command  = ["slice2java", "--tie"] + common()
    actions = []
    for basename, filename in basenames(where, dir):
        c = env.Command(
            jdep(env["DEPMAP"], filename + '.java' ),             # target
            filename + '.ice',                                    # source
            make_slice(command),                                  # command
            chdir = where )                                       # dir
        actions.append( c )
    return actions

def slice_py(env, where, dir):
    prefix = dir.replace("/","_") + "_"
    command = ["slice2py", "--prefix", "%s" % prefix ] + common()
    actions = []
    for basename, filename in basenames(where, dir):
        c = env.Command(
            [prefix + basename + '_ice.py' ],                      # target
            filename + '.ice',                                     # source
            make_slice(command),                                  # command
            chdir = where )                                        # dir
        actions.append( c )
    return actions

#
# Lists which can be used in a cross-product to generate
# all necessary files.
#
methods = [slice_java, slice_cpp, slice_py]
directories = ["omero", "omero/model", "omero/api"]
where = [generated, resources]


class OmeroEnvironment(SConsEnvironment):
    """
    Wrapper class around a scons environment for properly setting up the
    build environment
    """

    def __init__(self, **kwargs):
        SConsEnvironment.__init__(self, **kwargs)
        self.Decider('MD5-timestamp')
        self["ENV"] = os.environ
        win32 = self["PLATFORM"] == "win32"
        # Print statements
        if not os.environ.has_key("VERBOSE"):
            self.Replace(CXXCOMSTR  = "Compiling $TARGET")
            self.Replace(LINKCOMSTR = "Linking   $TARGET")
            self.Replace(SHCXXCOMSTR  = "Compiling $TARGET")
            self.Replace(SHLINKCOMSTR = "Linking   $TARGET")
        # CXXFLAGS
        self.Append(CPPFLAGS="-D_REENTRANT")
        if os.environ.has_key("CXXFLAGS"):
            self.Append(CPPFLAGS=self.Split(os.environ["CXXFLAGS"]))
        else:
            if not win32:
                self.Append(CPPFLAGS=self.Split("-O0 -g -Wall"))
                self.Append(CPPFLAGS=self.Split("-ansi"))
                # self.Append(CPPFLAGS=self.Split("-pedantic -ansi")) Ice fails pedantic due to extra ";"
                self.Append(CPPFLAGS=self.Split("-Wno-long-long -Wnon-virtual-dtor"))
                # self.Append(CPPFLAGS=self.Split("-Wno-long-long -Wctor-dtor-privacy -Wnon-virtual-dtor")) Ice fails the ctor check.
                self.Append(CPPFLAGS=self.Split("-Wno-unused-parameter -Wno-unused-function -Wunused-variable -Wunused-value -Werror"))
        # CPPPATH
        self.AppendUnique(CPPPATH=blitz_generated)
        if ice_home:
            self.Append(CPPPATH=os.path.join(ice_home, "include"))
        if os.environ.has_key("CPPPATH"):
            self.AppendUnique(CPPPATH=os.environ["CPPPATH"].split(os.path.pathsep))
        if win32:
            ice_win = "C:\\Ice-3.3.0\\include"
            if os.path.exists(ice_win):
                self.AppendUnique(ice_win)
        else:
            if os.path.exists("/opt/local/include"):
                self.AppendUnique(CPPPATH=["/opt/local/include"])
        # LIBPATH
        self.AppendUnique(LIBPATH=omerocpp_dir)
        if ice_home:
            self.Append(LIBPATH=[os.path.join(ice_home, "lib")])
        if os.environ.has_key("LIBPATH"):
            self.AppendUnique(LIBPATH=os.environ["LIBPATH"].split(os.path.pathsep))
        if os.path.exists("/opt/local/lib"):
            self.AppendUnique(LIBPATH=["/opt/local/lib"])

