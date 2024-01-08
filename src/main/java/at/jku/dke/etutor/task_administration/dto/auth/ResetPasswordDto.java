package at.jku.dke.etutor.task_administration.dto.auth;


import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * DTO for resetting the password.
 */
public class ResetPasswordDto {
    @NotEmpty
    private String token;
    @NotEmpty
    @Size(min = 6, max = 64)
    private String password;
    @NotEmpty
    private String passwordRepeat;

    /**
     * Creates a new instance of class {@link ResetPasswordDto}.
     */
    public ResetPasswordDto() {
    }

    /**
     * Creates a new instance of class {@link ResetPasswordDto}.
     *
     * @param token The token.
     */
    public ResetPasswordDto(String token) {
        this.token = token;
    }

    /**
     * Creates a new instance of class {@link ResetPasswordDto}.
     *
     * @param token          The token.
     * @param password       The password.
     * @param passwordRepeat The password repeat.
     */
    public ResetPasswordDto(String token, String password, String passwordRepeat) {
        this.token = token;
        this.password = password;
        this.passwordRepeat = passwordRepeat;
    }

    /**
     * Gets the token.
     *
     * @return The token.
     */
    public String getToken() {
        return token;
    }

    /**
     * Gets the password.
     *
     * @return The password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the password repeat.
     *
     * @return The password repeat.
     */
    public String getPasswordRepeat() {
        return passwordRepeat;
    }

    /**
     * Sets the token.
     *
     * @param token The token.
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Sets the password.
     *
     * @param password The password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the password repeat.
     *
     * @param passwordRepeat The password repeat.
     */
    public void setPasswordRepeat(String passwordRepeat) {
        this.passwordRepeat = passwordRepeat;
    }

    @AssertTrue(message = "{jakarta.validation.constraints.ValueEquals.message}")
    private boolean isValid() {
        return this.password != null && this.password.equals(this.passwordRepeat);
    }
}
