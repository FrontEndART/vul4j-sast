package de.intevation.lada.importer.laf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.PersistenceException;

import de.intevation.lada.importer.ReportItem;
import de.intevation.lada.model.land.LKommentarM;
import de.intevation.lada.model.land.LKommentarP;
import de.intevation.lada.model.land.LMessung;
import de.intevation.lada.model.land.LMesswert;
import de.intevation.lada.model.land.LOrt;
import de.intevation.lada.model.land.LProbe;
import de.intevation.lada.model.land.MessungTranslation;
import de.intevation.lada.model.stamm.SOrt;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.RepositoryType;

/**
 * Writer to persist new entities in the database.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Stateless
public class LafWriter {

    @Inject
    @RepositoryConfig(type=RepositoryType.RW)
    private Repository repository;

    @Inject
    @AuthorizationConfig(type=AuthorizationType.OPEN_ID)
    private Authorization authorization;

    private List<ReportItem> errors;
    private List<ReportItem> warnings;

    /**
     * Default constructor.
     */
    public LafWriter() {
        errors = new ArrayList<ReportItem>();
        warnings = new ArrayList<ReportItem>();
    }

    /**
     * Write a new {@link LProbe} object to the database using
     * authorization and validation.
     *
     * @param auth      The authentication information.
     * @param probe     The new {@link LProbe} object.
     * @return success
     */
    public boolean writeProbe(UserInfo userInfo, LProbe probe) {
        if (!authorization.isAuthorized(userInfo, probe)) {
            errors.add(new ReportItem("auth", "not authorized", 699));
            return false;
        }
        if (probe.getId() == null) {
            errors.add(new ReportItem("probeId", "missing", 673));
            return false;
        }
        try {
            repository.update(probe, "land");
        }
        catch (PersistenceException e) {
            errors.add(new ReportItem("probe", "writing", 670));
            return false;
        }
        return true;
    }

    /**
     * Write new {@link LMessung} objects to the database using
     * authorization and validation.
     *
     * @param auth      The authentication information.
     * @param messungen The new {@link LMessung} objects.
     * @return success
     */
    public boolean writeMessungen(
        UserInfo userInfo,
        Map<LMessung, MessungTranslation> messungen
    ) {
        for(LMessung messung: messungen.keySet()) {
            repository.create(messung, "land");
            MessungTranslation mt = messungen.get(messung);
            mt.setMessungsId(messung);
            repository.create(mt, "land");
        }
        return true;
    }

    /**
     * Write new {@link SOrt} objects to the database.
     *
     * @param auth      The authentication information.
     * @param orte      List of {@link SOrt} objects.
     * @return success
     */
    public boolean writeOrte(UserInfo userInfo, List<SOrt> orte) {
        for (SOrt ort :orte) {
            //TODO create the SOrt!!!
            //repository.create(ort, "land");
        }
        return true;
    }

    /**
     * Write new {@link LOrt} objects to the database using validation.
     *
     * @param auth  The authentication information.
     * @param orte  List of {@link LOrt} objects.
     * @return success
     */
    public boolean writeLOrte(UserInfo userInfo, List<LOrt> orte) {
        for(LOrt ort: orte) {
            repository.create(ort, "land");
        }
        return true;
    }

    /**
     * Write new {@link LKommentarP} objects to the database.
     *
     * @param auth          The authentication information.
     * @param kommentare    List of {@link LKommentarP} objects.
     * @return success
     */
    public boolean writeProbenKommentare(
        UserInfo userInfo,
        List<LKommentarP> kommentare
    ) {
        for(LKommentarP kommentar: kommentare) {
            repository.create(kommentar, "land");
        }
        return true;
    }

    /**
     * Write new {@link LKommentarM} objects to the database.
     *
     * @param auth          The authentication information.
     * @param kommentare    List of {@link LKommentarM} objects.
     * @return success
     */
    public boolean writeMessungKommentare(
        UserInfo userInfo,
        Map<LMessung, List<LKommentarM>> kommentare
    ) {
        for (LMessung messung : kommentare.keySet()) {
            for (LKommentarM kommentar: kommentare.get(messung)) {
                kommentar.setMessungsId(messung.getId());
                repository.create(kommentar, "land");
            }
        }
        return true;
    }

    /**
     * Write new {@link LMesswert} objects to the database using validation.
     *
     * @param auth      The authentication information.
     * @param werte     List of {@link LMesswert} objects.
     * @return success
     */
    public boolean writeMesswerte(
        UserInfo userInfo,
        Map<LMessung, List<LMesswert>> werte
    ) {
        for (LMessung messung : werte.keySet()) {
            for(LMesswert messwert: werte.get(messung)) {
                messwert.setMessungsId(messung.getId());
                repository.create(messwert, "land");
            }
        }
        return true;
    }

    /**
     * @return the errors
     */
    public List<ReportItem> getErrors() {
        return errors;
    }

    /**
     * @return the warnings
     */
    public List<ReportItem> getWarnings() {
        return warnings;
    }

    /**
     * Reset the errors and warnings.
     */
    public void reset() {
        this.warnings = new ArrayList<ReportItem>();
        this.errors = new ArrayList<ReportItem>();
    }
}
