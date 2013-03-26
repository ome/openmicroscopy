#!/usr/bin/env python
# -*- coding: utf-8 -*-
import omero
from omero.rtypes import *

# Sets and Lists may be interpreted differently on the server
list = rlist(rstring("a"), rstring("b"));
set = rset(rint(1), rint(2));
