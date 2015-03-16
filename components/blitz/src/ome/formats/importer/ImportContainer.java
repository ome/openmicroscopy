/*
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package ome.formats.importer;

import static omero.rtypes.rbool;
import static omero.rtypes.rstring;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ome.formats.importer.transfers.FileTransfer;
import ome.formats.importer.transfers.UploadFileTransfer;
import ome.services.blitz.repo.path.ClientFilePathTransformer;
import ome.services.blitz.repo.path.FsFile;
import omero.constants.namespaces.NSAUTOCLOSE;
import omero.constants.namespaces.NSFILETRANSFER;
import omero.grid.ImportSettings;
import omero.model.Annotation;
import omero.model.BooleanAnnotation;
import omero.model.BooleanAnnotationI;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.Fileset;
import omero.model.FilesetEntry;
import omero.model.FilesetEntryI;
import omero.model.IObject;
import omero.model.NamedValue;
import omero.model.UploadJob;
import omero.model.UploadJobI;

public class ImportContainer
{
    private String reader;
    private String[] usedFiles;
    private long usedFilesTotalSize;
    private Boolean isSPW;
    private File file;
    private Double[] userPixels;
    private String userSpecifiedName;
    private String userSpecifiedDescription;
    private boolean doThumbnails = true;
    private boolean noStatsInfo = false;
    private boolean noPixelsChecksum = false;
    private List<Annotation> customAnnotationList;
    private IObject target;
    private String checksumAlgorithm;
    private ImportConfig config;

    public ImportContainer(File file, IObject target, Double[] userPixels,
            String reader, String[] usedFiles, Boolean isSPW) {
        this(null, file, target, userPixels, reader, usedFiles, isSPW);
    }

    public ImportContainer(ImportConfig config,
            File file, IObject target, Double[] userPixels,
            String reader, String[] usedFiles, Boolean isSPW) {
        this.config = config;
        this.file = file;
        this.target = target;
        this.userPixels = userPixels;
        this.reader = reader;
        this.usedFiles = usedFiles;
        this.isSPW = isSPW;
    }

    // Various Getters and Setters //

    /**
     * Retrieves whether or not we are performing thumbnail creation upon
     * import completion.
     * @return <code>true</code> if we are to perform thumbnail creation and
     * <code>false</code> otherwise.
     * @since OMERO Beta 4.3.0.
     */
    public boolean getDoThumbnails()
    {
        return doThumbnails;
    }

    /**
     * Sets whether or not we are performing thumbnail creation upon import
     * completion.
     * @param v <code>true</code> if we are to perform thumbnail creation and
     * <code>false</code> otherwise.
     * @since OMERO Beta 4.3.0.
     */
    public void setDoThumbnails(boolean v)
    {
        doThumbnails = v;
    }

    /**
     * Retrieves whether or not we disabling <code>StatsInfo</code> population.
     * @returns <code>true</code> if we are to disable <code>StatsInfo</code>
     * population. <code>false</code> otherwise.
     * @since OMERO 5.1.
     */
    public boolean getNoStatsInfo()
    {
        return noStatsInfo;
    }

    /**
     * Sets whether or not we disabling <code>StatsInfo</code> population.
     * @param v <code>true</code> if we are to disable <code>StatsInfo</code>
     * population. <code>false</code> otherwise.
     * @since OMERO 5.1.
     */
    public void setNoStatsInfo(boolean v)
    {
        noStatsInfo = v;
    }

    /**
     * Retrieves whether or not we disabling <code>Pixels</code> checksum
     * computation.
     * @returns <code>true</code> if we are to disable <code>Pixels</code>
     * checksum computation. <code>false</code> otherwise.
     * @since OMERO 5.1.
     */
    public boolean getNoPixelsChecksum()
    {
        return noPixelsChecksum;
    }

    /**
     * Sets whether or not we are disabling <code>Pixels</code> checksum
     * computation.
     * @param v <code>true</code> if we are to disable <code>Pixels</code>
     * checksum computation. <code>false</code> otherwise.
     * @since OMERO 5.1.
     */
    public void setNoPixelsChecksum(boolean v)
    {
        noPixelsChecksum = v;
    }

    /**
     * Retrieves the current custom image/plate name string.
     * @return As above. <code>null</code> if it has not been set.
     */
    public String getUserSpecifiedName()
    {
        return userSpecifiedName;
    }

    /**
     * Sets the custom image/plate name for import. If this value is left
     * null, the image/plate name supplied by Bio-Formats will be used.
     * @param v A custom image/plate name to use for all entities represented
     * by this container.
     */
    public void setUserSpecifiedName(String v)
    {
        userSpecifiedName = v;
    }

    /**
     * Retrieves the current custom image/plate description string.
     * @return As above. <code>null</code> if it has not been set.
     * @since OMERO Beta 4.2.1.
     */
    public String getUserSpecifiedDescription()
    {
        return userSpecifiedDescription;
    }

    /**
     * Sets the custom image/plate description for import. If this value is left
     * null, the image/plate description supplied by Bio-Formats will be used.
     * @param v A custom image/plate description to use for all images represented
     * by this container.
     * @since OMERO Beta 4.2.1.
     */
    public void setUserSpecifiedDescription(String v)
    {
        userSpecifiedDescription = v;
    }

    /**
     * The list of custom, user specified, annotations to link to all images
     * represented by this container.
     * @return See above.
     * @since OMERO Beta 4.2.1.
     */
    public List<Annotation> getCustomAnnotationList()
    {
        return customAnnotationList;
    }

    /**
     * Sets the list of custom, user specified, annotations to link to all
     * images represented by this container.
     * @param v The list of annotations to use.
     * @since OMERO Beta 4.2.1.
     */
    public void setCustomAnnotationList(List<Annotation> v)
    {
        customAnnotationList = v;
    }

    /**
     * Return the reader class name used for reading the contents of this
     * import container.
     * @return See above.
     */
    public String getReader() {
        return reader;
    }

    /**
     * Sets the reader class name used for reading the contents of this
     * import container.
     * @param reader Bio-Formats reader class name.
     */
    public void setReader(String reader) {
        this.reader = reader;
    }

    /**
     * Return a list of file names that belong to this import container.
     * @return See above.
     */
    public String[] getUsedFiles() {
        return usedFiles;
    }

    /**
     * Set the list of image file names that belong to this import container.
     * @param usedFiles
     */
    public void setUsedFiles(String[] usedFiles) {
        this.usedFiles = usedFiles;
    }

    /**
     * Returns the total size in bytes (based on <code>File.length()</code>)
     * of all files in this import container.
     * @return See above.
     */
    public long getUsedFilesTotalSize() {
        return usedFilesTotalSize;
    }

    /**
     * Return true if this import container contains a Screen/Plate/Well image
     * group. False otherwise.
     * @return See above.
     */
    public Boolean getIsSPW() {
        return isSPW;
    }

    /**
     * Set true if the import container is filled in with a Screen/Plate/Well
     * image structure. False otherwise.
     * @param isSPW True if container contains S/P/W, false otherwise.
     */
    public void setIsSPW(Boolean isSPW) {
        this.isSPW = isSPW;
    }

    /**
     * @return the File
     */
    public File getFile() {
        return file;
    }

    /**
     * Package-private setter added during the 4.1 release to fix name ordering.
     * A better solution would be to have a copy-constructor which also takes a
     * chosen file.
     */
    void setFile(File file) {
        this.file = file;
    }

    public IObject getTarget() {
        return target;
    }

    public void setTarget(IObject obj) {
        this.target = obj;
    }

    public Double[] getUserPixels() {
        return userPixels;
    }

    public void setUserPixels(Double[] userPixels)
    {
        this.userPixels = userPixels;
    }

    public String getChecksumAlgorithm() {
        return this.checksumAlgorithm;
    }

    public void setChecksumAlgorithm(String ca) {
        this.checksumAlgorithm = ca;
    }

    public void fillData(ImportSettings settings, Fileset fs,
            ClientFilePathTransformer sanitizer) throws IOException {
        fillData(config, settings, fs, sanitizer, null);
    }

    public void fillData(ImportSettings settings, Fileset fs,
            ClientFilePathTransformer sanitizer, FileTransfer transfer) throws IOException {
        fillData(config, settings, fs, sanitizer, transfer);
    }

    public void fillData(ImportConfig config, ImportSettings settings, Fileset fs,
            ClientFilePathTransformer sanitizer) throws IOException {
        fillData(config, settings, fs, sanitizer, null);
    }

    public void fillData(ImportConfig config, ImportSettings settings, Fileset fs,
            ClientFilePathTransformer sanitizer, FileTransfer transfer) throws IOException {

        if (config == null) {
            config = new ImportConfig(); // Lazily load
        }

        // TODO: These should possible be a separate option like
        // ImportUserSettings rather than misusing ImportContainer.
        settings.doThumbnails = rbool(getDoThumbnails());
        settings.noStatsInfo = rbool(getNoStatsInfo());
        settings.noPixelsChecksum = rbool(getNoPixelsChecksum());
        settings.userSpecifiedTarget = getTarget();
        settings.userSpecifiedName = getUserSpecifiedName() == null ? null
                : rstring(getUserSpecifiedName());
        settings.userSpecifiedDescription = getUserSpecifiedDescription() == null ? null
                : rstring(getUserSpecifiedDescription());
        settings.userSpecifiedAnnotationList = getCustomAnnotationList();

        // 5.0.x: pass an annotation
        if (config.autoClose.get()) {
            BooleanAnnotation ba = new BooleanAnnotationI();
            ba.setBoolValue(rbool(true));
            ba.setNs(rstring(NSAUTOCLOSE.value));
            settings.userSpecifiedAnnotationList.add(ba);
        }

        if (getUserPixels() != null) {
            Double[] source = getUserPixels();
            double[] target = new double[source.length];
            for (int i = 0; i < source.length; i++) {
                if (source[i] == null) {
                    target = null;
                    break;
                }
                target[i] = source[i];
            }
            settings.userSpecifiedPixels = target; // May be null.
        }

        // Fill used paths
        for (String usedFile : getUsedFiles()) {
            final FilesetEntry entry = new FilesetEntryI();
            final FsFile fsPath = sanitizer.getFsFileFromClientFile(new File(usedFile), Integer.MAX_VALUE);
            entry.setClientPath(rstring(fsPath.toString()));
            fs.addFilesetEntry(entry);
        }

        // Record any special file transfer
        if (transfer != null &&
                !transfer.getClass().equals(UploadFileTransfer.class)) {
            String type = transfer.getClass().getName();
            CommentAnnotation transferAnnotation = new CommentAnnotationI();
            transferAnnotation.setNs(omero.rtypes.rstring(NSFILETRANSFER.value));
            transferAnnotation.setTextValue(omero.rtypes.rstring(type));
            fs.linkAnnotation(transferAnnotation);
        }

        // Fill BF info
        final List<NamedValue> clientVersionInfo = new ArrayList<NamedValue>();
        clientVersionInfo.add(new NamedValue(ImportConfig.VersionInfo.BIO_FORMATS_READER.key, reader));
        config.fillVersionInfo(clientVersionInfo);
        UploadJob upload = new UploadJobI();
        upload.setVersionInfo(clientVersionInfo);
        fs.linkJob(upload);

    }

    public void updateUsedFilesTotalSize() {
        usedFilesTotalSize = 0;
        for (String filePath : usedFiles) {
            usedFilesTotalSize += new File(filePath).length();
        }
    }

}
