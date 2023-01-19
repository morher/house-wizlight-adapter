package net.morher.house.wiz.bulb;

import lombok.Builder;
import lombok.Data;
import net.morher.house.api.utils.color.Brightness;
import net.morher.house.api.utils.color.Color;
import net.morher.house.wiz.style.WizBulbEffect;

@Data
@Builder
public class WizBulbState {
  private Boolean powerOn;
  private Brightness brightness;
  private Color color;
  private Brightness coldWhite;
  private Brightness warmWhite;
  private WizBulbEffect effect;
  private Integer temperature; // 2200 - 6500

  public static class WizBulbStateBuilder {}
}
