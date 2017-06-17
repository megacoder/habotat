##-----------------------------------------------------------------------------
## Process this file with automake to produce Makefile.in
##-----------------------------------------------------------------------------

# classpath for compilation

CLASSPATH	= ../../libs/kiwi.jar \
		  ../../libs/jdom.jar \
		  ../../server/habotat.jar

# additional files & dirs to include in distribution

EXTRA_FILES	= 

# jar file to generate

JARFILE = jotbot.jar

JAR_SUBDIR = plugins

# packages to compile

PACKAGES	= habotat.bots.jotbot

# resource directories to include in jar file

RESOURCE_DIRS   = habotat/bots/jotbot/images

# javadoc window & document title

JAVADOC_TITLE	= "$(PACKAGE)"

MANIFEST_FILE = manifest.mf

# import the generic Java build logic

include ../../java.mak

## eof
