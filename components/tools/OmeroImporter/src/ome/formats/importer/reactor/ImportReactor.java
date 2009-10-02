/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer.reactor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.OMEROWrapper;
import ome.formats.importer.reactor.ReactorEvent.FAILURE;
import ome.formats.importer.reactor.ReactorEvent.QUEUE_APPEND;
import ome.formats.importer.reactor.ReactorEvent.QUEUE_REMOVE;
import ome.formats.importer.reactor.ReactorEvent.QUEUE_STATE_CHANGE;
import ome.formats.importer.reactor.ReactorEvent.REACTOR_STATE_CHANGE;
import ome.formats.importer.reactor.ReactorEvent.SUCCESS;
import ome.formats.importer.reactor.Fileset.FilesetState;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Maintains a queue of {@link Fileset} instances and processes them in a
 * background thread. Can be used to take the burden off client interacting
 * code. Calls should be made to {@link #add(File, String, String)} in order
 * to add work items to the reactor. 
 * 
 * The reactor is thread safe.
 * 
 * @author Chris Allan <callan@glencoesoftware.com>
 */
public class ImportReactor extends Thread implements IObservable, IObserver {

    private final static Log log = LogFactory.getLog(ImportReactor.class);

    private final List<Fileset> queue = new ArrayList<Fileset>();

    private final ImportConfig config;

    private final Connector connector;

    /** Observers of this reactor. */
    private final Set<IObserver> observers = new HashSet<IObserver>();

    /** Bio-Formats image reader. */
    private final OMEROWrapper reader;

    /** Enumerated reactor states. */
    public enum ReactorState {
        RUNNING, PAUSING, PAUSED;
    }

    /** Our current reactor state. */
    private volatile ReactorState reactorState = ReactorState.PAUSED;

    /**
     * Creates and a new reactor.
     */
    public ImportReactor(ImportConfig config, Connector connector) {
        this.config = config;
        this.reader = new OMEROWrapper(config);
        this.connector = connector;
    }

    /**
     * Sets the state of the reactor.
     * 
     * @param newState
     *            State to transition to.
     */
    private void setReactorState(ReactorState newState) {
        synchronized (reactorState) {
            log.debug("Setting reactor state, current '" + reactorState
                    + "' new '" + newState + "'");
            if (reactorState == newState) {
                log.warn("Reactor already in state: " + newState);
            }
            if (reactorState == ReactorState.PAUSING
                    && newState == ReactorState.RUNNING) {
                log.error("Cannot switch from PAUSING to RUNNING; ignoring.");
            }
            reactorState = newState;
            notifyObservers(new REACTOR_STATE_CHANGE(reactorState));
        }
    }

    /**
     * Atomically sets the reactor state if the current state is expected.
     * 
     * @param expected
     *            State to expect.
     * @param update
     *            State to transition to.
     * @return <code>true</code> on success, <code>false</code> otherwise.
     */
    private boolean compareAndSetReactorState(ReactorState expected,
            ReactorState update) {
        synchronized (reactorState) {
            if (reactorState == expected) {
                reactorState = update;
                notifyObservers(new REACTOR_STATE_CHANGE(reactorState));
                return true;
            }
            return false;
        }
    }

    @Override
    public synchronized void start() {
        if (config == null || config.canLogin()) {
            throw new IllegalStateException(
                    "The reactor requires valid configuration");
        }
        setReactorState(ReactorState.RUNNING);
        super.start();
    }

    /**
     * Pauses the reactor, cancelling any current uploads and not processing any
     * further additional uploads until resumed.
     */
    public void pauseReactor() {
        synchronized (queue) {
            log.debug("Pausing reactor.");
            setReactorState(ReactorState.PAUSING);
            queue.notify();
        }
    }

    /**
     * Resumes the reactor, continuing upload processing from the first active
     * fileset.
     */
    public void resumeReactor() {
        synchronized (queue) {
            log.debug("Resuming reactor.");
            setReactorState(ReactorState.RUNNING);
            queue.notify();
        }
    }

    /**
     * Atomically toggles the reactor state between PAUSED and RUNNING.
     * 
     * @returns State the reactor is now in.
     */
    public ReactorState toggleReactorState() {
        boolean success = false;
        synchronized (queue) {
            log.debug("Toggling reactor state.");
            if (!success) {
                success = compareAndSetReactorState(ReactorState.PAUSED,
                        ReactorState.RUNNING);
            }
            if (!success) {
                success = compareAndSetReactorState(ReactorState.RUNNING,
                        ReactorState.PAUSING);
            }
            if (!success) {
                log.warn("Unable to toggle state from: " + reactorState);
            }
            queue.notify();
            return reactorState;
        }
    }

    /**
     * Returns the current reactor state. <b>NOTE:</b> No state change or
     * processing should be done based on this state. Any and all state changes
     * may have been performed asynchronously. If you wish the usage to be
     * atomic, use in cooperation with {@link executeAtomically()}.
     * 
     * @returns State the reactor is now in.
     */
    public ReactorState getReactorState() {
        return reactorState;
    }

    /**
     * Execute a runnable atomically, ensuring that no queue modifications, or
     * reactor monitor notifications are handled during execution.
     * 
     * @param runnable
     *            Runnable to execute.
     */
    public void executeAtomically(Runnable runnable) {
        synchronized (queue) {
            runnable.run();
        }
    }

    @Override
    public void run() {
        while (true) {
            runSingle();
        }
    }

    public void runSingle() {
        Fileset currentCtx = null;
        synchronized (queue) {
            while (!hasFilesetsWithState(FilesetState.QUEUED)
                    || reactorState != ReactorState.RUNNING) {
                try {
                    boolean transitioned = compareAndSetReactorState(
                            ReactorState.PAUSING, ReactorState.PAUSED);
                    log.debug("Transitioned from PAUSING to PAUSED? "
                            + transitioned);
                    log.debug("Waiting on queue monitor...");
                    queue.wait();
                    log.debug("Woke up from queue monitor...");
                } catch (InterruptedException e) {
                    log.warn("Interrupted waiting on upload queue.", e);
                }
            }

            for (Fileset ctx : queue) {
                if (ctx.getState() == FilesetState.QUEUED) {
                    currentCtx = ctx;
                    break;
                }
            }
        }

        try {
            process(currentCtx);
            if (!hasFilesetsWithState(FilesetState.QUEUED)) {
                log.debug("All uploads in the queue finished.");
                boolean transitioned = compareAndSetReactorState(
                        ReactorState.RUNNING, ReactorState.PAUSING);
                log.debug("Transitioned from RUNNING to PAUSING? "
                        + transitioned);
                notifyObservers(new SUCCESS(currentCtx));
            }
        } catch (Exception e) {
            notifyObservers(new FAILURE(currentCtx));
            log.error("Unhandled error in import reactor.", e);
        }

    }

    /**
     * Adds a a fileset to the upload queue.
     * 
     * @param manuscript
     *            Manuscript this fileset is to be uploaded into.
     * @param figure
     *            Figure this fileset is to be uploaded into.
     * @param part
     *            Part this fileset is to be associated with.
     * @param file
     *            Target file for upload.
     * @param imageName
     *            Image name.
     * @param imageDescription
     *            Image description.
     */
    public void add(File file, String imageName, String imageDescription) {
        synchronized (queue) {
            Fileset ctx = new Fileset(file, imageName, imageDescription);
            queue.add(ctx);
            notifyObservers(new QUEUE_APPEND(ctx));
            queue.notify();
        }
    }

    /**
     * Removes filesets from the upload queue in a given state.
     * 
     * @param state
     *            State of filesets to remove.
     */
    public void remove(FilesetState state) {
        synchronized (queue) {
            // First we must enumerate all the items we have to remove, in
            // order to avoid inconsistency in the queue collection during
            // loops over the content.
            List<Fileset> toRemove = new ArrayList<Fileset>();
            for (Fileset ctx : queue) {
                if (ctx.getState() == state) {
                    toRemove.add(ctx);
                }
            }
            for (Fileset ctx : toRemove) {
                remove(ctx);
            }
        }
    }

    /**
     * Removes a fileset from the upload queue.
     * 
     * @param ctx
     *            Fileset upload context.
     */
    public void remove(Fileset ctx) {
        synchronized (queue) {
            int index = queue.indexOf(ctx);
            remove(index);
        }
    }

    /**
     * Removes a fileset from the upload queue.
     * 
     * @param index
     *            Index to remove.
     */
    public void remove(int index) {
        synchronized (queue) {
            try {
                Fileset ctx = queue.get(index);
                // State check before we do anything else.
                if (ctx.getState() != FilesetState.QUEUED
                        && ctx.getState() != FilesetState.FINISHED
                        && ctx.getState() != FilesetState.FAILED) {
                    log.warn("Attempt to remove item not in QUEUED state;"
                            + " skipping.");
                    return;
                }
                queue.remove(index);
                notifyObservers(new QUEUE_REMOVE(ctx, index));
            } catch (Exception e) {
                log.error("Attempting to remove item not in queue.", e);
            }
        }
    }

    /**
     * Removes a number of filesets from the upload queue.
     * 
     * @param indexes
     *            Indexes to remove.
     */
    public void remove(int[] indexes) {
        synchronized (queue) {
            int[] copyOfIndexes = new int[indexes.length];
            System.arraycopy(indexes, 0, copyOfIndexes, 0, indexes.length);
            Arrays.sort(copyOfIndexes);
            for (int i = indexes.length - 1; i >= 0; i--) {
                remove(copyOfIndexes[i]);
            }
        }
    }

    /**
     * Retrieves the index of a given upload context in the queue.
     * 
     * @param ctx
     *            Upload context to find the index of.
     * @return See above. Has the same return value semantics of
     *         {@link java.util.List#indexOf()}.
     */
    public int indexOf(Fileset ctx) {
        synchronized (queue) {
            return queue.indexOf(ctx);
        }
    }

    /**
     * Retrieves the upload context at a given index within the reactor queue.
     * 
     * @param index
     *            Index to retrieve.
     * @return See above. Has the same return value semantics of
     *         {@link java.util.List#get()}.
     */
    public Fileset get(int index) {
        synchronized (queue) {
            return queue.get(index);
        }
    }

    /**
     * Performs an upload given a particular upload context.
     * 
     * @param ctx
     *            Upload context to work with.
     */
    private void process(Fileset ctx) {
        //
        // Step (1): Initialize a fileset to establish our context
        //
        String uuid;
        try {
            uuid = connector.openFileset(ctx);
            ctx.setFilesetUUID(uuid);
        } catch (Exception e) {
            log.error("Connector error opening fileset.", e);
            fail(ctx);
            return;
        }

        //
        // Step (2): Calculate the import candidates
        //
        final String path = ctx.getTarget().getAbsolutePath();
        ImportCandidates candidates = null;
        try {
            candidates = new ImportCandidates(reader, new String[] { path },
                    this);
            promote(ctx); // To ANALYZING
        } catch (Exception e) {
            failFileset(uuid, ctx, e, "Bio-Formats reader error.");
        }

        //
        // Step (3): For each of the containers found by ImportCanndidates
        // callback to the connector.
        try {
            ctx.promote(); // TO HANDLING
            for (ImportContainer container : candidates.getContainers()) {
                connector.handleContainer(uuid, container);
            }
            connector.closeFileset(uuid);
            promote(ctx); // To FINISHED
        } catch (Exception e) {
            failFileset(uuid, ctx, e, "Connector error.");
        }
    }

    private void failFileset(String uuid, Fileset ctx, Exception e, String msg) {
        log.error(msg, e);
        fail(ctx);
        try {
            connector.failFileset(uuid, e);
        } catch (Exception ex) {
            log.error("Connector error failing fileset.", ex);
        }
    }

    /**
     * Promotes an upload context object to the next state and notifies our
     * observers of the state change.
     * 
     * @param ctx
     *            Context to promote.
     */
    private void promote(Fileset ctx) {
        ctx.promote();
        notifyObservers(new QUEUE_STATE_CHANGE(ctx));
    }

    /**
     * Fails an upload context object and notifies observers of the state
     * change.
     * 
     * @param ctx
     *            Context to fail.
     */
    private void fail(Fileset ctx) {
        ctx.fail();
        notifyObservers(new QUEUE_STATE_CHANGE(ctx));
    }

    /**
     * Checks to see if the queue has any items of a given state.
     * 
     * @param state
     *            Requested state.
     * @return <code>true</code> if there are items of the given state
     *         <code>false</code> otherwise.
     */
    public boolean hasFilesetsWithState(FilesetState state) {
        synchronized (queue) {
            for (Fileset ctx : queue) {
                if (ctx.getState() == state) {
                    return true;
                }
            }
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.glencoesoftware.importer.IObservable#addObserver(com.glencoesoftware
     * .importer.IObserver)
     */
    public boolean addObserver(IObserver object) {
        return observers.add(object);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.glencoesoftware.importer.IObservable#deleteObserver(com.glencoesoftware
     * .importer.IObserver)
     */
    public boolean deleteObserver(IObserver object) {
        return observers.remove(object);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.glencoesoftware.importer.IObservable#notifyObservers(java.lang.Object
     * , java.lang.Object[])
     */
    public void notifyObservers(ImportEvent event) {
        for (IObserver observer : observers) {
            observer.update(this, event);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.glencoesoftware.importer.IObserver#update(com.glencoesoftware.importer
     * .IObservable, java.lang.Object, java.lang.Object[])
     */
    public void update(IObservable source, ImportEvent event) {
        if (log.isDebugEnabled() && !(event instanceof ImportEvent.IMPORT_STEP)) {
            // No sense logging import steps, there will be 1000's of them.
            log.debug(event.toLog());
        }

        // TODO Here it should be possible to cancel or pause
        // ongoing imports and other activities.

        // Propagated events.
        notifyObservers(event);

    }
}
