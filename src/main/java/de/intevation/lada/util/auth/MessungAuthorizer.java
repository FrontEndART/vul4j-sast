/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import java.util.ArrayList;
import java.util.List;

import de.intevation.lada.model.land.LMessung;
import de.intevation.lada.model.land.LProbe;
import de.intevation.lada.model.land.LStatusProtokoll;
import de.intevation.lada.model.stamm.AuthLstUmw;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;

public class MessungAuthorizer extends BaseAuthorizer {

    @Override
    public <T> boolean isAuthorized(
        Object data,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        LMessung messung = (LMessung)data;
        Response response =
            repository.getById(LProbe.class, messung.getProbeId(), "land");
        LProbe probe = (LProbe)response.getData();
        if (method == RequestMethod.PUT ||
                 method == RequestMethod.DELETE) {
            return !this.isMessungReadOnly(messung.getId()) &&
                getAuthorization(userInfo, probe);
        }
        return getAuthorization(userInfo, probe);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Response filter(
        Response data,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        if (data.getData() instanceof List<?>) {
            List<LMessung> messungen = new ArrayList<LMessung>();
            for (LMessung messung :(List<LMessung>)data.getData()) {
                messungen.add(setAuthData(userInfo, messung));
            }
            data.setData(messungen);
        }
        else if (data.getData() instanceof LMessung) {
            LMessung messung = (LMessung)data.getData();
            data.setData(setAuthData(userInfo, messung));
        }
        return data;
    }

    /**
     * Authorize a sinle messung object.
     *
     * @param userInfo  The user information.
     * @param messung     The messung object.
     * @return The messung.
     */
    private LMessung setAuthData(
        UserInfo userInfo,
        LMessung messung
    ) {
        LProbe probe =
            (LProbe)repository.getById(
                LProbe.class, messung.getProbeId(), "land").getData();
        if (!userInfo.getNetzbetreiber().contains(probe.getNetzbetreiberId()) &&
            !userInfo.getFunktionen().contains(3)) {
            messung.setOwner(false);
            messung.setReadonly(true);
            return messung;
        }
        if (userInfo.getMessstellen().contains(probe.getMstId())) {
            messung.setOwner(true);
        }
        else {
            messung.setOwner(false);
        }
        if (messung.getStatus() == null) {
            messung.setReadonly(false);
        }
        else {
            LStatusProtokoll status = repository.getByIdPlain(
                LStatusProtokoll.class,
                messung.getStatus(),
                "land");
            messung.setReadonly(
                status.getStatusWert() != 0 && status.getStatusWert() != 4);
        }

        boolean statusEdit = false;
        if (userInfo.getFunktionen().contains(3)) {
            QueryBuilder<AuthLstUmw> lstFilter = new QueryBuilder<AuthLstUmw>(
                repository.entityManager("stamm"),
                AuthLstUmw.class);
            lstFilter.or("lstId", userInfo.getMessstellen());
            List<AuthLstUmw> lsts =
                repository.filterPlain(lstFilter.getQuery(), "stamm");
            for (int i = 0; i < lsts.size(); i++) {
                if (lsts.get(i).getUmwId().equals(probe.getUmwId())) {
                    statusEdit = true;
                }
            }
        }
        else if (userInfo.getFunktionen().contains(2) &&
            userInfo.getNetzbetreiber().contains(probe.getNetzbetreiberId())) {
            statusEdit = true;
        }
        else if (userInfo.getFunktionen().contains(1) &&
            userInfo.getMessstellen().contains(probe.getMstId())) {
            statusEdit = true;
        }
        messung.setStatusEdit(statusEdit);

        return messung;
    }

}
