package pdfact.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import pdfact.api.model.RequestPayload;
import pdfact.core.util.exception.PdfActException;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static spark.Spark.post;

/**
 * The API to parse a pdf file.
 */
public class PdfApi {

    public static void main(String[] args) {
        PdfService pdfService = new PdfService();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        post("/api/pdf/parse", (request, response) -> parsePdf(request, response, pdfService, gson), gson::toJson);
    }

    private static Object parsePdf(Request request, Response response, PdfService pdfService, Gson gson) {
        Path tempFile = null;
        RequestPayload requestPayload = null;
        List<String> units = null;
        List<String> roles = null;

        try {
            if (request.contentType() != null &&
                    request.contentType().toLowerCase().startsWith("multipart/form-data")) {

                // Handle multipart
                request.attribute("org.eclipse.jetty.multipartConfig",
                        new javax.servlet.MultipartConfigElement("/tmp"));

                javax.servlet.http.Part filePart = request.raw().getPart("file");
                if (filePart == null || filePart.getSize() == 0) {
                    response.status(400);
                    return error("File upload is missing");
                }

                tempFile = Files.createTempFile("upload", ".pdf");
                try (InputStream in = filePart.getInputStream()) {
                    Files.copy(in, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }

                // Also parse units and roles from `request.queryParams` or `request.formData`
                String unitsStr = request.raw().getParameter("unit");
                String rolesStr = request.raw().getParameter("roles");

                if (unitsStr != null) units = List.of(unitsStr.split(","));
                if (rolesStr != null) roles = List.of(rolesStr.split(","));

            } else {
                // JSON path
                String body = request.body();
                requestPayload = gson.fromJson(body, RequestPayload.class);
                if (requestPayload == null || requestPayload.getUrl() == null || requestPayload.getUrl().isEmpty()) {
                    response.status(400);
                    return error("File url is required");
                }
            }

            JsonObject jsonResult;

            if (tempFile != null) {
                jsonResult = pdfService.parsePdfFromFile(tempFile, units, roles);
            } else {
                jsonResult = pdfService.parsePdfFromUrl(requestPayload.getUrl(), requestPayload.getUnit(), requestPayload.getRoles());
            }

            response.status(200);
            return jsonResult;

        } catch (Exception e) {
            response.status(500);
            return error("An error occurred: " + e.getMessage());
        }
    }

    private static JsonObject error(String msg) {
        JsonObject err = new JsonObject();
        err.addProperty("error", msg);
        return err;
    }
}

