/**
 * @authors Bala Subrahmanyam Kambala, Daniel William DaCosta
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 * @descriptrion A class to wrap up argument handling..
 */
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;

public class ArgumentHandler {

    Options options;
    String syntax;
    String header;
    String footer;

    public ArgumentHandler(String _syntax, String _header, String _footer) {
        options = new Options();
        syntax = _syntax;
        header = _header;
        footer = _footer;
    }

    public void addOption(Option p) {
        options.addOption(p);
    }

    public void addOption(String a, String b, boolean c, String d) {
        options.addOption(a, b, c, d);
    }

    public CommandLine parse(String[] args) {
        CommandLine commandLine;
        try {
            CommandLineParser parser = new BasicParser();
            commandLine = parser.parse(options, args);
            return commandLine;
        } catch (Exception e) {
            usage("\nImproper Usage!\n");
            System.exit(1);
            return null;
        }
    }

    public void usage(String msg) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setWidth(80);
        helpFormatter.printHelp(syntax, msg + header, options, footer);
    }

}
