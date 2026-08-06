package za.co.fnb.dcre.platform.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MandateOutcomeTest {

    /**
     * Snapshot of the MRV/MAS verdict vocabulary (M10 T1). These names are
     * persisted downstream (man_validation_log, MIR NACK reasons), so an
     * accidental rename or reorder is a data-contract break, not a refactor.
     */
    @Test
    void valuesSnapshotGuardsAgainstAccidentalRenames() {
        assertEquals(List.of(
                        "PASS",
                        "FAIL_STRUCTURE",
                        "FAIL_DUPLICATE_REF",
                        "FAIL_ACCOUNT_NOT_FOUND",
                        "FAIL_ACCOUNT_TYPE_DISALLOWED",
                        "FAIL_CONTRACT_FORMAT",
                        "FAIL_AMEND_UNKNOWN_REF",
                        "FAIL_CANCEL_UNKNOWN_REF",
                        "CONTRACT_HAS_LIVE_MANDATE",
                        "FAIL_SCORE_BELOW_THRESHOLD",
                        "HOLD_BUREAU_UNAVAILABLE"),
                Arrays.stream(MandateOutcome.values()).map(Enum::name).toList());
    }
}
