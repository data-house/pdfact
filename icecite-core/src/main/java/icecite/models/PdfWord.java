package icecite.models;

/**
 * A word in a PDF document.
 * 
 * @author Claudius Korzen
 */
public interface PdfWord extends PdfElement, HasCharacters, HasText {
  /**
   * The factory to create instances of {@link PdfWord}.
   * 
   * @author Claudius Korzen
   */
  public interface PdfWordFactory {
    /**
     * Creates a PdfWord.
     * 
     * @param page
     *        The page in which this word is located.
     * @param characters
     *        The characters of the word to create.
     * 
     * @return An instance of {@link PdfWord}.
     */
    PdfWord create(PdfPage page, PdfCharacterList characters);
  }
}