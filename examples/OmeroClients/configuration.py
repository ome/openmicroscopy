#!/usr/bin/env python
# -*- coding: utf-8 -*-
import omero
import Ice

# All configuration in file pointed to by
# --Ice.Config=file.config or ICE_CONFIG
# environment variable;
# No username, password entered
try:
    client1 = omero.client()
    client1.createSession()
    client1.closeSession()
except Ice.ConnectionRefusedException:
    pass  # Bad address or port?

# Most basic configuration.
# Uses default port 4064
# createSession needs username and password
try:
    client2 = omero.client("localhost")
    client2.createSession("root", "ome")
    client2.closeSession()
except Ice.ConnectionRefusedException:
    pass  # Bad address or port?

# Configuration with port information
try:
    client3 = omero.client("localhost", 24064)
    client3.createSession("root", "ome")
    client3.closeSession()
except Ice.ConnectionRefusedException:
    pass  # Bad address or port?

# Advanced configuration can also be done
# via an InitializationData instance.
data = Ice.InitializationData()
data.properties = Ice.createProperties()
data.properties.setProperty("omero.host", "localhost")
try:
    client4 = omero.client(data)
    client4.createSession("root", "ome")
    client4.closeSession()
except Ice.ConnectionRefusedException:
    pass  # Bad address or port?

# Or alternatively via a dict instance
m = {"omero.host": "localhost",
     "omero.user": "root",
     "omero.pass": "ome"}
client5 = omero.client(m)
# Again, no username or password needed
# since present in the map. But they *can*
# be overridden.
try:
    client5.createSession()
    client5.closeSession()
except Ice.ConnectionRefusedException:
    pass  # Bad address or port?
