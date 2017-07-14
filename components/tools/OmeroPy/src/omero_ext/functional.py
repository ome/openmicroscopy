from functools import wraps
import warnings

assert wraps  # silence pyflakes

warnings.warn("the omero_ext.functional module will be removed in OMERO 5.6",
              DeprecationWarning, stacklevel=2)
