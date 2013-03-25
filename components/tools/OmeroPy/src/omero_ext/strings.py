#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Python string utilities.

Josh Moore, josh at glencoesoftware.com
Copyright (c) 2008, Glencoe Software, Inc.
See LICENSE for details.

"""

import shlex as pyshlex

def shlex(input):
    """
    Used to split a string argument via shlex.split(). If the
    argument is not a string, then it is returned unchnaged.
    This is useful since the arg argument to all plugins can
    be either a list or a string.
    """
    if None == input:
        return []
    elif isinstance(input, str):
        return pyshlex.split(input)
    else:
        return input
