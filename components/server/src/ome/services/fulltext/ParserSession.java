/*
 *   $Id$
 *
 *   Copyright 2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import java.util.Set;
import java.util.HashSet;

import ome.services.messages.ParserOpenFileMessage;

import org.springframework.context.ApplicationListener;
import org.springframework.context.ApplicationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a session for file parsing by the search indexer
 *
 * @author Josh Ballanco, jballanc at glencoesoftware.com
 * @since 5.0.0
 */


public class ParserSession implements ApplicationListener {

    private static Logger log = LoggerFactory.getLogger(ParserSession.class);

    private final Set<ParserOpenFileMessage> openFiles;

    public ParserSession() {
        openFiles = new HashSet<ParserOpenFileMessage>();
    }

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ParserOpenFileMessage) {
            openFiles.add((ParserOpenFileMessage)event);
        }
    }

    public void closeParsedFiles() {
        for (ParserOpenFileMessage file : openFiles) {
            log.debug("Closing file: " + file);
            file.close();
        }
        openFiles.clear();
    }
}
