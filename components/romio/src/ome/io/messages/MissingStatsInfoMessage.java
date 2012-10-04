/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.io.messages;

/**
 * Published when an request is made for a pixels pyramid when the file does
 * not yet contain channel statistics.
 */
public class MissingStatsInfoMessage extends MissingPyramidMessage {

    private static final long serialVersionUID = -5771720674846742806L;

    public MissingStatsInfoMessage(Object source, long pixelsID) {
        super(source, pixelsID);
    }

}