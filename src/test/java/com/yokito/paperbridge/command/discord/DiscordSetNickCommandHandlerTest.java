package com.yokito.paperbridge.command.discord;

import com.yokito.paperbridge.integration.discordsrv.DiscordGateway;
import com.yokito.paperbridge.service.discord.DiscordText;
import com.yokito.paperbridge.service.nickname.NicknameRepository;
import com.yokito.paperbridge.service.nickname.NicknameService;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionMapping;
import github.scarsz.discordsrv.dependencies.jda.api.requests.restaction.interactions.ReplyAction;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DiscordSetNickCommandHandlerTest {

    private static final String DISCORD_USER_ID = "123456789";

    private NicknameService nicknameService;
    private DiscordGateway discordGateway;
    private DiscordSetNickCommandHandler handler;

    // ── setup ──────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() {
        nicknameService = new NicknameService(
                new NicknameRepository(new YamlConfiguration(), () -> {}));
        discordGateway = mock(DiscordGateway.class);
        handler = new DiscordSetNickCommandHandler(
                new NullLinkedPlayerResolver(), nicknameService, discordGateway);
    }

    // ── handle(): player not linked ─────────────────────────────────────────

    @Test
    void shouldReplyErrorWhenPlayerNotLinked() {
        List<String> replies = new ArrayList<>();
        SlashCommandEvent event = fakeEvent(DISCORD_USER_ID, "Yokito", replies);

        handler.handle(event);

        assertEquals(1, replies.size());
        assertEquals(DiscordText.PLAYER_NOT_JOINED_MESSAGE, replies.getFirst());
        verify(discordGateway, never()).syncMemberNickname(anyString(), anyString());
    }

    // ── handle(): nickname invalid ──────────────────────────────────────────

    @Test
    void shouldReplyErrorWhenNicknameInvalid() {
        UUID playerId = UUID.randomUUID();
        handler = new DiscordSetNickCommandHandler(
                new FixedLinkedPlayerResolver(playerId, "MCUser"), nicknameService, discordGateway);

        List<String> replies = new ArrayList<>();
        SlashCommandEvent event = fakeEvent(DISCORD_USER_ID, "12345678901", replies); // length 11

        handler.handle(event);

        assertEquals(1, replies.size());
        assertEquals(DiscordText.SET_NICK_INVALID_MESSAGE, replies.getFirst());
        verify(discordGateway, never()).syncMemberNickname(anyString(), anyString());
    }

    // ── handle(): success ───────────────────────────────────────────────────

    @Test
    void shouldSaveNicknameAndReplySuccess() {
        UUID playerId = UUID.randomUUID();
        handler = new DiscordSetNickCommandHandler(
                new FixedLinkedPlayerResolver(playerId, "MCUser"), nicknameService, discordGateway);

        List<String> replies = new ArrayList<>();
        SlashCommandEvent event = fakeEvent(DISCORD_USER_ID, "Yokito", replies);

        handler.handle(event);

        assertEquals(1, replies.size());
        assertTrue(replies.getFirst().contains("Yokito"));
        assertEquals("Yokito", nicknameService.getNickname(playerId));
    }

    @Test
    void shouldTriggerDiscordSyncOnSuccess() {
        UUID playerId = UUID.randomUUID();
        handler = new DiscordSetNickCommandHandler(
                new FixedLinkedPlayerResolver(playerId, "MCUser"), nicknameService, discordGateway);

        SlashCommandEvent event = fakeEvent(DISCORD_USER_ID, "Yokito", new ArrayList<>());
        handler.handle(event);

        // displayNickname = "Yokito | MCUser"
        verify(discordGateway).syncMemberNickname(
                eq(DISCORD_USER_ID), eq("Yokito | MCUser"));
    }

    // ── definition ─────────────────────────────────────────────────────────

    @Test
    void shouldHaveCorrectNameAndOneOption() {
        assertEquals("setnick", handler.name());
        assertEquals("setnick", handler.definition().getName());
        assertEquals(1, handler.definition().getOptions().size());
        assertEquals(DiscordSetNickCommandHandler.OPTION_NICKNAME,
                handler.definition().getOptions().getFirst().getName());
    }

    // ── helpers ────────────────────────────────────────────────────────────

    /**
     * Creates a fake SlashCommandEvent via Mockito that records reply messages.
     */
    private SlashCommandEvent fakeEvent(String discordUserId, String nickname, List<String> replies) {
        SlashCommandEvent event = mock(SlashCommandEvent.class);
        User fakeUser = mock(User.class);
        when(fakeUser.getId()).thenReturn(discordUserId);
        when(event.getUser()).thenReturn(fakeUser);

        OptionMapping nicknameOption = mock(OptionMapping.class);
        when(nicknameOption.getAsString()).thenReturn(nickname);
        when(event.getOption(DiscordSetNickCommandHandler.OPTION_NICKNAME)).thenReturn(nicknameOption);

        ReplyAction noOpReplyAction = mock(ReplyAction.class);
        when(noOpReplyAction.setEphemeral(anyBoolean())).thenReturn(noOpReplyAction);
        doNothing().when(noOpReplyAction).queue();

        when(event.reply(anyString())).thenAnswer(invocation -> {
            replies.add(invocation.getArgument(0));
            return noOpReplyAction;
        });

        return event;
    }

    // ── stub resolvers ─────────────────────────────────────────────────────

    /** Always returns null (player not linked). */
    private static final class NullLinkedPlayerResolver
            extends com.yokito.paperbridge.service.discord.DiscordLinkedPlayerResolver {

        NullLinkedPlayerResolver() {
            super(new NoOpGateway());
        }

        @Override
        public OfflinePlayer resolveLinkedPlayer(String discordUserId) {
            return null;
        }
    }

    /** Always returns a fake OfflinePlayer with the given UUID and name. */
    private static final class FixedLinkedPlayerResolver
            extends com.yokito.paperbridge.service.discord.DiscordLinkedPlayerResolver {

        private final UUID playerId;
        private final String playerName;

        FixedLinkedPlayerResolver(UUID playerId, String playerName) {
            super(new NoOpGateway());
            this.playerId = playerId;
            this.playerName = playerName;
        }

        @Override
        public OfflinePlayer resolveLinkedPlayer(String discordUserId) {
            return (OfflinePlayer) Proxy.newProxyInstance(
                    OfflinePlayer.class.getClassLoader(),
                    new Class<?>[]{OfflinePlayer.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "getUniqueId" -> playerId;
                        case "getName" -> playerName;
                        case "hasPlayedBefore" -> true;
                        default -> throw new UnsupportedOperationException(method.getName());
                    });
        }
    }

    private static final class NoOpGateway implements DiscordGateway {

        @Override
        public @Nullable github.scarsz.discordsrv.dependencies.jda.api.JDA getJda() {
            return null;
        }

        @Override
        public void subscribe(Object listener) {}

        @Override
        public void unsubscribe(Object listener) {}

        @Override
        public @Nullable UUID getLinkedPlayerId(String discordUserId) {
            return null;
        }

        @Override
        public Set<UUID> getLinkedPlayerIds() {
            return Set.of();
        }

        @Override
        public void syncMemberNickname(String discordUserId, String displayNickname) {}
    }
}
