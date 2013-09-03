#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
    Set up script

    These are fairly trivial script that creates a Project/Dataset
    and import the images into the newly created datasets.
    this is only for the root user.
    
    Copyright 2013 University of Dundee. All rights reserved.
    Use is subject to license terms supplied in LICENSE.txt

"""

import sys
import omero
import omero.cli
from omero.gateway import BlitzGateway
from omero.rtypes import wrap
from omero.model import DatasetI, ProjectI


def create_containers(cli, dataset, project=None):
    """
    Creates containers with names provided if they don't exist already.
    Returns Dataset ID.
    """
    sessionId = cli._event_context.sessionUuid
    conn = BlitzGateway(host='localhost')
    conn.connect(sUuid = sessionId)
    params = omero.sys.Parameters()
    params.theFilter = omero.sys.Filter()
    params.theFilter.ownerId = wrap(conn.getUser().getId())

    d = None
    prId = None
    p = conn.getObject("Project", attributes={'name': project}, params=params)
    if p is None:
        print "Creating Project:", project
        p = omero.model.ProjectI()
        p.name = wrap(project)
        prId = conn.getUpdateService().saveAndReturnObject(p).id.val
    else:
        print "Using Project:", project, p
        prId = p.getId()
        # Since Project already exists, check children for Dataset
        for c in p.listChildren():
            if c.getName() == dataset:
                d = c

    if d is None:
        d = conn.getObject("Dataset", attributes={'name': dataset}, params=params)

    if d is None:
        print "Creating Dataset:", dataset
        d = omero.model.DatasetI()
        d.name = wrap(dataset)
        dsId = conn.getUpdateService().saveAndReturnObject(d).id.val
        if prId is not None:
            print "Linking Project-Dataset..."
            link = omero.model.ProjectDatasetLinkI()
            link.child = omero.model.DatasetI(dsId, False)
            link.parent = omero.model.ProjectI(prId, False)
            conn.getUpdateService().saveObject(link)
    else:
        print "Using Dataset:", dataset, d
        dsId = d.getId()
    return dsId

#create a session and import an image in a project

def main(args):
    #filename=args[0];
    cli = omero.cli.CLI()
    cli.loadplugins()
    cli.invoke(["login", "root@localhost", "-w", "omero", "-C"], strict=True)
    import_args = ["import"]
    dsId = create_containers(cli, "dataset with images", "project with a dataset")
    import_args.extend(["-d", str(dsId)])
    import_args.append(args[0])
    print import_args
    cli.invoke(import_args, strict=True)


if __name__ == '__main__':
    main(sys.argv[1:])