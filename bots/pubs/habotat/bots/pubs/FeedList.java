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

package habotat.bots.pubs;

import kiwi.ui.model.*;

import habotat.api.*;

/**
 * @author Mark Lindner
 */

class FeedList extends DefaultKListModel
  {
  private static final String columns[] = { "Keyword", "Title", "RSS Type" };
  private static final Class types[] = { String.class, String.class,
                                         String.class };


  public Object getValueForProperty(Object item, String property)
    {
    if(item == null)
      {
      if(property.equals(COLUMN_NAMES_PROPERTY))
        return(columns);

      else if(property.equals(COLUMN_TYPES_PROPERTY))
        return(types);

      else
        return(null);
    }
    else
      {
      RSSFeed feed = (RSSFeed)item;

      if(property.equals("Keyword"))
        return(feed.getKeyword());
      else if(property.equals("Title"))
        return(feed.getTitle());
      else if(property.equals("RSS Type"))
        return(feed.getTypeAsString());
      else
        return(null);
      }
    }
  
  }

/* end of source file */
