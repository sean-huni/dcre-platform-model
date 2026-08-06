package za.co.fnb.dcre.platform.model;

/**
 * Symbolic MRV/MAS verdict vocabulary for the mandates flow (M10, SCRUM-73;
 * CtvOutcome pattern). Persisted by name into man_validation_log and the MAS
 * enquiry outcome, then rolled up per R-41 semantics and reported to OnHost
 * through MIR NACK reasons: renames are data-contract breaks.
 */
public enum MandateOutcome {

    /** MRV chain + MAS gate both passed; row proceeds to MIT initialization. */
    PASS,

    /** MRV structural stage: malformed record/field. Whole-file fatal rolls up BUSINESS_FILE_REJECTED; reportable via MIR NACK. */
    FAIL_STRUCTURE,

    /** MRV duplicate stage: mandate_ref clashes in-file (later occurrence fails). Item-tier reject, MIR-reportable. */
    FAIL_DUPLICATE_REF,

    /** MRV account stage: debtor account absent from dcre_man account master. Item-tier reject, MIR-reportable. */
    FAIL_ACCOUNT_NOT_FOUND,

    /** MRV account stage: account_type does not allow mandates (AG01 class). Item-tier reject, MIR-reportable. */
    FAIL_ACCOUNT_TYPE_DISALLOWED,

    /** MRV contract stage: contract_ref fails the format rule. Item-tier reject, MIR-reportable. */
    FAIL_CONTRACT_FORMAT,

    /** MRV action stage: AMEND targets a mandate_ref unknown to spine and projection. Item-tier reject, MIR-reportable. */
    FAIL_AMEND_UNKNOWN_REF,

    /** MRV action stage: CANCEL targets a mandate_ref unknown to spine and projection. Item-tier reject, MIR-reportable. */
    FAIL_CANCEL_UNKNOWN_REF,

    /**
     * MRV admissibility stage (SCRUM-91, refines R-20): the contract already carries an
     * effectively-active mandate (PDNG/ACCP/SUSPENDED) under a DIFFERENT mandate_ref.
     * Item-tier reject, MIR-reportable. Deliberately NOT prefixed FAIL_: this is an
     * admissibility refusal of a well-formed instruction, not a defect in it, and the
     * name is the reason string the spec and plan pin for the OnHost NACK.
     */
    CONTRACT_HAS_LIVE_MANDATE,

    /** MAS gate: bureau score below the per-client threshold; spine SCORE_DECLINED, reportable via MIR (R-08). */
    FAIL_SCORE_BELOW_THRESHOLD,

    /** MAS gate: bureau unreachable; NOT a decline (R-12 carry-over), row stays SCORE_PENDING for the next run, never MIR-NACKed. */
    HOLD_BUREAU_UNAVAILABLE
}
