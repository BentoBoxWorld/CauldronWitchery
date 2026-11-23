package world.bentobox.cauldronwitchery.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockbukkit.mockbukkit.MockBukkit;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.Settings;
import world.bentobox.bentobox.api.prompts.ChatInputListener;
import world.bentobox.bentobox.api.user.Notifier;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.managers.LocalesManager;
import world.bentobox.bentobox.managers.PlaceholdersManager;

/**
 * Tests that {@link ConversationUtils} hands its questions to BentoBox's prompts and passes the answers back
 * the way its callers expect: the value on success, and null (or false for a confirmation) when the player
 * backs out.
 * <p>
 * Dialogs cannot be built without a server, so they are switched off here and every prompt is answered in
 * chat. Translations come back as their locale key, so the cancel, done and confirm words are the keys.
 */
class ConversationUtilsTest
{
    @Mock
    private BentoBox plugin;

    @Mock
    private Player mockPlayer;

    @Mock
    private BukkitScheduler sch;

    @Mock
    private LocalesManager lm;

    @Mock
    private PlaceholdersManager phm;

    @Mock
    private Notifier notifier;

    private final UUID uuid = UUID.randomUUID();

    private AutoCloseable closeable;

    private MockedStatic<Bukkit> mockedBukkit;

    private static final String CANCEL = "prompts.chat.cancel-word";

    private User user;

    private final ChatInputListener listener = new ChatInputListener();

    private final List<Object> answers = new ArrayList<>();


    @BeforeEach
    public void setUp() throws Exception
    {
        this.closeable = MockitoAnnotations.openMocks(this);
        MockBukkit.mock();
        Field instance = BentoBox.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, this.plugin);

        this.mockedBukkit = Mockito.mockStatic(Bukkit.class, Mockito.RETURNS_DEEP_STUBS);
        this.mockedBukkit.when(Bukkit::getScheduler).thenReturn(this.sch);

        // Translations come back as their locale key
        when(this.lm.get(any(), any())).thenAnswer(invocation -> invocation.getArgument(1, String.class));
        when(this.phm.replacePlaceholders(any(), any())).
            thenAnswer(invocation -> invocation.getArgument(1, String.class));
        when(this.plugin.getLocalesManager()).thenReturn(this.lm);
        when(this.plugin.getPlaceholdersManager()).thenReturn(this.phm);
        when(this.plugin.getNotifier()).thenReturn(this.notifier);
        // Dialogs cannot be built without a server
        Settings settings = new Settings();
        settings.setDialogPrompts(false);
        when(this.plugin.getSettings()).thenReturn(settings);

        when(this.mockPlayer.getUniqueId()).thenReturn(this.uuid);
        when(this.mockPlayer.getName()).thenReturn("tastybento");
        User.setPlugin(this.plugin);
        User.clearUsers();
        this.user = User.getInstance(this.mockPlayer);
    }


    @AfterEach
    public void tearDown() throws Exception
    {
        PlayerQuitEvent quit = mock(PlayerQuitEvent.class);
        when(quit.getPlayer()).thenReturn(this.mockPlayer);
        this.listener.onQuit(quit);
        this.mockedBukkit.closeOnDemand();
        this.closeable.close();
        MockBukkit.unmock();
        User.clearUsers();
        Mockito.framework().clearInlineMocks();
    }


    /**
     * Sends a chat message as the player and runs the task that carries it to the main thread.
     */
    private void say(String text)
    {
        AsyncChatEvent chat = mock(AsyncChatEvent.class);
        when(chat.getPlayer()).thenReturn(this.mockPlayer);
        when(chat.message()).thenReturn(Component.text(text));
        this.listener.onChat(chat);
        ArgumentCaptor<Runnable> task = ArgumentCaptor.forClass(Runnable.class);
        verify(this.sch, atLeastOnce()).runTask(eq(this.plugin), task.capture());
        task.getValue().run();
    }


    @Test
    void testStringInput()
    {
        ConversationUtils.createStringInput(this.answers::add, this.user, "Search for?", "Updated");
        verify(this.mockPlayer).closeInventory();
        assertTrue(ChatInputListener.isWaitingFor(this.uuid));

        this.say("diamond");

        assertEquals(List.of("diamond"), this.answers);
        assertFalse(ChatInputListener.isWaitingFor(this.uuid));
    }


    @Test
    void testStringInputCancelGivesNull()
    {
        ConversationUtils.createStringInput(this.answers::add, this.user, "Search for?", null);

        this.say(CANCEL);

        assertEquals(1, this.answers.size());
        assertNull(this.answers.getFirst());
        assertFalse(ChatInputListener.isWaitingFor(this.uuid));
    }


    @Test
    void testNumericInput()
    {
        ConversationUtils.createNumericInput(this.answers::add, this.user, "How many?", 0, 10);

        this.say("lots");
        this.say("11");
        assertTrue(this.answers.isEmpty());
        assertTrue(ChatInputListener.isWaitingFor(this.uuid));

        this.say("7");

        assertEquals(1, this.answers.size());
        assertEquals(7, ((Number) this.answers.getFirst()).intValue());
    }


    @Test
    void testNumericInputCancelGivesNull()
    {
        ConversationUtils.createNumericInput(this.answers::add, this.user, "How many?", 0, 10);

        this.say(CANCEL);

        assertEquals(1, this.answers.size());
        assertNull(this.answers.getFirst());
    }


    @Test
    void testStringListInput()
    {
        ConversationUtils.createStringListInput(this.answers::add, this.user, "Describe it", "Saved");

        this.say("&aFirst");
        this.say("Second");
        assertTrue(this.answers.isEmpty());
        this.say("prompts.chat.done-word");

        assertEquals(List.of(List.of("§aFirst", "Second")), this.answers);
    }


    @Test
    void testConfirmation()
    {
        ConversationUtils.createConfirmation(this.answers::add, this.user, "Wipe everything?", "Wiped");

        this.say("prompts.chat.confirm-word");

        assertEquals(List.of(true), this.answers);
    }


    @Test
    void testConfirmationDeclined()
    {
        ConversationUtils.createConfirmation(this.answers::add, this.user, "Wipe everything?", "Wiped");

        this.say("no");

        assertEquals(List.of(false), this.answers);
    }


    @Test
    void testIdStringInputAsksAgainUntilValid()
    {
        ConversationUtils.createIDStringInput(this.answers::add,
            text -> !text.equalsIgnoreCase("taken"),
            this.user,
            "Name it",
            "Created",
            "cauldronwitchery.conversations.object-already-exists");

        this.say("Taken");
        assertTrue(this.answers.isEmpty());
        assertTrue(ChatInputListener.isWaitingFor(this.uuid));

        this.say("My Stick");

        // The consumer gets the text as typed; callers sanitize it themselves
        assertEquals(List.of("My Stick"), this.answers);
        assertFalse(ChatInputListener.isWaitingFor(this.uuid));
    }


    @Test
    void testIdStringInputCancelGivesNull()
    {
        ConversationUtils.createIDStringInput(this.answers::add, text -> true, this.user, "Name it", null, "fail");

        this.say(CANCEL);

        assertEquals(1, this.answers.size());
        assertNull(this.answers.getFirst());
    }
}
