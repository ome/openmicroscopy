/*
 * Copyright (C) 2014 Glencoe Software, Inc. All rights reserved.
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

package ome.system.metrics;

import static com.codahale.metrics.MetricRegistry.name;

import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ch.qos.logback.classic.LoggerContext;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.codahale.metrics.logback.InstrumentedAppender;

/**
 * Spring bean for configuring metrics in this JVM.
 */
public class DefaultMetrics implements Metrics, InitializingBean {

    private static Logger log = LoggerFactory.getLogger(Metrics.class);

    private static String DOMAIN = Metrics.class.getPackage().getName();

    private MetricRegistry registry = new MetricRegistry();

    private int slf4jMinutes = 0;

    private boolean jmxReporter = true;

    private boolean jvmInstrumentation = true;

    private boolean logbackInstrumentation = true;

    private String graphiteAddress = null;

    private Collection<String> beginsWith = null;

    public void setSlf4jMinutes(int minutes) {
        this.slf4jMinutes = minutes;
    }

    public void setBeginsWith(Collection<String> prefixes) {
        this.beginsWith = prefixes;
    }

    public void setGraphiteAddress(String address) {
        this.graphiteAddress = address;
    }

    private MetricFilter filter() {
        return new MetricFilter() {
            @Override
            public boolean matches(String arg0, Metric arg1) {
                if (beginsWith == null) {
                    return true;
                } else {
                    for (String b : beginsWith) {
                        if (arg0.startsWith(b)) {
                            return true;
                        }
                    }
                }
                return false;
            }};
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (slf4jMinutes > 0) {
            final Slf4jReporter reporter = Slf4jReporter.forRegistry(registry)
                .filter(filter())
                .outputTo(LoggerFactory.getLogger(DOMAIN))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
            reporter.start(slf4jMinutes, TimeUnit.MINUTES);
        }

        if (jmxReporter) {
            final JmxReporter jmx = JmxReporter.forRegistry(registry)
                    .inDomain(DOMAIN).build();
            jmx.start();
        }

        if (jvmInstrumentation) {
            BufferPoolMetricSet bufferPoolMetrics = new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer());
            registerAll("jvm.buffers", bufferPoolMetrics, registry);
            registerAll("jvm.gc", new GarbageCollectorMetricSet(), registry);
            registerAll("jvm.memory", new MemoryUsageGaugeSet(), registry);
            registerAll("jvm.threads", new ThreadStatesGaugeSet(), registry);
            registry.register("jvm.fileDescriptorCountRatio", new FileDescriptorRatioGauge());
        }

        if (logbackInstrumentation) {
            try {
                final LoggerContext factory = (LoggerContext) LoggerFactory.getILoggerFactory();
                final ch.qos.logback.classic.Logger root = factory.getLogger(Logger.ROOT_LOGGER_NAME);

                final InstrumentedAppender metrics = new InstrumentedAppender(registry);
                metrics.setContext(root.getLoggerContext());
                metrics.start();
                root.addAppender(metrics);
            } catch (Exception e) {
                log.error("Failed to instrument logback", e);
            }
        }

        if (graphiteAddress != null && !graphiteAddress.isEmpty()) {
            final Graphite graphite = new Graphite(new InetSocketAddress(graphiteAddress, 2003));
            final GraphiteReporter reporter = GraphiteReporter.forRegistry(registry)
                .prefixedWith(System.getProperty("Ice.Admin.ServerId", "OMERO"))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .build(graphite);
                reporter.start(1, TimeUnit.MINUTES);
        }
    }

    public Counter counter(Object obj, String name) {
        return new DefaultCounter(registry.counter(name(obj.getClass(), name)));
    }

    public Timer timer(Object obj, String name) {
        return new DefaultTimer(registry.timer(name(obj.getClass(), name)));
    }

    public Histogram histogram(Object obj, String name) {
        return new DefaultHistogram(registry.histogram(name(obj.getClass(), name)));
    }

    private void registerAll(String prefix, MetricSet metrics, MetricRegistry registry) {
        for (Map.Entry<String, Metric> entry : metrics.getMetrics().entrySet()) {
          String name = MetricRegistry.name(prefix, entry.getKey());
          if (entry.getValue() instanceof MetricSet) {
              registerAll(name, (MetricSet) entry.getValue(), registry);
          } else {
              registry.register(name,  entry.getValue());
          }
        }
      }
}
