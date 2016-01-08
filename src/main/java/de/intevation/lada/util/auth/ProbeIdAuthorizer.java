package de.intevation.lada.util.auth;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.intevation.lada.model.land.LProbe;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;

public class ProbeIdAuthorizer extends BaseAuthorizer {

    @Override
    public <T> boolean isAuthorized(
        Object data,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        Method m;
        try {
            m = clazz.getMethod("getProbeId");
        } catch (NoSuchMethodException | SecurityException e1) {
            return false;
        }
        Integer id;
        try {
            id = (Integer) m.invoke(data);
        } catch (IllegalAccessException |
            IllegalArgumentException |
            InvocationTargetException e
        ) {
            return false;
        }
        LProbe probe =
            repository.getByIdPlain(LProbe.class, id, "land");
        return !isProbeReadOnly(id) && getAuthorization(userInfo, probe);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Response filter(
        Response data,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        if (data.getData() instanceof List<?>) {
            List<Object> objects = new ArrayList<Object>();
            for (Object object :(List<Object>)data.getData()) {
                objects.add(setAuthData(userInfo, object, clazz));
            }
            data.setData(objects);
        }
        else {
            Object object = data.getData();
            data.setData(setAuthData(userInfo, object, clazz));
        }
        return data;
    }
    /**
     * Authorize a single data object that has a probeId Attribute.
     *
     * @param userInfo  The user information.
     * @param data      The Response object containing the data.
     * @param clazz     The data object class.
     * @return A Response object containing the data.
     */
    private <T> Object setAuthData(
        UserInfo userInfo,
        Object data,
        Class<T> clazz
    ) {
        try {
            Method getProbeId = clazz.getMethod("getProbeId");
            Integer id = null;
            if (getProbeId != null) {
                id = (Integer) getProbeId.invoke(data);
            }
            else {
                return null;
            }
            LProbe probe =
                (LProbe)repository.getById(LProbe.class, id, "land").getData();

            boolean readOnly = true;
            boolean owner = false;
            if (!userInfo.getNetzbetreiber().contains(
                    probe.getNetzbetreiberId())) {
                owner = false;
                readOnly = true;
            }
            else {
                if (userInfo.getMessstellen().contains(probe.getMstId())) {
                    owner = true;
                }
                else {
                    owner = false;
                }
                readOnly = this.isProbeReadOnly(id);
            }

            Method setOwner = clazz.getMethod("setOwner", boolean.class);
            Method setReadonly = clazz.getMethod("setReadonly", boolean.class);
            setOwner.invoke(data, owner);
            setReadonly.invoke(data, readOnly);
        } catch (NoSuchMethodException | SecurityException
            | IllegalAccessException | IllegalArgumentException
            | InvocationTargetException e) {
            return null;
        }
        return data;
    }
}
