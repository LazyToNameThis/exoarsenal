package com.scapeandrun.frostbite.client.render;

import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoQuad;

public final class GeoCubeFaces {
    public static void prepare(GeoCube cube) {
        int count = 0;
        for (GeoQuad face : cube.quads) if (face != null) count++;
        if (count == cube.quads.length) return;
        GeoQuad[] present = new GeoQuad[count];
        int index = 0;
        for (GeoQuad face : cube.quads) if (face != null) present[index++] = face;
        cube.quads = present;
    }

    private GeoCubeFaces() {}
}
