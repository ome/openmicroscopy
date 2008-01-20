import unittest

class TopLevel(unittest.TestCase):
    pass

def additional_tests():
    load = unittest.defaultTestLoader.loadTestsFromName
    suite = unittest.TestSuite()
    suite.addTest(load("test.t_model"))
    suite.addTest(load("test.t_permissions"))
    return suite
