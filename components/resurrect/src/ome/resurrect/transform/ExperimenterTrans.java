/*
 * ome.resurrect.transform.ExperimenterTrans
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
public class ExperimenterTrans extends Transformer {
    public ExperimenterTrans(Object model, Session session, Experimenter owner,
            Event creationEvent, List toSave) {
        if (model == null)
            throw new NullPointerException("Expecting not null model.");
        if (creationEvent == null)
            throw new NullPointerException("Expecting not null creationEvent");
        if (toSave == null)
            setToSave(new ArrayList());

        setModel(model);
        setCreationEvent(creationEvent);
    }

    @SuppressWarnings("unchecked")
    public List transmute() {
        ome.model.Experimenter oldExperimenter = (ome.model.Experimenter) getModel();

        List toSave = getToSave();

        Experimenter e = new Experimenter();
        e.setEmail(oldExperimenter.getEmail());
        e.setFirstName(oldExperimenter.getFirstname());
        e.setLastName(oldExperimenter.getLastname());
        e.setOmeName(oldExperimenter.getOmeName());
        e.setInstitution(oldExperimenter.getInstitution());

        toSave.add(e);
        return toSave;
    }
}
