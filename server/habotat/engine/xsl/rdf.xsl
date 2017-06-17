<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
  exclude-result-prefixes="rdf purl"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:purl="http://purl.org/rss/1.0/">
  
  <xsl:output method="html" omit-xml-declaration="yes"/>

  <xsl:template match="rdf:RDF">
    <xsl:apply-templates select="rdf:channel"/>
    <xsl:apply-templates select="purl:channel"/>
    <xsl:apply-templates select="rdf:item"/>
    <xsl:apply-templates select="purl:item"/>
  </xsl:template>

  <xsl:template match="rdf:channel">
    <b><a><xsl:attribute name="href"><xsl:value-of select="rdf:link"/></xsl:attribute><xsl:value-of select="rdf:title"/></a></b><br/>
  </xsl:template>

  <xsl:template match="purl:channel">
    <b><a><xsl:attribute name="href"><xsl:value-of select="purl:link"/></xsl:attribute><xsl:value-of select="purl:title"/></a></b><br/>
  </xsl:template>
  
  <xsl:template match="rdf:item">
    <xsl:if test="position() &lt;= 20">
      <a><xsl:attribute name="href"><xsl:value-of select="rdf:link"/></xsl:attribute><xsl:value-of select="rdf:title"/></a><br/>
      <xsl:comment>X</xsl:comment>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="purl:item">
    <xsl:if test="position() &lt;= 20">
      <a><xsl:attribute name="href"><xsl:value-of select="purl:link"/></xsl:attribute><xsl:value-of select="purl:title"/></a><br/>
      <xsl:comment>X</xsl:comment>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>