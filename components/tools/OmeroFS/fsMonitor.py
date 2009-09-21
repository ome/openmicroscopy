"""
    OMERO.fs Monitor module for Window XP.


"""
import logging
import fsLogger
log = logging.getLogger("fsserver."+__name__)

import threading
import os, sys, traceback
import uuid

# Third party path package. It provides much of the 
# functionality of os.path but without the complexity.
# Imported as pathModule to avoid clashes.
import path as pathModule

# Now try to import the correct MonitorServer package
import fsUtil
try:
    PlatformMonitor = __import__(fsUtil.monitorPackage())            
except:
    raise


from omero.grid import monitors

class MonitorFactory(object):
    @staticmethod
    def createMonitor(mType, eType, pMode, pathString, whitelist, blacklist, timeout, ignoreSysFiles, self, monitorId):
        if str(mType) == 'Persistent':
            return PersistentMonitor(eType, pMode, pathString, whitelist, blacklist, ignoreSysFiles, self, monitorId)
        elif str(mType) == 'OneShot':
            return OneShotMonitor(eType, pMode, pathString, whitelist, blacklist, timeout, ignoreSysFiles, self, monitorId)
        elif str(mType) == 'Inactivity':
            return InactivityMonitor(eType, pMode, pathString, whitelist, blacklist, timeout, ignoreSysFiles, self, monitorId)
        else:
            raise Exception("Unkown monitor type: %s", str(mType))
        

class Monitor(threading.Thread):
    """
        Abstract Monitor.

        :group Constructor: __init__
        :group Other methods: run, stop, callback

    """
    def __init__(self, proxy, monitorId):
        """
            Initialise Monitor.

        """
        threading.Thread.__init__(self)
 
        self.proxy = proxy
        self.monitorId = monitorId
        self.pMonitor = PlatformMonitor.PlatformMonitor()


    def run(self):
        """
            Start monitoring.

            :return: No explicit return value.

        """
        raise Exception('Abstract Method: must be implemented by the subclass.')

    def stop(self):
        """
            Stop monitoring 
            
            :return: No explicit return value.
            
        """
        raise Exception('Abstract Method: must be implemented by the subclass.')

    def callback(self, monitorId, eventPath):
        """
            Callback used by ProcessEvent methods

            :Parameters:

                id : string
                watch id.

                eventPath : string
                File paths of the event.

            :return: No explicit return value.

        """
        raise Exception('Abstract Method: must be implemented by the subclass.')


class PersistentMonitor(Monitor):
    """
        A Thread to monitor a path.
        
        :group Constructor: __init__
        :group Other methods: run, stop

    """
    def __init__(self, eventType, pathMode, pathString, whitelist, blacklist, ignoreSysFiles, proxy, monitorId):
        """
            Initialise Monitor.
                                 
        """
        Monitor.__init__(self, proxy, monitorId)
        self.pMonitor.setUp(eventType, pathMode, pathString, whitelist, blacklist, ignoreSysFiles, monitorId, self)

    def run(self):
        """
            Start monitoring.
            
            :return: No explicit return value.
            
        """
        self.pMonitor.start()

    def stop(self):
        """
            Stop monitoring 
            
            :return: No explicit return value.
            
        """
        self.pMonitor.stop()

    def callback(self, monitorId, eventList):
        """
            Callback used by ProcessEvent methods

            :Parameters:

                id : string
                watch id.

                eventList : string
                File paths of the event.

            :return: No explicit return value.

        """
        self.proxy.callback(monitorId, eventList)


class InactivityMonitor(Monitor):
    """
        A Thread to monitor a path.
        
        :group Constructor: __init__
        :group Other methods: run, stop

    """
    def __init__(self, eventType, pathMode, pathString, whitelist, blacklist, timeout, ignoreSysFiles, proxy, monitorId):
        """
            Initialise Monitor.
                                 
        """
        Monitor.__init__(self, proxy, monitorId)
        self.pMonitor.setUp(eventType, pathMode, pathString, whitelist, blacklist, ignoreSysFiles, monitorId, self)
        self.timer = threading.Timer(timeout, self.inactive)
        log.info('Inactivity monitor created. Timer: %s', str(self.timer))

    def inactive(self):
        """
        
        """
        self.stop()
        self.proxy.callback(self.monitorId, [("Inactive", monitors.EventType.System)])
        
    def run(self):
        """
            Start monitoring.
            
            :return: No explicit return value.
            
        """
        self.timer.start()
        self.pMonitor.start()

    def stop(self):
        """
            Stop monitoring 
            
            :return: No explicit return value.
            
        """
        try:
            self.timer.cancel()
        except:
            pass

        self.pMonitor.stop()

    def callback(self, monitorId, eventList):
        """
            Callback used by ProcessEvent methods

            :Parameters:

                id : string
                watch id.

                eventList : string
                File paths of the event.

            :return: No explicit return value.

        """
        self.stop()
        self.proxy.callback(monitorId, eventList)


class OneShotMonitor(Monitor):
    """
        A Thread to monitor a path.
        
        :group Constructor: __init__
        :group Other methods: run, stop

    """
    def __init__(self, eventType, pathMode, pathString, whitelist, blacklist, timeout, ignoreSysFiles, proxy, monitorId):
        """
            Initialise Monitor.
                                 
        """
        Monitor.__init__(self, proxy, monitorId)
        self.pMonitor.setUp(eventType, pathMode, pathString, whitelist, blacklist, ignoreSysFiles, monitorId, self)
        self.timer = threading.Timer(timeout, self.inactive)
        log.info('OneShot monitor created. Timer: %s', str(self.timer))

    def inactive(self):
        """
        
        """
        log.info('Timed out. Timer: %s', str(self.timer))
        self.stop()
        self.proxy.callback(self.monitorId, [("Timed out", monitors.EventType.System)])
        log.info('Stopped! Timer: %s', str(self.timer))
        
    def run(self):
        """
            Start monitoring.
            
            :return: No explicit return value.
            
        """
        self.timer.start()
        self.pMonitor.start()

    def stop(self):
        """
            Stop monitoring 
            
            :return: No explicit return value.
            
        """
        try:
            self.timer.cancel()
        except:
            pass

        self.pMonitor.stop()

    def callback(self, monitorId, eventList):
        """
            Callback used by ProcessEvent methods

            :Parameters:

                id : string
                watch id.

                eventList : string
                File paths of the event.

            :return: No explicit return value.

        """
        log.info('File arrived. Timer: %s', str(self.timer))
        self.stop()
        self.proxy.callback(monitorId, eventList)
        log.info('Stopped! Timer: %s', str(self.timer))

