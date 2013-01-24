#
#   $Id$
#
#   Copyright 2008 Glencoe Software, Inc. All rights reserved.
#   Use is subject to license terms supplied in LICENSE.txt
#

import sys, os, glob, time
blitz = os.path.abspath( os.path.join(os.path.curdir, os.path.pardir, os.path.pardir, "blitz") )
sys.path.append( blitz )
from blitz_tools import *

#
# At the moment, execution of this script requires,
# ant tools-init to have been run
#

env = OmeroEnvironment(CPPPATH=["src","target"])

if not env.GetOption('clean'):
    conf = Configure(env)
    if not conf.CheckCXXHeader(os.path.join("Ice","Ice.h")):
        print 'Ice/Ice.h not found'
        env.Exit(1)
    conf.Finish()

f = open("scons.log", "w")
f.write(env.Dump())
f.close()

compiler_env = dict()
compiler_env["timestamp"] = time.ctime()
compiler_env["ARCH"] = (env.is64bit() and "64" or "32")
compiler_env["PLATFORM"] = env.get("PLATFORM", "unknown")
compiler_env["RELEASE"] = (env.isdebug() and "dbg" or "rel")
compiler_env["LIBPATH"] = env.get("LIBPATH","unknown")
compiler_env["CPPPATH"] = env.get("CPPPATH","unknown")
compiler_env["CPPFLAGS"] = env.get("CPPFLAGS","unknown")
try:
    compiler_env["CXX"] = env["CC"] # Windows
except KeyError:
    compiler_env["CXX"] = env.get("CXX","unknown")
# Handle the absolute path required on Windows
compiler_env["CXX"] = os.path.basename(compiler_env["CXX"]).split(".")[0]

try:
    compiler_env["CXXVERSION"] = env["MSVS_VERSION"] # Windows
except KeyError:
    compiler_env["CXXVERSION"] = env.get("CXXVERSION", "unknown")

f = open("compiler.log", "w")
f.write("""
#
# Scons Compile Log : %(timestamp)s
#
CPPFLAGS=%(CPPFLAGS)s
CPPPATH=%(CPPPATH)s
CXX=%(CXX)s
CXXVERSION=%(CXXVERSION)s
LIBPATH=%(LIBPATH)s
ARCH=%(ARCH)s
RELEASE=%(RELEASE)s
PLATFORM=%(PLATFORM)s
""" % compiler_env)
f.close()

#
# Build the library
#
srcs = env.Glob("target/**/**/*.cpp") + \
       env.Glob("target/**/*.cpp") + \
       env.Glob("src/**/**/*.cpp") + \
       env.Glob("src/**/*.cpp")

target = "omero_client"
if env.iswin32():
    env.AppendUnique(CXXFLAGS=["/Fdomero_client.pdb"])
    target += ".dll"

library = env.SharedLibrary(\
    target = target,
    source = srcs,
    LIBS = env.icelibs())
env.Alias('lib', library)

#
# Utilities
#
uenv = env.Clone()
for x in  uenv.Glob("utils/*.cpp"):
    suffix = x.suffix
    base = str(x)[0:-4]
    exe = uenv.iswin32() and ".exe" or ""
    util = uenv.Program("%s%s" % (base, exe), x, LIBS = ["omero_client"] + env.icelibs())

#
# Visual Studio
#

if target.endswith("dll"):
    if not hasattr(env, "MSVSProject") and env.GetOption('clean'):
        pass # Allow cleaning even without Visual Studio installed
    else:
        msproj = env.MSVSProject(target = 'omero_client' + env['MSVSPROJECTSUFFIX'],
            srcs = [ str(x) for x in srcs ],
            buildtarget = library[0],
            variant = 'Release')
        env.Alias('msproj', msproj)

        install = env.Install('target/lib', library[0])
        install2 = env.Install('target/lib', library[1])
        env.Depends(install, install2)
        env.Alias('install', install)
else:
    install = env.Install('target/lib', library)
    env.Alias('install', install)

#
# Build tests
#
tenv = env.Clone()
tenv["CPPPATH"].append("test")

gtest = tenv.Object("test/gtest/gtest-all.cc")
main = tenv.Object("test/gtest/gtest_main.cc")
fixture = tenv.Object("test/omero/fixture.cpp")

def define_test(dir):
    exe = uenv.iswin32() and ".exe" or ""
    bin = "test/%s/%s%s" % (dir,dir,exe)
    test =  tenv.Program(bin,
        [gtest, main, fixture] + tenv.Glob("test/%s/*.cpp" % dir),
        LIBS = ["omero_client"]+env.icelibs())
    return test

unit = define_test("unit")
integration = define_test("integration")
