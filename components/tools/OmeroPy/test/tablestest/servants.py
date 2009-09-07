#!/usr/bin/env python

"""
   Test of the Tables facility independent of Ice.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import Ice
import exceptions
import omero, omero.tables
import unittest, os, logging

from path import path
from uuid import uuid4
from tablestest.library import TestCase

logging.basicConfig()

class communicator_provider(object):
    def __init__(self, ic = None):
        self.ic = ic
    def __call__(self, *args):
        return self.ic

class mock_communicator(object):
    def __init__(self):
        self.delegate  = Ice.initialize()
    def getProperties(self):
        return self.delegate.getProperties()
    def stringToProxy(self):
        return None

class mocked_internal_service_factory(object):
    def __init__(self, sf = None):
        if sf == None:
            sf = mocked_service_factory()
        self.sf = sf
    def __call__(self, *args, **kwargs):
        if not self.sf:
            raise exceptions.Exception("Mock error connecting to server")
        return self.sf

class mocked_service_factory(object):
    def __init__(self):
        self.db_uuid = str(uuid4())
        self.return_values = []
    def getConfigService(self):
        return mocked_config_service(self.db_uuid, self.return_values)
    def getQueryService(self):
        return mocked_query_service(self.return_values)
    def destroy(self):
        pass

class mocked_config_service(object):
    def __init__(self, db_uuid, return_values):
        self.db_uuid = db_uuid
        self.return_values = return_values
    def getDatabaseUuid(self):
        return self.db_uuid
    def getConfigValue(self, str):
        rv = self.return_values.pop(0)
        if isinstance(rv, omero.ServerError):
            raise rv
        else:
            return rv

class mocked_query_service(object):
    def __init__(self, return_values):
        self.return_values = return_values
    def findByQuery(self, *args):
        rv = self.return_values.pop(0)
        if isinstance(rv, omero.ServerError):
            raise rv
        else:
            return rv

class TestTables(TestCase):

    def setUp(self):
        TestCase.setUp(self)
        omero.util.internal_service_factory = mocked_internal_service_factory()
        self.sf = omero.util.internal_service_factory.sf
        self.communicator = communicator_provider(Ice.initialize())
        omero.tables.TablesI.communicator = self.communicator

    def repodir(self, make = True):
        tmpdir = path(self.tmpdir())
        self.communicator().getProperties().setProperty("omero.repo.dir", str(tmpdir))
        repo = tmpdir / ".omero" / "repository"
        if make:
            repo.makedirs()
        return str(repo)

    def repofile(self, db_uuid, repo_uuid):
        f = self.repodir()
        f = path(f) / db_uuid
        f.makedirs()
        f = f / "repo_uuid"
        f.write_lines(repo_uuid)

    # Note: some of the following method were added as __init__ called
    # first _get_dir() and then _get_uuid(), so the naming is off

    def testTablesIGetDirNoRepoSet(self):
        self.sf.return_values.append(self.tmpdir())
        self.assertRaises(omero.ResourceError, omero.tables.TablesI)

    def testTablesIGetDirNoRepoCreated(self):
        self.repodir(False)
        self.assertRaises(omero.ResourceError, omero.tables.TablesI)

    def testTablesIGetDirGetsRepoThenNoSF(self):
        self.repodir()
        omero.util.internal_service_factory = mocked_internal_service_factory(None)
        self.assertRaises(exceptions.Exception, omero.tables.TablesI)

    def testTablesIGetDirGetsRepoGetsSFCantFindRepoFile(self):
        self.repodir()
        self.assertRaises(exceptions.IOError, omero.tables.TablesI)

    def testTablesIGetDirGetsRepoGetsSFCantFindRepoObject(self):
        self.repofile(self.sf.db_uuid, str(uuid4()))
        self.sf.return_values.append( omero.ApiUsageException(None, None, "Cant Find") )
        self.assertRaises(omero.ApiUsageException, omero.tables.TablesI)

    def testTablesIGetDirGetsRepoGetsSFGetsRepo(self):
        self.repofile(self.sf.db_uuid, str(uuid4()))
        self.sf.return_values.append( omero.model.RepositoryI( 1, False) )
        tables = omero.tables.TablesI()

    def testTables(self):
        self.repodir()
        tables = omero.tables.TablesI()
        table = omero.tables.newTable()

    def testModificationCheckForReadOnlyFile(self):
        self.fail()

def test_suite():
    return 1

if __name__ == '__main__':
    unittest.main()

