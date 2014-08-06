/*
 * Copyright (C) 2012-2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.blitz.repo.path;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * An analog of {@link File} representing an FS repository file-path.
 * The file-path is relative to the root of the repository. As with
 * {@link File}, instances of this class are immutable.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0
 */
public class FsFile {
    /** the separator character for delimiting repository path components */
    public static char separatorChar = '/';

    /** the FsFile that has no path components */
    public static FsFile emptyPath = new FsFile();
    
    /* the components of this path */
    private final List<String> components;
    /* the string representation of this path
     * (i.e. its components with separatorChar in between each pair) */
    private final String path;

    /**
     * Split path components by the given separator.
     * Adjacent separators are considered to be only one.
     * @param path the path to split
     * @param separator the separator by which to split
     * @return the path components
     */
    private static List<String> splitComponents(String path, char separator) {
        final String[] splitBySeparator = path.split("\\" + separator);
        final List<String> components = new ArrayList<String>(splitBySeparator.length);
        for (final String component : splitBySeparator) {
            if (!"".equals(component)) 
                components.add(component);
        }
        return components;
    }
    
    /**
     * Construct an instance.
     * @param components the components of the path to which this instance corresponds, may be null
     */
    public FsFile(Collection<String> components) {
        if (components == null || components.isEmpty()) {
            this.components = Collections.emptyList();
            this.path = "";
        } else {
            this.components = Collections.unmodifiableList(new ArrayList<String>(components));
            final StringBuilder pathBuilder = new StringBuilder();
            for (final String component : components) {
                if (StringUtils.isEmpty(component))
                    throw new IllegalArgumentException("each path component must have content");
                if (component.indexOf(separatorChar) != -1)
                    throw new IllegalArgumentException("path components may not contain a path separator");
                pathBuilder.append(component);
                pathBuilder.append(FsFile.separatorChar);
            }
            pathBuilder.setLength(pathBuilder.length() - 1);
            this.path = pathBuilder.toString();
        }
    }
    
    /**
     * Construct an instance.
     * @param components the components of the path to which this instance corresponds
     */
    public FsFile(String... components) {
        this(Arrays.asList(components));
    }
    
    /**
     * Return a view of the last <em>n</em> elements of a list. If the list is
     * shorter than <em>n</em>, returns a view of the whole list without padding it.
     * @param list a list
     * @param count how many (<em>n</em>) elements to view, must be positive
     * @return a view of the list's last <em>n</em> elements
     */
    private static <X> List<X> tailOf(List<X> list, int count) {
        if (count < 0)
            throw new IllegalArgumentException("count must be positive");
        final int listSize = list.size();
        final int startIndex = listSize > count ? listSize - count : 0;
        return list.subList(startIndex, listSize);
    }
    
    /**
     * Construct an instance by trimming parent directories from an existing instance 
     * such that the depth does not exceed the given maximum.
     * Allows ignoring of client-side parent directories beyond those specified by the user.
     * @param file an existing instance
     * @param maxComponentCount the number of child components of the instance, 
     * including filename, above which parents should be ignored
     */
    public FsFile(FsFile file, int maxComponentCount) {
        this(FsFile.tailOf(file.components, maxComponentCount));
    }
    
    /**
     * Construct an instance.
     * @param file the file to whose absolute path this instance corresponds
     */
    public FsFile(File file) {
        this(splitComponents(file.getAbsolutePath(), File.separatorChar));
    }
    
    /**
     * Construct an instance.
     * @param path the path that this instance's string representation must match
     */
    public FsFile(String path) {
        this(splitComponents(path, FsFile.separatorChar));
    }
    
    /**
     * Transform each path component with the given transformer.
     * @param componentTransformer a transformer
     * @return the transformed path
     */
    public FsFile transform(Function<String, String> componentTransformer) {
        return new FsFile(Lists.transform(this.components, componentTransformer));
    }
    
    /**
     * Find the relative path of this path from a given parent.
     * Allows adjustment of absolute paths to the repository's root directory.
     * Matches path component names case-sensitively.
     * @param file a parent path (may be the same as this one)
     * @return the relative path, or null if none exists
     */
    public FsFile getPathFrom(FsFile file) {
        final Iterator<String> container = file.components.iterator();
        final Iterator<String> contained = this.components.iterator();
        for (;;) {
            if (!container.hasNext()) {
                /* now descended into parent */
                final List<String> childComponents = new ArrayList<String>();
                while (contained.hasNext())
                    childComponents.add(contained.next());
                return new FsFile(childComponents);
            } else if (!contained.hasNext())
                /* the "parent" is actually a child of this instance */
                return null;
            else if (!container.next().equals(contained.next())) {
                /* neither this instance nor the "parent" contain the other */
                return null;
            }
        }
    }
    
    /**
     * Concatenate paths.
     * @param files the paths to concatenate
     * @return the concatenated path
     */
    public static FsFile concatenate(FsFile... files) {
        int size = 0;
        for (final FsFile file : files)
            size += file.components.size();
        final List<String> components = new ArrayList<String>(size);
        for (final FsFile file : files)
            components.addAll(file.components);
        return new FsFile(components);
    }
    
    /**
     * Get the path components of this instance.
     * @return the path components, never null
     */
    public List<String> getComponents() {
        return this.components;
    }
    
    /**
     * Convert this instance to a {@link File}
     * relative to the given {@link File}.
     * @param file parent directory, may be null for a relative return value, 
     * but actually expected to be the repository's root directory
     * @return where this instance should be located in the server-side filesystem
     */
    public File toFile(File file) {
        for (final String component : this.components)
            file = new File(file, component);
        return file;
    }
    
    /** 
     * {@inheritDoc}
     * Provides repository path with components separated by {@link #separatorChar}.
     * Suitable for displaying to the user and for constructing a new instance.
     */
    @Override
    public String toString() {
        return this.path;
    }
    
    /** 
     * {@inheritDoc}
     * Instances are equal if their string representations match.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (!(object instanceof FsFile))
            return false;
        return this.path.equals(((FsFile) object).path);
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.path.hashCode() * 97;
    }
}
