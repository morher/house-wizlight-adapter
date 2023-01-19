package net.morher.house.wiz.style;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WizBulbEffect {
  OCEAN("Ocean", 1),
  ROMANCE("Romance", 2),
  SUNSET("Sunset", 3),
  PARTY("Party", 4),
  FIREPLACE("Fireplace", 5),
  COZY("Cozy", 6),
  FOREST("Forest", 7),
  PASTEL_COLORS("Pastel Colors", 8),
  WAKE_UP("Wake up", 9),
  BEDTIME("Bedtime", 10),
  WARM_WHITE("Warm white", 11),
  DAYLIGHT("Daylight", 12),
  COOL_WHITE("Cool white", 13),
  NIGHT_LIGHT("Night light", 14),
  FOCUS("Focus", 15),
  RELAX("Relax", 16),
  TRUE_colors("True colors", 17),
  TV_TIME("TV time", 18),
  PLANTGROWTH("Plantgrowth", 19),
  SPRING("Spring", 20),
  SUMMER("Summer", 21),
  FALL("Fall", 22),
  DEEPDIVE("Deepdive", 23),
  JUNGLE("Jungle", 24),
  MOJITO("Mojito", 25),
  CLUB("Club", 26),
  CHRISTMAS("Christmas", 27),
  HALLOWEEN("Halloween", 28),
  CANDLELIGHT("Candlelight", 29),
  GOLDEN_WHITE("Golden white", 30),
  PULSE("Pulse", 31),
  STEAMPUNK("Steampunk", 32),
  RHYTHM("Rhythm", 1000);

  private final String name;
  private final int id;

  public static WizBulbEffect fromId(Integer effectId) {
    if (effectId == null || effectId == 0) {
      return null;
    }
    for (WizBulbEffect effect : WizBulbEffect.values()) {
      if (effect.getId() == effectId) {
        return effect;
      }
    }
    throw new IllegalArgumentException("Unknown effect id " + effectId);
  }

  public static WizBulbEffect fromName(String name) {
    if (name == null) {
      return null;
    }
    for (WizBulbEffect effect : WizBulbEffect.values()) {
      if (name.equals(effect.getName())) {
        return effect;
      }
    }
    throw new IllegalArgumentException("Unknown effect name " + name);
  }
}
