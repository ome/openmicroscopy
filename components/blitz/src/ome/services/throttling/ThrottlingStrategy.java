/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.throttling;

import org.springframework.context.ApplicationListener;

import ome.services.blitz.util.BlitzExecutor;

/**
 * Strategy interface for controlling the execution of blitz methods. All
 * implementations are expected to properly handle events from the
 * ome.services.messages.stats package.
 * 
 */
public interface ThrottlingStrategy extends BlitzExecutor, ApplicationListener {

}
