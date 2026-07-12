package za.co.fnb.dcre.platform.model;

import java.math.BigDecimal;

/**
 * The single amount converter (R-21): BigDecimal from the raw digit string,
 * scale applied explicitly, never floating point, never guessing. The scale
 * is config-driven while A-1 stays open.
 */
public final class MoneyText {

    private MoneyText() {
    }

    public static BigDecimal parse(String raw, int scale) {
        String digits = raw.strip();
        if (digits.isEmpty() || !digits.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("not a raw amount field: '" + raw + "'");
        }
        BigDecimal whole = new BigDecimal(digits);
        return scale == 0 ? whole : whole.movePointLeft(scale);
    }
}
