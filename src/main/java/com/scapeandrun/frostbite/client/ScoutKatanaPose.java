package com.scapeandrun.frostbite.client;

import com.scapeandrun.frostbite.entity.ScoutCombatPattern;
import java.util.Map;

public final class ScoutKatanaPose {
    private ScoutKatanaPose() {}

    private static float c(float t, float... keys) {
        return ScoutBrawlPose.curve(t, keys);
    }

    private static void r(Map<String, float[]> out, String n, float x, float y, float z) {
        out.put(n, new float[] {x, y, z});
    }

    public static void apply(Map<String, float[]> out, int attack, float tick) {
        boolean cuts = attack == ScoutCombatPattern.KATANA_CUTS;
        float draw = 0, load = c(tick, 0, 0, 18, 1), flick = 0, toss = 0;
        if (cuts) {
            if (tick >= 30 && tick < 110)
                draw = c((tick - 30) % 16, 0, 0, 2, .1F, 4, 1, 7, 1, 12, 0, 16, 0);
            flick = c(tick, 112, 0, 125, -.4F, 130, 1, 136, .2F, 148, 0);
            if (tick >= 240 && tick < 352)
                draw = c((tick - 240) % 12, 0, 0, 4, -.15F, 8, 1, 10, .6F, 12, 0);
            if (tick >= 134 && tick < 210) toss = c((tick - 134) % 8, 0, 0, 3, -.2F, 6, 1, 8, 0);
        } else {
            for (int contact : new int[] {52, 116, 160})
                draw =
                        Math.max(
                                draw,
                                c(
                                        tick,
                                        contact - 14,
                                        0,
                                        contact - 5,
                                        -.15F,
                                        contact,
                                        1,
                                        contact + 7,
                                        .7F,
                                        contact + 18,
                                        0));
            for (int release : new int[] {34, 98, 142})
                toss =
                        Math.max(
                                toss,
                                c(
                                        tick,
                                        release - 16,
                                        0,
                                        release - 7,
                                        -.2F,
                                        release,
                                        1,
                                        release + 14,
                                        0));
        }
        int leap = cuts ? com.scapeandrun.frostbite.entity.ScoutKatanaPlatforms.DISMOUNT : 164,
                end = ScoutCombatPattern.duration(attack);
        float jump = c(tick, leap - 6, 0, leap, 1, leap + 14, 1, leap + 18, .4F, leap + 27, 0);
        float slam =
                c(tick, leap + 10, 0, leap + 17, -.3F, leap + 23, 1, leap + 31, .8F, end - 8, 0);
        float settle = 1 - c(tick, end - 18, 0, end, 1);
        r(
                out,
                "chassis",
                (-8 * load - 12 * draw + 4 * toss - 14 * slam) * settle,
                (-14 * load + 38 * draw + 12 * flick) * settle,
                (-3 * draw) * settle);
        r(
                out,
                "leftSwordArm",
                (-8 * load + 42 * draw + 12 * flick + 100 * jump - 20 * slam) * settle,
                (-48 * load + 92 * draw + 12 * flick) * settle,
                (-12 * load + 30 * draw) * settle);
        r(
                out,
                "leftSwordElbow",
                55 + (-35 * load - 5 * draw + 20 * jump - 10 * slam) * settle,
                0,
                8 * settle);
        r(
                out,
                "rightSwordArm",
                (32 * load + 12 * draw + 20 * jump) * settle,
                22 * load * settle,
                5 * settle);
        r(out, "rightSwordElbow", (32 * load + 10 * draw) * settle, 0, -8 * settle);
        r(out, "clawArm", (20 * load + 110 * toss) * settle, -20 * settle, -12 * settle);
        r(out, "leftViceElbow", (25 * load + 30 * toss) * settle, 0, 0);
        r(out, "viceArm", (28 * load + 14 * draw) * settle, 25 * settle, 12 * settle);
        r(out, "rightViceElbow", 35 * load * settle, 0, 0);
        r(
                out,
                "leftThigh",
                (12 * load - 30 * draw + 55 * jump + 18 * slam) * settle,
                0,
                -5 * settle);
        r(
                out,
                "rightThigh",
                (-8 * load + 32 * draw + 30 * jump + 22 * slam) * settle,
                0,
                5 * settle);
        r(out, "leftShin", (-12 * load - 35 * jump - 18 * slam) * settle, 0, 0);
        r(out, "rightShin", (-10 * load - 28 * jump - 20 * slam) * settle, 0, 0);
        if (cuts) {

            float ride = c(tick, 208, 0, 224, 1, 364, 1, 378, 0);
            out.get("chassis")[0] -= 12 * ride;
            out.get("chassis")[1] += 18 * ride;
            out.get("leftThigh")[0] += 36 * ride;
            out.get("leftThigh")[2] -= 9 * ride;
            out.get("rightThigh")[0] += 15 * ride;
            out.get("rightThigh")[2] += 13 * ride;
            out.get("leftShin")[0] -= 44 * ride;
            out.get("rightShin")[0] -= 25 * ride;
            out.get("clawArm")[2] -= 18 * ride;
            out.get("viceArm")[2] += 22 * ride;
        }
    }
}
