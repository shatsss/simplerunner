package geniusweb.simplerunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.logging.Level;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import geniusweb.protocol.CurrentNegoState;
import geniusweb.protocol.NegoState;
import geniusweb.protocol.ProtocolException;
import geniusweb.protocol.partyconnection.ProtocolToPartyConnFactory;
import geniusweb.protocol.session.SessionProtocol;
import geniusweb.protocol.session.SessionSettings;
import geniusweb.protocol.session.SessionState;
import tudelft.utilities.listener.Listener;
import tudelft.utilities.logging.ReportToLogger;
import tudelft.utilities.logging.Reporter;

public class SessionRunnerTest {
	private static final ProtocolException PROTOCOL_EXC = new ProtocolException(
			"fake protocol exception", "test");
	private CurrentNegoState finishedEvent;
	private final long NOW = 1000;
	private final Reporter logger = new ReportToLogger("test");

	@Before
	public void before() {
		finishedEvent = mock(CurrentNegoState.class);
		SessionState finishedstate = mock(SessionState.class);
		when(finishedstate.isFinal(anyLong())).thenReturn(true);
		when(finishedEvent.getState()).thenReturn(finishedstate);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void smokeTest() {
		new NegoRunner(mock(SessionSettings.class),
				mock(ProtocolToPartyConnFactory.class), logger);
	}

	@Test
	public void testRun() {
		SessionSettings settings = mock(SessionSettings.class);
		SessionProtocol protocol = mock(SessionProtocol.class);
		when(settings.getProtocol(logger)).thenReturn(protocol);
		@SuppressWarnings("unchecked")
		ProtocolToPartyConnFactory factory = mock(
				ProtocolToPartyConnFactory.class);

		NegoRunner runner = spy(new NegoRunner(settings, factory, logger));
		runner.run();

		verify(protocol, times(1)).addListener(any());
		verify(protocol, times(1)).start(factory);

		verify(runner, times(0)).stop();

	}

	@Test
	public void testStopNormally() {
		SessionSettings settings = mock(SessionSettings.class);
		SessionProtocol protocol = mock(SessionProtocol.class);
		when(settings.getProtocol(any())).thenReturn(protocol);
		SessionState state = mock(SessionState.class);
		when(state.getError()).thenReturn(null);
		when(protocol.getState()).thenReturn(state);
		Reporter logger = mock(Reporter.class);
		@SuppressWarnings("unchecked")
		ProtocolToPartyConnFactory factory = mock(
				ProtocolToPartyConnFactory.class);
		NegoRunner runner = spy(new NegoRunner(settings, factory, logger) {
			@Override
			protected void logFinal(Level level, NegoState state) {
				log.log(level, state.toString());
			}
		});
		ArgumentCaptor<Listener> listener = ArgumentCaptor
				.forClass(Listener.class);
		runner.run();

		verify(protocol, times(1)).addListener(listener.capture());

		// make the finixhed event.... too complex...
		listener.getValue().notifyChange(finishedEvent);
		verify(runner, times(1)).stop();
		verify(logger, times(1)).log(eq(Level.INFO), any(String.class));

	}

	@Test
	public void testStopWithError() throws InterruptedException {
		Reporter logger = mock(Reporter.class);
		SessionSettings settings = mock(SessionSettings.class);
		SessionProtocol protocol = mock(SessionProtocol.class);
		when(settings.getProtocol(any())).thenReturn(protocol);
		SessionState state = mock(SessionState.class);
		when(state.getError()).thenReturn(PROTOCOL_EXC);
		when(protocol.getState()).thenReturn(state);
		@SuppressWarnings("unchecked")
		ProtocolToPartyConnFactory factory = mock(
				ProtocolToPartyConnFactory.class);
		NegoRunner runner = spy(new NegoRunner(settings, factory, logger) {
			@Override
			protected void logFinal(Level level, NegoState state) {
				log.log(level, state.toString());
			}
		});
		ArgumentCaptor<Listener> listener = ArgumentCaptor
				.forClass(Listener.class);
		runner.run();

		verify(protocol, times(1)).addListener(listener.capture());

		listener.getValue().notifyChange(finishedEvent);
		verify(runner, times(1)).stop();
		// check that the protocol error is logged properly
		verify(logger, times(1)).log(eq(Level.WARNING), any(String.class));
	}

}
