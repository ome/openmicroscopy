#!/usr/bin/env python
"""
   Startup plugin for command-line deletes

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
import sys
import array
import exceptions

from omero.cli import BaseControl, CLI

HELP = """Delete OMERO data.

Where available (currently: Image & Plate) special methods
are used for deleting the objects. Otherwise, IUpdate.deleteObject()
is used.


Examples:

    bin/omero delete Image:50
    bin/omero delete Plate:1

    # New-style delete
    bin/omero delete /Image:1 /Screen:5

"""

class DeleteControl(BaseControl):

    def _configure(self, parser):
        parser.add_argument("--wait", type=long, help="""Number of seconds to wait for the delete to complete (Indefinite < 0; No wait=0).""", default=-1)
        parser.add_argument("--list", action="store_true", help="""Print a list of all available delete specs""")
        parser.add_argument("--list-details", action="store_true", help="""Print a list of all available delete specs along with detailed info""")
        parser.add_argument("--report", action="store_true", help="""Print more detailed report of each delete""")
        parser.add_argument("obj", nargs="*", help="""Objects to be deleted in the form "<Class>:<Id>""")
        parser.set_defaults(func=self.delete)

    def delete(self, args):

        import omero
        import omero.callbacks

        client = self.ctx.conn(args)
        delete = client.sf.getDeleteService()
        specs = delete.availableCommands()
        keys = sorted([spec.type for spec in specs])

        if args.list_details:
            map = dict()
            for spec in specs:
                map[spec.type] = spec
            for key in keys:
                spec = map[key]
                self.ctx.out("=== %s ===" % key)
                for k, v in spec.options.items():
                    self.ctx.out("%s" % (k,))
        elif args.list:
            self.ctx.out("\n".join(keys))
            return # Early exit.

        images = []
        plates = []
        objects = []
        commands = []
        for arg in args.obj:
            if 0 > arg.find(":"):
                self.ctx.die(5, "Format: 'Image:<id>'")
            klass, id = arg.split(":")
            if klass == "Image":
                images.append(long(id))
            elif klass == "Plate":
                plates.append(long(id))
            elif klass in keys:
                commands.append(omero.api.delete.DeleteCommand(klass, long(id), None))
            else:
                try:
                    ctor = getattr(omero.model, "%sI" % klass)
                except AttributeError:
                    self.ctx.die(6, "Unknown delete command: %s" % klass)
                if not ctor:
                    ctor = getattr(omero.model, klass)
                try:
                    objects.append(ctor(long(id), False))
                except exceptions.Exception, e:
                    self.ctx.dbg("Exception on ctor: %s" % e)
                    self.ctx.die(5, "Can't delete type: %s" % klass)

        def action(klass, method, *args):
            import omero
            self.status(klass, args)
            try:
                method(*args)
                self.ctx.out("ok.")
            except omero.ApiUsageException, aue:
                self.ctx.out(aue.message)
            except exceptions.Exception, e:
                self.ctx.out("failed (%s)" % e)

        deleteSrv = client.getSession().getDeleteService()
        updateSrv = client.getSession().getUpdateService()
        for image in images: action("Image", deleteSrv.deleteImage, image, True)
        for plate in plates: action("Plate", deleteSrv.deletePlate, plate)
        for object in objects: action(object.__class__.__name__, updateSrv.deleteObject, object)

        handle = deleteSrv.queueDelete(commands)
        if args.wait == 0:
            self.ctx.out("Not waiting for delete")
            return
        elif args.wait > 0:
            self.ctx.die(321, "Unsupported wait: %s" % args.wait)

        callback = omero.callbacks.DeleteCallbackI(client, handle)
        try:
            try:
                # Wait for finish
                rv = None
                while True:
                    rv = callback.block(500)
                    if rv is not None:
                        break

                reports = handle.report()
                if args.report:
                    self.detailed_report(rv, reports)
                else:
                    self.simple_report(rv, reports)

                if rv:
                    self.ctx.die(rv, "Failed")

            # If user uses Ctrl-C, then cancel
            except KeyboardInterrupt:
                self.ctx.out("Attempting cancel...")
                if handle.cancel():
                    self.ctx.out("Cancelled")
                else:
                    self.ctx.out("Failed to cancel")
        finally:
            callback.close()

    def status(self, klass, args):
        self.ctx.out(("Deleting %s %s... " % (klass, args)), newline = False)

    def simple_report(self, rv, reports):
        for i, report in enumerate(reports):
            command = report.command
            self.status(command.type, command.id)
            if rv:
                msg = "error"
            else:
                msg = "ok"
            self.ctx.out(msg)

            msg = ""
            if report.error:
                which = "error"
                msg = report.error
            elif report.warning:
                which = "warning"
                msg = report.warning

            for line in msg.split("\n"):
                line = line.strip()
                if line:
                    self.ctx.out("\t%s: %s" % (which, line))

    def detailed_report(self, rv, reports):
        for i, report in enumerate(reports):
            command = report.command
            self.status(command.type, command.id)
            if rv:
                msg = "error"
            else:
                msg = "ok"
            self.ctx.out(msg)

            for k, v in report.undeletedFiles.items():
                if v:
                    self.ctx.out("Undeleted %s objects" % k)
                    for i in v:
                        self.ctx.out("%s:%s" % (k, i))


            self.ctx.out("Steps: %s" % report.steps)
            self.ctx.out("Scheduled deletes: %s" % report.scheduledDeletes)
            self.ctx.out("Actual deletes: %s" % report.actualDeletes)
            if report.stop > 0 and report.start > 0:
                elapse = report.stop - report.start
                self.ctx.out("Elapsed time: %s secs." % (elapse/1000.0))
            else:
                self.ctx.out("Unfinished.")

            if report.warning:
                self.ctx.out("Warning message: %s" % report.warning)

            if report.error:
                self.ctx.out("Error message: %s" % report.error)

            self.ctx.out(" ")

try:
    register("delete", DeleteControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("delete", DeleteControl, HELP)
        cli.invoke(sys.argv[1:])
