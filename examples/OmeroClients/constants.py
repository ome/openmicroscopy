#!/usr/bin/env python
# -*- coding: utf-8 -*-
from omero.constants import MESSAGESIZEMAX, CONNECTTIMEOUT

print "By default, no method call can pass more than %s kb" % MESSAGESIZEMAX
print ("By default, client.createSession() will wait %s seconds for a"
       " connection" % (CONNECTTIMEOUT/1000))
