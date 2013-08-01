import omero
from omero.rtypes import rstring

#from omero.gateway.scripts import dbhelpers
from omero.gateway.scripts.testdb_create import *


import pytest

class GatewayWrapper (TestDBHelper):
    def __init__ (self):
        super(GatewayWrapper, self).__init__()
        self.setUp(skipTestDB=False, skipTestImages=True)

    def createTestImg_generated (self):
        ds = self.getTestDataset()
        assert ds
        testimg = self.createTestImage(dataset=ds)
        return testimg


@pytest.fixture(scope='module')
def gatewaywrapper (request):
    """
    Returns a test helper gateway object.
    """
    g = GatewayWrapper()
    def fin ():
        g.tearDown()
        dbhelpers.cleanup()
    request.addfinalizer(fin)
    return g


@pytest.fixture(scope='function')
def author_testimg_generated (gatewaywrapper):
    """
    logs in as Author and returns the test image, creating it first if needed.
    """
    gatewaywrapper.loginAsAuthor()
    return gatewaywrapper.createTestImg_generated()
