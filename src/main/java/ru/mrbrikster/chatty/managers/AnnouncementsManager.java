package ru.mrbrikster.chatty.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.mrbrikster.chatty.Config;
import ru.mrbrikster.chatty.Main;
import ru.mrbrikster.chatty.Utils;

import java.util.*;

@SuppressWarnings("all")
public class AnnouncementsManager {

    private static BukkitTask bukkitTask;
    private int currentMessage = -1;

    public AnnouncementsManager(Main main) {
        Config config = main.getConfiguration();

        if (!config.getAdvancementMessages().isEmpty()) {
            if (AnnouncementsManager.bukkitTask != null) {
                AnnouncementsManager.bukkitTask.cancel();
                currentMessage = -1;
            }

            AnnouncementsManager.bukkitTask = new BukkitRunnable() {

                @Override
                public void run() {
                    if (currentMessage == -1 || config.getAdvancementMessages().size() <= ++currentMessage) {
                        currentMessage = 0;
                    }

                    config.getAdvancementMessages().get(currentMessage)
                            .show(Bukkit.getOnlinePlayers());
                }

            }.runTaskTimer(main, config.getAnnouncementsTime() * 20, config.getAnnouncementsTime() * 20);
        }
    }

    public static class AdvancementMessage implements ConfigurationSerializable {

        private NamespacedKey id;
        private String icon;
        private String header, footer;
        private String frame = "goal";
        private boolean announce = false, toast = true;
        private JavaPlugin javaPlugin;

        public AdvancementMessage(Map<?, ?> list, Main main) {
            this((String) list.get("header"),
                    (String) list.get("footer"),
                    (String) list.get("icon"), main);
        }

        private AdvancementMessage(String header, String footer, String icon, JavaPlugin javaPlugin) {
            this.header = header;
            this.footer = footer;
            this.icon = icon;
            this.javaPlugin = javaPlugin;

            this.id = new NamespacedKey(javaPlugin, "chatty" + new Random().nextInt(1000000) + 1);
        }

        void show(Player player)	{
            show(Collections.singletonList(player));
        }

        void show(Collection<? extends Player> players) {
            this.register();

            for (Player player : players)
                this.grant(player);

            new BukkitRunnable() {

                @Override
                public void run() {
                    for (Player player : players)
                        revoke(player);

                    unregister();
                }

            }.runTaskLater(javaPlugin, 20);
        }

        private void register() {
            try {
                Bukkit.getUnsafe().loadAdvancement(id, this.json());
            } catch (IllegalArgumentException ignored){ }
        }

        private void unregister()	{
            Bukkit.getUnsafe().removeAdvancement(id);
        }

        private void grant(Player player) {
            Advancement advancement = Bukkit.getAdvancement(id);
            AdvancementProgress progress = player.getAdvancementProgress(advancement);
            if (!progress.isDone())	{
                for (String criteria : progress.getRemainingCriteria())	{
                    progress.awardCriteria(criteria);
                }
            };

        }

        private void revoke(Player player)	{
            Advancement advancement = Bukkit.getAdvancement(id);
            AdvancementProgress progress = player.getAdvancementProgress(advancement);
            if (progress.isDone())	{
                for (String criteria : progress.getAwardedCriteria())	{
                    progress.revokeCriteria(criteria);
                }
            }
        }

        private String json() {
            JsonObject json = new JsonObject();

            JsonObject display = new JsonObject();

            JsonObject icon = new JsonObject();
            icon.addProperty("item", this.icon);

            display.add("icon", icon);
            display.addProperty("title", Utils.colorize(this.header + "\n" + this.footer));
            display.addProperty("description", "Chatty Announcement");
            display.addProperty("background", "minecraft:textures/blocks/gold_block.png");
            display.addProperty("frame", this.frame);
            display.addProperty("announce_to_chat", announce);
            display.addProperty("show_toast", toast);
            display.addProperty("hidden", true);

            JsonObject trigger = new JsonObject();
            trigger.addProperty("trigger", "minecraft:impossible");

            JsonObject criteria = new JsonObject();
            criteria.add("impossible", trigger);

            json.add("criteria", criteria);
            json.add("display", display);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            return gson.toJson(json);
        }

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> map = new HashMap<>();

            map.put("icon", icon);
            map.put("header", header);
            map.put("footer", footer);

            return map;
        }

    }

}
