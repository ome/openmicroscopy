<?xml version = "1.0" encoding = "UTF-8"?>
<!--
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#
# Copyright (C) 2009-2012 Open Microscopy Environment
#       Massachusetts Institute of Technology,
#       National Institutes of Health,
#       University of Dundee,
#       University of Wisconsin at Madison
#
#    This library is free software; you can redistribute it and/or
#    modify it under the terms of the GNU Lesser General Public
#    License as published by the Free Software Foundation; either
#    version 2.1 of the License, or (at your option) any later version.
#
#    This library is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#    Lesser General Public License for more details.
#
#    You should have received a copy of the GNU Lesser General Public
#    License along with this library; if not, write to the Free Software
#    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
#
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-->

<!--
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# Written by:  Andrew Patterson: ajpatterson at lifesci.dundee.ac.uk
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:OME="http://www.openmicroscopy.org/Schemas/OME/2011-06"
	xmlns:Bin="http://www.openmicroscopy.org/Schemas/BinaryFile/2011-06"
	xmlns:SPW="http://www.openmicroscopy.org/Schemas/SPW/2011-06"
	xmlns:SA="http://www.openmicroscopy.org/Schemas/SA/2011-06"
	xmlns:ROI="http://www.openmicroscopy.org/Schemas/ROI/2011-06"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:xml="http://www.w3.org/XML/1998/namespace"
	exclude-result-prefixes="OME Bin SPW SA ROI"
	xmlns:exsl="http://exslt.org/common"
	extension-element-prefixes="exsl" version="1.0">
	
	<xsl:variable name="newOMENS">http://www.openmicroscopy.org/Schemas/OME/2012-06</xsl:variable>
	<xsl:variable name="newSPWNS">http://www.openmicroscopy.org/Schemas/SPW/2012-06</xsl:variable>
	<xsl:variable name="newBINNS"
		>http://www.openmicroscopy.org/Schemas/BinaryFile/2012-06</xsl:variable>
	<xsl:variable name="newROINS">http://www.openmicroscopy.org/Schemas/ROI/2012-06</xsl:variable>
	<xsl:variable name="newSANS">http://www.openmicroscopy.org/Schemas/SA/2012-06</xsl:variable>
	
	<xsl:output method="xml" indent="yes"/>
	<xsl:preserve-space elements="*"/>
	
	<!-- default value for non-numerical value when transforming the attribute of concrete shape -->
	<xsl:variable name="numberDefault" select="1"/>
	
	
	<!-- Actual schema changes -->
	
	<xsl:template match="OME:AcquiredDate">
		<xsl:element name="OME:AcquisitionDate" namespace="{$newOMENS}">
			<xsl:apply-templates select="node()"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="OME:Experimenter">
		<xsl:element name="OME:Experimenter" namespace="{$newOMENS}">
			<!-- Strip DisplayName -->
			<xsl:for-each select="@* [not(name() = 'DisplayName')]">
				<xsl:attribute name="{local-name(.)}">
					<xsl:value-of select="."/>
				</xsl:attribute>
			</xsl:for-each>
			<xsl:apply-templates select="node()"/>
		</xsl:element>
	</xsl:template>

	<xsl:template match="OME:Group">
		<xsl:element name="OME:ExperimenterGroup" namespace="{$newOMENS}">
			<xsl:apply-templates select="@*[not(local-name(.)='ID')]"/>
			<xsl:for-each select="@* [name() = 'ID']">
				<xsl:attribute name="ID">ExperimenterGroup:<xsl:value-of select="."/></xsl:attribute>
			</xsl:for-each>
			<xsl:apply-templates select="node()"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="OME:GroupRef">
		<xsl:element name="OME:ExperimenterGroupRef" namespace="{$newOMENS}">
			<xsl:apply-templates select="@*[not(local-name(.)='ID')]"/>
			<xsl:for-each select="@* [name() = 'ID']">
				<xsl:attribute name="ID">ExperimenterGroup:<xsl:value-of select="."/></xsl:attribute>
			</xsl:for-each>
			<xsl:apply-templates select="node()"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="SPW:ImageRef">
		<xsl:element name="OME:ImageRef" namespace="{$newOMENS}">
			<xsl:apply-templates select="@*|node()"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="SPW:Well">
		<xsl:element name="SPW:Well" namespace="{$newSPWNS}">
			<xsl:for-each select="@* [not(name() = 'Status')]">
				<xsl:attribute name="{local-name(.)}">
					<xsl:value-of select="."/>
				</xsl:attribute>
			</xsl:for-each>
			<xsl:for-each select="@* [name() = 'Status']">
				<xsl:attribute name="Type">
					<xsl:value-of select="."/>
				</xsl:attribute>
			</xsl:for-each>
			<xsl:apply-templates select="node()"/>
		</xsl:element>
	</xsl:template>

	<xsl:template match="ROI:Shape">
		<xsl:element name="ROI:Shape" namespace="{$newROINS}">
			<xsl:for-each select="@* [not(name() = 'Fill' or name() = 'Stroke' or name() = 'Name'  or name() = 'MarkerStart' or name() = 'MarkerEnd' or name() = 'Label' or name() = 'Transform')]">
				<xsl:attribute name="{local-name(.)}">
					<xsl:value-of select="."/>
				</xsl:attribute>
			</xsl:for-each>
			<xsl:for-each select="@* [name() = 'Fill']">
				<xsl:attribute name="FillColor">
					<xsl:value-of select="."/>
				</xsl:attribute>
			</xsl:for-each>
			<xsl:for-each select="@* [name() = 'Stroke']">
				<xsl:attribute name="StrokeColor">
					<xsl:value-of select="."/>
				</xsl:attribute>
			</xsl:for-each>
			<xsl:for-each select="@* [name() = 'Label']">
				<xsl:attribute name="Text">
					<xsl:value-of select="."/>
				</xsl:attribute>
			</xsl:for-each>
			<!-- end of attributes -->
			<xsl:for-each select="@* [name() = 'Transform']">
				<xsl:element name="Transform">
					<xsl:value-of select="."/>
				</xsl:element>
			</xsl:for-each>
			<xsl:for-each select="* [not(local-name(.) = 'Description' or local-name(.) = 'Path')]">
				<xsl:apply-templates select="."/>
			</xsl:for-each>
			<xsl:for-each select="* [local-name(.) = 'Path']">
				<xsl:comment>Path elements cannot be converted to 2012-06 Schema, they are not supported.</xsl:comment>
			</xsl:for-each>
		</xsl:element>
	</xsl:template>
	

	<xsl:template match="ROI:Text">
		<xsl:element name="ROI:Label" namespace="{$newROINS}">
			<xsl:apply-templates select="@*"/>
			<xsl:for-each select="* [not(local-name(.) = 'Value')]">
				<xsl:element name="{local-name(.)}" namespace="{$newROINS}">
					<xsl:apply-templates select="@*"/>
					<xsl:value-of select="."/>
				</xsl:element>
			</xsl:for-each>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="ROI:Line">
		<xsl:element name="ROI:Line" namespace="{$newROINS}">
			<xsl:apply-templates select="@*"/>
			<!-- Fix markers -->
			<xsl:for-each select="../@MarkerStart">
				<xsl:attribute name="MarkerStart"><xsl:value-of select="../@MarkerStart"/></xsl:attribute>
			</xsl:for-each>
			<xsl:for-each select="../@MarkerEnd">
				<xsl:attribute name="MarkerEnd"><xsl:value-of select="../@MarkerEnd"/></xsl:attribute>
			</xsl:for-each>
		</xsl:element>
	</xsl:template>

	<xsl:template match="ROI:Polyline">
		<!-- if closed -->
		<xsl:if test="@Closed = 'true'">
			<xsl:element name="ROI:Polygon" namespace="{$newROINS}">
				<xsl:apply-templates select="@* [ not(name() = 'Closed')]"/>
			</xsl:element>
		</xsl:if>
		<!-- if not closed -->
		<xsl:if test="@Closed = 'false'">
			<xsl:element name="ROI:Polyline" namespace="{$newROINS}">
				<xsl:apply-templates select="@* [ not(name() = 'Closed')]"/>
				<!-- Fix markers -->
				<xsl:for-each select="../@MarkerStart">
					<xsl:attribute name="MarkerStart"><xsl:value-of select="../@MarkerStart"/></xsl:attribute>
				</xsl:for-each>
				<xsl:for-each select="../@MarkerEnd">
					<xsl:attribute name="MarkerEnd"><xsl:value-of select="../@MarkerEnd"/></xsl:attribute>
				</xsl:for-each>
			</xsl:element>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="OME:OTF">
		<xsl:comment>OTF elements cannot be converted to 2012-06 Schema, they are not supported.</xsl:comment>
	</xsl:template>
	
	<xsl:template match="OME:OTFRef">
		<xsl:comment>OTFRef elements cannot be converted to 2012-06 Schema, they are not supported.</xsl:comment>
	</xsl:template>
	
	
	<!-- Rewriting all namespaces -->
	
	<xsl:template match="OME:OME">
		<OME xmlns="http://www.openmicroscopy.org/Schemas/OME/2012-06"
			xmlns:Bin="http://www.openmicroscopy.org/Schemas/BinaryFile/2012-06"
			xmlns:SPW="http://www.openmicroscopy.org/Schemas/SPW/2012-06"
			xmlns:SA="http://www.openmicroscopy.org/Schemas/SA/2012-06"
			xmlns:ROI="http://www.openmicroscopy.org/Schemas/ROI/2012-06"
			xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			xsi:schemaLocation="http://www.openmicroscopy.org/Schemas/OME/2012-06 
			../../../InProgress/ome.xsd">
			<xsl:apply-templates/>
		</OME>
	</xsl:template>
	
	<xsl:template match="OME:*">
		<xsl:element name="{name()}" namespace="{$newOMENS}">
			<xsl:apply-templates select="@*|node()"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="Bin:*">
		<xsl:element name="{name()}" namespace="{$newBINNS}">
			<xsl:apply-templates select="@*|node()"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="SA:*">
		<xsl:element name="{name()}" namespace="{$newSANS}">
			<xsl:apply-templates select="@*|node()"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="SPW:*">
		<xsl:element name="{name()}" namespace="{$newSPWNS}">
			<xsl:apply-templates select="@*|node()"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="ROI:*">
		<xsl:element name="{name()}" namespace="{$newROINS}">
			<xsl:apply-templates select="@*|node()"/>
		</xsl:element>
	</xsl:template>
	
	<!-- Default processing -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>