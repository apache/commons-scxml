<?xml version="1.0"?> 
<!-- Copyright 1998-2003 W3C (MIT, ERCIM, Keio), All Rights Reserved. See http://www.w3.org/Consortium/Legal/. -->
<!-- Adapted from http://www.w3.org/Voice/2013/scxml-irp/confXPath.xsl -->
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:conf="http://www.w3.org/2005/scxml-conformance"
    version="2.0">
    
<!-- Copy everything that doesn't match other rules -->
<xsl:template match="/ | @* | node()">
  <xsl:copy>
    <xsl:apply-templates select="@* | node()"/>
  </xsl:copy>
</xsl:template>

<!-- Success criteria -->

<xsl:template match="//@conf:targetpass"> 
	<xsl:attribute name="target">pass</xsl:attribute>
</xsl:template>

<xsl:template match="conf:pass">
 <final xmlns="http://www.w3.org/2005/07/scxml" id="pass"/>
</xsl:template>

<!-- Failure criteria -->

<xsl:template match="//@conf:targetfail"> 
	<xsl:attribute name="target">fail</xsl:attribute>
</xsl:template>

<xsl:template match="conf:fail">
 <final xmlns="http://www.w3.org/2005/07/scxml" id="fail"/>
</xsl:template>

<!-- returns true if machine is in the state specified -->

<xsl:template match="//@conf:inState"> 
	<xsl:attribute name="cond">In(<xsl:value-of select="."/>)</xsl:attribute>
</xsl:template>

</xsl:stylesheet>
