package nl.tabuu.headz.data;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import nl.tabuu.tabuucore.material.XMaterial;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Head implements Serializable {

    private static final long serialVersionUID = 4232489724376L;

    @Expose
    @SerializedName("name")
    String _name;

    @Expose
    @SerializedName("value")
    private String _textureValue;

    private ItemStack _item;
    private List<String> _tags;
    private HeadCategory _category;

    public Head(String name, HeadCategory category, String value, String... tags){
       _name = name;
       _textureValue = value;
       _tags = Arrays.asList(tags);
       _category = category;

       _item = null;
    }

    public ItemStack getItemStack(){
        if(_item != null)
            return _item;

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        PropertyMap propertyMap = profile.getProperties();

        if(propertyMap == null)
            throw new IllegalStateException("Could not find property map.");

        propertyMap.put("textures", new Property("textures", _textureValue));
        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
        ItemMeta headMeta = head.getItemMeta();
        Class<?> headMetaClass = headMeta.getClass();

        try {
            Field profileField = headMetaClass.getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (NoSuchFieldException | IllegalAccessException ignore) {}

        headMeta.setDisplayName(ChatColor.RESET + _name);
        head.setItemMeta(headMeta);

        return head;
    }

    public String getName(){
        return _name;
    }

    public List<String> getTags(){
        return _tags;
    }

    public HeadCategory getCategory(){
        return _category;
    }

    public String getTextureValue(){
        return _textureValue;
    }

    public void setCategory(HeadCategory category){
        _category = category;
    }

    static class Deserializer implements JsonDeserializer<Head> {

        @Override
        public Head deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            JsonObject headObject = jsonElement.getAsJsonObject();
            JsonElement element = headObject.get("tags");
            Head head = gson.fromJson(headObject, Head.class);

            head._tags = element.isJsonNull() ? Collections.emptyList() :
                                                Arrays.asList(element.getAsString().split(","));
            return head;
        }
    }
}
