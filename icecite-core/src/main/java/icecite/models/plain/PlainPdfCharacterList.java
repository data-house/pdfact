package icecite.models.plain;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import icecite.models.PdfCharacter;
import icecite.models.PdfCharacterList;
import icecite.models.PdfColor;
import icecite.models.PdfFont;
import icecite.utils.counter.FloatCounter;
import icecite.utils.counter.ObjectCounter;
import icecite.utils.geometric.Rectangle.RectangleFactory;

/**
 * A plain implementation of {@link PdfCharacterList}.
 * 
 * @author Claudius Korzen
 */
public class PlainPdfCharacterList extends PlainPdfElementList<PdfCharacter>
    implements PdfCharacterList {
  /**
   * The serial id.
   */
  protected static final long serialVersionUID = 6187582288718513001L;

  /**
   * The most common font of the characters in this list.
   */
  protected PdfFont mostCommonFont;

  /**
   * The characters with the most common font.
   */
  protected Set<PdfCharacter> charsWithMostCommonFont;

  /**
   * The most common color of the characters in this list.
   */
  protected PdfColor mostCommonColor;

  /**
   * The characters with the most common color.
   */
  protected Set<PdfCharacter> charsWithMostCommonColor;

  /**
   * The most common font size of the characters in this list.
   */
  protected float mostCommonFontsize = Float.NaN;

  /**
   * The characters with the most common font size.
   */
  protected Set<PdfCharacter> charsWithMostCommonFontsize;

  /**
   * The average font size.
   */
  protected float averageFontsize = Float.NaN;

  // ==========================================================================
  // Constructors.

  /**
   * Creates a new PdfCharacterList.
   * 
   * @param rectangleFactory
   *        The factory to create instances of {@Rectangle}.
   */
  @AssistedInject
  public PlainPdfCharacterList(RectangleFactory rectangleFactory) {
    this(rectangleFactory, DEFAULT_INITIAL_CAPACITY);
  }

  /**
   * Creates a new PdfCharacterList.
   * 
   * @param rectangleFactory
   *        The factory to create instances of {@Rectangle}.
   * @param initialCapacity
   *        The initial capacity of this list.
   */
  @AssistedInject
  public PlainPdfCharacterList(RectangleFactory rectangleFactory,
      @Assisted int initialCapacity) {
    super(rectangleFactory, initialCapacity);
  }

  // ==========================================================================

  @Override
  public PdfCharacterList getCharacters() {
    return this;
  }

  @Override
  public void setCharacters(PdfCharacterList characters) {
    this.clear();
    this.addAll(characters);
  }

  @Override
  public void addCharacters(PdfCharacterList characters) {
    this.addAll(characters);
  }

  @Override
  public void addCharacter(PdfCharacter character) {
    this.add(character);
  }

  // ==========================================================================

  /**
   * Computes the statistics about the characters in this list.
   */
  protected void computeStatistics() {
    super.computeStatistics();

    // Count the colors, fonts and font sizes.
    ObjectCounter<PdfColor> colorsCounter = new ObjectCounter<>();
    ObjectCounter<PdfFont> fontsCounter = new ObjectCounter<>();
    FloatCounter fontsizesCounter = new FloatCounter();

    for (PdfCharacter character : this) {
      fontsCounter.add(character.getFont());
      colorsCounter.add(character.getColor());
      fontsizesCounter.add(character.getFontSize());
    }

    // Obtain the most common (and average) values.
    this.mostCommonFont = fontsCounter.getMostCommonObject();
    this.mostCommonColor = colorsCounter.getMostCommonObject();
    this.mostCommonFontsize = fontsizesCounter.getMostCommonFloat();
    this.averageFontsize = fontsizesCounter.getAverageFloat();

    this.isStatisticsOutdated = false;
  }

  // ==========================================================================

  @Override
  public List<PdfCharacterList> cut(int index) {
    PdfCharacterView left = new PdfCharacterView(this, 0, index);
    PdfCharacterView right = new PdfCharacterView(this, index, this.size());
    return Arrays.asList(left, right);
  }

  // ==========================================================================

  @Override
  public PdfFont getMostCommonFont() {
    if (this.mostCommonFont == null || this.isStatisticsOutdated) {
      computeStatistics();
    }
    return this.mostCommonFont;
  }

  @Override
  public Set<PdfCharacter> getCharactersWithMostCommonFont() {
    if (this.charsWithMostCommonFont == null || this.isStatisticsOutdated) {
      this.charsWithMostCommonFont = computeCharsWithMostCommonFont();
    }
    return this.charsWithMostCommonFont;
  }

  /**
   * Computes the characters with the most common font.
   * 
   * @return The characters with the most common font.
   */
  protected Set<PdfCharacter> computeCharsWithMostCommonFont() {
    PdfFont mostCommonFont = getMostCommonFont();
    Set<PdfCharacter> characters = new HashSet<>(); // TODO: Choose capacity.
    for (PdfCharacter character : this) {
      if (character.getFont() == mostCommonFont) {
        characters.add(character);
      }
    }
    return characters;
  }

  // ==========================================================================

  @Override
  public PdfColor getMostCommonColor() {
    if (this.mostCommonColor == null || this.isStatisticsOutdated) {
      computeStatistics();
    }
    return this.mostCommonColor;
  }

  @Override
  public Set<PdfCharacter> getCharactersWithMostCommonColor() {
    if (this.charsWithMostCommonColor == null || this.isStatisticsOutdated) {
      this.charsWithMostCommonColor = computeCharsWithMostCommonColor();
    }
    return this.charsWithMostCommonColor;
  }

  /**
   * Computes the characters with the most common color.
   * 
   * @return The characters with the most common color.
   */
  protected Set<PdfCharacter> computeCharsWithMostCommonColor() {
    PdfColor mostCommonColor = getMostCommonColor();
    Set<PdfCharacter> characters = new HashSet<>(); // TODO: Choose capacity.
    for (PdfCharacter character : this) {
      if (character.getColor() == mostCommonColor) {
        characters.add(character);
      }
    }
    return characters;
  }

  // ==========================================================================

  @Override
  public float getAverageFontsize() {
    if (Float.isNaN(this.averageFontsize) || this.isStatisticsOutdated) {
      computeStatistics();
    }
    return this.averageFontsize;
  }

  @Override
  public float getMostCommonFontsize() {
    if (Float.isNaN(this.mostCommonFontsize) || this.isStatisticsOutdated) {
      computeStatistics();
    }
    return this.mostCommonFontsize;
  }

  @Override
  public Set<PdfCharacter> getCharactersWithMostCommonFontsize() {
    if (this.charsWithMostCommonFontsize == null || this.isStatisticsOutdated) {
      this.charsWithMostCommonFontsize = computeCharsWithMostCommonFontsize();
    }
    return this.charsWithMostCommonFontsize;
  }

  /**
   * Computes the characters with the most common font size.
   * 
   * @return The characters with the most common font size.
   */
  protected Set<PdfCharacter> computeCharsWithMostCommonFontsize() {
    float mostCommonFontsize = getMostCommonFontsize();
    Set<PdfCharacter> characters = new HashSet<>(); // TODO: Choose capacity.
    for (PdfCharacter character : this) {
      if (character.getFontSize() == mostCommonFontsize) {
        characters.add(character);
      }
    }
    return characters;
  }

  // ==========================================================================

  @Override
  public String toString() {
    return super.toString();
  }

  @Override
  public boolean equals(Object other) {
    return super.equals(other);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  // ==========================================================================

  /**
   * A view of a PdfCharacterList.
   * 
   * @author Claudius Korzen
   */
  class PdfCharacterView extends PlainPdfCharacterList
      implements PdfCharacterList {
    /**
     * The serial id.
     */
    protected static final long serialVersionUID = 884367879377788123L;

    /**
     * The parent list.
     */
    protected final PdfCharacterList parent;

    /**
     * The offset of this view in the parent list.
     */
    protected final int offset;

    /**
     * The size of this list (the number of elements in the list).
     */
    protected int size;

    // ========================================================================
    // Constructors.

    /**
     * Creates a new view based on the given parent list.
     * 
     * @param parent
     *        The parent list.
     * @param fromIndex
     *        The start index in the parent list.
     * @param toIndex
     *        The end index in the parent list.
     */
    PdfCharacterView(PdfCharacterList parent, int fromIndex, int toIndex) {
      super(PlainPdfCharacterList.this.rectangleFactory);
      this.parent = parent;
      this.offset = fromIndex;
      this.size = toIndex - fromIndex;
    }

    // ========================================================================
    // Don't allow to add elements to views.

    @Override
    public boolean add(PdfCharacter c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, PdfCharacter c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends PdfCharacter> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends PdfCharacter> c) {
      throw new UnsupportedOperationException();
    }

    // ========================================================================
    // Iterators.

    @Override
    public Iterator<PdfCharacter> iterator() {
      return listIterator(0);
    }

    @Override
    public ListIterator<PdfCharacter> listIterator(final int index) {
      return new ListIterator<PdfCharacter>() {
        int cursor = index;

        @Override
        public boolean hasNext() {
          return this.cursor != PdfCharacterView.this.size;
        }

        @Override
        public PdfCharacter next() {
          if (this.cursor >= PdfCharacterView.this.size) {
            throw new NoSuchElementException();
          }
          return PdfCharacterView.this.get(this.cursor++);
        }

        @Override
        public boolean hasPrevious() {
          return this.cursor != 0;
        }

        @Override
        public PdfCharacter previous() {
          if (this.cursor - 1 < 0) {
            throw new NoSuchElementException();
          }
          return PdfCharacterView.this.get(this.cursor--);
        }

        @Override
        public int nextIndex() {
          return this.cursor;
        }

        @Override
        public int previousIndex() {
          return this.cursor - 1;
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }

        @Override
        public void set(PdfCharacter c) {
          throw new UnsupportedOperationException();
        }

        @Override
        public void add(PdfCharacter c) {
          throw new UnsupportedOperationException();
        }
      };
    }

    @Override
    public Spliterator<PdfCharacter> spliterator() {
      throw new UnsupportedOperationException();
    }

    // ========================================================================
    // Getters.

    @Override
    public PdfCharacter get(int index) {
      return this.parent.get(this.offset + index);
    }

    @Override
    public int size() {
      return this.size;
    }

    public boolean isEmpty() {
      return this.size == 0;
    }

    // ========================================================================

    @Override
    public void swap(int i, int j) {
      this.parent.swap(this.offset + i, this.offset + j);
    }

    // ========================================================================

    @Override
    public List<PdfCharacterList> cut(int i) {
      // Create new views.
      int left = this.offset;
      int cut = this.offset + i;
      int right = this.offset + size();
      PdfCharacterView v1 = new PdfCharacterView(this.parent, left, cut);
      PdfCharacterView v2 = new PdfCharacterView(this.parent, cut, right);
      return Arrays.asList(v1, v2);
    }

    // ========================================================================

    @Override
    public String toString() {
      return super.toString();
    }

    @Override
    public boolean equals(Object other) {
      return super.equals(other);
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }
  }
}