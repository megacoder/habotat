##-----------------------------------------------------------------------------
## Process this file with automake to produce Makefile.in
##-----------------------------------------------------------------------------

# classpath for compilation

CLASSPATH	= ../../server/habotat.jar \
		  ../../libs/smack.jar \
		  ../../libs/smackx.jar \
		  ../../libs/kiwi.jar \
		  ../../libs/jdom.jar

# additional files & dirs to include in distribution

EXTRA_FILES	= 

# jar file to generate

JARFILE = jabber.jar

JAR_SUBDIR = adapters

# packages to compile

PACKAGES	= habotat.adapters.jabber

# resource directories to include in jar file

RESOURCE_DIRS   = habotat/adapters/jabber/images

# javadoc window & document title

JAVADOC_TITLE	= "$(PACKAGE)"

MANIFEST_FILE = manifest.mf

# import the generic Java build logic

include ../../java.mak

## eof
