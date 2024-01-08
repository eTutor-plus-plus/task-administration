package at.jku.dke.etutor.task_administration.data.entities;

/**
 * Provides the user roles.
 */
public enum UserRole {
    /**
     * Administrator has access to all.
     */
    ADMIN,

    /**
     * Instructors are allowed to manage tasks.
     */
    INSTRUCTOR,

    /**
     * Tutors are allowed to draft tasks.
     */
    TUTOR
}
