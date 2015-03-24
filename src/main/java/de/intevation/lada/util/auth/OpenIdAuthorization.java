package de.intevation.lada.util.auth;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ejb.Stateful;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import de.intevation.lada.model.land.LMessung;
import de.intevation.lada.model.land.LProbe;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.RepositoryType;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;

@Stateful
@AuthorizationConfig(type=AuthorizationType.OPEN_ID)
public class OpenIdAuthorization implements Authorization {

    @Inject
    @RepositoryConfig(type=RepositoryType.RO)
    private Repository repository;

    @Override
    public UserInfo getInfo(Object source) {
        if (source instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest)source;
            String roleString =
                request.getAttribute("lada.user.roles").toString();
            String[] roles = roleString.split(",");
            UserInfo info = getGroupsFromDB(roleString);
            info.setRoles(new ArrayList<String>(Arrays.asList(roles)));
            info.setName(request.getAttribute("lada.user.name").toString());
            return info;
        }
        return null;
    }

    @Override
    public <T> Response filter(Object source, Response data, Class<T> clazz) {
        UserInfo userInfo = this.getInfo(source);
        if (userInfo == null) {
            return data;
        }
        if (clazz == LProbe.class) {
            return this.authorizeProbe(userInfo, data);
        }
        Method[] methods = clazz.getMethods();
        for (Method method: methods) {
            if (method.getName().equals("getProbeId")) {
                return this.authorizeWithProbeId(userInfo, data, clazz);
            }
            if (method.getName().equals("getMessungsId")) {
                return this.authorizeWithMessungsId(userInfo, data, clazz);
            }
        }
        return data;
    }

    @Override
    public <T> boolean isAuthorized(
        Object source,
        Object data,
        RequestMethod method,
        Class<T> clazz
    ) {
        UserInfo userInfo = this.getInfo(source);
        if (userInfo == null) {
            return false;
        }
        if (clazz == LProbe.class) {
            LProbe probe = (LProbe)data;
            if (method == RequestMethod.POST) {
                return getAuthorization(userInfo, probe);
            }
            else if (method == RequestMethod.PUT ||
                     method == RequestMethod.DELETE) {
                return isReadOnly(probe.getId());
            }
            else {
                return false;
            }
        }
        else if (clazz == LMessung.class) {
            LMessung messung = (LMessung)data;
            Response response =
                repository.getById(LProbe.class, messung.getProbeId(), "land");
            LProbe probe = (LProbe)response.getData();
            if (method == RequestMethod.POST) {
                return getAuthorization(userInfo, probe);
            }
            else if (method == RequestMethod.PUT ||
                     method == RequestMethod.DELETE) {
                return isReadOnly(probe.getId());
            }
        }
        else {
            Method[] methods = clazz.getMethods();
            for (Method m: methods) {
                if (m.getName().equals("getProbeId")) {
                    Integer id;
                    try {
                        id = (Integer) m.invoke(data);
                    } catch (IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException e) {
                        return false;
                    }
                    Response response =
                        repository.getById(LProbe.class, id, "land");
                    LProbe probe = (LProbe)response.getData();
                    return isReadOnly(id) && getAuthorization(userInfo, probe);

                }
                if (m.getName().equals("getMessungsId")) {
                    Integer id;
                    try {
                        id = (Integer) m.invoke(data);
                    } catch (IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException e) {
                        return false;
                    }
                    Response mResponse =
                        repository.getById(LMessung.class, id, "land");
                    LMessung messung = (LMessung)mResponse.getData();
                    Response pResponse =
                        repository.getById(LProbe.class, messung.getProbeId(), "land");
                    LProbe probe = (LProbe)pResponse.getData();
                    return isReadOnly(probe.getId()) && getAuthorization(userInfo, probe);
                }
            }
        }
        return true;
    }

    private boolean getAuthorization(UserInfo userInfo, LProbe probe) {
        if (userInfo.getMessstellen().contains(probe.getMstId())) {
            return true;
        }
        else {
            return false;
        }
    }

    private UserInfo getGroupsFromDB(String roles) {
        String nativeQuery = "select * from stammdaten.auth where ldap_group in ";
        roles = roles.replaceAll(",", "', '");
        nativeQuery += "('" + roles + "')";
        Query query = repository.entityManager("land").createNativeQuery(nativeQuery);
        @SuppressWarnings("unchecked")
        List<Object[]> result = query.getResultList();
        List<String> netzbetreiber = new ArrayList<String>();
        List<String> messstellen = new ArrayList<String>();
        for (Object[] row: result) {
            if (row[2] != null) {
                netzbetreiber.add(row[2].toString());
            }
            if (row[3] != null) {
                messstellen.add(row[3].toString());
            }
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setNetzbetreiber(netzbetreiber);
        userInfo.setMessstellen(messstellen);
        return userInfo;
    }


    @SuppressWarnings("unchecked")
    private <T> Response authorizeWithMessungsId(UserInfo userInfo, Response data, Class<T> clazz) {
        if (data.getData() instanceof List<?>) {
            List<Object> objects = new ArrayList<Object>();
            for (Object object :(List<Object>)data.getData()) {
                objects.add(authorizeSingleWithMessungsId(userInfo, object, clazz));
            }
            data.setData(objects);
        }
        else {
            Object object = data.getData();
            data.setData(authorizeSingleWithMessungsId(userInfo, object, clazz));
        }
        return data;
    }

    @SuppressWarnings("unchecked")
    private <T> Response authorizeWithProbeId(UserInfo userInfo, Response data, Class<T> clazz) {
        if (data.getData() instanceof List<?>) {
            List<Object> objects = new ArrayList<Object>();
            for (Object object :(List<Object>)data.getData()) {
                objects.add(authorizeSingleWithProbeId(userInfo, object, clazz));
            }
            data.setData(objects);
        }
        else {
            Object object = data.getData();
            data.setData(authorizeSingleWithProbeId(userInfo, object, clazz));
        }
        return data;
    }

    private <T> Object authorizeSingleWithMessungsId(UserInfo userInfo, Object data, Class<T> clazz) {
        try {
            Method getMessungsId = clazz.getMethod("getMessungsId");
            Integer id = (Integer)getMessungsId.invoke(data);
            LMessung messung = (LMessung) repository.getById(LMessung.class, id, "land").getData();
            LProbe probe = (LProbe) repository.getById(LProbe.class, messung.getProbeId(), "land").getData();

            boolean readOnly = true;
            boolean owner = false;
            if (!userInfo.getNetzbetreiber().contains(probe.getNetzbetreiberId())) {
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
                readOnly = this.isReadOnly(probe.getId());
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

    private <T> Object authorizeSingleWithProbeId(UserInfo userInfo, Object data, Class<T> clazz) {
        try {
            Method getProbeId = clazz.getMethod("getProbeId");
            Integer id = null;
            if (getProbeId != null) {
                id = (Integer) getProbeId.invoke(data);
            }
            else {
                return null;
            }
            LProbe probe = (LProbe)repository.getById(LProbe.class, id, "land").getData();

            boolean readOnly = true;
            boolean owner = false;
            if (!userInfo.getNetzbetreiber().contains(probe.getNetzbetreiberId())) {
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
                readOnly = this.isReadOnly(id);
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

    @SuppressWarnings("unchecked")
    private Response authorizeProbe(UserInfo userInfo, Response data) {
        if (data.getData() instanceof List<?>) {
            List<LProbe> proben = new ArrayList<LProbe>();
            for (LProbe probe :(List<LProbe>)data.getData()) {
                proben.add(authorizeSingleProbe(userInfo, probe));
            }
            data.setData(proben);
        }
        else if (data.getData() instanceof LProbe) {
            LProbe probe = (LProbe)data.getData();
            data.setData(authorizeSingleProbe(userInfo, probe));
        }
        return data;
    }

    private LProbe authorizeSingleProbe(UserInfo userInfo, LProbe probe) {
        if (!userInfo.getNetzbetreiber().contains(probe.getNetzbetreiberId())) {
            probe.setIsOwner(false);
            probe.setReadonly(true);
            return probe;
        }
        if (userInfo.getMessstellen().contains(probe.getMstId())) {
            probe.setIsOwner(true);
        }
        else {
            probe.setIsOwner(false);
        }
        probe.setReadonly(this.isReadOnly(probe.getId()));
        return probe;
    }

    @Override
    public boolean isReadOnly(Integer probeId) {
        EntityManager manager = repository.entityManager("land");
        QueryBuilder<LMessung> builder =
            new QueryBuilder<LMessung>(
                manager,
                LMessung.class);
        builder.and("probeId", probeId);
        builder.and("fertig", true);
        Response response = repository.filter(builder.getQuery(), "land");
        @SuppressWarnings("unchecked")
        List<LMessung> messungen = (List<LMessung>) response.getData();
        if (messungen.isEmpty()) {
            return false;
        }
        return true;
    }
}
