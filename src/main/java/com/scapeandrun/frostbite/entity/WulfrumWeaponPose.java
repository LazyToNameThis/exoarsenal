package com.scapeandrun.frostbite.entity;

public final class WulfrumWeaponPose {
    public final float pitch, slide, fold, recoil, squint;

    private WulfrumWeaponPose(
            double pitch, double slide, double fold, double recoil, double squint) {
        this.pitch = (float) pitch;
        this.slide = (float) slide;
        this.fold = (float) fold;
        this.recoil = (float) recoil;
        this.squint = (float) squint;
    }

    private static double hit(double t, double at) {
        return WulfrumPairPose.hit(t, at);
    }

    private static double gate(double t, double a, double b) {
        return BrawlerScore.smooth((t - a) / 8) * (1 - BrawlerScore.smooth((t - b) / 12));
    }

    private static double cycle(double t, double period, double at) {
        double q = t - Math.floor(t / period) * period;
        return Math.min(1, hit(q, at) + hit(q - period, at) + hit(q + period, at));
    }

    private static double kick(double t, double period) {
        double q = t - Math.floor(t / period) * period;
        double duration = Math.min(14, period);
        return ExcavatorActuation.recoil(q * 14 / duration);
    }

    public static WulfrumWeaponPose sample(int attack, double t, boolean seer) {
        double pitch = 0, slide = 0, fold = 0, recoil = 0, squint = 0;
        switch (attack) {
            case 0:
            case 7:
                slide = -1.5 * (hit(t, 36) + hit(t, 94) + hit(t, 150));
                if (attack == 7) slide -= 1.2 * (hit(t, 65) + hit(t, 118));
                pitch = .22 * hit(t, 62) + .2 * hit(t, 159);
                fold = .3 * gate(t, 112, 132);
                break;
            case 1:
            case 8:
                recoil = kick(t, 24);
                fold = .2;
                break;
            case 2:
                slide = -1.8 * hit(t, 58);
                pitch = -.18 * hit(t, 58);
                fold = .45 * gate(t, 35, 80);
                break;
            case 3:
            case 11:
            case 12:
                fold = .55 * gate(t, 28, 190);
                pitch = Math.sin(t * .06) * .18;
                break;
            case 4:
                slide = -hit(t, 38) - hit(t, 76) - hit(t, 114);
                pitch = .3 * gate(t, 148, 188);
                break;
            case 5:
                for (int beat : new int[] {42, 86, 130, 174}) {
                    slide -= 1.8 * hit(t, beat);
                    fold = Math.max(fold, .7 * gate(t, beat - 28, beat + 10));
                }
                break;
            case 6:
                fold = .5 * gate(t, 38, 296);
                slide = -1.6 * (hit(t, 64) + hit(t, 124) + hit(t, 276));
                pitch = .25 * hit(t, 100);
                break;
            case 9:
            case 13:
            case 14:
                fold = .6 * gate(t, 25, 140);
                pitch = .2 * hit(t, 100);
                break;
            case 10:
                fold = .75 * gate(t, 28, 240);
                slide = -.8 * cycle(t, 18, 9);
                break;
            case 15:
                recoil = kick(t, 6);
                squint = .2 * gate(t, 25, 165);
                break;
            case 16:
                slide = -1.6 * (cycle(t, 96, 5) + cycle(t, 96, 82));
                pitch = .35 * cycle(t, 96, 32) - .2 * cycle(t, 96, 52);
                fold = .3 * gate(t, 202, 244);
                break;
            case 17:
                fold = .8 * gate(t, 25, 200);
                slide = -1.3 * gate(t, 40, 200);
                break;
            case 18:
                pitch = .35 * cycle(t, 26, 16);
                fold = .3 * gate(t, 45, 200);
                break;
            case 19:
                fold = .6 * gate(t, 35, 283);
                slide = -1.2 * cycle(t - 35, 40, 15);
                break;
            case 20:
                pitch = .24 * cycle(t, 24, 14);
                fold = .25 * gate(t, 20, 200);
                break;
            case 21:
                fold = .55 * gate(t % 72, 12, 34);
                slide = -1.6 * hit(t % 72, 28);
                break;
            case 22:
                recoil = kick(t - 34, 24);
                pitch = -.3 * cycle(t - 30, 24, 12);
                squint = .35 * gate(t, 30, 218);
                break;
            case 23:
                slide = -1.6 * hit(t % 42, 24);
                fold = .5 * gate(t % 42, 4, 30);
                break;
            case 24:
                pitch = .25 * gate(t, 28, 154);
                fold = .7 * gate(t, 28, 184);
                break;
            case 25:
                slide = -1.7 * cycle(t, 40, 24);
                recoil = kick(t, 8);
                break;
            case 26:
                fold = .45 * gate(t, 20, 244);
                pitch = .16 * cycle(t - 95, 18, 8);
                break;
            case 27:
                recoil = kick(t, 4);
                pitch = .2 * cycle(t, 8, 4);
                squint = .3 * gate(t, 190, 232);
                break;
            case 28:
                fold = .8 * gate(t, 32, 250);
                slide = -1.2 * gate(t, 208, 250);
                break;
            case 29:
                pitch = -.3 * gate(t, 35, 200);
                fold = .4 * gate(t, 190, 225);
                break;
            case 30:
                fold = .6 * gate(t, 15, 220);
                pitch = .2 * cycle(t, 20, 12);
                break;
            case 31:
                fold = .6 * gate(t, 15, 210);
                recoil = kick(t, 10);
                break;
            case 32:
                fold = .7 * gate(t, 40, 235);
                pitch = Math.sin(t * .7) * .05 * gate(t, 40, 235);
                squint = .4 * gate(t, 235, 270);
                break;
            default:
                if (attack >= 35 && attack <= 54) {
                    int form = (attack - 35) % 10;
                    double beat = form == 0 ? 30 : form == 7 ? 56 : form == 9 ? 28 : 40;
                    fold = .35 + .2 * gate(t, 30, 235);
                    slide = seer ? -1.4 * cycle(t - 40, beat, beat * .55) : 0;
                    pitch = seer ? .22 * cycle(t - 40, beat, beat * .55) : 0;
                    recoil = seer ? 0 : kick(t, form == 3 ? 20 : 18);
                    squint = form == 9 ? .5 * gate(t, 260, 320) : .15;
                }
                break;
        }
        if (!seer) slide = 0;
        return new WulfrumWeaponPose(pitch, slide, fold, recoil, squint);
    }

    private WulfrumWeaponPose() {
        this(0, 0, 0, 0, 0);
    }
}
