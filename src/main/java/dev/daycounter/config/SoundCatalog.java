package dev.daycounter.config;

import java.util.List;

/** Vanilla sound events only. Minecraft supplies all audio resources. */
public final class SoundCatalog {
    public static final String DEFAULT_ID = "minecraft:block.note_block.chime";
    public record Choice(String id, String key, float volume) { }
    public static final List<Choice> CHOICES = List.of(
            sound("block.note_block.chime", "chime", .65f),
            sound("block.note_block.pling", "pling", .55f),
            sound("entity.experience_orb.pickup", "orb", .5f),
            sound("block.amethyst_block.chime", "amethyst", .65f),
            sound("block.bell.use", "bell", .35f),
            sound("entity.player.levelup", "levelup", .35f),
            sound("block.note_block.bell", "note_bell", .55f),
            sound("block.note_block.harp", "harp", .65f),
            sound("block.note_block.flute", "flute", .55f),
            sound("block.note_block.xylophone", "xylophone", .6f),
            sound("block.note_block.bit", "bit", .5f),
            sound("block.note_block.guitar", "guitar", .65f),
            sound("block.note_block.banjo", "banjo", .6f),
            sound("block.note_block.cow_bell", "cow_bell", .45f),
            sound("block.note_block.iron_xylophone", "iron_xylophone", .6f),
            sound("block.enchantment_table.use", "enchant", .5f),
            sound("ui.button.click", "click", .4f),
            sound("ui.toast.in", "toast", .65f));

    private SoundCatalog() { }
    private static Choice sound(String id, String key, float volume) {
        return new Choice("minecraft:" + id, "daycounter.sound." + key, volume);
    }
    public static boolean contains(String id) {
        return CHOICES.stream().anyMatch(choice -> choice.id().equals(id));
    }
    public static Choice find(String id) {
        return CHOICES.stream().filter(choice -> choice.id().equals(id)).findFirst().orElse(CHOICES.getFirst());
    }
}
