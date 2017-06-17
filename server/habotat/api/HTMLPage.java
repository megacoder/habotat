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

package habotat.api;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

import kiwi.io.StreamUtils;
import kiwi.text.*;

/** An abstraction of an HTML page. This class provides access to the
 * text of the page itself, and any HTML forms within the page, as
 * well as a means to "scrape" the text of the page for specific bits
 * of data using regular expression pattern matching.
 *
 * @author Mark Lindner
 */

public class HTMLPage
  {
  private HTMLForm currentForm = null;
  private boolean inForm = false, inScript = false;
  private HashMap<String, HTMLForm> forms = new HashMap<String, HTMLForm>();
  private ArrayList<HTMLForm> formList = new ArrayList<HTMLForm>();
  private URL url;
  private StringBuffer buf = null;
  private String text;

  /** Fetch and parse an HTML Page.
   *
   * @param url The URL of the page to fetch.
   * @return A new <code>HTMLPage</code> object representing the retrieved
   * page.
   * @throws java.io.IOException If a network error occurs.   
   */
  
  public static HTMLPage fetch(URL url) throws IOException
    {
    return(fetch(url, null, true));
    }
  
  /** Fetch and parse an HTML Page.
   *
   * @param url The URL of the page to fetch.
   * @param cookies A cookie store for storing any cookies received from the
   * server.
   * @return A new <code>HTMLPage</code> object representing the retrieved
   * page.
   * @throws java.io.IOException If a network error occurs.   
   */

  public static HTMLPage fetch(URL url, CookieStore cookies)
    throws IOException
    {
    return(new HTMLPage(url, cookies, true));
    }

  /** Fetch an HTML Page.
   * @param url The URL of the page to fetch.
   * @param cookies A cookie store for storing any cookies received from the
   * server (may be <b>null</b>).
   * @param parse A flag indicating whether the body of the page should be
   * parsed to find HTML forms and strip out all markup.
   * @return A new <code>HTMLPage</code> object representing the retrieved
   * page.
   * @throws java.io.IOException If a network error occurs.   
   */
  
  public static HTMLPage fetch(URL url, CookieStore cookies, boolean parse)
    throws IOException
    {
    return(new HTMLPage(url, cookies, parse));
    }

  /*
   */

  HTMLPage(URL url, CookieStore cookies, boolean parse) throws IOException
    {
    this.url = url;
    
    read(url, cookies, parse);
    }

  /*
   */

  HTMLPage(HttpURLConnection conn, CookieStore cookies)
    throws IOException
    {
    read(conn, cookies, true);
    }

  /*
   */
  
  HTMLPage(HttpURLConnection conn, CookieStore cookies, boolean parse)
    throws IOException
    {
    read(conn, cookies, parse);
    }

  /*
   */
  
  private void read(URL url, CookieStore cookies, boolean parse)
    throws IOException
    {
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();

    conn.setRequestMethod("GET");
    conn.setDoOutput(true);
    conn.setDoInput(true);

    conn.setRequestProperty("Host", url.getHost());

    if(cookies != null)
      cookies.write(conn);
     
    conn.connect();

    read(conn, cookies, parse);
    }

  /**
   */
  
  private void read(HttpURLConnection conn, CookieStore cookies, boolean parse)
    throws IOException
    {
    int code = conn.getResponseCode();

    if(code == conn.HTTP_MOVED_TEMP)
      {
      //System.out.println("It's a redirect!");
      
      String loc = conn.getHeaderField("Location");
      //System.out.println("New loc: " + loc);

      try
        {
        url = new URL(loc);
        read(url, cookies, parse);
        return;
        }
      catch(MalformedURLException ex)
        {
        //HabotatServer.logError(ex);
        }
      }

    for(int i = 1; ; i++)
      {
      String key = conn.getHeaderFieldKey(i);
      if(key == null)
        break;

      String value = conn.getHeaderField(i);

      if(key.equalsIgnoreCase("Set-Cookie"))
        {
        if(cookies != null)
          cookies.add(new HTTPCookie(value));
        }

      //System.out.println(key + " = " + value);
      }

    InputStream ins = conn.getInputStream();

    if(parse)
      {
      HTMLParser parser = new HTMLParser(ins);

      try
        {
        parser.parse();
        }
      catch(EOFException ex)
        {
        }

      text = parser.getText();
      parser = null;
      }
    else
      text = StreamUtils.readStreamToString(ins);

    try
      {
      ins.close();
      }
    catch(IOException ex) { }
    
    conn.disconnect();

    currentForm = null;
    inForm = false;
    }

  /**
   */
  
  HTMLPage(InputStream stream)
    {
    }

  /** Get the number of HTML forms in the page.
   */
  
  public int getFormCount()
    {
    return(formList.size());
    }
  
  /**
   * Get the HTML form at the given position in the page.
   *
   * @param index The index of the form, where 0 indicates the first form
   * in the page.
   */

  public HTMLForm getForm(int index)
    {
    return(formList.get(index));
    }

  /**
   * Get the HTML form with the given name in the page.
   *
   * @param name The name of the form.
   */
  
  public HTMLForm getForm(String name)
    {
    return(forms.get(name));
    }

  /**
   * Find a form by its target URL.
   */

  public HTMLForm findForm(URL target)
    {
    for(HTMLForm form : formList)
      {
      if(form.getTarget().equals(target))
        return(form);
      }

    return(null);
    }

  /** Get the text of the page. If the page was parsed, the text will
   * be devoid of any markup, including HTML tags, JavaScript code,
   * and CSS data; all contiguous segments of markup will be replaced
   * with a single newline in the text, to prevent the concatenation
   * of words which are separated only by intervening markup. Otherwise,
   * the text will contain the actual content of the page.
   */
  
  public String getText()
    {
    return(text);
    }

  /** Scan the text of the HTML page for a matching pattern.
   *
   * @param pattern The regular expression pattern to scan for.
   * @return A Matcher object if the pattern was found, otherwise
   * <code>null</code>.
   */
  
  public Matcher scan(Pattern pattern)
    {
    Matcher mat = pattern.matcher(text);
    return(mat.find() ? mat : null);
    }

  private class HTMLParser extends XMLParser
    {
    private StringBuffer buf;
    
    HTMLParser(InputStream ins)
      {
      super(new InputStreamReader(ins));

      buf = new StringBuffer(1024);
      }

    public void consumeText(String text)
      {
      if(! inScript)
        {
        // this is bizarre...eBay pages (at least) use these non-breaking space
        // characters (0xA0) which Java interprets as the Unicode character
        // 65533.

        text = text.replace('\uFFFD', ' ');
        buf.append(text);
        }
      }

    public void consumeEntity(String entity)
      {
      if(entity.equals("amp"))
        buf.append('&');
      else if(entity.equals("lt"))
        buf.append('<');
      else if(entity.equals("gt"))
        buf.append('>');
      else if(entity.equals("nbsp"))
        buf.append(' ');
      else if(entity.equals("quot"))
        buf.append('\"');
      else if(entity.equals("apos"))
        buf.append('\'');
      else if(entity.equals("mdash") || entity.equals("ndash"))
        buf.append('-');
      else if(entity.equals("middot"))
        buf.append('*');
      else
        buf.append('?'); // unsupported entity
      }

    public void consumeElement(XMLElement e)
      {
      String tag = e.getTag();

      int l = buf.length();
      if((l > 0) && (buf.charAt(l - 1) != '\n'))
        buf.append('\n');

      if(tag.equalsIgnoreCase("form"))
        {
        if(inForm && e.isEnd())
          {
          if(currentForm != null)
            {
            forms.put(currentForm.getName(), currentForm);
            formList.add(currentForm);
            currentForm = null;
            }
        
          inForm = false;
          }
        else if(! inForm && ! e.isEnd())
          {
          String name = e.getAttributeValue("name");
          String urltxt = e.getAttributeValue("action");
          String mth = e.getAttributeValue("method");

          inForm = true;
        
          if((urltxt != null) && (mth != null))
            {
            int method = HTMLForm.POST;
            if(mth.equals("GET"))
              method = HTMLForm.GET;

            URL url = null;

            try
              {
              url = new URL(urltxt);
              currentForm = new HTMLForm(name, method, url);
              }
            catch(MalformedURLException ex)
              {
              // this should never happen
              //HabotatServer.logError(ex);
              }          
            }
          }
        }
      else if(tag.equalsIgnoreCase("input") && inForm && (currentForm != null))
        {
        String name = e.getAttributeValue("name");
        String value = e.getAttributeValue("value");
        String type = e.getAttributeValue("type");

        if((name != null) && (type != null))
          {
          if(! type.equalsIgnoreCase("submit"))
            currentForm.set(name, value);
          }
        }
      else if(tag.equalsIgnoreCase("script")
              || tag.equalsIgnoreCase("style"))
        {
        // skip over all the JavaScript and CSS crap...
      
        inScript = !e.isEnd();
        }
      }

    public String getText()
      {
      String s = buf.toString();
      buf = null;
      return(s);
      }

    }
  
  }

// eof
