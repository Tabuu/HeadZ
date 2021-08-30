package nl.tabuu.headz.commands;

import nl.tabuu.headz.HeadZ;
import nl.tabuu.headz.data.HeadCategory;
import nl.tabuu.headz.ui.CategorySelector;
import nl.tabuu.tabuucore.command.Command;
import nl.tabuu.tabuucore.command.CommandResult;
import nl.tabuu.tabuucore.command.SenderType;
import nl.tabuu.tabuucore.command.argument.ArgumentConverter;
import nl.tabuu.tabuucore.command.argument.ArgumentType;
import nl.tabuu.tabuucore.command.argument.converter.OrderedArgumentConverter;
import nl.tabuu.tabuucore.util.Dictionary;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class HeadZCommand extends Command {

    Dictionary _local;

    public HeadZCommand() {
        super("headz");
        this.addSubCommand("update", new HeadZUpdateCommand(this));
        this.setRequiredSenderType(SenderType.PLAYER);

        _local = HeadZ.getInstance().getLocal();
    }

    @Override
    protected CommandResult onCommand(CommandSender commandSender, List<Optional<?>> list) {
        Player player = (Player) commandSender;

        if (HeadZ.getInstance().getDatabase().getHeads().isEmpty())
            _local.send(player, "WARNING_EMPTY_DATABASE");

        new CategorySelector().open(player);

        return CommandResult.SUCCESS;
    }

    class HeadZUpdateCommand extends Command{

        protected HeadZUpdateCommand(Command parent) {
            super("headz update", parent);

            ArgumentConverter converter = new OrderedArgumentConverter().setSequence(ArgumentType.STRING);
            this.setArgumentConverter(converter);
        }

        @Override
        protected CommandResult onCommand(CommandSender commandSender, List<Optional<?>> list) {
            if(!list.get(0).isPresent()){
                for(HeadCategory category : HeadCategory.values())
                    HeadZ.getInstance().getDatabase().update(category);
            }
            else{
                String string = (String) list.get(0).get();

                HeadCategory category;
                try {
                    category = HeadCategory.valueOf(string.toUpperCase());
                }catch (IllegalArgumentException e){
                    return CommandResult.WRONG_SYNTAX;
                }

                HeadZ.getInstance().getDatabase().update(category);
            }

            commandSender.sendMessage(_local.translate("UPDATE_COMPLETE"));
            return CommandResult.SUCCESS;
        }

        @Override
        protected List<String> onTabSuggest(CommandSender sender, List<String> arguments, String partial, List<String> suggestions) {
            if(arguments.size() > 0) return Collections.emptyList();

            return Arrays.stream(HeadCategory.values())
                    .map(HeadCategory::name)
                    .filter(c -> c.startsWith(partial))
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
        }
    }
}
