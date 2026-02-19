package maeiro.worldpreview.temperature.mixin.client;

import caeruleusTait.world.preview.WorldPreview;
import caeruleusTait.world.preview.client.gui.screens.PreviewContainer;
import caeruleusTait.world.preview.client.gui.screens.settings.GeneralTab;
import caeruleusTait.world.preview.client.gui.widgets.WGCheckbox;
import caeruleusTait.world.preview.client.gui.widgets.WGLabel;
import maeiro.worldpreview.temperature.compat.WorldPreviewConfigExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GeneralTab.class, remap = false)
public abstract class GeneralTabMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void worldPreviewTemperatureAddon$appendGeneralSettings(Minecraft minecraft, CallbackInfo ci) {
        GridLayout layout = ((GridLayoutTabAccessor) this).worldPreviewTemperatureAddon$getLayout();
        WorldPreviewConfigExt cfg = (WorldPreviewConfigExt) WorldPreview.get().cfg();

        int lineWidth = 320;
        WGCheckbox enableBiomeTemperatureView = new WGCheckbox(
                0, 0,
                lineWidth,
                PreviewContainer.LINE_HEIGHT,
                Component.translatable("world_preview_temperature_addon.settings.general.biome_temp"),
                x -> cfg.worldPreviewTemperatureAddon$setEnableBiomeTemperatureView(x.selected()),
                cfg.worldPreviewTemperatureAddon$enableBiomeTemperatureView()
        );
        enableBiomeTemperatureView.setTooltip(Tooltip.create(Component.translatable("world_preview_temperature_addon.settings.general.biome_temp.tooltip")));

        WGCheckbox oceanAsGray = new WGCheckbox(
                0, 0,
                lineWidth,
                PreviewContainer.LINE_HEIGHT,
                Component.translatable("world_preview_temperature_addon.settings.general.biome_temp_ocean_gray"),
                x -> cfg.worldPreviewTemperatureAddon$setBiomeTemperatureOceanAsGray(x.selected()),
                cfg.worldPreviewTemperatureAddon$biomeTemperatureOceanAsGray()
        );
        oceanAsGray.setTooltip(Tooltip.create(Component.translatable("world_preview_temperature_addon.settings.general.biome_temp_ocean_gray.tooltip")));

        layout.addChild(new WGLabel(
                minecraft.font,
                0, 0,
                lineWidth,
                PreviewContainer.LINE_HEIGHT / 10,
                WGLabel.TextAlignment.CENTER,
                Component.literal(""),
                0xFFFFFF
        ), 9, 0, 1, 2);
        layout.addChild(enableBiomeTemperatureView, 10, 0, 1, 2);
        layout.addChild(oceanAsGray, 11, 0, 1, 2);

        // We inject after the original tab constructor finished, so we must reflow.
        layout.arrangeElements();
    }
}
