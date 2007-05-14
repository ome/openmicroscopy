/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.jsfcomp.chartcreator;

import java.io.IOException;
import java.util.Map;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

/**
 * @author Cagatay Civici
 * Component that retrieves the chart configuration and sends to the chartlet
 */

public class UIChart extends UIComponentBase {
	
	public static final String COMPONENT_TYPE = "net.sf.jsfcomp.chartcreator.UIChart";
	
	public static final String COMPONENT_FAMILY = "net.sf.jsfcomp.chartcreator";
	
	private Object datasource;

	private Integer width;

	private Integer height;

	private Integer alpha;

	private Integer explode;
	
	private Integer depth;

	private Integer startAngle;

	private String title;

	private String type;

	private String background;
	
	private String foreground;

	private String xlabel;

	private String ylabel;

	private String orientation;

	private String colors;

	private Boolean is3d;

	private Boolean legend;

	private Boolean antialias;

	private Boolean outline;
	
	private String styleClass;
	
	private String alt;
	
	private String imgTitle;
	
	private String onclick;
	
	private String ondblclick;
	
	private String onmousedown;
	
	private String onmouseup;
	
	private String onmouseover;
	
	private String onmousemove;
	
	private String onmouseout;
	
	private String onkeypress;
	
	private String onkeydown;
	
	private String onkeyup;
	
	private String output;
	
	private String usemap;
	
	public UIChart() {
		super();
		setRendererType(null);
	}

	public void encodeBegin(FacesContext context) throws IOException {
		setChartDataAtSession(context);
		
		ResponseWriter writer = context.getResponseWriter();
		writer.startElement("img", this);
		writer.writeAttribute("id", getClientId(context), null);
		writer.writeAttribute("width", String.valueOf(getWidth()), null);
		writer.writeAttribute("height", String.valueOf(getHeight()), null);
		writer.writeAttribute("src", ChartListener.CHART_REQUEST + ".jsf?ts=" + System.currentTimeMillis() + "&id=" + getClientId(context), null);
		ChartUtils.renderPassThruImgAttributes(writer, this);
	}
	
	public void encodeEnd(FacesContext context) throws IOException {
		context.getResponseWriter().endElement("img");
	}
	
	//creates and puts the chart data to session for this chart object
	private void setChartDataAtSession(FacesContext facesContext) {
		Map session = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();

		String clientId = getClientId(facesContext);
		ChartData data = new ChartData(this);
		session.put(clientId, data);
	}

	public String getFamily() {
		return COMPONENT_FAMILY;
	}

	/**
	 * Alpha attribute for pie charts
	 */
	public int getAlpha() {
		if(alpha != null)
			return alpha.intValue();
		
		ValueBinding vb = getValueBinding("alpha");
		Integer v = vb != null ? (Integer)vb.getValue(getFacesContext()) : null;
		return v != null ? v.intValue(): 100;
	}

	public void setAlpha(int alpha) {
		this.alpha = new Integer(alpha);
	}

	/**
	 * Alpha attribute for pie charts
	 */
	public int getExplode() {
		if(explode != null)
			return explode.intValue();
		
		ValueBinding vb = getValueBinding("explode");
		Integer v = vb != null ? (Integer)vb.getValue(getFacesContext()) : null;
		return v != null ? v.intValue(): 100;
	}

	public void setExplode(int explode) {
		this.explode = new Integer(explode);
	}

	/**
	 * Antialias attribute
	 */
	public boolean getAntialias() {
		if(antialias != null)
			return antialias.booleanValue();
		
		ValueBinding vb = getValueBinding("antialias");
		Boolean v = vb != null ? (Boolean)vb.getValue(getFacesContext()) : null;
		return v != null ? v.booleanValue(): false;
	}

	public void setAntialias(boolean antialias) {
		this.antialias = Boolean.valueOf(antialias);
	}

	/**
	 * Background attribute
	 */
	public String getBackground() {
		if(background != null)
			return background;
		
		ValueBinding vb = getValueBinding("background");
		String v = vb != null ? (String)vb.getValue(getFacesContext()) : null;
		return v != null ? v: "white";
	}

	public void setBackground(String background) {
		this.background = background;
	}
	
	/**
	 * Foreground attribute
	 */
	public String getForeground() {
		if(foreground != null)
			return foreground;
		
		ValueBinding vb = getValueBinding("foreground");
		String v = vb != null ? (String)vb.getValue(getFacesContext()) : null;
		return v != null ? v: "white";
	}

	public void setForeground(String foreground) {
		this.foreground = foreground;
	}

	/**
	 * 3D attribute
	 */
	public boolean getIs3d() {
		if(is3d != null)
			return is3d.booleanValue();
		
		ValueBinding vb = getValueBinding("is3d");
		Boolean v = vb != null ? (Boolean)vb.getValue(getFacesContext()) : null;
		return v != null ? v.booleanValue(): true;
	}

	public void setIs3d(boolean is3d) {
		this.is3d = Boolean.valueOf(is3d);
	}

	/**
	 * Colors attributes for bar charts
	 */
	public String getColors() {
		if(colors != null)
			return colors;
		
		ValueBinding vb = getValueBinding("colors");
		String v = vb != null ? (String)vb.getValue(getFacesContext()) : null;
		return v != null ? v: null;
	}

	public void setColors(String colors) {
		this.colors = colors;
	}

	/**
	 * DataSource attribute
	 */
	public Object getDatasource() {
		if(datasource != null)
			return datasource;
		
		ValueBinding vb = getValueBinding("datasource");
		Object v = vb != null ? vb.getValue(getFacesContext()) : null;
		return v != null ? v: null;
	}

	public void setDatasource(Object datasource) {
		this.datasource = datasource;
	}

	/**
	 * Depth attribute for pie charts
	 */
	public int getDepth() {
		if(depth != null)
			return depth.intValue();
		
		ValueBinding vb = getValueBinding("depth");
		Integer v = vb != null ? (Integer)vb.getValue(getFacesContext()) : null;
		return v != null ? v.intValue(): 15;
	}

	public void setDepth(int depth) {
		this.depth = new Integer(depth);
	}
	
	/**
	 * Width attribute
	 */
	public int getWidth() {
		if(width != null)
			return width.intValue();
		
		ValueBinding vb = getValueBinding("width");
		Integer v = vb != null ? (Integer)vb.getValue(getFacesContext()) : null;
		return v != null ? v.intValue(): 400;
	}

	public void setWidth(int width) {
		this.width = new Integer(width);
	}

	/**
	 * Height attribute
	 */
	public int getHeight() {
		if(height != null)
			return height.intValue();
		
		ValueBinding vb = getValueBinding("height");
		Integer v = vb != null ? (Integer)vb.getValue(getFacesContext()) : null;
		return v != null ? v.intValue(): 300;
	}

	public void setHeight(int height) {
		this.height = new Integer(height);
	}

	/**
	 * Legend attribute
	 */
	public boolean getLegend() {
		if(legend != null)
			return legend.booleanValue();
		
		ValueBinding vb = getValueBinding("legend");
		Boolean v = vb != null ? (Boolean)vb.getValue(getFacesContext()) : null;
		return v != null ? v.booleanValue(): true;
	}

	public void setLegend(boolean legend) {
		this.legend = Boolean.valueOf(legend);
	}

	/**
	 * Orientation attribute
	 */
	public String getOrientation() {
		if(orientation != null)
			return orientation;
		
		ValueBinding vb = getValueBinding("orientation");
		String v = vb != null ? (String)vb.getValue(getFacesContext()) : null;
		return v != null ? v: "vertical";
	}

	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}

	/**
	 * Outline attribute
	 */
	public boolean getOutline() {
		if(outline != null)
			return outline.booleanValue();
		
		ValueBinding vb = getValueBinding("outline");
		Boolean v = vb != null ? (Boolean)vb.getValue(getFacesContext()) : null;
		return v != null ? v.booleanValue(): true;
	}

	public void setOutline(boolean outline) {
		this.outline = Boolean.valueOf(outline);
	}

	/**
	 * Start Angle attribute for pie charts
	 */
	public int getStartAngle() {
		if(startAngle != null)
			return startAngle.intValue();
		
		ValueBinding vb = getValueBinding("startAngle");
		Integer v = vb != null ? (Integer)vb.getValue(getFacesContext()) : null;
		return v != null ? v.intValue(): 0;
	}

	public void setStartAngle(int startAngle) {
		this.startAngle = new Integer(startAngle);
	}

	/**
	 * Title attribute
	 */
	public String getTitle() {
		if(title != null)
			return title;
		
		ValueBinding vb = getValueBinding("title");
		String v = vb != null ? (String)vb.getValue(getFacesContext()) : null;
		return v != null ? v: null;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Type attribute
	 */
	public String getType() {
		if(type != null)
			return type;
		
		ValueBinding vb = getValueBinding("type");
		String v = vb != null ? (String)vb.getValue(getFacesContext()) : null;
		return v != null ? v: null;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * X-axis attribute
	 */
	public String getXlabel() {
		if(xlabel != null)
			return xlabel;
		
		ValueBinding vb = getValueBinding("xlabel");
		String v = vb != null ? (String)vb.getValue(getFacesContext()) : null;
		return v != null ? v: null;
	}

	public void setXlabel(String xlabel) {
		this.xlabel = xlabel;
	}

	/**
	 * Y-axis attribute
	 */
	public String getYlabel() {
		if(ylabel != null)
			return ylabel;
		
		ValueBinding vb = getValueBinding("ylabel");
		String v = vb != null ? (String)vb.getValue(getFacesContext()) : null;
		return v != null ? v: null;
	}

	public void setYlabel(String ylabel) {
		this.ylabel = ylabel;
	}

	/**
	 * StyleClass attribute
	 */
	public String getStyleClass() {
		if(styleClass != null)
			return styleClass;
		
		ValueBinding vb = getValueBinding("styleClass");
		String v = vb != null ? (String)vb.getValue(getFacesContext()) : null;
		return v != null ? v: null;
	}

	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}

	/**
	 * Alt attribute
	 */
	public String getAlt() {
		if(alt != null)
			return alt;
		
		ValueBinding vb = getValueBinding("alt");
		String v = vb != null ? (String)vb.getValue(getFacesContext()) : null;
		return v != null ? v: null;
	}

	public void setAlt(String alt) {
		this.alt = alt;
	}

	/**
	 * ImgTitle attribute
	 */
	public String getImgTitle() {
		if(imgTitle != null)
			return imgTitle;
		
		ValueBinding vb = getValueBinding("imgTitle");
		String v = vb != null ? (String)vb.getValue(getFacesContext()) : null;
		return v != null ? v: null;
	}

	public void setImgTitle(String imgTitle) {
		this.imgTitle = imgTitle;
	}

	/**
	 * Onclick attribute
	 */
	public String getOnclick() {
		if(onclick != null)
			return onclick;
		
		ValueBinding vb = getValueBinding("onclick");
		String v = vb != null ? (String)vb.getValue(getFacesContext()) : null;
		return v != null ? v: null;
	}

	public void setOnclick(String onclick) {
		this.onclick = onclick;
	}

	/**
	 * Ondblclick attribute
	 */
	public String getOndblclick() {
		if(ondblclick != null)
			return ondblclick;
		
		ValueBinding vb = getValueBinding("ondblclick");
		String v = vb != null ? (String)vb.getValue(getFacesContext()) : null;
		return v != null ? v: null;
	}

	public void setOndblclick(String ondblclick) {
		this.ondblclick = ondblclick;
	}

	/**
	 * Onkeydown attribute
	 */
	public String getOnkeydown() {
		if(onkeydown != null)
			return onkeydown;
		
		ValueBinding vb = getValueBinding("onkeydown");
		String v = vb != null ? (String)vb.getValue(getFacesContext()) : null;
		return v != null ? v: null;
	}

	public void setOnkeydown(String onkeydown) {
		this.onkeydown = onkeydown;
	}

	/**
	 * Onkeypress attribute
	 */
	public String getOnkeypress() {
		if(onkeypress != null)
			return onkeypress;
		
		ValueBinding vb = getValueBinding("onkeypress");
		String v = vb != null ? (String)vb.getValue(getFacesContext()) : null;
		return v != null ? v: null;
	}

	public void setOnkeypress(String onkeypress) {
		this.onkeypress = onkeypress;
	}

	/**
	 * Onkeyup attribute
	 */
	public String getOnkeyup() {
		if(onkeyup != null)
			return onkeyup;
		
		ValueBinding vb = getValueBinding("onkeyup");
		String v = vb != null ? (String)vb.getValue(getFacesContext()) : null;
		return v != null ? v: null;
	}

	public void setOnkeyup(String onkeyup) {
		this.onkeyup = onkeyup;
	}

	/**
	 * Onmousedown attribute
	 */
	public String getOnmousedown() {
		if(onmousedown != null)
			return onmousedown;
		
		ValueBinding vb = getValueBinding("onmousedown");
		String v = vb != null ? (String)vb.getValue(getFacesContext()) : null;
		return v != null ? v: null;
	}

	public void setOnmousedown(String onmousedown) {
		this.onmousedown = onmousedown;
	}

	/**
	 * Onmousemove attribute
	 */
	public String getOnmousemove() {
		if(onmousemove != null)
			return onmousemove;
		
		ValueBinding vb = getValueBinding("onmousemove");
		String v = vb != null ? (String)vb.getValue(getFacesContext()) : null;
		return v != null ? v: null;
	}

	public void setOnmousemove(String onmousemove) {
		this.onmousemove = onmousemove;
	}

	/**
	 * Onmouseout attribute
	 */
	public String getOnmouseout() {
		if(onmouseout != null)
			return onmouseout;
		
		ValueBinding vb = getValueBinding("onmouseout");
		String v = vb != null ? (String)vb.getValue(getFacesContext()) : null;
		return v != null ? v: null;
	}

	public void setOnmouseout(String onmouseout) {
		this.onmouseout = onmouseout;
	}

	/**
	 * Onmouseover attribute
	 */
	public String getOnmouseover() {
		if(onmouseover != null)
			return onmouseover;
		
		ValueBinding vb = getValueBinding("onmouseover");
		String v = vb != null ? (String)vb.getValue(getFacesContext()) : null;
		return v != null ? v: null;
	}

	public void setOnmouseover(String onmouseover) {
		this.onmouseover = onmouseover;
	}

	/**
	 * Onmouseup attribute
	 */
	public String getOnmouseup() {
		if(onmouseup != null)
			return onmouseup;
		
		ValueBinding vb = getValueBinding("onmouseup");
		String v = vb != null ? (String)vb.getValue(getFacesContext()) : null;
		return v != null ? v: null;
	}

	public void setOnmouseup(String onmouseup) {
		this.onmouseup = onmouseup;
	}

	/**
	 * Output attribute, default value is png
	 */
	public String getOutput() {
		if(output != null)
			return output;
		
		ValueBinding vb = getValueBinding("output");
		String v = vb != null ? (String)vb.getValue(getFacesContext()) : null;
		return v != null ? v: "png";
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getUsemap() {
		if(usemap != null)
			return usemap;
		
		ValueBinding vb = getValueBinding("usemap");
		String v = vb != null ? (String)vb.getValue(getFacesContext()) : null;
		return v != null ? v: null;
	}

	public void setUsemap(String usemap) {
		this.usemap = usemap;
	}
	
	public Object saveState(FacesContext context) {
	    Object values[] = new Object[35];
	    values[0] = super.saveState(context);
	    values[1] = datasource;
	    values[2] = width;
	    values[3] = height;
	    values[4] = alpha; 
	    values[5] = depth;
	    values[6] =	startAngle;
	    values[7] =	title;
	    values[8] =	type;
	    values[9] =	background;
	    values[10] = foreground;
	    values[11] = xlabel;
	    values[12] = ylabel;
	    values[13] = orientation;
	    values[14] = colors;
	    values[15] = is3d;
	    values[16] = legend;
	    values[17] = antialias;
	    values[18] = outline;
	    values[19] = styleClass;
	    values[20] = alt;
	    values[21] = imgTitle;
	    values[22] = onclick;
	    values[23] = ondblclick;
	    values[24] = onmousedown;
	    values[25] = onmouseup;
	    values[26] = onmouseover;
	    values[27] = onmousemove;
	    values[28] = onmouseout;
	    values[29] = onkeypress;
	    values[30] = onkeydown;
	    values[31] = onkeyup;
	    values[32] = output;
	    values[33] = usemap;
	    values[34] = explode;
	    return values;
	  }

	  public void restoreState(FacesContext context, Object state) {
	    Object values[] = (Object[]) state;
	    super.restoreState(context, values[0]);
	    this.datasource = values [1];
	    this.width = (Integer) values[2];
	    this.height = (Integer) values[3];
	    this.alpha = (Integer) values[4];
	    this.depth = (Integer) values[5];
	    this.startAngle = (Integer) values[6];
	    this.title = (String) values[7];
	    this.type = (String) values[8];
	    this.background = (String) values[9];
	    this.foreground = (String)values[10];
	    this.xlabel = (String) values[11];
	    this.ylabel = (String) values[12];
	    this.orientation = (String) values[13];
	    this.colors = (String) values[14];
	    this.is3d = (Boolean )values[15];
	    this.legend = (Boolean) values[16];
	    this.antialias = (Boolean) values[17];
	    this.outline = (Boolean) values[18];
	    this.styleClass = (String) values[19];
	    this.alt = (String) values[20];
	    this.imgTitle = (String) values[21];
	    this.onclick = (String) values[22];
	    this.ondblclick = (String) values[23];
	    this.onmousedown = (String) values[24];
	    this.onmouseup = (String) values[25];
	    this.onmouseover = (String) values[26];
	    this.onmousemove = (String) values[27];
	    this.onmouseout = (String) values[28];
	    this.onkeypress = (String) values[29];
	    this.onkeydown = (String) values[30];
	    this.onkeyup = (String) values[31];
	    this.output = (String) values[32];
	    this.usemap = (String) values[33];
	    this.explode = (Integer) values[34];
	  }
}


