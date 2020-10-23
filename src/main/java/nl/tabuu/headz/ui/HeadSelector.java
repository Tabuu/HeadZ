package nl.tabuu.headz.ui;

import nl.tabuu.headz.HeadZ;
import nl.tabuu.headz.data.Head;
import nl.tabuu.headz.data.HeadCategory;
import nl.tabuu.headz.data.HeadDatabase;
import nl.tabuu.tabuucore.inventory.InventorySize;
import nl.tabuu.tabuucore.inventory.ui.InventoryFormUI;
import nl.tabuu.tabuucore.inventory.ui.element.Button;
import nl.tabuu.tabuucore.inventory.ui.element.Element;
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

public class HeadSelector extends InventoryFormUI {

    private List<Head> _heads;
    private Dictionary _local;
    private int _page;
    private final int _content;
    private final int _pageMax;
    private HeadCategory _category;

    public HeadSelector(List<Head> heads) {
        super("Heads (" + heads.size() + "/" + HeadZ.getInstance().getDatabase().getHeads().size() + ")", InventorySize.SIX_ROWS);

        _local = HeadZ.getInstance().getLocal();
        _heads = heads;
        _category = null;

        _content = (getSize().getHeight() - 2) * (getSize().getWidth() - 2);
        _pageMax = _heads.size() / _content;

        setTitle(_local.translate("HEAD_SELECTOR_UI_TITLE", getReplacements()));
        reload();
    }

    public HeadSelector(HeadCategory category) {
        this(HeadZ.getInstance().getDatabase().find(category));
        _category = category;
    }

    @Override
    protected void onDraw() {
        //region pallet
        ItemBuilder
                black = new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE)
                        .setDisplayName(" "),

                gray = new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE)
                        .setDisplayName(" "),

                lightBlueDye = new ItemBuilder(XMaterial.LIGHT_BLUE_DYE)
                        .setDisplayName(_local.translate("HEAD_SELECTOR_UI_NEXT_BUTTON"))
                        .setLore(_local.translate("HEAD_SELECTOR_UI_NEXT_BUTTON_LORE", getReplacements())),

                lightBlueDye2 = new ItemBuilder(XMaterial.LIGHT_BLUE_DYE)
                        .setDisplayName(_local.translate("HEAD_SELECTOR_UI_PREVIOUS_BUTTON"))
                        .setLore(_local.translate("HEAD_SELECTOR_UI_PREVIOUS_BUTTON_LORE", getReplacements())),

                barrier = new ItemBuilder(XMaterial.BARRIER)
                        .setDisplayName(_local.translate("HEAD_SELECTOR_UI_EXIT_BUTTON"))
                        .setLore(_local.translate("HEAD_SELECTOR_UI_EXIT_BUTTON_LORE")),

                compass = new ItemBuilder(XMaterial.COMPASS)
                        .setDisplayName(_local.translate("HEAD_SELECTOR_UI_SEARCH"))
                        .setLore(_local.translate("HEAD_SELECTOR_UI_SEARCH_LORE")),

                air = new ItemBuilder(XMaterial.AIR);
        //endregion

        IBrush border = new CheckerBrush(black.build(), gray.build());

        setBrush(border);
        drawRectangle(new Vector2f(0, 0), new Vector2f(8, 5));

        Style previousButtonStyle = new Style(lightBlueDye2.build(), air.build()),
                nextButtonStyle = new Style(lightBlueDye.build(), air.build()),
                exitButtonStyle = new Style(barrier.build(), air.build());

        TextInputStyle headSearchInputStyle = new TextInputStyle(compass.build(),
                air.build(),
                XMaterial.NAME_TAG.parseItem(),
                _local.translate("HEAD_SELECTOR_UI_SEARCH_PLACEHOLDER"));

        Element previousButton = new Button(previousButtonStyle, this::onPreviousButtonClick),
                nextButton = new Button(nextButtonStyle, this::onNextButtonClick),
                exitButton = new Button(exitButtonStyle, this::onExitButtonClick),
                searchButton = new TextInput(headSearchInputStyle, this, this::searchHead);

        setElement(new Vector2f(3, 5), previousButton);
        setElement(new Vector2f(4, 5), exitButton);
        setElement(new Vector2f(5, 5), nextButton);
        setElement(new Vector2f(4, 0), searchButton);

        for (int i = 0; i < _content; i++) {

            Vector2f position = new Vector2f(i % 7, i / 7);
            position.add(new Vector2f(1, 1));

            int index = i + (getPage() * _content);

            if (index >= _heads.size()) {
                setElement(position, new Button(new Style(air.build(), air.build())));
                continue;
            }

            Head head = _heads.get(index);

            String displayName = _local.translate("HEAD_SELECTOR_UI_ITEM",
                    "{HEAD_NAME}", head.getName());

            String lore = _local.translate("HEAD_SELECTOR_UI_ITEM_LORE",
                    "{HEAD_TAGS}", String.join(", ", head.getTags()));

            ItemBuilder headItem = new ItemBuilder(head.getItemStack());
            headItem.setDisplayName(displayName);
            headItem.addLore(lore);

            Style itemButtonStyle = new Style(headItem.build(), air.build());
            Button itemButton = new Button(itemButtonStyle, p -> this.giveHead(p, head));
            setElement(position, itemButton);
        }

        super.onDraw();
    }

    public int getPage() {
        return _page;
    }

    private int getPageCount() {
        return _pageMax;
    }

    private void nextPage() {
        if(_page >= _pageMax) return;

        _page++;
        setTitle(_local.translate("HEAD_SELECTOR_UI_TITLE", getReplacements()));
        reload();
        onDraw();
    }

    private void previousPage() {
        if(_page <= 0) return;

        _page--;
        setTitle(_local.translate("HEAD_SELECTOR_UI_TITLE", getReplacements()));
        reload();
        onDraw();
    }

    private void searchHead(Player player, String string) {
        HeadDatabase database = HeadZ.getInstance().getDatabase();
        String[] keywords = string.split(" ");
        List<Head> heads = _category == null ? database.find(keywords) : database.find(_category, keywords);

        HeadSelector selector = new HeadSelector(heads);
        selector._category = _category;

        Bukkit.getScheduler().runTask(HeadZ.getInstance(), () -> selector.open(player));
    }

    public Object[] getReplacements() {
        return new Object[] {
                "{PAGE}", getPage() + 1,
                "{PAGE_COUNT}", getPageCount() + 1
        };
    }

    private void onNextButtonClick(Player player) {
        nextPage();
    }

    private void onPreviousButtonClick(Player player) {
        previousPage();
    }

    private void onExitButtonClick(Player player) {
        new CategorySelector().open(player);
    }

    private void giveHead(Player player, Head head) {
        player.getInventory().addItem(head.getItemStack());
    }
}
