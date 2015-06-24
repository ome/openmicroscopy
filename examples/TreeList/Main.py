#!/usr/bin/env python
# -*- coding: utf-8 -*-
import sys
import omero
import Usage
import AllProjects
import PrintProjects

if __name__ == "__main__":
    try:
        host = sys.argv[1]
        port = sys.argv[2]
        user = sys.argv[3]
        pasw = sys.argv[4]
    except:
        Usage.usage()

    client = omero.client(sys.argv)
    try:
        factory = client.createSession(user, pasw)
        projects = AllProjects.getProjects(factory.getQueryService(), user)
        PrintProjects.print_(projects)
    finally:
        client.closeSession()
