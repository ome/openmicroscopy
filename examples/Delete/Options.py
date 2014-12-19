#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Passes a non-empty options map to specify
that all Annotations should be kept.
"""

import omero
import omero.callbacks

c = omero.client()
s = c.createSession()

try:
    deleteServicePrx = s.getDeleteService()

    # The path here can either be relative or absolute, e.g.
    # /Image/ImageAnnotationLink/Annotation. "KEEP" specifies
    # that no delete should take place. "SOFT" specifies that
    # if a delete is not possible no error should be signaled.
    dc = omero.api.delete.DeleteCommand("/Image", 1, {"/Annotation": "KEEP"})
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
