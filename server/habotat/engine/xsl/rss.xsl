<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
  exclude-result-prefixes="rdf purl"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:purl="http://purl.org/rss/1.0/">
  
  <xsl:output method="html"/>

  <xsl:template match="rss">
    <xsl:apply-templates select="channel"/>
  </xsl:template>

  <xsl:template match="channel">
    <b><a><xsl:attribute name="href"><xsl:value-of select="link"/></xsl:attribute><xsl:value-of select="title"/></a></b><br/>
    <xsl:apply-templates select="item"/>
  </xsl:template>

  <xsl:template match="item">
    <xsl:if test="position() &lt;= 20"> <!-- in case it's excessively long -->
      <a><xsl:attribute name="href"><xsl:value-of select="link"/></xsl:attribute><xsl:value-of select="title"/></a><br/>
      <xsl:apply-templates select="description"/>
      <xsl:comment>X</xsl:comment>
    </xsl:if>
  </xsl:template>

  <xsl:template match="description">
    <xsl:value-of select="text()"/><br/>
  </xsl:template>
  
</xsl:stylesheet>
