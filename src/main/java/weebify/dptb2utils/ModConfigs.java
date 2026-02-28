package weebify.dptb2utils;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModConfigs {
    // TODO: a map specifically for class of properties
    // like Map<String, Class<?>> propertyTypes = new LinkedHashMap<>();
    // propertyTypes.put("shopUpdate", Boolean.class);

    public Map<String, JsonElement> othersMap = new LinkedHashMap<>();
    public Map<String, JsonElement> notifsMap = new LinkedHashMap<>();
    public Map<String, JsonElement> buttonTimerMap = new LinkedHashMap<>();
    public Map<String, JsonElement> itemCooldownMap = new LinkedHashMap<>();
    public Map<String, JsonElement> waypointsMap = new LinkedHashMap<>();
    public Map<String, JsonElement> microTimerMap = new LinkedHashMap<>();


    public static final Map<String, JsonElement> othersDefaultMap = new LinkedHashMap<>();
    public static final Map<String, JsonElement> notifsDefaultMap = new LinkedHashMap<>();
    public static final Map<String, JsonElement> buttonTimerDefaultMap = new LinkedHashMap<>();
    public static final Map<String, JsonElement> itemCooldownDefaultMap = new LinkedHashMap<>();
    public static final Map<String, JsonElement> waypointsDefaultMap = new LinkedHashMap<>();
    public static final Map<String, JsonElement> microTimerDefaultMap = new LinkedHashMap<>();

    public static Map<String, Class<?>> propertyTypes = new LinkedHashMap<>();

    private Map<String, JsonElement>[] getMap(String prop) {
        String category = prop.split("\\.")[0];
        /*
        switch (category) {
            case "others":
                return new Map[]{othersMap, othersDefaultMap};
            case "notifs":
                return new Map[]{notifsMap, notifsDefaultMap};
            case "buttonTimer":
                return new Map[]{buttonTimerMap, buttonTimerDefaultMap};
            case "itemCooldown":
                return new Map[]{itemCooldownMap, itemCooldownDefaultMap};
            case "waypoints":
                return new Map[]{waypointsMap, waypointsDefaultMap};
            default:
                return null;
        }
        */
        if      ("others".equals(category))         return new Map[]{othersMap, othersDefaultMap};
        else if ("notifs".equals(category))         return new Map[]{notifsMap, notifsDefaultMap};
        else if ("buttonTimer".equals(category))    return new Map[]{buttonTimerMap, buttonTimerDefaultMap};
        else if ("itemCooldown".equals(category))   return new Map[]{itemCooldownMap, itemCooldownDefaultMap};
        else if ("waypoints".equals(category))      return new Map[]{waypointsMap, waypointsDefaultMap};
        else if ("microTimer".equals(category))      return new Map[]{microTimerMap, microTimerDefaultMap};
        else return null;
    }

    private <T> void createNewConfig(String prop, String defaultValue, Class<T> clazz) {
        String key = prop.split("\\.")[1];
        Map<String, JsonElement>[] maps = getMap(prop);
        Map<String, JsonElement> map = maps[0];
        Map<String, JsonElement> defaultMap = maps[1];
        Type type = TypeToken.get(clazz).getType();

        propertyTypes.put(prop, clazz);
        try {
            T TDefaultValue = DPTB2Utils.GSON.fromJson(defaultValue, type);
            map.put(key, DPTB2Utils.GSON.toJsonTree(TDefaultValue));
            defaultMap.put(key, DPTB2Utils.GSON.toJsonTree(TDefaultValue));
        } catch (Exception e) {
            map.put(key, DPTB2Utils.GSON.toJsonTree(defaultValue));
            defaultMap.put(key, DPTB2Utils.GSON.toJsonTree(defaultValue));
        }
    }

    public ModConfigs() {
        createNewConfig("notifs.shopUpdate", "false", Boolean.class);
        createNewConfig("notifs.bootsCollected", "false", Boolean.class);
        createNewConfig("notifs.slimeBoots", "true", Boolean.class);
        createNewConfig("notifs.doorSwitch", "false", Boolean.class);
        createNewConfig("notifs.buttonMayhem", "false", Boolean.class);
        createNewConfig("notifs.buttonDisable", "false", Boolean.class);
        createNewConfig("notifs.buttonImmunity", "false", Boolean.class);
        createNewConfig("notifs.dontDelaySfx", "false", Boolean.class);

        createNewConfig("others.autoCheer", "false", Boolean.class);

        // mistakes were made.
        createNewConfig("others.discordRamper", "true", Boolean.class);
        createNewConfig("others.consentRamper", "true", Boolean.class);
        createNewConfig("others.dptbotHost", "87.106.105.24", String.class);
        createNewConfig("others.dptbotPort", "10092", Integer.class);
        createNewConfig("others.broadcastToast", "false", Boolean.class);
        createNewConfig("others.broadcastChat", "true", Boolean.class);
        createNewConfig("others.woahSecretSetting", "false", Boolean.class); // lol
        createNewConfig("others.indicatorPath", "textures/indicator/icon.png", String.class);
        createNewConfig("others.incognito", "false", Boolean.class);
        createNewConfig("others.discColor", "5555FF", String.class);
        createNewConfig("others.wptbColor", "D2FFC8", String.class);
        createNewConfig("others.discBlocks", "[]", List.class);
        createNewConfig("others.wptbBlocks", "[]", List.class);
        createNewConfig("others.broadcastSounds", "true", Boolean.class);

        createNewConfig("buttonTimer.enabled", "false", Boolean.class);
        createNewConfig("buttonTimer.textShadow", "false", Boolean.class);
        createNewConfig("buttonTimer.renderBackground", "true", Boolean.class);
        createNewConfig("buttonTimer.posX", "0.5", Float.class);
        createNewConfig("buttonTimer.posY", "0.5", Float.class);

        createNewConfig("itemCooldown.enabled", "false", Boolean.class);
        createNewConfig("itemCooldown.textShadow", "false", Boolean.class);
        createNewConfig("itemCooldown.renderBackground", "true", Boolean.class);
        createNewConfig("itemCooldown.textAlign", "left", String.class);
        createNewConfig("itemCooldown.posX", "0", Float.class);
        createNewConfig("itemCooldown.posY", "0.5", Float.class);

        createNewConfig("microTimer.enabled", "false", Boolean.class);
        createNewConfig("microTimer.textShadow", "false", Boolean.class);
        createNewConfig("microTimer.renderBackground", "true", Boolean.class);
        createNewConfig("microTimer.posX", "0.25", Float.class);
        createNewConfig("microTimer.posY", "0.5", Float.class);

        createNewConfig("waypoints.enabled", "false", Boolean.class);
    }

    public <T> T getConfig(String prop) {
        Map<String, JsonElement>[] maps = getMap(prop);
        String key = prop.split("\\.")[1];
        if (maps == null) return null;
        Map<String, JsonElement> map = maps[0];
        Map<String, JsonElement> defaultMap = maps[1];
        Class<T> clazz = (Class<T>) propertyTypes.get(prop);

        if (!map.containsKey(key)) {
            map.put(key, defaultMap.get(key));
        }
        return DPTB2Utils.GSON.fromJson(map.get(key), clazz);
    }

    public <T> T setConfig(String prop, T value) {
        Map<String, JsonElement>[] maps = getMap(prop);
        String key = prop.split("\\.")[1];
        if (maps == null) return null;
        Map<String, JsonElement> map = maps[0];
        Map<String, JsonElement> defaultMap = maps[1];
        Class<T> clazz = (Class<T>) propertyTypes.get(prop);

        JsonElement jsonValue = DPTB2Utils.GSON.toJsonTree(value, clazz);
        JsonElement oldValue = map.put(key, jsonValue);
        return oldValue == null ? null : DPTB2Utils.GSON.fromJson(oldValue, clazz);
    }

    public <T> T getDefaultConfig(String prop) {
        Map<String, JsonElement>[] maps = getMap(prop);
        String key = prop.split("\\.")[1];
        if (maps == null) return null;
        Map<String, JsonElement> defaultMap = maps[1];
        Class<T> clazz = (Class<T>) propertyTypes.get(prop);

        return DPTB2Utils.GSON.fromJson(defaultMap.get(key), clazz);
    }
}
