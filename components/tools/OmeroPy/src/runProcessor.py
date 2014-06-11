#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# OMERO Processor Runner
# Copyright 2009 Glencoe Software, Inc.  All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import os
import sys
import Ice
import omero

from omero.processor import ProcessorI

impl = os.environ.get("OMERO_PROCESSOR", "")
servant = ProcessorI.load_class(impl, ProcessorI)

if __name__ == "__main__":
    app = omero.util.Server(servant, "ProcessorAdapter", Ice.Identity("Processor",""))
    sys.exit(app.main(sys.argv))

