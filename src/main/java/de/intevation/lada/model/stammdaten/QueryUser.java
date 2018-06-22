package de.intevation.lada.model.stammdaten;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * The persistent class for the query_user database table.
 *
 */
@Entity
@Table(name="query_user")
public class QueryUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    private String description;

    private String name;

    @Column(name="user_id")
    private Integer userId;

    //uni-directional many-to-one association to Query
    @Column(name="base_query")
    private Integer baseQuery;

    //bi-directional many-to-one association to QueryMessstelle
    @OneToMany(mappedBy="queryUser", fetch=FetchType.EAGER, cascade = CascadeType.ALL)
    private List<QueryMessstelle> messStelles;

    @Transient
    private String[] messStellesIds;

    public QueryUser() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getUserId() {
        return this.userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getBaseQuery() {
        return this.baseQuery;
    }

    public void setBaseQuery(Integer query) {
        this.baseQuery = query;
    }

    @JsonIgnore
    public List<QueryMessstelle> getMessStelles() {
        if (this.messStelles == null) {
            this.messStelles = new ArrayList<QueryMessstelle>();
        }
        return this.messStelles;
    }

    public void setMessStelles(List<QueryMessstelle> messStelles) {
        this.messStelles = messStelles;
    }

    public QueryMessstelle addMessStelle(QueryMessstelle messStelle) {
        getMessStelles().add(messStelle);
        messStelle.setQueryUser(this);

        return messStelle;
    }

    public QueryMessstelle removeMessStelle(QueryMessstelle messStelle) {
        getMessStelles().remove(messStelle);
        messStelle.setQueryUser(null);

        return messStelle;
    }

    public String[] getMessStellesIds() {
        return messStellesIds;
    }

    public void setMessStellesIds(String[] messStellesIds) {
        this.messStellesIds = messStellesIds;
    }
}