try:
    from unittests import *
except ImportError:
    # Happens if the PYTHONPATH is not correct, which is what happens when all you are
    # trying to import is seleniumbase
    raise
