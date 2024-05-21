package pdfact;

import com.google.gson.Gson;

import static spark.Spark.post;

public class PdfApi {
    public static void main(String[] args) {
        PdfService pdfService = new PdfService();
        Gson gson = new Gson();

        post("/api/pdf/parse", (request, response) -> {
            String filePath = request.queryParams("filePath");

            if (filePath == null || filePath.isEmpty()) {
                response.status(400);
                return "File path is required";
            }

            String jsonResult;
            try {
                jsonResult = pdfService.parsePdf(filePath);
                response.status(200);
            } catch (Exception e) {
                response.status(500);
                jsonResult = "Error processing PDF: " + e.getMessage();
            }

            return jsonResult;
        }, gson::toJson);
    }
}