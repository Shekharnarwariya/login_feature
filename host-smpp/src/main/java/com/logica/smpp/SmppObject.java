/*
 * Copyright (c) 1996-2001
 * Logica Mobile Networks Limited
 * All rights reserved.
 *
 * This software is distributed under Logica Open Source License Version 1.0
 * ("Licence Agreement"). You shall use it and distribute only in accordance
 * with the terms of the License Agreement.
 *
 */
package com.logica.smpp;


/**
 * Class <code>SmppObject</code> is the root of the SMPP Library
 * class hierarchy. Every class in the library has <code>SmppObject</code>
 * as a superclass except of classes in the <code>com.logica.smpp.debug</code>
 * package, exceptions and classes <code>Data</code>,
 * <code>OutbindListener</code> and <code>OutbindEvent</code>.
 * 
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version 1.2, 26 Sep 2001
 * @see Debug
 * @see Event
 */

/*
  13-07-01 ticp@logica.com added getDebug() and getEvent() static methods
                           for returning current library-wide debug objects
  26-09-01 ticp@logica.com added toolkit wide functional groups constant
                           definitions for debugging
*/
public class SmppObject	implements java.io.Serializable 
{
    
}
