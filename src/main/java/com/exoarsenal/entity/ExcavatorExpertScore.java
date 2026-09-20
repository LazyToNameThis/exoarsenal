package com.exoarsenal.entity;

public final class ExcavatorExpertScore {
    public static final int IMPACT = 0,
            FALSE_IMPACT = 1,
            TENNIS = 2,
            RAILGUN = 3,
            SURVEY = 4,
            CRUSTBREAKER = 5,
            HARPOONS = 6,
            EXTRACTION = 7,
            FEEDBACK = 8,
            REVOKED = 9,
            COUNTER = 10,
            CROSSFIRE = 11,
            ACCIDENT = 12,
            COMPLETE = 13;
    public static final String[] NAMES = {
        "Impact Test",
        "False Impact",
        "Drill Tennis",
        "Subterranean Railgun",
        "Survey → Execute",
        "Crustbreaker",
        "Probe Harpoons",
        "Core Extraction",
        "Bore/Beam Feedback",
        "Mining Rights Revoked",
        "Counter-Bore",
        "Fault-Line Crossfire",
        "Industrial Accident",
        "Geological Survey Complete"
    };

    public static int select(boolean overbore, int cursor) {
        return overbore ? 5 + Math.floorMod(cursor, 9) : Math.floorMod(cursor, 5);
    }

    public static int parries(int attack) {
        return attack == TENNIS || attack == COUNTER ? 3 : 1;
    }

    public static double rate(int attack, boolean charge, double distance, boolean hitstop) {
        return hitstop ? 0 : attack == IMPACT && charge && distance < 24 ? .25 : 1;
    }

    private ExcavatorExpertScore() {}
}
