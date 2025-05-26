package pdfact.api.model;

import java.util.List;

/**
 * The expected request payload.
 */
public class RequestPayload {
    /**
     * The url to access the pdf file.
     */
    private String url;
    /**
     * The unit to split text on (e.g., paragraphs, words, characters, etc.).
     */
    private List<String> unit;
    /**
     * The roles to extract (e.g., body, title, etc.).
     */
    private List<String> roles;

    // ==============================================================================================

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // ==============================================================================================

    public List<String> getUnit() {
        return unit;
    }

    public void setUnit(List<String> unit) {
        this.unit = unit;
    }

    // ==============================================================================================

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
