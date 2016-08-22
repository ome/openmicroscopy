/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
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

    /** Number of rows and columns composing the file.*/
    private int rows, columns;

    private String firstTok;

    /**
     * Counts the lines.
     * @param r The file reader.
     * @throws IOException
     */
    private void countLines(Reader r) throws IOException {
        StreamTokenizer tok = new StreamTokenizer(r);
        int wordsPerRow = 0, wordsInPreviousRow = 0;
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
                    rows++;
                    if (wordsPerRow == 0)
                        rows--;  // ignore empty rows
                    if (rows == 1 && wordsPerRow > 0)
                        columns = wordsPerRow;
                    if (rows > 1 && wordsPerRow != 0 &&
                            wordsPerRow != wordsInPreviousRow)
                        throw new IOException("Row "+rows+ " is not "
                                + "the same length as the first row.");
                    if (wordsPerRow != 0)
                        wordsInPreviousRow = wordsPerRow;
                    wordsPerRow = 0;
                    break;
                case StreamTokenizer.TT_WORD:
                    wordsPerRow++;
            }
        }
        if (wordsPerRow == columns) 
            rows++; // last row does not end with EOL
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
        rows = 0;
        columns = 1;
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
            int offset2 = ys*columns+x;
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
        if (columns*rows == 0)
            return 0;
        int height = rows;
        float[] pixels = new float[columns*height];
        read(r, pixels);
        r.close();
        int firstRowNaNCount = 0;
        for (int i = 0; i < columns; i++) {
            if (i < pixels.length && Float.isNaN(pixels[i]))
                firstRowNaNCount++;
        }

        float[] values = pixels;
        if (firstRowNaNCount == columns && !("NaN".equals(firstTok)||
                "nan".equals(firstTok))) { // assume first row is header
            height = rows-1;
            values = crop(0, 1, columns, height, pixels);
        }

        if (columns < 3 || columns > 4 || height < SIZE|| height > SIZE+2)
            return 0;
        int x = columns == 4 ? 1 : 0;
        int y = height > SIZE ? 1: 0;
        //crop again
        float[] result = crop(x, y, 3, SIZE, values);
        columns = 3;
        for (int i = 0; i< SIZE; i++) {
            reds[i] = (byte) result[i*columns];
            greens[i] = (byte) result[i*columns+1];
            blues[i] = (byte) result[i*columns+2];
        }
        return SIZE;
    }
}
