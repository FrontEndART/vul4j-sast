/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3) 
 * and comes with ABSOLUTELY NO WARRANTY! Check out 
 * the documentation coming with IMIS-Labordaten-Application for details. 
 */
package de.intevation.lada.auth;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import de.intevation.lada.data.QueryBuilder;
import de.intevation.lada.data.Repository;
import de.intevation.lada.model.LMessung;
import de.intevation.lada.rest.Response;

/**
* This implementation of the authorization interface handles access to data
* objects.
*
* @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
*/
@ApplicationScoped
@Named("dataauthorization")
public class DataAuthorization
implements Authorization
{
    @Inject
    @Named("readonlyrepository")
    private Repository repository;

    /**
     * Determine if the LProbe identified by probeId is writable for the user.
     *
     * @param probeId   The probe id.
     */
    public boolean isReadOnly(String probeId) {
        QueryBuilder<LMessung> builder =
            new QueryBuilder<LMessung>(
                repository.getEntityManager(),
                LMessung.class);
        builder.and("probeId", probeId);
        Response response = repository.filter(builder.getQuery());
        @SuppressWarnings("unchecked")
        List<LMessung> messungen = (List<LMessung>) response.getData();
        if (messungen.isEmpty()) {
            return false;
        }
        for(LMessung messung : messungen) {
            if (messung.isFertig()) {
                return true;
            }
        }
        return false;
    }
}
