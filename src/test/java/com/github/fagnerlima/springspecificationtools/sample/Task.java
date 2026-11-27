package com.github.fagnerlima.springspecificationtools.sample;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/**
 * Sample entity based on the README example, with extra columns to exercise every {@code SpecOperation}.
 */
@Entity
@Table(name = "task")
public class Task extends BaseEntity {

    private static final long serialVersionUID = 1L;

    public enum Status {
        TODO, DOING, DONE
    }

    @Embedded
    private Period period;

    private String description;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Integer priority;

    private LocalDateTime createdAt;

    private Boolean done;

    private Integer minEstimate;

    private Integer maxEstimate;

    private String notes;

    @ManyToMany
    @JoinTable(name = "tag_task",
            joinColumns = @JoinColumn(name = "id_task"),
            inverseJoinColumns = @JoinColumn(name = "id_tag"))
    private Set<Tag> tags = new HashSet<>();

    protected Task() {
    }

    public Task(String description) {
        this.description = description;
    }

    public Task period(Period period) {
        this.period = period;
        return this;
    }

    public Task status(Status status) {
        this.status = status;
        return this;
    }

    public Task priority(Integer priority) {
        this.priority = priority;
        return this;
    }

    public Task createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Task done(Boolean done) {
        this.done = done;
        return this;
    }

    public Task estimate(Integer minEstimate, Integer maxEstimate) {
        this.minEstimate = minEstimate;
        this.maxEstimate = maxEstimate;
        return this;
    }

    public Task notes(String notes) {
        this.notes = notes;
        return this;
    }

    public Task tags(Tag... tags) {
        this.tags.addAll(Set.of(tags));
        return this;
    }

    public String getDescription() {
        return description;
    }

}
