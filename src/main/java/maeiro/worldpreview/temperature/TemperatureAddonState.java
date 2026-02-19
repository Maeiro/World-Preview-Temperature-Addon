package maeiro.worldpreview.temperature;

public final class TemperatureAddonState {
    private static boolean biomeTemperatureMode;

    private TemperatureAddonState() {
    }

    public static boolean isBiomeTemperatureMode() {
        return biomeTemperatureMode;
    }

    public static void setBiomeTemperatureMode(boolean biomeTemperatureMode) {
        TemperatureAddonState.biomeTemperatureMode = biomeTemperatureMode;
    }
}
