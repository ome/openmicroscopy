package org.ome.srv.net;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ome.interfaces.ImageService;
import org.ome.model.LSID;
import org.ome.model.LSObject;
import org.ome.model.Vocabulary;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * MultiActionController for the image list/upload UI.
 * @author Juergen Hoeller
 * @since 07.01.2004
 */
public class ImageController extends MultiActionController {

	private ImageService imageService;

	public void setImageService(ImageService imageService) {
		this.imageService = imageService;
	}

	public ModelAndView retrieveImage(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    try {
		    String idString =request.getParameter("id");
	        int id = Integer.parseInt(idString);
	        LSObject lsobject = this.imageService.retrieveImage(new LSID(Vocabulary.NS+"img"+id));
	        return new ModelAndView("lsobject", "lsobject",lsobject);
	    } catch (NumberFormatException nfe){
	        return new ModelAndView("failure");
	    }
	    
	    
	}
	
}
