"""
    OMERO.fs Notification Scheduler.


"""
import logging
import fsLogger
log = logging.getLogger("fsserver."+__name__)

import threading, Queue
import time

class NotificationScheduler(threading.Thread):
    def __init__(self, proxy, monitorId, timeout=0.0, blockSize=1):
        threading.Thread.__init__(self)
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
        log.info('Notification Scheduler running')
        while not self.event.isSet():
            qnow = self.queue.qsize()
            if qnow > 0:
                log.info('Notification queue size = %s', qnow)
                if qnow >= self.blockSize:
                    qnow = self.blockSize
                notice = []
                for i in range(qnow):
                    notice += self.queue.get_nowait()
                log.info('Notification queue %s items removed.', qnow)
                log.info('Notification queue size = %s', self.queue.qsize())
                self.proxy.callback(self.monitorId, notice)
            time.sleep(0.1)
        log.info('Notification Scheduler stopped')
           
    def stop(self):
        self.event.set()
        
