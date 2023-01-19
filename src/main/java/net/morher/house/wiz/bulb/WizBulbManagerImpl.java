package net.morher.house.wiz.bulb;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import net.morher.house.api.schedule.DelayedTrigger;
import net.morher.house.api.schedule.HouseScheduler;
import net.morher.house.api.utils.Registry;
import net.morher.house.wiz.udp.WizBulbListener;
import net.morher.house.wiz.udp.WizDeviceInfo;

@Slf4j
public class WizBulbManagerImpl implements WizBulbManager {
  private final HouseScheduler scheduler = HouseScheduler.get();
  private final WizBulbOperations operations;
  private final Duration availablilityTimeout;
  private final Registry<String, Device> availableDevices = new Registry<>(Device::new);
  private final InetAddress broadcastAddr;

  public WizBulbManagerImpl(WizBulbOperations operations, WizBulbManagerOptions options) {
    this.operations = operations;
    this.broadcastAddr = options.broadcastAddress();
    this.availablilityTimeout = options.availablilityTimeout();
    this.operations.addListener(new Listener());
    scheduler.scheduleWithFixedDelay(
        this::maintainDeviceList, 0, options.pollInterval().toMillis(), MILLISECONDS);
  }

  @Override
  public Collection<String> availableBulbs() {
    return availableDevices.items().stream().map(Device::getMac).collect(toList());
  }

  @Override
  public void updateBulb(String mac, WizBulbState stateUpdate) {
    availableDevices.get(mac).setRequestedState(stateUpdate);
  }

  private void maintainDeviceList() {
    pollAll();
    cleanup();
  }

  private void pollAll() {
    operations.getState(broadcastAddr);
  }

  private void cleanup() {
    Instant unavailableThreshold = scheduler.now().minus(availablilityTimeout);
    for (Device device : availableDevices.items()) {
      if (device.getLastInteraction() != null
          && device.getLastInteraction().isBefore(unavailableThreshold)) {
        availableDevices.remove(device.getMac());
      }
    }
  }

  private class Listener implements WizBulbListener {

    @Override
    public void onState(WizDeviceInfo deviceInfo, WizBulbState state) {
      availableDevices.get(deviceInfo.getMac()).reportObservation(deviceInfo, state);
    }

    @Override
    public void onSetStateResponse(WizDeviceInfo deviceInfo, boolean success) {
      operations.getState(deviceInfo.getAddress());
    }
  }

  private class Device {
    private final DelayedTrigger trigger;
    @Getter private final String mac;
    @Getter private InetAddress address;
    @Getter private Instant lastObserved;
    @Getter private Instant lastInteraction;
    private WizBulbState lastStateReport;
    private WizBulbState requestedState;
    private int commandAttempts;
    private Instant nextAttempt;

    public Device(String mac) {
      this.trigger =
          scheduler.delayedTrigger("Update bulb device " + mac + " state", this::handleState);
      this.mac = mac;
    }

    @Synchronized
    public void reportObservation(WizDeviceInfo deviceInfo, WizBulbState state) {
      this.address = deviceInfo.getAddress();
      Instant now = scheduler.now();
      this.lastObserved = now;
      this.lastInteraction = now;
      this.lastStateReport = state;
      trigger.runNow();
    }

    @Synchronized
    public void setRequestedState(WizBulbState requestedState) {
      this.requestedState = requestedState;
      this.commandAttempts = 0;
      Instant now = scheduler.now();
      this.nextAttempt = now;
      this.lastInteraction = now;
      trigger.runNow();
    }

    @Synchronized
    private void handleState() {
      if (requestedState != null) {
        if (address == null) {
          return;
        }

        if (commandAttempts > 0 && requestedState.equals(lastStateReport)) {
          log.debug("State confirmed for bulb {} after {} attempts", mac, commandAttempts);
          requestedState = null;
          return;
        }

        if (commandAttempts >= 10) {
          log.debug("Giving up setting state for bulb {} after {} attempts", mac, commandAttempts);
          requestedState = null;
          return;
        }

        if (!nextAttempt.isAfter(scheduler.now())) {
          log.debug("Set state for bulb {}, attempt {}", mac, commandAttempts);
          operations.setState(address, requestedState);
          commandAttempts++;
          nextAttempt = scheduler.now().plusMillis(100 * commandAttempts * commandAttempts);
        }
        trigger.runAt(nextAttempt);
      }
    }
  }
}
