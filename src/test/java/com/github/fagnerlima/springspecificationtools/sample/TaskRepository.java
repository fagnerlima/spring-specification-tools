package com.github.fagnerlima.springspecificationtools.sample;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    /**
     * @return the descriptions of the tasks matching {@code specification}, sorted, so assertions do not depend on
     *         database ordering
     */
    default List<String> findDescriptions(Specification<Task> specification) {
        return findAll(specification, Sort.by("description")).stream()
                .map(Task::getDescription)
                .toList();
    }

}
