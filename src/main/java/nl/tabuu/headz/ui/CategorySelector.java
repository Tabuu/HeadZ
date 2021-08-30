package nl.tabuu.headz.ui;

import nl.tabuu.headz.HeadZ;
import nl.tabuu.headz.data.Head;
import nl.tabuu.headz.data.HeadCategory;
import nl.tabuu.headz.data.HeadDatabase;
import nl.tabuu.tabuucore.configuration.IConfiguration;
import nl.tabuu.tabuucore.inventory.InventorySize;
import nl.tabuu.tabuucore.inventory.ui.InventoryFormUI;
import nl.tabuu.tabuucore.inventory.ui.element.Button;
import nl.tabuu.tabuucore.inventory.ui.element.TextInput;
import nl.tabuu.tabuucore.inventory.ui.element.style.Style;
import nl.tabuu.tabuucore.inventory.ui.element.style.TextInputStyle;
import nl.tabuu.tabuucore.inventory.ui.graphics.brush.CheckerBrush;
import nl.tabuu.tabuucore.inventory.ui.graphics.brush.IBrush;
import nl.tabuu.tabuucore.item.ItemBuilder;
import nl.tabuu.tabuucore.material.XMaterial;
import nl.tabuu.tabuucore.util.Dictionary;
import nl.tabuu.tabuucore.util.vector.Vector2f;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class CategorySelector extends InventoryFormUI {

    private final Dictionary _local;
    private final IConfiguration _config;

    public CategorySelector() {
        super("", InventorySize.FIVE_ROWS);

        _local = HeadZ.getInstance().getLocal();
        _config = HeadZ.getInstance().getConfiguration();

        setTitle(_local.translate("CATEGORY_SELECTOR_UI_TITLE"));
        reload();
    }

    @Override
    protected void onDraw() {
        //region pallet
        ItemBuilder
                black = new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE)
                .setDisplayName(" "),

                gray = new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE)
                        .setDisplayName(" "),

                compass = new ItemBuilder(XMaterial.COMPASS)
                        .setDisplayName(_local.translate("CATEGORY_SELECTOR_UI_SEARCH"))
                        .setLore(_local.translate("CATEGORY_SELECTOR_UI_SEARCH_LORE"));
        //endregion

        IBrush brush = new CheckerBrush(black.build(), gray.build());
        setBrush(brush);
        drawFilledRectangle(new Vector2f(0, 0), new Vector2f(8, 4));

        TextInputStyle searchInputStyle = new TextInputStyle(compass.build(),
                compass.build(),
                XMaterial.NAME_TAG.parseItem(),
                _local.translate("CATEGORY_SELECTOR_UI_SEARCH_PLACEHOLDER"));

        TextInput searchInput = new TextInput(searchInputStyle, null, this::searchHead);
        setElement(new Vector2f(4, 3), searchInput);

        int[] slots = new int[]{10, 11, 12, 13, 14, 15, 16, 21, 22, 23};
        for (HeadCategory category : HeadCategory.values()) {
            XMaterial material = _config.get("Category." + category.name() + ".DisplayItem", XMaterial::valueOf, XMaterial.BARRIER);
            ItemBuilder displayItem = new ItemBuilder(material)
                    .setDisplayName(_local.translate("CATEGORY_SELECTOR_UI_CATEGORY_" + category.name()));

            Style style = new Style(displayItem.build(), XMaterial.AIR.parseItem());
            Button button = new Button(style, p -> selectCategory(p, category));

            int slot = slots[category.ordinal()];
            Vector2f position = getSize().slotToVector(slot);

            setElement(position, button);
        }
        super.onDraw();
    }

    private void searchHead(Player player, String string) {
        HeadDatabase database = HeadZ.getInstance().getDatabase();
        String[] keywords = string.split(" ");
        List<Head> heads = database.find(keywords);

        Bukkit.getScheduler().runTask(HeadZ.getInstance(), () ->
                new HeadSelector(heads).open(player));
    }

    private void selectCategory(Player player, HeadCategory category) {
        new HeadSelector(category).open(player);
    }
}
