/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.Transient;


/**
 * The persistent class for the ort database table.
 */
@MappedSuperclass
@Table(name="ort")
public class Ort implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id", unique=true, nullable=false)
    private Integer id;

    @Column(name="letzte_aenderung")
    private Timestamp letzteAenderung;

    @Column(name="orts_typ")
    private String ortsTyp;

    private String ortszusatztext;

    @Column(name="probe_id")
    private Integer probeId;

    @Column(name="ort_id")
    private BigInteger ort;

    @Transient
    private boolean owner;

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

    public Timestamp getLetzteAenderung() {
        return this.letzteAenderung;
    }

    public void setLetzteAenderung(Timestamp letzteAenderung) {
        this.letzteAenderung = letzteAenderung;
    }

    public String getOrtsTyp() {
        return this.ortsTyp;
    }

    public void setOrtsTyp(String ortsTyp) {
        this.ortsTyp = ortsTyp;
    }

    public String getOrtszusatztext() {
        return this.ortszusatztext;
    }

    public void setOrtszusatztext(String ortszusatztext) {
        this.ortszusatztext = ortszusatztext;
    }

    public Integer getProbeId() {
        return this.probeId;
    }

    public void setProbeId(Integer probeId) {
        this.probeId = probeId;
    }

    public BigInteger getOrt() {
        return this.ort;
    }

    public void setOrt(BigInteger ort) {
        this.ort = ort;
    }

    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }
}
