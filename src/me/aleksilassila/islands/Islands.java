package me.aleksilassila.islands;

import me.aleksilassila.islands.generation.IslandGeneration;
import me.aleksilassila.islands.generation.IslandGrid;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public class Islands {
    public Main plugin;
    public World world;
    public World sourceWorld;

    public IslandGeneration islandGeneration;
    public IslandGrid grid;

    public Set<Player> playersWithNoFall = new HashSet<>();


    public enum IslandSize {
        SMALL, // 32*32
        NORMAL, // 64*64
        BIG, // 80*80
        HUGE
    }

    public Islands(World world, World sourceWorld, Main plugin) {
        this.plugin = plugin;

        this.world = world;
        this.sourceWorld = sourceWorld;

        this.islandGeneration = new IslandGeneration(this);
        this.grid = new IslandGrid(this);

        Bukkit.getLogger().info("World source seed: " + sourceWorld.getSeed());
    }

    public static class IslandsException extends java.lang.Exception {
        public IslandsException(String message) {
            super(message);
        }
    }

    public int parseIslandSize(IslandSize size) {
        switch (size) {
            case SMALL:
                return plugin.getConfig().getInt("island.SMALL");
            case BIG:
                return plugin.getConfig().getInt("island.BIG");
            case NORMAL:
            default:
                return plugin.getConfig().getInt("island.NORMAL");

        }
    }

    public String createNewIsland(Biome biome, IslandSize size, UUID uuid) throws IslandsException {
        int islandSize = parseIslandSize(size);

        String islandId = grid.createIsland(uuid, islandSize);

        boolean success = islandGeneration.copyIsland(
                uuid.toString(),
                biome,
                islandSize,
                plugin.getIslandsConfig().getInt("islands."+islandId+".x"),
                plugin.getIslandsConfig().getInt("islands."+islandId+".y"),
                plugin.getIslandsConfig().getInt("islands."+islandId+".z")
        );

        if (!success) {
            throw new IslandsException("Could not copy island");
        }

        return islandId;

    }

    public boolean regenerateIsland(String islandId, Biome biome, IslandSize islandSize, UUID uuid) {
        grid.updateIslandSize(islandId, parseIslandSize(islandSize));

        boolean success = islandGeneration.copyIsland(
                uuid.toString(),
                biome,
                parseIslandSize(islandSize),
                plugin.getIslandsConfig().getInt("islands." + islandId + ".x"),
                plugin.getIslandsConfig().getInt("islands." + islandId + ".y"),
                plugin.getIslandsConfig().getInt("islands." + islandId + ".z")
        );

        return success;
    };
}
