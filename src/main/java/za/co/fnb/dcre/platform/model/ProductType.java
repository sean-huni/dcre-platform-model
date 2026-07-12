package za.co.fnb.dcre.platform.model;

/** Product dimension for cap checks (R-22): never client-code strings. */
public enum ProductType {
    BALANCE_CARRYING,   // FNBRF: cap = balance
    LIMIT_CARRYING;     // FNBCC: cap = max_credit_limit

    public static ProductType fromProductCode(String code) {
        return code != null && code.startsWith("FNBRF") ? BALANCE_CARRYING : LIMIT_CARRYING;
    }
}
