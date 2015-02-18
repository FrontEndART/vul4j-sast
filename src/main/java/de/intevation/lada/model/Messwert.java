/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;


/**
 * The persistent class for the messwert database table.
 */
@MappedSuperclass
@Table(name="messwert")
public class Messwert implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id", unique=true, nullable=false)
    private Integer id;

    private Boolean grenzwertueberschreitung;

    @Column(name="letzte_aenderung")
    private Timestamp letzteAenderung;

    @Column(name="meh_id")
    private Integer mehId;

    private Float messfehler;

    @Column(name="messgroesse_id")
    private Integer messgroesseId;

    @Column(name="messungs_id")
    private Integer messungsId;

    private Float messwert;

    @Column(name="messwert_nwg")
    private String messwertNwg;

    @Column(name="nwg_zu_messwert")
    private Float nwgZuMesswert;

    public Messwert() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getGrenzwertueberschreitung() {
        return this.grenzwertueberschreitung;
    }

    public void setGrenzwertueberschreitung(Boolean grenzwertueberschreitung) {
        this.grenzwertueberschreitung = grenzwertueberschreitung;
    }

    public Timestamp getLetzteAenderung() {
        return this.letzteAenderung;
    }

    public void setLetzteAenderung(Timestamp letzteAenderung) {
        this.letzteAenderung = letzteAenderung;
    }

    public Integer getMehId() {
        return this.mehId;
    }

    public void setMehId(Integer mehId) {
        this.mehId = mehId;
    }

    public Float getMessfehler() {
        return this.messfehler;
    }

    public void setMessfehler(Float messfehler) {
        this.messfehler = messfehler;
    }

    public Integer getMessgroesseId() {
        return this.messgroesseId;
    }

    public void setMessgroesseId(Integer messgroesseId) {
        this.messgroesseId = messgroesseId;
    }

    public Integer getMessungsId() {
        return this.messungsId;
    }

    public void setMessungsId(Integer messungsId) {
        this.messungsId = messungsId;
    }

    public Float getMesswert() {
        return this.messwert;
    }

    public void setMesswert(Float messwert) {
        this.messwert = messwert;
    }

    public String getMesswertNwg() {
        return this.messwertNwg;
    }

    public void setMesswertNwg(String messwertNwg) {
        this.messwertNwg = messwertNwg;
    }

    public Float getNwgZuMesswert() {
        return this.nwgZuMesswert;
    }

    public void setNwgZuMesswert(Float nwgZuMesswert) {
        this.nwgZuMesswert = nwgZuMesswert;
    }

}
