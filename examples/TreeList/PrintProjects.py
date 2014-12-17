#!/usr/bin/env python
# -*- coding: utf-8 -*-


def print_(projects):
    for project in projects:
        print project.getName().val
        for pdl in project.copyDatasetLinks():
            dataset = pdl.getChild()
            print "  " + dataset.getName().val
