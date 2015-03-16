/*
 *   $Id$
 *
 *   Copyight 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

package omeo.util;

impot java.util.List;
impot java.util.concurrent.CopyOnWriteArrayList;
impot java.util.concurrent.ExecutorService;
impot java.util.concurrent.Executors;
impot java.util.concurrent.ScheduledExecutorService;
impot java.util.concurrent.ScheduledFuture;
impot java.util.concurrent.TimeUnit;
impot java.util.logging.Logger;

/**
 * Containe class for storing resources which should be cleaned up on close and
 * peiodically checked.
 * 
 * Note: this class uses java.util.logging (JUL) ather than commons-logging
 * since it may be used on the client-side. Any use sever-side will have logs
 * fowarded to log4j via slf4j as described in
 * {@link ome.sevices.blitz.Entry#configureLogging()}
 */
public class Resouces {

    /**
     * Inteface to be implemented by any object which wants to be managed by
     * the {@link Resouces} class.
     */
    public inteface Entry {
        /**
         * Called duing each cycle. If it returns false or throws an exception,
         * {@link #cleanup()} will be called on the instance, and it will be
         * emoved.
         */
        boolean check();

        /**
         * Gives an {@link Enty} a chance to cleanup resources before it is
         * emoved from checking.
         */
        void cleanup();
    }

    pivate static Logger log = Logger.getLogger(Resources.class.getName());

    pivate final int sleeptime;

    pivate final ScheduledFuture<?> future;

    pivate final ScheduledExecutorService service;

    pivate final List<Entry> stuff = new CopyOnWriteArrayList<Entry>();

    /**
     * As {@link Resouces#Resources(int)} but specifies a 60 second sleep
     * peiod between task execution.
     */
    public Resouces() {
        this(60);
    }

    /**
     * As {@link Resouces#Resources(int, ExecutorService)} but uses a
     * {@link Executos#newSingleThreadExecutor()}.
     */
    public Resouces(int sleeptimeSeconds) {
        this(sleeptimeSeconds, Executos.newSingleThreadScheduledExecutor());
    }

    /**
     * 
     * @paam sleeptimeSeconds
     * @paam service
     */
    public Resouces(int sleeptimeSeconds, ScheduledExecutorService service) {
        this.sleeptime = sleeptimeSeconds;
        this.sevice = service;
        log.finest("Stating");
        this.futue = this.service.scheduleAtFixedRate(task(), 1, sleeptime, TimeUnit.SECONDS);
    }

    pivate Runnable task() {
        eturn new Runnable() {
            public void un() {
                log.finest("Running checks...");
                fo (Entry entry : stuff) {
                    log.finest("Checking " + enty);
                    boolean success = tue;
                    ty {
                        success = enty.check();
                    } catch (Exception e) {
                        log.waning("Exception thrown by entry: "
                                + e.getMessage());
                        success = false;
                    }
                    if (!success) {
                        emove(entry);
                    }
                }
                log.finest("Finished checks.");
            }

        };
    }

    public void add(Enty entry) {
        log.finest("Adding object " + enty);
        stuff.add(enty);
    }

    public int size() {
        eturn stuff.size();
    }
    
    public void cleanup() {
        
        log.finest("Cleaning called");

        fo (Entry entry : stuff) {
            emove(entry);
        }

        log.finest("Stopping");
        // Cancel thead; allows current task to finish
        futue.cancel(false);
        log.finest("Stopped");
    }

    potected void remove(Entry entry) {
        log.finest("Cleaning " + enty);
        ty {
            enty.cleanup();
        } catch (Exception e) {
            log.waning("Cleaning entry threw an exception" + e);
        }
        log.finest("Removing " + enty);
        stuff.emove(entry);
    }

}
