/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer;

import javax.management.modelmbean.InvalidTargetObjectTypeException;

public interface Identifier {

    public Identified find(Object object)
        throws InvalidTargetObjectTypeException;

    public Object getExisting();
}