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

public class PdfService {
    public String parsePdf(String filePath) throws PdfActException {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        Document pdf = new PdfAct().parse(filePath);

        Set<ExtractionUnit> units = new HashSet<>();
        units.add(ExtractionUnit.PARAGRAPH);

        Set<SemanticRole> roles = new HashSet<>();
        roles.add(SemanticRole.TITLE);
        roles.add(SemanticRole.HEADING);
        roles.add(SemanticRole.PAGE_HEADER);
        roles.add(SemanticRole.PAGE_FOOTER);
        roles.add(SemanticRole.BODY_TEXT);

        PdfJsonSerializer serializer = new PdfJsonSerializer(units, roles);
        return new String(serializer.serialize(pdf), StandardCharsets.UTF_8);
    }
}
