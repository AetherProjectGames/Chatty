package ru.mrbrikster.chatty;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static String colorize(String string) {
        return string == null ? null : ChatColor.translateAlternateColorCodes('&', string);
    }

    public static List<Player> getLocalRecipients(Player player, int distance) {
        Location location = player.getLocation();
        List<Player> recipients = new ArrayList<>();

        double squaredDistance = Math.pow(distance, 2);
        for (Player recipient : player.getWorld().getPlayers()) {
            if (location.distanceSquared(recipient.getLocation()) < squaredDistance) {
                recipients.add(recipient);
            }
        }

        return recipients;
    }

}
