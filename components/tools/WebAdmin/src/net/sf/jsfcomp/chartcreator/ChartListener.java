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
import java.io.OutputStream;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

/**
 * @author Cagatay Civici 
 * PhaseListener generating the chart image
 */
public class ChartListener implements PhaseListener {

	public final static String CHART_REQUEST = "chartcreatorrequest";

	public void afterPhase(PhaseEvent phaseEvent) {
		String rootId = phaseEvent.getFacesContext().getViewRoot().getViewId();

		if (rootId.indexOf(CHART_REQUEST) != -1) {
			handleChartRequest(phaseEvent);
		}
	}
	
	private void handleChartRequest(PhaseEvent phaseEvent) {
		FacesContext facesContext = phaseEvent.getFacesContext();
		ExternalContext externalContext = facesContext.getExternalContext();
		Map requestMap = externalContext.getRequestParameterMap();		
		Map sessionMap = externalContext.getSessionMap();

		String id = (String)requestMap.get("id");
		ChartData chartData = (ChartData) sessionMap.get(id);
		JFreeChart chart = ChartUtils.createChartWithType(chartData);
		ChartUtils.setGeneralChartProperties(chart, chartData);

		try {
			if(externalContext.getResponse() instanceof HttpServletResponse)
				writeChartWithServletResponse((HttpServletResponse)externalContext.getResponse(),chart, chartData);
			else if(externalContext.getResponse() instanceof RenderResponse)
				writeChartWithPortletResponse((RenderResponse)externalContext.getResponse(), chart, chartData);
		} catch (Exception e) {
			System.err.println(e.toString());
		} finally {
			emptySession(sessionMap, id);
			facesContext.responseComplete();
		}
	}

	public void beforePhase(PhaseEvent phaseEvent) {

	}

	public PhaseId getPhaseId() {
		return PhaseId.RESTORE_VIEW;
	}
	
	private void writeChartWithServletResponse(HttpServletResponse response, JFreeChart chart, ChartData chartData) throws IOException{
		OutputStream stream = response.getOutputStream();
		response.setContentType(ChartUtils.resolveContentType(chartData.getOutput()));
		writeChart(stream, chart, chartData);
	}
	
	private void writeChartWithPortletResponse(RenderResponse response, JFreeChart chart, ChartData chartData) throws IOException{
		OutputStream stream = response.getPortletOutputStream();
		response.setContentType(ChartUtils.resolveContentType(chartData.getOutput()));
		writeChart(stream, chart, chartData);
	}
	
	private void writeChart(OutputStream stream, JFreeChart chart, ChartData chartData) throws IOException{
		if(chartData.getOutput().equalsIgnoreCase("png"))
			ChartUtilities.writeChartAsPNG(stream, chart, chartData.getWidth(), chartData.getHeight());
		else if (chartData.getOutput().equalsIgnoreCase("jpeg"))
			ChartUtilities.writeChartAsJPEG(stream, chart, chartData.getWidth(), chartData.getHeight());
		
		stream.flush();
		stream.close();
	}
	
	private void emptySession(Map sessionMap, String id) {
		sessionMap.remove(id);
	}
}
