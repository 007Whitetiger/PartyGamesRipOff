package me.whitetiger.partygames.games.helper;

public enum GameType {
    FishSlap("FishSlap"),
    JungleParkour("JungleParkour"),
    LavaParkour("LavaParkour"),
    HoeHoeHoe("HoeHoeHoe"),
    JigsawRush("JigsawRush"),
    Trampoline("Trampoline"),
    CannonPainters("CannonPainters"),
    PigFishing("PigFishing"),
    ChickenRun("ChickenRun");

    private final String name;

    GameType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
