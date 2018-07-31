package ru.mrbrikster.chatty.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.mrbrikster.chatty.chat.ChatManager;
import ru.mrbrikster.chatty.config.Configuration;
import ru.mrbrikster.chatty.dependencies.DependencyManager;
import ru.mrbrikster.chatty.moderation.ModerationManager;

public class NORMAL extends ChatListener {

    public NORMAL(Configuration configuration,
                  ChatManager chatManager,
                  DependencyManager dependencyManager,
                  ModerationManager moderationManager) {
        super(configuration,
                chatManager,
                dependencyManager,
                moderationManager);
    }

    @EventHandler(
            priority = EventPriority.NORMAL,
            ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent playerChatEvent) {
        super.onChat(playerChatEvent);
    }

}
