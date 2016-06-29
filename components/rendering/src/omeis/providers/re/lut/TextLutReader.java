/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package omeis.providers.re.lut;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;


/**
 * Read the text Lut. After code from ImageJ.
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.3
 */
class TextLutReader
    extends BasicLutReader
{

    /** Values used during the reading.*/
    private int lines, width;

    private String firstTok;

    /**
     * Counts the lines.
     * @param r The file reader.
     * @throws IOException
     */
    private void countLines(Reader r) throws IOException {
        StreamTokenizer tok = new StreamTokenizer(r);
        int wordsPerLine=0, wordsInPreviousLine=0;
        tok.resetSyntax();
        tok.wordChars(43, 43);
        tok.wordChars(45, 127);
        tok.whitespaceChars(0, 42);
        tok.whitespaceChars(44, 44);
        tok.whitespaceChars(128, 255);
        tok.eolIsSignificant(true);

        while (tok.nextToken() != StreamTokenizer.TT_EOF) {
            switch (tok.ttype) {
                case StreamTokenizer.TT_EOL:
                    lines++;
                    if (wordsPerLine == 0)
                        lines--;  // ignore empty lines
                    if (lines == 1 && wordsPerLine > 0)
                        width = wordsPerLine;
                    if (lines > 1 && wordsPerLine != 0 &&
                            wordsPerLine != wordsInPreviousLine)
                        throw new IOException("Line "+lines+ " is not "
                                + "the same length as the first line.");
                    if (wordsPerLine != 0)
                        wordsInPreviousLine = wordsPerLine;
                    wordsPerLine = 0;
                    break;
                case StreamTokenizer.TT_WORD:
                    wordsPerLine++;
            }
        }
        if (wordsPerLine == width) 
            lines++; // last line does not end with EOL
    }

    /**
     * Parses the value.
     *
     * @param value The value to pass.
     * @param defaultValue The default value.
     * @return See above
     */
    private double parseDouble(String value, double defaultValue)
    {
        if (value == null) return defaultValue;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {}
        return defaultValue;
    }

    /**
     * Reader the file and populates the specified array.
     *
     * @param r The reader.
     * @param pixels The array storing the values.
     * @throws IOException
     */
    private void read(Reader r, float[] pixels) throws IOException {
        int size = pixels.length;
        StreamTokenizer tok = new StreamTokenizer(r);
        tok.resetSyntax();
        tok.wordChars(43, 43);
        tok.wordChars(45, 127);
        tok.whitespaceChars(0, 42);
        tok.whitespaceChars(44, 44);
        tok.whitespaceChars(128, 255);
        int i = 0;
        int inc = size/20;
        if (inc < 1) {
            inc = 1;
        }
        while (tok.nextToken() != StreamTokenizer.TT_EOF) {
            if (tok.ttype == StreamTokenizer.TT_WORD) {
                if (i == 0) {
                    firstTok = tok.sval;
                }
                pixels[i++] = (float) parseDouble(tok.sval, Double.NaN);
                if (i == size)
                    break;
            }
        }
    }

    /** Creates a new instance.*/
    TextLutReader()
    {
        lines = 0;
        width = 1;
    }

    /**
     * Crops. Returns the extracted values as an array.
     *
     * @param x The x-value of the area to crop.
     * @param y The y-value of the area to crop.
     * @param w The width of the area to crop.
     * @param h The height of the area to crop.
     * @param pixels The array to crop.
     * @return See above.
     */
    private float[] crop(int x, int y, int w, int h, float[] pixels) {
        float[] pixels2 = new float[w*h];
        for (int ys = y; ys < y+h; ys++) {
            int offset1 = (ys-y)*w;
            int offset2 = ys*w+x;
            for (int xs = 0; xs < w; xs++)
                pixels2[offset1++] = pixels[offset2++];
        }
        return pixels2;
    }

    /**
     * Reads the file. The <code>raw</code> flag is not used.
     */
    @Override
    int read(File file, boolean raw) throws Exception {
        String path = file.getAbsolutePath();
        Reader r = new BufferedReader(new FileReader(path));
        countLines(r);
        r.close();
        r = new BufferedReader(new FileReader(path));
        if (width*lines == 0)
            return 0;
        int height = lines;
        float[] pixels = new float[width*height];
        read(r, pixels);
        r.close();
        int firstRowNaNCount = 0;
        for (int i = 0; i < width; i++) {
            if (i < pixels.length && Float.isNaN(pixels[i]))
                firstRowNaNCount++;
        }

        float[] values = pixels;
        if (firstRowNaNCount == width && !("NaN".equals(firstTok)||
                "nan".equals(firstTok))) { // assume first row is header
            height = lines-1;
            values = crop(0, 1, width, height, pixels);
        }

        if (width < 3 || width > 4 || height < SIZE|| height > SIZE+2)
            return 0;
        int x = width == 4 ? 1 : 0;
        int y = height > SIZE ? 1: 0;
        //crop again
        float[] result = crop(x, y, 3, SIZE, values);
        width = 3;
        for (int i = 0; i< SIZE; i++) {
            reds[i] = (byte) result[i*width];
            greens[i] = (byte) result[i*width+1];
            blues[i] = (byte) result[i*width+2];
        }
        return SIZE;
    }
}
