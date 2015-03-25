/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.land;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * The persistent class for the status database table.
 */
@Entity
@Table(name="status")
public class LStatus extends de.intevation.lada.model.Status {
    private static final long serialVersionUID = 1L;

    @Column(name="tree_modified")
    private Timestamp treeModified;

    public Timestamp getTreeModified() {
        return treeModified;
    }

    public void setTreeModified(Timestamp treeModified) {
        this.treeModified = treeModified;
    }
}
