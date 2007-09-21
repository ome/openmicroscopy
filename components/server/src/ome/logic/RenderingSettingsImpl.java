package ome.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.api.IPixels;
import ome.api.IRenderingSettings;
import ome.api.ServiceInterface;
import ome.conditions.ApiUsageException;
import ome.conditions.ValidationException;
import ome.io.nio.PixelsService;
import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.core.LogicalChannel;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.Color;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;
import ome.parameters.Parameters;
import ome.services.util.OmeroAroundInvoke;
import omeis.providers.re.Renderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the {@link IRenderingSettings} I/F.
 * 
 * @author Aleksandra Tarkowska <a
 *         href="mailto:a.tarkowska@dundee.ac.uk">a.tarkowska@dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: 1696 $ $Date:
 *          2007/09/06 23:27:31 $) </small>
 * @since OME3.0
 */
@RevisionDate("$Date: 2007-09-06 14:29:18 +0100 (Thu, 06 Sep 2007) $")
@RevisionNumber("$Revision: 1792 $")
@Stateless
@Remote(IRenderingSettings.class)
@RemoteBinding(jndiBinding = "omero/remote/ome.api.IRenderingSettings")
@Local(IRenderingSettings.class)
@LocalBinding(jndiBinding = "omero/local/ome.api.IRenderingSettings")
@SecurityDomain("OmeroSecurity")
@TransactionManagement(TransactionManagementType.BEAN)
@Transactional
@Interceptors( { OmeroAroundInvoke.class, SimpleLifecycle.class })
public class RenderingSettingsImpl extends AbstractLevel2Service implements
		IRenderingSettings, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4383698215540637039L;
	
	/** Reference to the service used to retrieve the pixels data. */
	private PixelsService pixelsData;

	/** Reference to the service used to retrieve the pixels metadata. */
	private IPixels pixelsMetadata;

	/**
	 * Checks if the specified sets of pixels are compatible.
	 * Returns <code>true</code> if the pixels type is valid, <code>false</code>
	 * otherwise.
	 * 
	 * @param pFrom	The pixels set to copy the settings from.
	 * @param pTo	The pixels set to copy the settings to.
	 * @return See above.
	 */
	private boolean sanityCheckPixels(Pixels pFrom, Pixels pTo)
	{
		if (pTo == null || pFrom == null) return false;
			//throw new ValidationException("Pixels not valid.");
		String vFrom = pFrom.getPixelsType().getValue();
		String vTo = pTo.getPixelsType().getValue();
		if (!vFrom.equals(vTo)) return false;
			//throw new ValidationException("Pixels type must be the same.");
		if (pFrom.getSizeC().compareTo(pTo.getSizeC()) != 0) return false;
			//throw new ValidationException("The Pixels sets must have the " +
			//		"same number of channels.");
		if (pFrom.getSizeX().compareTo(pTo.getSizeX()) != 0)
			return false;
			//throw new ValidationException("The Pixels sets must have the " +
			//		"same number of pixels along the X-axis.");
		if (pFrom.getSizeY().compareTo(pTo.getSizeY()) != 0)
			return false;
			//throw new ValidationException("The Pixels sets must have the " +
			//		"same number of pixels along the Y-axis.");
		List lFrom = pFrom.getChannels();
		List lTo = pTo.getChannels();
		Iterator i = lFrom.iterator();
		Channel c;
		List<Integer> wavelengths = new ArrayList<Integer>(lFrom.size());
		//Problem no access to channel index.
		LogicalChannel lc;
		while (i.hasNext()) {
			c = (Channel) i.next();
			lc = c.getLogicalChannel();
			if (lc != null) 
				wavelengths.add(lc.getEmissionWave());
		}
		i = lTo.iterator();
		int r = 0;
		while (i.hasNext()) {
			c = (Channel) i.next();
			lc = c.getLogicalChannel();
			if (lc != null && wavelengths.contains(lc.getEmissionWave()))
				r++;
		}
		if (r != wavelengths.size()) return false;
			//throw new ValidationException("Emission wavelengths must match.");
		return true;
	}
	
	/**
	 * Applies the settings to the passed collection of Images.
	 * Returns a map with two keys: A <code>True</code> key whose value is 
	 * a list of image's ids the settings were successfully applied to, a
	 * <code>False</code> key whose value is 
	 * a list of image's ids the settings could not be applied to.
	 * 
	 * @param from		The image to copy the settings from.
	 * @param images	The collection of image to copy the settings to.
	 * @return See above.
	 */
	protected Map<Boolean, List<Long>> applySettings(long from,
			Set<Image> images) {
		
		if (images.isEmpty())
			throw new ValidationException("Target does not contain any Images.");
		
		List<Long> trueList = new ArrayList<Long>();
		List<Long> falseList = new ArrayList<Long>();

		try {
			Iterator<Image> i = images.iterator();
			Image image;
			boolean r;
			while (i.hasNext()) {
				image = i.next();
				try {
					r = applySettingsToImage(from, image.getId());
					if (r)
						trueList.add(image.getId());
					else
						falseList.add(image.getId());
				} catch (Exception e) {
					falseList.add(image.getId());
				}
			}
		} catch (NoSuchElementException expected) {
			throw new ApiUsageException(
					"There are no elements assigned to the Dataset");
		}

		Map<Boolean, List<Long>> result = new HashMap<Boolean, List<Long>>();
		result.put(Boolean.TRUE, trueList);
		result.put(Boolean.FALSE, falseList);

		return result;
	}
	
	/**
	 * Returns the rendering settings specified by the passed id.
	 * 
	 * @param id The rendering settings id.
	 * @return See above.
	 */
	protected RenderingDef getRenderingDef(long id) {
		return iQuery.get(RenderingDef.class, id);
	}
	
	/** 
	 * Sets injector. For use during configuration. Can only be called once. 
	 * 
	 * @param metaService The value to set.
	 */
	public void setPixelsMetadata(IPixels metaService) {
		getBeanHelper().throwIfAlreadySet(this.pixelsMetadata, metaService);
		pixelsMetadata = metaService;
	}

	/** 
	 * Sets injector. For use during configuration. Can only be called once. 
	 * 
	 * @param dataService The value to set.
	 */
	public void setPixelsData(PixelsService dataService) {
		getBeanHelper().throwIfAlreadySet(this.pixelsData, dataService);
		pixelsData = dataService;
	}

	/**
	 * Returns the interface this implementation is for.
	 * @see AbstractLevel2Service#getServiceInterface()
	 */
	public Class<? extends ServiceInterface> getServiceInterface() {
		return IRenderingSettings.class;
	}

	/**
	 * Implemented as specified by the {@link IRenderingSettings} I/F
	 * @see IRenderingSettings#applySettingsToSet(long, Class, Set)
	 */
	public <T> void applySettingsToSet(long from, Class<T> toType, Set<T> to) {
		// TODO Auto-generated method stub

	}

	/**
	 * Implemented as specified by the {@link IRenderingSettings} I/F
	 * @see IRenderingSettings#applySettingsToCategory(long, long)
	 */
	public Map<Boolean, List<Long>> applySettingsToCategory(long from, long to) 
	{

		String sql = "select i from Image i "
				+ " left outer join fetch i.categoryLinks cil "
				+ " left outer join fetch cil.parent c "
				+ " where c.id = :id";
		Set<Image> images = new HashSet(iQuery.findAllByQuery(sql,
				new Parameters().addId(to)));

		return applySettings(from, images);
	}
	
	/**
	 * Implemented as specified by the {@link IRenderingSettings} I/F
	 * @see IRenderingSettings#applySettingsToProject(long, long)
	 */
	public Map<Boolean, List<Long>> applySettingsToProject(long from, long to)
	{

		String sql = "select i from Image i "
				+ " left outer join fetch i.datasetLinks dil "
				+ " left outer join fetch dil.parent d "
				+ " left outer join fetch d.projectLinks pdl "
				+ " left outer join fetch pdl.parent pr "
				+ " where pr.id = :id";
		Set<Image> images = new HashSet(iQuery.findAllByQuery(sql,
				new Parameters().addId(to)));

		return applySettings(from, images);
	}

	/**
	 * Implemented as specified by the {@link IRenderingSettings} I/F
	 * @see IRenderingSettings#applySettingsToDataset(long, long)
	 */
	public Map<Boolean, List<Long>> applySettingsToDataset(long from, long to)
	{
		String sql = "select i from Image i "
				+ " left outer join fetch i.datasetLinks dil "
				+ " left outer join fetch dil.parent d where d.id = :id";

		Set<Image> images = new HashSet(iQuery.findAllByQuery(sql,
				new Parameters().addId(to)));

		return applySettings(from, images);
	}

	/**
	 * Implemented as specified by the {@link IRenderingSettings} I/F
	 * @see IRenderingSettings#applySettingsToImage(long, long)
	 */
	public boolean applySettingsToImage(long from, long to) {
		Image img = iQuery.get(Image.class, to);
		return applySettingsToPixel(from, img.getDefaultPixels().getId());
	}
	
	/**
	 * Implemented as specified by the {@link IRenderingSettings} I/F
	 * @see IRenderingSettings#applySettingsToPixel(long, long)
	 */
	public boolean applySettingsToPixel(long from, long to) {

		Pixels pTo = pixelsMetadata.retrievePixDescription(to);
		Pixels pFrom = pixelsMetadata.retrievePixDescription(from);
		
		boolean b = sanityCheckPixels(pFrom, pTo);
		if (!b) return false;
		// get  rendering settings from RenderingDef to PixelId
		RenderingDef rdFrom = getRenderingDef(from);
		RenderingDef rdTo = pixelsMetadata.retrieveRndSettings(to);

		if (rdFrom == null) return false;
		//pixelsMetadata.
		//Controls
		if (rdTo == null) {
			// create Rnd Settings.
			rdTo = Renderer.createNewRenderingDef(pTo);
		}
		rdTo.setModel(rdFrom.getModel());

		QuantumDef qDefFrom = rdFrom.getQuantization();
		QuantumDef qDefTo = rdTo.getQuantization();

		qDefTo.setBitResolution(qDefFrom.getBitResolution());
		qDefTo.setCdEnd(qDefFrom.getCdEnd());
		qDefTo.setCdStart(qDefFrom.getCdStart());

		List wavesFrom = rdFrom.getWaveRendering();
		List wavesTo = rdTo.getWaveRendering();
		Iterator i = wavesFrom.iterator();
		Iterator iTo = wavesTo.iterator();
		ChannelBinding binding, bindingTo;
		Color cFrom, cTo;
		while (i.hasNext()) {
			binding = (ChannelBinding) i.next();
			bindingTo = (ChannelBinding) iTo.next();

			// channel on or off
			bindingTo.setActive(binding.getActive());
			// mapping coefficient
			bindingTo.setCoefficient(binding.getCoefficient());
			// type of map used
			bindingTo.setFamily(binding.getFamily());
			// lower bound of the pixels intensity interval
			bindingTo.setInputStart(binding.getInputStart());
			// upper bound of the pixels intensity interval
			bindingTo.setInputEnd(binding.getInputEnd());
			// turn on or off the noise reduction algo.
			bindingTo.setNoiseReduction(binding.getNoiseReduction());
			// color used
			cFrom = binding.getColor();
			cTo = bindingTo.getColor();
			cTo.setAlpha(cFrom.getAlpha());
			cTo.setBlue(cFrom.getBlue());
			cTo.setGreen(cFrom.getGreen());
			cTo.setRed(cFrom.getRed());

		}
		pixelsMetadata.saveRndSettings(rdTo);
		return true;
	}
	
	/**
	 * Implemented as specified by the {@link IRenderingSettings} I/F
	 * @see IRenderingSettings#getRenderingSettings(long)
	 */
	public RenderingDef getRenderingSettings(long pixelsId) {
		return pixelsMetadata.retrieveRndSettings(pixelsId);
	}

	/**
	 * Implemented as specified by the {@link IRenderingSettings} I/F
	 * @see IRenderingSettings#resetDefaults(long)
	 */
	public void resetDefaults(long pixelsId) {
		//TODO: not yet implemented.
	}

}
