
#
# --repeat argument for py.test taken from:
# http://stackoverflow.com/questions/21764473/
# how-can-i-repeat-each-test-multiple-times-in-a-py-test-run
#


def pytest_addoption(parser):
    parser.addoption(
        '--repeat', action='store',
        help='Number of times to repeat each test')


def pytest_generate_tests(metafunc):
    if metafunc.config.option.repeat is not None:
        count = int(metafunc.config.option.repeat)

        # We're going to duplicate these tests by parametrizing them,
        # which requires that each test has a fixture to accept the parameter.
        # We can add a new fixture like so:
        metafunc.fixturenames.append('tmp_ct')

        # Now we parametrize. This is what happens when we do e.g.,
        # @pytest.mark.parametrize('tmp_ct', range(count))
        # def test_foo(): pass
        metafunc.parametrize('tmp_ct', range(count))


class Methods(object):

    @classmethod
    def assertAlmostEqual(self, first, second,
                          places=None,
                          delta=None):
        # Copied largely from unittest
        """Fail if the two objects are unequal as determined by their
           difference rounded to the given number of decimal places
           (default 7) and comparing to zero, or by comparing that the
           between the two objects is more than the given delta.

           Note that decimal places (from zero) are usually not the same
           as significant digits (measured from the most signficant digit).

           If the two objects compare equal then they will automatically
           compare almost equal.
        """
        if first == second:
            # shortcut
            return
        if delta is not None and places is not None:
            raise TypeError("specify delta or places not both")

        if delta is not None:
            if abs(first - second) <= delta:
                return

            standardMsg = '%s != %s within %s delta' % (first,
                                                        second,
                                                        delta)
        else:
            if places is None:
                places = 7

            if round(abs(second-first), places) == 0:
                return

            standardMsg = '%s != %s within %r places' % (first,
                                                         second,
                                                         places)
        raise Exception(standardMsg)


def pytest_namespace():
    """
    Add helper methods to the 'pytest' module
    """
    return {
        "assertAlmostEqual": Methods.assertAlmostEqual
    }
