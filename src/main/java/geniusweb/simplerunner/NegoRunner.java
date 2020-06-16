package geniusweb.simplerunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import geniusweb.events.ProtocolEvent;
import geniusweb.protocol.CurrentNegoState;
import geniusweb.protocol.NegoProtocol;
import geniusweb.protocol.NegoSettings;
import geniusweb.protocol.NegoState;
import geniusweb.protocol.partyconnection.ProtocolToPartyConnFactory;
import tudelft.utilities.logging.Reporter;

/**
 * A simple tool to run a negotiation stand-alone, without starting the servers.
 * All referred files and classes need to be stored locally (or be in the
 * dependency list if you use maven).
 * 
 */
public class NegoRunner implements Runnable {
	private final NegoSettings settings;
	private final NegoProtocol protocol;
	private final ProtocolToPartyConnFactory connectionfactory;
	protected final Reporter log;
	private final static ObjectMapper jackson = new ObjectMapper();

	public NegoRunner(NegoSettings settings,
			ProtocolToPartyConnFactory connectionfactory, Reporter logger) {
		if (settings == null || connectionfactory == null) {
			throw new NullPointerException("Arguments must be not null");
		}
		this.settings = settings;
		this.log = logger;
		this.protocol = settings.getProtocol(log);
		this.connectionfactory = connectionfactory;
	}

	@Override
	public void run() {
		protocol.addListener(evt -> handle(evt));
		protocol.start(connectionfactory);
	}

	private void handle(ProtocolEvent evt) {
		if (evt instanceof CurrentNegoState && ((CurrentNegoState) evt)
				.getState().isFinal(System.currentTimeMillis())) {
			stop();
		}
	}

	protected void stop() {
		Level level = protocol.getState().getError() == null ? Level.INFO
				: Level.WARNING;
		logFinal(level, protocol.getState());
	}

	/**
	 * Separate so that we can intercept this when mocking, as this will crash
	 * on mocks
	 * 
	 * @param level
	 * @param state
	 */
	protected void logFinal(Level level, NegoState state) {
		try {
			log.log(level, "protocol ended normally: "
					+ jackson.writeValueAsString(protocol.getState()));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			showusage();
			return;
		}
		String serialized = new String(Files.readAllBytes(Paths.get(args[0])),
				StandardCharsets.UTF_8);
		NegoSettings settings = jackson.readValue(serialized,
				NegoSettings.class);

		NegoRunner runner = new NegoRunner(settings,
				new ClassPathConnectionFactory(), new StdOutReporter());
		runner.run();
	}

	private static void showusage() {
		System.err.println("GeniusWeb stand-alone runner. ");
		System.err.println("first argument should be <settings.json>.");
		System.err.println(
				"The settings.json file should contain the NegoSettings.");
		System.err.println(
				"See the settings.json example file and the GeniusWeb wiki pages. ");

	}
}

class StdOutReporter implements Reporter {

	@Override
	public void log(Level arg0, String arg1) {
		System.out.println(arg0 + ":" + arg1);
	}

	@Override
	public void log(Level arg0, String arg1, Throwable arg2) {
		System.out.println(arg0 + ">" + arg1);
	}

}
