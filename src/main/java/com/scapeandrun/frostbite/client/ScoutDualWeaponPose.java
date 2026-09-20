package com.scapeandrun.frostbite.client;

import com.scapeandrun.frostbite.entity.ScoutCombatPattern;
import java.util.Map;

public final class ScoutDualWeaponPose {
    private ScoutDualWeaponPose() {}

    public static void apply(Map<String, float[]> out, int attack, float tick) {
        int hit = ScoutCombatPattern.offhandContact(attack);
        if (hit < 0) return;
        float wind = ScoutBrawlPose.curve(tick, 0, 0, hit - 15, 0, hit - 5, 1, hit, 0, hit + 10, 0);
        float cut = ScoutBrawlPose.curve(tick, 0, 0, hit - 4, 0, hit, 1, hit + 3, 1, hit + 10, 0);
        out.put(
                "rightSwordArm",
                new float[] {
                    18 + 32 * wind - 26 * cut, 24 - 16 * wind + 58 * cut, 8 - 20 * wind + 18 * cut
                });
        out.put("rightSwordElbow", new float[] {42 + 25 * wind - 26 * cut, 0, -8});
        out.put("rightWrist", new float[] {-8 * wind - 18 * cut, 0, 0});
    }
}
