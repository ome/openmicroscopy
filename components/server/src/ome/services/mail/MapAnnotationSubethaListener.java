/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
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
package ome.services.mail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import ome.model.annotations.MapAnnotation;
import ome.model.internal.NamedValue;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.helper.SimpleMessageListener;

/**
 * Basic {@link SimpleMessageListener} which stores all received emails as
 * {@link MapAnnotation} instances belong to root with the namespace
 * {@link #NAMESPACE}. The description of the annotation is the body of the
 * text and two fields are set with the keys {@ #FROM} and {@link #TO}.
 *
 * @since 5.1.0
 */

public class MapAnnotationSubethaListener implements SimpleMessageListener {

    public final static String NAMESPACE = "openmicroscopy.org/testing/smtp";

    public final static String FROM = "from";

    public final static String TO = "to";

    private final static Logger log = LoggerFactory.getLogger(MapAnnotationSubethaListener.class);

    private final Executor executor;

    private final Principal principal;

    public MapAnnotationSubethaListener(Executor executor, Principal principal) {
        this.executor = executor;
        this.principal = principal;
    }

    public MapAnnotationSubethaListener(Executor executor, String uuid) {
        this.executor = executor;
        this.principal = new Principal(uuid);
    }

    /**
     * Accepts all mails.
     */
    @Override
    public boolean accept(String arg0, String arg1) {
        return true;
    }

    /**
     * Stores each email as a map annotation with {@link #NAMESPACE} set.
     */
    @Override
    public void deliver(final String from, final String recipient, final InputStream stream)
            throws TooMuchDataException, IOException {
       executor.execute(principal, new Executor.SimpleWork(this, "deliver") {
        @Transactional(readOnly=false)
        @Override
        public Object doWork(Session session, ServiceFactory sf) {
            MapAnnotation ma = new MapAnnotation();
            ma.setNs(NAMESPACE);
            ma.setDescription(parseStream(stream));
            ma.setMapValue(new ArrayList<NamedValue>());
            ma.getMapValue().add(new NamedValue("from", from));
            ma.getMapValue().add(new NamedValue("to", recipient));
            ma = sf.getUpdateService().saveAndReturnObject(ma);
            log.info("Saved email: MapAnnotation:" + ma.getId());
            return ma;
        }});
    }

    protected String parseStream(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        
        String line = null;
        try {
                while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                }
        } catch (IOException e) {
                e.printStackTrace();
        }
        return sb.toString();
    }

}