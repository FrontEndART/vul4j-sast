package de.intevation.lada.importer.laf;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import de.intevation.lada.importer.ReportItem;
import de.intevation.lada.model.land.LKommentarM;
import de.intevation.lada.model.land.LKommentarP;
import de.intevation.lada.model.land.LMessung;
import de.intevation.lada.model.land.LMesswert;
import de.intevation.lada.model.land.LProbe;
import de.intevation.lada.model.land.LZusatzWert;
import de.intevation.lada.model.land.MessungTranslation;
import de.intevation.lada.model.land.ProbeTranslation;
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.RepositoryType;
import de.intevation.lada.util.rest.Response;

/**
 * The AttributeMapper is used to set object attributes via string based
 * key value pairs. The key represents a member of an entity object.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Stateless
public class AttributeMapper
{

    @Inject
    private Logger logger;

    @Inject
    @RepositoryConfig(type=RepositoryType.RO)
    private Repository repository;

    private List<ReportItem> warnings;
    private List<ReportItem> errors;

    /**
     * Default constructor to create a new AttributeMapper object.
     */
    public AttributeMapper() {
        this.warnings = new ArrayList<ReportItem>();
        this.errors = new ArrayList<ReportItem>();
    }

    /**
     * Add an attribute to the given LProbe object.
     *
     * @param key       The key mapping to a object member.
     * @param value     The value to set.
     * @param probe     The entity object.
     * @return The updated entity object.
     */
    public LProbe addAttribute(String key, Object value, LProbe probe) {
        DateFormat format = new SimpleDateFormat("yyyyMMdd HHmm");
        if ("datenbasis_s".equals(key) && probe.getDatenbasisId() == null) {
            Integer v = Integer.valueOf(value.toString());
            probe.setDatenbasisId(v);
        }
        else if ("datenbasis_s".equals(key) && probe.getDatenbasisId() != null){
            this.warnings.add(new ReportItem(key, value.toString(), 672));
        }

        if ("datenbasis".equals(key) && probe.getDatenbasisId() == null) {
            String nativeQuery = "select id from stammdaten.datenbasis where datenbasis = '";
            nativeQuery += value.toString() + "'";
            Query query = repository.entityManager("land").createNativeQuery(nativeQuery);
            List<Object[]> result = query.getResultList();

            Integer v = Integer.valueOf(result.get(0)[0].toString());
            probe.setDatenbasisId(v);
        }
        else if ("datenbasis".equals(key) && probe.getDatenbasisId() != null){
            this.warnings.add(new ReportItem(key, value.toString(), 672));
        }

        if ("hauptprobennummer".equals(key)) {
            probe.setHauptprobenNr(value.toString());
        }

        if ("mpr_id".equals(key)) {
            Integer v = Integer.valueOf(value.toString());
            probe.setMprId(v);
        }

        if ("netzkennung".equals(key)) {
            probe.setNetzbetreiberId(value.toString());
        }

        if ("messstelle".equals(key)) {
            probe.setMstId(value.toString());
        }

        if ("messprogramm_s".equals(key) && probe.getBaId() == null) {
            probe.setBaId(value.toString());
        }
        else if ("messprogramm_s".equals(key) && probe.getBaId() != null){
            this.warnings.add(new ReportItem(key, value.toString(), 672));
        }

        if ("soll_datum_uhrzeit_a".equals(key)) {
            try {
                Date d = format.parse(value.toString());
                probe.setSolldatumBeginn(new Timestamp(d.getTime()));
            }
            catch (ParseException e) {
                this.warnings.add(new ReportItem(key, value.toString(), 674));
            }
        }
        if ("soll_datum_uhrzeit_e".equals(key)) {
            try {
                Date d = format.parse(value.toString());
                probe.setSolldatumEnde(new Timestamp(d.getTime()));
            }
            catch (ParseException e) {
                this.warnings.add(new ReportItem(key, value.toString(), 674));
            }
        }
        if ("probenahme_datum_uhrzeit_a".equals(key)) {
            try {
                Date d = format.parse(value.toString());
                probe.setProbeentnahmeBeginn(new Timestamp(d.getTime()));
            }
            catch (ParseException e) {
                this.warnings.add(new ReportItem(key, value.toString(), 674));
            }
        }
        if ("probenahme_datum_uhrzeit_e".equals(key)) {
            try {
                Date d = format.parse(value.toString());
                probe.setProbeentnahmeEnde(new Timestamp(d.getTime()));
            }
            catch (ParseException e) {
                this.warnings.add(new ReportItem(key, value.toString(), 674));
            }
        }

        if ("umweltbereich_s".equals(key) && probe.getUmwId() == null) {
            probe.setUmwId(value.toString());
        }
        else if ("umweltbereich_s".equals(key) && probe.getUmwId() != null){
            this.warnings.add(new ReportItem(key, value.toString(), 672));
        }
        if ("umweltbereich_c".equals(key) && probe.getUmwId() == null) {
            String nativeQuery = "select id from stammdaten.umwelt where umwelt_bereich= '";
            int length = value.toString().length() > 80 ? 80 : value.toString().length();
            nativeQuery += value.toString().substring(0, length) + "'";
            Query query = repository.entityManager("land").createNativeQuery(nativeQuery);
            List<Object[]> result = query.getResultList();
            probe.setUmwId(result.get(0)[0].toString());
        }
        else if ("umweltbereich_c".equals(key) && probe.getUmwId() != null){
            this.warnings.add(new ReportItem(key, value.toString(), 672));
        }

        if ("deskriptoren".equals(key)) {
            probe.setMediaDesk(value.toString());
        }

        if ("testdaten".equals(key)) {
            if (!value.toString().equals("0")) {
                probe.setTest(true);
            }
            else {
                probe.setTest(false);
            }
        }

        if ("medium".equals(key)) {
            probe.setMedia(value.toString());
        }

        if ("probenart".equals(key)) {
            String nativeQuery = "select id from stammdaten.probenart where probenart = '";
            nativeQuery += value.toString() + "'";
            Query query = repository.entityManager("land").createNativeQuery(nativeQuery);
            List<Object> result = query.getResultList();
            probe.setProbenartId(Integer.valueOf(result.get(0).toString()));
        }
        return probe;
    }

    public ProbeTranslation addAttribute(
        String key,
        Object value,
        ProbeTranslation probeTranslation
    ) {
        if ("probe_id".equals(key)) {
            QueryBuilder<ProbeTranslation> builder =
                new QueryBuilder<ProbeTranslation>(
                    repository.entityManager("land"), ProbeTranslation.class);
            builder.and("probeIdAlt", value);
            Response response =
                repository.filter(builder.getQuery(), "land");
            List<ProbeTranslation> info = (List<ProbeTranslation>)response.getData();
            if (info != null && info.size() > 0) {
                errors.add(new ReportItem("probe_id", value.toString(), 671));
                return null;
            }
            probeTranslation.setProbeIdAlt(value.toString());
        }
        return probeTranslation;
    }

    /**
     * Add an attribute to the given LKommentarP object.
     *
     * @param key       The key mapping to a object member.
     * @param value     The value to set.
     * @param kommentar The entity object.
     * @return The updated entity object.
     */
    public LKommentarP addAttribute(
        String key,
        Object value,
        LKommentarP kommentar
    ) {
        DateFormat format = new SimpleDateFormat("yyyyMMdd HHmm");
        String v = value.toString();
        String erzeuger = v.substring(1, 6);
        String date = v.substring(8, 21);
        Date d;
        try {
            d = format.parse(date);
            kommentar.setDatum(new Timestamp(d.getTime()));
        }
        catch (ParseException e) {
            this.warnings.add(new ReportItem(key, value.toString(), 674));
        }
        String text = v.substring(23, v.length() -1);
        kommentar.setErzeuger(erzeuger);
        kommentar.setText(text);
        return kommentar;
    }

    /**
     * Add an attribute to the given LKommentarM object.
     *
     * @param key       The key mapping to a object member.
     * @param value     The value to set.
     * @param kommentar The entity object.
     * @return The updated entity object.
     */
    public LKommentarM addAttribute(
        String key,
        Object value,
        LKommentarM kommentar
    ) {
        DateFormat format = new SimpleDateFormat("yyyyMMdd HHmm");
        String v = value.toString();
        String erzeuger = v.substring(1, 6);
        String date = v.substring(8, 21);
        Date d;
        try {
            d = format.parse(date);
            kommentar.setDatum(new Timestamp(d.getTime()));
        }
        catch (ParseException e) {
            this.warnings.add(new ReportItem(key, value.toString(), 674));
        }
        String text = v.substring(23, v.length() -1);
        kommentar.setErzeuger(erzeuger);
        kommentar.setText(text);
        return kommentar;
    }

    /**
     * Add an attribute to the given LMessung object.
     *
     * @param key       The key mapping to a object member.
     * @param value     The value to set.
     * @param messung   The entity object.
     * @return The updated entity object.
     */
    public LMessung addAttribute(
        String key,
        Object value,
        LMessung messung
    ) {
        DateFormat format = new SimpleDateFormat("yyyyMMdd HHmm");
        if ("nebenprobennummer".equals(key)) {
            messung.setNebenprobenNr(value.toString());
        }
        else if ("mess_datum_uhrzeit".equals(key)) {
            try {
                Date d = format.parse(value.toString());
                messung.setMesszeitpunkt(new Timestamp(d.getTime()));
            }
            catch (ParseException e) {
                this.warnings.add(new ReportItem(key, value.toString(), 674));
            }
        }
        else if ("messzeit_sekunden".equals(key)) {
            Integer i = Integer.valueOf(value.toString());
            messung.setMessdauer(i);
        }
        else if ("messmethode_s".equals(key)) {
            messung.setMmtId(value.toString());
        }
        else if ("bearbeitungsstatus".equals(key)) {
            //ignored.!?
        }
        else if ("erfassung_abgeschlossen".equals(key)) {
            if(!value.toString().equals("0")) {
                messung.setFertig(true);
            }
            else {
                messung.setFertig(false);
            }
        }
        return messung;
    }

    public MessungTranslation addAttribute(
        String key,
        Object value,
        MessungTranslation mt
    ) {
        if ("messungs_id".equals(key)) {
            mt.setMessungsIdAlt(Integer.valueOf(value.toString()));
        }
        return mt;
    }

    /**
     * Add an attribute to the given LMesswert object.
     *
     * @param key       The key mapping to a object member.
     * @param value     The value to set.
     * @param messwert  The entity object.
     * @return The updated entity object.
     */
    public LMesswert addAttribute(
        String key,
        Object value,
        LMesswert messwert
    ) {
        Pattern p = Pattern.compile(
            "(\".+\")( .+ )(\".+\")( .*)( .{1,12})( .{1,9})(.{0,9})(.{0,3})");
        //TODO Does not perfectly match... Use better matching for floats.
        Matcher m = p.matcher(value.toString());
        if (m.matches()) {
            String messgroesse = m.group(1).substring(1, m.group(1).length() - 1);
            String wert = m.group(2);
            String einheit = m.group(3).substring(1, m.group(3).length() - 1);
            if (wert.startsWith(" <")) {
                wert = wert.substring(2);
                messwert.setGrenzwertueberschreitung(false);
            }
            else if (wert.startsWith(" >")) {
                wert = wert.substring(2);
                messwert.setGrenzwertueberschreitung(true);
            }
            float fWert = Float.valueOf(wert);
            messwert.setMesswert(fWert);

            String nativeQuery = "select id from stammdaten.mess_einheit where einheit = '";
            nativeQuery += einheit + "'";
            Query query = repository.entityManager("land").createNativeQuery(nativeQuery);
            List<Object> result = query.getResultList();

            if (result.isEmpty()) {
                this.errors.add(new ReportItem("messeinheit", "null", 673));
                return null;
            }
            else {
                messwert.setMehId((Integer)result.get(0));
            }

            String nativeQuery2 = "select id from stammdaten.messgroesse where messgroesse = '";
            nativeQuery2 += messgroesse + "'";
            Query query2 = repository.entityManager("land").createNativeQuery(nativeQuery2);
            List<Object> result2 = query2.getResultList();

            if (result2.isEmpty()) {
                this.errors.add(new ReportItem("messgroesse", "null", 673));
                return null;
            }
            else {
                messwert.setMessgroesseId((Integer)result2.get(0));
            }
        }
        return messwert;
    }

    /**
     * Add an attribute to the OrtCreator. The creator is used to build the
     * two objects Ort and LOrt.
     *
     * @param key       The key mapping to a object member.
     * @param value     The value to set.
     * @param ort       The creator object.
     * @return The updated creator object.
     */
    public OrtCreator addAttribute(
        String key,
        Object value,
        OrtCreator ort
    ) {
        if ("ort_code".equals(key)) {
            ort.setOrtCode(value.toString());
        }
        if ("ort_typ".equals(key)) {
            ort.setOrtTyp(value.toString());
        }
        if ("ort_zusatz".equals(key)) {
            ort.setZusatztext(value.toString());
        }
        if ("ort_land_lang".equals(key)) {
            ort.setLandLang(value.toString());
        }
        if ("ort_land_kurz".equals(key)) {
            ort.setLandKurz(value.toString());
        }
        if ("ort_land_s".equals(key)) {
            ort.setLandS(value.toString());
        }
        if ("ort_gemeindeschlüssel".equals(key)) {
            ort.setGemSchluessel(value.toString());
        }
        if ("ort_bezeichnung".equals(key)) {
            ort.setBezeichnung(value.toString());
        }
        if ("ort_beschreibung".equals(key)) {
            ort.setBeschreibung(value.toString());
        }
        if ("ort_nuts_code".equals(key)) {
            ort.setNuts(value.toString());
        }
        if ("ort_hoehe_land".equals(key)) {
            ort.setHoehe(value.toString());
        }
        if ("ort_koordinaten".equals(key)) {
            ort.setKoordinaten(value.toString());
        }
        if ("ort_koordinaten_s".equals(key)) {
            ort.setKoordinatenS(value.toString());
        }
        return ort;
    }

    /**
     * Add an attribute to the given LZusatzwert object.
     *
     * @param lKey       The key mapping to a object member.
     * @param value     The value to set.
     * @param wert      The entity object.
     * @return The updated entity object.
     */
    public LZusatzWert addAttribute(
        String lKey,
        Object value,
        LZusatzWert wert
    ) {
        String v = value.toString().substring(1);
        int ndx = v.indexOf("\"");
        String groesse = v.substring(0, ndx);
        v = v.substring(ndx + 2);
        ndx = v.indexOf(" ");
        String w = v.substring(0, ndx);
        v = v.substring(ndx + 2);
        ndx = v.indexOf("\"");
        String einheit = v.substring(0, ndx);
        String fehler = v.substring(ndx + 2);

        String nativeQuery = "select id from stammdaten.probenzusatz where zusatzwert = '";
        nativeQuery += groesse + "'";
        Query query = repository.entityManager("land").createNativeQuery(nativeQuery);
        List<Object[]> result = query.getResultList();

        if (result == null || result.isEmpty()) {
            this.errors.add(new ReportItem(lKey, "zusatzwert", 673));
            return null;
        }
        wert.setPzsId(result.get(0)[0].toString());
        wert.setMesswertPzs(Float.valueOf(w));
        wert.setMessfehler(Float.valueOf(fehler));
        return wert;
    }

    /**
     * Add an attribute to the given LZusatzwert object.
     *
     * @param lKey       The key mapping to a object member.
     * @param value     The value to set.
     * @param wert     The entity object.
     * @return The updated entity object.
     */
    public LZusatzWert addAttributeS(
        String lKey,
        Object value,
        LZusatzWert wert
    ) {
        String v = value.toString().substring(1);
        int ndx = v.indexOf("\"");
        String groesse = v.substring(0, ndx);
        v = v.substring(ndx + 2);
        ndx = v.indexOf(" ");
        String w = v.substring(0, ndx);
        v = v.substring(ndx + 2);
        ndx = v.indexOf(" ");
        String einheit = v.substring(0, ndx);
        String fehler = v.substring(ndx + 2);
        wert.setPzsId(groesse);
        wert.setMesswertPzs(Float.valueOf(w));
        wert.setMessfehler(Float.valueOf(fehler));
        return wert;
    }

    /**
     * @return the warnings
     */
    public List<ReportItem> getWarnings() {
        return warnings;
    }

    /**
     * @return the errors
     */
    public List<ReportItem> getErrors() {
        return errors;
    }

    public void reset() {
        errors = new ArrayList<ReportItem>();
        warnings = new ArrayList<ReportItem>();
    }
}
