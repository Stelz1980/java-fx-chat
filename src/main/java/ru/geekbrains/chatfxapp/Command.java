package ru.geekbrains.chatfxapp;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Command {
    AUTH("/auth") {
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMITER);
            return new String[]{split[1], split[2]};
        }
    },
    AUTHOK("/authok") {
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMITER);
            return new String[]{split[1]};
        }
    },
    END("/end") {
        @Override
        public String[] parse(String commandText) {
            return new String[0];
        }
    },
    PRIVATE_MESSAGE("/w") {
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMITER, 3);
            return new String[]{split[1], split[2]};
        }
    },
    CLIENTS("/clients") {
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMITER);
            String[] nicks = new String[split.length - 1];
            for (int i = 0; i < nicks.length; i++) {
                nicks[i] = split[i + 1];
            }
            return nicks;
        }
    },
    ERROR("/error") {
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMITER, 2);
            return new String[]{split[1]};
        }
    },
    MESSAGE("/message") {
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMITER, 2);
            return new String[]{split[1]};
        }
    },
    NEW_NICK("/nick") {
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMITER, 2);
            return new String[]{split[1]};
        }
    },
    SAVE_HISTORY("/save_history") {
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMITER, 2);
            return new String[]{split[1]};
        }
    },
    RESTORE_HISTORY("/restore_history") {
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMITER, 2);
            return new String[]{split[1]};
        }
    };


    private final String command;
    private static final String TOKEN_DELIMITER = "\\p{Blank}+";
    static final Map<String, Command> commandMap = Arrays.stream(values()).collect(Collectors.toMap(Command::getCommand, Function.identity()));

    Command(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public static boolean isCommand(String message) {
        return message.startsWith("/");
    }

    public static Command getCommand(String message) {
        if (!isCommand(message)) {
            throw new RuntimeException("'" + message + "' is not command");
        }
        final String cmd = message.split(TOKEN_DELIMITER, 2)[0];
        final Command command = commandMap.get(cmd);
        if (command == null) {
            throw new RuntimeException("'" + message + "' is unknown command");
        }
        return command;
    }

    public abstract String[] parse(String commandText);

    public String collectMessage(String... params) {
        return this.command + " " + String.join(" ", params);
    }
}
