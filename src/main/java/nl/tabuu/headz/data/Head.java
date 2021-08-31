package nl.tabuu.headz.data;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import nl.tabuu.tabuucore.configuration.IDataHolder;
import nl.tabuu.tabuucore.material.XMaterial;
import nl.tabuu.tabuucore.serialization.ISerializable;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

public class Head implements ISerializable<IDataHolder> {

    @Expose
    @SerializedName("name")
    private final String _name;

    @Expose
    @SerializedName("value")
    private final String _textureValue;

    private final ItemStack _item;
    private HashSet<String> _tags;
    private HeadCategory _category;

    public Head(String name, HeadCategory category, String value, Collection<String> tags) {
        _name = name;
        _category = category;
        _textureValue = value;
        _tags = new HashSet<>(tags);

        _item = null;
    }

    public Head(IDataHolder data) {
        this(
                data.getString("Name"),
                data.get("Category", HeadCategory::valueOf),
                data.getString("TextureValue"),
                data.getStringList("Tags")
        );
    }

    public ItemStack getItemStack() {
        if (_item != null)
            return _item;

        GameProfile profile = new GameProfile(UUID.randomUUID(), "Tabuu");
        PropertyMap propertyMap = profile.getProperties();

        if (propertyMap == null)
            throw new IllegalStateException("Could not find property map.");

        propertyMap.put("textures", new Property("textures", _textureValue));
        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
        ItemMeta headMeta = head.getItemMeta();
        Class<?> headMetaClass = headMeta.getClass();

        try {
            Field profileField = headMetaClass.getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (NoSuchFieldException | IllegalAccessException ignore) { }

        headMeta.setDisplayName(ChatColor.RESET + _name);
        head.setItemMeta(headMeta);

        return head;
    }

    public String getName() {
        return _name;
    }

    public Set<String> getTags() {
        return _tags;
    }

    public HeadCategory getCategory() {
        return _category;
    }

    public String getTextureValue() {
        return _textureValue;
    }

    public void setCategory(HeadCategory category) {
        _category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Head head = (Head) o;
        return _name.equals(head._name) && _textureValue.equals(head._textureValue) && _tags.equals(head._tags) && _category == head._category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_name, _textureValue, _tags, _category);
    }

    @Override
    public IDataHolder serialize(IDataHolder data) {
        data.set("Name", _name);
        data.set("Category", _category, HeadCategory::name);
        data.set("TextureValue", _textureValue);
        data.setStringList("Tags", new ArrayList<>(_tags));
        return data;
    }

    static class Deserializer implements JsonDeserializer<Head> {

        @Override
        public Head deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            JsonObject headObject = jsonElement.getAsJsonObject();
            JsonElement element = headObject.get("tags");
            Head head = gson.fromJson(headObject, Head.class);

            head._tags = new HashSet<>(
                    element.isJsonNull() ?
                            Collections.emptySet() :
                            Arrays.asList(element.getAsString().split(","))
            );

            return head;
        }
    }
}