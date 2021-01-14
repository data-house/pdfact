package pdfact.cli;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.helper.HelpScreenException;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import pdfact.cli.model.ExtractionUnit;
import pdfact.cli.model.SerializeFormat;
import pdfact.cli.util.exception.PdfActParseCommandLineException;
import pdfact.core.PdfActCoreSettings;
import pdfact.core.model.LogLevel;
import pdfact.core.model.SemanticRole;
import pdfact.core.util.exception.PdfActException;

/**
 * The command line interface of PdfAct.
 *
 * @author Claudius Korzen
 */
public class PdfActCli {
  /**
   * Starts this command line interface.
   *
   * @param args The command line arguments.
   */
  protected void start(String[] args) {
    int statusCode = 0;
    String errorMessage = null;
    Throwable cause = null;

    // Create the command line argument parser.
    PdfActCommandLineParser parser = new PdfActCommandLineParser();

    try {
      // Parse the command line arguments.
      parser.parseArgs(args);

      // Create an instance of PdfAct.
      PdfAct pdfAct = new PdfAct();

      // Pass the log level.
      pdfAct.setLogLevel(LogLevel.getLogLevel(parser.getLogLevel()));

      // Pass the serialization format if there is any.
      String serializationFormatStr = parser.getSerializationFormat();
      if (serializationFormatStr != null) {
        pdfAct.setSerializationFormat(SerializeFormat.fromString(serializationFormatStr));
      }

      // Pass the serialization target path.
      String serializationPathStr = parser.getSerializationPath();
      if (serializationPathStr != null) {
        pdfAct.setSerializationPath(Paths.get(serializationPathStr));
      } else {
        pdfAct.setSerializationStream(System.out);
      }

      // Pass the target of the visualization.
      String visualizationPathStr = parser.getVisualizationPath();
      if (visualizationPathStr != null) {
        pdfAct.setVisualizationPath(Paths.get(visualizationPathStr));
      }

      // Pass the chosen text unit.
      List<String> extractionUnits = parser.getExtractionUnits();
      if (extractionUnits != null) {
        pdfAct.setExtractionUnits(ExtractionUnit.fromStrings(extractionUnits));
      }

      // Compute the semantic roles to include on serialization & visualization.
      Set<String> roles = new HashSet<>();
      List<String> semanticRolesToInclude = parser.getSemanticRolesToInclude();
      if (semanticRolesToInclude != null) {
        roles.addAll(parser.getSemanticRolesToInclude());
      }
      List<String> semanticRolesToExclude = parser.getSemanticRolesToExclude();
      if (semanticRolesToExclude != null) {
        roles.removeAll(parser.getSemanticRolesToExclude());
      }
      pdfAct.setSemanticRoles(SemanticRole.fromStrings(roles));

      // Run PdfAct.
      pdfAct.parse(parser.getPdfPath());
    } catch (PdfActException e) {
      statusCode = e.getExitCode();
      errorMessage = e.getMessage();
      cause = e.getCause();
    }

    if (statusCode != 0) {
      // Print the error message (regardless of the log level).
      System.err.println(errorMessage);
      // Print the stack trace if there is any and debugging is enabled.
      if (cause != null && LogManager.getRootLogger().isDebugEnabled()) {
        cause.printStackTrace();
      }
    }

    System.exit(statusCode);
  }

  // ==============================================================================================

  /**
   * The main method to run the command line interface.
   *
   * @param args The command line arguments.
   */
  public static void main(String[] args) {
    new PdfActCli().start(args);
  }

  // ==============================================================================================

  /**
   * A parser to parse the command line arguments.
   *
   * @author Claudius Korzen
   */
  class PdfActCommandLineParser {
    /**
     * The command line argument parser.
     */
    protected ArgumentParser parser;

    // ============================================================================================

    /**
     * The name of the option to define the path to the PDF file to process.
     */
    protected static final String PDF_PATH = "pdfPath";

    /**
     * The path to the PDF file to process.
     */
    @Arg(dest = PDF_PATH)
    protected String pdfPath;

    // ============================================================================================

    /**
     * The name of the option to define the target path for the serialization.
     */
    protected static final String SERIALIZE_PATH = "serializationPath";

    /**
     * The target path for the serialization.
     */
    @Arg(dest = SERIALIZE_PATH)
    protected String serializePath;

    // ============================================================================================

    /**
     * The name of the option to define the serialization format.
     */
    protected static final String SERIALIZE_FORMAT = "format";

    /**
     * The serialization format.
     */
    @Arg(dest = SERIALIZE_FORMAT)
    protected String serializeFormat = "txt";

    // ============================================================================================

    /**
     * The name of the option to define the target path for the visualization.
     */
    protected static final String VISUALIZATION_PATH = "visualize";

    /**
     * The target path for the visualization.
     */
    @Arg(dest = VISUALIZATION_PATH)
    protected String visualizationPath;

    // ============================================================================================

    /**
     * The name of the option to define the units to extract.
     */
    protected static final String EXTRACTION_UNITS = "units";

    /**
     * The text unit to extract.
     */
    @Arg(dest = EXTRACTION_UNITS)
    protected List<String> extractionUnits = Arrays.asList("paragraphs");

    // ============================================================================================

    /**
     * The name of the option to define the semantic roles to include (text blocks with a semantic
     * role that is not included won't be extracted).
     */
    protected static final String INCLUDE_SEMANTIC_ROLES = "include-roles";

    /**
     * The semantic role(s) to include (text blocks with a semantic role that is not included won't
     * be extracted).
     */
    @Arg(dest = INCLUDE_SEMANTIC_ROLES)
    protected List<String> semanticRolesToInclude = new ArrayList<>(SemanticRole.getNames());

    // ============================================================================================

    /**
     * The name of the option to define the semantic roles to exclude (text blocks with a semantic
     * role that is excluded won't be extracted).
     */
    protected static final String EXCLUDE_SEMANTIC_ROLES = "exclude-roles";

    /**
     * The semantic role(s) to exclude (text blocks with a semantic role that is excluded won't be
     * extracted).
     */
    @Arg(dest = EXCLUDE_SEMANTIC_ROLES)
    protected List<String> semanticRolesToExclude = new ArrayList<>();

    // ============================================================================================

    /**
     * The name of the option to enable log output.
     */
    protected static final String LOG_LEVEL = "debug";

    /**
     * The log level.
     */
    @Arg(dest = LOG_LEVEL)
    protected int logLevel = PdfActCoreSettings.DEFAULT_LOG_LEVEL.getIntLevel();

    // ============================================================================================

    /**
     * Creates a new command line argument parser.
     */
    public PdfActCommandLineParser() {
      this.parser = ArgumentParsers.newFor("pdfact")
        .terminalWidthDetection(false)
        .defaultFormatWidth(100).build();
      this.parser.description("A tool to extract the text, structure and layout from PDF files.");

      // Add an option to define the path to the PDF file to be processed.
      this.parser.addArgument(PDF_PATH).dest(PDF_PATH)
        .required(true)
        .metavar("<pdf-file>")
        .help("The path to the PDF file to be processed.");

      // Add an argument to define the target path to the output file.
      this.parser.addArgument(SERIALIZE_PATH).dest(SERIALIZE_PATH)
        .required(false).nargs("?")
        .metavar("<output-file>")
        .help("The path to the file to which the extraction output should be written.\n"
            + "If not specified, the output will be written to stdout.");

      // Add an option to define the output format.
      Set<String> choices = SerializeFormat.getNames();
      String choicesStr = String.join(", ", choices);
      String defaultStr = this.serializeFormat;
      this.parser.addArgument("--" + SERIALIZE_FORMAT).dest(SERIALIZE_FORMAT)
        .required(false)
        .metavar("<format>")
        .choices(choices)
        .setDefault(this.serializeFormat)
        .help("The output format.\n" 
            + "- Available options: " + choicesStr + ".\n" 
            + "- Default: \"" + defaultStr + "\".\n"
            + "In case of txt, the text elements will be extracted as plain text, in the "
            + "format: one text element per line. In case of xml or json, the text elements "
            + "will be extracted together with their layout information, e.g., their positions "
            + "in the PDF file, their fonts and their colors.");

      // Add an option to define the text units.
      choicesStr = String.join(", ", ExtractionUnit.getPluralNames());
      defaultStr = String.join(",", this.extractionUnits);
      this.parser.addArgument("--" + EXTRACTION_UNITS).dest(EXTRACTION_UNITS)
        .required(false)
        .metavar("<units>")
        .action(new SplitAtDelimiterAction(","))
        .setDefault(this.extractionUnits)
        .help("The granularity in which the elements should be extracted in the output, "
            + "separated by \",\".\n" 
            + "- Available options: " + choicesStr + ".\n" 
            + "- Default: \"" + defaultStr + "\".\n"
            + "For example, when the script is called with the option \"--" + EXTRACTION_UNITS
            + " words\", the output will be broken down by words, that is: the text and layout "
            + "information are provided word-wise.");

      // Add an argument to define the semantic role(s) to include.
      choicesStr = String.join(", ", SemanticRole.getNames());
      defaultStr = String.join(",", this.semanticRolesToInclude);
      this.parser.addArgument("--" + INCLUDE_SEMANTIC_ROLES).dest(INCLUDE_SEMANTIC_ROLES)
        .required(false)
        .metavar("<roles>")
        .action(new SplitAtDelimiterAction(","))
        .setDefault(this.semanticRolesToInclude)
        .help("The list of the semantic roles to include, separated by \",\".\n"
            + "- Available options: " + choicesStr + ".\n"
            + "- Default: \"" + defaultStr + "\".\n"
            + "Only the elements with a semantic role that is included in this list will be "
            + "extracted. All other elements won't be extracted. For example, if the script is "
            + "called with the option \"--" + INCLUDE_SEMANTIC_ROLES + " headings,body\", the "
            + "output will only contain the text elements (and optionally, the layout "
            + "information) belonging to a heading or a body text paragraph. Per default, all "
            + "available semantic roles are included, that is: all elements will be extracted, "
            + "regardless of the semantic roles.\n"
            + "NOTE: The detection of the semantic roles of the text elements is still in an "
            + "experimental state. So don't expect the semantic roles to be highly accurate.");

      // Add an argument to define the semantic role(s) to exclude.
      defaultStr = String.join(",", this.semanticRolesToExclude);
      this.parser.addArgument("--" + EXCLUDE_SEMANTIC_ROLES).dest(EXCLUDE_SEMANTIC_ROLES)
        .required(false)
        .metavar("<roles>")
        .action(new SplitAtDelimiterAction(","))
        .setDefault(this.semanticRolesToExclude)
        .help("The list of the semantic roles to exclude, separated by \",\".\n"
            + "- Available options: " + choicesStr + ".\n"
            + "- Default: \"" + defaultStr + "\".\n"
            + "All elements with a semantic role that is included in this list won't be "
            + "extracted. For example, if the script is called with the option \"--"
            + EXCLUDE_SEMANTIC_ROLES + " body\", the text (and layout information) belonging to "
            + "body text paragraphs won't be extracted. Per default, no semantic role is "
            + "excluded, that is: all elements will be extracted.\n"
            + "NOTE: The detection of the semantic roles of the text elements is still in an "
            + "experimental state. So don't expect the semantic roles to be highly accurate.");

      // Add an argument to define the target path for the visualization.
      this.parser.addArgument("--" + VISUALIZATION_PATH).dest(VISUALIZATION_PATH)
        .required(false)
        .type(String.class)
        .metavar("<path>")
        .help("The path to a file (ending in *.pdf) to which a visualization of the extracted "
            + "elements (that is: the original PDF file enriched which bounding boxes around the "
            + "extracted elements and the semantic roles in case the unit is \"paragraph\") "
            + "should be written to. The file doesn't have to be existent before. If not "
            + "specified, no such visualization will be created.");

      // Add an argument to define the log level.
      StringBuilder choiceStr = new StringBuilder();
      int i = 0;
      for (LogLevel level : LogLevel.getLogLevels()) {
        choiceStr.append(level.getIntLevel() + " (= " + level + ")");
        choiceStr.append(i < LogLevel.getLogLevels().size() - 1 ? ", " : "");
        i++;
      }

      LogLevel debugLevel = LogLevel.DEBUG;
      this.parser.addArgument("--" + LOG_LEVEL).dest(LOG_LEVEL)
        .required(false)
        .metavar("<level>")
        .type(Integer.class)
        .action(new StoreDefaultArgumentAction(debugLevel.getIntLevel()))
        .setDefault(this.logLevel)
        .help("The verbosity of the log messages.\n" 
            + "- Available options: " + choiceStr.toString() + ".\n" 
            + "- Default: " + this.logLevel + ".\n"
            + "This defines the minimum level of severity required for a message to be logged.");
    }

    /**
     * Parses the given command line arguments.
     *
     * @param args The command line arguments to parse.
     *
     * @throws PdfActException If parsing the command line arguments fails.
     */
    public void parseArgs(String[] args) throws PdfActException {
      try {
        this.parser.parseArgs(args, this);
      } catch (HelpScreenException e) {
        // Set the status code to 0, such that no error message is shown.
        throw new PdfActParseCommandLineException(null, 0, e);
      } catch (ArgumentParserException e) {
        String message = e.getMessage() + "\n\n" + getUsage();
        throw new PdfActParseCommandLineException(message, e);
      }
    }

    /**
     * Returns the usage for this command line parser.
     *
     * @return The usage for this command line parser.
     */
    public String getUsage() {
      return this.parser.formatUsage();
    }

    /**
     * Returns the help for this command line parser.
     *
     * @return The help for this command line parser.
     */
    public String getHelp() {
      return this.parser.formatHelp();
    }

    // ============================================================================================
    // Getters methods.

    /**
     * Returns true, if a path to a PDF file is given; false otherwise.
     *
     * @return True, if a path to a PDF file is given; false otherwise.
     */
    public boolean hasPdfPath() {
      return this.pdfPath != null;
    }

    /**
     * Returns the path to the PDF file.
     *
     * @return The path to the PDF file.
     */
    public String getPdfPath() {
      return this.pdfPath;
    }

    // ============================================================================================

    /**
     * Returns true, if a target path for the serialization is given; false otherwise.
     *
     * @return True, if a target path for the serialization is given; false otherwise.
     */
    public boolean hasSerializationPath() {
      return this.serializePath != null;
    }

    /**
     * Returns the target path for the serialization.
     *
     * @return The target path for the serialization.
     */
    public String getSerializationPath() {
      return this.serializePath;
    }

    // ============================================================================================

    /**
     * Returns true, if an serialization format is given.
     *
     * @return True, if an serialization format is given.
     */
    public boolean hasSerializationFormat() {
      return this.serializeFormat != null;
    }

    /**
     * Returns the serialization format.
     *
     * @return The serialization format.
     */
    public String getSerializationFormat() {
      return this.serializeFormat;
    }

    // ============================================================================================

    /**
     * Returns true, if a target path for the visualization is given.
     *
     * @return True, if a target path for the visualization is given.
     */
    public boolean hasVisualizationPath() {
      return this.visualizationPath != null;
    }

    /**
     * Returns the target path for the visualization.
     *
     * @return The target path for the visualization.
     */
    public String getVisualizationPath() {
      return this.visualizationPath;
    }

    // ============================================================================================

    /**
     * Returns true, if there is a text unit given.
     *
     * @return True, if there is a text unit given. False otherwise.
     */
    public boolean hasTextUnit() {
      return this.extractionUnits != null;
    }

    /**
     * Returns the units to extract.
     *
     * @return The units to extract.
     */
    public List<String> getExtractionUnits() {
      return this.extractionUnits;
    }

    // ============================================================================================

    /**
     * Returns the list of semantic role(s) to include.
     *
     * @return The list of semantic role(s) to include.
     */
    public List<String> getSemanticRolesToInclude() {
      return this.semanticRolesToInclude;
    }

    /**
     * Returns the list of semantic role(s) to exclude.
     *
     * @return The list of semantic role(s) to exclude.
     */
    public List<String> getSemanticRolesToExclude() {
      return this.semanticRolesToExclude;
    }

    // ============================================================================================

    /**
     * Returns the log level.
     *
     * @return The log level.
     */
    public int getLogLevel() {
      return this.logLevel;
    }
  }

  /**
   * Argument action to store argument value or a given default value if the argument value is null.
   */
  private static class StoreDefaultArgumentAction implements ArgumentAction {
    /**
     * The default value to store if the argument value is null.
     */
    protected Object defaultValue;

    /**
     * Creates a new StoreDefaultArgumentAction.
     *
     * @param defaultValue The default value to store if the argument value is null.
     */
    public StoreDefaultArgumentAction(Object defaultValue) {
      this.defaultValue = defaultValue;
    }

    @Override
    public void run(ArgumentParser parser, Argument arg, Map<String, Object> attrs, String flag,
            Object value) throws ArgumentParserException {
      if (value == null) {
        attrs.put(arg.getDest(), this.defaultValue);
      } else {
        attrs.put(arg.getDest(), value);
      }
    }

    @Override
    public void onAttach(Argument arg) {
    }

    @Override
    public boolean consumeArgument() {
      return true;
    }
  }

  /**
   * Argument action to split a given string at a given delimiter and to store a list of all
   * resulting substrings.
   */
  private static class SplitAtDelimiterAction implements ArgumentAction {
    /**
     * The delimiter to split at.
     */
    protected String delimiter;

    /**
     * Creates a new SplitAtDelimiterAction.
     *
     * @param delimiter The delimiter to split at.
     */
    public SplitAtDelimiterAction(String delimiter) {
      this.delimiter = delimiter;
    }

    @Override
    public void run(ArgumentParser parser, Argument arg, Map<String, Object> attrs, String flag,
            Object value) throws ArgumentParserException {
      if (value != null) {
        attrs.put(arg.getDest(), Arrays.asList(((String) value).split(delimiter)));
      }
    }

    @Override
    public void onAttach(Argument arg) {
    }

    @Override
    public boolean consumeArgument() {
      return true;
    }
  }
}