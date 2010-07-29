import sys, os

sys.path.append('icepy')
sys.path.append('lib')

import Ice
import blitz_connector
import time

from omero.model import PixelsI
from datetime import datetime
from django_restapi.resource import Resource

class MyResource(Resource):

    def read(self, request):
        if os.path.exists('etc/ice.config'):
            blitz_connector.BlitzConnector.ICE_CONFIG='etc/ice.config'
        c = blitz_connector.BlitzConnector('root','ome','127.0.0.1',4064)
        c.allow_thread_timeout = False
        if not c.connect():
            return "Can not connect"
            #import sys
            #sys.exit('can not connect')
        else:
            return c.getEventContext().sessionUuid
            
    def update(self, request):
        if os.path.exists('etc/ice.config'):
            blitz_connector.BlitzConnector.ICE_CONFIG='etc/ice.config'
        c = blitz_connector.BlitzConnector('root','ome','127.0.0.1',4064)
        c.allow_thread_timeout = False
        if not c.connect():
            return "Can not connect"
            #import sys
            #sys.exit('can not connect')
        else:
            return c.getEventContext().sessionUuid