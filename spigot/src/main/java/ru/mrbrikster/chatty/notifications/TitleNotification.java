package ru.mrbrikster.chatty.notifications;

import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.reflection.Reflection;
import ru.mrbrikster.chatty.util.TextUtil;
import ru.mrbrikster.chatty.util.textapi.Title;

import java.util.List;
import java.util.stream.Collectors;

public class TitleNotification extends Notification {

    private static final String PERMISSION_NODE = NOTIFICATION_PERMISSION_NODE + "title.%s";
    private final String name;
    private final List<String> messages;

    TitleNotification(String name, int delay, List<String> messages, boolean permission, boolean random) {
        super(delay, permission, messages.size(), random);

        this.name = name;
        this.messages = messages.stream()
                .map(TextUtil::stylish)
                .map(TextUtil::fixMultilineFormatting)
                .collect(Collectors.toList());
    }

    @Override
    public void run() {
        if (messages.isEmpty()) {
            return;
        }

        Chatty.instance().debugger().debug("Run \"%s\" TitleNotification.", name);

        String[] message = messages.get(nextMessage()).split("(\n)|(\\\\n)", 2);

        Title title = new Title(message[0], message.length == 2 ? message[1] : "", 20, 40, 20);
        Reflection.getOnlinePlayers().stream().filter(player -> !isPermission() || player.hasPermission(String.format(PERMISSION_NODE, name))).forEach(title::send);
    }

}
