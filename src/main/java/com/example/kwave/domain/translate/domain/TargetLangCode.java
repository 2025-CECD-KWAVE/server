package com.example.kwave.domain.translate.domain;

import java.util.Locale;
import java.util.Optional;

public enum TargetLangCode {

    AR("AR"),       // Arabic
    BG("BG"),       // Bulgarian
    CS("CS"),       // Czech
    DA("DA"),       // Danish
    DE("DE"),       // German
    EL("EL"),       // Greek
    EN("EN"),       // English (unspecified variant for backward compatibility; please select EN-GB or EN-US instead)
    EN_GB("EN-GB"), // English (British)
    EN_US("EN-US"), // English (American)
    ES("ES"),       // Spanish
    ET("ET"),       // Estonian
    FI("FI"),       // Finnish
    FR("FR"),       // French
    HU("HU"),       // Hungarian
    ID("ID"),       // Indonesian
    IT("IT"),       // Italian
    JA("JA"),       // Japanese
    KO("KO"),       // Korean
    LT("LT"),       // Lithuanian
    LV("LV"),       // Latvian
    NB("NB"),       // Norwegian Bokmål
    NL("NL"),       // Dutch
    PL("PL"),       // Polish
    PT("PT"),       // Portuguese (unspecified variant for backward compatibility; please select PT-BR or PT-PT instead)
    PT_BR("PT-BR"), // Portuguese (Brazilian)
    PT_PT("PT-PT"), // Portuguese (all Portuguese variants excluding Brazilian Portuguese)
    RO("RO"),       // Romanian
    RU("RU"),       // Russian
    SK("SK"),       // Slovak
    SL("SL"),       // Slovenian
    SV("SV"),       // Swedish
    TR("TR"),       // Turkish
    UK("UK"),       // Ukrainian
    ZH("ZH"),       // Chinese (unspecified variant for backward compatibility; please select ZH-HANS or ZH-HANT instead)
    ZH_HANS("ZH-HANS"), // Chinese (simplified)
    ZH_HANT("ZH-HANT"); // Chinese (traditional)

    private final String stringCode;

    TargetLangCode(String targetLangCode) {
        this.stringCode = targetLangCode;
    }

    public String getStringCode() {
        return this.stringCode;
    }

    public static Optional<TargetLangCode> convertLocale(Locale locale) {
        if (locale == null) {
            return Optional.empty();
        }
        String target = locale.toLanguageTag().toUpperCase().replace('-', '_');
        try {
            return Optional.of(TargetLangCode.valueOf(target));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
