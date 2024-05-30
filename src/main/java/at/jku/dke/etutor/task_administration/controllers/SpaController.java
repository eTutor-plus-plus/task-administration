package at.jku.dke.etutor.task_administration.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for the single page application.
 */
@Controller
public class SpaController {
    /**
     * Creates a new instance of class {@link SpaController}.
     */
    public SpaController() {
    }

    /**
     * Returns the index view of the app.
     *
     * @return The forward to the index view.
     */
    @RequestMapping("/")
    public String redirectSpaRoot() {
        return "redirect:/app";
    }

    /**
     * Returns the index view of the app.
     *
     * @return The forward to the index view.
     */
    @RequestMapping("/app")
    public String redirectSpa() {
        return "forward:/app/index.html";
    }

    /**
     * Returns the index view of the app.
     *
     * @return The forward to the index view.
     */
    @RequestMapping("/app/")
    public String redirectSpaSlash() {
        return "forward:/app/index.html";
    }

    /**
     * Forwards all requests not containing a dot to the index view.
     *
     * @return Forward
     */
    @RequestMapping({"/app/{path:[^\\.]*}"})
    public String redirectSpaNotFoundFileLevel1(@SuppressWarnings("unused") @PathVariable String path) {
        return "forward:/app/index.html";
    }

    /**
     * Forwards all requests not containing a dot to the index view.
     *
     * @return Forward
     */
    @RequestMapping("/app/*/{path:[^\\.]*}")
    public String redirectSpaNotFoundFileLevel2(@SuppressWarnings("unused") @PathVariable String path) {
        return "forward:/app/index.html";
    }
}
