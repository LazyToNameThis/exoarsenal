package com.scapeandrun.frostbite.client;

import com.scapeandrun.frostbite.combat.WeaponDiscipline;

public final class WeaponPose {
    public final float x, y, z, body, lean;

    private WeaponPose(float x, float y, float z, float body, float lean) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.body = body;
        this.lean = lean;
    }

    private static WeaponPose pose(float x, float y, float z, float body, float lean) {
        return new WeaponPose(x, y, z, body, lean);
    }

    public static WeaponPose sample(WeaponDiscipline type, float age, boolean heavy, int combo) {
        float contact = type.contact(heavy),
                end = type.duration(heavy),
                side = (combo & 1) == 0 ? 1 : -1;
        WeaponPose ready = pose(-0.65F, -0.15F, 0.12F, 0, 0), wind, hit;
        switch (type) {
            case AXE:
                wind = pose(-2.75F, -0.35F, 0.22F, -0.3F, -0.08F);
                hit = pose(-0.75F, 0.15F, -0.05F, 0.22F, 0.22F);
                break;
            case HAMMER:
                wind = pose(-2.95F, -0.5F, 0.3F, -0.48F, -0.14F);
                hit = pose(-0.5F, 0.22F, -0.16F, 0.42F, 0.32F);
                break;
            case RAPIER:
                wind = pose(-0.85F, -0.65F, 0.18F, -0.28F, -0.04F);
                hit = pose(-1.57F, 0.02F, 0.02F, 0.28F, 0.12F);
                break;
            case SCISSORS:
                wind = pose(-1.4F, -0.58F, 0.4F, -0.28F, -0.04F);
                hit = pose(-1.45F, 0.38F, -0.2F, 0.30F, 0.12F);
                break;
            case SAW:
                wind = pose(-0.7F, -0.4F, 0.25F, -0.24F, -0.02F);
                hit = pose(-1.32F, 0.35F, -0.18F, 0.3F, 0.17F);
                break;
            case KATANA:
                wind = pose(-1.45F, -0.95F * side, 0.55F * side, -0.42F * side, -0.03F);
                hit = pose(-1.3F, 0.75F * side, -0.5F * side, 0.4F * side, 0.08F);
                break;
            case ENERGY:
                wind = pose(-1.9F, -0.8F * side, 0.5F * side, -0.38F * side, -0.04F);
                hit = pose(-1.05F, 0.65F * side, -0.35F * side, 0.35F * side, 0.12F);
                break;
            default:
                wind = pose(-1.85F, -0.65F * side, 0.45F * side, -0.3F * side, -0.04F);
                hit = pose(-0.95F, 0.55F * side, -0.25F * side, 0.3F * side, 0.1F);
        }
        float launch = contact - 3;
        if (age < launch) return mix(ready, wind, CombatMotion.smooth(age / Math.max(1, launch)));
        if (age < contact) return mix(wind, hit, CombatMotion.smooth((age - launch) / 3));

        return mix(
                hit,
                ready,
                CombatMotion.smooth((age - contact - 2) / Math.max(1, end - contact - 2)));
    }

    private static WeaponPose mix(WeaponPose a, WeaponPose b, float t) {
        return pose(
                a.x + (b.x - a.x) * t,
                a.y + (b.y - a.y) * t,
                a.z + (b.z - a.z) * t,
                a.body + (b.body - a.body) * t,
                a.lean + (b.lean - a.lean) * t);
    }

    public static float wrist(WeaponDiscipline d, float age, boolean heavy) {
        float angle =
                d == WeaponDiscipline.RAPIER
                        ? 82
                        : d == WeaponDiscipline.SCISSORS ? 48 : d == WeaponDiscipline.SAW ? 40 : 0;
        float contact = d.contact(heavy), end = d.duration(heavy);
        return angle
                * (age < contact
                        ? CombatMotion.smooth(age / contact)
                        : 1
                                - CombatMotion.smooth(
                                        (age - contact - 2) / Math.max(1, end - contact - 2)));
    }
}
