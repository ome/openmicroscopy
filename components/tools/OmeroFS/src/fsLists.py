#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
    OMERO.fs List classes

    These are fairly trivial classes to wrap sets. In the present
    implementation there is no functional difference between
    Whitelist and Blacklist. They have been factored out for literal
    reasons and to leave the door open for future changes.

    Copyright 2009 University of Dundee. All rights reserved.
    Use is subject to license terms supplied in LICENSE.txt

"""


class Greylist(object):

    """
        An abstract class representing a general list as a set.

        It's neither black nor white! It delegates behaviour to
        the Python set type using that data structure's feature
        of having no duplicated items.

        :group Constructor: __init__
        :group Other methods: add, remove, asList
        :group Query methods: onList
        :group String Representation: __repr__

    """

    def __init__(self, initialList=None):
        """
            Create an initial initial list.

            :Parameters:
                initialList : list<string>
                    An initial list of items.

        """
        if initialList is None:
            initialList = []
        self.theSet = set(initialList)

    def __repr__(self):
        """
            Return the string representation of the list.

            :return: String representation of the string.
            :rtype: string

        """
        return str(self.theSet)

    def add(self, anItem):
        """
            Add an item to the list.

            As a set is being used duplicates don't occur and
            add fails to add duplicates silently.

            :Parameters:
                anItem : string
                    Any string.

            :return: No explicit return value.

            """
        self.theSet.add(anItem)

    def remove(self, anItem):
        """
            Remove an item from the list.

            Items that are not on the list fail to be removed
            silently.

            :Parameters:
                anItem : string
                    Any string.

            :return: No explicit return value.

        """
        self.theSet.discard(anItem)

    def asList(self):
        """
            Return the contents of the set as a list.

            The list will not be ordered in any way.

            :return: List representation of the set.
            :rtype: list<string>

        """
        return list(self.theSet)

    def onList(self, anItem):
        """
            Return whether an item is on the list.

            :Parameters:
                anItem : string
                    A string.

            :return: Existence of item on list.
            :rtype: boolean

        """
        if len(self.theSet) == 0:
            return True
        else:
            return anItem in self.theSet


class Whitelist(Greylist):

    """
        A class representing a whitelist of file extensions
        that should be included in any watching.

        :group Constructor: __init__

    """

    def __init__(self, initialList=None):
        """
            Create an initial whitelist.

            :Parameters:
                initialList : list<string>
                    An initial list of extensions.

        """
        if initialList is None:
            initialList = []
        Greylist.__init__(self, initialList)


class Blacklist(Greylist):

    """
        A class representing a blacklist of paths that should be
        excluded from any watching.

        :group Constructor: __init__

    """

    def __init__(self, initialList=None):
        """
            Create an initial blacklist.

            :Parameters:
                initialList : list<string>
                    An initial list of subdirectories.

        """
        if initialList is None:
            initialList = []
        Greylist.__init__(self, initialList)
