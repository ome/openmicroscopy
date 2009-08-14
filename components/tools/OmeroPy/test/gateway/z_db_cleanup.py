from library import *

class TDTest(GTest):
    def setUp (self):
        super(TDTest, self).setUp(skipTestDB=True)
        dbhelpers.cleanup()

    def runTest (self):
        pass

if __name__ == '__main__':
    unittest.main()
