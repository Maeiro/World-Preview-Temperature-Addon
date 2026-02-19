package maeiro.worldpreview.temperature.mixin.client;

import caeruleusTait.world.preview.RenderSettings;
import caeruleusTait.world.preview.WorldPreviewConfig;
import caeruleusTait.world.preview.client.gui.PreviewDisplayDataProvider;
import caeruleusTait.world.preview.client.gui.widgets.PreviewDisplay;
import caeruleusTait.world.preview.client.gui.widgets.lists.BiomesList;
import maeiro.worldpreview.temperature.TemperatureAddonState;
import maeiro.worldpreview.temperature.compat.WorldPreviewConfigExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BiomeTags;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.util.List;

@Mixin(value = PreviewDisplay.class, remap = false)
public abstract class PreviewDisplayMixin {
    @Shadow
    @Final
    private PreviewDisplayDataProvider dataProvider;
    @Shadow
    @Final
    private RenderSettings renderSettings;
    @Shadow
    @Final
    private WorldPreviewConfig config;
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private int[] colorMap;
    @Shadow
    private int[] colorMapGrayScale;

    @Shadow
    private static int textureColor(int orig) {
        throw new AssertionError();
    }

    @Shadow
    private static int grayScale(int orig) {
        throw new AssertionError();
    }

    @Unique
    private static final float worldPreviewTemperatureAddon$BIOME_TEMP_MIN = -1.0f;
    @Unique
    private static final float worldPreviewTemperatureAddon$BIOME_TEMP_MAX = 2.0f;

    @Unique
    private int[] worldPreviewTemperatureAddon$temperatureColorMap;
    @Unique
    private int[] worldPreviewTemperatureAddon$temperatureColorMapGrayScale;
    @Unique
    private int[] worldPreviewTemperatureAddon$originalColorMap;
    @Unique
    private int[] worldPreviewTemperatureAddon$originalColorMapGrayScale;
    @Unique
    private boolean worldPreviewTemperatureAddon$swappedColorMaps;
    @Unique
    private Method worldPreviewTemperatureAddon$hoveredBiomeMethod;
    @Unique
    private Method worldPreviewTemperatureAddon$hoveredStructuresMethod;

    @Inject(method = "reloadData", at = @At("TAIL"), remap = false)
    private void worldPreviewTemperatureAddon$afterReloadData(CallbackInfo ci) {
        if (colorMap == null) {
            worldPreviewTemperatureAddon$temperatureColorMap = null;
            worldPreviewTemperatureAddon$temperatureColorMapGrayScale = null;
            return;
        }

        worldPreviewTemperatureAddon$temperatureColorMap = new int[colorMap.length];
        worldPreviewTemperatureAddon$temperatureColorMapGrayScale = new int[colorMap.length];
        for (short i = 0; i < colorMap.length; ++i) {
            BiomesList.BiomeEntry entry = dataProvider.biome4Id(i);
            if (entry == null) {
                worldPreviewTemperatureAddon$temperatureColorMap[i] = 0xFF000000;
                worldPreviewTemperatureAddon$temperatureColorMapGrayScale[i] = 0xFF000000;
                continue;
            }
            float baseTemperature = entry.entry().value().getBaseTemperature();
            boolean oceanGray = worldPreviewTemperatureAddon$config().worldPreviewTemperatureAddon$biomeTemperatureOceanAsGray()
                    && entry.entry().is(BiomeTags.IS_OCEAN);
            int rgb = worldPreviewTemperatureAddon$biomeTemperatureToColor(baseTemperature, oceanGray);
            int textureColor = textureColor(rgb);
            worldPreviewTemperatureAddon$temperatureColorMap[i] = textureColor;
            worldPreviewTemperatureAddon$temperatureColorMapGrayScale[i] = grayScale(textureColor);
        }
    }

    @Inject(method = "updateTexture", at = @At("HEAD"), remap = false)
    private void worldPreviewTemperatureAddon$beforeUpdateTexture(List<?> renderData, CallbackInfo ci) {
        if (!worldPreviewTemperatureAddon$shouldUseTemperatureColors()) {
            return;
        }
        if (worldPreviewTemperatureAddon$temperatureColorMap == null || worldPreviewTemperatureAddon$temperatureColorMapGrayScale == null) {
            return;
        }

        worldPreviewTemperatureAddon$originalColorMap = colorMap;
        worldPreviewTemperatureAddon$originalColorMapGrayScale = colorMapGrayScale;
        colorMap = worldPreviewTemperatureAddon$temperatureColorMap;
        colorMapGrayScale = worldPreviewTemperatureAddon$temperatureColorMapGrayScale;
        worldPreviewTemperatureAddon$swappedColorMaps = true;
    }

    @Inject(method = "updateTexture", at = @At("TAIL"), remap = false)
    private void worldPreviewTemperatureAddon$afterUpdateTexture(List<?> renderData, CallbackInfo ci) {
        if (!worldPreviewTemperatureAddon$swappedColorMaps) {
            return;
        }
        colorMap = worldPreviewTemperatureAddon$originalColorMap;
        colorMapGrayScale = worldPreviewTemperatureAddon$originalColorMapGrayScale;
        worldPreviewTemperatureAddon$swappedColorMaps = false;
    }

    @Inject(method = "updateTooltip", at = @At("TAIL"), remap = false)
    private void worldPreviewTemperatureAddon$appendBaseTemperatureToTooltip(double mouseX, double mouseY, CallbackInfo ci) {
        try {
            Object hoverInfo = worldPreviewTemperatureAddon$invokeHoveredBiome(mouseX, mouseY);
            if (hoverInfo == null) {
                return;
            }
            List<?> structures = worldPreviewTemperatureAddon$invokeHoveredStructures(mouseX, mouseY);
            if (structures != null && !structures.isEmpty()) {
                return;
            }

            BiomesList.BiomeEntry entry = (BiomesList.BiomeEntry) worldPreviewTemperatureAddon$recordComponent(hoverInfo, "entry");
            int blockX = ((Number) worldPreviewTemperatureAddon$recordComponent(hoverInfo, "blockX")).intValue();
            int blockY = ((Number) worldPreviewTemperatureAddon$recordComponent(hoverInfo, "blockY")).intValue();
            int blockZ = ((Number) worldPreviewTemperatureAddon$recordComponent(hoverInfo, "blockZ")).intValue();
            short height = ((Number) worldPreviewTemperatureAddon$recordComponent(hoverInfo, "height")).shortValue();

            double temperature = ((Number) worldPreviewTemperatureAddon$recordComponent(hoverInfo, "temperature")).doubleValue();
            double humidity = ((Number) worldPreviewTemperatureAddon$recordComponent(hoverInfo, "humidity")).doubleValue();
            double continentalness = ((Number) worldPreviewTemperatureAddon$recordComponent(hoverInfo, "continentalness")).doubleValue();
            double erosion = ((Number) worldPreviewTemperatureAddon$recordComponent(hoverInfo, "erosion")).doubleValue();
            double depth = ((Number) worldPreviewTemperatureAddon$recordComponent(hoverInfo, "depth")).doubleValue();
            double weirdness = ((Number) worldPreviewTemperatureAddon$recordComponent(hoverInfo, "weirdness")).doubleValue();
            double pv = ((Number) worldPreviewTemperatureAddon$recordComponent(hoverInfo, "pv")).doubleValue();

            double biomeBaseTemperature = Double.NaN;
            if (entry != null) {
                biomeBaseTemperature = entry.entry().value().getBaseTemperature();
            }

            String blockPosTemplate = "\u00A73X=\u00A7b%d\u00A7r \u00A73Y=\u00A7b%d\u00A7r \u00A73Z=\u00A7b%d\u00A7r";
            String heightText = height > Short.MIN_VALUE ? String.format("\u00A7b%d\u00A7r", height) : "\u00A77<N/A>\u00A7r";

            String climate = "";
            if (!Double.isNaN(biomeBaseTemperature)) {
                climate = "\n\n\u00A73BT=\u00A7b%.2f\u00A7r".formatted(biomeBaseTemperature);
            }
            if (!Double.isNaN(temperature)) {
                climate += "\n\u00A73T=\u00A7b%.2f\u00A7r \u00A73H=\u00A7b%.2f\u00A7r \u00A73C=\u00A7b%.2f\u00A7r\n"
                        + "\u00A73E=\u00A7b%.2f\u00A7r \u00A73D=\u00A7b%.2f\u00A7r \u00A73W=\u00A7b%.2f\u00A7r\n"
                        + "\u00A73PV=\u00A7b%.2f\u00A7r".formatted(
                        temperature, humidity, continentalness, erosion, depth, weirdness, pv
                );
            }

            String name = entry == null ? "<N/A>" : entry.name();
            Component tooltip = Component.translatable(
                    config.showControls ? "world_preview.preview-display.tooltip.controls" : "world_preview.preview-display.tooltip",
                    worldPreviewTemperatureAddon$nameFormatter(name),
                    blockPosTemplate.formatted(blockX, blockY, blockZ),
                    heightText,
                    climate
            );
            worldPreviewTemperatureAddon$setTooltipNow(Tooltip.create(tooltip));
        } catch (Throwable ignored) {
        }
    }

    @Unique
    private boolean worldPreviewTemperatureAddon$shouldUseTemperatureColors() {
        return worldPreviewTemperatureAddon$config().worldPreviewTemperatureAddon$enableBiomeTemperatureView()
                && TemperatureAddonState.isBiomeTemperatureMode()
                && renderSettings.mode == RenderSettings.RenderMode.BIOMES;
    }

    @Unique
    private static String worldPreviewTemperatureAddon$nameFormatter(String value) {
        int idx = value.indexOf(':');
        if (idx < 0) {
            return "\u00A7e" + value + "\u00A7r";
        }
        return "\u00A75\u00A7o" + value.substring(0, idx) + "\u00A7r\u00A75:" + value.substring(idx + 1) + "\u00A7r";
    }

    @Unique
    private Object worldPreviewTemperatureAddon$invokeHoveredBiome(double mouseX, double mouseY) throws ReflectiveOperationException {
        if (worldPreviewTemperatureAddon$hoveredBiomeMethod == null) {
            worldPreviewTemperatureAddon$hoveredBiomeMethod = PreviewDisplay.class.getDeclaredMethod("hoveredBiome", double.class, double.class);
            worldPreviewTemperatureAddon$hoveredBiomeMethod.setAccessible(true);
        }
        return worldPreviewTemperatureAddon$hoveredBiomeMethod.invoke(this, mouseX, mouseY);
    }

    @Unique
    private List<?> worldPreviewTemperatureAddon$invokeHoveredStructures(double mouseX, double mouseY) throws ReflectiveOperationException {
        if (worldPreviewTemperatureAddon$hoveredStructuresMethod == null) {
            worldPreviewTemperatureAddon$hoveredStructuresMethod = PreviewDisplay.class.getDeclaredMethod("hoveredStructures", double.class, double.class);
            worldPreviewTemperatureAddon$hoveredStructuresMethod.setAccessible(true);
        }
        return (List<?>) worldPreviewTemperatureAddon$hoveredStructuresMethod.invoke(this, mouseX, mouseY);
    }

    @Unique
    private Object worldPreviewTemperatureAddon$recordComponent(Object record, String name) throws ReflectiveOperationException {
        Method method = record.getClass().getDeclaredMethod(name);
        method.setAccessible(true);
        return method.invoke(record);
    }

    @Unique
    private void worldPreviewTemperatureAddon$setTooltipNow(Tooltip tooltip) {
        if (minecraft.screen != null) {
            minecraft.screen.setTooltipForNextRenderPass(tooltip, DefaultTooltipPositioner.INSTANCE, true);
        }
    }

    @Unique
    private static int worldPreviewTemperatureAddon$lerpChannel(int from, int to, float t) {
        return Math.max(0, Math.min(255, (int) (from + ((to - from) * t))));
    }

    @Unique
    private static int worldPreviewTemperatureAddon$biomeTemperatureToColor(float baseTemperature, boolean oceanGray) {
        if (oceanGray) {
            return 0xC0C0C0;
        }

        float clamped = Math.max(
                worldPreviewTemperatureAddon$BIOME_TEMP_MIN,
                Math.min(worldPreviewTemperatureAddon$BIOME_TEMP_MAX, baseTemperature)
        );
        float normalized = (clamped - worldPreviewTemperatureAddon$BIOME_TEMP_MIN)
                / (worldPreviewTemperatureAddon$BIOME_TEMP_MAX - worldPreviewTemperatureAddon$BIOME_TEMP_MIN);

        if (normalized <= 0.5f) {
            float t = normalized / 0.5f;
            return (worldPreviewTemperatureAddon$lerpChannel(32, 70, t) << 16)
                    | (worldPreviewTemperatureAddon$lerpChannel(90, 185, t) << 8)
                    | worldPreviewTemperatureAddon$lerpChannel(220, 95, t);
        }
        if (normalized <= 0.75f) {
            float t = (normalized - 0.5f) / 0.25f;
            return (worldPreviewTemperatureAddon$lerpChannel(70, 220, t) << 16)
                    | (worldPreviewTemperatureAddon$lerpChannel(185, 205, t) << 8)
                    | worldPreviewTemperatureAddon$lerpChannel(95, 55, t);
        }
        float t = (normalized - 0.75f) / 0.25f;
        return (worldPreviewTemperatureAddon$lerpChannel(220, 215, t) << 16)
                | (worldPreviewTemperatureAddon$lerpChannel(205, 65, t) << 8)
                | worldPreviewTemperatureAddon$lerpChannel(55, 45, t);
    }

    @Unique
    private WorldPreviewConfigExt worldPreviewTemperatureAddon$config() {
        return (WorldPreviewConfigExt) config;
    }
}
