package com.wsproxy.version;
/*
    0.1.0   - Initial alpha release
    0.1.1   - Project database migration to sqlite
    0.1.2   - Script engine rewrite ( python only now )
    0.1.3   - Built-in http server
    0.1.4   - Immediate
    0.1.5   - bugfixes
    0.1.6   - bugfixes
    0.1.7   - bugfixes
    0.1.8   - active rules
    0.1.9   - mt/at refactor, beginnings of analyzer model
    0.1.10  - https upstream proxy
    0.1.11  - ui polish, style ( nimbus )
    0.1.12  - bugfixes
    0.1.13  - protocol tester
    0.1.14  - bugfixes, interactsh
    0.1.15  - update client improvements
    0.1.16  - Event scripts, api, http server improvements
    0.1.17  - Breakpoints
 */
public final class VERSION {
    public static final int VERSION_MAJOR = 0;
    public static final int VERSION_MINOR = 1;
    public static final int VERSION_PATCH = 17;

    public static String getVersionStr() {
        return String.format("%d.%d.%d", VERSION_MAJOR, VERSION_MINOR, VERSION_PATCH);
    }
}
