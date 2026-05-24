package com.shs.financevault.parser

import com.shs.financevault.data.Category

object CategoryClassifier {

    private val rules = listOf(
        Category.FOOD to listOf(
            "zomato", "swiggy", "dominos", "domino", "pizza", "burger", "mcdonald",
            "kfc", "subway", "cafe", "coffee", "starbucks", "dunkin", "restaurant",
            "food", "biryani", "hotel", "eat", "dining", "chai", "udupi", "dosa"
        ),
        Category.GROCERIES to listOf(
            "blinkit", "zepto", "bigbasket", "grofers", "dmart", "reliance fresh",
            "more supermarket", "spencer", "nature basket", "jiomart", "grocery",
            "supermarket", "bazaar", "kirana", "vegetables", "fruits", "milk"
        ),
        Category.TRANSPORT to listOf(
            "uber", "ola", "rapido", "namma yatri", "yatri", "auto", "cab",
            "metro", "irctc", "railway", "redbus", "abhibus", "bus", "petrol",
            "fuel", "hp ", "iocl", "bpcl", "fasttag", "toll", "parking", "flight",
            "indigo", "spicejet", "airindia", "makemytrip", "goibibo", "cleartrip"
        ),
        Category.SHOPPING to listOf(
            "amazon", "flipkart", "myntra", "ajio", "meesho", "nykaa", "snapdeal",
            "tatacliq", "reliance digital", "croma", "vijay sales", "shopping",
            "store", "mart", "retail", "clothe", "fashion", "apparel"
        ),
        Category.BILLS to listOf(
            "electricity", "bescom", "msedcl", "tpddl", "water", "gas", "airtel",
            "jio", "bsnl", "vi ", "vodafone", "idea", "broadband", "internet",
            "wifi", "recharge", "postpaid", "prepaid", "dth", "tata play",
            "dish tv", "insurance", "lic ", "rent", "society", "maintenance"
        ),
        Category.ENTERTAINMENT to listOf(
            "netflix", "prime video", "hotstar", "disney", "sonyliv", "zee5",
            "spotify", "gaana", "jiosaavn", "youtube premium", "movie", "pvr",
            "inox", "bookmyshow", "game", "steam", "playstation"
        ),
        Category.HEALTH to listOf(
            "apollo", "medplus", "netmeds", "pharmeasy", "1mg", "pharmacy",
            "chemist", "medical", "hospital", "clinic", "doctor", "lab",
            "diagnostic", "thyrocare", "lal path", "gym", "cult.fit", "healthify"
        ),
        Category.SALARY to listOf(
            "salary", "payroll", "wages", "stipend", "employer", "neft cr"
        ),
        Category.INVESTMENT to listOf(
            "zerodha", "groww", "upstox", "angelone", "icicidirect", "hdfc sec",
            "mutual fund", "sip", "nps", "ppf", "fd ", "fixed deposit", "smallcase"
        )
    )

    fun classify(merchant: String, rawText: String): Category {
        val combined = (merchant + " " + rawText).lowercase()
        for ((category, keywords) in rules) {
            if (keywords.any { combined.contains(it) }) return category
        }
        return Category.OTHER
    }
}
