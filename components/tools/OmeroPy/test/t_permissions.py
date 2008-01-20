import unittest, omero.model, omero_model_PermissionsI

class TestPermissions(unittest.TestCase):

    def setUp(self):
        self.p = omero.model.PermissionsI()

    def testperm1(self):
        # The default
        self.assert_( self.p.isUserRead() )
        self.assert_( self.p.isUserWrite() )
        self.assert_( self.p.isGroupRead() )
        self.assert_( not  self.p.isGroupWrite() )
        self.assert_( self.p.isWorldRead() )
        self.assert_( not  self.p.isWorldWrite() )
        self.assert_( not  self.p.isLocked() ) # flags reversed

        # All off
        self.p.perm1 = 0L
        self.assert_( not  self.p.isUserRead() )
        self.assert_( not  self.p.isUserWrite() )
        self.assert_( not  self.p.isGroupRead() )
        self.assert_( not  self.p.isGroupWrite() )
        self.assert_( not  self.p.isWorldRead() )
        self.assert_( not  self.p.isWorldWrite() )
        self.assert_( self.p.isLocked() ) # flags reversed

        # All on
        self.p.perm1 = -1L
        self.assert_( self.p.isUserRead() )
        self.assert_( self.p.isUserWrite() )
        self.assert_( self.p.isGroupRead() )
        self.assert_( self.p.isGroupWrite() )
        self.assert_( self.p.isWorldRead() )
        self.assert_( self.p.isWorldWrite() )
        self.assert_( not  self.p.isLocked() ) # flags reversed

        # Various swaps
        self.p.setUserRead(False)
        self.assert_( not self.p.isUserRead() )
        self.p.setGroupWrite(True)
        self.assert_( self.p.isGroupWrite() )
        self.p.setLocked(True)
        self.assert_( self.p.isLocked() )

        # Now reverse each of the above
        self.p.setUserRead(True)
        self.assert_( self.p.isUserRead() )
        self.p.setGroupWrite(False)
        self.assert_( not self.p.isGroupWrite() )
        self.p.setLocked(False)
        self.assert_( not self.p.isLocked() )

if __name__ == '__main__':
    unittest.main()
