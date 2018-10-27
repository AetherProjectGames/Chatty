package ru.mrbrikster.chatty.commands;

import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.chat.PermanentStorage;
import ru.mrbrikster.chatty.config.Configuration;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PrefixCommand extends AbstractCommand {

    private final Configuration configuration;
    private final PermanentStorage permanentStorage;


    PrefixCommand(Configuration configuration,
                  PermanentStorage permanentStorage) {
        super("prefix", "setprefix");

        this.configuration = configuration;
        this.permanentStorage = permanentStorage;
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (args.length >= 2) {
            if (!sender.hasPermission("chatty.command.prefix")) {
                sender.sendMessage(Configuration.getMessages().get("no-permission"));
                return;
            }

            Player player = Bukkit.getPlayer(args[0]);

            if (player == null) {
                sender.sendMessage(Configuration.getMessages().get("prefix-command.player-not-found"));
                return;
            }

            if (!player.equals(sender) && !sender.hasPermission("chatty.command.prefix.others")) {
                sender.sendMessage(Configuration.getMessages().get("prefix-command.no-permission-others"));
                return;
            }

            if (args[1].equalsIgnoreCase("clear")) {
                permanentStorage.setProperty(player, "prefix", null);

                sender.sendMessage(Configuration.getMessages().get("prefix-command.prefix-clear")
                        .replace("{player}", player.getName()));
            } else {
                String prefix = Arrays.stream(Arrays.copyOfRange(args, 1, args.length)).collect(Collectors.joining(" "));

                permanentStorage.setProperty(player, "prefix", new JsonPrimitive(prefix
                        + configuration.getNode("general.prefix-command.after-prefix").getAsString("")));

                sender.sendMessage(Configuration.getMessages().get("prefix-command.prefix-set")
                        .replace("{player}", player.getName())
                        .replace("{prefix}", ChatColor.translateAlternateColorCodes('&', prefix)));
            }
        } else {
            sender.sendMessage(Configuration.getMessages().get("prefix-command.usage")
                    .replace("{label}", label));
        }
    }

}
