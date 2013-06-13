/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.cmd.basic;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.OK;
import omero.cmd.Response;
import omero.cmd.Timing;
import omero.cmd.HandleI.Cancel;

/**
 * Diagnostic tool for testing call overhead.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.4.0
 */
public class TimingI extends Timing implements IRequest {

    private static final long serialVersionUID = -1L;

    private final CountDownLatch latch = new CountDownLatch(1);

    private Helper helper;

    public Map<String, String> getCallContext() {
        return null;
    }

    public void init(Helper helper) {
        this.helper = helper;
        if (this.steps > 1000000) {
            helper.cancel(new ERR(), null, "too-many-steps",
            		"steps", ""+this.steps);
        } else if (this.millisPerStep > 5*60*1000) {
            helper.cancel(new ERR(), null, "too-long-steps",
            		"millisPerStep", ""+millisPerStep);
        } else if ((this.millisPerStep * this.steps) > 5*60*1000) {
            helper.cancel(new ERR(), null, "too-long",
            		"millisPerStep", ""+this.millisPerStep,
            		"steps", ""+this.steps);
        }
        this.helper.setSteps(this.steps);
    }

    public Object step(int step) {
        helper.assertStep(step);
        try {
			latch.await(millisPerStep, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			helper.debug("Interrupted");
		}
        return null;
    }

    @Override
    public void finish() throws Cancel {
        // no-op
    }

    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);
        if (helper.isLast(step)) {
            helper.setResponseIfNull(new OK());
        }
    }

    public Response getResponse() {
        return helper.getResponse();
    }

}
