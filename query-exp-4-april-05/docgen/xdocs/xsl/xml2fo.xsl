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

<xsl:variable name="pageheight" select="/document/document-meta-info/page-height"/>
<xsl:variable name="pagewidth" select="/document/document-meta-info/page-width"/>
<xsl:variable name="header" select="/document/document-header"/>
<xsl:variable name="footer" select="/document/document-footer"/>
	
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
			<!-- cover -->
			<fo:simple-page-master master-name="cover" margin-bottom="25mm" margin-left="25mm" 
			margin-right="25mm" margin-top="25mm">
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
			
			<!-- check if toc page is requested -->
			<xsl:if test="$generate-toc-page = 'yes'">
				<fo:simple-page-master master-name="toc" margin-bottom="25mm" margin-left="25mm" 
				margin-right="25mm" margin-top="25mm">
				<!-- layout for the body part -->
					<fo:region-body margin-top="{substring-before($header-height, 'pt')+1}pt" margin-bottom="50pt"/>
						<xsl:if test="not($supress-toc-header = 'yes')">
						<!-- layout for the header part -->
							<fo:region-before margin-top="10pt" extent="{$header-height}"/>
						</xsl:if>
						<!-- layout for the footer part -->
						<xsl:if test="not($supress-toc-header = 'yes')">
							<fo:region-after margin-top="5pt" extent="{$footer-height}"/>
						</xsl:if>
				</fo:simple-page-master>
			</xsl:if>
			
			<!-- main pages -->
			<fo:simple-page-master master-name="body" margin-bottom="25pt" margin-left="40pt" 
				margin-right="40pt" margin-top="20pt">
				<!-- layout for the body part -->
				<fo:region-body margin-top="{substring-before($header-height, 'pt')+1}pt" margin-bottom="50pt">
					<xsl:if test="$dfi/body-left-margin">
						<xsl:attribute name="margin-left"><xsl:value-of select="$dfi/body-left-margin"/></xsl:attribute>
					</xsl:if>
					<xsl:if test="$dfi/body-right-margin">
						<xsl:attribute name="margin-right"><xsl:value-of select="$dfi/body-right-margin"/></xsl:attribute>
					</xsl:if>
				</fo:region-body>
				<!-- layout for the header part -->
				<fo:region-before margin-top="10pt" extent="{$header-height}" />
				<!-- layout for the footer part -->
				<fo:region-after margin-top="5pt" extent="{$footer-height}" />
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
		<xsl:call-template name="body"/>		
	</fo:root>
</xsl:template>

<!-- 
*************************************************************************
This template renders the document header.
*************************************************************************
-->
<xsl:template name="document-header">
	<fo:static-content flow-name="xsl-region-before">
		<fo:block font-size="10pt" font-family="Verdana, Arial, Helvetica" font-style="italic" 
		keep-with-next.within-page="always" border-after-style="solid" border-after-width="1pt">
		 <xsl:apply-templates select="$header"/>
		</fo:block>
	</fo:static-content>
</xsl:template>

<!-- 
*************************************************************************
This template renders the document footer.
*************************************************************************
-->
<xsl:template name="document-footer">
<xsl:param name="page-number"/>
	<fo:static-content flow-name="xsl-region-after">
		<!-- Render hr before the footer -->
		<fo:block>
			<fo:leader leader-pattern="rule" leader-length="100%" space-before.optimum="2pt" space-after.optimum="2pt"
			color="black" rule-thickness="1pt">
			</fo:leader>
		</fo:block>
		<fo:block>
			<fo:block text-align="left" font-family="Verdana, Arial, Helvetica" font-size="8px" font-weight="bold">
			<xsl:apply-templates select="$footer"/>
			</fo:block>	
			<fo:block text-align="end" font-family="Verdana, Arial, Helvetica" font-size="8px" font-style="italic">
			<xsl:if test="$page-number = 'yes' ">Page-<fo:page-number/></xsl:if>		
			</fo:block>
		</fo:block>
	</fo:static-content>
</xsl:template>
	
<!-- 
*************************************************************************
This template renders the body.
*************************************************************************
-->
<xsl:template name="body">
	<fo:page-sequence master-name="body" master-reference="body" initial-page-number="1" force-page-count="auto">
		<!--header-->
		<xsl:call-template name="document-header"/>
		<!-- footer -->
		<xsl:call-template name="document-footer">
		<xsl:with-param name="page-number" select="'yes'"/>
		</xsl:call-template>
		<!-- apply the body -->
		<fo:flow flow-name="xsl-region-body">
			<xsl:apply-templates select="document-body"/>
		</fo:flow>
	</fo:page-sequence>
</xsl:template>

<!-- 
*************************************************************************
This template renders the cover page. 
*************************************************************************
-->
<xsl:template name="cover-page">
<fo:page-sequence master-reference="cover" force-page-count="no-force">
	<!--header-->
	<fo:static-content flow-name="xsl-region-before">
	<fo:block font-size="10pt" font-family="Verdana, Arial, Helvetica" font-style="italic"
		keep-with-next.within-page="always" border-after-style="solid" border-after-width="1pt">Open Microscopy Environment
		</fo:block>
	</fo:static-content>
	<!-- footer -->
	<xsl:call-template name="document-footer">
		<xsl:with-param name="page-number" select="'no'"/>
	</xsl:call-template>
	<!-- body -->	
	<fo:flow flow-name="xsl-region-body">	
		<!-- all attributes -->
		<fo:block text-indent="1em" font-family="Verdana, Arial, Helvetica" font-size="10pt" font-weight="bold" 
		background-color="#EEEEEE" line-height="5mm" text-align="left" color="#000000">
			<xsl:for-each select="$dmi/attribute">
				<xsl:value-of select="@name"/>: <xsl:apply-templates select="."/>
				<fo:block/>
			</xsl:for-each>
		</fo:block>	
			<!-- title -->
			<fo:block font-size="20pt" font-weight="bold" text-align="center" space-before="40mm" space-before.conditionality="retain"  color="gray">
			<xsl:value-of select="$dmi/title"/>
			</fo:block> 
			<fo:block  text-align="center" space-before="40mm" space-before.conditionality="retain" >
				<fo:external-graphic src="url('logo-selzer.gif')" /> 
			</fo:block>
	</fo:flow>
</fo:page-sequence>
</xsl:template>

<!-- 
*************************************************************************
This template renders the toc page.
*************************************************************************
-->
<xsl:template name="toc-page">
	<fo:page-sequence master-reference="toc" force-page-count="no-force">
		<!-- header -->
		<xsl:call-template name="document-header"/>
		<!-- footer -->		
		<xsl:call-template name="document-footer">
			<xsl:with-param name="page-number" select="'no'"/>
		</xsl:call-template>
		<fo:flow flow-name="xsl-region-body">
			<fo:block text-align="start" font-family="Verdana, Arial, Helvetica" font-size="13pt" 
			font-weight="bold" space-after="5pt" keep-with-next.within-page="always"
				background-color="#EEEEEE" line-height="5mm">Table Of Contents
			</fo:block>
			<fo:block>
			<fo:leader leader-pattern="rule" leader-length="100%" space-before.optimum="2pt" 
				space-after.optimum="2pt" rule-thickness="2pt"/>
			</fo:block>
			<xsl:for-each select="$toc/link">
				<xsl:call-template name="toc-link">
				<xsl:with-param name="link-node" select="."/>
				</xsl:call-template>
			</xsl:for-each>
		</fo:flow>
	</fo:page-sequence>
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
	<!-- margin-left="0em"  -->
	<fo:block space-before.optimum="5pt" font-size="1em" font-weight="700" text-align-last="justify" last-line-end-indent="-24pt" color="steelblue">
		<xsl:attribute name="margin-left"><xsl:value-of select="$section-level - 1"/>em</xsl:attribute>
		<fo:basic-link>
			<xsl:attribute name="internal-destination"><xsl:value-of select="substring-after($link-node/@href,  '#')"/></xsl:attribute>
			<xsl:if test="$generate-section-numbers = 'yes'"><xsl:value-of select="concat($section-number,  ' ')"/></xsl:if>
			<fo:inline keep-with-next.within-line="always">
				<xsl:call-template name="make-safe">
				<xsl:with-param name="str" select="$link-node/text()"/>
				</xsl:call-template>
			</fo:inline>
			<fo:inline keep-together.within-line="always">
				<fo:leader leader-pattern="dots" leader-pattern-width="1pt" leader-alignment="reference-area" keep-with-next.within-line="always"/>
				<fo:page-number-citation ref-id="{substring-after($link-node/@href,  '#')}"/>
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
Template for the document body. Defines a fo:block and applies other 
templates.
*************************************************************************
-->
<xsl:template match="document-body">
	<fo:block space-before.optimum="12pt" text-align-last="justify" >
		<xsl:apply-templates/>
	</fo:block>
</xsl:template>
	
<!-- 
*************************************************************************
This template handles a section element.
*************************************************************************
-->
<xsl:template match="section">
	<xsl:variable name="section-name"><xsl:value-of select="@name"/></xsl:variable>
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
			<!-- show section no if required -->
			<xsl:if test="$generate-section-numbers = 'yes'"><xsl:value-of select="concat($section-number, ' ')"/></xsl:if>
			<xsl:value-of select="@label"/>
		</fo:inline>
	</fo:block>
	<fo:block space-before.optimum="5pt"/>
		<fo:block>
			<xsl:for-each select="text()|*">
				<xsl:choose>
					<xsl:when test="local-name() = ''"><xsl:value-of select="."/>
					</xsl:when>
					<xsl:when test="not(local-name() = 'page-break')">
						<xsl:apply-templates select="."/>
					</xsl:when>
				</xsl:choose>
			</xsl:for-each>
			<!-- make the tree for this section -->
			<fo:block start-indent="0.5in">
				<xsl:call-template name="sub-section-links">
				<xsl:with-param name="link-node" select="$toc//link[substring-after(@href, '#')=$section-name]"/>
				<xsl:with-param name="level" select="2"/>
			</xsl:call-template>
			</fo:block>
	</fo:block>
	<fo:block space-before.optimum="20pt"/>
	<xsl:apply-templates select="page-break"/>
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
This template handles the table-of-content tag to produce 
the outline.
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
	<fo:inline text-decoration="none" color="steelblue">
		<fo:basic-link>
			<xsl:attribute name="internal-destination"><xsl:value-of select="substring-after($link/@href, '#')"/></xsl:attribute>
			<xsl:variable name="section-number">
				<xsl:call-template name="get-section-number">
					<xsl:with-param name="href" select="@href"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:value-of select="$section-number"/>
			<xsl:value-of select="text()"/>
		</fo:basic-link>
		</fo:inline>
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
			<fo:inline text-decoration="none" color="steelblue">
				<fo:basic-link>
					<xsl:attribute name="internal-destination"><xsl:value-of select="substring-after($link/@href, '#')"/></xsl:attribute>
					<xsl:value-of select="$link/text()"/>
				</fo:basic-link>
				</fo:inline>
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
This template handles a link to a section.
*************************************************************************
-->
<xsl:template match="section-link">
	<xsl:variable name="href">
		<xsl:choose>
			<xsl:when test="contains(@href, '#')"><xsl:value-of select="substring-after(@href, '#')"/></xsl:when>
			<xsl:otherwise><xsl:value-of select="@href"/></xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<fo:inline text-decoration="none" color="steelblue">
		<fo:basic-link>
			<xsl:attribute name="internal-destination"><xsl:value-of select="$href"/></xsl:attribute>
			<xsl:apply-templates/>
		</fo:basic-link>
	</fo:inline>
</xsl:template>
	
<!-- 
*************************************************************************
This template handles the link tag outside the toc.
*************************************************************************
-->
<xsl:template match="link">
<xsl:if test="count(ancestor::table-of-content) = 0 ">
	<fo:inline text-decoration="none" color="steelblue">
	<fo:basic-link>
			<xsl:attribute name="internal-destination"><xsl:value-of select="substring-after(@href, '#')"/></xsl:attribute>
			<xsl:value-of select="text()"/>
		</fo:basic-link>
	</fo:inline>
	</xsl:if>
</xsl:template>


<!-- 
*************************************************************************
This template is responsible for creating an anchor/link.
*************************************************************************
-->
<xsl:template match="a">
	<fo:inline text-decoration="none" color="steelblue">
		<!-- if name attribute is present then possibility is that it can be used as an anchor.-->
		<xsl:if test="@name">
			<xsl:attribute name="id"><xsl:value-of select="@name"/></xsl:attribute>
		</xsl:if>
		<!-- if href is not present then it is just an anchor name and not a link. -->
		<xsl:if test="not(@href)"><xsl:apply-templates/></xsl:if>
		<!-- if href is present then is is a link. -->
		<xsl:if test="@href">
			<fo:basic-link>
				<xsl:choose>
					<!-- href starts with # that means it is an internal link. -->
					<xsl:when test="starts-with(@href,  '#')">
						<xsl:attribute name="internal-destination"><xsl:value-of select="substring-after(@href,  '#')"/>
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


<!-- HTML tags -->
<!-- 
*************************************************************************
This template renders div tag.  
*************************************************************************
-->
<xsl:template match="div">
<xsl:choose>
	<xsl:when test="@class ='noteFrame'">
		<xsl:call-template name="note-frame">
					<xsl:with-param name="div" select="."/>
		</xsl:call-template>
	</xsl:when>
	<xsl:when test="@class ='noteLabel'"/>
	<xsl:otherwise>
	<fo:block space-before="1em" space-after="1em">
		<xsl:call-template name="get-alignment">
					<xsl:with-param name="align" select="@align"/>
		</xsl:call-template>
		<xsl:apply-templates/>
		</fo:block>
	</xsl:otherwise>
</xsl:choose>
</xsl:template>

<!-- 
*************************************************************************
This template renders a div tag with attribute 
class = noteFrame
*************************************************************************
-->
<xsl:template name="note-frame">
<xsl:param name="div"/>
	<xsl:variable name="note-label">
		<xsl:value-of select="$div/div[@class = 'noteLabel']/text()"/>
	</xsl:variable>
	<fo:table table-layout="fixed">
	<!-- Need to nest the block in a table b/c the "keep-together" attribute is only implemented for table-row --> 
	<fo:table-column column-width="170mm"/>
		<fo:table-body>
			<fo:table-row keep-together="always">
				<fo:table-cell>
						<fo:block font-family="Verdana, Arial, Helvetica" font-size="8pt" background-color="#F0F0FF" margin-top="20pt" 
						margin-bottom="20pt" margin-left="30pt" margin-right="30pt" color="#000000" border-color="gray" border-style="solid" 
						border-width="0.1mm" keep-with-next.within-page="always">
							<xsl:call-template name="get-alignment">
								<xsl:with-param name="align" select="@align"/>
							</xsl:call-template>
							<fo:block background-color="#7099C5">
								<fo:inline font-weight="bold"><xsl:value-of select="$note-label"/></fo:inline>
							</fo:block>
							<fo:block space-before.optimum="5pt"/>
							<fo:block margin-left="30pt" margin-right="30pt" margin-top="20pt" margin-bottom="20pt" padding-left="3mm" padding-right="3mm">
								<xsl:apply-templates/>
							</fo:block>
						</fo:block>
				</fo:table-cell>
			</fo:table-row>
		</fo:table-body>
	</fo:table>
</xsl:template>

<!-- 
*************************************************************************
This template renders preformatted text.
*************************************************************************
-->
<xsl:template match="pre">

<xsl:choose>
	<xsl:when test="@class ='code'">
	<fo:block space-before="1em" space-after="1em" wrap-option="no-wrap" white-space-collapse="false" white-space="pre" 
		font-size="0.83em" font-family="monospace"  border-color="#CFDCED" border-style="solid"  border-width="0.1mm"  
		background-color="#FFFFF0" padding="3mm">
		<xsl:apply-templates/>
	</fo:block>
	</xsl:when>
	<xsl:otherwise>
		<fo:block space-before="1em" space-after="1em" wrap-option="no-wrap" white-space-collapse="false" white-space="pre" 
		font-size="0.83em" font-family="monospace">
		<xsl:apply-templates/>
	</fo:block>
	</xsl:otherwise>
</xsl:choose>
</xsl:template>

<!-- 
*************************************************************************
This template is responsible for drawing a horizontal rule.
*************************************************************************
-->
<xsl:template match="hr">

<fo:block>
		<xsl:if test="@align">
			<xsl:call-template name="get-alignment">
				<xsl:with-param name="align" select="@align"/>
			</xsl:call-template>
		</xsl:if>
		<fo:leader leader-pattern="rule" space-before="0.67em" space-after="0.67em">
			<xsl:if test="@color">
				<xsl:attribute name="color"><xsl:value-of select="@color"/></xsl:attribute>
			</xsl:if>
			<xsl:if test="@size">
				<xsl:attribute name="rule-thickness"><xsl:value-of select="@size"/>pt</xsl:attribute>
			</xsl:if>
			<xsl:if test="@width">
				<xsl:attribute name="leader-length"><xsl:value-of select="@width"/></xsl:attribute>
			</xsl:if>
		</fo:leader>
</fo:block>
	
</xsl:template>

<!-- 
*************************************************************************
This template renders an img tag.
*************************************************************************
-->
<xsl:template match="img">
	<fo:external-graphic src="{@src}">
		<xsl:if test="@width">
			<xsl:attribute name="width"><xsl:value-of select="concat(@width, 'px')"/></xsl:attribute>
		</xsl:if>
		<xsl:if test="@height">
			<xsl:attribute name="height"><xsl:value-of select="concat(@height, 'px')"/></xsl:attribute>
		</xsl:if>
		<xsl:if test="@hspace">
			<xsl:attribute name="padding-left"><xsl:value-of select="@hspace"/>px</xsl:attribute>
			<xsl:attribute name="padding-right"><xsl:value-of select="@hspace"/>px</xsl:attribute>
		</xsl:if>
		<xsl:if test="@vspace">
			<xsl:attribute name="padding-top"><xsl:value-of select="@vspace"/>px</xsl:attribute>
			<xsl:attribute name="padding-bottom"><xsl:value-of select="@vspace"/>px</xsl:attribute>
		</xsl:if>
	</fo:external-graphic>
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
	<fo:list-block space-after="1em" space-before="1em">
		<xsl:for-each select="li">
			<fo:list-item>
				<fo:list-item-label end-indent="label-end()">
					<fo:block><fo:inline font-family="Symbol" font-size="{$bullet-size}">•</fo:inline></fo:block>
				</fo:list-item-label>
				<fo:list-item-body start-indent="body-start()">
					<fo:block><xsl:apply-templates/></fo:block>
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
	<fo:list-block space-after="1em" space-before="1em">
		<xsl:for-each select="li">
			<fo:list-item>
				<fo:list-item-label end-indent="label-end()">
					<fo:block>
						<fo:inline>
							<xsl:choose>
								<xsl:when test="parent::ol/@type"><xsl:number value="position()" format="{parent::ol/@type}"/>.</xsl:when>
								<xsl:otherwise><xsl:number value="position()"/>.</xsl:otherwise>
							</xsl:choose>
						</fo:inline>
					</fo:block>
				</fo:list-item-label>
				<fo:list-item-body start-indent="body-start()">
					<fo:block><xsl:apply-templates/></fo:block>
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
	<fo:block space-after="1em" space-before="1em">
		<xsl:apply-templates select="dt|dd"/>
	</fo:block>
</xsl:template>

<!-- 
*************************************************************************
This template renders a definition term.
*************************************************************************
-->
<xsl:template match="dt">
	<fo:block keep-with-next.within-column="always" keep-together.within-column="always">
		<xsl:apply-templates/>
	</fo:block>
</xsl:template>
	
<!-- 
*************************************************************************
This template renders a definition description.
*************************************************************************
-->
<xsl:template match="dd">
	<fo:block start-indent="24pt">
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
This template sets the font weight as bold.
*************************************************************************
-->
<xsl:template match="b|strong">
	<fo:inline font-weight="bold">
		<xsl:apply-templates/>
	</fo:inline>
</xsl:template>

<!-- 
*************************************************************************
This template sets the font weight as bold.
*************************************************************************
-->
<xsl:template match="strong//em|em//strong">
	<fo:inline font-weight="bolder" font-style="italic">
		<xsl:apply-templates/>
	</fo:inline>
</xsl:template>

<!-- 
*************************************************************************
This template handles the font-style of the following tags: 
i, em, cite, var, dfn.
*************************************************************************
-->
<xsl:template match="i|em|cite|var|dfn">
	<fo:inline font-style="italic">
		<xsl:apply-templates/>
	</fo:inline>
</xsl:template>

<!-- 
*************************************************************************
This template handles the font-familyt of the following tags: 
code, tt, kbd, samp
*************************************************************************
-->
<xsl:template match="code|tt|kbd|samp">
	<fo:inline font-family="monospace">
		<xsl:apply-templates/>
	</fo:inline>
</xsl:template>

<!-- 
*************************************************************************
This template handles the big tag.
*************************************************************************
-->
<xsl:template match="big">
	<fo:inline font-size="larger">
		<xsl:apply-templates/>
	</fo:inline>
</xsl:template>

<!-- 
*************************************************************************
This template handles the small tag.
*************************************************************************
-->
<xsl:template match="small">
	<fo:inline font-size="smaller">
		<xsl:apply-templates/>
	</fo:inline>
</xsl:template>

<!-- 
*************************************************************************
This template handles the text-decoration of the following 
tags: u, ins
*************************************************************************
-->
<xsl:template match="u|ins">
	<fo:inline text-decoration="underline">
		<xsl:apply-templates/>
	</fo:inline>
</xsl:template>

<!-- 
*************************************************************************
This template handles the text-decoration of the following 
tags: s, strike, del.
*************************************************************************
-->
<xsl:template match="s|strike|del">
	<fo:inline text-decoration="line-through">
		<xsl:apply-templates/>
	</fo:inline>
</xsl:template>

<!-- 
*************************************************************************
This template handles the p tags.
*************************************************************************
-->
<xsl:template match="p">
	<fo:block space-before="1em" space-after="1em">
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
This template renders a blockquote(indented) section.
*************************************************************************
-->
<xsl:template match="blockquote">
	<fo:block start-indent="24pt" space-before="1em" space-after="1em" end-indent="24pt">
		<xsl:apply-templates/>
	</fo:block>
</xsl:template>

<!-- 
*************************************************************************
This template renders superscript text.
*************************************************************************
-->
<xsl:template match="sup">
	<fo:inline vertical-align="super" font-size="smaller">
		<xsl:apply-templates/>
	</fo:inline>
</xsl:template>

<!-- 
*************************************************************************
This template renders subscript text.
*************************************************************************
-->
<xsl:template match="sub">
	<fo:inline vertical-align="sub" font-size="smaller">
		<xsl:apply-templates/>
	</fo:inline>
</xsl:template>
	

<!-- 
*************************************************************************
This template sets a new font for the content enclosed
within it.
*************************************************************************
-->
<xsl:template match="font">
	<fo:inline>
		<xsl:if test="@color">
			<xsl:attribute name="color"><xsl:value-of select="@color"/></xsl:attribute>
		</xsl:if>
		<xsl:if test="@face">
			<xsl:attribute name="font-family"><xsl:value-of select="@face"/></xsl:attribute>
		</xsl:if>
		<xsl:apply-templates/>
	</fo:inline>
</xsl:template>


<!-- 
*************************************************************************
This template handles the address tag.
*************************************************************************
-->
<xsl:template match="address">
	<fo:block font-style="italic">
		<xsl:apply-templates/>
	</fo:block>
</xsl:template>

<!-- 
*************************************************************************
This template handles the fieldset,form, dir, menu tag.
*************************************************************************
-->
<xsl:template match="fieldset|form|dir|menu">
	<fo:block space-before="1em" space-after="1em">
		<xsl:apply-templates/>
	</fo:block>
</xsl:template>

<!-- 
*************************************************************************
This template handles the span.
*************************************************************************
-->
<xsl:template match="span">
	<fo:inline>
		<xsl:apply-templates/>
	</fo:inline>
</xsl:template>

<!-- 
*************************************************************************
This template handles the span with a dir attribute.
*************************************************************************
-->
<xsl:template match="span[@dir]">
	<fo:bidi-override direction="{@dir}" unicode-bidi="embed">
		<xsl:apply-templates/>
	</fo:bidi-override>
</xsl:template>

<!-- 
*************************************************************************
This template handles the span with style attribute
*************************************************************************
-->
<xsl:template match="span[@style and contains(@style, 'writing-mode')]">
	<fo:inline-container alignment-baseline="central" text-indent="0pt" last-line-end-indent="0pt" 
	start-indent="0pt" end-indent="0pt" text-align="center" text-align-last="center">
		<xsl:call-template name="process-attributes" /> 
		<fo:block wrap-option="no-wrap" line-height="1">
			<xsl:apply-templates /> 
		</fo:block>
		</fo:inline-container>
 </xsl:template>

<!-- 
*************************************************************************
This template handles the h1 heading tag.
*************************************************************************
-->
<xsl:template match="h1">
	<xsl:call-template name="format-h">
		<xsl:with-param name="font-size">2em</xsl:with-param>
		<xsl:with-param name="space-before">0.67em</xsl:with-param>
		<xsl:with-param name="space-after">0.67em</xsl:with-param>
	</xsl:call-template>
</xsl:template>

<!-- 
*************************************************************************
This template handles the h2 heading tag.
*************************************************************************
-->
<xsl:template match="h2">
	<xsl:call-template name="format-h">
		<xsl:with-param name="font-size">1.5em</xsl:with-param>
		<xsl:with-param name="space-before">0.83em</xsl:with-param>
		<xsl:with-param name="space-after">0.83em</xsl:with-param>
	</xsl:call-template>
</xsl:template>

<!-- 
*************************************************************************
This template handles the h3 heading tag
*************************************************************************
-->
<xsl:template match="h3">
	<xsl:call-template name="format-h">
		<xsl:with-param name="font-size">1.17em</xsl:with-param>
		<xsl:with-param name="space-before">1em</xsl:with-param>
		<xsl:with-param name="space-after">1em</xsl:with-param>
	</xsl:call-template>
</xsl:template>

<!-- 
*************************************************************************
This template handles the h4 heading tag
*************************************************************************
-->
<xsl:template match="h4">
	<xsl:call-template name="format-h">
		<xsl:with-param name="font-size">1em</xsl:with-param>
		<xsl:with-param name="space-before">1.17em</xsl:with-param>
		<xsl:with-param name="space-after">1.17em</xsl:with-param>
	</xsl:call-template>
</xsl:template>

<!-- 
*************************************************************************
This template handles the h5 heading tag
*************************************************************************
-->
<xsl:template match="h5">
	<xsl:call-template name="format-h">
		<xsl:with-param name="font-size">0.83em</xsl:with-param>
		<xsl:with-param name="space-before">1.33em</xsl:with-param>
		<xsl:with-param name="space-after">1.33em</xsl:with-param>
	</xsl:call-template>
</xsl:template>

<!-- 
*************************************************************************
This template handles the h6 heading tag
*************************************************************************
-->
<xsl:template match="h6">
	<xsl:call-template name="format-h">
		<xsl:with-param name="font-size">0.67em</xsl:with-param>
		<xsl:with-param name="space-before">1.67em</xsl:with-param>
		<xsl:with-param name="space-after">1.67em</xsl:with-param>
	</xsl:call-template>
</xsl:template>

<!-- 
*************************************************************************
This template handles the heading tag
*************************************************************************
-->
<xsl:template name="format-h">
	<xsl:param name="font-size"/>
	<xsl:param name="space-before"/>
	<xsl:param name="space-after"/>
	<fo:block font-weight="bold" keep-with-next.within-column="always" keep-together.within-column="always">
		<xsl:attribute name="space-before"><xsl:value-of select="$space-before"/></xsl:attribute>
		<xsl:attribute name="space-after"><xsl:value-of select="$space-after"/></xsl:attribute>
		<xsl:attribute name="font-size"><xsl:value-of select="$font-size"/></xsl:attribute>
		<xsl:call-template name="get-alignment">
			<xsl:with-param name="align" select="@align"/>
		</xsl:call-template>
		<xsl:apply-templates/>
	</fo:block>
</xsl:template>

<!-- 
*************************************************************************
This template renders a table.
*************************************************************************
-->
<xsl:template match="table">
	<fo:table table-layout="fixed">
	<!-- Need to nest the block in a table b/c the "keep-together" attribute is only implemented for table-row --> 
		<fo:table-body>
			<fo:table-row keep-together="always">
				<fo:table-cell>
				<!-- start nested table-->
					<fo:table table-layout="fixed">
						<!-- bgcolor -->
						<xsl:if test="@bgcolor">
							<xsl:attribute name="background-color"><xsl:value-of select="@bgcolor"/></xsl:attribute>
						</xsl:if>
						<xsl:if test="@border">
							<xsl:attribute name="border-width"><xsl:value-of select="@border"/>pt</xsl:attribute>
							<xsl:attribute name="border-style">solid</xsl:attribute>
						</xsl:if>
						<!-- table columns -->
						<xsl:for-each select="tr[1]/td">
							<!-- determine the column width -->
							<xsl:variable name="col-width">
								<xsl:choose>
									<xsl:when test="@width">
										<xsl:choose>
											<xsl:when test="contains(@width, '%')">
												<xsl:value-of select="170 * substring-before(@width, '%') div 100"/>mm
											</xsl:when>
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
				<!-- end nested table -->
				</fo:table-cell>
			</fo:table-row>
		</fo:table-body>
		<fo:table-column column-width="170mm"/>
	</fo:table>
</xsl:template>

<!-- 
*************************************************************************
This template renders a tr - table row.
*************************************************************************
-->
<xsl:template match="tr">
	<fo:table-row  keep-together="always">
		<!-- bgcolor -->
		<xsl:if test="@bgcolor">
			<xsl:attribute name="background-color"><xsl:value-of select="@bgcolor"/></xsl:attribute>
		</xsl:if>
		<xsl:if test="@align">
			<xsl:call-template name="get-alignment">
				<xsl:with-param name="align" select="@align"/>
			</xsl:call-template>
		</xsl:if>
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
			<xsl:attribute name="background-color"><xsl:value-of select="@bgcolor"/></xsl:attribute>
		</xsl:if>
		<!-- colspan -->
		<xsl:if test="@colspan">
			<xsl:attribute name="number-columns-spanned"><xsl:value-of select="@colspan"/></xsl:attribute>
		</xsl:if>
		<!-- rowspan -->
		<xsl:if test="@rowspan">
			<xsl:attribute name="number-rows-spanned"><xsl:value-of select="@rowspan"/></xsl:attribute>
		</xsl:if>
		<xsl:if test="ancestor::table/@border">
			<xsl:attribute name="border-width"><xsl:value-of select="ancestor::table/@border"/>pt</xsl:attribute>
			<xsl:attribute name="border-style">solid</xsl:attribute>
		</xsl:if>
		<xsl:if test="ancestor::table/@cellpadding">
			<xsl:attribute name="padding"><xsl:value-of select="ancestor::table/@cellpadding"/>pt</xsl:attribute>
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
			<xsl:call-template name="get-alignment">
				<xsl:with-param name="align" select="@align"/>
			</xsl:call-template>
			<xsl:apply-templates/>
		</fo:block>
	</fo:table-cell>
</xsl:template>

<!-- B: SECTION FUNCTIONS -->

<!--
*************************************************************************
Template process tags attributes
*************************************************************************
-->
<xsl:template name="process-attributes">
	<xsl:choose>
		<xsl:when test="@xml:lang">
			<xsl:attribute name="xml:lang"><xsl:value-of select="@xml:lang" /> </xsl:attribute>
		</xsl:when>
		<xsl:when test="@lang">
			<xsl:attribute name="xml:lang"><xsl:value-of select="@lang" /></xsl:attribute>
		</xsl:when>
	</xsl:choose>
	<xsl:if test="@id">
		<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
	</xsl:if>
	<xsl:if test="@align = 'bottom' or @align = 'middle' or @align = 'top'">
		<xsl:attribute name="vertical-align"><xsl:value-of select="@align" /></xsl:attribute>
	</xsl:if>
	<xsl:if test="@style">
		<xsl:call-template name="process-style">
			<xsl:with-param name="style" select="@style" /> 
		</xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template name="process-style">
	<xsl:param name="style" /> 
	<!--  e.g., style="text-align: center; color: red"
         converted to text-align="center" color="red" --> 
	<xsl:variable name="name" select="normalize-space(substring-before($style, ':'))" /> 
	<xsl:if test="$name">
		<xsl:variable name="value-and-rest" select="normalize-space(substring-after($style, ':'))" /> 
		<xsl:variable name="value">
			<xsl:choose>
				<xsl:when test="contains($value-and-rest, ';')">
					<xsl:value-of select="normalize-space(substring-before( $value-and-rest, ';'))" /> 
				</xsl:when>
				<xsl:otherwise><xsl:value-of select="$value-and-rest" /></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
	</xsl:if>
	<xsl:variable name="rest" select="normalize-space(substring-after($style, ';'))" /> 
	<xsl:if test="$rest">
		<xsl:call-template name="process-style">
		  <xsl:with-param name="style" select="$rest" /> 
		 </xsl:call-template>
	</xsl:if>
</xsl:template>



<!--
*************************************************************************
Template to get the section no.
*************************************************************************
-->
<xsl:template name="get-section-number">
<xsl:param name="format"><xsl:value-of select="$snf"/></xsl:param>
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
				<xsl:when test="$alignment='justify'">justify</xsl:when>
				<xsl:otherwise>start</xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
	</xsl:if>
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
		<xsl:otherwise><xsl:value-of select="$str"/></xsl:otherwise>
	</xsl:choose>
</xsl:template>
	<!-- E: STRING FUNCTIONS -->
</xsl:stylesheet>
