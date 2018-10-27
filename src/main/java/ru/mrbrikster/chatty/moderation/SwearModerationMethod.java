package ru.mrbrikster.chatty.moderation;

import com.google.common.io.Files;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import ru.mrbrikster.chatty.config.ConfigurationNode;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SwearModerationMethod extends ModerationMethod {

    private final String replacement;
    private final List<String> words;
    @Getter private final boolean useBlock;

    private static Pattern swearsPattern;
    private static List<Pattern> swearsWhitelist = new ArrayList<>();
    private static File swearsDirectory;
    private static File swearsFile;
    private static File whitelistFile;
    private String editedMessage;

    SwearModerationMethod(ConfigurationNode configurationNode, String message) {
        super(message);

        this.replacement = configurationNode.getNode("replacement").getAsString("<swear>");
        this.words = new ArrayList<>();
        this.useBlock = configurationNode.getNode("block").getAsBoolean(true);
    }

    static void init(JavaPlugin javaPlugin) {
        SwearModerationMethod.swearsDirectory = new File(javaPlugin.getDataFolder(), "swears");
        SwearModerationMethod.swearsFile = new File(swearsDirectory, "swears.txt");
        SwearModerationMethod.whitelistFile = new File(swearsDirectory, "whitelist.txt");

        if (!swearsDirectory.exists())
            swearsDirectory.mkdir();

        if (!swearsFile.exists()) {
            try {
                swearsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!whitelistFile.exists()) {
            try {
                whitelistFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            StringBuilder pattern = new StringBuilder();
            for (String swear : Files.readLines(swearsFile, Charset.forName("UTF-8"))) {
                if (swear.isEmpty())
                    continue;

                if (pattern.length() == 0) {
                    pattern.append(swear);
                } else {
                    pattern.append("|").append(swear);
                }
            }

            SwearModerationMethod.swearsPattern = Pattern.compile(pattern.toString(), Pattern.CASE_INSENSITIVE);
            SwearModerationMethod.swearsWhitelist = Files.readLines(whitelistFile, Charset.forName("UTF-8")).stream().map(whitelistPattern
                    -> Pattern.compile(whitelistPattern.toLowerCase(), Pattern.CASE_INSENSITIVE)).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getWhitelistFile() {
        return whitelistFile;
    }

    public static void addWord(Pattern pattern) {
        swearsWhitelist.add(pattern);
    }

    public List<String> getWords() {
        return words;
    }

    @Override
    public String getEditedMessage() {
        if (editedMessage != null)
            return editedMessage;

        this.editedMessage = message;
        Matcher swearPatternMatcher = swearsPattern.matcher(message.toLowerCase());

        while (swearPatternMatcher.find()) {
            if (swearPatternMatcher.group().trim().isEmpty())
                continue;

            String swear = getWord(message, swearPatternMatcher.start(), swearPatternMatcher.end());

            boolean matches = false;
            for (Pattern pattern : swearsWhitelist) {
                if (pattern.matcher(swear).matches())
                    matches = true;
            }

            if (!matches) {
                words.add(swear);
                editedMessage = editedMessage.replaceAll(Pattern.quote(swear), replacement);
            }
        }

        return editedMessage;
    }

    @Override
    public boolean isBlocked() {
        return !getEditedMessage().equals(message);
    }

    private String getWord(String message, int start, int end) {
        int wordStart = 0;
        int wordEnd = message.length();

        char[] chars = message.toCharArray();
        for (int i = start; i >= 0; i--) {
            if (chars[i] == ' ') {
                wordStart = i;
                break;
            }
        }

        for (int i = end; i < message.length(); i++) {
            if (chars[i] == ' ') {
                wordEnd = i;
                break;
            }
        }

        String word = message.substring(wordStart, wordEnd);
        return word.trim();
    }

}
