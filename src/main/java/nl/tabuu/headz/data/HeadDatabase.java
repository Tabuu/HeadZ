package nl.tabuu.headz.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import nl.tabuu.headz.HeadZ;
import nl.tabuu.tabuucore.configuration.IDataHolder;
import nl.tabuu.tabuucore.serialization.ISerializable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class HeadDatabase implements ISerializable<IDataHolder> {

    private Gson _gson;
    private Set<Head> _heads;

    public HeadDatabase(Collection<Head> heads) {
        _heads = new HashSet<>(heads);
        _gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Head.class, new Head.Deserializer())
                .create();
    }

    public HeadDatabase() {
        this(new HashSet<>());
    }

    public HeadDatabase(IDataHolder data) {
        this(data.getSerializableList("Heads", Head.class));
    }

    public Collection<Head> getHeads() {
        return _heads;
    }

    public List<Head> find(HeadCategory category) {
        return _heads.stream().filter(head -> head.getCategory().equals(category)).collect(Collectors.toList());
    }


    public List<Head> find(String... keywords) {
        Map<Head, Short> scores = new HashMap<>();
        List<String> keys = Arrays.stream(keywords)
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        for (Head head : _heads) {
            short score = 0;
            List<String> name = Arrays.stream(head.getName().split(" "))
                    .map(String::toUpperCase)
                    .collect(Collectors.toList());

            if (String.join(" ", keys).equalsIgnoreCase(String.join(" ", name)))
                score += 50;

            for (String key : keys) {
                for (String namePart : name) {
                    if (namePart.equals(key)) score += 10;
                    else if (namePart.contains(key)) score += 1;
                }

                for (String tag : head.getTags())
                    if (key.equalsIgnoreCase(tag)) score += 5;
            }
            scores.put(head, score);
        }

        List<Map.Entry<Head, Short>> entries = new ArrayList<>(scores.entrySet());
        entries.sort(Map.Entry.comparingByValue());
        Collections.reverse(entries);

        return entries.stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<Head> find(HeadCategory category, String... keywords) {
        return find(keywords).stream().filter(head -> head.getCategory().equals(category)).collect(Collectors.toList());
    }

    public void update(HeadCategory category) {
        _heads.removeIf(h -> h.getCategory() == null);
        _heads.removeIf(h -> h.getCategory().equals(category));

        String json = jsonFromURL(category.getURL());

        Type type = new TypeToken<ArrayList<Head>>() {
        }.getType();
        List<Head> heads = _gson.fromJson(json, type);
        heads.forEach(head -> head.setCategory(category));
        _heads.addAll(heads);
    }

    private String jsonFromURL(String urlString) {
        try (Scanner scanner = new Scanner(new URL(urlString).openStream(), StandardCharsets.UTF_8.toString())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } catch (IOException e) {
            HeadZ.getInstance().getLogger().severe("Could not update database!");
            return "";
        }
    }

    @Override
    public IDataHolder serialize(IDataHolder data) {
        data.setSerializableList("Heads", new ArrayList<>(_heads));
        return data;
    }
}