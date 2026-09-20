package com.exoarsenal.expedition;

public enum WildSpecies {
    CAVE_BAT("cave_bat", Shape.BAT, Habitat.CAVE, 12, 3, 0, 0x746780),
    GIANT_WORM("giant_worm", Shape.WORM, Habitat.CAVE, 24, 4, 2, 0xB18C80),
    MOTHER_SLIME("mother_slime", Shape.SLIME, Habitat.CAVE, 36, 5, 2, 0x655A82),
    BABY_SLIME("baby_slime", Shape.SLIME, Habitat.CAVE, 8, 2, 0, 0x8373A6),
    UNDEAD_MINER("undead_miner", Shape.HUMAN, Habitat.CAVE, 26, 4, 3, 0xB4A272),
    TIM("tim", Shape.HUMAN, Habitat.DEEP, 36, 5, 2, 0xA069AD),
    CRAWDAD("crawdad", Shape.CRAB, Habitat.CAVE, 30, 5, 6, 0xB36C5D),
    GIANT_SHELLY("giant_shelly", Shape.SHELL, Habitat.CAVE, 32, 7, 8, 0xB9A776),
    SALAMANDER("salamander", Shape.LIZARD, Habitat.CAVE, 26, 4, 2, 0x91A96E),
    COCHINEAL_BEETLE("cochineal_beetle", Shape.BEETLE, Habitat.CAVE, 14, 3, 4, 0xBF5268),
    CYAN_BEETLE("cyan_beetle", Shape.BEETLE, Habitat.ICE, 14, 3, 4, 0x55A6BC),
    LAC_BEETLE("lac_beetle", Shape.BEETLE, Habitat.JUNGLE_CAVE, 16, 3, 4, 0xA665BB),
    ICE_BAT("ice_bat", Shape.BAT, Habitat.ICE, 16, 4, 0, 0x9BBFD0),
    ICE_SLIME("ice_slime", Shape.SLIME, Habitat.ICE, 20, 4, 2, 0x8DC8D7),
    SPIKED_ICE_SLIME("spiked_ice_slime", Shape.SLIME, Habitat.ICE, 28, 5, 4, 0x72ABCF),
    SPORE_SKELETON("spore_skeleton", Shape.HUMAN, Habitat.MUSHROOM, 28, 5, 2, 0x91A1B5),
    JUNGLE_BAT("jungle_bat", Shape.BAT, Habitat.JUNGLE, 16, 4, 0, 0xB89765),
    JUNGLE_SLIME("jungle_slime", Shape.SLIME, Habitat.JUNGLE, 24, 4, 2, 0x6EAA62),
    SPIKED_JUNGLE_SLIME(
            "spiked_jungle_slime", Shape.SLIME, Habitat.JUNGLE_CAVE, 32, 5, 4, 0xBD7864),
    HORNET("hornet", Shape.HORNET, Habitat.JUNGLE_CAVE, 24, 4, 3, 0xC7A64E),
    SNATCHER("snatcher", Shape.PLANT, Habitat.JUNGLE, 28, 6, 2, 0x889E53),
    MAN_EATER("man_eater", Shape.PLANT, Habitat.JUNGLE_CAVE, 38, 7, 4, 0xBB7377),
    PIRANHA("piranha", Shape.FISH, Habitat.JUNGLE, 16, 4, 1, 0x87AF68),
    EATER_OF_SOULS("eater_of_souls", Shape.EATER, Habitat.CORRUPTION, 28, 5, 3, 0x8B769F),
    DEVOURER("devourer", Shape.WORM, Habitat.CORRUPTION, 40, 7, 4, 0x887F99),
    CRIMERA("crimera", Shape.EATER, Habitat.CRIMSON, 30, 5, 3, 0xC27D79),
    FACE_MONSTER("face_monster", Shape.HUMAN, Habitat.CRIMSON, 38, 7, 3, 0xBC706D),
    BLOOD_CRAWLER("blood_crawler", Shape.SPIDER, Habitat.CRIMSON, 30, 5, 4, 0xB26570),
    ANTLION("antlion", Shape.BEETLE, Habitat.DESERT, 24, 4, 4, 0xB8A16A),
    ANTLION_CHARGER("antlion_charger", Shape.BEETLE, Habitat.DESERT, 34, 6, 5, 0xAD8659),
    ANTLION_SWARMER("antlion_swarmer", Shape.HORNET, Habitat.DESERT, 24, 5, 3, 0xC5AC76),
    TOMB_CRAWLER("tomb_crawler", Shape.WORM, Habitat.DESERT, 46, 7, 5, 0xBFA878),
    ANGRY_BONES("angry_bones", Shape.HUMAN, Habitat.DUNGEON, 32, 6, 4, 0xB6B6A6),
    DARK_CASTER("dark_caster", Shape.HUMAN, Habitat.DUNGEON, 30, 5, 2, 0x746EA4),
    CURSED_SKULL("cursed_skull", Shape.EATER, Habitat.DUNGEON, 28, 6, 5, 0xBBC5AF),
    DUNGEON_SLIME("dungeon_slime", Shape.SLIME, Habitat.DUNGEON, 40, 5, 4, 0x786C9F),
    HELLBAT("hellbat", Shape.BAT, Habitat.UNDERWORLD, 24, 5, 2, 0xC78663),
    LAVA_SLIME("lava_slime", Shape.SLIME, Habitat.UNDERWORLD, 32, 6, 4, 0xDC985D),
    FIRE_IMP("fire_imp", Shape.HUMAN, Habitat.UNDERWORLD, 34, 5, 3, 0xC58268),
    DEMON("demon", Shape.HORNET, Habitat.UNDERWORLD, 46, 7, 5, 0xB26C72),
    BONE_SERPENT("bone_serpent", Shape.WORM, Habitat.UNDERWORLD, 60, 8, 6, 0xD0C3A7);

    public enum Shape {
        BAT,
        WORM,
        SLIME,
        HUMAN,
        BEETLE,
        CRAB,
        SHELL,
        LIZARD,
        PLANT,
        HORNET,
        EATER,
        SPIDER,
        FISH
    }

    public enum Habitat {
        CAVE,
        DEEP,
        ICE,
        JUNGLE,
        JUNGLE_CAVE,
        CORRUPTION,
        CRIMSON,
        MUSHROOM,
        DESERT,
        DUNGEON,
        UNDERWORLD
    }

    public final String id;
    public final Shape shape;
    public final Habitat habitat;
    public final int health, damage, armor, color;

    WildSpecies(
            String id, Shape shape, Habitat habitat, int health, int damage, int armor, int color) {
        this.id = id;
        this.shape = shape;
        this.habitat = habitat;
        this.health = health;
        this.damage = damage;
        this.armor = armor;
        this.color = color;
    }
}
