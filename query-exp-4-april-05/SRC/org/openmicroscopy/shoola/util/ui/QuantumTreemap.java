/**
 * Copyright (C) 2001 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 *
 * Written by Benjamin B. Bederson
 * bederson@cs.umd.edu
 * http://www.cs.umd.edu/~bederson
 *
 * May 2001
 *
The Quantum Treemap software is subject to the Mozilla Public License
Version 1.0 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/.

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
the License for the specific language governing rights and limitations
under the License.

The Original Code is Quantum Treemap v1.0

The Initial Developer of the Original Code is Ben Bederson.
All of the code is Copyright (C) University of Maryland. All Rights Reserved.

INTELLECTUAL PROPERTY NOTES

In this directory tree, we are releasing Jazz to the interested
development community as Open Source code.

Quantum Treemap is copyrighted by the University of Maryland, and is available
for all users, in accordance with the Open Source model. It is
available as free software for license according to the Mozilla Public
License.

If you re-distribute this code, with or without modifications, then
one of the provisions of the Mozilla Public License, under which you
are using it, is to include the following statement:

-----------------------------------------------------------------------------

"The contents of this directory tree are subject to the Mozilla Public
License Version 1.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License
at http://www.mozilla.org/MPL/.

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
the License for the specific language governing rights and limitations
under the License.

The Original Code is Quantum Treemap v1.0

The Initial Developer of the Original Code is Benjamin B. Bederson. 
All of the code is Copyright (C) University of Maryland. All Rights Reserved.

--------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.ui;

import java.awt.*;

public class QuantumTreemap
{
    static public final int INDEX_BY_MIDDLE = 1;
    static public final int INDEX_BY_SPLIT_SIZE = 2;
    static public final int INDEX_BY_BIGGEST = 3;

    static public final double EXPECTED_WASTE_FACTOR = 1.15;

    static public boolean debug = false;
    static public boolean squarified = true;

    protected int indexType = INDEX_BY_MIDDLE;
    protected int[] origSizes;
    protected Rectangle origBox;
    protected double origiar;
    protected int numQuadLayouts;
    protected int numSnake3Layouts;
    protected int numSnake4Layouts;
    protected int numSnake5Layouts;
    protected Rectangle[] resultRects = null;

    boolean debugBoxEvening = false;

    public QuantumTreemap(int[] sizes, double iar, Rectangle box)
    {
        origSizes = sizes;
        origBox = box;
        origiar = iar;
    }

    public Rectangle[] quantumLayout()
    {
        numQuadLayouts = 0;
        numSnake3Layouts = 0;
        numSnake4Layouts = 0;
        numSnake5Layouts = 0;

        int area = computeSize(origSizes);
        area *= EXPECTED_WASTE_FACTOR; // Add room for expected waste factor
        double ar = computeAspectRatio(origBox) / origiar;
        int h = (int) Math.ceil(Math.sqrt(area / ar));
        int w = (int) Math.ceil((double) area / h);
        Rectangle box = new Rectangle(origBox.x, origBox.y, w, h);
        debugPrintln("Orig box = " + origBox + ", Image AR = " + origiar);

        double boxAR = computeAspectRatio(box);
        boolean growWide = ((boxAR >= 1) ? true : false);
        resultRects = quantumLayout(origSizes, box, growWide);

        return resultRects;
    }

    public Rectangle[] getResult()
    {
        return resultRects;
    }

    public Rectangle getResultBox()
    {
        return computeUnion(resultRects);
    }

    /**
     * Prints statistics from the most recent layout.
     */
    public void printStatistics()
    {
        if (resultRects == null)
        {
            System.out.println("Layout hasn't been computed yet");
        }
        else
        {
            System.out.print("Sizes: ");
            for (int i = 0; i < origSizes.length; i++)
            {
                System.out.print(origSizes[i] + " ");
            }
            System.out.println("");
            System.out.println("Requested Box = " + origBox);
            System.out.println("Final Box = " + computeUnion(resultRects));
            System.out.println(
                "Average aspect ratio: " + getAverageAspectRatio());
            System.out.println("Wasted Space: " + getWastedSpace() + "%");
        }
    }

    /**
     * Compute and return the average aspect ratio of all the computed rectangles
     * of the most recent layout.
     */
    public double getAverageAspectRatio()
    {
        double aar = 0;

        if (resultRects == null)
        {
            System.out.println("Layout hasn't been computed yet");
        }
        else
        {
            aar = computeAverageAspectRatio(resultRects);
        }

        return aar;
    }

    /**
     * Compute and return the wasted space as a percentage of the total final box size,
     * based on the most recent layout.
     */
    public double getWastedSpace()
    {
        double wastedSpace = 0;

        if (resultRects == null)
        {
            System.out.println("Layout hasn't been computed yet");
        }
        else
        {
            Rectangle finalBox = computeUnion(resultRects);
            double ar = computeAspectRatio(finalBox);
            double origAR = computeAspectRatio(origBox) / origiar;
            if (ar > origAR)
            {
                int newHeight = (int) (finalBox.width / origAR);
                finalBox.height = newHeight;
            }
            else
            {
                int newWidth = (int) (finalBox.height * origAR);
                finalBox.width = newWidth;
            }
            double totalArea = computeArea(finalBox);
            double usedArea = computeSize(origSizes);
            wastedSpace = 100.0 * (totalArea - usedArea) / totalArea;
            // System.out.println("origBox="+origBox+", resultBox="+computeUnion(resultRects)+", finalBox="+finalBox+", totalArea="+totalArea+", usedArea="+usedArea);
        }

        return wastedSpace;
    }

    static public Dimension layoutElements(int numElements, double ar)
    {
        double ar1, ar2;
        int c1, r1;
        int c2, r2;
        Dimension dim;

        c1 = (int) Math.floor(Math.sqrt(numElements * ar));
        if (c1 == 0)
        {
            c1 = 1;
        }
        r1 = (int) Math.ceil((double) numElements / c1);
        ar1 = (double) c1 / r1;

        c2 = (int) Math.ceil(Math.sqrt(numElements * ar));
        if (c2 == 0)
        {
            c2 = 1;
        }
        r2 = (int) Math.ceil((double) numElements / c2);
        ar2 = (double) c2 / r2;
        if (Math.max((ar1 / ar), (ar / ar1))
            > Math.max((ar2 / ar), (ar / ar2)))
        {
            dim = new Dimension(c2, r2);
        }
        else
        {
            dim = new Dimension(c1, r1);
        }

        return dim;
    }

    protected Rectangle[] quantumLayout(
        int[] sizes,
        Rectangle box,
        boolean growWide)
    {
        int i;
        int[] l1 = null;
        int[] l2 = null;
        int[] l3 = null;
        int l1Size = 0;
        int l2Size = 0;
        int l3Size = 0;
        Rectangle r1 = null;
        Rectangle r2 = null;
        Rectangle r3 = null;
        Rectangle rp = null;
        double r1AR, r2AR, r3AR;
        int r1w, r1h;
        int pivotIndex;
        int pivotSize;
        double pivotAR;
        Rectangle[] boxes = null;
        double boxAR;
        Rectangle box2; // Layout box for P, R2 and R3
        double box2AR;
        int w, h;
        int b2w, b2h;
        int b2Size;
        double wd, hd;
        double ratio;
        Dimension dim1 = null;
        Dimension dim2 = null;
        Dimension dim3 = null;
        Rectangle l1finalbox = null;
        Rectangle l2finalbox = null;
        Rectangle[] l1boxes = null;
        Rectangle[] l2boxes = null;
        Rectangle[] l3boxes = null;
        int numBoxes = 0;
        boolean newGrowWide;

        pivotIndex = computePivotIndex(sizes);
        pivotSize = sizes[pivotIndex];
        boxAR = computeAspectRatio(box);

        debugPrintln(
            "Items="
                + sizes.length
                + ", box="
                + box
                + ", pivotIndex = "
                + pivotIndex);
        debugPrint("Sizes: ");
        for (i = 0; i < sizes.length; i++)
        {
            debugPrint(sizes[i] + " ");
        }
        debugPrintln("");

        // Stopping conditions
        if (sizes.length == 1)
        {
            boxes = new Rectangle[1];
            boxes[0] = box;
            debugPrintln("Stop 1: box = " + box);

            return boxes;
        }

        if (sizes.length == 2)
        {
            boxes = new Rectangle[2];
            ratio = (double) sizes[0] / (sizes[0] + sizes[1]);
            if (growWide)
            {
                dim1 = computeTableLayout(sizes[0], boxAR * ratio);
                dim2 = computeTableLayout(sizes[1], boxAR * (1 - ratio));
                h = Math.max(dim1.height, dim2.height);
                dim2 = computeTableLayoutGivenHeight(sizes[1], h);
                boxes[0] = new Rectangle(box.x, box.y, dim1.width, h);
                boxes[1] =
                    new Rectangle(
                        box.x + dim1.width,
                        box.y,
                        dim2.width,
                        dim2.height);
            }
            else
            {
                dim1 = computeTableLayout(sizes[0], boxAR / ratio);
                dim2 = computeTableLayout(sizes[1], boxAR / (1 - ratio));
                w = Math.max(dim1.width, dim2.width);
                dim2 = computeTableLayoutGivenWidth(sizes[1], w);
                boxes[0] = new Rectangle(box.x, box.y, w, dim1.height);
                boxes[1] =
                    new Rectangle(
                        box.x,
                        box.y + dim1.height,
                        dim2.width,
                        dim2.height);
            }

            debugPrintln("Stop 2: box[0] = " + boxes[0]);
            debugPrintln("Stop 2: box[1] = " + boxes[1]);
            return boxes;
        }

        // First, compute R1
        if (pivotIndex > 0)
        {
            l1 = new int[pivotIndex];
            System.arraycopy(sizes, 0, l1, 0, pivotIndex);
            l1Size = computeSize(l1);
            b2Size = computeSize(sizes, pivotIndex, sizes.length - 1);
            if (growWide)
            {
                dim1 = computeTableLayoutGivenHeight(l1Size, box.height);
                dim2 = computeTableLayoutGivenHeight(b2Size, box.height);
                r1 = new Rectangle(box.x, box.y, dim1.width, dim1.height);
                box2 =
                    new Rectangle(
                        box.x + dim1.width,
                        box.y,
                        dim2.width,
                        dim2.height);
            }
            else
            {
                dim1 = computeTableLayoutGivenWidth(l1Size, box.width);
                dim2 = computeTableLayoutGivenWidth(b2Size, box.width);
                r1 = new Rectangle(box.x, box.y, dim1.width, dim1.height);
                box2 =
                    new Rectangle(
                        box.x,
                        box.y + dim1.height,
                        dim2.width,
                        dim2.height);
            }
        }
        else
        {
            box2 = new Rectangle(box.x, box.y, box.width, box.height);
        }
        debugPrintln("r1 = " + r1);

        // Recurse on R1 to compute better box2
        if (l1 != null)
        {
            if (l1.length > 1)
            {
                debugPrintln("Recurse on R1");
                r1AR = computeAspectRatio(r1);
                if (r1AR == 1)
                {
                    newGrowWide = growWide;
                }
                else
                {
                    newGrowWide = ((r1AR >= 1) ? true : false);
                }
                l1boxes = quantumLayout(l1, r1, newGrowWide);
            }
            else
            {
                l1boxes = new Rectangle[1];
                l1boxes[0] = r1;
            }
            l1finalbox = computeUnion(l1boxes);
            if (growWide)
            {
                box2.height = r1.height; // l1finalbox.height;
            }
            else
            {
                box2.width = r1.width; // l1finalbox.width;
            }
            debugPrintln(
                "Final R1 Box = " + l1finalbox + ", Orig R1 Box = " + r1);
            debugPrintln("box2 = " + box2);
        }

        // Then compute R2 and R3
        box2AR = computeAspectRatio(box2);
        // First, split up l2 and l3, for best aspect ratio of pivot
        boolean first = true;
        double bestAR = 0;
        int w1, h1;
        Dimension bestdim1 = null;
        int bestl2Size = 0;
        int bestl3Size = 0;
        int bestIndex = 0;

        for (i = pivotIndex + 1; i < sizes.length; i++)
        {
            l2Size = computeSize(sizes, pivotIndex + 1, i);
            ratio = (double) pivotSize / (pivotSize + l2Size);
            if (growWide)
            {
                h1 = (int) Math.ceil(ratio * box2.height);
                dim1 = computeTableLayoutGivenHeight(pivotSize, h1);
            }
            else
            {
                w1 = (int) Math.ceil(ratio * box2.width);
                dim1 = computeTableLayoutGivenWidth(pivotSize, w1);
            }
            pivotAR =
                Math.max(
                    ((double) dim1.width / dim1.height),
                    ((double) dim1.height / dim1.width));
            if (first || (pivotAR < bestAR))
            {
                first = false;
                bestAR = pivotAR;
                bestdim1 = dim1;
                bestl2Size = l2Size;
                bestIndex = i;
            }
            // debugPrintln("split["+i+"]: dim1 = " + dim1 + ", ar = " + pivotAR);
        }
        debugPrintln(
            "best split: pivot="
                + pivotIndex
                + ", bestIndex="
                + bestIndex
                + ", bestDim="
                + bestdim1
                + ", bestAR="
                + bestAR);
        if (bestIndex > 0)
        {
            l2 = new int[bestIndex - pivotIndex];
            System.arraycopy(sizes, pivotIndex + 1, l2, 0, l2.length);
            if ((sizes.length - 1 - bestIndex) > 0)
            {
                l3 = new int[sizes.length - 1 - bestIndex];
                System.arraycopy(sizes, bestIndex + 1, l3, 0, l3.length);
            }
        }
        if (l2 != null)
        {
            if (growWide)
            {
                dim2 =
                    computeTableLayoutGivenHeight(
                        bestl2Size,
                        box2.height - bestdim1.height);
                rp =
                    new Rectangle(
                        box2.x,
                        box2.y,
                        bestdim1.width,
                        bestdim1.height);
                r2 =
                    new Rectangle(
                        box2.x,
                        box2.y + dim1.height,
                        dim2.width,
                        dim2.height);
                if (l3 != null)
                {
                    l3Size =
                        computeSize(sizes, bestIndex + 1, sizes.length - 1);
                    dim3 = computeTableLayoutGivenHeight(l3Size, box2.height);
                    r3 =
                        new Rectangle(
                            box2.x + dim2.width,
                            box2.y,
                            dim3.width,
                            dim3.height);
                }
            }
            else
            {
                dim2 =
                    computeTableLayoutGivenWidth(
                        bestl2Size,
                        box2.width - bestdim1.width);
                rp =
                    new Rectangle(
                        box2.x,
                        box2.y,
                        bestdim1.width,
                        bestdim1.height);
                r2 =
                    new Rectangle(
                        box2.x + dim1.width,
                        box2.y,
                        dim2.width,
                        dim2.height);
                if (l3 != null)
                {
                    l3Size =
                        computeSize(sizes, bestIndex + 1, sizes.length - 1);
                    dim3 = computeTableLayoutGivenWidth(l3Size, box2.width);
                    r3 =
                        new Rectangle(
                            box2.x,
                            box2.y + dim2.height,
                            dim3.width,
                            dim3.height);
                }
            }
        }
        else
        {
            if (growWide)
            {
                dim1 = computeTableLayoutGivenHeight(pivotSize, r1.height);
                // l1finalbox.height);
            }
            else
            {
                dim1 = computeTableLayoutGivenWidth(pivotSize, r1.width);
                // l1finalbox.width);
            }
            rp = new Rectangle(box2.x, box2.y, dim1.width, dim1.height);
        }
        debugPrintln("DIM1 = " + dim1);
        debugPrintln("DIM2 = " + dim2);
        debugPrintln("DIM3 = " + dim3);
        debugPrintln("rp = " + rp);
        if (l2 != null)
        {
            debugPrintln("r2 = " + r2);
        }
        if (l3 != null)
        {
            debugPrintln("r3 = " + r3);
        }

        // Finally, recurse on sublists in R2 and R3
        if (l2 != null)
        {
            if (l2.length > 1)
            {
                debugPrintln("Recurse on R2");
                r2AR = computeAspectRatio(r2);
                if (r2AR == 1)
                {
                    newGrowWide = growWide;
                }
                else
                {
                    newGrowWide = ((r2AR >= 1) ? true : false);
                }
                l2boxes = quantumLayout(l2, r2, newGrowWide);
            }
            else
            {
                l2boxes = new Rectangle[1];
                l2boxes[0] = r2;
            }
            l2finalbox = computeUnion(l2boxes);
            debugPrintln("Final R2 Box = " + l2finalbox);
        }

        if (l3 != null)
        {
            if (l3.length > 1)
            {
                debugPrintln("Recurse on R3");
                r3AR = computeAspectRatio(r3);
                if (r3AR == 1)
                {
                    newGrowWide = growWide;
                }
                else
                {
                    newGrowWide = ((r3AR >= 1) ? true : false);
                }
                l3boxes = quantumLayout(l3, r3, newGrowWide);
            }
            else if (l3.length == 1)
            {
                l3boxes = new Rectangle[1];
                l3boxes[0] = r3;
            }
        }

        // Shift and expand/contract the new layouts
        // depending on the the other sub-layouts
        if (growWide)
        {
            if (l1 != null)
            {
                rp.x = l1finalbox.x + l1finalbox.width;
                rp.y = l1finalbox.y;
            }
            if (l2 != null)
            {
                translateBoxesTo(l2boxes, rp.x, (rp.y + rp.height));
                evenBoxWidth(rp, l2boxes);
                if (l3 != null)
                {
                    l2finalbox = computeUnion(l2boxes);
                    translateBoxesTo(
                        l3boxes,
                        (l2finalbox.x + l2finalbox.width),
                        rp.y);
                }
                evenBoxHeight(l1boxes, l2boxes, l3boxes);
            }
            else
            {
                evenBoxHeight(rp, l1boxes);
            }
        }
        else
        {
            if (l1 != null)
            {
                rp.x = l1finalbox.x;
                rp.y = l1finalbox.y + l1finalbox.height;
            }
            if (l2 != null)
            {
                translateBoxesTo(l2boxes, (rp.x + rp.width), rp.y);
                evenBoxHeight(rp, l2boxes);
                if (l3 != null)
                {
                    l2finalbox = computeUnion(l2boxes);
                    translateBoxesTo(
                        l3boxes,
                        rp.x,
                        (l2finalbox.y + l2finalbox.height));
                }
                evenBoxWidth(l1boxes, l2boxes, l3boxes);
            }
            else
            {
                evenBoxWidth(rp, l1boxes);
            }
        }

        numBoxes = 0;
        if (l1 != null)
        {
            numBoxes += l1.length;
        }
        numBoxes++;
        if (l2 != null)
        {
            numBoxes += l2.length;
        }
        if (l3 != null)
        {
            numBoxes += l3.length;
        }
        boxes = new Rectangle[numBoxes];
        i = 0;
        if (l1 != null)
        {
            System.arraycopy(l1boxes, 0, boxes, 0, l1boxes.length);
            i += l1boxes.length;
        }
        boxes[i] = rp;
        i++;
        if (l2 != null)
        {
            System.arraycopy(l2boxes, 0, boxes, i, l2boxes.length);
            i += l2boxes.length;
        }
        if (l3 != null)
        {
            System.arraycopy(l3boxes, 0, boxes, i, l3boxes.length);
        }

        boxAR = computeAspectRatio(box);
        if (boxAR == 1)
        {
            newGrowWide = growWide;
        }
        else
        {
            newGrowWide = ((boxAR >= 1) ? true : false);
        }
        if (squarified)
        {
            boxes = tryAlternativeLayouts(sizes, box, boxes, newGrowWide);
        }

        debugPrintln("return numBoxes=" + boxes.length);

        return boxes;
    }

    protected Rectangle[] tryAlternativeLayouts(
        int[] sizes,
        Rectangle box,
        Rectangle[] origBoxes,
        boolean growWide)
    {
        Rectangle[] boxes = origBoxes;
        Rectangle[] nboxes = null;
        double ratio1, ratio2, ratio3, ratio4, ratio5;
        int bottom, right;
        int w, h;
        int w1, w2, w3, w4, w5;
        int h1, h2, h3, h4, h5;
        double ar1, ar2, ar3, ar4;
        double boxAR = computeAspectRatio(box);
        double origAvgAR, newAvgAR;
        int origWastedSpace, newWastedSpace;
        Dimension dim1, dim2, dim3, dim4, dim5;
        double arSavings, wasteSavings;

        if (sizes.length == 3)
        {
            // Try snake alg.
            nboxes = new Rectangle[3];
            if (growWide)
            {
                h = box.height;
                dim1 = computeTableLayoutGivenHeight(sizes[0], h);
                dim2 = computeTableLayoutGivenHeight(sizes[1], h);
                dim3 = computeTableLayoutGivenHeight(sizes[2], h);
                nboxes[0] = new Rectangle(box.x, box.y, dim1.width, h);
                nboxes[1] =
                    new Rectangle(box.x + dim1.width, box.y, dim2.width, h);
                nboxes[2] =
                    new Rectangle(
                        box.x + dim1.width + dim2.width,
                        box.y,
                        dim3.width,
                        h);
            }
            else
            {
                w = box.width;
                dim1 = computeTableLayoutGivenWidth(sizes[0], w);
                dim2 = computeTableLayoutGivenWidth(sizes[1], w);
                dim3 = computeTableLayoutGivenWidth(sizes[2], w);
                nboxes[0] = new Rectangle(box.x, box.y, w, dim1.height);
                nboxes[1] =
                    new Rectangle(box.x, box.y + dim1.height, w, dim2.height);
                nboxes[2] =
                    new Rectangle(
                        box.x,
                        box.y + dim1.height + dim2.height,
                        w,
                        dim3.height);
            }

            origAvgAR = computeAverageAspectRatio(boxes);
            newAvgAR = computeAverageAspectRatio(nboxes);
            origWastedSpace = computeWastedSpace(sizes, boxes);
            newWastedSpace = computeWastedSpace(sizes, nboxes);
            arSavings = 100.0 * ((origAvgAR / newAvgAR) - 1);
            wasteSavings =
                100.0
                    * (double) (origWastedSpace - newWastedSpace)
                    / computeSize(sizes);
            if (((wasteSavings > 0) && (arSavings > 0))
                || ((wasteSavings > -arSavings)))
            {
                numSnake3Layouts++;
                boxes = nboxes;
                debugPrintln("Snake 3:  boxes[0] = " + boxes[0]);
                debugPrintln("Snake 3:  boxes[1] = " + boxes[1]);
                debugPrintln("Snake 3:  boxes[2] = " + boxes[2]);
            }
        }

        if (sizes.length == 4)
        {
            // Try quad alg.
            nboxes = new Rectangle[4];
            ratio1 =
                (double) (sizes[0] + sizes[1])
                    / (sizes[0] + sizes[1] + sizes[2] + sizes[3]);
            if (growWide)
            {
                w1 = (int) Math.ceil(ratio1 * box.width);
                dim1 = computeTableLayoutGivenWidth(sizes[0], w1);
                dim2 = computeTableLayoutGivenWidth(sizes[1], w1);
                w2 = box.width - w1;
                dim3 = computeTableLayoutGivenWidth(sizes[2], w2);
                dim4 = computeTableLayoutGivenWidth(sizes[3], w2);

                bottom =
                    Math.max(
                        (dim1.height + dim2.height),
                        (dim3.height + dim4.height));
                dim1 = computeTableLayoutGivenHeight(sizes[0], dim1.height);
                dim2 =
                    computeTableLayoutGivenHeight(
                        sizes[1],
                        bottom - dim1.height);
                w1 = Math.max(dim1.width, dim2.width);
                dim3 = computeTableLayoutGivenHeight(sizes[2], dim3.height);
                dim4 =
                    computeTableLayoutGivenHeight(
                        sizes[3],
                        bottom - dim3.height);
                w2 = Math.max(dim3.width, dim4.width);

                nboxes[0] = new Rectangle(box.x, box.y, w1, dim1.height);
                nboxes[1] =
                    new Rectangle(box.x, box.y + dim1.height, w1, dim2.height);
                nboxes[2] = new Rectangle(box.x + w1, box.y, w2, dim3.height);
                nboxes[3] =
                    new Rectangle(
                        box.x + w1,
                        box.y + dim3.height,
                        w2,
                        dim4.height);
            }
            else
            {
                h1 = (int) Math.ceil(ratio1 * box.height);
                dim1 = computeTableLayoutGivenHeight(sizes[0], h1);
                dim2 = computeTableLayoutGivenHeight(sizes[1], h1);
                h2 = (int) Math.ceil((1.0 - ratio1) * box.height);
                dim3 = computeTableLayoutGivenHeight(sizes[2], h2);
                dim4 = computeTableLayoutGivenHeight(sizes[3], h2);

                right =
                    Math.max(
                        (dim1.width + dim2.width),
                        (dim3.width + dim4.width));
                dim1 = computeTableLayoutGivenWidth(sizes[0], dim1.width);
                dim2 =
                    computeTableLayoutGivenWidth(sizes[1], right - dim1.width);
                h1 = Math.max(dim1.height, dim2.height);
                dim3 = computeTableLayoutGivenWidth(sizes[2], dim3.width);
                dim4 =
                    computeTableLayoutGivenWidth(sizes[3], right - dim3.width);
                h2 = Math.max(dim3.height, dim4.height);

                nboxes[0] = new Rectangle(box.x, box.y, dim1.width, h1);
                nboxes[1] =
                    new Rectangle(box.x + dim1.width, box.y, dim2.width, h1);
                nboxes[2] = new Rectangle(box.x, box.y + h1, dim3.width, h2);
                nboxes[3] =
                    new Rectangle(
                        box.x + dim3.width,
                        box.y + h1,
                        dim4.width,
                        h2);
            }

            origAvgAR = computeAverageAspectRatio(boxes);
            newAvgAR = computeAverageAspectRatio(nboxes);
            origWastedSpace = computeWastedSpace(sizes, boxes);
            newWastedSpace = computeWastedSpace(sizes, nboxes);
            arSavings = 100.0 * ((origAvgAR / newAvgAR) - 1);
            wasteSavings =
                100.0
                    * (double) (origWastedSpace - newWastedSpace)
                    / computeSize(sizes);
            if (((wasteSavings > 0) && (arSavings > 0))
                || ((wasteSavings > -arSavings)))
            {
                numQuadLayouts++;
                boxes = nboxes;

                debugPrintln("Quad:  boxes[0] = " + boxes[0]);
                debugPrintln("Quad:  boxes[1] = " + boxes[1]);
                debugPrintln("Quad:  boxes[2] = " + boxes[2]);
                debugPrintln("Quad:  boxes[3] = " + boxes[3]);
            }

            // Then try 4 snake alg.
            nboxes = new Rectangle[4];
            if (growWide)
            {
                h = box.height;
                dim1 = computeTableLayoutGivenHeight(sizes[0], h);
                dim2 = computeTableLayoutGivenHeight(sizes[1], h);
                dim3 = computeTableLayoutGivenHeight(sizes[2], h);
                dim4 = computeTableLayoutGivenHeight(sizes[3], h);
                nboxes[0] = new Rectangle(box.x, box.y, dim1.width, h);
                nboxes[1] =
                    new Rectangle(box.x + dim1.width, box.y, dim2.width, h);
                nboxes[2] =
                    new Rectangle(
                        box.x + dim1.width + dim2.width,
                        box.y,
                        dim3.width,
                        h);
                nboxes[3] =
                    new Rectangle(
                        box.x + dim1.width + dim2.width + dim3.width,
                        box.y,
                        dim4.width,
                        h);
            }
            else
            {
                w = box.width;
                dim1 = computeTableLayoutGivenWidth(sizes[0], w);
                dim2 = computeTableLayoutGivenWidth(sizes[1], w);
                dim3 = computeTableLayoutGivenWidth(sizes[2], w);
                dim4 = computeTableLayoutGivenWidth(sizes[3], w);
                nboxes[0] = new Rectangle(box.x, box.y, w, dim1.height);
                nboxes[1] =
                    new Rectangle(box.x, box.y + dim1.height, w, dim2.height);
                nboxes[2] =
                    new Rectangle(
                        box.x,
                        box.y + dim1.height + dim2.height,
                        w,
                        dim3.height);
                nboxes[3] =
                    new Rectangle(
                        box.x,
                        box.y + dim1.height + dim2.height + dim3.height,
                        w,
                        dim4.height);
            }

            origAvgAR = computeAverageAspectRatio(boxes);
            newAvgAR = computeAverageAspectRatio(nboxes);
            origWastedSpace = computeWastedSpace(sizes, boxes);
            newWastedSpace = computeWastedSpace(sizes, nboxes);
            arSavings = 100.0 * ((origAvgAR / newAvgAR) - 1);
            wasteSavings =
                100.0
                    * (double) (origWastedSpace - newWastedSpace)
                    / computeSize(sizes);
            if (((wasteSavings > 0) && (arSavings > 0))
                || ((wasteSavings > -arSavings)))
            {
                numSnake4Layouts++;
                boxes = nboxes;

                debugPrintln("Snake 4:  boxes[0] = " + boxes[0]);
                debugPrintln("Snake 4:  boxes[1] = " + boxes[1]);
                debugPrintln("Snake 4:  boxes[2] = " + boxes[2]);
                debugPrintln("Snake 4:  boxes[3] = " + boxes[3]);
            }
        }

        if (sizes.length == 5)
        {
            // Try 5 snake alg.
            nboxes = new Rectangle[5];
            if (growWide)
            {
                h = box.height;
                dim1 = computeTableLayoutGivenHeight(sizes[0], h);
                dim2 = computeTableLayoutGivenHeight(sizes[1], h);
                dim3 = computeTableLayoutGivenHeight(sizes[2], h);
                dim4 = computeTableLayoutGivenHeight(sizes[3], h);
                dim5 = computeTableLayoutGivenHeight(sizes[4], h);
                nboxes[0] = new Rectangle(box.x, box.y, dim1.width, h);
                nboxes[1] =
                    new Rectangle(box.x + dim1.width, box.y, dim2.width, h);
                nboxes[2] =
                    new Rectangle(
                        box.x + dim1.width + dim2.width,
                        box.y,
                        dim3.width,
                        h);
                nboxes[3] =
                    new Rectangle(
                        box.x + dim1.width + dim2.width + dim3.width,
                        box.y,
                        dim4.width,
                        h);
                nboxes[4] =
                    new Rectangle(
                        box.x
                            + dim1.width
                            + dim2.width
                            + dim3.width
                            + dim4.width,
                        box.y,
                        dim5.width,
                        h);
            }
            else
            {
                w = box.width;
                dim1 = computeTableLayoutGivenWidth(sizes[0], w);
                dim2 = computeTableLayoutGivenWidth(sizes[1], w);
                dim3 = computeTableLayoutGivenWidth(sizes[2], w);
                dim4 = computeTableLayoutGivenWidth(sizes[3], w);
                dim5 = computeTableLayoutGivenWidth(sizes[4], w);
                nboxes[0] = new Rectangle(box.x, box.y, w, dim1.height);
                nboxes[1] =
                    new Rectangle(box.x, box.y + dim1.height, w, dim2.height);
                nboxes[2] =
                    new Rectangle(
                        box.x,
                        box.y + dim1.height + dim2.height,
                        w,
                        dim3.height);
                nboxes[3] =
                    new Rectangle(
                        box.x,
                        box.y + dim1.height + dim2.height + dim3.height,
                        w,
                        dim4.height);
                nboxes[4] =
                    new Rectangle(
                        box.x,
                        box.y
                            + dim1.height
                            + dim2.height
                            + dim3.height
                            + dim4.height,
                        w,
                        dim5.height);
            }

            origAvgAR = computeAverageAspectRatio(boxes);
            newAvgAR = computeAverageAspectRatio(nboxes);
            origWastedSpace = computeWastedSpace(sizes, boxes);
            newWastedSpace = computeWastedSpace(sizes, nboxes);
            arSavings = 100.0 * ((origAvgAR / newAvgAR) - 1);
            wasteSavings =
                100.0
                    * (double) (origWastedSpace - newWastedSpace)
                    / computeSize(sizes);
            if (((wasteSavings > 0) && (arSavings > 0))
                || ((wasteSavings > -arSavings)))
            {
                numSnake5Layouts++;
                boxes = nboxes;

                debugPrintln("Snake 5:  boxes[0] = " + boxes[0]);
                debugPrintln("Snake 5:  boxes[1] = " + boxes[1]);
                debugPrintln("Snake 5:  boxes[2] = " + boxes[2]);
                debugPrintln("Snake 5:  boxes[3] = " + boxes[3]);
                debugPrintln("Snake 5:  boxes[4] = " + boxes[4]);
            }
        }

        return boxes;
    }

    protected void setPivotIndexType(int indexType)
    {
        this.indexType = indexType;
    }

    protected int computePivotIndex(int[] sizes)
    {
        int i;
        int index = 0;
        int leftSize, rightSize;
        double ratio;
        double bestRatio = 0;
        int biggest;
        boolean first = true;

        switch (indexType)
        {
            case INDEX_BY_MIDDLE :
                index = (sizes.length - 1) / 2;
                break;
            case INDEX_BY_SPLIT_SIZE :
                leftSize = 0;
                rightSize = computeSize(sizes);

                for (i = 0; i < sizes.length; i++)
                {
                    ratio =
                        Math.max(
                            ((double) leftSize / rightSize),
                            ((double) rightSize / leftSize));
                    if (first || (ratio < bestRatio))
                    {
                        first = false;
                        bestRatio = ratio;
                        index = i;
                    }

                    leftSize += sizes[i];
                    rightSize -= sizes[i];
                }
                break;
            case INDEX_BY_BIGGEST :
                biggest = 0;
                for (i = 0; i < sizes.length; i++)
                {
                    if (first || (sizes[i] > biggest))
                    {
                        first = false;
                        biggest = sizes[i];
                        index = i;
                    }
                }
                break;
        }

        return index;
    }

    protected double computeAspectRatio(Rectangle rect)
    {
        return (double) rect.width / rect.height;
    }

    protected int computeArea(Rectangle rect)
    {
        return rect.width * rect.height;
    }

    protected int computeSize(int[] sizes)
    {
        int size = 0;
        if (sizes != null)
        {
            for (int i = 0; i < sizes.length; i++)
            {
                size += sizes[i];
            }
        }

        return size;
    }

    protected Rectangle computeUnion(Rectangle[] boxes)
    {
        int x1, x2, y1, y2;
        Rectangle box = new Rectangle(boxes[0]);

        for (int i = 1; i < boxes.length; i++)
        {
            x1 = Math.min(box.x, boxes[i].x);
            x2 = Math.max(box.x + box.width, boxes[i].x + boxes[i].width);
            y1 = Math.min(box.y, boxes[i].y);
            y2 = Math.max(box.y + box.height, boxes[i].y + boxes[i].height);

            box.setRect(x1, y1, x2 - x1, y2 - y1);
        }

        return box;
    }

    protected void translateBoxesTo(Rectangle[] boxes, int x, int y)
    {
        Rectangle box = computeUnion(boxes);
        int dx = x - box.x;
        int dy = y - box.y;

        for (int i = 0; i < boxes.length; i++)
        {
            boxes[i].x += dx;
            boxes[i].y += dy;
        }
    }

    protected void evenBoxWidth(Rectangle b1, Rectangle[] b2)
    {
        if (debugBoxEvening)
        {
            return;
        }

        if ((b1 == null) || (b2 == null))
        {
            return;
        }

        Rectangle[] b1boxes = new Rectangle[1];
        b1boxes[0] = b1;
        evenBoxWidth(b1boxes, b2, null);
    }

    protected void evenBoxWidth(Rectangle[] b1, Rectangle[] b2, Rectangle[] b3)
    {
        if (debugBoxEvening)
        {
            return;
        }

        int dx = 0;
        int right;
        int newRight;
        Rectangle[] b = null;
        ;
        Rectangle bBounds = null;
        ;
        Rectangle b1Bounds, b2Bounds, b3Bounds;

        if (b1 != null)
        {
            b1Bounds = computeUnion(b1);
        }
        else
        {
            b1Bounds = new Rectangle();
        }
        if (b2 != null)
        {
            b2Bounds = computeUnion(b2);
        }
        else
        {
            b2Bounds = new Rectangle();
        }
        if (b3 != null)
        {
            b3Bounds = computeUnion(b3);
        }
        else
        {
            b3Bounds = new Rectangle();
        }

        // First compute the preferred new width which is
        // the max of all the widths;
        newRight =
            Math.max(
                Math.max(
                    (b1Bounds.x + b1Bounds.width),
                    (b2Bounds.x + b2Bounds.width)),
                (b3Bounds.x + b3Bounds.width));

        // Then, fix up each region that is not the same width
        if (b1 != null)
        {
            if ((b1Bounds.x + b1Bounds.width) != newRight)
            {
                dx = newRight - (b1Bounds.x + b1Bounds.width);
                right = b1Bounds.x + b1Bounds.width;
                for (int i = 0; i < b1.length; i++)
                {
                    if ((b1[i].x + b1[i].width) == right)
                    {
                        b1[i].width += dx;
                    }
                }
            }
        }
        if (b2 != null)
        {
            if ((b2Bounds.x + b2Bounds.width) != newRight)
            {
                dx = newRight - (b2Bounds.x + b2Bounds.width);
                right = b2Bounds.x + b2Bounds.width;
                for (int i = 0; i < b2.length; i++)
                {
                    if ((b2[i].x + b2[i].width) == right)
                    {
                        b2[i].width += dx;
                    }
                }
            }
        }
        if (b3 != null)
        {
            if ((b3Bounds.x + b3Bounds.width) != newRight)
            {
                dx = newRight - (b3Bounds.x + b3Bounds.width);
                right = b3Bounds.x + b3Bounds.width;
                for (int i = 0; i < b3.length; i++)
                {
                    if ((b3[i].x + b3[i].width) == right)
                    {
                        b3[i].width += dx;
                    }
                }
            }
        }
    }

    protected void evenBoxHeight(Rectangle b1, Rectangle[] b2)
    {
        if (debugBoxEvening)
        {
            return;
        }

        if ((b1 == null) || (b2 == null))
        {
            return;
        }

        Rectangle[] b1boxes = new Rectangle[1];
        b1boxes[0] = b1;
        evenBoxHeight(b1boxes, b2, null);
    }

    protected void evenBoxHeight(
        Rectangle[] b1,
        Rectangle[] b2,
        Rectangle[] b3)
    {
        if (debugBoxEvening)
        {
            return;
        }

        int dy = 0;
        int bottom;
        int newBottom;
        Rectangle[] b = null;
        ;
        Rectangle bBounds = null;
        ;
        Rectangle b1Bounds, b2Bounds, b3Bounds;

        // Compute the actual bounds of the 3 regions
        if (b1 != null)
        {
            b1Bounds = computeUnion(b1);
        }
        else
        {
            b1Bounds = new Rectangle();
        }
        if (b2 != null)
        {
            b2Bounds = computeUnion(b2);
        }
        else
        {
            b2Bounds = new Rectangle();
        }
        if (b3 != null)
        {
            b3Bounds = computeUnion(b3);
        }
        else
        {
            b3Bounds = new Rectangle();
        }

        // Then, compute the preferred new height which is
        // the max of all the heights;
        newBottom =
            Math.max(
                Math.max(
                    (b1Bounds.y + b1Bounds.height),
                    (b2Bounds.y + b2Bounds.height)),
                (b3Bounds.y + b3Bounds.height));

        // Then, fix up each region that is not the same height
        if (b1 != null)
        {
            if ((b1Bounds.y + b1Bounds.height) != newBottom)
            {
                dy = newBottom - (b1Bounds.y + b1Bounds.height);
                bottom = b1Bounds.y + b1Bounds.height;
                for (int i = 0; i < b1.length; i++)
                {
                    if ((b1[i].y + b1[i].height) == bottom)
                    {
                        b1[i].height += dy;
                    }
                }
            }
        }
        if (b2 != null)
        {
            if ((b2Bounds.y + b2Bounds.height) != newBottom)
            {
                dy = newBottom - (b2Bounds.y + b2Bounds.height);
                bottom = b2Bounds.y + b2Bounds.height;
                for (int i = 0; i < b2.length; i++)
                {
                    if ((b2[i].y + b2[i].height) == bottom)
                    {
                        b2[i].height += dy;
                    }
                }
            }
        }
        if (b3 != null)
        {
            if ((b3Bounds.y + b3Bounds.height) != newBottom)
            {
                dy = newBottom - (b3Bounds.y + b3Bounds.height);
                bottom = b3Bounds.y + b3Bounds.height;
                for (int i = 0; i < b3.length; i++)
                {
                    if ((b3[i].y + b3[i].height) == bottom)
                    {
                        b3[i].height += dy;
                    }
                }
            }
        }
    }

    /**
     * Compute the total size of the objects between the specified indices, inclusive.
     */
    protected int computeSize(int[] sizes, int i1, int i2)
    {
        int size = 0;
        for (int i = i1; i <= i2; i++)
        {
            size += sizes[i];
        }

        return size;
    }

    protected double computeAverageAspectRatio(Rectangle[] rects)
    {
        double ar;
        double totAR = 0;
        int w, h;
        int i;
        int numRects = 0;

        for (i = 0; i < rects.length; i++)
        {
            w = rects[i].width;
            h = rects[i].height;
            if ((w != 0) && (h != 0))
            {
                ar = Math.max(((double) w / h), ((double) h / w));
                totAR += ar;
                numRects++;
            }
        }
        totAR /= numRects;

        return totAR;
    }

    protected int computeWastedSpace(int[] sizes, Rectangle[] rects)
    {
        int i;
        int area;
        int wastedSpace = 0;

        for (i = 0; i < rects.length; i++)
        {
            area = computeArea(rects[i]);
            wastedSpace += area - sizes[i];
        }

        return wastedSpace;
    }

    protected Dimension computeTableLayout(int numItems, double ar)
    {
        // debugPrintln("table layout: num="+numItems+", ar="+ar);
        int w, h;

        if (ar >= 1)
        {
            h = (int) Math.ceil(Math.sqrt(numItems / ar));
            if (h == 0)
            {
                h = 1;
            }
            w = (int) (numItems / h);
            if ((h * w) < numItems)
            {
                w++;
                h--;
            }
            while ((h * w) < numItems)
            {
                h++;
            }
        }
        else
        {
            w = (int) Math.ceil(Math.sqrt(numItems * ar));
            if (w == 0)
            {
                w = 1;
            }
            h = (int) (numItems / w);
            if ((h * w) < numItems)
            {
                h++;
                w--;
            }
            while ((h * w) < numItems)
            {
                w++;
            }
        }
        // debugPrintln("final layout: w="+w+", h="+h);

        return new Dimension(w, h);
    }

    protected Dimension computeTableLayoutGivenWidth(int numItems, int width)
    {
        int h;

        if (width < 1)
        {
            width = 1;
        }
        h = (int) Math.ceil((double) numItems / width);

        return new Dimension(width, h);
    }

    protected Dimension computeTableLayoutGivenHeight(int numItems, int height)
    {
        int w;

        if (height < 1)
        {
            height = 1;
        }
        w = (int) Math.ceil((double) numItems / height);

        return new Dimension(w, height);
    }

    static protected void debugPrint(String str)
    {
        if (debug)
        {
            System.out.print(str);
        }
    }

    static protected void debugPrintln(String str)
    {
        if (debug)
        {
            System.out.println(str);
        }
    }
}
