/* ----------------------------------------------------------------------------
   Habotat
   Copyright (C) 2004-05 Mark A. Lindner

   This file is part of Habotat.
   
   This program is free software; you can redistribute it and/or modify it
   under the terms of the GNU General Public License as published by the
   Free Software Foundation; either version 2 of the License, or (at your
   option) any later version.

   This program is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.

   You should have received a copy of the GNU General Public License along
   with this program; if not, write to the Free Software Foundation, Inc.,
   59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 
   The author may be contacted at <mark_a_lindner@yahoo.com>
   ----------------------------------------------------------------------------
*/

package habotat.engine;

import java.io.*;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import habotat.api.*;

/**
 *
 * @author Mark Lindner
 */

class XSLManager
  {
  private TransformerFactory transFactory;
  private HashMap<BotDef, HashMap<String, Transformer>> styleSheets
    = new HashMap<BotDef, HashMap<String, Transformer>>();
  private static final int MAX_STYLESHEETS = 5;
  private static final String HTML_TOKEN = "<!--X-->";

  /*
   */
  
  XSLManager()
    {
    transFactory = TransformerFactory.newInstance();
    }

  /*
   */
  
  void addSystemStyleSheet(String name, String data)
    {
    try
      {
      StringReader reader = new StringReader(data);
      Transformer trans = transFactory.newTransformer(
        new StreamSource(reader));

      HashMap<String, Transformer> hash = styleSheets.get(null);
      if(hash == null)
        {
        hash = new HashMap<String, Transformer>();
        styleSheets.put(null, hash);
        }

      hash.put(name, trans);
      }
    catch(TransformerException ex)
      {
      // this shouldn't ever happen
      HabotatServer.logError("Error creating XSL transformer", ex);
      }
    }

  /*
   */
  
  void removeSystemStyleSheet(String name)
    {
    HashMap<String, Transformer> hash = styleSheets.get(null);
    if(hash != null)
      hash.remove(name);
    }

  /*
   */
  
  void addStyleSheet(BotDef bot, String name, String data)
    throws BotException
    {
    if((name == null) || (data == null))
      throw(new BotException("Invalid arguments"));
    
    HashMap<String, Transformer> hash = styleSheets.get(bot);
    if(hash == null)
      {
      hash = new HashMap<String, Transformer>();
      styleSheets.put(bot, hash);
      }
    else if(hash.size() == MAX_STYLESHEETS)
      throw(new BotException("maximum number of stylesheets added"));

    else if(hash.containsKey(name))
      throw(new BotException("duplicate stylesheet name"));
    
    try
      {
      StringReader reader = new StringReader(data);
      Transformer trans = transFactory.newTransformer(
        new StreamSource(reader));
      hash.put(name, trans);
      }
    catch(TransformerException ex)
      {
      throw(new BotException("transformer error"));
      }
    }

  /*
   */

  void removeStyleSheet(BotDef bot, String name) throws BotException
    {
    HashMap<String, Transformer> hash = styleSheets.get(bot);
    if(hash == null)
      throw(new BotException("no such stylesheet: " + name));

    if(! hash.containsKey(name))
      throw(new BotException("no such stylesheet: " + name));

    hash.remove(name);
    }

  /*
   */

  void removeAllStyleSheets(BotDef bot)
    {
    HashMap<String, Transformer> hash = styleSheets.get(bot);
    if(hash != null)
      hash.clear();
    }

  /*
   */
  
  String applyStyleSheet(BotDef bot, String name, String data, int maxLength)
    throws BotException
    {
    Transformer trans = null;
    
    HashMap<String, Transformer> hash = styleSheets.get(bot);
    if(hash == null)
      hash = styleSheets.get(null);

    if(hash != null)
      trans = hash.get(name);
    
    if(trans == null)
      throw(new BotException("no such stylesheet"));

    String result = "";
    
    try
      {
      StringReader reader = new StringReader(data);
      StringWriter writer = new StringWriter();
      trans.transform(new StreamSource(reader), new StreamResult(writer));
      reader.close();
      result = writer.toString();
      }
    catch(TransformerException ex)
      {
      HabotatServer.logError("Error transforming XSL", ex);
      }

    result = result.replaceAll("\n", "");

    int len = 0, start = 0, end;
    boolean truncate = false;

    while((end = result.indexOf(HTML_TOKEN, start)) >= 0)
      {
      if(end > maxLength)
        {
        truncate = true;
        break;
        }

      start = ++end;
      }
    
    if(truncate)
      result = result.substring(0, start - 1);

    result = result.replaceAll(HTML_TOKEN, "");
    
    return(result);
    }
  
  }

/* end of source file */

