#
#   $Id$
#
#   Copyright 2008 Glencoe Software, Inc. All rights reserved.
#   Use is subject to license terms supplied in LICENSE.txt
#

import os, glob

#
# At the moment, execution of this script requires,
# ant tools-init to have been run
#

env = Environment(
    CPPPATH = ['/opt/local/include','src','target'],
    ENV = os.environ )
env.Decider('MD5-timestamp')

#
# Build the library
#
library = env.SharedLibrary("omero_client",
    env.Glob("target/**/**/*.cpp") +
    env.Glob("target/**/*.cpp") +
    env.Glob("src/**/**/*.cpp") +
    env.Glob("src/**/*.cpp"),
    LIBS=["Ice","Glacier2","IceUtil"],
    LIBPATH=".:/opt/local/lib")
env.Alias('lib', library)

install = env.Install('../target/lib', library)
env.Alias('install', install)


#
# Build tests
#

tenv = env.Clone()
tenv["CPPPATH"].append("test")
tenv["ENV"]["BOOST_TEST_DYN_LINK"] = "1"

main = tenv.Object("test/boost_main.cpp")
fixture = tenv.Object("test/boost_fixture.cpp")

def define_test(dir):
    test =  tenv.Program("test/%s.exe" % dir,
        [main, fixture] + tenv.Glob("test/%s/*.cpp" % dir),
        LIBS=["Ice","Glacier2","IceUtil","omero_client","boost_unit_test_framework-mt-d"],
        LIBPATH=".:/opt/local/lib")
    return test

unit = define_test("unit")
integration = define_test("integration")
