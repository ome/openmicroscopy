#
#   $Id$
#
#   Copyright 2008 Glencoe Software, Inc. All rights reserved.
#   Use is subject to license terms supplied in LICENSE.txt
#

import os, glob
from blitz_tools import *

env = OmeroEnvironment()
env.Repository(["generated","resources"])

#
# JAVA: since Ice doesn't (and can't really) create a single
# Java file per ice file, mapping from ice input to java output
# is difficult, and requires extra configuration. This map is
# used by the jdep() method from blitz_tools
#

env["DEPMAP"] = {
    "omero/fwd.java" : "unknon",
    "omero/API.java" : "omero/api/ServiceFactoryPrx.java",
    "omero/Constants.java" : "omero/constants/USERNAME.java",
    "omero/Collections.java" : "omero/api/ServiceListHolder.java",
    "omero/ROMIO.java" : "omero/romio/RedBand.java",
    "omero/RTypes.java" : "omero/RType.java",
    "omero/Scripts.java" : "omero/grid/Param.java",
    "omero/ServerErrors.java" : "omero/ServerError.java",
    "omero/ServicesF.java" : "omero/api/ServiceInterfacePrx.java",
    "omero/System.java" : "omero/sys/Parameters.java"
}


#
# Call slice2java and slice2py
#

actions = []
for m in methods:
    for w in where:
        for d in directories:
            actions.extend( m(env, w, d) )

env.Alias("slice", actions)
