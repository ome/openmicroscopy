#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Performs various performance metrics and reports on OMERO.importer log files.
"""

# Copyright (C) 2009 University of Dundee

# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.

# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.

# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

import re
import os
import sys

import mx.DateTime as DateTime
from mx.DateTime import DateTimeDelta
from getopt import getopt, GetoptError

def usage(error):
    """Prints usage so that we don't have to. :)"""
    cmd = sys.argv[0]
    print """%s
Usage: %s [options...] <importer_log_file>
Generate performance metrics from an OMERO.importer log file.

Options:
  --series_report     Print a CSV report for each import's series I/O
  --help              Display this help and exit

Examples:
  %s importer.log
  %s --series_report importer.log > series_report.csv

Report bugs to ome-devel@lists.openmicroscopy.org.uk""" % \
        (error, cmd, cmd, cmd)
    sys.exit(2)

class ParsingError(Exception):
    """Raised whenever there is an error parsing a log file."""
    pass

class Import(object):
    """Stores context about a given import."""

    def __init__(self, start, name):
        self.start = start
        self.end = None
        self.name = name
        self.setid_start = start
        self.setid_end = None
        self.post_process_start = None
        self.post_process_end = None
        self.save_to_db_start = None
        self.save_to_db_end = None
        self.overlays_start = None
        self.thumbnailing_start = None
        self.series = []

class Series(object):
    """Stores context about a given series."""

    def __init__(self, start):
        self.start = start
        self.end = None
        self.planes = []

class Plane(object):
    """Stores context about a given plane."""

    def __init__(self, abs_date_time):
        self.abs_date_time = abs_date_time

class ImporterLog(object):
    """
    Parses and stores context from an OMERO.importer log file. It also has
    the capability of producing various reports.
    """

    # Regular expression for matching log4j log lines
    log_regex = re.compile(
            '^(?P<date_time>\S+\s+\S+)\s+(?P<ms_elapsed>\d+)\s+' \
            '(?P<thread>\[.*?\])\s+(?P<level>\S+)\s+(?P<class>\S+)\s+-\s+' \
            '(?P<message>.*)$')

    # Regular expression for matching possible OMERO.importer status messages
    status_regex = re.compile('^[A-Z_]*')

    # Format string for matching log4j date/time strings
    date_time_fmt = '%Y-%m-%d %H:%M:%S'

    def __init__(self, log_file):
        self.log_file = log_file
        self.imports = []
        self.parse()
        self.last_import = None
        self.last_series = None
    
    def parse(self):
        """Parses the specified log file."""
        line_no = 1
        for line in self.log_file:
            match = self.log_regex.match(line)
            if match:
                self.handle_match(match)
            line_no += 1

    def handle_match(self, match):
        """Handles cases where the log_regex is matched."""
        message = match.group('message')
        if not self.status_regex.match(message):
            return
        date_time = match.group('date_time')
        date_time, ms = date_time.split(',')
        date_time = DateTime.strptime(date_time, self.date_time_fmt)
        ms = DateTimeDelta(0, 0, 0, int(ms) / 1000.0)
        date_time = date_time + ms
        if message.startswith('LOADING_IMAGE'):
            name = message[message.find(':') + 2:]
            self.last_import = Import(date_time, name)
            self.imports.append(self.last_import)
        elif not hasattr(self, 'last_import') or self.last_import is None:
            return
        elif message.startswith('LOADED_IMAGE'):
            self.last_import.setid_end = date_time
        elif message.startswith('BEGIN_POST_PROCESS'):
            self.last_import.post_process_start = date_time
        elif message.startswith('END_POST_PROCESS'):
            self.last_import.post_process_end = date_time
        elif message.startswith('BEGIN_SAVE_TO_DB'):
            self.last_import.save_to_db_start = date_time
        elif message.startswith('END_SAVE_TO_DB'):
            self.last_import.save_to_db_end = date_time
        elif message.startswith('IMPORT_OVERLAYS'):
            self.last_import.overlays_start = date_time
        elif message.startswith('IMPORT_THUMBNAILING'):
            self.last_import.thumbnailing_start = date_time
        elif message.startswith('IMPORT_DONE'):
            self.last_import.end = date_time
            self.last_import = None
        elif message.startswith('DATASET_STORED'):
            self.last_series = Series(date_time)
            self.last_import.series.append(self.last_series)
        elif message.startswith('DATA_STORED'):
            self.last_import.series[-1].end = date_time
        elif message.startswith('IMPORT_STEP'):
            self.last_series.planes.append(Plane(date_time))

    def elapsed(self, start, end):
        if start is not None and end is not None:
            return str((end - start).seconds) + "sec"
        return 'Unknown'

    def report(self):
        """
        Prints a simple report to STDOUT stating timings for the overall
        import and Bio-Formats setId().
        """
        for import_n, i in enumerate(self.imports):
            elapsed = self.elapsed(i.start, i.end)
            print "Import(%s) %d start: %s end: %s elapsed: %s" % \
                    (i.name, import_n, i.start, i.end, elapsed)
            elapsed = self.elapsed(i.setid_start, i.setid_end)
            print "setId() start: %s end: %s elapsed: %s" % \
                    (i.setid_start, i.setid_end, elapsed)
            elapsed = self.elapsed(i.post_process_start, i.post_process_end)
            print "Post process start: %s end: %s elapsed: %s" % \
                    (i.post_process_start, i.post_process_end, elapsed)
            elapsed = self.elapsed(i.save_to_db_start, i.save_to_db_end)
            print "Save to DB start: %s end: %s elapsed: %s" % \
                    (i.save_to_db_start, i.save_to_db_end, elapsed)
            if len(i.series) > 0:
                elapsed = self.elapsed(i.series[0].start, i.series[-1].end)
                print "Image I/O start: %s end: %s elapsed: %s" % \
                        (i.series[0].start, i.series[-1].end, elapsed)
                elapsed = self.elapsed(i.overlays_start, i.thumbnailing_start)
                print "Overlays start: %s end: %s elapsed: %s" % \
                        (i.overlays_start, i.thumbnailing_start, elapsed)
                elapsed = self.elapsed(i.thumbnailing_start, i.end)
                print "Thumbnailing start: %s end: %s elapsed: %s" % \
                        (i.thumbnailing_start, i.end, elapsed)

    def series_report_csv(self):
        """
        Prints a CSV report to STDOUT with timings for the I/O operations
        of each import's set of image series.
        """
        print ','.join(['import', 'series', 'series_start', 'series_end',
                        'series_elapsed'])
        for import_n, i in enumerate(self.imports):
            for series_n, series in enumerate(i.series):
                if series.start is None or series.end is None:
                    continue
                elapsed = (series.end - series.start).seconds
                values = [import_n, series_n, series.start, series.end,
                          elapsed * 1000]
                print ','.join([str(v) for v in values])

if __name__ == "__main__":
    try:
        options, args = getopt(sys.argv[1:], "", ['series_report', 'help'])
    except GetoptError, (msg, opt):
        usage(msg)

    try:
        log_file, = args
    except ValueError:
        usage('Must specify at least one log file.')
    log = ImporterLog(open(log_file))

    do_default_report = True
    for option, argument in options:
        if option == '--help':
            usage('')
        if option == '--series_report':
            do_default_report = False
            log.series_report_csv()

    if do_default_report:
        log.report()

