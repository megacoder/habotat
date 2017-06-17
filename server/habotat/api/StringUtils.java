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
import java.util.*;

/**
 * Assorted string utilities.
 *
 * @author Mark Lindner
 */

public final class StringUtils
  {
  private StringUtils() { }

  /** Parse a command string into words, breaking the text on whitespace but
   * preserving literal strings that are enclosed in double quotes.
   *
   * @param command The text to parse.
   * @return The array of parsed words.
   */
  
  public static String[] parseCommand(String command)
    {
    char quoteChar = '\"';

    if(command == null)
      return(null);

    ArrayList<String> vec = new ArrayList<String>();
    
    try
      {
      StreamTokenizer st = new StreamTokenizer(new StringReader(command));
      st.ordinaryChars('!', '~');
      st.wordChars('!', '~');
      st.quoteChar(quoteChar);
      
      while(st.nextToken() != st.TT_EOF)
        {
        if((st.ttype == st.TT_WORD) || (st.ttype == quoteChar))
          vec.add(st.sval);
        }
      }
    catch(IOException ex) { /* ignore */ }

    String words[] = new String[vec.size()];
    vec.toArray(words);

    return(words);
    }

  /** Parse a CSV record into fields.
   *
   * @param text The text to parse.
   * @return A list of fields.
   */
  
  public static List<String> parseCSVLine(String text)
    {
    LinkedList<String> list = new LinkedList<String>();
    
    try
      {
      Reader r = new StringReader(text);
      StreamTokenizer st = new StreamTokenizer(r);
      st.resetSyntax();
      st.wordChars('!', '~');
      st.quoteChar('\"');
      st.ordinaryChar(',');

      int prev = ',';
      for(;;)
        {
        int tok = st.nextToken();

        if(tok == st.TT_EOF)
          break;
        else if((tok == ',') && (prev == tok))
          list.add("");
        else if((tok == st.TT_WORD) || tok == '\"')
          list.add(st.sval);

        prev = tok;
        }
      
      }
    catch(IOException ex)
      {
      }

    return(list);
    }

  }

/* end of source file */
