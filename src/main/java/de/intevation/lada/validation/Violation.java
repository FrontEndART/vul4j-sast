/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class Violation {

    private MultivaluedMap<String, Integer> warnings;

    private MultivaluedMap<String, Integer> errors;

    public Violation() {
        this.warnings = new MultivaluedHashMap<String, Integer>();
        this.errors = new MultivaluedHashMap<String, Integer>();
    }

    public MultivaluedMap<String, Integer> getWarnings() {
        return this.warnings;
    }

    public MultivaluedMap<String, Integer> getErrors() {
        return this.errors;
    }

    public void addWarning(String key, Integer value) {
        this.warnings.add(key, value);
    }

    public void addError(String key, Integer value) {
        this.errors.add(key, value);
    }

    public void addWarnings(MultivaluedMap<String, Integer> warnings) {
        for (String key: warnings.keySet()) {
            this.warnings.addAll(key, warnings.get(key));
        }
    }

    public void addErrors(MultivaluedMap<String, Integer> errors) {
        for (String key: errors.keySet()) {
            this.errors.addAll(key, errors.get(key));
        }
    }

    public boolean hasWarnings() {
        return this.warnings.size() > 0;
    }

    public boolean hasErrors() {
        return this.errors.size() > 0;
    }
}
