import sys, os

sys.path.append('icepy')
sys.path.append('lib')

import blitz_connector

#
# blitz_test2.py - manhole to test and debug blitz_connector classes
# 
# Copyright (c) 2007, 2008 Glencoe Software, Inc. All rights reserved.
# 
# This software is distributed under the terms described by the LICENCE file
# you can find at the root of the distribution bundle, which states you are
# free to use it only for non commercial purposes.
# If the file is missing please request a copy by contacting
# jason@glencoesoftware.com.

#c = blitz_connector.BlitzConnector('demo1','1omed','envy.glencoesoftware.com',9998)

if os.path.exists('etc/ice.config'):
    blitz_connector.BlitzConnector.ICE_CONFIG='etc/ice.config'
c = blitz_connector.BlitzConnector('demo1','1omed','127.0.0.1',4063)
c.allow_thread_timeout = False
if not c.connect():
    print "Can not connect"
    #import sys
    #sys.exit('can not connect')
else:
    query = c.getQueryService()
    image = c.listProjects().next().listChildren().next().listChildren().next()
    channels = image.getChannels()
