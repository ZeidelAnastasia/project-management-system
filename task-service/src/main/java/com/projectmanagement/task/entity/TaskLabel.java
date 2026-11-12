
package com.projectmanagement.task.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "task_labels")
@Getter
@Setter
@NoArgsConstructor
public class TaskLabel {

    @EmbeddedId
    private TaskLabelId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("taskId")
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("labelId")
    @JoinColumn(name = "label_id", nullable = false)
    private Label label;
}
