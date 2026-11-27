package com.github.fagnerlima.springspecificationtools.sample;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "tag")
public class Tag extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String description;

    protected Tag() {
    }

    public Tag(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
