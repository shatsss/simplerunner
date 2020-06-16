package geniusweb.simplerunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.databind.ObjectMapper;

import geniusweb.protocol.NegoSettings;
import tudelft.utilities.logging.ReportToLogger;
import tudelft.utilities.logging.Reporter;

/**
 * E2E test doing a full real run of a session with real agents and protocol.
 * NOTICE This will exercise a full system to errors here may point far outside
 * this module.
 */
@RunWith(Parameterized.class)
public class SessionRunnerE2ETest {
	private final ObjectMapper jackson = new ObjectMapper();
	private NegoRunner runner;
	private Reporter logger = new ReportToLogger("test");
	private String filename;

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays
				.asList(new Object[][] { { "src/test/resources/settings.json" },
						{ "src/test/resources/settings2.json" }});
	}

	public SessionRunnerE2ETest(String file) {
		this.filename = file;
	}

	@Before
	public void before() throws IOException {
		System.out.println("Running from file " + filename);
		String serialized = new String(Files.readAllBytes(Paths.get(filename)),
				StandardCharsets.UTF_8);
		NegoSettings settings = jackson.readValue(serialized,
				NegoSettings.class);

		runner = new NegoRunner(settings, new ClassPathConnectionFactory(),
				logger);

	}

	@Test
	public void smokeTest() throws IOException {
	}

	@Test
	public void runTest() throws IOException {
		runner.run();
	}

	@Test
	public void runTestMainFunction() throws IOException {
		NegoRunner.main(new String[] { filename });
	}
}
