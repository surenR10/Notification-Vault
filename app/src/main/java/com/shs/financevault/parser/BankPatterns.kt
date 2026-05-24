package com.shs.financevault.parser

/**
 * Regex patterns for parsing Indian bank and UPI notification text.
 * Each entry covers the real notification format sent by that app/bank.
 */
object BankPatterns {

    // ── Supported package names ───────────────────────────────────────────────
    val FINANCIAL_PACKAGES = setOf(
        "com.google.android.apps.nbu.paisa.user",   // GPay
        "com.phonepe.app",                           // PhonePe
        "net.one97.paytm",                           // Paytm
        "in.amazon.mShop.android.shopping",          // Amazon Pay
        "com.snapwork.hdfc",                         // HDFC MobileBanking
        "com.sbi.lotusintouch",                      // SBI YONO
        "com.csam.icici.bank.imobile",               // ICICI iMobile
        "com.axis.mobile",                           // Axis Mobile
        "com.msf.kbank.mobile",                      // Kotak
        "com.indusind.mobile",                       // IndusInd
        "com.freecharge.android",                    // Freecharge
        "com.mobikwik_new",                          // MobiKwik
        "com.dreamplug.androidapp",                  // CRED
        "com.whatsapp",                              // WhatsApp Pay
        "com.boi.mobile"                             // Bank of India
    )

    // ── Amount patterns ───────────────────────────────────────────────────────
    // Handles: ₹500, Rs.500, Rs 500, INR 500, 500.00
    val AMOUNT = Regex(
        """(?:₹|Rs\.?|INR)\s*([\d,]+(?:\.\d{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    // ── Debit indicators ──────────────────────────────────────────────────────
    val DEBIT_KEYWORDS = Regex(
        """(?:debited|debit|paid|payment|sent|transferred out|withdrawn|spent|charged)""",
        RegexOption.IGNORE_CASE
    )

    // ── Credit indicators ─────────────────────────────────────────────────────
    val CREDIT_KEYWORDS = Regex(
        """(?:credited|credit|received|refund|cashback|added|deposited)""",
        RegexOption.IGNORE_CASE
    )

    // ── Available balance ─────────────────────────────────────────────────────
    val BALANCE = Regex(
        """(?:avl\.?\s*bal(?:ance)?|balance|bal)\s*(?:is|:)?\s*(?:₹|Rs\.?|INR)?\s*([\d,]+(?:\.\d{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    // ── UPI reference number ──────────────────────────────────────────────────
    val UPI_REF = Regex(
        """(?:UPI\s*Ref(?:erence)?(?:\s*No\.?)?|Ref\s*No\.?)\s*:?\s*(\d{10,})""",
        RegexOption.IGNORE_CASE
    )

    // ── Merchant / payee name extraction ─────────────────────────────────────
    // "paid to Zomato", "sent to John", "to VPA merchant@upi"
    val MERCHANT_TO = Regex(
        """(?:paid to|sent to|to|payment to)\s+([A-Za-z0-9 &._\-]{2,30})""",
        RegexOption.IGNORE_CASE
    )

    // "received from John"
    val MERCHANT_FROM = Regex(
        """(?:received from|from)\s+([A-Za-z0-9 &._\-]{2,30})""",
        RegexOption.IGNORE_CASE
    )

    // UPI VPA merchant: merchant@okhdfc, zomato@icici
    val VPA = Regex(
        """([a-zA-Z0-9.\-_]+)@(?:okhdfc|okicici|oksbi|okaxis|ybl|upi|paytm|axl|ibl|ikwithdraw)""",
        RegexOption.IGNORE_CASE
    )

    // "Info: UPI-ZOMATO-..." (HDFC style)
    val INFO_FIELD = Regex(
        """Info:\s*UPI[-/]([A-Za-z0-9 _\-]+)""",
        RegexOption.IGNORE_CASE
    )

    // Source app label map
    fun sourceLabel(pkg: String) = when (pkg) {
        "com.google.android.apps.nbu.paisa.user" -> "GPay"
        "com.phonepe.app"                         -> "PhonePe"
        "net.one97.paytm"                         -> "Paytm"
        "in.amazon.mShop.android.shopping"        -> "Amazon Pay"
        "com.snapwork.hdfc"                        -> "HDFC Bank"
        "com.sbi.lotusintouch"                    -> "SBI YONO"
        "com.csam.icici.bank.imobile"             -> "ICICI Bank"
        "com.axis.mobile"                         -> "Axis Bank"
        "com.msf.kbank.mobile"                    -> "Kotak Bank"
        "com.dreamplug.androidapp"                -> "CRED"
        "com.whatsapp"                            -> "WhatsApp Pay"
        else                                      -> pkg.substringAfterLast(".")
    }
}
