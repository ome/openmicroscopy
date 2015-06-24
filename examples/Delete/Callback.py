#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Uses the default {@link DeleteCallbackI} instance.
"""

import omero
import omero.callbacks

c = omero.client()
s = c.createSession()

try:
    deleteServicePrx = s.getDeleteService()
    dc = omero.api.delete.DeleteCommand("/Image", 1, None)
    deleteHandlePrx = deleteServicePrx .queueDelete([dc])
    cb = omero.callbacks.DeleteCallbackI(c, deleteHandlePrx)

    try:

        try:
            cb.loop(10, 500)
        except omero.LockTimeout:
            print "Not finished in 5 seconds. Cancelling..."
            if not deleteHandlePrx.cancel():
                print "ERROR: Failed to cancel"

        reports = deleteHandlePrx.report()
        r = reports[0]  # We only sent one command
        print "Report:error=%s,warning=%s,deleted=%s" % \
            (r.error, r.warning, r.actualDeletes)

    finally:
        cb.close()

finally:
    c.closeSession()
