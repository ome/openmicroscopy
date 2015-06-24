#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
    OMERO.fs Monitor module .

    Copyright 2009 University of Dundee. All rights reserved.
    Use is subject to license terms supplied in LICENSE.txt

"""
import logging
import threading

__import__("omero.all")
import omero.grid.monitors as monitors


class MonitorFactory(object):

    @staticmethod
    def createMonitor(mType, eTypes, pMode, pathString, whitelist, blacklist,
                      timeout, blockSize, ignoreSysFiles, ignoreDirEvents,
                      platformCheck, proxy, monitorId):

        if str(mType) == 'Persistent':
            return PersistentMonitor(
                eTypes, pMode, pathString, whitelist, blacklist, timeout,
                blockSize, ignoreSysFiles, ignoreDirEvents, platformCheck,
                proxy, monitorId)

        elif str(mType) == 'OneShot':
            return OneShotMonitor(
                eTypes, pMode, pathString, whitelist, blacklist, timeout,
                ignoreSysFiles, ignoreDirEvents, platformCheck, proxy,
                monitorId)

        elif str(mType) == 'Inactivity':
            return InactivityMonitor(
                eTypes, pMode, pathString, whitelist, blacklist, timeout,
                ignoreSysFiles, ignoreDirEvents, platformCheck, proxy,
                monitorId)

        else:
            raise Exception("Unknown monitor type: %s", str(mType))


class AbstractMonitor(object):

    """
        Abstract Monitor.

        :group Constructor: __init__
        :group Other methods: run, stop, callback

    """

    def __init__(self, eventTypes, pathMode, pathString, whitelist, blacklist,
                 ignoreSysFiles, ignoreDirEvents, platformCheck, proxy,
                 monitorId):
        """
            Initialise Monitor.

        """
        self.log = logging.getLogger("fsclient." + __name__)

        # Now try to import the correct MonitorServer package
        import fsUtil
        PlatformMonitor = __import__(fsUtil.monitorPackage(platformCheck))

        self.proxy = proxy
        self.monitorId = monitorId
        self.pMonitor = PlatformMonitor.PlatformMonitor(
            eventTypes, pathMode, pathString, whitelist, blacklist,
            ignoreSysFiles, ignoreDirEvents, self)

    def start(self):
        """
            Start monitoring.

            :return: No explicit return value.

        """
        raise Exception(
            'Abstract Method: must be implemented by the subclass.')

    def stop(self):
        """
            Stop monitoring

            :return: No explicit return value.

        """
        raise Exception(
            'Abstract Method: must be implemented by the subclass.')

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
        raise Exception(
            'Abstract Method: must be implemented by the subclass.')


from fsNotificationScheduler import NotificationScheduler


class PersistentMonitor(AbstractMonitor):

    """
        A Thread to monitor a path.

        :group Constructor: __init__
        :group Other methods: run, stop

    """

    def __init__(self, eventTypes, pathMode, pathString, whitelist, blacklist,
                 timeout, blockSize, ignoreSysFiles, ignoreDirEvents,
                 platformCheck, proxy, monitorId):
        """
            Initialise Monitor.

        """
        AbstractMonitor.__init__(
            self, eventTypes, pathMode, pathString, whitelist, blacklist,
            ignoreSysFiles, ignoreDirEvents, platformCheck, proxy, monitorId)

        self.notifier = NotificationScheduler(
            self.proxy, self.monitorId, timeout, blockSize)
        self.notifier.start()

    def start(self):
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

    def callback(self, eventList):
        """
            Callback used by ProcessEvent methods

            :Parameters:

                eventList : string
                File paths of the event.

            :return: No explicit return value.

        """
        self.notifier.schedule(eventList)


class InactivityMonitor(AbstractMonitor):

    """
        A Thread to monitor a path.

        :group Constructor: __init__
        :group Other methods: run, stop

    """

    def __init__(self, eventTypes, pathMode, pathString, whitelist, blacklist,
                 timeout, ignoreSysFiles, ignoreDirEvents, platformCheck,
                 proxy, monitorId):
        """
            Initialise Monitor.

        """
        AbstractMonitor.__init__(
            self, eventTypes, pathMode, pathString, whitelist, blacklist,
            ignoreSysFiles, ignoreDirEvents, platformCheck, proxy, monitorId)
        self.timer = threading.Timer(timeout, self.inactive)
        self.log.info('Inactivity monitor created. Timer: %s', str(self.timer))

    def inactive(self):
        """

        """
        self.stop()
        self.proxy.callback(
            self.monitorId, [("Inactive", monitors.EventType.System)])

    def start(self):
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
        self.proxy.callback(self.monitorId, eventList)


class OneShotMonitor(AbstractMonitor):

    """
        A Thread to monitor a path.

        :group Constructor: __init__
        :group Other methods: run, stop

    """

    def __init__(self, eventTypes, pathMode, pathString, whitelist, blacklist,
                 timeout, ignoreSysFiles, ignoreDirEvents, platformCheck,
                 proxy, monitorId):
        """
            Initialise Monitor.

        """
        AbstractMonitor.__init__(
            self, eventTypes, pathMode, pathString, whitelist, blacklist,
            ignoreSysFiles, ignoreDirEvents, platformCheck, proxy, monitorId)
        self.timer = threading.Timer(timeout, self.inactive)
        self.log.info('OneShot monitor created. Timer: %s', str(self.timer))

    def inactive(self):
        """

        """
        self.log.info('Timed out. Timer: %s', str(self.timer))
        self.stop()
        self.callback([("Timed out", monitors.EventType.System)])
        self.log.info('Stopped! Timer: %s', str(self.timer))

    def start(self):
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

    def callback(self, eventList):
        """
            Callback used by ProcessEvent methods

            :Parameters:

                id : string
                watch id.

                eventList : string
                File paths of the event.

            :return: No explicit return value.

        """
        self.log.info('File arrived. Timer: %s', str(self.timer))
        self.stop()
        self.proxy.callback(self.monitorId, eventList)
        self.log.info('Stopped! Timer: %s', str(self.timer))
