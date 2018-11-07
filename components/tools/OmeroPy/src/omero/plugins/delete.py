#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   Startup plugin for command-line deletes

   Copyright 2009-2015 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import sys

from omero.cli import CLI, GraphControl

HELP = """Delete OMERO data.

Remove entire graphs of data based on the ID of the top-node.

By default linked tag, file and term annotations are not deleted.
To delete linked annotations they must be explicitly included.

Examples:

    # Delete an image but not its linked tag, file and term annotations
    omero delete Image:50
    # Delete an image including linked tag, file and term annotations
    omero delete Image:51 --include TagAnnotation,FileAnnotation,TermAnnotation
    # Delete an image including all linked annotations
    omero delete Image:52 --include Annotation

    # Delete three images and two datasets including their contents
    omero delete Image:101,102,103 Dataset:201,202
    # Delete five images and four datasets including their contents
    # Note that --force flag is required when deleting a range, if not
    # passed, a dry run is performed
    omero delete Image:106-110 Dataset:203-205,207 --force
    # Delete a project excluding contained datasets and linked annotations
    omero delete Project:101 --exclude Dataset,Annotation

    # Delete all images contained under a project
    omero delete Project/Dataset/Image:53
    # Delete all images contained under two projects
    omero delete Project/Image:201,202

    # Do a dry run of a delete reporting the outcome if the delete had been run
    omero delete Dataset:53 --dry-run
    # Do a dry run of a delete, reporting all the objects
    # that would have been deleted
    omero delete Dataset:53 --dry-run --report

"""


class DeleteControl(GraphControl):

    def cmd_type(self):
        import omero
        import omero.all
        return omero.cmd.Delete2

    def print_detailed_report(self, req, rsp, status):
        import omero
        if isinstance(rsp, omero.cmd.DoAllRsp):
            for response in rsp.responses:
                if isinstance(response, omero.cmd.Delete2Response):
                    self.print_delete_response(response)
        elif isinstance(rsp, omero.cmd.Delete2Response):
            self.print_delete_response(rsp)

    def print_delete_response(self, rsp):
        if rsp.deletedObjects:
            self.ctx.out("Deleted objects")
            objIds = self._get_object_ids(rsp.deletedObjects)
            for k in objIds:
                self.ctx.out("  %s:%s" % (k, objIds[k]))

    def default_exclude(self):
        """
        Don't delete these three types of Annotation by default
        """
        return ["TagAnnotation", "TermAnnotation", "FileAnnotation"]


try:
    register("delete", DeleteControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("delete", DeleteControl, HELP)
        cli.invoke(sys.argv[1:])
