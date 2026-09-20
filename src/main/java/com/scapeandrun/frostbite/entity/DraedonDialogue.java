package com.scapeandrun.frostbite.entity;

public final class DraedonDialogue {
    public static int next(int state, String action) {
        if (state == 1 && action.equals("technology")) return 2;
        if (state == 1 && action.equals("skip") || state == 2 && action.equals("yes")) return 3;
        if (state == 2 && action.equals("no")) return 4;
        return state;
    }

    public static double height(double age) {
        return 3 + (1 - BrawlerScore.smooth((age - 30) / 70D)) * 29;
    }

    private DraedonDialogue() {}
}
