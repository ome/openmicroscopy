/*
 * ome.resurrect.transform.Transformer
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.resurrect.transform;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import ome.model.meta.Event;
import ome.model.meta.Experimenter;

/**
 * @author callan
 * 
 */
public abstract class Transformer {
    /** The model object that the transformer is operating on * */
    private Object model;

    /** Hibernate session for performing necessary queries * */
    private Session session;

    /** Owner of this and all subsequent data types * */
    private Experimenter owner;

    /** A reference to the list of new model objects to be saved * */
    private List toSave;

    /** Creation event for this and all subsequent data types * */
    private Event creationEvent;

    Transformer() {
    }

    Transformer(Object model, Session session, Experimenter owner,
            Event creationEvent, List toSave) {
        if (model == null)
            throw new NullPointerException("Expecting not null model.");
        if (session == null)
            throw new NullPointerException("Expecting not null session.");
        if (owner == null)
            throw new NullPointerException("Expecting not null owner.");
        if (creationEvent == null)
            throw new NullPointerException("Expecting not null creationEvent");
        if (toSave == null)
            toSave = new ArrayList();

        this.model = model;
        this.session = session;
        this.owner = owner;
        this.creationEvent = creationEvent;
        this.toSave = toSave;
    }

    public void setModel(Object model) {
        if (model == null) {
            throw new NullPointerException("Expecting not null model.");
        }
        this.model = model;
    }

    public Object getModel() {
        return model;
    }

    public void setSession(Session session) {
        if (session == null)
            throw new NullPointerException("Expecting not null session.");
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    public void setOwner(Experimenter owner) {
        if (owner == null) {
            throw new NullPointerException("Expecting not null owner.");
        }
        this.owner = owner;
    }

    public Experimenter getOwner() {
        return owner;
    }

    public void setToSave(List toSave) {
        if (toSave == null) {
            throw new NullPointerException("Expecting not null toSave.");
        }
        this.toSave = toSave;
    }

    public List getToSave() {
        return toSave;
    }

    public void setCreationEvent(Event creationEvent) {
        if (creationEvent == null) {
            throw new NullPointerException("Expecting not null creationEvent.");
        }
        this.creationEvent = creationEvent;
    }

    public Event getCreationEvent() {
        return creationEvent;
    }

    public abstract List transmute();
}
