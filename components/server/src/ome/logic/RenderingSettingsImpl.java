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
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.Color;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;
import ome.parameters.Parameters;
import ome.services.util.OmeroAroundInvoke;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
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

	/** set injector. For use during configuration. Can only be called once. */
	public void setPixelsMetadata(IPixels metaService) {
		getBeanHelper().throwIfAlreadySet(this.pixelsMetadata, metaService);
		pixelsMetadata = metaService;
	}

	/** set injector. For use during configuration. Can only be called once. */
	public void setPixelsData(PixelsService dataService) {
		getBeanHelper().throwIfAlreadySet(this.pixelsData, dataService);
		pixelsData = dataService;
	}

	public Class<? extends ServiceInterface> getServiceInterface() {
		return IRenderingSettings.class;
	}

	public <T> void applySettingsToSet(long from, Class<T> toType, Set<T> to) {
		// TODO Auto-generated method stub

	}

	public Map<Boolean, List<Long>> applySettingsToCategory(long from, long to) {

		String sql = "select i from Image i "
				+ " left outer join fetch i.categoryLinks cil "
				+ " left outer join fetch cil.parent c "
				+ " where c.id = :id";
		Set<Image> images = new HashSet(iQuery.findAllByQuery(sql,
				new Parameters().addId(to)));

		return applySettings(from, images);
	}
	
	public Map<Boolean, List<Long>> applySettingsToProject(long from, long to) {

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

	public Map<Boolean, List<Long>> applySettingsToDataset(long from, long to) {
		String sql = "select i from Image i "
				+ " left outer join fetch i.datasetLinks dil "
				+ " left outer join fetch dil.parent d where d.id = :id";

		Set<Image> images = new HashSet(iQuery.findAllByQuery(sql,
				new Parameters().addId(to)));

		return applySettings(from, images);
	}

	protected Map<Boolean, List<Long>> applySettings(long from,
			Set<Image> images) {
		
		if (images.isEmpty())
			throw new ValidationException("Target does not contain any Images.");
		
		List<Long> trueList = new ArrayList<Long>();
		List<Long> falseList = new ArrayList<Long>();

		try {
			Iterator<Image> i = images.iterator();
			while (i.hasNext()) {
				Image image = (Image) i.next();
				try {
					boolean r = applySettingsToImage(from, image.getId());
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

	public boolean applySettingsToImage(long from, long to) {
		Image img = iQuery.get(Image.class, to);
		return applySettingsToPixel(from, img.getDefaultPixels().getId());
	}

	public boolean applySettingsToPixel(long from, long to) {

		// get rendering settings from RenderingDef to PixelId
		RenderingDef rdFrom = getRenderingDef(from);
		RenderingDef rdTo = pixelsMetadata.retrieveRndSettings(to);

		if (rdFrom == null)
			throw new ValidationException(
					"Rendering definition could not be retrieved");

		if (rdTo == null) {
			// create Rnd Settings.
			Pixels p = iQuery.get(Pixels.class, to);
			rdTo = new RenderingDef();
			rdTo.setQuantization(new QuantumDef());
			ArrayList<ChannelBinding> cbs = new ArrayList<ChannelBinding>();
			ChannelBinding binding;
			for (int i = 0; i < p.getSizeC(); i++) {
				binding = new ChannelBinding();
				binding.setColor(new Color());
				cbs.add(binding);
			}
			rdTo.setWaveRendering(cbs);
			rdTo.setPixels(p);
		}

		// TODO: Add more controls
		Integer size1 = rdFrom.getWaveRendering().size();
		Integer size2 = rdTo.getWaveRendering().size();
		if (size1.compareTo(size2) != 0) {
			throw new ValidationException("Channel are not equals");
		}
		rdTo.setDefaultT(rdFrom.getDefaultT());
		rdTo.setDefaultZ(rdFrom.getDefaultZ());
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
		ChannelBinding binding;
		ChannelBinding bindingTo;

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
			Color cFrom = binding.getColor();
			Color cTo = bindingTo.getColor();
			cTo.setAlpha(cFrom.getAlpha());
			cTo.setBlue(cFrom.getBlue());
			cTo.setGreen(cFrom.getGreen());
			cTo.setRed(cFrom.getRed());

		}

		pixelsMetadata.saveRndSettings(rdTo);

		return true;

	}

	public RenderingDef getRenderingSettings(long pixelsId) {
		return pixelsMetadata.retrieveRndSettings(pixelsId);
	}

	public void resetDefaults(long pixelsId) {
		// TODO Auto-generated method stub

	}

	protected RenderingDef getRenderingDef(long id) {
		return iQuery.get(RenderingDef.class, id);
	}

}
