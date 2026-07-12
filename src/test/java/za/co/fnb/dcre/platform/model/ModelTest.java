package za.co.fnb.dcre.platform.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModelTest {

    @Test
    void opaqueRefKeepsRawAndTrimsCanonical() {
        OpaqueRef ref = OpaqueRef.ofFixedWidth("FNBCC 22082400001                  ");
        assertEquals("FNBCC 22082400001", ref.canonical());
        assertEquals(35, ref.rawBytes().length());
    }

    @Test
    void moneyParsesBothScaleReadings() {
        assertEquals(new BigDecimal("196.65"), MoneyText.parse("000000000019665", 2));
        assertEquals(new BigDecimal("19665"), MoneyText.parse("000000000019665", 0));
        assertThrows(IllegalArgumentException.class, () -> MoneyText.parse("0000000000014FN", 2));
    }

    @Test
    void productTypeByCodePrefixOnly() {
        assertEquals(ProductType.BALANCE_CARRYING, ProductType.fromProductCode("FNBRF"));
        assertEquals(ProductType.LIMIT_CARRYING, ProductType.fromProductCode("FNBCC"));
    }
}
