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

    public static List<Head> findAndSortHeads(Collection<Head> heads, String... keywords) {
        List<Head> list = new ArrayList<>(heads);
        list.sort(getComparator(keywords));
        return list;
    }

    public List<Head> find(Collection<Head> heads, String... keywords) {
        return findAndSortHeads(heads, keywords);
    }

    public List<Head> find(HeadCategory category) {
        return _heads.stream().filter(head -> head.getCategory().equals(category)).collect(Collectors.toList());
    }

    public List<Head> find(String... keywords) {
        return find(_heads, keywords);
    }

    public List<Head> find(HeadCategory category, String... keywords) {
        return findAndSortHeads(find(category), keywords);
    }

    /**
     * Updates the specified category, and returns the amount of newly found heads.
     * @param category The category to update.
     * @return The amount of newly found heads.
     */
    public int update(HeadCategory category) {
        String json = jsonFromURL(category.getURL());
        List<Head> heads = _gson.fromJson(json, new TypeToken<ArrayList<Head>>() {
        }.getType());

        int newCounter = 0;
        for (Head head : heads) {
            head.setCategory(category);
            if (_heads.add(head))
                newCounter++;
        }

        // Cleanup in case of shenanigans.
        _heads.removeIf(h -> h.getCategory() == null); // In case of shenanigans.

        return newCounter;
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

    public static Comparator<Head> getComparator(String... keywords) {
        return (head, other) -> -1 * Float.compare(getSimilarityScore(head, keywords), getSimilarityScore(other, keywords));
    }

    private static final float
            NAME_MATCH_WEIGHT = .6f,
            TAGS_MATCH_WEIGHT = .4f;


    private static float getSimilarityScore(Head head, String... keywords) {
        float score = 0;

        String name = head.getName().toLowerCase();
        String fullQuery = String.join(" ", keywords).toLowerCase();

        List<String> nameParts = Arrays.stream(name.split(" "))
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        // Checking for name match.
        if (!fullQuery.equals(head.getName())) {
            float namePartWorth = NAME_MATCH_WEIGHT / Math.max(keywords.length, nameParts.size());
            for (String namePart : nameParts)
                for (String keyword : keywords)
                    if (keyword.equals(namePart) || (namePart.length() * 1.5f < keyword.length() && namePart.contains(keyword)))
                        score += namePartWorth;
        } else score += NAME_MATCH_WEIGHT;

        // Checking for tag match.
        float tagWorth = TAGS_MATCH_WEIGHT / head.getTags().size();
        for (String tag : head.getTags()) {
            tag = tag.toLowerCase();

            for (String keyword : keywords) {
                if (tag.equals(keyword))
                    score += tagWorth;
            }
        }

        return score;
    }
}