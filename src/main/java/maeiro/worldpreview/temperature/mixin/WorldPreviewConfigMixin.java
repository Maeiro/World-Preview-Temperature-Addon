package maeiro.worldpreview.temperature.mixin;

import caeruleusTait.world.preview.WorldPreviewConfig;
import maeiro.worldpreview.temperature.compat.WorldPreviewConfigExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = WorldPreviewConfig.class, remap = false)
public abstract class WorldPreviewConfigMixin implements WorldPreviewConfigExt {
    @Unique
    public boolean worldPreviewTemperatureAddonEnableBiomeTemperatureView = true;
    @Unique
    public boolean worldPreviewTemperatureAddonBiomeTemperatureOceanAsGray = true;

    @Override
    public boolean worldPreviewTemperatureAddon$enableBiomeTemperatureView() {
        return worldPreviewTemperatureAddonEnableBiomeTemperatureView;
    }

    @Override
    public void worldPreviewTemperatureAddon$setEnableBiomeTemperatureView(boolean value) {
        worldPreviewTemperatureAddonEnableBiomeTemperatureView = value;
    }

    @Override
    public boolean worldPreviewTemperatureAddon$biomeTemperatureOceanAsGray() {
        return worldPreviewTemperatureAddonBiomeTemperatureOceanAsGray;
    }

    @Override
    public void worldPreviewTemperatureAddon$setBiomeTemperatureOceanAsGray(boolean value) {
        worldPreviewTemperatureAddonBiomeTemperatureOceanAsGray = value;
    }
}
