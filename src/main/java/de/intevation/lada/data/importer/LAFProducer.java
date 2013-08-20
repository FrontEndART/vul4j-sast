package de.intevation.lada.data.importer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Named;

import de.intevation.lada.model.LKommentarM;
import de.intevation.lada.model.LKommentarP;
import de.intevation.lada.model.LMessung;
import de.intevation.lada.model.LMessungId;
import de.intevation.lada.model.LMesswert;
import de.intevation.lada.model.LMesswertId;
import de.intevation.lada.model.LOrt;
import de.intevation.lada.model.LProbe;

@Named("lafproducer")
public class LAFProducer
implements Producer
{

    private LProbe probe;
    private LMessung messung;
    private LOrt ort;

    private List<LKommentarP> pKommentare;
    private List<LKommentarM> mKommentare;
    private List<LMessung> messungen;
    private List<LOrt> orte;
    private List<LMesswert> messwerte;

    private List<EntryFormat> probenFormat;
    private List<EntryFormat> messungFormat;
    private List<EntryFormat> ortFormat;

    private AttributeMapper mapper;

    public LAFProducer() {
        this.mapper = new AttributeMapper();
        this.probe = new LProbe();
        this.pKommentare = new ArrayList<LKommentarP>();
        this.mKommentare = new ArrayList<LKommentarM>();
        this.messungen = new ArrayList<LMessung>();
        this.orte = new ArrayList<LOrt>();
        this.messwerte = new ArrayList<LMesswert>();
        String fileName = System.getProperty("de_intevation_lada_import");
        LAFFormat format = new LAFFormat();
        format.readConfigFile(fileName);
        probenFormat = format.getFormat("probe");
        messungFormat = format.getFormat("messung");
        ortFormat = format.getFormat("ort");
    }

    @Override
    public void addData(String key, Object values) {
        String lKey = key.toLowerCase();
        if(lKey.equals("probenkommentar")) {
            this.pKommentare.add(
                mapper.addAttribute(lKey, values, new LKommentarP()));
        }
        else if (lKey.equals("kommentar")) {
            this.mKommentare.add(
                mapper.addAttribute(lKey, values, new LKommentarM()));
        }
        else if (lKey.equals("probenzusatzbeschreibung")) {
            //TODO: implement this!
        }
        else if (lKey.equals("pzb_s")) {
            //TODO: implement this!
        }
        else if (lKey.equals("messwert")) {
            LMesswertId id = new LMesswertId();
            id.setProbeId(this.probe.getProbeId());
            id.setMessungsId(this.messung.getId().getMessungsId());
            LMesswert m = new LMesswert();
            m.setId(id);
            this.messwerte.add(
                mapper.addAttribute(lKey, values, m));
        }
        else if (isValidMessung(lKey, values.toString())) {
            this.messung = mapper.addAttribute(lKey, values, this.messung);
        }
        else if (isValidProbe(lKey, values.toString())) {
            this.probe = mapper.addAttribute(lKey, values, this.probe);
        }
        else if (isValidOrt(lKey, values.toString())) {
            this.ort = mapper.addAttribute(lKey, values, this.ort);
        }
    }

    private boolean isValidOrt(String key, String value) {
        for (EntryFormat ef: ortFormat) {
            if (ef.getKey().equals(key.toLowerCase())) {
                if (ef.getPattern().matcher(value).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValidMessung(String key, String value) {
        for (EntryFormat ef: messungFormat) {
            if (ef.getKey().equals(key.toLowerCase())) {
                if (ef.getPattern().matcher(value).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValidProbe(String key, String value) {
        for (EntryFormat ef: probenFormat) {
            if (ef.getKey().equals(key.toLowerCase())) {
                if (ef.getPattern().matcher(value).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public LProbe getProbe() {
        return this.probe;
    }

    @Override
    public List<LMessung> getMessungen() {
        return this.messungen;
    }

    @Override
    public List<LOrt> getOrte() {
        return this.orte;
    }

    @Override
    public List<LKommentarP> getProbenKommentare() {
        return this.pKommentare;
    }

    @Override
    public List<LKommentarM> getMessungsKommentare() {
        return this.mKommentare;
    }

    @Override
    public List<LMesswert> getMesswerte() {
        return this.messwerte;
    }

    @Override
    public void reset() {
        this.probe = new LProbe();
        this.messungen.clear();
        this.messung = null;
        this.orte.clear();
        this.ort = null;
        this.messwerte.clear();
        this.mKommentare.clear();
        this.pKommentare.clear();
    }

    public void newMessung() {
        if (this.messung != null) {
            this.messungen.add(this.messung);
        }
        LMessungId id = new LMessungId();
        id.setProbeId(this.probe.getProbeId());
        this.messung = new LMessung();
        this.messung.setId(id);
    }

    public void newOrt() {
        if (this.ort != null) {
            this.orte.add(this.ort);
        }
        this.ort = new LOrt();
        this.ort.setProbeId(this.probe.getProbeId());
    }
}
