/*
 * ome.util.tasks.Run
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package ome.util.tasks;

//Java imports
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

//Third-party libraries

//Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.system.ServiceFactory;

/** 
 * Command-line adapter which can run any task. {@link ServiceFactory} and 
 * {@link Task} configuration can be specified as arguments in the form
 * "key=value". The only mandatory argument for all tasks is the task name:
 * <code>
 *   java Run task=org.example.MyTask
 * </code>
 * However a search for tasks will also be performed under "ome.util.tasks". E.g.
 * <code>
 *   java Run task=admin.AddUserTask
 * </code>
 * resolves to ome.util.tasks.admin.AddUserTask.
 * 
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see		Configuration
 * @see     Task
 * @since   3.0-M4
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public class Run
{

	/** Parses the command line into a {@link Properties} instance which gets
	 * passed to {@link Configuration}. {@link Configuration#createTask()} is
	 * called and the returned {@link Task} instance is {@link Task#run() run}. 
	 */
	public static void main(String[] args) 
	{
		Properties props = parseArgs(args);
		Configuration opts = new Configuration(props);
		Task task = opts.createTask();
		task.run();
	}
	
	// ~ Helpers
	// =========================================================================
	
	protected static Properties parseArgs(String[] args)
	{
		Properties p = new Properties();
		if (args == null || args.length == 0) return p; // Early exit.
		
		List<String> argList = Arrays.asList(args);
		for (int i = 0; i < args.length; i++) {
			String[] parts = args[i].split("=");
			if (parts.length==1)
			{
				p.put(parts[0],"");
			} else if (parts.length==2) {
				p.put(parts[0],parts[1]);
			} else {
				throw new IllegalArgumentException(
						"Arguments can only have one \"=\".");
			}
		}
		return p;
	}
	
}
