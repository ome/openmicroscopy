<?xml version="1.0" encoding="UTF-8"?>
<!-- $Id$ -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output method="html" indent="yes" doctype-public="--//W3C//DTD HTML 4.01 Transitional//EN"/>
<!-- Stylesheet used to generate an AurigaDoc xml file. -->
<!-- A: COMMON VARIABLES -->
<xsl:variable name="title" select="/xdoc/xdoc-info/title"/>

<xsl:variable name="revision" select="/xdoc/xdoc-info/revision/@number"/>
<xsl:variable name="last-modified" select="/xdoc/xdoc-info/last-modified/@date"/>

<!-- Root -->

<xsl:template match="xdoc">
<document>
<xsl:call-template name="document-meta-info"/>
<xsl:call-template name="document-formatting-info"/>
<xsl:call-template name="document-header"/>	
<xsl:call-template name="document-body"/>	
<xsl:call-template name="document-footer"/>	
</document>
</xsl:template>

<!-- 
*************************************************************************
This template generates the document-meta-info, information on the document.
*************************************************************************
-->
<xsl:template name="document-meta-info">
<document-meta-info>
<title><xsl:value-of select="$title"/></title>
<attribute name="Project">
<a href="http://www.openmicroscopy.org/">Open Microscopy Environment</a>
</attribute>
<xsl:variable name="number-authors">
<xsl:value-of select="count(/xdoc/xdoc-info/author)"/>
</xsl:variable>
<xsl:choose>
<xsl:when test="$number-authors = 1">
<attribute name="Author">
<a>
<xsl:attribute name="href">
<xsl:value-of select="concat('mailto:', /xdoc/xdoc-info/author/@email)"/>
</xsl:attribute>
<xsl:value-of select="/xdoc/xdoc-info/author/@name"/>
</a>
</attribute>
</xsl:when>
<xsl:when test="$number-authors &gt; 1">
<attribute name="Authors">
<xsl:for-each select="/xdoc/xdoc-info/author">
<a>
<xsl:attribute name="href">
<xsl:value-of select="concat('mailto:', @email)"/>
</xsl:attribute>
<xsl:value-of select="@name"/>
</a>
<xsl:if test="position() != last()">, </xsl:if>
</xsl:for-each>
</attribute>
</xsl:when>
</xsl:choose>

<attribute name="Revision">
<xsl:value-of select="substring($revision, 12, string-length($revision)-13)"/>
</attribute>
<attribute name="Date">
<xsl:value-of select="substring($last-modified, 7, string-length($last-modified)-8)"/>
</attribute>
</document-meta-info>
</xsl:template>

<!-- 
*************************************************************************
This template generates the document-formatting-info tag, containing
HTML and PDF settings.
*************************************************************************
-->
<xsl:template name="document-formatting-info">
<document-formatting-info>
<!-- HTML settings. -->
<stylesheet url="styles/style.css"/>
<!-- PDF settings. -->
<!-- cover page -->
<cover-left-margin>40pt</cover-left-margin>
<cover-right-margin>40pt</cover-right-margin>
<cover-top-margin>50pt</cover-top-margin>
<cover-bottom-margin>25pt</cover-bottom-margin>

<!-- table of content -->
<generate-toc-page>yes</generate-toc-page>
<toc-left-margin>40pt</toc-left-margin>
<toc-right-margin>40pt</toc-right-margin>
<toc-top-margin>50pt</toc-top-margin>
<toc-bottom-margin>25pt</toc-bottom-margin>

<left-margin>40pt</left-margin>
<right-margin>40pt</right-margin>
<top-margin>20pt</top-margin>
<bottom-margin>25pt</bottom-margin>
<header-height>80pt</header-height>
<footer-height>50pt</footer-height>
</document-formatting-info>
</xsl:template>

<!-- 
*************************************************************************
This template generates the document header.
*************************************************************************
-->
<xsl:template name="document-header">
<document-header>
<b><xsl:value-of select="$title"/></b>
</document-header>
</xsl:template>

<!-- 
*************************************************************************
This template generates the document footer.
*************************************************************************
-->
<xsl:template name="document-footer">
<document-footer>
Copyright &#169; 2002-2005 
<a href="http://www.openmicroscopy.org/">Open Microscopy Environment</a>.  
All Rights reserved.
</document-footer>
</xsl:template>	

<!-- 
*************************************************************************
This template generates the document body.
*************************************************************************
-->
<xsl:template name="document-body">
<document-body>
<!-- meta info -->
<xsl:call-template name="document-toc"/>	
<xsl:for-each select="//section">
<xsl:call-template name="section">
<xsl:with-param name="href" select="@href"/>
<xsl:with-param name="label" select="@name"/>
</xsl:call-template>
</xsl:for-each>
</document-body>	
</xsl:template>	

<!-- 
*************************************************************************
This template generates the table of content.
*************************************************************************
-->
<xsl:template name="document-toc">
<table-of-content>
<xsl:for-each select="/xdoc/toc/section">
<link>
<xsl:call-template name="section-href">
<xsl:with-param name="text" select="@name"/>
<xsl:with-param name="href" select="@href"/>
</xsl:call-template>
<xsl:if test="count(./section) != 0">
<xsl:for-each select="./section">
<link>
<xsl:call-template name="section-href">
<xsl:with-param name="text" select="@name"/>
<xsl:with-param name="href" select="@href"/>
</xsl:call-template>	
</link>
</xsl:for-each>	
</xsl:if>
</link>
</xsl:for-each>
</table-of-content>	
</xsl:template>

<!-- 
*************************************************************************
This template generates a section element.
*************************************************************************
-->

<xsl:template name="section">
<xsl:param name="href"/>
<xsl:param name="label"/>
<xsl:variable name="href-normalized">
<xsl:call-template name="normalized-href">
<xsl:with-param name="href" select="$href"/>
</xsl:call-template>
</xsl:variable>
<section>
<xsl:attribute name="name"> 
<xsl:value-of select="$href-normalized"/>
</xsl:attribute>
<xsl:attribute name="label"> 
<xsl:value-of select="$label"/>
</xsl:attribute>
<!-- retrieve the content of the file -->
<xsl:variable name="section">
<xsl:call-template name="html-body">
<xsl:with-param name="href" select="$href"/>
</xsl:call-template>
</xsl:variable>
<xsl:choose>
<xsl:when test="$section = ''">
<br/>
<br/>
<br/>
</xsl:when>
<xsl:otherwise>
<xsl:copy-of select="$section"/>
<page-break/>
</xsl:otherwise>
</xsl:choose>
</section>
</xsl:template>

<!-- 
*************************************************************************
This template handles a reference to a section element in the toc.
*************************************************************************
-->

<xsl:template name="section-href">
<xsl:param name="text"/>
<xsl:param name="href"/>
<xsl:variable name="href-normalized">
<xsl:call-template name="normalized-href">
<xsl:with-param name="href" select="$href"/>
</xsl:call-template>
</xsl:variable>
<xsl:attribute name="href"> 
<xsl:value-of select="concat('#', $href-normalized)"/>
</xsl:attribute>
<xsl:value-of select="$text"/>
</xsl:template>

<!-- 
*************************************************************************
This template removes the ".html" or ".htm" extension.
*************************************************************************
-->

<xsl:template name="normalized-href">
<xsl:param name="href"/>
<xsl:choose>
<xsl:when test="contains($href, '.html') and substring($href, string-length($href)-4) = '.html'">
<xsl:value-of select="substring($href, 0, string-length($href)-4)"/>
</xsl:when>
<xsl:when test="contains($href, '.htm') and substring($href, string-length($href)-3) = '.htm'">
<xsl:value-of select="substring($href, 0, string-length($href)-3)"/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="$href"/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<!--
*************************************************************************
This template retrieves the body of an html or htm file.
*************************************************************************
-->

<xsl:template name="html-body">
<xsl:param name="href"/>
<xsl:variable name="body">
<xsl:choose>
<xsl:when test="contains($href, '.html') and substring($href, string-length($href)-4) = '.html'">
<xsl:apply-templates select="document($href)/html/body/node()"/>
</xsl:when>
<xsl:when test="contains($href, '.htm') and substring($href, string-length($href)-3) = '.htm'">
<xsl:apply-templates select="document($href)/html/body/node()"/>
</xsl:when>
<xsl:otherwise>
</xsl:otherwise>
</xsl:choose>
</xsl:variable>
<xsl:copy-of select="$body"/>
</xsl:template>

<!--
*************************************************************************
This template analyzes the attribute href of an a tag.
*************************************************************************
-->

<xsl:template match="a">
<xsl:choose>
<xsl:when test="not(contains(@href, '://')) and contains(@href, '.html') and substring(@href, string-length(@href)-4) = '.html'">
<xsl:call-template name="format-a">
<xsl:with-param name="node" select="."/>
<xsl:with-param name="href" select="@href"/>
</xsl:call-template>
</xsl:when>
<xsl:when test="not(contains(@href, '://')) and contains(@href, '.htm') and substring(@href, string-length(@href)-3) = '.htm'">
<xsl:call-template name="format-a">
<xsl:with-param name="node" select="."/>
<xsl:with-param name="href" select="@href"/>
</xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:copy>
<xsl:copy-of select="@*"/>
<xsl:apply-templates/>
</xsl:copy>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<!--
*************************************************************************
This template transforms an a tag into a link tag.
*************************************************************************
-->

<xsl:template name="format-a">
<xsl:param name="node"/>
<xsl:param name="href"/>	
<link>
<xsl:variable name="href-normalized">
<xsl:call-template name="normalized-href">
<xsl:with-param name="href" select="$href"/>
</xsl:call-template>
</xsl:variable>
<xsl:attribute name="href"> 
<xsl:value-of select="concat('#', $href-normalized)"/>
</xsl:attribute>
<xsl:copy-of select="$node/@*[not(name() = 'href')]"/>
<xsl:copy-of select="$node//node()"/>
</link>
</xsl:template>

<!--
*************************************************************************
This template analyses the content of the src attribute of an img tag.
*************************************************************************
-->
<xsl:template match="img">
<xsl:copy>
<xsl:attribute name="src">
<xsl:choose>
<xsl:when test="substring(@src, string-length(@src)-3) = '.sVg'">
<xsl:value-of select="concat(substring(@src, 0, string-length(@src)-3), '.png')"/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="@src"/>
</xsl:otherwise>
</xsl:choose>
</xsl:attribute>
<xsl:copy-of select="@*[not(name() = 'src')]"/>
</xsl:copy>
</xsl:template>

<!--
*************************************************************************
This template processes all others tags and attibutes.
*************************************************************************
-->

<xsl:template match="@*|node()">
<xsl:copy>
<xsl:apply-templates select="@*"/>
<xsl:apply-templates/>
</xsl:copy>
</xsl:template>

</xsl:stylesheet>
