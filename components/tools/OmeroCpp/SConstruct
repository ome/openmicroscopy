#
#   $Id$
#
#   Copyright 2008 Glencoe Software, Inc. All rights reserved.
#   Use is subject to license terms supplied in LICENSE.txt
#

import sys, os, glob
blitz = os.path.abspath( os.path.join(os.path.curdir, os.path.pardir, os.path.pardir, "blitz") )
sys.path.append( blitz )
from blitz_tools import *

BOOST_DEBUG="boost_unit_test_framework-mt-d"
BOOST_NDEBUG="boost_unit_test_framework-mt"

#
# At the moment, execution of this script requires,
# ant tools-init to have been run
#

boost_check = """
#define BOOST_TEST_MAIN
#define BOOST_TEST_DYN_LINK
#include <boost/test/included/unit_test.hpp>
"""
def CheckBoost(context):
    context.Message('Checking for boost_unit_test...')
    result = context.TryLink(boost_check, '.cpp')
    context.Result(result)
    return result

boost_libs = []

env = OmeroEnvironment(CPPPATH=["src","target"])
if not env.GetOption('clean'):
    conf = Configure(env, custom_tests = {'CheckBoost':CheckBoost})
    if not conf.CheckCXXHeader("Ice/Ice.h"):
        print 'Ice/Ice.h not found'
        env.Exit(1)
    has_boost = conf.CheckBoost()
    if has_boost:
    	if conf.CheckLib(BOOST_DEBUG):
	    boost_libs.append(BOOST_DEBUG)
	elif conf.CheckLib(BOOST_NDEBUG):
	    boost_libs.append(BOOST_NDEBUG)
	else:
	    print "Has boost but doesn't have boost?"
            env.Exit(1)
    conf.Finish()
else:
    has_boost = True

#
# Build the library
#
library = env.SharedLibrary("omero_client",
    env.Glob("target/**/**/*.cpp") +
    env.Glob("target/**/*.cpp") +
    env.Glob("src/**/**/*.cpp") +
    env.Glob("src/**/*.cpp"),
    LIBS=["Ice","Glacier2","IceUtil"])
env.Alias('lib', library)

install = env.Install('../target/lib', library)
env.Alias('install', install)


#
# Build tests
#
if has_boost:
    tenv = env.Clone()
    tenv["CPPPATH"].append("test")
    tenv["ENV"]["BOOST_TEST_DYN_LINK"] = "1"

    main = tenv.Object("test/boost_main.cpp")
    fixture = tenv.Object("test/boost_fixture.cpp")

    def define_test(dir):
        test =  tenv.Program("test/%s.exe" % dir,
            [main, fixture] + tenv.Glob("test/%s/*.cpp" % dir),
            LIBS=["Ice","Glacier2","IceUtil","omero_client"]+boost_libs)
        return test

    unit = define_test("unit")
    integration = define_test("integration")
