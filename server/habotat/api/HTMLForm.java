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
import java.util.*;

/** An abstraction of an HTML form.
 *
 * @author Mark Lindner
 */

public class HTMLForm
  {
  private String name;
  private HashMap<String, String[]> elements = new HashMap<String, String[]>();
  /** The HTTP "GET" method. */
  public static final int GET = 0;
  /** The HTTP "POST" method. */
  public static final int POST = 1;
  private URL target;
  private int method = POST;

  /** Construct a new <code>HTMLForm</code> with the given name, method,
   * and target.
   *
   * @param name The name for the form.
   * @param method The method for the form: either <code>GET</code> or
   * <code>POST</code>.
   * @param target The URL to which the form will be submitted.
   * @throws java.lang.IllegalArgumentException If the method is invalid or
   * the target is <code>null</code>.
   */
  
  public HTMLForm(String name, int method, URL target)
    {
    if((target == null) || ((method != GET) && (method != POST)))
      throw(new IllegalArgumentException());
    
    this.name = name;
    this.method = method;
    this.target = target;
    }

  /** Get the name of the form.
   *
   * @return The name of the form, or <code>null</code> if it does not have
   * a name.
   */
  
  public String getName()
    {
    return(name);
    }

  /** Set the value of a parameter.
   *
   * @param name The parameter name.
   * @param value The value to assign to the parameter.
   * @throws java.lang.IllegalArgumentException If name is null.
   */
  
  public void set(String name, String value)
    {
    if(name == null)
      throw(new IllegalArgumentException());

    elements.put(name, new String[] { value });
    }

  /** Set the values of a parameter.
   *
   * @param name The parameter name.
   * @param values An array of values to assign to the parameter.
   *
   * @throws java.lang.IllegalArgumentException If either argument is null,
   * or if values is an empty array.
   */

  public void set(String name, String values[])
    {
    if(name == null)
      throw(new IllegalArgumentException());

    elements.put(name, values);
    }

  /** The the value for the given parameter.
   *
   * @param name The parameter name.
   * @return The value for the given parameter, or <code>null</code> if
   * there are none. If there are multiple values for the given parameter,
   * the first one is returned.
   */
  
  public String get(String name)
    {
    String values[] = elements.get(name);
    if(values == null)
      return(null);

    return(values[0]);
    }

  /** Get all values for the given parameter.
   *
   * @param name The parameter name.
   * @return The values for the given parameter, or <code>null</code> if
   * there are none.
   */
  
  public String[] getAll(String name)
    {
    return(elements.get(name));
    }

  /** Submit the form.
   *
   * @return An <code>HTMLPage</code> containing the response from the
   * submission.
   * @throws java.io.IOException If a network error occurs.
   */
  
  public HTMLPage submit() throws IOException
    {
    return(submit(null));
    }

  /** Submit the form.
   *
   * @param cookies A cookie store. Any cookies in the store that match the
   * target URL of the form will be submitted along with the form data.
   * @return An <code>HTMLPage</code> containing the response from the
   * submission.
   * @throws java.io.IOException If a network error occurs.
   */
  
  public HTMLPage submit(CookieStore cookies) throws IOException
    {
    //System.out.println("submit to: " + target);
    
    HttpURLConnection conn = (HttpURLConnection)target.openConnection();

    conn.setRequestMethod(method == POST ? "POST" : "GET");
    conn.setDoOutput(true);
    conn.setDoInput(true);

    conn.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded");

    conn.setRequestProperty("Host", target.getHost());
    

    if(cookies != null)
      cookies.write(conn);

    // URL encode the stuff

    String payload = urlEncode();

    //System.out.println("payload:");
    //System.out.println(payload);
    
    conn.setRequestProperty("Content-Length",
                            String.valueOf(payload.length()));

    conn.connect();
    
    OutputStream outs = conn.getOutputStream();

    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outs));
    
    writer.write(payload);
    writer.flush();
    
    writer.close();
    outs.close();

    HTMLPage page = new HTMLPage(conn, cookies);
    
    conn.disconnect();

    return(page);
    }

  /** Get the submission method for this form; either <code>GET</code> or
   * <code>POST</code>.
   */
  
  public int getMethod()
    {
    return(method);
    }

  /** Get the target of this form.
   */
  
  public URL getTarget()
    {
    return(target);
    }

  /**
   */
  
  public void dump()
    {
    System.out.println("Form:   " + name);
    System.out.println("Method: " + (method == POST ? "POST" : "GET"));
    System.out.println("Target: " + target);
    System.out.println();

    Iterator<String> iter = elements.keySet().iterator();
    while(iter.hasNext())
      {
      String key = iter.next();
      String val[] = elements.get(key);

      if(val == null)
        continue;

      for(int i = 0; i < val.length; i++)
        System.out.println("\t" + key + " = " + val[i]);
      }
    }

  /*
   */
  
  private void urlEncodeKV(StringBuffer buf, String key, String value)
    {
      if(buf.length() > 0)
        buf.append('&');
    
      try
        {
        buf.append(URLEncoder.encode(key, "UTF-8"));
        buf.append('=');
        if(value != null)
          buf.append(URLEncoder.encode(value, "UTF-8"));
        }
      catch(UnsupportedEncodingException ex) {}
    }
  
  /**
   */
  
  String urlEncode()
    {
    StringBuffer buf = new StringBuffer();

    Iterator<String> iter = elements.keySet().iterator();
    while(iter.hasNext())
      {
      String key = iter.next();
      String[] val = elements.get(key);

      for(int i = 0; i < val.length; i++)
        urlEncodeKV(buf, key, val[i]);
      }

    return(buf.toString());
    }
  
  }

// eof
