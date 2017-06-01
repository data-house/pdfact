package icecite.models;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * A PDF document.
 * 
 * @author Claudius Korzen
 */
public interface PdfDocument
    extends HasCharacters, HasFigures, HasShapes, HasTextLines, HasParagraphs {
  /**
   * Returns the pages of this PDF document.
   * 
   * @return The pages of this PDF document.
   */
  List<PdfPage> getPages();

  /**
   * Sets the pages of this PDF document.
   * 
   * @param pages
   *        The list of pages to set.
   */
  void setPages(List<PdfPage> pages);

  /**
   * Adds the given page to this PDF document.
   * 
   * @param page
   *        The page to add.
   */
  void addPage(PdfPage page);

  // ==========================================================================

  /**
   * Returns the file as a File object on which the PDF document is based on.
   * 
   * @return The file as a File object on which the PDF document is based on.
   */
  File getFile();

  /**
   * Sets the file on which the PDF document is based on.
   * 
   * @param file
   *        The file on which the PDF document is based on.
   */
  void setFile(File file);

  // ==========================================================================

  /**
   * Returns the file as Path object on which the PDF document is based on.
   * 
   * @return The file as Path object on which the PDF document is based on.
   */
  Path getPath();

  /**
   * Returns the file on which the PDF document is based on.
   * 
   * @param path
   *        The file on which the PDF document is based on.
   */
  void setPath(Path path);

  // ==========================================================================

  /**
   * The factory to create instances of {@link PdfDocument}.
   * 
   * @author Claudius Korzen
   */
  public interface PdfDocumentFactory {
    /**
     * Creates a PdfDocument.
     * 
     * @param pdf
     *        The file on which the PDF document is based on.
     * 
     * @return An instance of {@link PdfDocument}.
     */
    PdfDocument create(File pdf);

    /**
     * Creates a PdfDocument.
     * 
     * @param pdf
     *        The file on which the PDF document is based on.
     * 
     * @return An instance of {@link PdfDocument}.
     */
    PdfDocument create(Path pdf);
  }
}