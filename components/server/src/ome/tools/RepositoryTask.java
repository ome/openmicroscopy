package ome.tools;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ome.api.IRepositoryInfo;
import ome.system.OmeroContext;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * Class implementation of various mechanised tasks, database queries, file I/O,
 * etc. This class is used by the public services provided by IRepositoryInfo
 * <p>
 * Copyright 2007 Glencoe Software Inc. All rights reserved. Use is subject to
 * license terms supplied in LICENSE.txt <p/>
 * 
 * @author David L. Whitehurst &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:david@glencoesoftware.com">david@glencoesoftware.com</a>
 * @version $Revision$
 * @since 3.0
 * @see IRepositoryInfo
 */
public class RepositoryTask {

	private OmeroContext ctx;
	private SimpleJdbcTemplate template;

	/**
	 * Constructor
	 */
	public RepositoryTask() {
		ctx = OmeroContext.getManagedServerContext();
		template = (SimpleJdbcTemplate) ctx.getBean("simpleJdbcTemplate");
	}
	
	/**
	 * This public method is used to return a list of file ids that require
	 * deletion from the disk repository.
	 * 
	 * @return List<Long> representing the ids for files that were deleted
	 */
	public List<Long> getFileIds() {
		List<Long> list;

		String sql = "select entityid from eventlog " + 
		"where action = 'DELETE' and entitytype = 'ome.model.core.OriginalFile'";

		ParameterizedRowMapper<Long> mapper = new ParameterizedRowMapper<Long>() {
			public Long mapRow(ResultSet resultSet, int rowNum)
					throws SQLException {
				Long id = new Long(resultSet.getString(1));
				return id;
			}
		};

		list = template.query(sql, mapper, (Object[]) null);

		return list;
	}
	
	/**
	 * This public method is used to return a list of pixel ids that require
	 * deletion from the disk repository.
	 * 
	 * @return List<Long> representing the ids for pixels that were deleted
	 */
	public List<Long> getPixelIds() {
		List<Long> list;

		String sql = "select entityid from eventlog " + 
		"where action = 'DELETE' and entitytype = 'ome.model.core.Pixels'";

		ParameterizedRowMapper<Long> mapper = new ParameterizedRowMapper<Long>() {
			public Long mapRow(ResultSet resultSet, int rowNum)
					throws SQLException {
				Long id = new Long(resultSet.getString(1));
				return id;
			}
		};

		list = template.query(sql, mapper, (Object[]) null);

		return list;
	}
	
	/**
	 * This public method is used to return a list of thumbnail ids that require
	 * deletion from the disk repository.
	 * 
	 * @return List<Long> representing the ids for thumbnails that were deleted
	 */
	public List<Long> getThumbnailIds() {
		List<Long> list;

		String sql = "select entityid from eventlog " + 
			"where action = 'DELETE' and entitytype = 'ome.model.display.Thumbnail'";
		
		ParameterizedRowMapper<Long> mapper = new ParameterizedRowMapper<Long>() {
			public Long mapRow(ResultSet resultSet, int rowNum)
					throws SQLException {
				Long id = new Long(resultSet.getString(1));
				return id;
			}
		};

		list = template.query(sql, mapper, (Object[]) null);

		return list;
	}

	/**
	 * Private method used for testing only. Will be removed later.
	 * 
	 * @return
	 */
	public List<Long> getTestIds() {
		ArrayList<Long> list = new ArrayList<Long>();
		Long var1 = new Long(63);
		Long var2 = new Long(2613);
		Long var3 = new Long(3331);

		list.add(var1);
		list.add(var2);
		list.add(var3);

		return list;
	}

}
