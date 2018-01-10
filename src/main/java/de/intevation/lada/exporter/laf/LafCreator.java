/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.exporter.laf;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import de.intevation.lada.exporter.Creator;
import de.intevation.lada.model.land.KommentarM;
import de.intevation.lada.model.land.KommentarP;
import de.intevation.lada.model.land.Messung;
import de.intevation.lada.model.land.Messwert;
import de.intevation.lada.model.land.Ortszuordnung;
import de.intevation.lada.model.land.Probe;
import de.intevation.lada.model.land.ZusatzWert;
import de.intevation.lada.model.stammdaten.MessEinheit;
import de.intevation.lada.model.stammdaten.MessStelle;
import de.intevation.lada.model.stammdaten.Messgroesse;
import de.intevation.lada.model.stammdaten.Ort;
import de.intevation.lada.model.stammdaten.ProbenZusatz;
import de.intevation.lada.model.stammdaten.Probenart;
import de.intevation.lada.model.stammdaten.ReiProgpunktGruppe;
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.RepositoryType;
import de.intevation.lada.util.data.Strings;
import de.intevation.lada.util.rest.Response;

/**
 * This creator produces a LAF conform String containing all information about
 * a single {@link LProbe} object including subobjects like
 * {@link LMessung}, {@link LMesswert}, {@link LKommentarP}...
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Named("lafcreator")
public class LafCreator
implements Creator
{
    // Some format strings corresponding to LAF notation
    private static final String KEY_FORMAT = "%-30s";
    private static final String DEFAULT_FORMAT = "%s";
    private static final String CN = "\"%s\""; // cn, mcn, scn

    @Inject
    private Logger logger;
    /**
     * The repository used to read data.
     */
    @Inject
    @RepositoryConfig(type=RepositoryType.RO)
    private Repository repository;

    /**
     * Create the LAF conform String.
     *
     * @param probeId   The {@link LProbe} id.
     */
    @Override
    public String create(String probeId) {
        String lafProbe = "%PROBE%\n";
        lafProbe += lafLine("UEBERTRAGUNGSFORMAT", "7", CN);
        lafProbe += lafLine("VERSION", "0084", CN);
        lafProbe += probeToLAF(probeId);
        return lafProbe;
    }

    /**
     * Find the {@link LProbe} object and produce the LAF conform string.
     * @param probeId The {@link LProbe} id.
     * @return LAF conform string.
     */
    private String probeToLAF(String probeId) {
        Response found = repository.getById(Probe.class, Integer.valueOf(probeId), Strings.LAND);
        if (found.getData() == null) {
            return null;
        }
        Probe aProbe = (Probe)found.getData();
        String lafProbe = writeAttributes(aProbe);
        return lafProbe;
    }

    /**
     * Write the attributes and subobjects.
     *
     * @param probe The {@link LProbeInfo} object.
     * @return LAF conform string.
     */
    @SuppressWarnings("unchecked")
    private String writeAttributes(Probe probe) {
        QueryBuilder<KommentarP> kommBuilder =
            new QueryBuilder<KommentarP>(
                repository.entityManager(Strings.LAND), KommentarP.class);
        kommBuilder.and("probeId", probe.getId());
        Response kommentar = repository.filter(kommBuilder.getQuery(), Strings.LAND);
        List<KommentarP> kommentare = (List<KommentarP>)kommentar.getData();

        QueryBuilder<Probenart> builder =
            new QueryBuilder<Probenart>(
                repository.entityManager(Strings.STAMM),
                Probenart.class);
        builder.and("id", probe.getProbenartId());
        List<Probenart> probenarten =
            (List<Probenart>)repository.filter(
                builder.getQuery(),
                Strings.STAMM).getData();
        String probenart = probenarten.get(0).getProbenart();

        MessStelle messstelle =
            repository.getByIdPlain(MessStelle.class, probe.getMstId(), Strings.STAMM);

        QueryBuilder<ZusatzWert> zusatzBuilder =
            new QueryBuilder<ZusatzWert>(
                repository.entityManager(Strings.LAND), ZusatzWert.class);
        zusatzBuilder.and("probeId", probe.getId());
        Response zusatz = repository.filter(zusatzBuilder.getQuery(), Strings.LAND);
        List<ZusatzWert> zusatzwerte = (List<ZusatzWert>)zusatz.getData();

        String laf = "";
        laf += probe.getDatenbasisId() == null ?
            "": lafLine("DATENBASIS_S",
                String.format("%02d", probe.getDatenbasisId()));
        laf += messstelle == null ?
            "" : lafLine("NETZKENNUNG", messstelle.getNetzbetreiberId(), CN);
        laf += probe.getMstId() == null ?
            "" : lafLine("MESSSTELLE", probe.getMstId(), CN);
        laf += probe.getLaborMstId() == null ?
            "" : lafLine("MESSLABOR", probe.getLaborMstId(), CN);
        laf += lafLine("PROBE_ID", probe.getIdAlt(), CN);
        laf += probe.getHauptprobenNr() == null ?
            "" : lafLine("HAUPTPROBENNUMMER", probe.getHauptprobenNr(), CN);
        laf += probe.getBaId() == null ?
            "" : lafLine("MESSPROGRAMM_S", probe.getBaId(), CN);
        laf += probe.getProbenartId() == null ?
            "" : lafLine("PROBENART", probenart, CN);
        laf += probe.getSolldatumBeginn() == null ?
            "" : lafLine("SOLL_DATUM_UHRZEIT_A",
                toUTCString(probe.getSolldatumBeginn()));
        laf += probe.getSolldatumEnde() == null ?
            "" : lafLine("SOLL_DATUM_UHRZEIT_E",
                toUTCString(probe.getSolldatumEnde()));
        laf += probe.getProbeentnahmeBeginn() == null ?
            "" : lafLine("PROBENAHME_DATUM_UHRZEIT_A",
                toUTCString(probe.getProbeentnahmeBeginn()));
        laf += probe.getProbeentnahmeEnde() == null ?
            "" : lafLine("PROBENAHME_DATUM_UHRZEIT_E",
                toUTCString(probe.getProbeentnahmeEnde()));
        laf += probe.getUmwId() == null ?
            "" : lafLine("UMWELTBEREICH_S", probe.getUmwId(), CN);
        laf += probe.getMediaDesk() == null ?
            "" : lafLine("DESKRIPTOREN",
                probe.getMediaDesk().replaceAll(" ", "").substring(2), CN);
        laf += probe.getTest() == Boolean.TRUE ?
            lafLine("TESTDATEN", "1") : lafLine("TESTDATEN", "0");
        if (probe.getReiProgpunktGrpId() != null) {
            ReiProgpunktGruppe rpg = repository.getByIdPlain(
                ReiProgpunktGruppe.class, probe.getReiProgpunktGrpId(), "stamm");
            laf += lafLine("REI_PROGRAMMPUNKTGRUPPE", rpg.getReiProgPunktGruppe(), CN);
        }
        laf += lafLine("ZEITBASIS_S", "2");
        laf += writeOrt(probe);
        for (ZusatzWert zw : zusatzwerte) {
            laf += writeZusatzwert(zw);
        }
        for (KommentarP kp : kommentare) {
            laf += writeKommentar(kp);
        }
        laf += writeMessung(probe);
        return laf;
    }

    /**
     * Write {@link LZusatzWert} attributes.
     *
     * @param zw    The {@link LZusatzWert}.
     * @return Single LAF line.
     */
    @SuppressWarnings("unchecked")
    private String writeZusatzwert(ZusatzWert zw) {
        QueryBuilder<ProbenZusatz> builder =
            new QueryBuilder<ProbenZusatz>(
                repository.entityManager(Strings.STAMM),
                ProbenZusatz.class);
        builder.and("id", zw.getPzsId());
        List<ProbenZusatz> zusatz =
            (List<ProbenZusatz>)repository.filter(
                builder.getQuery(),
                Strings.STAMM).getData();

        String value = "\"" + zusatz.get(0).getId() + "\"";
        value += " " + zw.getMesswertPzs();
        value += " " + zusatz.get(0).getMessEinheitId();
        value += " " + zw.getMessfehler();
        return lafLine("PZB_S", value);
    }

    /**
     * Write {@link LOrt} attributes.
     *
     * @param probe The {@link LProbeInfo} object.
     * @return LAF conform string
     */
    @SuppressWarnings("unchecked")
    private String writeOrt(Probe probe) {
        QueryBuilder<Ortszuordnung> builder =
            new QueryBuilder<Ortszuordnung>(
                repository.entityManager(Strings.LAND),
                Ortszuordnung.class);
        builder.and("probeId", probe.getId());
        Response objects = repository.filter(builder.getQuery(), Strings.LAND);
        List<Ortszuordnung> orte =
            (List<Ortszuordnung>)objects.getData();

        String laf = "";
        for(Ortszuordnung o : orte) {
            String type = "";
            if ("E".equals(o.getOrtszuordnungTyp())) {
                type = "P_";
            }
            else if ("U".equals(o.getOrtszuordnungTyp()) ||
                "R".equals(o.getOrtszuordnungTyp())) {
                type = "U_";
                laf += "%URSPRUNGSORT%\n";
            }
            else {
                continue;
            }
            if (o.getOrtszusatztext() != null &&
                o.getOrtszusatztext().length() > 0) {
                laf += lafLine(type + "ORTS_ZUSATZTEXT",
                    o.getOrtszusatztext(), CN);
            }
            QueryBuilder<Ort> oBuilder =
                new QueryBuilder<Ort>(
                    repository.entityManager(Strings.STAMM),
                    Ort.class);
            oBuilder.and("id", o.getOrtId());
            List<Ort> sOrte=
                (List<Ort>)repository.filter(
                    oBuilder.getQuery(),
                    Strings.STAMM).getData();

            if (sOrte.get(0).getStaatId() != null) {
                laf += lafLine(type + "HERKUNFTSLAND_S",
                    String.format("%08d", sOrte.get(0).getStaatId()));
            }

            if (sOrte.get(0).getGemId() != null &&
                sOrte.get(0).getGemId().length() > 0) {
                laf += lafLine(type + "GEMEINDESCHLUESSEL",
                    sOrte.get(0).getGemId());
            }

            String koord = String.format("%02d", sOrte.get(0).getKdaId());
            koord += " \"";
            koord += sOrte.get(0).getKoordXExtern() + "\" \"";
            koord += sOrte.get(0).getKoordYExtern() + "\"";
            laf += lafLine(type + "KOORDINATEN_S", koord);

            if (probe.getReiProgpunktGrpId() != null) {
                lafLine(type + "ORTS_ZUSATZCODE",
                    sOrte.get(0).getKtaGruppeId() + sOrte.get(0).getGemId());
            }
            else if (sOrte.get(0).getOzId() != null &&
                sOrte.get(0).getOzId().length() > 0) {
                laf += lafLine(type + "ORTS_ZUSATZCODE",
                    sOrte.get(0).getOzId(), CN);
            }
            if (sOrte.get(0).getHoeheUeberNn() != null) {
                laf += lafLine(type + "HOEHE_NN",
                    String.format("%f", sOrte.get(0).getHoeheUeberNn()));
            }
        }
        return laf;
    }

    /**
     * Write {@link LKommentarP} attributes.
     *
     * @param kp    The {@link LKommentarP} object.
     * @return Single LAF line.
     */
    private String writeKommentar(KommentarP kp) {
        String value = "\"" + kp.getMstId() + "\" " +
            toUTCString(kp.getDatum()) + " " +
            "\"" + kp.getText() + "\"";
        return lafLine("PROBENKOMMENTAR", value);
    }

    /**
     * Write {@link LMessung} attributes.
     *
     * @param probe The {@link LProbeInfo} object.
     * @return LAF conform string.
     */
    @SuppressWarnings("unchecked")
    private String writeMessung(Probe probe) {
        // Get all messungen
        QueryBuilder<Messung> builder =
            new QueryBuilder<Messung>(
                repository.entityManager(Strings.LAND),
                Messung.class);
        builder.and("probeId", probe.getId());
        Response objects = repository.filter(builder.getQuery(), Strings.LAND);
        List<Messung> mess = (List<Messung>)objects.getData();

        String laf = "";
        for(Messung m : mess) {
            laf += "%MESSUNG%\n";
            QueryBuilder<Messwert> wertBuilder =
                new QueryBuilder<Messwert>(
                    repository.entityManager(Strings.LAND), Messwert.class);
            wertBuilder.and("messungsId", m.getId());
            Response messw = repository.filter(wertBuilder.getQuery(), Strings.LAND);
            List<Messwert> werte = (List<Messwert>)messw.getData();
            QueryBuilder<KommentarM> kommBuilder =
                new QueryBuilder<KommentarM>(
                    repository.entityManager(Strings.LAND), KommentarM.class);
            kommBuilder.and("messungsId", m.getId());
            Response kommentar = repository.filter(kommBuilder.getQuery(), Strings.LAND);
            List<KommentarM> kommentare = (List<KommentarM>)kommentar.getData();
            laf += lafLine("MESSUNGS_ID", m.getIdAlt().toString());
            laf += lafLine("NEBENPROBENNUMMER", m.getNebenprobenNr(), CN);
            laf += m.getMesszeitpunkt() == null ?
                "" : lafLine(
                    "MESS_DATUM_UHRZEIT",
                    toUTCString(m.getMesszeitpunkt()));
            laf += m.getMessdauer() == null ?
                "" : lafLine("MESSZEIT_SEKUNDEN", m.getMessdauer().toString());
            laf += m.getMmtId() == null ?
                "" : lafLine("MESSMETHODE_S", m.getMmtId(), CN);
            laf += lafLine("ERFASSUNG_ABGESCHLOSSEN", (m.getFertig() ? "1" : "0"));
            for (Messwert mw : werte) {
                laf += writeMesswert(mw);
            }
            for (KommentarM mk: kommentare) {
                laf += writeKommentar(mk);
            }
        }
        return laf;
    }

    /**
     * Write {@link LKommentarM} attributes.
     * @param mk    The {@link LKommentarM} object.
     * @return Single LAF line.
     */
    private String writeKommentar(KommentarM mk) {
        String value = "\"" + mk.getMstId() + "\" " +
            toUTCString(mk.getDatum()) + " " +
            "\"" + mk.getText() + "\"";
        return lafLine("KOMMENTAR", value);
    }

    /**
     * Write {@link LMesswert} attributes.
     * @param mw    The {@link LMesswert} object.
     * @return Single LAF line.
     */
    @SuppressWarnings("unchecked")
    private String writeMesswert(Messwert mw) {
        QueryBuilder<Messgroesse> builder =
            new QueryBuilder<Messgroesse>(
                repository.entityManager(Strings.STAMM),
                Messgroesse.class);
        builder.and("id", mw.getMessgroesseId());
        List<Messgroesse> groessen =
            (List<Messgroesse>)repository.filter(
                builder.getQuery(),
                Strings.STAMM).getData();

        QueryBuilder<MessEinheit> eBuilder =
            new QueryBuilder<MessEinheit>(
                repository.entityManager(Strings.STAMM),
                MessEinheit.class);
        eBuilder.and("id", mw.getMehId());
        List<MessEinheit> einheiten =
            (List<MessEinheit>)repository.filter(
                eBuilder.getQuery(),
                Strings.STAMM).getData();

        String tag = "MESSWERT";
        String value = "\"" + groessen.get(0).getMessgroesse() + "\"";
        value += " ";
        value += mw.getMesswertNwg() == null ? " " : mw.getMesswertNwg();
        value += mw.getMesswert();
        value += " \"" + einheiten.get(0).getEinheit() + "\"";
        value += mw.getMessfehler() == null ? " 0.0" : " " + mw.getMessfehler();
        if (mw.getGrenzwertueberschreitung() == null ||
            !mw.getGrenzwertueberschreitung()) {
            if (mw.getNwgZuMesswert() != null) {
                tag += "_NWG";
                value += " " + mw.getNwgZuMesswert();
            }
        }
        else {
            tag += "_NWG_G";
            value += " " + (mw.getNwgZuMesswert() == null ? "0.0": mw.getNwgZuMesswert());
            value += " " + (mw.getGrenzwertueberschreitung() == null ? " N" :
                mw.getGrenzwertueberschreitung() ? " J" : " N");
        }
        return lafLine(tag, value);
    }

    /**
     * Write a single LAF conform line from key and value.
     *
     * @param key   The key.
     * @param value The value.
     * @return LAF conform line.
     */
    private String lafLine(String key, String value) {
        return lafLine(key, value, DEFAULT_FORMAT);
    }

    /**
     * Write a single LAF conform line from key and value.
     *
     * @param key    The key.
     * @param value  The value.
     * @param format A format string for the value
     * @return LAF conform line.
     */
    private String lafLine(String key, Object value, String format) {
        return String.format(KEY_FORMAT, key)
            + String.format(format, value)
            + "\n";
    }

    private String toUTCString(Timestamp timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HHmm");
        return formatter.format(timestamp.toInstant().atZone(ZoneOffset.UTC));
    }
}
