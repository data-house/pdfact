package pdfact.api;

import pdfact.cli.PdfAct;
import pdfact.cli.model.ExtractionUnit;
import pdfact.cli.pipes.serialize.PdfJsonSerializer;
import pdfact.core.model.Document;
import pdfact.core.model.SemanticRole;
import pdfact.core.util.exception.PdfActException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * The service to process pdf file and extract text.
 */
public class PdfService {

    /**
     * Download a pdf file and create a json representation of its content.
     *
     * @param fileUrl:       The url to access the pdf file.
     * @param unitSelected:  The unit to split text on (e.g., paragraphs, words, characters, etc.).
     * @param rolesSelected: The roles to extract (e.g., body, title, etc.).
     * @throws IOException:              If the file download/load goes wrong.
     * @throws PdfActException:          If the pdf processing or text extraction goes wrong.
     * @throws IllegalArgumentException: If wrong roles or units are passed by.
     * @return: A json representation of the extracted text.
     */
    public String parsePdf(String fileUrl, String unitSelected, List<String> rolesSelected) throws IOException, PdfActException, IllegalArgumentException {
        PdfAct pdfAct = new PdfAct();
        String jsonString;
        Set<ExtractionUnit> unit = new HashSet<>();
        Set<SemanticRole> roles;

        if (unitSelected != null) {
            unit = getExtractionUnitSet(unitSelected);
            pdfAct.setExtractionUnits(unit);
        } else {
            unit.add(ExtractionUnit.PARAGRAPH);
        }
        if (rolesSelected != null) {
            roles = convertToSemanticRoles(rolesSelected);
            pdfAct.setSemanticRoles(roles);
        } else {
            roles = new HashSet<>(Arrays.asList(SemanticRole.values()));
        }

        Path tempFile = downloadFileFromUrl(fileUrl);
        Document pdf = pdfAct.parse(tempFile.toString());
        PdfJsonSerializer serializer = new PdfJsonSerializer(unit, roles);
        byte[] serializedPdf = serializer.serialize(pdf);
        String jsonString = new String(serializedPdf, StandardCharsets.UTF_8);

        return jsonString;

    }

    /**
     * Download a pdf file.
     *
     * @param fileUrl: The url to access the pdf file.
     * @throws IOException: If the file download/load goes wrong.
     * @return: The path to the downloaded pdf file.
     */
    private Path downloadFileFromUrl(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        Path tempFile = Files.createTempFile("temp", ".pdf");
        try (InputStream in = url.openStream()) {
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile;
    }

    /**
     * Validate the given unit.
     *
     * @param unit: The unit to split text on (e.g., paragraphs, words, characters, etc.).
     * @throws IllegalArgumentException: If wrong units are passed by.
     * @return: The validated unit.
     */
    public Set<ExtractionUnit> getExtractionUnitSet(String unit) throws IllegalArgumentException {
        Set<ExtractionUnit> unitSelected = new HashSet<>();
        try {
            ExtractionUnit extractionUnit = ExtractionUnit.valueOf(unit.toUpperCase());
            unitSelected.add(extractionUnit);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("The extraction unit `" + unit + "` is not valid.", e);
        }
        return unitSelected;
    }

    /**
     * Validate the list of roles.
     *
     * @param rolesList: The roles to extract (e.g., body, title, etc.).
     * @throws IllegalArgumentException: If wrong roles are passed by.
     * @return: The validated list of roles.
     */
    public Set<SemanticRole> convertToSemanticRoles(List<String> rolesList) throws IllegalArgumentException {
        Set<SemanticRole> roles = new HashSet<>();
        for (String role : rolesList) {
            try {
                SemanticRole semanticRole = SemanticRole.valueOf(role.toUpperCase());
                roles.add(semanticRole);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("The role `" + role + "` is not valid.", e);
            }
        }
        return roles;
    }

}
