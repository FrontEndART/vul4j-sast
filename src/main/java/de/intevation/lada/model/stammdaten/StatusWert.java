package de.intevation.lada.model.stammdaten;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * The persistent class for the status_wert database table.
 *
 */
@Entity
@Table(name="status_wert")
public class StatusWert implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private String wert;

    public StatusWert() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getWert() {
        return this.wert;
    }

    public void setWert(String wert) {
        this.wert = wert;
    }
}
