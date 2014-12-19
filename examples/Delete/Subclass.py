#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Uses the default {@link DeleteCallbackI} instance.
"""

import omero
import omero.callbacks

c = omero.client()
s = c.createSession()


class Subclass(omero.callbacks.DeleteCallbackI):

    def finished(self, errors):
        omero.callbacks.DeleteCallbackI.finished(self, errors)
        print "Finished. Error count=%s" % errors

        try:
            reports = self.handle.report()
            for r in reports:
                print "Report:error=%s,warning=%s,deleted=%s" % (
                    r.error, r.warning, r.actualDeletes)

        except omero.ServerError:
            print "Something happened to the handle?!?"


try:
    deleteServicePrx = s.getDeleteService()
    dc = omero.api.delete.DeleteCommand("/Image", 1, None)
    deleteHandlePrx = deleteServicePrx .queueDelete([dc])
    cb = Subclass(c, deleteHandlePrx)

    try:

        try:

            cb.loop(10, 500)
            # If we reach here, finished() was called.

        except omero.LockTimeout:
            print "Not finished in 5 seconds. Cancelling..."
            if not deleteHandlePrx.cancel():
                print "ERROR: Failed to cancel"

    finally:
        cb.close()

finally:
    c.closeSession()
