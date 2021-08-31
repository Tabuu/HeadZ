package nl.tabuu.headz.command;

import nl.tabuu.headz.HeadZ;
import nl.tabuu.headz.data.HeadCategory;
import nl.tabuu.headz.ui.CategorySelector;
import nl.tabuu.tabuucore.command.CommandResult;
import nl.tabuu.tabuucore.command.argument.ArgumentType;
import nl.tabuu.tabuucore.command.register.ICommandListener;
import nl.tabuu.tabuucore.command.register.annotation.ChildCommand;
import nl.tabuu.tabuucore.command.register.annotation.CommandExecutor;
import nl.tabuu.tabuucore.command.register.annotation.TabSuggester;
import nl.tabuu.tabuucore.material.XMaterial;
import nl.tabuu.tabuucore.util.Dictionary;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class HeadZCommand implements ICommandListener {

    private final Dictionary _locale;

    public HeadZCommand() {
        _locale = HeadZ.getInstance().getLocale();
    }

    @CommandExecutor(
            value = "headz",
            children = {
                    @ChildCommand(label = "update", method = "headzUpdate")
            }
    )
    private CommandResult headz(Player player, List<?> arguments) {
        if (HeadZ.getInstance().getDatabase().getHeads().isEmpty()) {
            _locale.send(player, "WARNING_EMPTY_DATABASE");
            return CommandResult.SUCCESS;
        }

        new CategorySelector().open(player);

        return CommandResult.SUCCESS;
    }

    @CommandExecutor(
            value = "headz update",
            argumentSequence = ArgumentType.STRING,
            tabSuggestMethod = "headzUpdateSuggest"
    )
    private CommandResult headzUpdate(CommandSender sender, List<Optional<?>> arguments) {
        if(arguments.get(0).isPresent()) {
            String categoryName = (String) arguments.get(0).get();
            HeadCategory category;

            try {
                category = HeadCategory.valueOf(categoryName.toUpperCase());
            } catch (IllegalArgumentException exception) {
                return CommandResult.WRONG_SYNTAX;
            }

            sender.sendMessage(_locale.translate("UPDATE_STARTED"));

            int newCount = HeadZ.getInstance().getDatabase().update(category);
            String message = _locale.translate("UPDATE_COMPLETE_CATEGORY",
                    "{CATEGORY}", category,
                    "{NEW_COUNT}", newCount);

            sender.sendMessage(message);
        } else {
            sender.sendMessage(_locale.translate("UPDATE_STARTED"));
            for (HeadCategory category : HeadCategory.values()) {
                int newCount = HeadZ.getInstance().getDatabase().update(category);
                String message = _locale.translate("UPDATE_COMPLETE_CATEGORY",
                        "{CATEGORY}", category,
                        "{NEW_COUNT}", newCount);

                sender.sendMessage(message);
            }

            sender.sendMessage(_locale.translate("UPDATE_COMPLETE"));
        }

        return CommandResult.SUCCESS;
    }

    @TabSuggester
    private List<String> headzUpdateSuggest(CommandSender sender, List<String> arguments, String partial, List<String> suggestions) {
        if(arguments.size() != 0) return suggestions;
        String upperPartial = partial.toUpperCase();

        return Arrays.stream(HeadCategory.values())
                .map(HeadCategory::name)
                .filter(c -> c.startsWith(upperPartial.toUpperCase()))
                .collect(Collectors.toList());
    }
}
