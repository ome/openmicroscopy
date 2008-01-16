/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.scheduler;

import org.quartz.Scheduler;

/**
 * Produces a <a href="http://www.opensymphony.com/quartz/"Quartz</a>
 * {@link Scheduler} which automatically loads all the triggers it can find.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class SchedulerFactoryBean extends
        org.springframework.scheduling.quartz.SchedulerFactoryBean {

}
