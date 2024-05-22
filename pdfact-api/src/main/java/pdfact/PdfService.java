package pdfact;

import pdfact.cli.PdfAct;
import pdfact.cli.model.ExtractionUnit;
import pdfact.cli.pipes.serialize.PdfJsonSerializer;
import pdfact.core.model.Document;
import pdfact.core.model.SemanticRole;
import pdfact.core.util.exception.PdfActException;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.List;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;


public class PdfService {

    public String parsePdf(String fileUrl, String unitSelected, List<String> rolesSelected) throws PdfActException {
        
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        PdfAct pdfAct = new PdfAct();

        Set<ExtractionUnit> unit = new HashSet<>();
        Set<SemanticRole> roles = new HashSet<>();
        
        if (unitSelected != null) {
            unit = getExtractionUnitSet(unitSelected);
            pdfAct.setExtractionUnits(unit);
        }else{
            unit.add(ExtractionUnit.PARAGRAPH);
        }
        
        if (rolesSelected != null) {
            roles = convertToSemanticRoles(rolesSelected);
            pdfAct.setSemanticRoles(roles);
        }else{
            for (SemanticRole role : SemanticRole.values()) {
                roles.add(role);
            }
        }
        
        String jsonString;

        try{
            Path tempFile = downloadFileFromUrl(fileUrl);
            Document pdf = pdfAct.parse(tempFile.toString());
            PdfJsonSerializer serializer = new PdfJsonSerializer(unit, roles);
            byte[] serializedPdf = serializer.serialize(pdf);
            jsonString = new String(serializedPdf, StandardCharsets.UTF_8);
        
        } catch (PdfActException | IOException e){
            jsonString = "Errore: " + e.getMessage();
            e.printStackTrace();
        }

        return jsonString;

    }

    private Path downloadFileFromUrl(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        Path tempFile = Files.createTempFile("temp", ".pdf");
        try (InputStream in = url.openStream()) {
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile;
    }

    public Set<ExtractionUnit> getExtractionUnitSet(String unit) {
        Set<ExtractionUnit> unitSelected = new HashSet<>();
        try {
            ExtractionUnit extractionUnit = ExtractionUnit.valueOf(unit.toUpperCase());
            unitSelected.add(extractionUnit);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("L'unità di estrazione '" + unit + "' non è valida.", e);
        }
        return unitSelected;
    }

    public Set<SemanticRole> convertToSemanticRoles(List<String> rolesList) {
        Set<SemanticRole> roles = new HashSet<>();
        for (String role : rolesList) {
            try {
                SemanticRole semanticRole = SemanticRole.valueOf(role.toUpperCase());
                roles.add(semanticRole);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Il ruolo '" + role + "' non è valido. Ignorato.", e);
            }
        }
        return roles;
    }

}
