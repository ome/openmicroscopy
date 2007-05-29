/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.tools;

import java.io.File;

import ome.system.OmeroContext;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.SQLExec;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.selectors.FilenameSelector;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * An ant task for checking the current database version.
 * Under construction.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class DbUpdateTask extends SQLExec {

	private OmeroContext ctx;

	private SimpleJdbcTemplate jdbc;

	public DbUpdateTask() {
        //ClassLoader cl = Thread.currentThread().getContextClassLoader(); 
		ctx = OmeroContext.getManagedServerContext(); 
		//Thread.currentThread().setContextClassLoader(DbUpdateTask.class.getClassLoader()); 
		jdbc = (SimpleJdbcTemplate) ctx.getBean("simpleJdbcTemplate"); 
		//Thread.currentThread().setContextClassLoader(cl); 		
	}

	private File _sqlDir;

	protected String _driver;

	@Override
	public void setDriver(String driver) {
		super.setDriver(driver);
		_driver = driver;
	}

	public void setSqldir(File dir) {
		_sqlDir = dir;
	}

	@Override
	public void execute() throws BuildException {
		if (_sqlDir == null) {
			throw new BuildException("No directory given.");
		} else if (!_sqlDir.canRead()) {
			throw new BuildException("Can't access sqldir:" + _sqlDir);
		} else if (!_sqlDir.isDirectory()) {
			throw new BuildException("Not a directory:" + _sqlDir);
		}

		boolean filesFound = true;
		while (filesFound) {

			String version__patch = getVersionPatch();
			log("Current version:patch = " + version__patch);
			log("Searching for files...");

			FilenameSelector fs = new FilenameSelector();
			fs.setName(String.format("%s__*__*.sql", version__patch));
			FileSet sql = new FileSet();
			sql.setDir(_sqlDir);
			sql.add(fs);
			DirectoryScanner scanner = sql.getDirectoryScanner(getProject());
			scanner.scan();
			String[] files = scanner.getIncludedFiles();
			if (files.length == 0) {
				filesFound = false;
			}
			for (int i = 0; i < files.length; i++) {
				String filename = sql.getDir(getProject()) + File.separator
						+ files[i];
				File file = new File(filename);
				if (!file.exists()) {
					log("File " + file + " not found.");
				} else {
					exec(file);
				}
			}
		}
		log("Database uptodate.");
	}

	String getVersionPatch() {
		String version = jdbc.queryForObject(
				"select currentversion from dbpatch order by id desc limit 1",
				String.class);
		long patch = jdbc
				.queryForLong("select currentpatch from dbpatch order by id desc limit 1");

		return version + "__" + patch;
	}

	void exec(File file) {
		this.setSrc(file);
		super.execute();
	}

}
