#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2011 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
   Example test of the new omero.cmd API
"""

from integration.library import ITest


class CmdTest(ITest):

    def testConnection(self):
        import Ice
        ic = Ice.initialize()

        from omero_cmd import SessionI
        session = Session() # username, password, host, etc from config
        session.connect()
        session.disconnect()

    def testCommands(self):
        sess = getSession()
        client = SyncClient(session)

        create = Create(type="Project", {"name": "test"})
        response = client.call(create) # , loop=10

        modify = Modify(type="Project", id=response.id, {"name": "test2"})
        sess.submit(modify)

        query = ListQuery("select d from Dataset d")
        response = client.call(query)

        link = Link(parent="Project:1", child=response.list[0])
        handle = sess.submit(link)

        pop = PopStatus(10, include=[FAILURE])
        response = client.call(pop) # Removes from queue

        findHandles = FindHandles(include=[DONE], exclude=[SUCCESS])
        response = client.call(findHandles)

        response = sess.call(ListCommands())

        # Multi-call
        requests = RequestList(list=[ListCommands(), ListCommands(),ListCommands()])
        responses = sess.call(req_list).list

if __name__ == '__main__':
    unittest.main()
