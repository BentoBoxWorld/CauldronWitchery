package world.bentobox.cauldronwitchery.utils;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import world.bentobox.bentobox.api.user.User;


class UtilsGiveItemTest
{
    private User user;
    private PlayerInventory inventory;
    private World world;
    private Location location;
    private ItemStack item;


    @BeforeEach
    void setUp()
    {
        this.user = mock(User.class);
        Player player = mock(Player.class);
        this.inventory = mock(PlayerInventory.class);
        this.world = mock(World.class);
        this.location = mock(Location.class);
        this.item = mock(ItemStack.class);

        when(this.user.isPlayer()).thenReturn(true);
        when(this.user.getPlayer()).thenReturn(player);
        when(player.getInventory()).thenReturn(this.inventory);
        when(player.getWorld()).thenReturn(this.world);
        when(player.getLocation()).thenReturn(this.location);
    }


    @Test
    void testItemGoesToInventory()
    {
        when(this.inventory.addItem(this.item)).thenReturn(new HashMap<>());

        Utils.giveItem(this.user, this.item);

        verify(this.inventory).addItem(this.item);
        verify(this.world, never()).dropItemNaturally(any(Location.class), any(ItemStack.class));
    }


    @Test
    void testLeftoverIsDropped()
    {
        ItemStack leftover = mock(ItemStack.class);
        when(this.inventory.addItem(this.item)).thenReturn(new HashMap<>(Map.of(0, leftover)));

        Utils.giveItem(this.user, this.item);

        verify(this.world).dropItemNaturally(this.location, leftover);
    }


    @Test
    void testNoPlayerDoesNothing()
    {
        when(this.user.isPlayer()).thenReturn(false);

        Utils.giveItem(this.user, this.item);

        verify(this.inventory, never()).addItem(any(ItemStack[].class));
    }
}
