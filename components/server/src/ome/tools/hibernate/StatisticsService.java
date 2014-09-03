/*
 * Copyright (C) 2014 Glencoe Software, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.tools.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.FactoryBean;

/**
 * A simple class to provide statistics for JMX consumption. See the following ticket for
 * more info: https://hibernate.atlassian.net/browse/HHH-6190
 */

public class StatisticsService implements FactoryBean<Statistics> {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private boolean statisticsEnabled;

    @Override
    public Statistics getObject() throws Exception {
        final Statistics statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(this.statisticsEnabled);

        return statistics;
    }

    @Override
    public Class<?> getObjectType() {
        return Statistics.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
