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

import java.text.ParseException;
import java.util.*;

import kiwi.text.*;

/** An HTTP Cookie.
 *
 * @author Mark Lindner
 */

public class HTTPCookie
  {
  private String name;
  private String content;
  private String domain;
  private long expires = -1;
  private String path;
  private String raw;
  // date format is: Wdy, DD-Mon-YYYY HH:MM:SS GMT
  private DateFormatter fmt = new DateFormatter("%a, %d-%b-%Y %H:%M:%S %Z");

  /**
   */
  
  HTTPCookie(String text)
    {
    raw = text;

    StringTokenizer st = new StringTokenizer(text, ";");
    for(int i = 0; st.hasMoreTokens(); i++)
      {
      String tok = st.nextToken();

      StringTokenizer st2 = new StringTokenizer(tok, "=");

      if(st2.countTokens() != 2)
        continue;

      String k = st2.nextToken().trim();
      String v = st2.nextToken();

//      System.out.println("cookie key: " + k);
//      System.out.println("cookie val: " + v);

      if(i == 0)
        {
        name = k;
        content = v;
        }
      else
        {
        if(k.equalsIgnoreCase("Domain"))
          domain = v;
        else if(k.equalsIgnoreCase("Expires"))
          {
          synchronized(fmt)
            {
            try
              {
              Date dt = fmt.parse(v);
              expires = dt.getTime();
              }
            catch(ParseException ex)
              {
              //System.out.println("error parsing cookie date: " + v);
              expires = -1;
              }
            }
          }
        else if(k.equalsIgnoreCase("Path"))
          path = v;
        }
      }
    }

  /** Get the name fo the cookie.
   */
  
  public String getName()
    {
    return(name);
    }

  /** Get the content of the cookie.
   */
  
  public String getContent()
    {
    return(content);
    }

  /** Get the domain of the cookie.
   */
  
  public String getDomain()
    {
    return(domain);
    }

  /** Get the expiration time of the cookie.
   */
  
  public long getExpireTime()
    {
    return(expires);
    }

  /** Determine if this cookie is expired.
   *
   * @param now The current time.
   * @return <b>true</b> if the cookie is expired, <b>false</b> otherwise.
   */

  public boolean isExpired(long now)
    {
    return((expires >= 0) && (expires < now));
    }

  /** Get the path of the cookie.
   */
  
  public String getPath()
    {
    return(path);
    }

  /**
   */
  
  public String toString()
    {
    return(raw);
    }
    
  }

// eof
