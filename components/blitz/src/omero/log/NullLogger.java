/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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
package omero.log;

/**
 * Empty {@link Logger} implementation whichs doesn't do anything
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class NullLogger implements Logger {

    @Override
    public void debug(Object originator, String logMsg) {
    }

    @Override
    public void debug(Object originator, LogMessage msg) {
    }

    @Override
    public void info(Object originator, String logMsg) {
    }

    @Override
    public void info(Object originator, LogMessage msg) {
    }

    @Override
    public void warn(Object originator, String logMsg) {
    }

    @Override
    public void warn(Object originator, LogMessage msg) {
    }

    @Override
    public void error(Object originator, String logMsg) {
    }

    @Override
    public void error(Object originator, LogMessage msg) {
    }

    @Override
    public void fatal(Object originator, String logMsg) {
    }

    @Override
    public void fatal(Object originator, LogMessage msg) {
    }

    @Override
    public String getLogFile() {
        return null;
    }

}
