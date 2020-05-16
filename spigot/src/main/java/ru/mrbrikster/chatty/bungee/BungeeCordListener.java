package ru.mrbrikster.chatty.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import ru.mrbrikster.chatty.chat.Chat;
import ru.mrbrikster.chatty.chat.ChatManager;
import ru.mrbrikster.chatty.json.fanciful.FancyMessage;
import ru.mrbrikster.chatty.reflection.Reflection;

import java.util.Optional;
import java.util.stream.Collectors;

public class BungeeCordListener implements PluginMessageListener {

    private final ChatManager chatManager;

    public BungeeCordListener(ChatManager chatManager) {
        this.chatManager = chatManager;
    }

    @SuppressWarnings("all")
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();

        if (subchannel.equals("chatty")) {
            in.readShort(); // Skip unnecessary data

            String chatName = in.readUTF();
            String text = in.readUTF();
            boolean json = in.readBoolean();

            Optional<Chat> optionalChat = chatManager.getChats().stream().filter(c -> c.getName().equals(chatName)).findAny();

            if (!optionalChat.isPresent()) {
                return;
            }

            Chat chat = optionalChat.get();

            if (chat.getRange() > -3) {
                return;
            }

            if (json) {
                FancyMessage fancyMessage = FancyMessage.deserialize(text);
                fancyMessage.send(Reflection.getOnlinePlayers().stream().filter(recipient -> {
                    return !chat.isPermission()
                            || recipient.hasPermission("chatty.chat." + chat.getName() + ".see")
                            || recipient.hasPermission("chatty.chat." + chat.getName());
                }).collect(Collectors.toList()));

                fancyMessage.send(Bukkit.getConsoleSender());
            } else {
                Reflection.getOnlinePlayers().stream().filter(recipient -> {
                    return !chat.isPermission()
                            || recipient.hasPermission("chatty.chat." + chat.getName() + ".see")
                            || recipient.hasPermission("chatty.chat." + chat.getName());
                }).forEach(onlinePlayer -> {
                    onlinePlayer.sendMessage(text);
                });

                Bukkit.getConsoleSender().sendMessage(text);
            }
        }
    }

}
