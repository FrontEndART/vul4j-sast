/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.land;

import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * The persistent class for the messwert database table.
 */
@Entity
@Table(name="messwert")
public class LMesswert extends de.intevation.lada.model.Messwert {
    private static final long serialVersionUID = 1L;

}
