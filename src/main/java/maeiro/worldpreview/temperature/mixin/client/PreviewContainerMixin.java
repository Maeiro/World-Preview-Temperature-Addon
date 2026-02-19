package maeiro.worldpreview.temperature.mixin.client;

import caeruleusTait.world.preview.RenderSettings;
import caeruleusTait.world.preview.WorldPreviewConfig;
import caeruleusTait.world.preview.client.gui.PreviewContainerDataProvider;
import caeruleusTait.world.preview.client.gui.screens.PreviewContainer;
import caeruleusTait.world.preview.client.gui.widgets.ToggleButton;
import caeruleusTait.world.preview.client.gui.widgets.lists.BiomesList;
import it.unimi.dsi.fastutil.shorts.Short2LongMap;
import maeiro.worldpreview.temperature.TemperatureAddonState;
import maeiro.worldpreview.temperature.WorldPreviewTemperatureAddon;
import maeiro.worldpreview.temperature.compat.BiomeListColorApplier;
import maeiro.worldpreview.temperature.compat.WorldPreviewConfigExt;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

@Mixin(value = PreviewContainer.class, remap = false)
public abstract class PreviewContainerMixin implements BiomeListColorApplier {
    @Shadow
    @Final
    private RenderSettings renderSettings;
    @Shadow
    @Final
    private ToggleButton toggleBiomes;
    @Shadow
    @Final
    private ToggleButton toggleNoise;
    @Shadow
    @Final
    private CycleButton<RenderSettings.RenderMode> noiseCycleButton;
    @Shadow
    @Final
    private BiomesList biomesList;
    @Shadow
    @Final
    private WorldPreviewConfig cfg;
    @Shadow
    @Final
    private List<AbstractWidget> toRender;
    @Shadow
    private BiomesList.BiomeEntry[] allBiomes;

    @Shadow
    private void selectViewMode(RenderSettings.RenderMode mode) {
        throw new AssertionError();
    }

    @Unique
    private static final float worldPreviewTemperatureAddon$BIOME_TEMP_MIN = -1.0f;
    @Unique
    private static final float worldPreviewTemperatureAddon$BIOME_TEMP_MAX = 2.0f;
    @Unique
    private static final ResourceLocation worldPreviewTemperatureAddon$BUTTON_TEXTURE = new ResourceLocation(
            WorldPreviewTemperatureAddon.MOD_ID,
            "textures/gui/biome_temperature_toggle.png"
    );
    @Unique
    private static final Component worldPreviewTemperatureAddon$BTN_ENABLED_TOOLTIP = Component.translatable(
            "world_preview_temperature_addon.preview.btn-toggle-biome-temperature"
    );
    @Unique
    private static final Component worldPreviewTemperatureAddon$BTN_DISABLED_TOOLTIP = Component.translatable(
            "world_preview_temperature_addon.preview.btn-toggle-biome-temperature.disabled"
    );

    @Unique
    private ToggleButton worldPreviewTemperatureAddon$toggleBiomeTemperature;
    @Unique
    private int[] worldPreviewTemperatureAddon$biomeTemperatureListColors = new int[0];
    @Unique
    private int[] worldPreviewTemperatureAddon$baseBiomeListColors = new int[0];
    @Unique
    private Field worldPreviewTemperatureAddon$biomeEntryColorField;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void worldPreviewTemperatureAddon$addButton(Screen screen, PreviewContainerDataProvider provider, CallbackInfo ci) {
        worldPreviewTemperatureAddon$toggleBiomeTemperature = new ToggleButton(
                0, 0, 20, 20,
                0, 0, 20, 20,
                worldPreviewTemperatureAddon$BUTTON_TEXTURE, 40, 60,
                x -> worldPreviewTemperatureAddon$onToggleButtonPressed()
        ) {
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                // Map states to the user-provided atlas order:
                // 1-2
                // 3-4
                // 5-6
                // disabled -> 2
                // enabled+off -> 4
                // enabled+off+hover -> 6
                // enabled+on -> 3
                // enabled+on+hover -> 5
                int texX;
                int texY;
                if (!this.isActive()) {
                    texX = this.xTexStart + this.xDiff;
                    texY = this.yTexStart;
                } else if (this.selected) {
                    texX = this.xTexStart;
                    texY = this.yTexStart + (this.isHoveredOrFocused() ? this.yDiffTex * 2 : this.yDiffTex);
                } else {
                    texX = this.xTexStart + this.xDiff;
                    texY = this.yTexStart + (this.isHoveredOrFocused() ? this.yDiffTex * 2 : this.yDiffTex);
                }

                RenderSystem.disableDepthTest();
                guiGraphics.blit(
                        this.resourceLocation,
                        this.getX(),
                        this.getY(),
                        texX,
                        texY,
                        this.width,
                        this.height,
                        this.textureWidth,
                        this.textureHeight
                );
            }
        };
        worldPreviewTemperatureAddon$toggleBiomeTemperature.visible = false;
        worldPreviewTemperatureAddon$toggleBiomeTemperature.active = false;
        toRender.add(worldPreviewTemperatureAddon$toggleBiomeTemperature);
        worldPreviewTemperatureAddon$syncToggleEnabledState();
        worldPreviewTemperatureAddon$syncToggleSelection();
    }

    @Inject(method = "selectViewMode", at = @At("HEAD"), remap = false)
    private void worldPreviewTemperatureAddon$clearTemperatureMode(RenderSettings.RenderMode mode, CallbackInfo ci) {
        TemperatureAddonState.setBiomeTemperatureMode(false);
    }

    @Inject(method = "selectViewMode", at = @At("TAIL"), remap = false)
    private void worldPreviewTemperatureAddon$afterSelectViewMode(RenderSettings.RenderMode mode, CallbackInfo ci) {
        worldPreviewTemperatureAddon$syncToggleSelection();
        worldPreviewTemperatureAddon$applyBiomeListDisplayColors(biomesList.children());
    }

    @Inject(method = "updateSettings_real", at = @At("TAIL"), remap = false)
    private void worldPreviewTemperatureAddon$afterUpdateSettings(WorldCreationContext wcContext, CallbackInfo ci) {
        worldPreviewTemperatureAddon$rebuildBiomeTemperatureListColors();
        worldPreviewTemperatureAddon$syncToggleEnabledState();
        worldPreviewTemperatureAddon$syncToggleSelection();
        worldPreviewTemperatureAddon$applyBiomeListDisplayColors(biomesList.children());
    }

    @Inject(method = "onVisibleBiomesChanged", at = @At("TAIL"), remap = false)
    private void worldPreviewTemperatureAddon$afterVisibleBiomesChanged(Short2LongMap visibleBiomes, CallbackInfo ci) {
        worldPreviewTemperatureAddon$applyBiomeListDisplayColors(biomesList.children());
    }

    @Inject(method = "onBiomeVisuallySelected", at = @At("TAIL"), remap = false)
    private void worldPreviewTemperatureAddon$afterBiomeVisuallySelected(BiomesList.BiomeEntry entry, CallbackInfo ci) {
        worldPreviewTemperatureAddon$applyBiomeListDisplayColors(biomesList.children());
    }

    @Inject(method = "doLayout", at = @At("TAIL"), remap = false)
    private void worldPreviewTemperatureAddon$adjustLayout(ScreenRectangle screenRectangle, CallbackInfo ci) {
        if (worldPreviewTemperatureAddon$toggleBiomeTemperature == null) {
            return;
        }

        int noiseX = toggleNoise.getX();
        int noiseY = toggleNoise.getY();
        worldPreviewTemperatureAddon$toggleBiomeTemperature.setPosition(noiseX + 22, noiseY);
        noiseCycleButton.setPosition(noiseX + 44, noiseY);
        worldPreviewTemperatureAddon$toggleBiomeTemperature.visible = toggleNoise.visible;

        worldPreviewTemperatureAddon$syncToggleEnabledState();
        worldPreviewTemperatureAddon$syncToggleSelection();
    }

    @Override
    public void worldPreviewTemperatureAddon$applyBiomeListDisplayColors(Collection<BiomesList.BiomeEntry> entries) {
        if (entries == null) {
            return;
        }
        short selected = biomesList.getSelected() == null ? -1 : biomesList.getSelected().id();
        boolean tempMode = worldPreviewTemperatureAddon$isTemperatureViewActive();
        for (BiomesList.BiomeEntry entry : entries) {
            if (entry.id() < 0 || entry.id() >= worldPreviewTemperatureAddon$baseBiomeListColors.length) {
                continue;
            }
            int color = worldPreviewTemperatureAddon$baseBiomeListColors[entry.id()];
            if (tempMode && entry.id() < worldPreviewTemperatureAddon$biomeTemperatureListColors.length) {
                color = worldPreviewTemperatureAddon$biomeTemperatureListColors[entry.id()];
                if (selected >= 0 && selected != entry.id()) {
                    color = worldPreviewTemperatureAddon$toGrayScale(color);
                }
            }
            worldPreviewTemperatureAddon$setBiomeEntryColor(entry, color);
        }
    }

    @Unique
    private void worldPreviewTemperatureAddon$onToggleButtonPressed() {
        if (!worldPreviewTemperatureAddon$config().worldPreviewTemperatureAddon$enableBiomeTemperatureView()) {
            TemperatureAddonState.setBiomeTemperatureMode(false);
            worldPreviewTemperatureAddon$syncToggleSelection();
            return;
        }
        selectViewMode(RenderSettings.RenderMode.BIOMES);
        TemperatureAddonState.setBiomeTemperatureMode(true);
        worldPreviewTemperatureAddon$syncToggleSelection();
        worldPreviewTemperatureAddon$applyBiomeListDisplayColors(biomesList.children());
    }

    @Unique
    private boolean worldPreviewTemperatureAddon$isTemperatureViewActive() {
        return worldPreviewTemperatureAddon$config().worldPreviewTemperatureAddon$enableBiomeTemperatureView()
                && TemperatureAddonState.isBiomeTemperatureMode()
                && renderSettings.mode == RenderSettings.RenderMode.BIOMES;
    }

    @Unique
    private void worldPreviewTemperatureAddon$syncToggleEnabledState() {
        if (worldPreviewTemperatureAddon$toggleBiomeTemperature == null) {
            return;
        }
        boolean enabled = worldPreviewTemperatureAddon$config().worldPreviewTemperatureAddon$enableBiomeTemperatureView();
        worldPreviewTemperatureAddon$toggleBiomeTemperature.active = enabled;
        worldPreviewTemperatureAddon$toggleBiomeTemperature.setTooltip(Tooltip.create(
                enabled ? worldPreviewTemperatureAddon$BTN_ENABLED_TOOLTIP : worldPreviewTemperatureAddon$BTN_DISABLED_TOOLTIP
        ));
        if (!enabled) {
            TemperatureAddonState.setBiomeTemperatureMode(false);
        }
    }

    @Unique
    private void worldPreviewTemperatureAddon$syncToggleSelection() {
        if (worldPreviewTemperatureAddon$toggleBiomeTemperature == null) {
            return;
        }
        boolean selected = worldPreviewTemperatureAddon$isTemperatureViewActive();
        worldPreviewTemperatureAddon$toggleBiomeTemperature.selected = selected;
        if (selected) {
            toggleBiomes.selected = false;
        }
    }

    @Unique
    private void worldPreviewTemperatureAddon$rebuildBiomeTemperatureListColors() {
        if (allBiomes == null || allBiomes.length == 0) {
            worldPreviewTemperatureAddon$biomeTemperatureListColors = new int[0];
            return;
        }
        worldPreviewTemperatureAddon$biomeTemperatureListColors = new int[allBiomes.length];
        worldPreviewTemperatureAddon$baseBiomeListColors = new int[allBiomes.length];
        WorldPreviewConfigExt cfgExt = worldPreviewTemperatureAddon$config();
        for (short i = 0; i < allBiomes.length; ++i) {
            worldPreviewTemperatureAddon$baseBiomeListColors[i] = allBiomes[i].color();
            float baseTemperature = allBiomes[i].entry().value().getBaseTemperature();
            boolean oceanGray = cfgExt.worldPreviewTemperatureAddon$biomeTemperatureOceanAsGray() && allBiomes[i].entry().is(BiomeTags.IS_OCEAN);
            worldPreviewTemperatureAddon$biomeTemperatureListColors[i] = worldPreviewTemperatureAddon$biomeTemperatureToColor(baseTemperature, oceanGray);
        }
    }

    @Unique
    private static int worldPreviewTemperatureAddon$toGrayScale(int rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        int gray = Math.max(32, Math.min(224, (red + green + blue) / 3));
        return (gray << 16) | (gray << 8) | gray;
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
    private void worldPreviewTemperatureAddon$setBiomeEntryColor(BiomesList.BiomeEntry entry, int color) {
        try {
            if (worldPreviewTemperatureAddon$biomeEntryColorField == null) {
                worldPreviewTemperatureAddon$biomeEntryColorField = BiomesList.BiomeEntry.class.getDeclaredField("color");
                worldPreviewTemperatureAddon$biomeEntryColorField.setAccessible(true);
            }
            worldPreviewTemperatureAddon$biomeEntryColorField.setInt(entry, color & 0x00FFFFFF);
        } catch (Throwable ignored) {
        }
    }

    @Unique
    private WorldPreviewConfigExt worldPreviewTemperatureAddon$config() {
        return (WorldPreviewConfigExt) cfg;
    }
}
