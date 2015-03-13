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
 * The persistent class for the kommentar_m database table.
 */
@MappedSuperclass
@Table(name="kommentar_m")
public class KommentarM implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name="messungs_id")
    private Integer messungsId;

    private Timestamp datum;

    private String erzeuger;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id", unique=true, nullable=false)
    private Integer id;

    private String text;

    public KommentarM() {
    }

    public Timestamp getDatum() {
        return this.datum;
    }

    public void setDatum(Timestamp datum) {
        this.datum = datum;
    }

    public String getErzeuger() {
        return this.erzeuger;
    }

    public void setErzeuger(String erzeuger) {
        this.erzeuger = erzeuger;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getMessungsId() {
        return this.messungsId;
    }

    public void setMessungsId(Integer messungsId) {
        this.messungsId = messungsId;
    }
}
