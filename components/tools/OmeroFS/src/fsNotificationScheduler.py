#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
    OMERO.fs Notification Scheduler.

    Copyright 2009 University of Dundee. All rights reserved.
    Use is subject to license terms supplied in LICENSE.txt

"""
import logging
import threading, Queue
import time

class NotificationScheduler(threading.Thread):
    def __init__(self, proxy, monitorId, timeout=0.0, blockSize=0):
        threading.Thread.__init__(self)
        self.log = logging.getLogger("fsclient."+__name__)
        # infinite queue for first test
        self.queue = Queue.Queue(0)
        self.event = threading.Event()
        self.proxy = proxy
        self.monitorId = monitorId
        self.timeout = timeout
        self.blockSize = blockSize

    def schedule(self, eventList):
        self.queue.put_nowait(eventList)
        
    def run(self):
        self.log.info('Notification Scheduler running')
        while not self.event.isSet():
            qnow = self.queue.qsize()
            if qnow > 0:
                self.log.info('Notification queue size = %s', qnow)
                if self.blockSize != 0 and qnow >= self.blockSize:
                    qnow = self.blockSize
                notice = []
                for i in range(qnow):
                    notice += self.queue.get_nowait()
                self.log.info('Notification queue %s items removed.', qnow)
                self.log.info('Notification queue size = %s', self.queue.qsize())
                self.proxy.callback(self.monitorId, notice)
            time.sleep(0.1)
        self.log.info('Notification Scheduler stopped')
           
    def stop(self):
        self.event.set()
        
