# Features

## Enchantments

- Shows the max level in all enchantment texts,<br/>
  unless the enchantment is already at its maximum level.

#### Examples

(for any enchanted item, and the enchanting table)

- `Sharpness II / V` instead of `Sharpness II`,
- `Mending` left as is — it only has one level, i.e. maximum.

## Potions

- Shows `-` as the name postfix if the potion has a prolonged (redstone) variant, _but the current variant is not it._

- Shows `I` as the amplifier if the potion has a strong (glowstone) variant, _but the current variant is not it._<br/>

  > **Exclusions:**<br/>
  > Potion of the Turtle Master — its effects have an amplifier by default.

- Shows the max level on the Ominous Bottle's effect, unless already at its maximum level.

#### Examples

- Potion of Swiftness:
  - `Speed- I (03:00)` instead of `Speed (03:00)`,
  - `Speed+ I (08:00)` instead of `Speed (08:00)`,
  - `Speed- II (01:30)` instead of `Speed II (01:30)`.
- Potion of Healing:
  - `Instant Health I` instead of `Instant Health`,
  - `Instant Health II` left as is.
- Potion of Infestation:
  - `Infested (03:00)` left as is.
- Potion of Fire Resistance:
  - `Fire Resistance- (03:00)` instead of `Fire Resistance (03:00)`,
  - `Fire Resistance+ (08:00)` instead of `Fire Resistance (08:00)`.
- Ominous Bottle:
  - `Bad Omen I / V (01:40:00)` instead of `Bad Omen (01:40:00)`.
