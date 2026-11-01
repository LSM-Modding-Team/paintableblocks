package net.nicomar.paintableblocks.paint;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;
import net.nicomar.paintableblocks.balance.PaintingBalance;

/** Immutable catalog bundled from the approved CSV; numeric identities never depend on registry order. */
public final class PaintCatalog {
    public record Entry(int paintId, String blockId, String nameEs, String nameEn) {
        public Identifier identifier() { return Identifier.parse(blockId); }
        public String translationKey() { return "paint.paintableblocks." + paintId; }
        public boolean survivalAvailable() { return !CREATIVE_ONLY.contains(blockId); }
        public BlockState sampleState() {
            BlockState state = BuiltInRegistries.BLOCK.getValue(identifier()).defaultBlockState();
            if (state.hasProperty(BlockStateProperties.AXIS)) state = state.setValue(BlockStateProperties.AXIS, Direction.Axis.Y);
            if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
            if (state.hasProperty(BlockStateProperties.FACING)) state = state.setValue(BlockStateProperties.FACING, Direction.NORTH);
            if (state.hasProperty(BlockStateProperties.LIT)) state = state.setValue(BlockStateProperties.LIT, false);
            if (state.hasProperty(BlockStateProperties.POWERED)) state = state.setValue(BlockStateProperties.POWERED, false);
            if (state.hasProperty(BlockStateProperties.RESPAWN_ANCHOR_CHARGES)) state = state.setValue(BlockStateProperties.RESPAWN_ANCHOR_CHARGES, 0);
            return state;
        }
    }
    private static final Set<String> CREATIVE_ONLY = Set.of(
            "minecraft:bedrock", "minecraft:budding_amethyst", "minecraft:reinforced_deepslate");
    private static final class Holder { private static final List<Entry> ENTRIES = load(); }
    private PaintCatalog() {}
    public static List<Entry> entries() { return Holder.ENTRIES; }
    public static boolean validId(int id) { return id >= 1 && id <= PaintingBalance.PAINTS; }
    public static Entry byId(int id) {
        if (!validId(id)) throw new IllegalArgumentException("Unknown paint: " + id);
        return entries().get(id - 1);
    }
    /** Crystal identities are appended in pairs: vanilla, then connected. */
    public static boolean isGlass(int id) { return id >= 338 && id <= 373; }
    public static boolean isConnectedGlass(int id) { return isGlass(id) && (id - 338) % 2 == 1; }
    public static boolean transmitsLight(int id) { return isGlass(id) && id < 372; }
    public static int alternateGlass(int id) {
        if (!isGlass(id)) throw new IllegalArgumentException("Not a glass paint");
        return isConnectedGlass(id) ? id - 1 : id + 1;
    }
    public static void validateRegistries() {
        for (Entry entry : entries()) {
            if (!BuiltInRegistries.BLOCK.containsKey(entry.identifier())) {
                throw new IllegalStateException("Catalog block missing in Minecraft 26.2: " + entry.blockId());
            }
        }
    }
    private static List<Entry> load() {
        var stream = PaintCatalog.class.getResourceAsStream("/assets/paintableblocks/catalog/paints.json");
        if (stream == null) throw new IllegalStateException("Missing approved paint catalog");
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            Entry[] original = new Gson().fromJson(reader, Entry[].class);
            if (original == null || original.length != 337) throw new IllegalStateException("Expected 337 original paints");
            Entry[] entries = Arrays.copyOf(original, PaintingBalance.PAINTS);
            String[] colors = {"glass", "white_stained_glass", "orange_stained_glass", "magenta_stained_glass", "light_blue_stained_glass", "yellow_stained_glass", "lime_stained_glass", "pink_stained_glass", "gray_stained_glass", "light_gray_stained_glass", "cyan_stained_glass", "purple_stained_glass", "blue_stained_glass", "brown_stained_glass", "green_stained_glass", "red_stained_glass", "black_stained_glass", "tinted_glass"};
            for (int i = 0; i < colors.length; i++) {
                int id = 338 + i * 2;
                entries[id - 1] = new Entry(id, "minecraft:" + colors[i], colors[i], colors[i]);
                entries[id] = new Entry(id + 1, "minecraft:" + colors[i], colors[i] + " conectable", colors[i] + " connected");
            }
            var names = new java.util.HashSet<String>();
            for (int i = 0; i < entries.length; i++) {
                Entry entry = entries[i];
                if (entry.paintId() != i + 1 || (!names.add(entry.blockId()) && !isConnectedGlass(entry.paintId())) || entry.nameEs().isBlank() || entry.nameEn().isBlank()) {
                    throw new IllegalStateException("Invalid or duplicate catalog entry " + (i + 1));
                }
                entry.identifier();
            }
            return List.copyOf(Arrays.asList(entries));
        } catch (java.io.IOException exception) { throw new IllegalStateException("Cannot read paint catalog", exception); }
    }
}
