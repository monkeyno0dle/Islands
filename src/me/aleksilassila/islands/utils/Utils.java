package me.aleksilassila.islands.utils;

import com.sun.istack.internal.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Biome;

public class Utils {
    public static int getHighestYAt(World world, int x, int z) {
        for (int y = 256; y > 0; y--) {
            if (!world.getBlockAt(x, y, z).isEmpty()) return y;
        }

        return -1;
    }

    @Nullable
    static public OfflinePlayer getOfflinePlayer(String name) {
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if (name.equalsIgnoreCase(player.getName())) return player;
        }

        return null;
    }

    @Nullable
    public static Biome getTargetBiome(String biome) {
         Biome targetBiome = null;

         for (Biome b : Biome.values()) {
             if (b.name().equalsIgnoreCase(biome)) {
                 targetBiome = b;
             }
         }

         return targetBiome;
    }
}
