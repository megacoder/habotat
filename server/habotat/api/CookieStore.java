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

import java.net.HttpURLConnection;
import java.util.*;

/** Storage for HTTP cookies. This class manages HTTP cookies, which are
 * grouped by domain.
 *
 * @author Mark Lindner
 */

public class CookieStore
  {
  private HashMap<String, HashMap<String, HTTPCookie>> cookies
    = new HashMap<String, HashMap<String, HTTPCookie>>();

  /** Construct a new <code>CookieStore</code>.
   */

  public CookieStore()
    {
    }

  /** Add a cookie to the cookie store.
   *
   * @param cookie The cookie to add.
   */
  
  public void add(HTTPCookie cookie)
    {
    String domain = cookie.getDomain();
    
    HashMap<String, HTTPCookie> map = cookies.get(domain);
    if(map == null)
      {
      map = new HashMap<String, HTTPCookie>();
      cookies.put(domain, map);
      }
    
    map.put(cookie.getName(), cookie);

    // System.out.println("added cookie: " + cookie);
    }

  /** Look up a cookie by domain and name.
   *
   * @param domain The domain of the cookie.
   * @param name The name of the cookie.
   * @return The matching cookie, if found, otherwise <b>null</b>.
   */

  public HTTPCookie get(String domain, String name)
    {
    HashMap<String, HTTPCookie> map = cookies.get(domain);
    if(map == null)
      return(null);
    
    return(map.get(name));
    }

  /** Return an iterator to all of the cookies for the given domain.
   *
   * @param domain The domain.
   * @return An iterator to the cookies for the given domain, or
   * <code>null</code> if none were found.
   */
  
  public Iterator<HTTPCookie> getCookies(String domain)
    {
    HashMap<String, HTTPCookie> map = cookies.get(domain);
    if(map == null)
      return(null);
    
    return(map.values().iterator());
    }

  /** Remove all cookies from the cookie store.
   */
  
  public void clear()
    {
    cookies.clear();
    }

  /** Remove all cookies for the given domain from the cookie store.
   *
   * @param domain The domain.
   */

  public void clear(String domain)
    {
    cookies.remove(domain);
    }

  /** Remove a specific cookie from the cookie store.
   *
   * @param domain The domain of the cookie.
   * @param name The name of the cookie.
   */

  public void clear(String domain, String name)
    {
    HashMap<String, HTTPCookie> map = cookies.get(domain);
    if(map != null)
      map.remove(name);
    }
  
  /*
   */

  private String domainForHost(String host)
    {
    boolean isIP = true;
    
    for(int i = 0; i < host.length(); i++)
      {
      char c = host.charAt(i);
      if(!((c >= '0' && c <= '9') || (c == '.')))
        {
        isIP = false;
        break;
        }
      }

    if(isIP)
      return(host);

    int x = host.lastIndexOf('.');
    if(x < 0)
      return(null);

    x = host.lastIndexOf('.', --x);
    if(x < 0)
      return(null);

    //System.out.println("domain for " + host + " is " + host.substring(x));
    
    return(host.substring(x));
    
    }

  /** Write the cookies to an HTTP connection. This method writes the
   * appropriate HTTP headers to send the cookies for the corresponding domain
   * (if any) to the HTTP server. Any cookies which have expired will not be
   * sent, and will be removed from the cookie store.
   *
   * @param conn The HTTP connection.
   */
  
  public void write(HttpURLConnection conn)
    {
    String host = conn.getURL().getHost();
    String domain = domainForHost(host);
    long now = System.currentTimeMillis();
    
    Iterator<HTTPCookie> iter = getCookies(domain);
    if(iter != null)
      {
      while(iter.hasNext())
        {
        HTTPCookie cookie = iter.next();

        if(cookie.isExpired(now))
          iter.remove();
        else
          conn.setRequestProperty("Cookie", cookie.getName() + "="
                                  + cookie.getContent());
        }
      }
    }
  
  }

// eof
