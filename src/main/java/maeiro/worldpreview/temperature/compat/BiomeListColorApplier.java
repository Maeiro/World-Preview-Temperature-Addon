package maeiro.worldpreview.temperature.compat;

import caeruleusTait.world.preview.client.gui.widgets.lists.BiomesList;

import java.util.Collection;

public interface BiomeListColorApplier {
    void worldPreviewTemperatureAddon$applyBiomeListDisplayColors(Collection<BiomesList.BiomeEntry> entries);
}
