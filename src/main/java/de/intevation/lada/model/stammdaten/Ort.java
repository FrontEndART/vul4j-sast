package de.intevation.lada.model.stammdaten;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;


/**
 * The persistent class for the ort database table.
 * 
 */
@Entity
@NamedQuery(name="Ort.findAll", query="SELECT o FROM Ort o")
public class Ort implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    private String aktiv;

    @Column(name="anlage_id")
    private Integer anlageId;

    private String berichtstext;

    @Column(name="gem_id")
    private String gemId;

    @Column(name="hoehe_land")
    private Float hoeheLand;

    @Column(name="koord_x_extern")
    private String koordXExtern;

    @Column(name="koord_y_extern")
    private String koordYExtern;

    private String kurztext;

    private String langtext;

    private Double latitude;

    @Column(name="letzte_aenderung")
    private Timestamp letzteAenderung;

    private Double longitude;

    @Column(name="mp_art")
    private String mpArt;

    @Column(name="netzbetreiber_id")
    private String netzbetreiberId;

    @Column(name="nuts_code")
    private String nutsCode;

    @Column(name="ort_id")
    private String ortId;

    @Column(name="ort_typ")
    private Integer ortTyp;

    @Column(name="oz_id")
    private Integer ozId;

    private String sektor;

    @Column(name="staat_id")
    private Integer staatId;

    private String unscharf;

    private String zone;

    private String zustaendigkeit;

    @Column(name="kda_id")
    private Integer kdaId;

    @Transient
    private boolean readonly;

    public Ort() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAktiv() {
        return this.aktiv;
    }

    public void setAktiv(String aktiv) {
        this.aktiv = aktiv;
    }

    public Integer getAnlageId() {
        return this.anlageId;
    }

    public void setAnlageId(Integer anlageId) {
        this.anlageId = anlageId;
    }

    public String getBerichtstext() {
        return this.berichtstext;
    }

    public void setBerichtstext(String berichtstext) {
        this.berichtstext = berichtstext;
    }

    public String getGemId() {
        return this.gemId;
    }

    public void setGemId(String gemId) {
        this.gemId = gemId;
    }

    public Float getHoeheLand() {
        return this.hoeheLand;
    }

    public void setHoeheLand(Float hoeheLand) {
        this.hoeheLand = hoeheLand;
    }

    public String getKoordXExtern() {
        return this.koordXExtern;
    }

    public void setKoordXExtern(String koordXExtern) {
        this.koordXExtern = koordXExtern;
    }

    public String getKoordYExtern() {
        return this.koordYExtern;
    }

    public void setKoordYExtern(String koordYExtern) {
        this.koordYExtern = koordYExtern;
    }

    public String getKurztext() {
        return this.kurztext;
    }

    public void setKurztext(String kurztext) {
        this.kurztext = kurztext;
    }

    public String getLangtext() {
        return this.langtext;
    }

    public void setLangtext(String langtext) {
        this.langtext = langtext;
    }

    public Double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Timestamp getLetzteAenderung() {
        return this.letzteAenderung;
    }

    public void setLetzteAenderung(Timestamp letzteAenderung) {
        this.letzteAenderung = letzteAenderung;
    }

    public Double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getMpArt() {
        return this.mpArt;
    }

    public void setMpArt(String mpArt) {
        this.mpArt = mpArt;
    }

    public String getNetzbetreiberId() {
        return this.netzbetreiberId;
    }

    public void setNetzbetreiberId(String netzbetreiberId) {
        this.netzbetreiberId = netzbetreiberId;
    }

    public String getNutsCode() {
        return this.nutsCode;
    }

    public void setNutsCode(String nutsCode) {
        this.nutsCode = nutsCode;
    }

    public String getOrtId() {
        return this.ortId;
    }

    public void setOrtId(String ortId) {
        this.ortId = ortId;
    }

    public Integer getOrtTyp() {
        return this.ortTyp;
    }

    public void setOrtTyp(Integer ortTyp) {
        this.ortTyp = ortTyp;
    }

    public Integer getOzId() {
        return this.ozId;
    }

    public void setOzId(Integer ozId) {
        this.ozId = ozId;
    }

    public String getSektor() {
        return this.sektor;
    }

    public void setSektor(String sektor) {
        this.sektor = sektor;
    }

    public Integer getStaatId() {
        return this.staatId;
    }

    public void setStaatId(Integer staatId) {
        this.staatId = staatId;
    }

    public String getUnscharf() {
        return this.unscharf;
    }

    public void setUnscharf(String unscharf) {
        this.unscharf = unscharf;
    }

    public String getZone() {
        return this.zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getZustaendigkeit() {
        return this.zustaendigkeit;
    }

    public void setZustaendigkeit(String zustaendigkeit) {
        this.zustaendigkeit = zustaendigkeit;
    }

    public Integer getKdaId() {
        return this.kdaId;
    }

    public void setKdaId(Integer kdaId) {
        this.kdaId = kdaId;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

}
