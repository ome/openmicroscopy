/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package ols;


import java.util.LinkedHashMap;

public class Ontologies {
	
	private static Ontologies uniqueInstance;
	LinkedHashMap<String, String> supportedOntologies = new LinkedHashMap<String, String>();
	
	private static String[] OBO_REL_TERMS = {"DEVELOPS_FROM", 
		"ALT_ID",
		"BROAD",
		"PART_OF",
		"RELATED",
		"NARROW",
		"IS_A",
		"EXACT",
		"SYNONYM"};
	
	public static final String BS = "Biosapiens Annotations";
	public static final String BSPO = "Spatial Reference Ontology";
	public static final String BTO = "BRENDA tissue / enzyme source";
	public static final String CARO = "Common Anatomy Reference Ontology";
	public static final String CHEBI = "Chemical Entities of Biological Interest";
	public static final String CL = "Cell Type";
	public static final String DDANAT = "Dictyostelium discoideum Anatomy";
	public static final String DOID = "Human Disease";
	public static final String ECO = "Evidence Codes";
	public static final String EHDA = "Human Developmental Anatomy";
	public static final String EHDAA = "Human Developmental Anatomy";
	public static final String EMAP = "Mouse Gross Anatomy and Development";
	public static final String ENVO = "Environmental Ontology";
	public static final String EO = "Plant Environmental Conditions";
	public static final String EV = "eVOC (Expressed Sequence Annotation for Humans)";
	public static final String FAO = "Fungal Gross Anatomy";
	public static final String FBbi = "Biological Imaging Methods";
	public static final String FBbt = "Drosophila Gross Anatomy";
	public static final String FBcv = "Flybase Controlled Vocabulary";
	public static final String FBdv = "Drosophila Development";
	public static final String FIX = "Physico-Chemical Methods and Properties";
	public static final String GO = "Gene Ontology";
	public static final String GRO = "Cereal Plant Development";
	public static final String IEV = "Event (INOH)";
	public static final String IMR = "Molecule Role (INOH)";
	public static final String MA = "Mouse Adult Gross Anatomy";
	public static final String MFO = "Medaka Fish Anatomy and Development";
	public static final String MI = "Molecular Interaction (PSI MI 2.5)";
	public static final String MOD = "Protein Modifications (PSI-MOD)";
	public static final String MP = "Mammalian Phenotype";
	public static final String MPATH = "Mouse Pathology";
	public static final String NEWT = "NEWT UniProt Taxonomy Database";
	public static final String OBO_REL = "OBO Relationship Types";
	public static final String PATO = "Phenotypic qualities (properties)";
	public static final String PB = "Proteome Binders";
	public static final String PM = "Phenotypic manifestation (genetic context)";
	public static final String PO = "Plant Ontology (Structure, Growth and Developmental Stage)";
	public static final String PRIDE = "PRIDE Controlled Vocabulary";
	public static final String PSI = "Mass Spectroscopy CV (PSI-MS)";
	public static final String PW = "Pathway Ontology";
	public static final String REX = "Physico-Chemical Process";
	public static final String RO = "Multiple Alignment";
	public static final String SEP = "Separation Methods";
	public static final String SO = "Sequence Types and Features";
	public static final String SPD = "Spider Comparative Biology Ontology";
	public static final String TAIR = "Arabidopsis Development";
	public static final String TAO = "Teleost Anatomy and Development Ontology";
	public static final String TGMA = "Mosquito Gross Anatomy";
	public static final String TO = "Cereal Plant Trait";
	public static final String UO = "Unit Ontology";
	public static final String WBls = "C. elegans Development";
	public static final String XAO = "Xenopus anatomy and development";
	public static final String ZDB = "Zebrafish Anatomy and Development";
	public static final String ZEA = "Maize Gross Anatomy";
	
	private Ontologies() {
		supportedOntologies.put("BS", BS);
		supportedOntologies.put("BSPO", BSPO);
		supportedOntologies.put("BTO", BTO);
		supportedOntologies.put("CARO", CARO);
		supportedOntologies.put("CHEBI", CHEBI);
		supportedOntologies.put("CL", CL);
		supportedOntologies.put("DDANAT", DDANAT);
		supportedOntologies.put("DOID", DOID);
		supportedOntologies.put("ECO", ECO);
		supportedOntologies.put("EHDA", EHDA);
		supportedOntologies.put("EHDAA", EHDAA);
		supportedOntologies.put("EMAP", EMAP);
		supportedOntologies.put("ENVO", ENVO);
		supportedOntologies.put("EO", EO);
		supportedOntologies.put("EV", EV);
		supportedOntologies.put("FAO", FAO);
		supportedOntologies.put("FBbi", FBbi);
		supportedOntologies.put("FBbt", FBbt);
		supportedOntologies.put("FBcv", FBcv);
		supportedOntologies.put("FBdv", FBdv);
		supportedOntologies.put("FIX", FIX); 
		supportedOntologies.put("GO", GO); 
		supportedOntologies.put("GRO", GRO); 
		supportedOntologies.put("IEV", IEV); 
		supportedOntologies.put("IMR", IMR); 
		supportedOntologies.put("MA", MA); 
		supportedOntologies.put("MFO", MFO); 
		supportedOntologies.put("MI", MI); 
		supportedOntologies.put("MOD", MOD); 
		supportedOntologies.put("MP", MP); 
		supportedOntologies.put("MPATH", MPATH); 
		supportedOntologies.put("NEWT", NEWT); 
		supportedOntologies.put("OBO_REL", OBO_REL); 
		supportedOntologies.put("PATO", PATO); 
		supportedOntologies.put("PB", PB); 
		supportedOntologies.put("PM", PM); 
		supportedOntologies.put("PO", PO); 
		supportedOntologies.put("PRIDE", PRIDE); 
		supportedOntologies.put("PSI", PSI); 
		supportedOntologies.put("PW", PW); 
		supportedOntologies.put("REX", REX); 
		supportedOntologies.put("RO", RO); 
		supportedOntologies.put("SEP", SEP); 
		supportedOntologies.put("SO", SO); 
		supportedOntologies.put("SPD", SPD); 
		supportedOntologies.put("TAIR", TAIR); 
		supportedOntologies.put("TAO", TAO); 
		supportedOntologies.put("TGMA", TGMA); 
		supportedOntologies.put("TO", TO); 
		supportedOntologies.put("UO", UO); 
		supportedOntologies.put("WBls", WBls); 
		supportedOntologies.put("XAO", XAO); 
		supportedOntologies.put("ZDB", ZDB); 
		supportedOntologies.put("ZEA", ZEA); 
	}
	
	public static Ontologies getInstance() {
		
		if (uniqueInstance == null) {
			uniqueInstance = new Ontologies();
		}
		
		return uniqueInstance;
	}
	
	public LinkedHashMap<String, String> getSupportedOntologies() {
		return supportedOntologies;
	}

	public static String[] getOboRelationshipTerms() {
		return OBO_REL_TERMS;
	}
	
	public static String getOntologyIdFromTermId(String termId) {
		
		if (termId == null) {
			return null;
		}
		
		int colonIndex = termId.indexOf(":");
		if (colonIndex < 1) {
			throw new RuntimeException("Ontology term Id didn't contain : ");
		} 
		return termId.substring(0, colonIndex);
	}
}
