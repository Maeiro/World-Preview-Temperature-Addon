package maeiro.worldpreview.temperature;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(WorldPreviewTemperatureAddon.MOD_ID)
public class WorldPreviewTemperatureAddon {
    public static final String MOD_ID = "world_preview_temperature_addon";
    public static final Logger LOGGER = LogUtils.getLogger();

    public WorldPreviewTemperatureAddon() {
        LOGGER.info("World Preview Temperature Addon initialized");
    }
}
