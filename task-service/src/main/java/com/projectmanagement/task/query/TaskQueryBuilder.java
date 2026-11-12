
package com.projectmanagement.task.query;

import com.projectmanagement.task.entity.Task;
import com.projectmanagement.task.entity.TaskLabel;
import com.projectmanagement.task.entity.enums.TaskPriority;
import com.projectmanagement.task.entity.enums.TaskStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TaskQueryBuilder {

    private TaskQueryBuilder() {
    }

    public static List<Predicate> buildTaskFilterPredicates(
            CriteriaBuilder cb,
            Root<Task> root,
            CriteriaQuery<?> query,
            UUID projectId,
            TaskStatus status,
            TaskPriority priority,
            UUID assigneeId,
            LocalDate dueDateFrom,
            LocalDate dueDateTo,
            UUID labelId
    ) {
        List<Predicate> predicates = new ArrayList<>();

        if (projectId != null) {
            predicates.add(
                    cb.equal(root.get("projectId"), projectId)
            );
        }

        if (status != null) {
            predicates.add(
                    cb.equal(root.get("status"), status)
            );
        }

        if (priority != null) {
            predicates.add(
                    cb.equal(root.get("priority"), priority)
            );
        }

        if (assigneeId != null) {
            predicates.add(
                    cb.equal(root.get("assigneeId"), assigneeId)
            );
        }

        if (dueDateFrom != null) {
            predicates.add(
                    cb.greaterThanOrEqualTo(root.get("dueDate"), dueDateFrom)
            );
        }

        if (dueDateTo != null) {
            predicates.add(
                    cb.lessThanOrEqualTo(root.get("dueDate"), dueDateTo)
            );
        }

        if (labelId != null) {
            Subquery<Integer> labelSubquery = query.subquery(Integer.class);
            Root<TaskLabel> taskLabelRoot = labelSubquery.from(TaskLabel.class);

            labelSubquery
                    .select(cb.literal(1))
                    .where(
                            cb.equal(
                                    taskLabelRoot.get("task").get("id"),
                                    root.get("id")
                            ),
                            cb.equal(
                                    taskLabelRoot.get("label").get("id"),
                                    labelId
                            )
                    );

            predicates.add(cb.exists(labelSubquery));
        }

        return predicates;
    }
}
