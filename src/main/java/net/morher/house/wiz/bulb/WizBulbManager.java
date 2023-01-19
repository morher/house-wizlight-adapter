package net.morher.house.wiz.bulb;

import java.util.Collection;

/**
 * The bulb manager is responsible for maintaining a list of all available and periodically poll the
 * state.
 *
 * <p>The manager can be called to change the state of a bulb. The manager will message the bulb
 * multiple times to make sure the message is received.
 *
 * @author Morten Hermansen
 */
public interface WizBulbManager {

  Collection<String> availableBulbs();

  void updateBulb(String mac, WizBulbState stateUpdate);
}
