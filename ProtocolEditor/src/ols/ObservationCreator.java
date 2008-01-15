package ols;

import java.util.ArrayList;

import tree.DataField;
import xmlMVC.XMLModel;


public class ObservationCreator {

	
	public static ArrayList<Observation> getObservations(XMLModel model) {
		
		ArrayList<Observation> observations = new ArrayList<Observation>();
		
		ArrayList<DataField> observationFields = model.getObservationFields();
		
		for (DataField field: observationFields) {
			observations.add(new Observation(field));
		}
		
		return observations;
	}
	
	
	public static void testPrintAllObservations(XMLModel model) {
		
		ArrayList<Observation> observations = getObservations(model);
		
		for (Observation obs: observations) {
			printObservation(obs);
		}
		
	}
	
	public static void printObservation(Observation observation) {
		
		System.out.println();
		System.out.println("Observation: \n" + "   Name:   " + observation.getObservationName());
		System.out.println("   Obs Type:   " + observation.getDataType());
		System.out.println("   AttributeTermId:   " + observation.getAttributeTermId());
		System.out.println("   EntityTermId:   " + observation.getEntityTermId());
		System.out.println("   UnitsTermId:   " + observation.getUnitsTermId());
		System.out.println();
	}
}
