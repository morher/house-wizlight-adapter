package net.morher.house.wiz.bulb;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import net.morher.house.api.subscription.Subscription;
import net.morher.house.test.schedule.TestHouseScheduler;
import net.morher.house.wiz.config.WizOptions;
import net.morher.house.wiz.udp.WizBulbListener;
import net.morher.house.wiz.udp.WizDeviceInfo;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class WizBulbManagerTest {
  private static final InetAddress BROADCAST_ADDR = ip("127.0.0.1");

  private static final WizDeviceInfo BULB1 =
      WizDeviceInfo.builder().mac("001122334455").address(BROADCAST_ADDR).build();

  private static final WizBulbState STATE_POWER_OFF = WizBulbState.builder().powerOn(false).build();

  private static final WizBulbState STATE_POWER_ON = WizBulbState.builder().powerOn(true).build();

  private final TestHouseScheduler scheduler = TestHouseScheduler.get();
  private final WizBulbOperations operations = mock(WizBulbOperations.class);
  private final Subscription listenerSubscription = mock(Subscription.class);
  private final WizBulbManager manager;
  private final WizBulbListener listener;

  public WizBulbManagerTest() {
    ArgumentCaptor<WizBulbListener> listenerCaptor = ArgumentCaptor.forClass(WizBulbListener.class);
    doReturn(listenerSubscription).when(operations).addListener(listenerCaptor.capture());

    manager = new WizBulbManagerImpl(operations, new WizOptions());
    listener = listenerCaptor.getValue();
  }

  @Test
  public void testRegularPoll() {
    scheduler.runWaitingTasks();
    verify(operations, times(1)).getState(any());

    scheduler.skipAhead(15, TimeUnit.SECONDS).runWaitingTasks();
    verify(operations, times(2)).getState(any());
  }

  @Test
  public void testRegisterAvailableBulbs() {
    reportBulbStateToListener(BULB1, STATE_POWER_OFF);

    scheduler.skipAheadToLastRegisteredTaskAndRunAllWaiting();

    Collection<String> availableBulbs = manager.availableBulbs();
    assertThat(availableBulbs, is(not(nullValue())));
    assertThat(availableBulbs.size(), is(1));
    assertThat(availableBulbs, hasItems(BULB1.getMac()));
  }

  @Test
  public void testForgetUnavailableBulbs() {
    reportBulbStateToListener(BULB1, STATE_POWER_OFF);

    scheduler.skipAheadToLastRegisteredTaskAndRunAllWaiting();

    Collection<String> availableBulbs = manager.availableBulbs();
    assertThat(availableBulbs, hasItems(BULB1.getMac()));

    scheduler.skipAhead(60, MINUTES);

    availableBulbs = manager.availableBulbs();
    assertThat(availableBulbs, is(empty()));
  }

  @Test
  public void testSendBulbUpdate() {
    // Set up bulb in manager
    scheduler.skipAheadToLastRegisteredTaskAndRunAllWaiting();
    reportBulbStateToListener(BULB1, STATE_POWER_OFF);
    clearInvocations(operations);

    // Call update bulb
    manager.updateBulb(BULB1.getMac(), STATE_POWER_ON);

    // Verify set state is called
    scheduler.runWaitingTasks();
    verify(operations).setState(eq(BROADCAST_ADDR), eq(STATE_POWER_ON));
    verifyNoMoreInteractions(operations);
    clearInvocations(operations);

    // The bulb will answer with success
    listener.onSetStateResponse(BULB1, true);

    // Verify state update is requested to the bulb-address
    scheduler.runWaitingTasks();
    verify(operations).getState(eq(BULB1.getAddress()));
    verifyNoMoreInteractions(operations);
    clearInvocations(operations);

    // The bulb will answer with the new state
    listener.onState(BULB1, STATE_POWER_ON);

    // Verify state update is requested to the bulb-address
    scheduler.skipAhead(1, ChronoUnit.HOURS).runWaitingTasks();
    verify(operations, never()).setState(eq(BROADCAST_ADDR), any(WizBulbState.class));
  }

  @Test
  public void testResendBulbUpdate() {
    // Set up bulb in manager
    scheduler.skipAheadToLastRegisteredTaskAndRunAllWaiting();
    reportBulbStateToListener(BULB1, STATE_POWER_OFF);
    clearInvocations(operations);

    // Call update bulb
    manager.updateBulb(BULB1.getMac(), STATE_POWER_ON);

    // Verify set state is called
    scheduler.runWaitingTasks();
    verify(operations).setState(eq(BROADCAST_ADDR), eq(STATE_POWER_ON));
    verifyNoMoreInteractions(operations);
    clearInvocations(operations);

    // The bulb will not answer
    scheduler.skipAhead(100, MILLISECONDS).runWaitingTasks();

    // Verify set state is called again
    scheduler.runWaitingTasks();
    verify(operations).setState(eq(BROADCAST_ADDR), eq(STATE_POWER_ON));
    verifyNoMoreInteractions(operations);
    clearInvocations(operations);

    // The bulb will not answer, but the second wait is longer
    scheduler.skipAhead(400, MILLISECONDS).runWaitingTasks();

    // Verify set state is called again
    scheduler.runWaitingTasks();
    verify(operations).setState(eq(BROADCAST_ADDR), eq(STATE_POWER_ON));
    verifyNoMoreInteractions(operations);
    clearInvocations(operations);
  }

  private void reportBulbStateToListener(WizDeviceInfo deviceInfo, WizBulbState state) {
    listener.onState(deviceInfo, state);
  }

  private static InetAddress ip(String ip) {
    try {
      return InetAddress.getByName(ip);

    } catch (UnknownHostException e) {
      throw new IllegalArgumentException("Invalid IP (" + ip + ")", e);
    }
  }
}
