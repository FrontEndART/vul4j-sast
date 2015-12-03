package de.intevation.lada.model.stamm;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * The persistent class for the messprogramm_kategorie database table.
 * 
 */
@Entity
@Table(name="messprogramm_kategorie")
public class MessprogrammKategorie implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private String bezeichnung;

    @Column(name="letzte_aenderung")
    private Timestamp letzteAenderung;

    @Column(name="mpl_id")
    private String mplId;

    @Column(name="netzbetreiber_id")
    private String netzbetreiberId;

    public MessprogrammKategorie() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBezeichnung() {
        return this.bezeichnung;
    }

    public void setBezeichnung(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public Timestamp getLetzteAenderung() {
        return this.letzteAenderung;
    }

    public void setLetzteAenderung(Timestamp letzteAenderung) {
        this.letzteAenderung = letzteAenderung;
    }

    public String getMplId() {
        return this.mplId;
    }

    public void setMplId(String mplId) {
        this.mplId = mplId;
    }

    public String getNetzbetreiberId() {
        return this.netzbetreiberId;
    }

    public void setNetzbetreiberId(String netzbetreiberId) {
        this.netzbetreiberId = netzbetreiberId;
    }

}
