package za.co.fnb.dcre.platform.model;

/**
 * Opaque identity (R-15): raw fixed-width bytes preserved for audit and
 * round-trip; canonical value (right-trimmed) used for correlation and XML.
 * Never parsed for semantics.
 */
public record OpaqueRef(String rawBytes, String canonical) {

    public static OpaqueRef ofFixedWidth(String raw) {
        return new OpaqueRef(raw, raw.stripTrailing());
    }
}
