package de.intevation.lada.data;

import java.util.List;
import java.util.Map;

import javax.ejb.EJBTransactionRolledbackException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.TransactionRequiredException;
import javax.persistence.criteria.CriteriaQuery;

import de.intevation.lada.manage.Manager;
import de.intevation.lada.model.LProbe;
import de.intevation.lada.model.LProbeInfo;
import de.intevation.lada.rest.Response;
import de.intevation.lada.validation.ValidationException;
import de.intevation.lada.validation.Validator;

/**
 * This Container is an interface to read, write and update LProbe objects.
 * 
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@ApplicationScoped
@Named("lproberepository")
public class LProbeRepository implements Repository{
    /**
     * The entitymanager managing the data.
     */
    @Inject
    private EntityManager em;

    /**
     * The data manager providing database operations.
     */
    @Inject
    @Named("datamanager")
    private Manager manager;

    /**
     * The validator used for LProbe objects.
     */
    @Inject
    @Named("lprobevalidator")
    private Validator validator;

    public EntityManager getEntityManager() {
        return this.em;
    }

    /**
     * Filter object list by the given criteria.
     *
     * @param criteria  The query filter
     * @return Response object.
     */
    public <T> Response filter(CriteriaQuery<T> filter) {
        List<T> result = em.createQuery(filter).getResultList();
        return new Response(true, 200, result);
    }

    /**
     * Get all objects.
     *
     * @param clazz The object type. (unused)
     * @return Response object.
     */
    public <T> Response findAll(Class<T> clazz) {
        QueryBuilder<LProbeInfo> builder =
            new QueryBuilder<LProbeInfo>(this.getEntityManager(), LProbeInfo.class);
        builder.distinct();
        return filter(builder.getQuery());
    }

    /**
     * Find object identified by its id.
     *
     * @param clazz The object type.(unused)
     * @param id    The object id.
     * @return Response object.
     */
    public <T> Response findById(Class<T> clazz, String id) {
        QueryBuilder<LProbeInfo> builder =
            new QueryBuilder<LProbeInfo>(this.getEntityManager(), LProbeInfo.class);
        builder.and("probeId", id);
        builder.distinct();
        return filter(builder.getQuery());
    }

    /**
     * Validate and persist a new LProbe object.
     *
     * @param probe The new LProbe object
     * @return Response.
     */
    public Response create(Object object) {
        if (!(object instanceof LProbe)) {
            return new Response(false, 602, object);
        }
        LProbe probe = (LProbe)object;
        Response response = new Response(true, 200, probe);
        // Try to save the new LProbe.
        try {
            Map<String, Integer> warnings = validator.validate(probe, false);
            manager.create(probe);
            response.setWarnings(warnings);
            return response;
        }
        catch (EntityExistsException eee) {
            response.setSuccess(false);
            response.setMessage(601);
        }
        catch (IllegalArgumentException iae) {
            response.setSuccess(false);
            response.setMessage(602);
        }
        catch (TransactionRequiredException tre) {
            response.setSuccess(false);
            response.setMessage(603);
        }
        catch (ValidationException ve) {
            response.setSuccess(false);
            response.setMessage(604);
            response.setErrors(ve.getErrors());
            response.setWarnings(ve.getWarnings());
        }
        catch (EJBTransactionRolledbackException te) {
            response.setSuccess(false);
            response.setMessage(604);
        }
        return response;
    }

    /**
     * Validate and update a LProbe object.
     *
     * @param object    The object to update.
     * @return Response object.
     */
    public Response update(Object object) {
        if (!(object instanceof LProbe)) {
            return new Response(false, 602, object);
        }
        LProbe probe = (LProbe)object;
        Response response = new Response(true, 200, probe);
        try {
            Map<String, Integer> warnings = validator.validate(probe, true);
            manager.update(probe);
            response.setWarnings(warnings);
            return response;
        }
        catch (EntityExistsException eee) {
            response.setSuccess(false);
            response.setMessage(601);
        }
        catch (IllegalArgumentException iae) {
            response.setSuccess(false);
            response.setMessage(602);
        }
        catch (TransactionRequiredException tre) {
            response.setSuccess(false);
            response.setMessage(603);
        }
        catch (ValidationException ve) {
            response.setSuccess(false);
            response.setMessage(604);
            response.setErrors(ve.getErrors());
            response.setWarnings(ve.getWarnings());
        }
        catch (EJBTransactionRolledbackException te) {
            response.setSuccess(false);
            response.setMessage(604);
        }
        return response;
    }

    /**
     * This class does not support this operation.
     *
     * @param object
     */
    public Response delete(Object object) {
        return null;
    }
}
