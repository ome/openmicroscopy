#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""

   Function for parsing OMERO log files.
   The format expected is defined for Python in
   omero.util.configure_logging.

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

   :author: Josh Moore <josh@glencoesoftware.com>

"""

import numpy as np
import matplotlib.pyplot as plt
import matplotlib.lines as lines
import matplotlib.transforms as mtransforms
import matplotlib.text as mtext

from time import mktime, strptime

import fileinput
import logging
import sys
import os
import re

def parse_time(value):
    """
    parse the time format used by log4j into seconds (float)
    since the epoch
    """
    parts = value.split(",")
    value = parts[0]
    millis = float(parts[1]) / 1000.0
    t = mktime(strptime(value, "%Y-%m-%d %H:%M:%S"))
    t = float(t)
    t += millis
    return t

class log_line(object):
    """
    2009-04-09 15:11:58,029 INFO  [        ome.services.util.ServiceHandler] (l.Server-6)  Meth:    interface ome.api.IQuery.findByQuery
    01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789
    """
    def __init__(self, line):
        self.line = line
        line.strip()
        self.date = line[0:23]
        self.level = line[24:28]
        self.thread = line[74:84]
        self.message = line[85:].strip()
        self.status = line[86:91]
        self.method = line[96:].strip()

    def contains(self, s):
        return 0 <= self.line.find(s)

    def contains_any(self, l):
        for i in l:
            if self.contains(i):
                return True
        return False

class log_watcher(object):

    def __init__(self, files, entries, exits, storeonce = None, storeall = None):
        if storeonce is None: storeonce = []
        if storeall is None: storeall = []
        self.files = files
        self.entries = entries
        self.exits = exits
        self.storeonce = storeonce
        self.storeall = storeall

    def gen(self):
        self.m = {}
        try:
            for line in fileinput.input(self.files):
                ll = log_line(line)
                if ll.contains_any(self.entries):
                    self.m[ll.thread] = ll
                elif ll.contains_any(self.storeonce):
                    try:
                        value = self.m[ll.thread]
                        try:
                            value.once
                        except:
                            value.once = ll
                    except KeyError:
                        logging.debug("Not found: " + line)
                elif ll.contains_any(self.storeall):
                    try:
                        value = self.m[ll.thread]
                        value.all.append(ll)
                    except AttributeError:
                        value.all = [ll]
                    except KeyError:
                        logging.debug("Not found: " + line)
                elif ll.contains_any(self.exits):
                    try:
                        value = self.m[ll.thread]
                        del self.m[ll.thread] # Free memory

                        value.start = parse_time(value.date)
                        value.stop = parse_time(ll.date)
                        value.took = value.stop - value.start
                        yield value
                    except KeyError:
                        logging.debug("Not found: " + line)
        finally:
            fileinput.close()

class allthreads_watcher(log_watcher):
    def __init__(self, files):
        log_watcher.__init__(self, files, ["Meth:","Executor.doWork"],["Rslt:","Excp:"])

class saveAndReturnObject_watcher(log_watcher):
    def __init__(self, files):
        log_watcher.__init__(self, files, ["saveAndReturnObject"],["Rslt:","Excp:"],storeonce=["Args:"],storeall=["Adding log"])

# http://matplotlib.sourceforge.net/examples/api/line_with_text.html
class MyLine(lines.Line2D):

   def __init__(self, *args, **kwargs):
      # we'll update the position when the line data is set
      self.text = mtext.Text(0, 0, '')
      lines.Line2D.__init__(self, *args, **kwargs)

      # we can't access the label attr until *after* the line is
      # inited
      self.text.set_text(self.get_label())

   def set_figure(self, figure):
      self.text.set_figure(figure)
      lines.Line2D.set_figure(self, figure)

   def set_axes(self, axes):
      self.text.set_axes(axes)
      lines.Line2D.set_axes(self, axes)

   def set_transform(self, transform):
      # 2 pixel offset
      texttrans = transform + mtransforms.Affine2D().translate(2, 2)
      self.text.set_transform(texttrans)
      lines.Line2D.set_transform(self, transform)

   def set_data(self, x, y):
      if len(x):
         self.text.set_position((x[-1], y[-1]))

      lines.Line2D.set_data(self, x, y)

   def draw(self, renderer):
      # draw my label at the end of the line with 2 pixel offset
      lines.Line2D.draw(self, renderer)
      self.text.draw(renderer)

def plot_threads(watcher, all_colors = ("blue","red","yellow","green","pink","purple")):
    digit = re.compile(".*(\d+).*")

    fig = plt.figure()
    ax = fig.add_subplot(111)

    first = None
    last = None
    colors = {}
    for ll in watcher.gen():
        last = ll.stop
        if first is None:
            first = ll.start

        if ll.thread.strip() == "main":
            t = -1
        else:
            try:
                t = digit.match(ll.thread).group(1)
            except:
                print "Error parsing thread:", ll.thread
                raise
        y = np.array([int(t),int(t)])
        x = np.array([ll.start-first, ll.stop-first])
        c = colors.get(t,all_colors[0])
        i = all_colors.index(c)
        colors[t] = all_colors[ (i+1) % len(all_colors) ]

        if True:
            line = MyLine(x, y, c=c, lw=2, alpha=0.5)#, mfc='red')#, ms=12, label=str(len(ll.logs)))
            #line.text.set_text('line label')
            line.text.set_color('red')
            #line.text.set_fontsize(16)
            ax.add_line(line)
        else:
            # http://matplotlib.sourceforge.net/examples/pylab_examples/broken_barh.html
            ax.broken_barh([ (110, 30), (150, 10) ] , (10, 9), facecolors='blue')

    ax.set_ylim(-2,25)
    ax.set_xlim(0, (last-first))
    plt.show()

if __name__ == "__main__":
    for g in allthreads_watcher(sys.argv).gen():
        print "Date:%s\nElapsed:%s\nLevel:%s\nThread:%s\nMethod:%s\nStatus:%s\n\n" % (g.date, g.took, g.level, g.thread, g.message, g.status)
