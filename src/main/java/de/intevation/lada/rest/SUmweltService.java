/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3) 
 * and comes with ABSOLUTELY NO WARRANTY! Check out 
 * the documentation coming with IMIS-Labordaten-Application for details. 
 */
package de.intevation.lada.rest;

import java.util.ArrayList;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import de.intevation.lada.auth.Authentication;
import de.intevation.lada.auth.AuthenticationException;
import de.intevation.lada.data.Repository;
import de.intevation.lada.model.SUmwelt;

/**
 * This class produces a RESTful service to read SUmwelt objects.
 * 
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("/uwb")
@RequestScoped
public class SUmweltService {

    /**
     * The Repository for SUmwelt.
     */
    @Inject
    @Named("readonlyrepository")
    private Repository repository;

    /**
     * The authorization module.
     */
    @Inject
    @Named("ldapauth")
    private Authentication authentication;

    /**
     * The logger for this class.
     */
    @Inject
    private Logger log;

    /**
     * Request all SUmwelt objects.
     *
     * @param headers   The HTTP header containing authorization information.
     * @return JSON Object via Rest service
     */
    @GET
    @Produces("text/json")
    public Response findAll(@Context HttpHeaders headers) {
        try {
            if (authentication.isAuthorizedUser(headers)) {
                return repository.findAll(SUmwelt.class);
            }
            return new Response(false, 699, new ArrayList<SUmwelt>());
        }
        catch(AuthenticationException ae) {
            return new Response(false, 699, new ArrayList<SUmwelt>());
        }
    }

    /**
     * Request a SUmwelt object via its id.
     *
     * @param id        The object id.
     * @param headers   The HTTP header containing authorization information.
     * @return JSON Object via REST service.
     */
    @GET
    @Path("/{id:[0-9][0-9]*}")
    @Produces("text/json")
    public Response findById(
        @PathParam("id") String id,
        @Context HttpHeaders headers
    ) {
        try {
            if (authentication.isAuthorizedUser(headers)) {
                return repository.findById(SUmwelt.class, id);
            }
            return new Response(false, 699, new ArrayList<SUmwelt>());
        }
        catch(AuthenticationException ae) {
            return new Response(false, 699, new ArrayList<SUmwelt>());
        }
    }
}
