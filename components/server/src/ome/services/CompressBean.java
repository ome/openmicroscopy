package ome.services;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import ome.api.IRepositoryInfo;
import ome.api.ServiceInterface;
import ome.api.ICompress;
import ome.io.nio.ThumbnailService;
import ome.logic.AbstractLevel2Service;
import ome.model.display.Thumbnail;

public class CompressBean extends AbstractLevel2Service implements ICompress {

	/** The default compression quality in fractional percent. */
    public static final float DEFAULT_COMPRESSION_QUALITY = 0.85F;
    
    /** The ROMIO thumbnail service. */
    private transient ThumbnailService ioService;
    
    /** the disk space checking service */
    private transient IRepositoryInfo iRepositoryInfo;
    
    /** is file service checking for disk overflow */
    private transient boolean diskSpaceChecking;
    
    /**
     * I/O service (ThumbnailService) Bean injector.
     * 
     * @param ioService
     *            a <code>ThumbnailService</code>.
     */
    public void setIoService(ThumbnailService ioService) {
        getBeanHelper().throwIfAlreadySet(this.ioService, ioService);
        this.ioService = ioService;
    }
    
    /**
     * Disk Space Usage service Bean injector
     * @param iRepositoryInfo
     *   		  	an <code>IRepositoryInfo</code>
     */
    public final void setIRepositoryInfo(IRepositoryInfo iRepositoryInfo) {
        getBeanHelper().throwIfAlreadySet(this.iRepositoryInfo, iRepositoryInfo);
        this.iRepositoryInfo = iRepositoryInfo;
    }
    
	public Class<? extends ServiceInterface> getServiceInterface() {
		return ICompress.class;
	}
	
    /**
     * overriden to allow Spring to set boolean
     * @param checking
     */
    public CompressBean(boolean checking) {
    	this.diskSpaceChecking = checking;
    }
    
	public boolean isDiskSpaceChecking() {
		return diskSpaceChecking;
	}

	public void setDiskSpaceChecking(boolean diskSpaceChecking) {
		this.diskSpaceChecking = diskSpaceChecking;
	}

    public void compressThumbnailToStream(BufferedImage image,
            OutputStream outputStream) throws IOException {
        // Get a JPEG image writer
        ImageWriter jpegWriter =
        	ImageIO.getImageWritersByFormatName("jpeg").next();

        // Setup the compression value from (0.05, 0.75 and 0.95)
        ImageWriteParam iwp = jpegWriter.getDefaultWriteParam();
        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        iwp.setCompressionQuality(DEFAULT_COMPRESSION_QUALITY);

        // Write the JPEG to our ByteArray stream
    	ImageOutputStream imageOutputStream = null;
        try {
        	imageOutputStream = ImageIO.createImageOutputStream(outputStream);
        	jpegWriter.setOutput(imageOutputStream);
        	jpegWriter.write(null, new IIOImage(image, null, null), iwp);
        } finally {
        	if (imageOutputStream != null)
        		imageOutputStream.close();
        }
    }


}
