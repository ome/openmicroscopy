#!/usr/bin/env python
# 
# 
# 
# Copyright (c) 2008 University of Dundee. 
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
# 
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
# 
# Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>, 2008.
# 
# Version: 1.0
#

import calendar
import datetime
import time
from django.conf import settings

from webclient.controller import BaseController

class BaseCalendar(BaseController):

    day = None
    month = None
    year = None
    next_month = None
    next_month_name = None
    last_month = None
    last_month_name = None
    next_year = None
    last_year = None

    def __init__(self, conn, year=None, month=None, day=None, **kw):
        BaseController.__init__(self, conn)
        self.year = int(year)
        self.month = int(month)

        if day:
            self.day = int(day)
            # only for python 2.5
            # date = datetime.datetime.strptime(("%i-%i-%i" % (self.year, self.month, self.day)), "%Y-%m-%d")
            date = datetime.datetime(*(time.strptime(("%i-%i-%i" % (self.year, self.month, self.day)), "%Y-%m-%d")[0:6]))
            self.eContext['breadcrumb'] = ['<a href="/%s/history/calendar/%i/%i/">History</a>' % (settings.WEBCLIENT_ROOT_BASE, self.year, self.month), '%s %s' % (date.strftime("%A, %d"), date.strftime("%B %Y"))]
            self.nameday = date.strftime("%A")
        else:
            # only for python 2.5
            # date = datetime.datetime.strptime(("%i-%i" % (self.year, self.month)), "%Y-%m")
            date = datetime.datetime(*(time.strptime(("%i-%i" % (self.year, self.month)), "%Y-%m")[0:6]))
            self.eContext['breadcrumb'] = ["History"]

    def create_calendar(self):
        calendar.setfirstweekday(settings.FIRST_DAY_OF_WEEK)
        now = datetime.datetime(self.year, self.month, 1)

        if self.month == 12:
            self.next_month = now.replace(year=now.year+1, month=1)
            self.next_year = self.year+1
        else:
            self.next_month = now.replace(month=now.month+1)
            self.next_year = self.year
        
        if self.month == 1:
            self.last_month = now.replace(year=self.year-1, month=12)
            self.last_year = self.year-1
        else:
            self.last_month = now.replace(month=now.month-1)
            self.last_year = self.year

        next_month_name = self.next_month.strftime('%B')
        last_month_name = self.last_month.strftime('%B')

        self.week_day_labels = [x for x in calendar.weekheader(5).split(' ') if x != '']
        self.current_month = datetime.datetime(self.year, self.month, 1)
        self.month_name = calendar.month_name[self.month]

        if self.month == 12: 
            self.next_month = self.current_month.replace(year=self.year+1, month=1)
        else:   
            self.next_month = self.current_month.replace(month=self.current_month.month+1)

        self.next_month_name = self.next_month.strftime('%B')

        if self.month == 1: 
            self.last_month = self.current_month.replace(year=self.year-1, month=12)
        else:   
            self.last_month = self.current_month.replace(month=self.current_month.month-1)

        self.last_month_name = self.last_month.strftime('%B')

        self.cal_weeks = calendar.monthcalendar(self.year, self.month) 
        self.monthrange = calendar.monthrange(self.year, self.month)[1] 

        self.cal_days = []

        items = self.calendar_items(self.month, self.monthrange)

        for week,day in [(week,day) for week in xrange(0,len(self.cal_weeks)) for day in xrange(0,7)]:
            imgCounter = dict()
            rdCounter = dict()
            dsCounter = dict()
            prCounter = dict()
            imgCounter = 0
            rdCounter = 0
            dsCounter = 0
            prCounter = 0
            d = int(self.cal_weeks[week][day])
            if d > 0:
                for item in items.get(d):
                    if item.get('type') == 'ome.model.core.Image':
                        imgCounter += 1
                    #elif item.get('type') == 'ome.model.display.RenderingDef':
                    #    rdCounter += 1
                    elif item.get('type') == 'ome.model.containers.Dataset':
                        dsCounter += 1
                    elif item.get('type') == 'ome.model.containers.Project':
                        prCounter += 1
                self.cal_days.append({'day':self.cal_weeks[week][day], 'counter': {'imgCounter':imgCounter, 'rdCounter':rdCounter, 'dsCounter':dsCounter, 'prCounter':prCounter }})
            else:
                self.cal_days.append({'day':self.cal_weeks[week][day], 'counter': {}})
            self.cal_weeks[week][day] = {'cell': self.cal_days[-1]}

    def calendar_items(self, month, monthrange):
        if month < 10:
            mn = '0%i' % month
        else:
            mn = month
        # only for python 2.5
        # d1 = datetime.datetime.strptime(("%i-%s-01 00:00:00" % (self.year, mn)), "%Y-%m-%d %H:%M:%S")
        # d2 = datetime.datetime.strptime(("%i-%s-%i 23:59:59" % (self.year, mn, monthrange)), "%Y-%m-%d %H:%M:%S")
        d1 = datetime.datetime(*(time.strptime(("%i-%s-01 00:00:00" % (self.year, mn)), "%Y-%m-%d %H:%M:%S")[0:6]))
        d2 = datetime.datetime(*(time.strptime(("%i-%s-%i 23:59:59" % (self.year, mn, monthrange)), "%Y-%m-%d %H:%M:%S")[0:6]))
        
        start = long(time.mktime(d1.timetuple())+1e-6*d1.microsecond)*1000
        end = long(time.mktime(d2.timetuple())+1e-6*d2.microsecond)*1000
        all_logs = self.conn.getEventsByPeriod(start, end)
        
        items = dict()
        for d in xrange(1,monthrange+1):
            items[d] = list()
        for i in all_logs:
            for d in items:
                if time.gmtime(i.event.time.val / 1000).tm_mday == d:
                    items[d].append({'id':i.entityId, 'type': i.entityType, 'action': i.action})
        return items
        
    def month_range(self, year, month):
        if month == 12:
            year += 1
            month = 1
        else:
            month += 1
        return (datetime.date(year, month, 1), datetime.date(year, month, 1)-datetime.timedelta(days=1))

    def get_items(self, cal_type=None):
        if self.month < 10:
            mn = '0%i' % self.month
        else:
            mn = self.month
        if self.day < 10:
            dy = '0%i' % self.day
        else:
            dy = self.day
        # only for python 2.5
        # d1 = datetime.datetime.strptime(('%i-%s-%s 00:00:00' % (self.year, mn, dy)), "%Y-%m-%d %H:%M:%S")
        # d2 = datetime.datetime.strptime(('%i-%s-%s 23:59:59' % (self.year, mn, dy)), "%Y-%m-%d %H:%M:%S")
        
        d1 = datetime.datetime(*(time.strptime(('%i-%s-%s 00:00:00' % (self.year, mn, dy)), "%Y-%m-%d %H:%M:%S")[0:6]))
        d2 = datetime.datetime(*(time.strptime(('%i-%s-%s 23:59:59' % (self.year, mn, dy)), "%Y-%m-%d %H:%M:%S")[0:6]))
        
        start = long(time.mktime(d1.timetuple())+1e-6*d1.microsecond)*1000
        end = long(time.mktime(d2.timetuple())+1e-6*d2.microsecond)*1000
        
        self.day_items = list()
        self.day_items_size = 0
        self.total_items_size = self.conn.countImagesByPeriod(start, end)+self.conn.countDatasetsByPeriod(start, end)+self.conn.countProjectsByPeriod(start, end)
        #self.conn.countRenderingDefByPeriod(start, end)+
        
        
        if cal_type is not None:
            if cal_type == 'image':
                img_logs = list(self.conn.getImagesByPeriod(start, end))
                if len(img_logs) > 0 :
                    self.day_items.append({'image':img_logs})
                    self.day_items_size = len(img_logs)
            elif cal_type == 'dataset':
                ds_logs = list(self.conn.getDatasetsByPeriod(start, end))
                if len(ds_logs) > 0 :
                    self.day_items.append({'dataset':ds_logs})
                    self.day_items_size = len(ds_logs)
            elif cal_type == 'project':
                pr_logs = list(self.conn.getProjectsByPeriod(start, end))
                if len(pr_logs) > 0 :
                    self.day_items.append({'project':pr_logs})
                    self.day_items_size = len(pr_logs)
            #elif cal_type == 'renderdef':
                #rd_logs = list(self.conn.getRenderingDefByPeriod(start, end))
                #if len(rd_logs) > 0 :
                #    self.day_items.append({'rdef':rd_logs})
                #    self.day_items_size = len(rd_logs)
        else:
            img_logs = list(self.conn.getImagesByPeriod(start, end)) 
            #rd_logs = list(self.conn.getRenderingDefByPeriod(start, end)) 
            ds_logs = list(self.conn.getDatasetsByPeriod(start, end)) 
            pr_logs = list(self.conn.getProjectsByPeriod(start, end)) 
            if len(img_logs) > 0 or len(ds_logs) > 0 or len(pr_logs): #or len(rd_logs) > 0
                self.day_items.append({'image':img_logs, 'dataset':ds_logs, 'project':pr_logs}) #'rdef':rd_logs, 
                self.day_items_size = len(img_logs)+len(ds_logs)+len(pr_logs) #+len(rd_logs)

