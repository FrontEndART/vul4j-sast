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

import de.intevation.lada.model.stamm.Auth;

/**
 * Container for user specific information.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class UserInfo {
    private String name;
    private Integer userId;
    private List<String> messstellen;
    private List<String> netzbetreiber;
    private List<String> roles;
    private List<Integer> funktionen;
    private List<Auth> auth;
    private Integer statusRole;

    public UserInfo() {
        messstellen = new ArrayList<String>();
        netzbetreiber = new ArrayList<String>();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the userId
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * @return the messstellen
     */
    public List<String> getMessstellen() {
        List<String> ret = new ArrayList<String>();
        for (Auth a : auth) {
            if (a.getMstId() != null) {
                ret.add(a.getMstId());
            }
        }
        return ret;
    }

    /**
     * @return the netzbetreiber
     */
    public List<String> getNetzbetreiber() {
        List<String> ret = new ArrayList<String>();
        for (Auth a : auth) {
            if (a.getNetzbetreiberId() != null) {
                ret.add(a.getNetzbetreiberId());
            }
        }
        return ret;
    }

    public List<Integer> getFunktionen() {
        List<Integer> ret = new ArrayList<Integer>();
        for (Auth a : auth) {
            if (a.getFunktionId() != null) {
                ret.add(a.getFunktionId());
            }
        }
        return ret;
    }

    /**
     * @return the funktionen
     */
    public List<Integer> getFunktionenForMst(String mstId) {
        List<Integer> ret = new ArrayList<Integer>();
        for (Auth a : auth) {
            if (a.getMstId() != null && a.getMstId().equals(mstId)) {
                ret.add(a.getFunktionId());
            }
        }
        return ret;
    }

    /**
     * @return the funktionen
     */
    public List<Integer> getFunktionenForNetzbetreiber(String nId) {
        List<Integer> ret = new ArrayList<Integer>();
        for (Auth a : auth) {
            if (a.getNetzbetreiberId() != null &&
                a.getNetzbetreiberId().equals(nId)) {
                ret.add(a.getFunktionId());
            }
        }
        return ret;
    }

    /**
     * @return the funktionen
     */
    public List<String> getFunktionenAsString() {
        List<String> retVal = new ArrayList<String>();
        for (int i = 0; i < this.funktionen.size(); i++) {
            retVal.add(this.funktionen.get(i).toString());
        }
        return retVal;
    }

    public List<String> getRoles() {
        List<String> ret = new ArrayList<String>();
        for (Auth a : auth) {
            if (a.getLdapGroup() != null) {
                ret.add(a.getLdapGroup());
            }
        }
        return ret;
    }

    /**
     * @return the statusRole
     */
    public Integer getStatusRole() {
        return statusRole;
    }

    /**
     * @param statusRole the statusRole to set
     */
    public void setStatusRole(Integer statusRole) {
        this.statusRole = statusRole;
    }

    public void setAuth(List<Auth> auth) {
        this.auth = auth;
    }
}
