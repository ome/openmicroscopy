<?xml version="1.0" encoding="UTF-8"?>
<!-- $Id$ -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:fox="http://xml.apache.org/fop/extensions" version="1.0">
	
<!-- B: COMMON VARIABLES -->
<!--
*************************************************************************
Here we declare the document scope variable 
*************************************************************************
-->
<xsl:variable name="dfi" select="/document/document-formatting-info"/>

<xsl:variable name="toc" select="/document/document-body/table-of-content"/>

<xsl:variable name="dmi" select="/document/document-meta-info"/>

<xsl:variable name="nbsp">
<xsl:text> </xsl:text>
</xsl:variable>

<!-- number format for numbering sections -->
<xsl:variable name="snf">
<xsl:choose>
<xsl:when test="$dfi/generate-section-numbers/@number-format">
<xsl:value-of select="$dfi/generate-section-numbers/@number-format"/>
</xsl:when>
<xsl:otherwise>1</xsl:otherwise>
</xsl:choose>
</xsl:variable>

<xsl:variable name="generate-section-numbers">
<xsl:call-template name="onOff">
<xsl:with-param name="content" select="$dfi/generate-section-numbers"/>
</xsl:call-template>
</xsl:variable>

<!-- E: COMMON VARIABLES -->

<xsl:param name="css-file"/>

<xsl:variable name="generate-toc-page">
<xsl:call-template name="onOff">
<xsl:with-param name="content" select="$dfi/generate-toc-page"/>
</xsl:call-template>
</xsl:variable>

<xsl:variable name="supress-coverpage-header">
<xsl:call-template name="onOff">
<xsl:with-param name="content" select="$dfi/supress-coverpage-header"/>
</xsl:call-template>
</xsl:variable>

<xsl:variable name="supress-coverpage-footer">
<xsl:call-template name="onOff">
<xsl:with-param name="content" select="$dfi/supress-coverpage-footer"/>
</xsl:call-template>
</xsl:variable>

<xsl:variable name="supress-toc-header">
<xsl:call-template name="onOff">
<xsl:with-param name="content" select="$dfi/supress-toc-header"/>
</xsl:call-template>
</xsl:variable>

<xsl:variable name="supress-toc-footer">
<xsl:call-template name="onOff">
<xsl:with-param name="content" select="$dfi/supress-toc-footer"/>
</xsl:call-template>
</xsl:variable>

<xsl:variable name="header-height">
<xsl:value-of select="$dfi/header-height"/>
</xsl:variable>

<xsl:variable name="footer-height">
<xsl:value-of select="$dfi/footer-height"/>
</xsl:variable>

<!--
*************************************************************************
Template to set a variable to true or false.
*************************************************************************
-->

<xsl:template name="onOff">
<xsl:param name="content"/>
<xsl:choose>
<xsl:when test="$content  = 'yes'">yes</xsl:when>
<xsl:otherwise>no</xsl:otherwise>
</xsl:choose>
</xsl:template>

<!-- 
*************************************************************************
Template for the document element. This template matches other templates 
from here
*************************************************************************
-->
<xsl:template match="document">
<fo:root>
      
	<!-- layout -->
	<fo:layout-master-set>
      
		<fo:simple-page-master master-name="cover">
			<!-- left margin -->
			<xsl:attribute name="margin-left">
			    <xsl:choose>
				<xsl:when test="$dfi/cover-left-margin">
				<xsl:value-of select="$dfi/cover-left-margin"/>
			    </xsl:when>
			    <xsl:otherwise>40pt</xsl:otherwise>
			    </xsl:choose>
			</xsl:attribute>

			<!-- right margin -->
			<xsl:attribute name="margin-right">
			    <xsl:choose>
				<xsl:when test="$dfi/cover-right-margin">
				<xsl:value-of select="$dfi/cover-right-margin"/>
			    </xsl:when>
			    <xsl:otherwise>40pt</xsl:otherwise>
			    </xsl:choose>
			</xsl:attribute>
			
			<!-- top margin -->
			<xsl:attribute name="margin-top">
			    <xsl:choose>
				<xsl:when test="$dfi/cover-top-margin">
				<xsl:value-of select="$dfi/cover-top-margin"/>
			    </xsl:when>
			    <xsl:otherwise>50pt</xsl:otherwise>
			    </xsl:choose>
			</xsl:attribute>
			
			<!-- bottom margin -->
			<xsl:attribute name="margin-bottom">
			    <xsl:choose>
				<xsl:when test="$dfi/cover-bottom-margin">
				<xsl:value-of select="$dfi/cover-bottom-margin"/>
			    </xsl:when>
			    <xsl:otherwise>25pt</xsl:otherwise>
			    </xsl:choose>
			</xsl:attribute>
			
			<!-- layout for the body part -->
			<fo:region-body margin-top="{substring-before($header-height, 'pt')+1}pt" margin-bottom="50pt"/>



			<xsl:if test="not($supress-coverpage-header = 'yes')">
				<!-- layout for the header part -->
				<fo:region-before margin-top="10pt" extent="{$header-height}"/>
			</xsl:if>

			
			<!-- layout for the footer part -->
			
			<xsl:if test="not($supress-coverpage-header = 'yes')">
				<!-- layout for the footer part -->
				<fo:region-after margin-top="5pt" extent="{$footer-height}"/>
			</xsl:if>

		</fo:simple-page-master>
	 
		<!-- check if toc page is reqd -->
		<xsl:if test="$generate-toc-page = 'yes'">
		<fo:simple-page-master master-name="toc">
			<!-- left margin -->
			<xsl:attribute name="margin-left">
			    <xsl:choose>
				<xsl:when test="$dfi/toc-left-margin">
				<xsl:value-of select="$dfi/toc-left-margin"/>
			    </xsl:when>
			    <xsl:otherwise>40pt</xsl:otherwise>
			    </xsl:choose>
			</xsl:attribute>
			
			<!-- right margin -->
			<xsl:attribute name="margin-right">
			    <xsl:choose>
				<xsl:when test="$dfi/toc-right-margin">
				<xsl:value-of select="$dfi/toc-right-margin"/>
			    </xsl:when>
			    <xsl:otherwise>40pt</xsl:otherwise>
			    </xsl:choose>
			</xsl:attribute>
			
			<!-- top margin -->
			<xsl:attribute name="margin-top">
			    <xsl:choose>
				<xsl:when test="$dfi/toc-top-margin">
				<xsl:value-of select="$dfi/toc-top-margin"/>
			    </xsl:when>
			    <xsl:otherwise>50pt</xsl:otherwise>
			    </xsl:choose>
			</xsl:attribute>
			
			<!-- bottom margin -->
			<xsl:attribute name="margin-bottom">
			    <xsl:choose>
				<xsl:when test="$dfi/toc-bottom-margin">
				<xsl:value-of select="$dfi/toc-bottom-margin"/>
			    </xsl:when>
			    <xsl:otherwise>25pt</xsl:otherwise>
			    </xsl:choose>
			</xsl:attribute>

			<!-- layout for the body part -->
			<fo:region-body margin-top="{substring-before($header-height, 'pt')+1}pt" margin-bottom="50pt"/>

			<xsl:if test="not($supress-toc-header = 'yes')">
				<!-- layout for the header part -->
				<fo:region-before margin-top="10pt" extent="{$header-height}"/>
			</xsl:if>
			
			<xsl:if test="not($supress-toc-header = 'yes')">
				<!-- layout for the footer part -->
				<fo:region-after margin-top="5pt" extent="{$footer-height}"/>
			</xsl:if>

		</fo:simple-page-master>
		</xsl:if>
	
	  
		<fo:simple-page-master master-name="body">
			<!-- left margin -->
			<xsl:attribute name="margin-left">
			<xsl:choose>
			<xsl:when test="$dfi/left-margin">
				<xsl:value-of select="$dfi/left-margin"/>
			</xsl:when>
			<xsl:otherwise>40pt</xsl:otherwise>
			</xsl:choose>
			</xsl:attribute>

			<!-- right margin -->
			<xsl:attribute name="margin-right">
			<xsl:choose>
			<xsl:when test="$dfi/right-margin">
				<xsl:value-of select="$dfi/right-margin"/>
			</xsl:when>
			<xsl:otherwise>40pt</xsl:otherwise>
			</xsl:choose>
			</xsl:attribute>

			<!-- top margin -->
			<xsl:attribute name="margin-top">
			<xsl:choose>
			<xsl:when test="$dfi/top-margin">
				<xsl:value-of select="$dfi/top-margin"/>
			</xsl:when>
			<xsl:otherwise>50pt</xsl:otherwise>
			</xsl:choose>
			</xsl:attribute>

			<!-- bottom margin -->
			<xsl:attribute name="margin-bottom">
			<xsl:choose>
			<xsl:when test="$dfi/bottom-margin">
				<xsl:value-of select="$dfi/bottom-margin"/>
			</xsl:when>
			<xsl:otherwise>25pt</xsl:otherwise>
			</xsl:choose>
			</xsl:attribute>
        
			<!-- layout for the body part -->
			<fo:region-body margin-top="{substring-before($header-height, 'pt')+1}pt" margin-bottom="50pt">
				<xsl:if test="$dfi/body-left-margin">
					<xsl:attribute name="margin-left">
						<xsl:value-of select="$dfi/body-left-margin"/>
					</xsl:attribute>
				</xsl:if>
				<xsl:if test="$dfi/body-right-margin">
					<xsl:attribute name="margin-right">
						<xsl:value-of select="$dfi/body-right-margin"/>
					</xsl:attribute>
				</xsl:if>
			</fo:region-body>		

			<!-- layout for the header part -->
			<fo:region-before margin-top="10pt" extent="{$header-height}"/>

			<!-- layout for the footer part -->
			<fo:region-after margin-top="5pt" extent="{$footer-height}"/>
		</fo:simple-page-master>
	</fo:layout-master-set>
		
	<!-- get the outline -->
	<xsl:apply-templates select="document-body/table-of-content" mode="outline"/>	
	
	<!-- call the cover page -->
	<xsl:call-template name="cover-page"/>
	      
	<!-- call the toc page -->
	<xsl:if test="$generate-toc-page = 'yes'">
		<xsl:call-template name="toc-page"/>
	</xsl:if>

	<fo:page-sequence master-name="body" master-reference="body">

		<!-- apply the header -->
		<xsl:apply-templates select="document-header"/>
	
		<!-- apply the footer -->
		<xsl:apply-templates select="document-footer"/>
	 
		<!-- apply the body -->
		<fo:flow flow-name="xsl-region-body">
  		
			<xsl:apply-templates select="document-body"/>

   		</fo:flow>
     
	</fo:page-sequence>

</fo:root>
</xsl:template>

<!-- 
*************************************************************************
Template for the document body. Defines a fo:block and applies other 
templates.
*************************************************************************
-->
<xsl:template match="document-body">
<fo:block space-before.optimum="12pt">
<xsl:call-template name="get-css-for-element">
<xsl:with-param name="element">body</xsl:with-param>
</xsl:call-template>
<xsl:apply-templates/>	
</fo:block>
</xsl:template>

<!-- 
*************************************************************************
This template is responsible for drawing a horizontal rule.
*************************************************************************
-->

<xsl:template match="hr">
<fo:block>
<fo:leader leader-pattern="rule" leader-length="100%" space-before.optimum="2pt" space-after.optimum="2pt">
<xsl:if test="@color">
<xsl:attribute name="color">
<xsl:value-of select="@color"/>
</xsl:attribute>
</xsl:if>
<xsl:if test="@size">
<xsl:attribute name="rule-thickness">
<xsl:value-of select="@size"/>pt
</xsl:attribute>
</xsl:if>
<xsl:call-template name="apply-css">
<xsl:with-param name="element" select="."/>
</xsl:call-template>
</fo:leader>
</fo:block>
</xsl:template>

<!-- 
*************************************************************************
This template is responsible for creating an anchor/link.
*************************************************************************
-->

<xsl:template match="a">
<fo:inline>
<xsl:call-template name="apply-css">
<xsl:with-param name="element" select="."/>
</xsl:call-template>

<!-- if name attribute is present then possibility is that it can be 
	used as an anchor.
-->
<xsl:if test="@name">
<xsl:attribute name="id">
<xsl:value-of select="@name"/>
</xsl:attribute>
</xsl:if>

<!-- if href is not present then it is just an anchor name and not a link. -->
<xsl:if test="not(@href)">
<xsl:apply-templates/>
</xsl:if>

<!-- if href is present then is is a link. -->
<xsl:if test="@href">
<fo:basic-link>
<xsl:call-template name="apply-css">
<xsl:with-param name="element" select="."/>
</xsl:call-template>
<xsl:choose>
<!-- href starts with # that means it is an internal link. -->
<xsl:when test="starts-with(@href, '#')">
<xsl:attribute name="internal-destination">
<xsl:value-of select="substring-after(@href, '#')"/>
</xsl:attribute>	
</xsl:when>
<!-- else it is an external link. -->
<xsl:otherwise>
<xsl:attribute name="external-destination">
<xsl:value-of select="@href"/>
</xsl:attribute>	
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates/>
</fo:basic-link>
</xsl:if>

</fo:inline>
</xsl:template>


<!-- 
*************************************************************************
This template handles a section element.
*************************************************************************
-->

<xsl:template match="section">
<xsl:variable name="section-name">
<xsl:value-of select="@name"/>
</xsl:variable>
<xsl:variable name="href" select="concat('#', $section-name)"/>
<xsl:variable name="section-number">
<xsl:call-template name="get-section-number">
<xsl:with-param name="href" select="$href"/>
</xsl:call-template>
</xsl:variable>
<xsl:variable name="section-level">
<xsl:call-template name="get-section-level">
<xsl:with-param name="href" select="$href"/>
</xsl:call-template>
</xsl:variable>
<fo:block id="{@name}">
<fo:inline font-weight="bold">
<!-- apply css for the section heading -->
<xsl:call-template name="get-css-for-class">
<xsl:with-param name="class" select="concat('section-heading-', $section-level)"/>
</xsl:call-template>

<!-- show section no if required -->
<xsl:if test="$generate-section-numbers = 'yes'">
<xsl:value-of select="concat($section-number, ' ')"/>
</xsl:if>
<xsl:value-of select="@label"/>
</fo:inline>
</fo:block>

<fo:block space-before.optimum="5pt"/>

<fo:block>
<xsl:call-template name="get-css-for-class">
<xsl:with-param name="class" select="concat('section-body-', $section-level)"/>
</xsl:call-template>
<xsl:for-each select="text()|*">
<xsl:choose>
<xsl:when test="local-name() = ''">
<xsl:value-of select="."/>
</xsl:when>
<xsl:when test="not(local-name() = 'page-break')">
<xsl:apply-templates select="."/>
</xsl:when>
</xsl:choose>
</xsl:for-each>

<!-- make the tree for this section -->
<fo:block start-indent="0.5in">
<xsl:call-template name="sub-section-links">
<xsl:with-param name="link-node" select="/document/document-body/table-of-content//link[substring-after(@href, '#')=$section-name]"/>
<xsl:with-param name="level" select="2"/>
</xsl:call-template>
</fo:block>

</fo:block>

<fo:block space-before.optimum="20pt"/>

<xsl:apply-templates select="page-break"/>
</xsl:template>



<!-- 
*************************************************************************
This template renders an unordered list.
*************************************************************************
-->
<xsl:template match="ul">
	<!-- get the bullet size -->
	<xsl:variable name="bullet-size">
	<xsl:choose>
	<xsl:when test="@bullet-size">
	<xsl:value-of select="@bullet-size"/>
	</xsl:when>
	<xsl:otherwise>12pt</xsl:otherwise>
	</xsl:choose>
	</xsl:variable>

	<!-- list -->
	<fo:list-block>
	<xsl:for-each select="li">
	<fo:list-item>
	<fo:list-item-label end-indent="label-end()">
	<fo:block>
	<fo:inline font-family="Symbol" font-size="{$bullet-size}">•</fo:inline>
	</fo:block>
	</fo:list-item-label>
	
	<fo:list-item-body start-indent="body-start()">
        <fo:block>
		<xsl:apply-templates/>
		</fo:block>
	</fo:list-item-body>
	</fo:list-item>
	</xsl:for-each>
	</fo:list-block>
</xsl:template>

<!-- 
*************************************************************************
This template renders an ordered list.
*************************************************************************
-->
<xsl:template match="ol">
	<!-- list -->
	<fo:list-block>
	<xsl:for-each select="li">
	<fo:list-item>
	<fo:list-item-label end-indent="label-end()">
	<fo:block>
		<fo:inline>
			<xsl:choose>
			<xsl:when test="parent::ol/@type">
				<xsl:number value="position()" format="{parent::ol/@type}"/>.
			</xsl:when>
			<xsl:otherwise>
				<xsl:number value="position()"/>.
			</xsl:otherwise>
			</xsl:choose>
		</fo:inline>
	</fo:block>
	</fo:list-item-label>
	
	<fo:list-item-body start-indent="body-start()">
		<fo:block>
        <xsl:apply-templates/>
       </fo:block>
	</fo:list-item-body>
	
	</fo:list-item>
	</xsl:for-each>
	</fo:list-block>
</xsl:template>


<!-- 
*************************************************************************
This template renders a definition list.
*************************************************************************
-->
<xsl:template match="dl">
<fo:block space-after.optimum="15pt">
<xsl:apply-templates select="dt|dd"/>
</fo:block>
</xsl:template>

<!-- 
*************************************************************************
This template renders a definition term.
*************************************************************************
-->
<xsl:template match="dt">
<fo:block>
<xsl:apply-templates/>
</fo:block>
</xsl:template>


<!-- 
*************************************************************************
This template renders a definition description.
*************************************************************************
-->

<xsl:template match="dd">
<fo:block space-after.optimum="5pt" start-indent="0.5in">
<xsl:apply-templates/>
</fo:block>
</xsl:template>


<!-- 
*************************************************************************
This template renders a line break.
*************************************************************************
-->
<xsl:template match="br">
<fo:block space-before.optimum="5pt"/>
<xsl:apply-templates/>
</xsl:template>



<!-- 
*************************************************************************
This template renders an image.
*************************************************************************
-->
<xsl:template match="img">
<fo:external-graphic src="{@src}">
<xsl:if test="@width">
<xsl:attribute name="width">
<xsl:value-of select="concat(@width, 'px')"/>
</xsl:attribute>
</xsl:if>
<xsl:if test="@height">
<xsl:attribute name="height">
<xsl:value-of select="concat(@height, 'px')"/>
</xsl:attribute>
</xsl:if>
<xsl:if test="@hspace">
<xsl:attribute name="padding-left">
<xsl:value-of select="@vspace"/>px</xsl:attribute>
<xsl:attribute name="padding-right">
<xsl:value-of select="@vspace"/>px</xsl:attribute>
</xsl:if>
<xsl:if test="@vspace">
<xsl:attribute name="padding-top">
<xsl:value-of select="@vspace"/>px</xsl:attribute>
<xsl:attribute name="padding-bottom">
<xsl:value-of select="@vspace"/>px</xsl:attribute>
</xsl:if>
<xsl:call-template name="apply-css">
	<xsl:with-param name="element" select="."/>
</xsl:call-template>
</fo:external-graphic>
</xsl:template>



<!-- 
*************************************************************************
This template sets the font weight as bold.
*************************************************************************
-->
<xsl:template match="b|strong">
<fo:inline font-weight="bold">
<xsl:call-template name="apply-css">
<xsl:with-param name="element" select="."/>
</xsl:call-template>
<xsl:apply-templates/>
</fo:inline>
</xsl:template>

<!-- 
*************************************************************************
This template sets the font style as italic.
*************************************************************************
-->
<xsl:template match="i|em">
<fo:inline font-style="italic">
<xsl:call-template name="apply-css">
<xsl:with-param name="element" select="."/>
</xsl:call-template>
<xsl:apply-templates/>
</fo:inline>
</xsl:template>


<!-- 
*************************************************************************
This template sets the font style as underlined.
*************************************************************************
-->
<xsl:template match="u">
<fo:inline text-decoration="underline">
<xsl:call-template name="apply-css">
<xsl:with-param name="element" select="."/>
</xsl:call-template>
<xsl:apply-templates/>
</fo:inline>
</xsl:template>


<!-- 
*************************************************************************
This template sets the font style as striked-through.
*************************************************************************
-->
<xsl:template match="s">
<fo:inline text-decoration="line-through">
<xsl:call-template name="apply-css">
<xsl:with-param name="element" select="."/>
</xsl:call-template>
<xsl:apply-templates/>
</fo:inline>
</xsl:template>


<!-- 
*************************************************************************
This template handles the p and div tags.
*************************************************************************
-->
<xsl:template match="p|div">
<fo:block space-before.optimum="10pt" space-after.optimum="10pt">
<xsl:call-template name="apply-css">
<xsl:with-param name="element" select="."/>
</xsl:call-template>
<xsl:call-template name="get-alignment">
<xsl:with-param name="align" select="@align"/>
</xsl:call-template>
<xsl:apply-templates/>
</fo:block>
</xsl:template>


<!-- 
*************************************************************************
This template handles the center tags.
*************************************************************************
-->
<xsl:template match="center">
<fo:block text-align="center">
<xsl:apply-templates/>
</fo:block>
</xsl:template>


<!-- 
*************************************************************************
This template handles the table-of-content tag to produce the outline.
*************************************************************************
-->
<xsl:template match="table-of-content" mode="outline">
<xsl:for-each select="link">
<xsl:call-template name="outline-link">
<xsl:with-param name="link-node" select="."/>
<xsl:with-param name="level" select="1"/>
<xsl:with-param name="section-no" select="position()"/>
</xsl:call-template>
</xsl:for-each>
</xsl:template>

<!-- 
*************************************************************************
This template handles each link inside the toc. Depending on whether the 
generate-section-numbers is set to yes/no it shows/hides the section
numbers.
*************************************************************************
-->
<xsl:template name="outline-link">
<xsl:param name="link-node"/>
<xsl:param name="level"/>
<xsl:param name="section-no"/>

<!-- construct the bookmark label -->
<xsl:variable name="bookmark-label">
<xsl:choose>
<xsl:when test="$generate-section-numbers= 'yes'">
<xsl:value-of select="concat($section-no, '.', $link-node/text())"/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="$link-node/text()"/>
</xsl:otherwise>
</xsl:choose>
</xsl:variable>

<fox:outline internal-destination="{substring-after($link-node/@href, '#')}">
<xsl:variable name="safe-label">
<xsl:call-template name="make-safe">
<xsl:with-param name="str" select="$bookmark-label"/>
</xsl:call-template>
</xsl:variable>
<fox:label>
<xsl:value-of select="$safe-label"/>
</fox:label>
<xsl:for-each select="$link-node/link">
<xsl:call-template name="outline-link">
<xsl:with-param name="link-node" select="."/>
<xsl:with-param name="level" select="$level + 1"/>
<xsl:with-param name="section-no" select="concat($section-no, '.', position())"/>
</xsl:call-template>
</xsl:for-each>
</fox:outline>
</xsl:template>

<!-- 
*************************************************************************
This template handles the h1 heading tag.
*************************************************************************
-->
<xsl:template match="h1">
<xsl:call-template name="format-h">
<xsl:with-param name="font-size">28pt</xsl:with-param>
</xsl:call-template>
</xsl:template>

<!-- 
*************************************************************************
This template handles the h2 heading tag.
*************************************************************************
-->
<xsl:template match="h2">
<xsl:call-template name="format-h">
<xsl:with-param name="font-size">24pt</xsl:with-param>
</xsl:call-template>
</xsl:template>

<!-- 
*************************************************************************
This template handles the h3 heading tag
*************************************************************************
-->
<xsl:template match="h3">
<xsl:call-template name="format-h">
<xsl:with-param name="font-size">20pt</xsl:with-param>
</xsl:call-template>
</xsl:template>

<!-- 
*************************************************************************
This template handles the h4 heading tag
*************************************************************************
-->
<xsl:template match="h4">
<xsl:call-template name="format-h">
<xsl:with-param name="font-size">16pt</xsl:with-param>
</xsl:call-template>
</xsl:template>

<!-- 
*************************************************************************
This template handles the h5 heading tag
*************************************************************************
-->

<xsl:template match="h5">
<xsl:call-template name="format-h">
<xsl:with-param name="font-size">12pt</xsl:with-param>
</xsl:call-template>
</xsl:template>

<!-- 
*************************************************************************
This template handles the h6 heading tag
*************************************************************************
-->
<xsl:template match="h6">
<xsl:call-template name="format-h">
<xsl:with-param name="font-size">8pt</xsl:with-param>
</xsl:call-template>
</xsl:template>

<!-- 
*************************************************************************
This template handles the heading tag
*************************************************************************
-->
<xsl:template name="format-h">
<xsl:param name="font-size"/>
<fo:block space-before.optimum="10pt" space-after.optimum="10pt" font-size="$font-size">
<xsl:call-template name="apply-css">
<xsl:with-param name="element" select="."/>
</xsl:call-template>
<xsl:call-template name="get-alignment">
<xsl:with-param name="align" select="@align"/>
</xsl:call-template>
<xsl:apply-templates/>
</fo:block>
</xsl:template>

<!-- 
*************************************************************************
Dummy template to avoid this template being applied in a normal way.
*************************************************************************
-->
<xsl:template match="link">
</xsl:template>

<!-- 
*************************************************************************
This template handles the address tag.
*************************************************************************
-->
<xsl:template match="address">
<fo:block font-style="italic">
<xsl:call-template name="apply-css">
<xsl:with-param name="element" select="."/>
</xsl:call-template>
<xsl:apply-templates/>
</fo:block>
</xsl:template>

<!-- 
*************************************************************************
This template renders the page number.
*************************************************************************
-->
<xsl:template match="page-number">
<fo:page-number/>
</xsl:template>

<!-- 
*************************************************************************
This template renders the document header.
*************************************************************************
-->

<xsl:template match="document-header">
<fo:static-content flow-name="xsl-region-before">
<fo:block>
<xsl:call-template name="get-css-for-class">
<xsl:with-param name="class">document-header</xsl:with-param>
</xsl:call-template>
<xsl:apply-templates/>
</fo:block>
</fo:static-content>
</xsl:template>

<!-- 
*************************************************************************
This template renders the document footer.
*************************************************************************
-->
<xsl:template match="document-footer">
<fo:static-content flow-name="xsl-region-after">
<fo:block>
<xsl:call-template name="get-css-for-class">
<xsl:with-param name="class">document-footer</xsl:with-param>
</xsl:call-template>
<xsl:apply-templates/>
</fo:block>
</fo:static-content>
</xsl:template>


<!-- 
*************************************************************************
This template renders the cover page.
*************************************************************************
-->
<xsl:template name="cover-page">
<xsl:if test="/document/document-meta-info">
<fo:page-sequence master-reference="cover">
	<xsl:if test="not($supress-coverpage-header = 'yes')">
		<!-- apply the header -->
		<xsl:apply-templates select="/document/document-header"/>
	</xsl:if>

	<xsl:if test="not($supress-coverpage-footer = 'yes')">
		<!-- apply the footer -->
		<xsl:apply-templates select="/document/document-footer"/>
	</xsl:if>

	<fo:flow flow-name="xsl-region-body">

	<!-- title -->
	<fo:block space-before.optimum="16pt" font-weight="bold">
	<xsl:call-template name="get-css-for-class">
		<xsl:with-param name="class">document-title</xsl:with-param>
	</xsl:call-template>

	<xsl:value-of select="/document/document-meta-info/title"/>
	</fo:block>
		
	<!-- all attributes -->
	<fo:block space-before.optimum="16pt">
	<xsl:call-template name="get-css-for-class">
		<xsl:with-param name="class">document-attributes</xsl:with-param>
	</xsl:call-template>

	<xsl:for-each select="/document/document-meta-info/attribute">
		<xsl:value-of select="@name"/>: <xsl:apply-templates select="."/>
		<fo:block/>
	</xsl:for-each>
	</fo:block>
	</fo:flow>
	</fo:page-sequence>
	</xsl:if>
</xsl:template>


<!-- 
*************************************************************************
This template renders the toc page.
*************************************************************************
-->
<xsl:template name="toc-page">
	<xsl:if test="/document/document-body/table-of-content">
	<fo:page-sequence master-reference="toc">
	<xsl:if test="not($supress-toc-header = 'yes')">
		<!-- apply the header -->
		<xsl:apply-templates select="/document/document-header"/>
	</xsl:if>

	<xsl:if test="not($supress-toc-footer = 'yes')">
		<!-- apply the footer -->
		<xsl:apply-templates select="/document/document-footer"/>
	</xsl:if>

	<fo:flow flow-name="xsl-region-body">
	<fo:block text-align="start" font-weight="bold" font-size="16pt">
	Table Of Contents
	</fo:block>
	<fo:block>
<fo:leader leader-pattern="rule" leader-length="100%" space-before.optimum="2pt" space-after.optimum="2pt" rule-thickness="2pt"/>
	</fo:block>

	<xsl:for-each select="/document/document-body/table-of-content/link">
		<xsl:call-template name="toc-link">
			<xsl:with-param name="link-node" select="."/>
		</xsl:call-template>
	</xsl:for-each>
	</fo:flow>
	</fo:page-sequence>
	</xsl:if>
</xsl:template>

<!-- 
*************************************************************************
This template renders the toc link.
*************************************************************************
-->
<xsl:template name="toc-link">
<xsl:param name="link-node"/>

<xsl:variable name="section-level">
<xsl:call-template name="get-section-level">
<xsl:with-param name="href" select="$link-node/@href"/>
</xsl:call-template>
</xsl:variable>

<xsl:variable name="section-number">
<xsl:call-template name="get-section-number">
<xsl:with-param name="href" select="$link-node/@href"/>
</xsl:call-template>
</xsl:variable>

<fo:block space-before.optimum="5pt" text-align-last="justify" last-line-end-indent="-24pt">
<xsl:attribute name="margin-left">
<xsl:value-of select="$section-level - 1"/>em
</xsl:attribute>
<fo:basic-link>
<xsl:call-template name="get-css-for-element">
	<xsl:with-param name="element">a</xsl:with-param>
</xsl:call-template>

<xsl:attribute name="internal-destination">
<xsl:value-of select="substring-after($link-node/@href, '#')"/>
</xsl:attribute>	
<xsl:if test="$generate-section-numbers = 'yes'">
 	<xsl:value-of select="concat($section-number, ' ')"/>
</xsl:if>
<fo:inline keep-with-next.within-line="always">
<xsl:call-template name="make-safe">
	<xsl:with-param name="str" select="$link-node/text()"/>
</xsl:call-template>
</fo:inline>
<fo:inline keep-together.within-line="always">
<!--
<fo:leader leader-alignment="reference-area" leader-pattern-width="8pt" leader-pattern="dots"/>
-->
<fo:leader leader-pattern="dots" leader-pattern-width="3pt" leader-alignment="reference-area" keep-with-next.within-line="always"/>
<fo:page-number-citation ref-id="{substring-after($link-node/@href, '#')}"/>
</fo:inline>
</fo:basic-link>
</fo:block>
<xsl:for-each select="$link-node/link">
	<xsl:call-template name="toc-link">
		<xsl:with-param name="link-node" select="."/>
	</xsl:call-template>
</xsl:for-each>
</xsl:template>


<!-- 
*************************************************************************
This template forces a page-break.
*************************************************************************
-->
<xsl:template match="page-break">
	<fo:block break-before="page" keep-with-previous="auto"/>	
</xsl:template>


<!-- 
*************************************************************************
This template renders a table.
*************************************************************************
-->
<xsl:template match="table">
<fo:table table-layout="fixed">	
	<!-- bgcolor -->
	<xsl:if test="@bgcolor">
	<xsl:attribute name="background-color">
<xsl:value-of select="@bgcolor"/>
</xsl:attribute>
	</xsl:if>
	
	<xsl:if test="@border">
	<xsl:attribute name="border-width">
<xsl:value-of select="@border"/>pt</xsl:attribute>
	<xsl:attribute name="border-style">solid</xsl:attribute>
	</xsl:if>
	
	<xsl:call-template name="apply-css">
		<xsl:with-param name="element" select="."/>
	</xsl:call-template>

	<!-- table columns -->
	<xsl:for-each select="tr[1]/td">
	<xsl:variable name="col-width">
	<xsl:choose>
	<xsl:when test="@width">
	<xsl:choose>
	<xsl:when test="contains(@width, '%')">
<xsl:value-of select="170 * substring-before(@width, '%') div 100"/>mm</xsl:when>
	<xsl:otherwise>
<xsl:value-of select="concat(@width, 'pt')"/>
</xsl:otherwise>
	</xsl:choose>
	</xsl:when>
	<xsl:otherwise>
<xsl:value-of select="170 div count(../td)"/>mm</xsl:otherwise>
	</xsl:choose>
	</xsl:variable>
	<fo:table-column column-width="{$col-width}"/>
	</xsl:for-each>

	<fo:table-body>	
		<xsl:apply-templates select="tr"/>
	</fo:table-body>
</fo:table>	
</xsl:template>

<!-- 
*************************************************************************
This template renders a tr - table row.
*************************************************************************
-->
<xsl:template match="tr">
<fo:table-row>
<!-- bgcolor -->
<xsl:if test="@bgcolor">
<xsl:attribute name="background-color">
<xsl:value-of select="@bgcolor"/>
</xsl:attribute>
</xsl:if>
<xsl:call-template name="apply-css">
<xsl:with-param name="element" select="."/>
</xsl:call-template>	
<xsl:apply-templates/>
</fo:table-row>
</xsl:template>


<!-- 
*************************************************************************
This template renders a td - table cell.
*************************************************************************
-->
<xsl:template match="td">
<fo:table-cell>
<!-- bgcolor -->
<xsl:if test="@bgcolor">
<xsl:attribute name="background-color">
<xsl:value-of select="@bgcolor"/>
</xsl:attribute>
</xsl:if>
<xsl:call-template name="apply-css">
<xsl:with-param name="element" select="."/>
</xsl:call-template>
	
<!-- colspan -->
<xsl:if test="@colspan">
<xsl:attribute name="number-columns-spanned">
<xsl:value-of select="@colspan"/>
</xsl:attribute>
</xsl:if>

<!-- rowspan -->
<xsl:if test="@rowspan">
<xsl:attribute name="number-rows-spanned">
<xsl:value-of select="@rowspan"/>
</xsl:attribute>
</xsl:if>

<xsl:if test="ancestor::table/@border">
<xsl:attribute name="border-width">
<xsl:value-of select="ancestor::table/@border"/>pt</xsl:attribute>
<xsl:attribute name="border-style">solid</xsl:attribute>
</xsl:if>

<xsl:if test="ancestor::table/@cellpadding">
<xsl:attribute name="padding">
<xsl:value-of select="ancestor::table/@cellpadding"/>pt</xsl:attribute>
</xsl:if>
<xsl:if test="@valign">
<xsl:attribute name="display-align">
<xsl:choose>
<xsl:when test="@valign='bottom'">bottom</xsl:when>
<xsl:when test="@valign='middle'">center</xsl:when>
<xsl:otherwise>center</xsl:otherwise>
</xsl:choose>
</xsl:attribute>
</xsl:if>	
<fo:block> 
<xsl:attribute name="text-align">
<xsl:choose>
<xsl:when test="@align='right'">end</xsl:when>
<xsl:when test="@align='center'">center</xsl:when>
<xsl:when test="@align='justify'">justify</xsl:when>
<xsl:otherwise>start</xsl:otherwise>
</xsl:choose>
</xsl:attribute>
<xsl:apply-templates/>
</fo:block> 
</fo:table-cell>
</xsl:template>

<!-- 
*************************************************************************
This template renders preformatted text.
*************************************************************************
-->
<xsl:template match="pre">
<fo:block space-before.optimum="10pt" space-after.optimum="10pt" wrap-option="no-wrap" white-space-collapse="false">
<xsl:call-template name="apply-css">
<xsl:with-param name="element" select="."/>
</xsl:call-template>
<xsl:apply-templates/>
</fo:block>
</xsl:template>

<!-- 
*************************************************************************
This template renders a blockquote(indented) section.
*************************************************************************
-->
<xsl:template match="blockquote">
<fo:block start-indent="1.0in">
<xsl:call-template name="apply-css">
<xsl:with-param name="element" select="."/>
</xsl:call-template>
<xsl:apply-templates/>
</fo:block>
</xsl:template>

<!-- 
*************************************************************************
This template renders superscript text.
*************************************************************************
-->
<xsl:template match="sup">
<fo:inline vertical-align="super">
<xsl:call-template name="apply-css">
<xsl:with-param name="element" select="."/>
</xsl:call-template>
<xsl:apply-templates/>
</fo:inline>
</xsl:template>

<!-- 
*************************************************************************
This template renders subscript text.
*************************************************************************
-->
<xsl:template match="sub">
<fo:inline vertical-align="sub">
<xsl:call-template name="apply-css">
<xsl:with-param name="element" select="."/>
</xsl:call-template>
<xsl:apply-templates/>
</fo:inline>
</xsl:template>

<!-- 
*************************************************************************
This template handles a link to a section.
*************************************************************************
-->
<xsl:template match="section-link">
<xsl:variable name="href">
<xsl:choose>
<xsl:when test="contains(@href,'#')">
<xsl:value-of select="substring-after(@href, '#')"/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="@href"/>
</xsl:otherwise>
</xsl:choose>
</xsl:variable>
<fo:inline>
<fo:basic-link>
<xsl:call-template name="get-css-for-element">
<xsl:with-param name="element">a</xsl:with-param>
</xsl:call-template>

<xsl:attribute name="internal-destination">
<xsl:value-of select="$href"/>
</xsl:attribute>	
<xsl:apply-templates/>
</fo:basic-link>
</fo:inline>
</xsl:template>

<!-- 
*************************************************************************
This template sets a new font for the content enclosed within it.
*************************************************************************
-->
<xsl:template match="font">
<fo:inline>
<xsl:if test="@color">
<xsl:attribute name="color">
<xsl:value-of select="@color"/>
</xsl:attribute>
</xsl:if>
<xsl:if test="@face">
<xsl:attribute name="font-family">
<xsl:value-of select="@face"/>
</xsl:attribute>
</xsl:if>
<xsl:apply-templates/>
</fo:inline>
</xsl:template>

<!-- 
*************************************************************************
This template handles the code tag.
*************************************************************************
-->

<xsl:template match="code">
<fo:inline font-family="monospace">
<xsl:call-template name="apply-css">
<xsl:with-param name="element" select="."/>
</xsl:call-template>
<xsl:apply-templates/>
</fo:inline>
</xsl:template>

<!-- 
*************************************************************************
This template renders the sub section links
*************************************************************************
-->
<xsl:template name="sub-section-links">
<xsl:param name="link-node"/>
<xsl:param name="level"/>
<xsl:if test="$link-node/link">
<fo:block space-before.optimum="10pt"/>
<xsl:choose>
<xsl:when test="$generate-section-numbers = 'yes'">
<xsl:for-each select="$link-node/link">
<xsl:call-template name="sub-section-link-with-section-nos">
<xsl:with-param name="link" select="."/>
<xsl:with-param name="level" select="$level"/>
</xsl:call-template>
</xsl:for-each>
</xsl:when>
<xsl:otherwise>
<fo:list-block>
<xsl:for-each select="$link-node/link">
<xsl:call-template name="sub-section-link-without-section-nos">
<xsl:with-param name="link" select="."/>
<xsl:with-param name="level" select="$level"/>
</xsl:call-template>
</xsl:for-each>
</fo:list-block>
</xsl:otherwise>
</xsl:choose>
</xsl:if>
</xsl:template>

<!-- sub-section-link-with-section-nos -->
<xsl:template name="sub-section-link-with-section-nos">
<xsl:param name="link"/>
<xsl:param name="level"/>
<fo:block>
<fo:basic-link>
<xsl:call-template name="get-css-for-element">
<xsl:with-param name="element">a</xsl:with-param>
</xsl:call-template>

<xsl:attribute name="internal-destination">
<xsl:value-of select="substring-after($link/@href, '#')"/>
</xsl:attribute>

<xsl:variable name="section-number">
<xsl:call-template name="get-section-number">
<xsl:with-param name="href" select="@href"/>
</xsl:call-template>
</xsl:variable>

<xsl:value-of select="$section-number"/> <xsl:value-of select="text()"/>
</fo:basic-link>
</fo:block>

<xsl:if test="$link/link">
	<xsl:for-each select="$link/link">
	<xsl:call-template name="sub-section-link-with-section-nos">
		<xsl:with-param name="link" select="."/>
		<xsl:with-param name="level" select="$level + 1"/>
	</xsl:call-template>
	</xsl:for-each>
</xsl:if>
</xsl:template>

<!-- sub-section-link-without-section-nos -->
<xsl:template name="sub-section-link-without-section-nos">
<xsl:param name="link"/>
<xsl:param name="level"/>
<fo:list-item>
<fo:list-item-label end-indent="label-end()">
<fo:block>
<fo:inline font-family="Symbol" font-size="12pt">•</fo:inline>
</fo:block>
</fo:list-item-label>
<fo:list-item-body start-indent="body-start()">
<fo:block>
<fo:basic-link>
<xsl:call-template name="get-css-for-element">
	<xsl:with-param name="element">a</xsl:with-param>
</xsl:call-template>
<xsl:attribute name="internal-destination">
<xsl:value-of select="substring-after($link/@href, '#')"/>
</xsl:attribute>
<xsl:value-of select="$link/text()"/>
</fo:basic-link>
</fo:block>
<xsl:if test="$link/link">
<fo:list-block>
	<xsl:for-each select="$link/link">
		<xsl:call-template name="sub-section-link-without-section-nos">
			<xsl:with-param name="link" select="."/>
			<xsl:with-param name="level" select="$level + 1"/>
		</xsl:call-template>
	</xsl:for-each>
</fo:list-block>
</xsl:if>
</fo:list-item-body>
</fo:list-item>
</xsl:template>


<!-- 
*************************************************************************
This template applies the css for the given element or class.
*************************************************************************
-->
<xsl:template name="apply-css">
<xsl:param name="element"/>
<xsl:variable name="name" select="local-name($element)"/>
<!-- if style attrib is present, apply it -->
<xsl:if test="$element/@style">
<xsl:call-template name="parse-css-attrib">
<xsl:with-param name="style" select="normalize-space(element/@style)"/>
</xsl:call-template>
</xsl:if>

<!-- if css rule exists for this element apply it -->
<xsl:call-template name="get-css-for-element">
<xsl:with-param name="element" select="$name"/>
</xsl:call-template>

<!-- if class attribute exists this element apply it -->
<xsl:call-template name="get-css-for-class">
<xsl:with-param name="element" select="$name"/>
<xsl:with-param name="class" select="$element/@class"/>
</xsl:call-template>
</xsl:template>

<!-- 
*************************************************************************
This template applies the css rules for the given class.
*************************************************************************
-->
<xsl:template name="get-css-for-class">
<xsl:param name="element"/>
<xsl:param name="class"/>
<xsl:if test="$class and $css-file">
<xsl:for-each select="document($css-file)/css/selector[@name = concat($element, '.', $class) or @name = concat('*.', $class)]/attrib">
<xsl:attribute name="{@name}">
<xsl:value-of select="@value"/>
</xsl:attribute>
</xsl:for-each>
</xsl:if>
</xsl:template>

<!-- 
*************************************************************************
This template applies the css rules for the given element.
*************************************************************************
-->
<xsl:template name="get-css-for-element">
<xsl:param name="element"/>
<xsl:if test="$css-file">
<xsl:for-each select="document($css-file)/css/selector[@name = $element]/attrib">
<xsl:attribute name="{@name}">
<xsl:value-of select="@value"/>
</xsl:attribute>
</xsl:for-each>
</xsl:if>
</xsl:template>

<!-- 
*************************************************************************
This template parses the style attribute and gets the individual 
components.
*************************************************************************
-->
<xsl:template name="parse-css-attrib">
<xsl:param name="style"/>
<xsl:if test="$style">
<xsl:variable name="attrib" select="$style"/>
<!--
<xsl:variable name="attrib" select="translate($style, ' ', '')" />
-->
<xsl:choose>
<xsl:when test="contains($attrib, ';')">
<xsl:variable name="item" select="substring-before($attrib, ';')"/>
<xsl:variable name="remaining" select="substring-after($attrib, ';')"/>
<xsl:call-template name="parse-css-item">
<xsl:with-param name="item" select="$item"/>
</xsl:call-template>
<xsl:if test="not($remaining = '')">
<xsl:call-template name="parse-css-attrib">
<xsl:with-param name="style" select="$remaining"/>
</xsl:call-template>
</xsl:if>
</xsl:when>
<xsl:otherwise>
<xsl:call-template name="parse-css-item">
<xsl:with-param name="item" select="$attrib"/>
</xsl:call-template>
</xsl:otherwise>
</xsl:choose>
</xsl:if>
</xsl:template>

<!-- 
*************************************************************************
This template process a single css attribute and generates an xsl
attribute based on that.
*************************************************************************
-->
<xsl:template name="parse-css-item">
<xsl:param name="item"/>
<xsl:variable name="name">
<xsl:call-template name="to-lower-case">
<xsl:with-param name="str" select="substring-before($item, ':')"/>
</xsl:call-template>
</xsl:variable>
<xsl:variable name="value">
<xsl:call-template name="to-lower-case">
<xsl:with-param name="str" select="substring-after($item, ':')"/>
</xsl:call-template>
</xsl:variable>
<xsl:attribute name="{$name}">
<xsl:value-of select="$value"/>
</xsl:attribute>
</xsl:template>

<!-- 
*************************************************************************
This template converts the given string value to lowercase.
*************************************************************************
-->
<xsl:template name="to-lower-case">
<xsl:param name="str"/>
<xsl:value-of select="translate($str, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
</xsl:template>

<!--
*************************************************************************
This template gets the align attribute.
*************************************************************************
-->
<xsl:template name="get-alignment">
<xsl:param name="align"/>
<xsl:if test="$align">
<xsl:variable name="alignment" select="translate($align, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/> 
<xsl:attribute name="text-align">
<xsl:choose>
<xsl:when test="$alignment = 'right'">end</xsl:when>
<xsl:when test="$alignment = 'center'">center</xsl:when>
<xsl:otherwise>start</xsl:otherwise>
</xsl:choose>
</xsl:attribute>
</xsl:if>
</xsl:template>


<!-- B: SECTION FUNCTIONS -->
<!--
*************************************************************************
Template to get the section no.
*************************************************************************
-->
<xsl:template name="get-section-number">
<xsl:param name="format">
<xsl:value-of select="$snf"/>
</xsl:param>
<xsl:param name="href"/>
<xsl:for-each select="/document/document-body/table-of-content//link[@href = $href]">
<xsl:number level="multiple" format="{$format}"/>.</xsl:for-each>
</xsl:template>

<!--
*************************************************************************
Template to get the section level.
*************************************************************************
-->
<xsl:template name="get-section-level">
<xsl:param name="href"/>
<xsl:for-each select="/document/document-body/table-of-content//link[@href = $href]">
<xsl:variable name="section-number">
<xsl:call-template name="get-section-number">
<xsl:with-param name="href" select="$href"/>
<xsl:with-param name="format">1</xsl:with-param>
</xsl:call-template>
</xsl:variable>
<xsl:variable name="level" select="string-length(translate($section-number, '0123456789',''))"/>
<xsl:value-of select="$level"/>
</xsl:for-each>
</xsl:template>

<!-- B: STRING FUNCTIONS -->
<!-- common space characters -->
<xsl:variable name="lf">
<xsl:text>
</xsl:text>
</xsl:variable>
<xsl:variable name="tab">
<xsl:text>	</xsl:text>
</xsl:variable>
<xsl:variable name="lftab">
<xsl:copy-of select="$lf"/>
<xsl:copy-of select="$tab"/>
</xsl:variable>
<!-- template to replace characters like tab, newline, etc -->
<xsl:template name="make-safe">
<xsl:param name="str"/>
<xsl:variable name="replacement" select="concat('','')"/>
<xsl:variable name="tab-safe">
<xsl:call-template name="replace-text">
<xsl:with-param name="str" select="$str"/>
<xsl:with-param name="replace" select="$tab"/>
<xsl:with-param name="replacement" select="$replacement"/>
</xsl:call-template>
</xsl:variable>
<xsl:variable name="lf-safe">
<xsl:call-template name="replace-text">
<xsl:with-param name="str" select="$tab-safe"/>
<xsl:with-param name="replace" select="$lf"/>
<xsl:with-param name="replacement" select="$replacement"/>
</xsl:call-template>
</xsl:variable>
<xsl:variable name="safe">
<xsl:call-template name="replace-text">
<xsl:with-param name="str" select="$lf-safe"/>
<xsl:with-param name="replace" select="$lftab"/>
<xsl:with-param name="replacement" select="$replacement"/>
</xsl:call-template>
</xsl:variable>
<xsl:value-of select="$safe"/>
</xsl:template>
<!-- template to replace text -->
<xsl:template name="replace-text">
<xsl:param name="str"/>
<xsl:param name="replace"/>
<xsl:param name="replacement"/>
<xsl:choose>
<xsl:when test="contains($str, $replace)">
<xsl:value-of select="concat(substring-before($str, $replace), $replacement)"/>
<xsl:call-template name="replace-text">
<xsl:with-param name="str" select="substring-after($str, $replace)"/>
<xsl:with-param name="replace" select="$replace"/>
<xsl:with-param name="replacement" select="$replacement"/>
</xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="$str"/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>
<!-- E: STRING FUNCTIONS -->
</xsl:stylesheet>
