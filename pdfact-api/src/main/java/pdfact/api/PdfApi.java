package pdfact.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import pdfact.api.model.RequestPayload;
import pdfact.core.util.exception.PdfActException;
import spark.Request;
import spark.Response;

import java.io.IOException;

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
        String body = request.body();
        RequestPayload requestPayload = gson.fromJson(body, RequestPayload.class);

        if (requestPayload == null || requestPayload.getUrl() == null || requestPayload.getUrl().isEmpty()) {
            response.status(400);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "File url is required");
            return errorResponse;
        }

        JsonObject jsonResult;

        try {
            String jsonString = pdfService.parsePdf(requestPayload.getUrl(), requestPayload.getUnit(), requestPayload.getRoles());
            jsonResult = gson.fromJson(jsonString, JsonObject.class);
            response.status(200);
        } catch (IllegalArgumentException e) {
            response.status(422);
            jsonResult = new JsonObject();
            jsonResult.addProperty("error", "Illegal arguments. " + e.getMessage());
        } catch (IOException e) {
            response.status(400);
            jsonResult = new JsonObject();
            jsonResult.addProperty("error", "An error occurred while downloading the pdf file. " + e.getMessage());
        } catch (PdfActException e) {
            response.status(500);
            jsonResult = new JsonObject();
            jsonResult.addProperty("error", "An error occurred while processing the pdf file.");
        }
        return jsonResult;
    }
}

