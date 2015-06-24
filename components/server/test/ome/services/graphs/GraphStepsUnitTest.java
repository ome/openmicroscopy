/*
 * Copyright (C) 2013 Glencoe Software, Inc. All rights reserved.
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

package ome.services.graphs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ome.model.IObject;
import ome.model.containers.Dataset;
import ome.model.core.Image;

import org.testng.annotations.Test;

/**
 */
@SuppressWarnings("deprecation")
public class GraphStepsUnitTest extends MockGraphTest {

    GraphSteps steps;

    GraphStep step(Class<? extends IObject> k, long...ids) {
        return step(k.getSimpleName(), k, ids[ids.length-1], ids);
    }

    @Test
    public void testEmpty() throws Exception {
        steps = new GraphSteps(new ArrayList<GraphStep>());
        assertEquals(0, steps.size());
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testModificationDisallowed() throws Exception {
        prepareGetHibernateClass();
        steps = new GraphSteps(new ArrayList<GraphStep>());
        steps.add(step(Image.class, 1L));
    }

    public void testJustOne() throws Exception {
        prepareGetHibernateClass();
        steps = new GraphSteps(Arrays.asList(step(Image.class, 1L)));
        assertEquals(1, steps.size());
    }

    public void testMoreThanOne() throws Exception {
        prepareGetHibernateClass();
        GraphStep step1 = step(Image.class, 1L);
        GraphStep step2 = step(Image.class, 1L);
        steps = new GraphSteps(Arrays.asList(step1, step2));
        assertTrue(steps.willBeTriedAgain(step1));
        assertFalse(steps.willBeTriedAgain(step2));
    }

    public void testSucceed() throws Exception {
        prepareGetHibernateClass();
        GraphStep step = step(Image.class, 1L);
        steps = new GraphSteps(Arrays.asList(step));
        assertFalse(steps.alreadySucceeded(step));
        steps.succeeded(step);
        assertTrue(steps.alreadySucceeded(step));
    }


    public void testDifferentTypes() throws Exception {
        prepareGetHibernateClass();
        GraphStep img1a = step(Image.class, 1L);
        GraphStep img1b = step(Image.class, 1L);
        GraphStep img2a = step(Image.class, 2L);
        GraphStep img2b = step(Image.class, 2L);
        GraphStep img2c = step(Image.class, 2L);
        GraphStep ds1a = step(Dataset.class, 1L);
        GraphStep ds1b = step(Dataset.class, 1L);
        GraphStep ds2a = step(Dataset.class, 2L);
        GraphStep ds2b = step(Dataset.class, 2L);
        GraphStep ds2c = step(Dataset.class, 2L);
        List<GraphStep> list = Arrays.asList(
                img1a, img1b, img2a, img2b, img2c,
                ds1a, ds1b, ds2a, ds2b, ds2c);

        steps = new GraphSteps(list);

        // Checks
        for (GraphStep step : list) {
            assertFalse(this.steps.alreadySucceeded(step));
        }
        assertTrue(this.steps.willBeTriedAgain(img1a));
        assertFalse(this.steps.willBeTriedAgain(img1b));
        assertTrue(this.steps.willBeTriedAgain(img2a));
        assertTrue(this.steps.willBeTriedAgain(img2b));
        assertFalse(this.steps.willBeTriedAgain(img2c));
        assertTrue(this.steps.willBeTriedAgain(ds1a));
        assertFalse(this.steps.willBeTriedAgain(ds1b));
        assertTrue(this.steps.willBeTriedAgain(ds2a));
        assertTrue(this.steps.willBeTriedAgain(ds2b));
        assertFalse(this.steps.willBeTriedAgain(ds2c));

        // Only images modified
        steps.succeeded(img2b);
        assertTrue(steps.alreadySucceeded(img2a));
        assertTrue(steps.alreadySucceeded(img2b));
        assertTrue(steps.alreadySucceeded(img2c));
        assertFalse(steps.alreadySucceeded(img1a));
        assertFalse(steps.alreadySucceeded(img1b));
        assertFalse(steps.alreadySucceeded(ds1a));
        assertFalse(steps.alreadySucceeded(ds1b));
        assertFalse(steps.alreadySucceeded(ds2a));
        assertFalse(steps.alreadySucceeded(ds2b));
        assertFalse(steps.alreadySucceeded(ds2c));

    }

}
