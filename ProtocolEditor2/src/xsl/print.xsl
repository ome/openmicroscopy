<?xml version="1.0"?>
<!-- print.xsl -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="html"/>

<xsl:template match="protocol">
  <html><head>
  	<style type='text/css'>
 		 div {padding: 5px 30px 5px 30px; margin: 5px; font-family: Arial;}
 		 h3 {padding: 0px; margin:0px; font-size: 110%;}
 		 .protocol {background: #dddddd; padding: 5px; font-size: 120%; border: 1px #390d61 solid;}
 	 </style>
  </head>
  <body>
  <div class="protocol">
  		<h3><xsl:value-of select="@name"/></h3>
  </div>
  
    <xsl:apply-templates/>
  </body></html>
</xsl:template>



<xsl:template match="input">

<div>
  <h3><xsl:value-of select="@name"/></h3>
  <div style='padding: 5px 30px 5px 30px;'>
  	<xsl:value-of select="@description"/><br/>
  </div>
  
  <xsl:if test="@substepsCollapsed != 'true'">
  
  	<xsl:apply-templates/>
  	
  </xsl:if>
  
 </div>
 
</xsl:template>



</xsl:stylesheet>