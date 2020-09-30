package me.aleksilassila.islands.commands;

import me.aleksilassila.islands.Main;
import me.aleksilassila.islands.Permissions;
import me.aleksilassila.islands.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class TrustCommands {
    private Main plugin;

    public TrustCommands(Main plugin) {
        this.plugin = plugin;
    }

    public class UntrustCommand implements CommandExecutor {
        public UntrustCommand() {
            plugin.getCommand("untrust").setExecutor(this);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) return false;

            Player player = (Player) sender;

            plugin.islands.confirmations.remove(player.getUniqueId().toString());

            if (!Permissions.checkPermission(player, Permissions.island.untrust)) {
                player.sendMessage(Messages.error.NO_PERMISSION);
                return true;
            }


            if (!player.getWorld().equals(plugin.islandsWorld)) {
                player.sendMessage(Messages.error.WRONG_WORLD);
                return true;
            }


            if (args.length != 1) {
                player.sendMessage(Messages.help.UNTRUST);
                return true;
            }

            String ownerUUID = plugin.islands.grid.getBlockOwnerUUID(player.getLocation().getBlockX(), player.getLocation().getBlockZ());
            String islandId = plugin.islands.grid.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

            if (ownerUUID == null || islandId == null) {
                player.sendMessage(Messages.error.NOT_ON_ISLAND);
                return true;
            }

            if (!ownerUUID.equals(player.getUniqueId().toString()) && !Permissions.checkPermission(player, Permissions.bypass.untrust)) {
                player.sendMessage(Messages.error.NOT_OWNED);
                return true;
            }

            Player targetPlayer = Bukkit.getPlayer(args[0]);

            if (targetPlayer == null) {
                player.sendMessage(Messages.error.PLAYER_NOT_FOUND);
                return true;
            }

            plugin.islands.grid.removeTrusted(islandId, targetPlayer.getUniqueId().toString());

            player.sendMessage(Messages.success.UNTRUSTED);

            return true;
        }
    }

    public class TrustCommand implements CommandExecutor {
        public TrustCommand() {
            plugin.getCommand("trust").setExecutor(this);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) return false;

            Player player = (Player) sender;

            plugin.islands.confirmations.remove(player.getUniqueId().toString());

            if (!Permissions.checkPermission(player, Permissions.island.trust)) {
                player.sendMessage(Messages.error.NO_PERMISSION);
                return true;
            }

            if (args.length != 1) {
                player.sendMessage(Messages.help.TRUST);
                return true;
            }

            String ownerUUID = plugin.islands.grid.getBlockOwnerUUID(player.getLocation().getBlockX(), player.getLocation().getBlockZ());
            String islandId = plugin.islands.grid.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

            if (ownerUUID == null || islandId == null) {
                player.sendMessage(Messages.error.NOT_ON_ISLAND);
                return true;
            }

            if (!ownerUUID.equals(player.getUniqueId().toString()) && !Permissions.checkPermission(player, Permissions.bypass.trust)) {
                player.sendMessage(Messages.error.NOT_OWNED);
                return true;
            }

            Player targetPlayer = Bukkit.getPlayer(args[0]);

            if (targetPlayer == null) {
                player.sendMessage(Messages.error.PLAYER_NOT_FOUND);
                return true;
            }

            plugin.islands.grid.addTrusted(islandId, targetPlayer.getUniqueId().toString());

            player.sendMessage(Messages.success.TRUSTED);

            return true;
        }
    }

    public class ListTrustedCommand implements CommandExecutor {
        public ListTrustedCommand() {
            plugin.getCommand("trusted").setExecutor(this);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) return false;

            Player player = (Player) sender;

            plugin.islands.confirmations.remove(player.getUniqueId().toString());

            if (!Permissions.checkPermission(player, Permissions.island.listTrusted)) {
                player.sendMessage(Messages.error.NO_PERMISSION);
                return true;
            }

            String ownerUUID = plugin.islands.grid.getBlockOwnerUUID(player.getLocation().getBlockX(), player.getLocation().getBlockZ());
            String islandId = plugin.islands.grid.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

            if (ownerUUID == null || islandId == null) {
                player.sendMessage(Messages.error.NOT_ON_ISLAND);
                return true;
            }

            if (!ownerUUID.equals(player.getUniqueId().toString()) && !Permissions.checkPermission(player, Permissions.bypass.listTrusted)) {
                player.sendMessage(Messages.error.NOT_OWNED);
                return true;
            }

            List<String> trustedList = plugin.islands.grid.getTrusted(islandId);

            player.sendMessage(Messages.info.TRUSTED_INFO(trustedList.size()));
            for (String uuid : trustedList) {
                Player trustedPlayer = Bukkit.getPlayer(UUID.fromString(uuid));

                if (trustedPlayer != null) player.sendMessage(Messages.info.TRUSTED_PLAYER(trustedPlayer.getDisplayName()));
            }

            return true;
        }
    }
}