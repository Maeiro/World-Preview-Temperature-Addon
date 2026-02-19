package maeiro.worldpreview.temperature.mixin.client;

import caeruleusTait.world.preview.client.gui.screens.PreviewContainer;
import caeruleusTait.world.preview.client.gui.widgets.lists.BiomesList;
import maeiro.worldpreview.temperature.compat.BiomeListColorApplier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BiomesList.class, remap = false)
public abstract class BiomesListMixin {
    @Shadow
    @Final
    private PreviewContainer previewContainer;

    @Inject(method = "setSelected(LcaeruleusTait/world/preview/client/gui/widgets/lists/BiomesList$BiomeEntry;Z)V", at = @At("TAIL"), remap = false)
    private void worldPreviewTemperatureAddon$afterSetSelected(BiomesList.BiomeEntry entry, boolean centerScroll, CallbackInfo ci) {
        if (previewContainer instanceof BiomeListColorApplier colorApplier) {
            BiomesList self = (BiomesList) (Object) this;
            colorApplier.worldPreviewTemperatureAddon$applyBiomeListDisplayColors(self.children());
        }
    }
}
