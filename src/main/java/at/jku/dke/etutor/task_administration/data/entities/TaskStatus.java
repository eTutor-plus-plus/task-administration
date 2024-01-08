package at.jku.dke.etutor.task_administration.data.entities;

/**
 * Provides the task statuses.
 */
public enum TaskStatus {
    /**
     * The task (group) is a draft.
     */
    DRAFT,

    /**
     * The task (group) is ready for approval.
     */
    READY_FOR_APPROVAL,

    /**
     * The task (group) is approved.
     */
    APPROVED
}
