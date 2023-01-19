package net.morher.house.wiz.style;

import java.util.Collection;
import java.util.List;
import net.morher.house.api.entity.light.LightState;
import net.morher.house.wiz.bulb.WizBulbState;

public interface StyleManager {

  WizBulbState calculateState(LightState state, Collection<String> channels);

  List<String> getStyles();
}
