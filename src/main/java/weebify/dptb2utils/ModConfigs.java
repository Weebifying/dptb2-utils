package weebify.dptb2utils;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
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

    public static final Map<String, JsonElement> othersDefaultMap = new LinkedHashMap<>();
    public static final Map<String, JsonElement> notifsDefaultMap = new LinkedHashMap<>();
    public static final Map<String, JsonElement> buttonTimerDefaultMap = new LinkedHashMap<>();
    public static final Map<String, JsonElement> itemCooldownDefaultMap = new LinkedHashMap<>();
    public static final Map<String, JsonElement> waypointsDefaultMap = new LinkedHashMap<>();

    static <T> void createNewConfig(Map<String, JsonElement> map, Map<String, JsonElement> defaultMap, String key, String defaultValue, Class<T> clazz) {
        Type type = TypeToken.get(clazz).getType();

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
        createNewConfig(notifsMap, notifsDefaultMap, "shopUpdate", "false", Boolean.class);
        createNewConfig(notifsMap, notifsDefaultMap, "bootsCollected", "false", Boolean.class);
        createNewConfig(notifsMap, notifsDefaultMap, "slimeBoots", "true", Boolean.class);
        createNewConfig(notifsMap, notifsDefaultMap, "doorSwitch", "false", Boolean.class);
        createNewConfig(notifsMap, notifsDefaultMap, "buttonMayhem", "false", Boolean.class);
        createNewConfig(notifsMap, notifsDefaultMap, "buttonDisable", "false", Boolean.class);
        createNewConfig(notifsMap, notifsDefaultMap, "buttonImmunity", "false", Boolean.class);

        createNewConfig(notifsMap, notifsDefaultMap, "dontDelaySfx", "false", Boolean.class);

        createNewConfig(othersMap, othersDefaultMap, "autoCheer", "false", Boolean.class);
        createNewConfig(othersMap, othersDefaultMap, "discordRamper", "false", Boolean.class);
        createNewConfig(othersMap, othersDefaultMap, "dptbotHost", "87.106.101.66", String.class);
        createNewConfig(othersMap, othersDefaultMap, "dptbotPort", "6212", Integer.class);
        createNewConfig(othersMap, othersDefaultMap, "broadcastToast", "true", Boolean.class);
        createNewConfig(othersMap, othersDefaultMap, "broadcastChat", "true", Boolean.class);

        createNewConfig(buttonTimerMap, buttonTimerDefaultMap, "enabled", "false", Boolean.class);
        createNewConfig(buttonTimerMap, buttonTimerDefaultMap, "textShadow", "false", Boolean.class);
        createNewConfig(buttonTimerMap, buttonTimerDefaultMap, "renderBackground", "true", Boolean.class);
        createNewConfig(buttonTimerMap, buttonTimerDefaultMap, "posX", "0.5", Float.class);
        createNewConfig(buttonTimerMap, buttonTimerDefaultMap, "posY", "0.5", Float.class);

        createNewConfig(itemCooldownMap, itemCooldownDefaultMap, "enabled", "false", Boolean.class);
        createNewConfig(itemCooldownMap, itemCooldownDefaultMap, "posX", "0", Float.class);
        createNewConfig(itemCooldownMap, itemCooldownDefaultMap, "posY", "0.5", Float.class);

        createNewConfig(waypointsMap, waypointsDefaultMap, "enabled", "false", Boolean.class);
    }
}
