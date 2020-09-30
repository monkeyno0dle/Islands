package me.aleksilassila.islands.commands;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.Main;
import me.aleksilassila.islands.Permissions;
import me.aleksilassila.islands.generation.IslandGrid;
import me.aleksilassila.islands.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.List;

public class IslandCommands {
    private Main plugin;
    private Islands islands;
    private IslandGrid grid;

    public IslandCommands(Main plugin) {
        this.plugin = plugin;
        this.islands = plugin.islands;
        this.grid = plugin.islands.grid;
    }

    public class VisitCommand implements CommandExecutor {

        public VisitCommand() {
            plugin.getCommand("visit").setExecutor(this);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This is for players only.");
                return false;
            }

            Player player = (Player) sender;

            plugin.islands.confirmations.remove(player.getUniqueId().toString());

            if (!Permissions.checkPermission(player, Permissions.island.visit)) {
                player.sendMessage(Messages.error.NO_PERMISSION);
                return true;
            }

            if (args.length != 1) {
                player.sendMessage(Messages.help.VISIT);
                return true;
            }

            if (!canTeleport(player)) {
                player.sendMessage(Messages.error.COOLDOWN(teleportCooldown(player)));
                return true;
            }

            String islandId = plugin.islands.grid.getPublicIsland(args[0]);

            if (islandId != null) {
                player.teleport(plugin.islands.grid.getIslandSpawn(islandId));
            } else {
                player.sendMessage(Messages.error.HOME_NOT_FOUND);
            }

            return true;
        }
    }

    public class HomeCommand implements CommandExecutor {
        public HomeCommand() {
            plugin.getCommand("home").setExecutor(this);
            plugin.getCommand("homes").setExecutor(this);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This is for players only.");
                return false;
            }

            Player player = (Player) sender;

            plugin.islands.confirmations.remove(player.getUniqueId().toString());

            if (args.length == 1 && args[0].equalsIgnoreCase("list") || label.equalsIgnoreCase("homes")) {
                if (!Permissions.checkPermission(player, Permissions.island.listHomes)) {
                    player.sendMessage(Messages.error.NO_PERMISSION);
                    return true;
                }

                List<String> ids = plugin.islands.grid.getAllIslandIds(player.getUniqueId());

                player.sendMessage(Messages.success.HOMES_FOUND(ids.size()));
                for (String islandId : ids) {
                    String name = plugin.getIslandsConfig().getString("islands." + islandId + ".name");
                    String homeNumber = plugin.getIslandsConfig().getString("islands." + islandId + ".home");
                    player.sendMessage(ChatColor.AQUA + " - " + name + " (" + homeNumber + ")");
                }

                return true;
            } else {
                if (!Permissions.checkPermission(player, Permissions.island.home)) {
                    player.sendMessage(Messages.error.NO_PERMISSION);
                    return true;
                }

                if (!canTeleport(player)) {
                    player.sendMessage(Messages.error.COOLDOWN(teleportCooldown(player)));
                    return true;
                }

                try {
                    if (args.length != 0) {
                        Integer.parseInt(args[0]);
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(Messages.help.HOME);
                    return true;
                }
            }

            if (player.getWorld().getName().equals("world_nether") && !Permissions.checkPermission(player, Permissions.bypass.home)) {
                player.sendMessage(Messages.info.IN_OVERWORLD);
                return true;
            }

            if (player.getWorld().getName().equals("world") && !Permissions.checkPermission(player, Permissions.bypass.home)) {
                // Check if is on surface
                Location playerLocation = player.getLocation();

                for (int y = playerLocation.getBlockY(); y < player.getWorld().getHighestBlockYAt(playerLocation); y++) {
                    playerLocation.setY(y);
                    if (player.getWorld().getBlockAt(playerLocation).getBlockData().getMaterial().equals(Material.STONE)) {
                        player.sendMessage(Messages.info.ON_SURFACE);
                        return true;
                    }
                }
            }

            String homeId = args.length == 0 ? "1" : args[0];

            Location location = grid.getIslandSpawn(grid.getHomeIsland(player.getUniqueId(), homeId));

            if (location != null) {
                player.teleport(location);
            } else {
                player.sendMessage(Messages.error.ISLAND_NOT_FOUND);
            }

            return true;
        }
    }

    private boolean canTeleport(Player player) {
        if (plugin.islands.teleportCooldowns.containsKey(player.getUniqueId().toString())) {
            long cooldownTime  = plugin.getConfig().getInt("tpCooldownTime") * 1000;
            long timePassed = new Date().getTime() - plugin.islands.teleportCooldowns.get(player.getUniqueId().toString());

            return timePassed >= cooldownTime;
        }

        return true;
    }

    private int teleportCooldown(Player player) {
        if (plugin.islands.teleportCooldowns.containsKey(player.getUniqueId().toString())) {
            long cooldownTime  = plugin.getConfig().getInt("tpCooldownTime") * 1000;
            long timePassed = new Date().getTime() - plugin.islands.teleportCooldowns.get(player.getUniqueId().toString());

            long remaining = cooldownTime - timePassed;

            return remaining < 0 ? 0 : (int)(remaining / 1000);
        }

        return 0;
    }

    static class Messages extends ChatUtils {
        static class error {
            public static final String ISLAND_NOT_FOUND = error("404 - Home not found.");
            public static final String NO_PERMISSION = error("You don't have permission to use this command.");
            public static final String HOME_NOT_FOUND = error("404 - Home not found :(");

            public static String COOLDOWN(int remainingTime) {
                return error("You took damage recently. You have to wait for " + remainingTime + "s before teleporting.");
            }
        }

        static class success {


            public static String HOMES_FOUND(int amount) {
                return success("Found " + amount + " home(s).");
            }
        }

        static class info {
            public static final String ON_SURFACE = info("You can only use this command on surface.");
            public static final String IN_OVERWORLD = info("You can only use this command in overworld.");
        }

        static class help {
            public static final String VISIT = info("Usage: /visit name");
            public static final String HOME = error("Usage: /home <id>");
        }
    }
}