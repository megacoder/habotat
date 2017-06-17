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

import org.jdom.*;

/**
 * Convenience methods for manipulating XML data.
 *
 * @author Mark Lindner
 */

public final class XMLUtils
  {
  private XMLUtils() { }

  /** Get the value of an attribute as an integer.
   *
   * @param elem The element.
   * @param attr The attribute name.
   * @return The value of the attribute.
   * @throws habotat.api.InvalidXMLException If a parsing error occurs.
   */

  public static int getIntAttr(Element elem, String attr)
    throws InvalidXMLException
    {
    return(getIntAttr(elem, attr, 0, false));
    }
  
  /** Get the value of an attribute as an integer.
   *
   * @param elem The element.
   * @param attr The attribute name.
   * @param defaultValue The default value to return if the attribute is
   * not present.
   * @return The value of the attribute.
   * @throws habotat.api.InvalidXMLException If a parsing error occurs.
   */

  public static int getIntAttr(Element elem, String attr, int defaultValue)
    throws InvalidXMLException
    {
    return(getIntAttr(elem, attr, defaultValue, false));
    }
  
  /** Get the value of an attribute as an integer.
   *
   * @param elem The element.
   * @param attr The attribute name.
   * @param defaultValue The default value to return if the attribute is
   * not present.
   * @param required A flag indicating whether the attribute must be
   * present.
   * @return The value of the attribute.
   * @throws habotat.api.InvalidXMLException If a parsing error occurs, or
   * if <code>required</code> is <b>true</b> and the attribute is not
   * present.
   */
  
  public static int getIntAttr(Element elem, String attr, int defaultValue,
                               boolean required)
    throws InvalidXMLException
    {
    String val = elem.getAttributeValue(attr);
    if(val == null)
      {
      if(required)
        throw(new InvalidXMLException("Missing attribute: " + attr));
      else
        return(defaultValue);
      }

    int value = 0;

    try
      {
      value = Integer.parseInt(val);
      }
    catch(NumberFormatException ex)
      {
      throw(new InvalidXMLException("Invalid value: " + val));
      }

    return(value);
    }

  /** Set the value of an attribute as an integer.
   *
   * @param elem The element.
   * @param attr The attribute name.
   * @param value The value to set.
   */

  public static void setIntAttr(Element elem, String attr, int value)
    {
    elem.setAttribute(attr, String.valueOf(value));
    }

  /** Get the value of an attribute as an double.
   *
   * @param elem The element.
   * @param attr The attribute name.
   * @return The value of the attribute.
   * @throws habotat.api.InvalidXMLException If a parsing error occurs.
   */

  public static double getFloatAttr(Element elem, String attr)
    throws InvalidXMLException
    {
    return(getFloatAttr(elem, attr, 0.0, false));
    }
  
  /** Get the value of an attribute as a double.
   *
   * @param elem The element.
   * @param attr The attribute name.
   * @param defaultValue The default value to return if the attribute is
   * not present.
   * @return The value of the attribute.
   * @throws habotat.api.InvalidXMLException If a parsing error occurs.
   */

  public static double getFloatAttr(Element elem, String attr,
                                    double defaultValue)
    throws InvalidXMLException
    {
    return(getFloatAttr(elem, attr, defaultValue, false));
    }
  
  /** Get the value of an attribute as a double.
   *
   * @param elem The element.
   * @param attr The attribute name.
   * @param defaultValue The default value to return if the attribute is
   * not present.
   * @param required A flag indicating whether the attribute must be
   * present.
   * @return The value of the attribute.
   * @throws habotat.api.InvalidXMLException If a parsing error occurs, or
   * if <code>required</code> is <b>true</b> and the attribute is not
   * present.
   */

  public static double getFloatAttr(Element elem, String attr,
                                    double defaultValue, boolean required)
    throws InvalidXMLException
    {
    String val = elem.getAttributeValue(attr);
    if(val == null)
      {
      if(required)
        throw(new InvalidXMLException("Missing attribute: " + attr));
      else
        return(defaultValue);
      }

    double value = 0.0;

    try
      {
      value = Double.parseDouble(val);
      }
    catch(NumberFormatException ex)
      {
      throw(new InvalidXMLException("Invalid value: " + val));
      }

    return(value);
    }

  /** Set the value of an attribute as a double.
   *
   * @param elem The element.
   * @param attr The attribute name.
   * @param value The value to set.
   */

  public static void setFloatAttr(Element elem, String attr, double value)
    {
    elem.setAttribute(attr, String.valueOf(value));
    }
  
  /** Get the value of an attribute as a boolean.
   *
   * @param elem The element.
   * @param attr The attribute name.
   * @return The value of the attribute.
   * @throws habotat.api.InvalidXMLException If a parsing error occurs.
   */

  public static boolean getBoolAttr(Element elem, String attr)
    throws InvalidXMLException
    {
    return(getBoolAttr(elem, attr, false, false));
    }
  
  /** Get the value of an attribute as a boolean.
   *
   * @param elem The element.
   * @param attr The attribute name.
   * @param defaultValue The default value to return if the attribute is
   * not present.
   * @return The value of the attribute.
   * @throws habotat.api.InvalidXMLException If a parsing error occurs.
   */

  public static boolean getBoolAttr(Element elem, String attr,
                                    boolean defaultValue)
    throws InvalidXMLException
    {
    return(getBoolAttr(elem, attr, defaultValue, false));
    }
  
  /** Get the value of an attribute as a boolean.
   *
   * @param elem The element.
   * @param attr The attribute name.
   * @param defaultValue The default value to return if the attribute is
   * not present.
   * @param required A flag indicating whether the attribute must be
   * present.
   * @return The value of the attribute.
   * @throws habotat.api.InvalidXMLException If a parsing error occurs, or
   * if <code>required</code> is <b>true</b> and the attribute is not
   * present.
   */

  public static boolean getBoolAttr(Element elem, String attr,
                                    boolean defaultValue, boolean required)
    throws InvalidXMLException
    {
    String val = elem.getAttributeValue(attr);
    if(val == null)
      {
      if(required)
        throw(new InvalidXMLException("Missing attribute: " + attr));
      else
        return(defaultValue);
      }

    boolean value = false;

    try
      {
      value = Boolean.parseBoolean(val);
      }
    catch(NumberFormatException ex)
      {
      throw(new InvalidXMLException("Invalid value: " + val));
      }

    return(value);
    }

  /** Set the value of an attribute as a boolean.
   *
   * @param elem The element.
   * @param attr The attribute name.
   * @param value The value to set.
   */

  public static void setBoolAttr(Element elem, String attr, boolean value)
    {
    elem.setAttribute(attr, String.valueOf(value));
    }

  /** Get the value of an attribute as a string.
   *
   * @param elem The element.
   * @param attr The attribute name.
   * @return The value of the attribute.
   * @throws habotat.api.InvalidXMLException If a parsing error occurs.
   */

  public static String getStringAttr(Element elem, String attr)
    throws InvalidXMLException
    {
    return(getStringAttr(elem, attr, null, false));
    }
  
  /** Get the value of an attribute as a string.
   *
   * @param elem The element.
   * @param attr The attribute name.
   * @param defaultValue The default value to return if the attribute is
   * not present.
   * @return The value of the attribute.
   * @throws habotat.api.InvalidXMLException If a parsing error occurs.
   */

  public static String getStringAttr(Element elem, String attr,
                                     String defaultValue)
    throws InvalidXMLException
    {
    return(getStringAttr(elem, attr, defaultValue, false));
    }
    
  /** Get the value of an attribute as a string.
   *
   * @param elem The element.
   * @param attr The attribute name.
   * @param defaultValue The default value to return if the attribute is
   * not present.
   * @param required A flag indicating whether the attribute must be
   * present.
   * @return The value of the attribute.
   * @throws habotat.api.InvalidXMLException If a parsing error occurs, or
   * if <code>required</code> is <b>true</b> and the attribute is not
   * present.
   */

  public static String getStringAttr(Element elem, String attr,
                                    String defaultValue, boolean required)
    throws InvalidXMLException
    {
    String val = elem.getAttributeValue(attr);
    if(val == null)
      {
      if(required)
        throw(new InvalidXMLException("Missing attribute: " + attr));
      else
        return(defaultValue);
      }

    return(val);
    }

  /** Set the value of an attribute as a string.
   *
   * @param elem The element.
   * @param attr The attribute name.
   * @param value The value to set.
   */
  
  public static void setStringAttr(Element elem, String attr, String value)
    {
    elem.setAttribute(attr, value);
    }

  /** Get a child element of another element.
   *
   * @param elem The parent element.
   * @param name The name of the child element.
   * @return The element, if found, otherwise <b>null</b>.
   * @throws habotat.api.InvalidXMLException
   */

  public static Element getChildElem(Element elem, String name)
    throws InvalidXMLException
    {
    return(getChildElem(elem, name, false));
    }

  /** Get a child element of another element.
   *
   * @param elem The parent element.
   * @param name The name of the child element.
   * @param required A flag indicating whether the child element must be
   * present.
   * @return The element.
   * @throws habotat.api.InvalidXMLException If <code>required</b> is
   * <b>true</b> and the child element is not found.
   */
  
  public static Element getChildElem(Element elem, String name,
                                     boolean required)
    throws InvalidXMLException
    {
    Element child = elem.getChild(name);

    if((child == null) && required)
      throw(new InvalidXMLException("Missing node: " + name));

    return(child);
    }

  /** Get the text content of an element.
   *
   * @param elem The element.
   * @return The text content, with leading and trailing whitespace
   * trimmed.
   * @throws habotat.api.InvalidXMLException
   */

  public static String getTextContent(Element elem)
    throws InvalidXMLException
    {
    return(getTextContent(elem, false));
    }

  /** Get the text content of an element.
   *
   * @param elem The element.
   * @param required A flag indicating whether some non-whitespace text
   * must be present.
   * @return The text content, with leading and trailing whitespace
   * trimmed.
   * @throws habotat.api.InvalidXMLException If <code>required</code> is
   * <b>true</b> and no non-whitespace text is present.
   */
  
  public static String getTextContent(Element elem, boolean required)
    throws InvalidXMLException
    {
    String content = elem.getText().trim();

    if(content.equals(""))
      {
      if(required)
        throw(new InvalidXMLException("No content inside: " + elem.getName()));
      else
        return(null);
      }

    return(content);
    }

  /** Set the text content of an element.
   *
   * @param elem The element.
   * @param text The text to set.
   */

  public static void setTextContent(Element elem, String text)
    {
    elem.setText(text == null ? "" : text);
    }

  }

/* end of source file */
