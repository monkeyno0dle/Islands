package me.aleksilassila.islands.generation;

import com.sun.istack.internal.Nullable;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.biomes.Biomes;
import me.aleksilassila.islands.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IslandGeneration {

    private final Islands islands;
    public Biomes biomes;
    public List<CopyTask> queue = new ArrayList<>();

    public IslandGeneration(Islands islands) {
        this.islands = islands;
        this.biomes = new Biomes(islands.sourceWorld, islands.plugin);
    }

    public void addToQueue(CopyTask task) {
        popFromQueue(task.player.getUniqueId().toString());
        queue.add(task);

        task.player.sendMessage(Messages.Info.QUEUE_STATUS(queue.size()));
    }

    @Nullable
    public CopyTask popFromQueue(String UUID) {
        int index = 0;
        for (CopyTask task : queue) {
            if (index != 0 && task.player.getUniqueId().toString().equals(UUID)) {
                queue.remove(task);

                return task;
            }
            index++;
        }

        return null;
    }

    class CopyTask extends BukkitRunnable {
        private final Player player;
        private final int startX;
        private final int startY;
        private final int startZ;
        private final int targetX;
        private final int targetY;
        private final int targetZ;
        private final int islandSize;
        private int index;

        public CopyTask(Player player, int startX, int startY, int startZ, int targetX, int targetY, int targetZ, int islandSize) {
            this.startX = startX;
            this.startY = startY;
            this.startZ = startZ;
            this.targetX = targetX;
            this.targetY = targetY;
            this.targetZ = targetZ;
            this.islandSize = islandSize;
            this.player = player;

            this.index = 0;
        }

        @Override
        public void run() {
            for (int y = startY; y < startY + islandSize; y++) {
                int relativeX = index / islandSize;
                int relativeZ = index - relativeX * islandSize;

                Block sourceBlock = islands.sourceWorld.getBlockAt(startX + relativeX, y, startZ + relativeZ);

                //WIP
    //            if (Math.random() < - ((8 * (y - sourceY)) / (double) islandSize) + 2) {
    //                continue;
    //            }

                Block target = islands.world.getBlockAt(targetX + relativeX, targetY + (y - startY), targetZ + relativeZ);
                if (isBlockInIslandShape(relativeX, y - startY, relativeZ, islandSize)) {
                    target.setBlockData(sourceBlock.getBlockData());
                } else {
                    target.setType(Material.AIR);
                }

                target.setBiome(sourceBlock.getBiome());
            }

            if (index >= islandSize * islandSize) {
                // Update lighting
                islands.world.getChunkAt(targetX + islandSize / 2, targetZ + islandSize / 2);

                player.sendMessage(Messages.Success.GENERATION_DONE);
                queue.remove(this);

                if (queue.size() > 0) {
                    CopyTask nextTask = queue.get(0);
                    nextTask.runTaskTimer(islands.plugin, 0,1);
                    nextTask.player.sendMessage(Messages.Info.GENERATION_STARTED(nextTask.islandSize * nextTask.islandSize / 20.0));
                }

                this.cancel();
            } else if (index == islandSize * islandSize / 4) {
                player.sendMessage(Messages.Info.GENERATION_STATUS(25));
            } else if (index == islandSize * islandSize / 2) {
                player.sendMessage(Messages.Info.GENERATION_STATUS(50));
            } else if (index == islandSize * islandSize / 4 * 3) {
                player.sendMessage(Messages.Info.GENERATION_STATUS(75));
            }

            index++;
        }
    }

    public boolean copyIsland(Player player, Biome biome, int islandSize, int targetX, int targetY, int targetZ) throws IllegalArgumentException {
        if (queue.size() > 0 && queue.get(0).player.getUniqueId().toString().equals(player.getUniqueId().toString())) {
            return false;
        }

        List<Location> locations = biomes.availableLocations.get(biome);

        if (locations == null) {
            throw new IllegalArgumentException();
        }

        if (locations.size() == 0) {
            throw new IllegalArgumentException();
        }

        Location sourceLocation = locations.get(new Random().nextInt(locations.size()));

        int centerY = 100;
        while (true) {
            int centerX = (int) (sourceLocation.getBlockX() + ((double) islandSize) / 2.0);
            int centerZ = (int) (sourceLocation.getBlockZ() + ((double) islandSize) / 2.0);

            Material material = islands.sourceWorld.getBlockAt(centerX, centerY, centerZ).getBlockData().getMaterial();
            if (material == Material.STONE || material == Material.SANDSTONE || material == Material.WATER) {
                break;
            }

            centerY--;
        }

        int startX = sourceLocation.getBlockX();
        int startY = centerY - islandSize / 2;
        int startZ = sourceLocation.getBlockZ();

        CopyTask task = new CopyTask(player, startX, startY, startZ, targetX, targetY, targetZ, islandSize);

        // Copy blocks
        if (queue.size() == 0) {
            task.runTaskTimer(islands.plugin, 0, 1);
        }

        addToQueue(task);

        return true;
    }

    public boolean isBlockInIslandShape(int x, int y, int z, int islandSize) {
        return (Math.pow(x - islandSize / 2.0, 2) + (islandSize / Math.pow(y, 2) + 1.3) * Math.pow(y - islandSize / 2.0, 2) + Math.pow(z - islandSize / 2.0, 2))
                <= Math.pow(islandSize / 2.0, 2);
    }

    public boolean isBlockInIslandSphere(int x, int y, int z, int islandSize) {
        return (Math.pow(x - islandSize / 2.0, 2) + Math.pow(y - islandSize / 2.0, 2) + Math.pow(z - islandSize / 2.0, 2))
                <= Math.pow(islandSize / 2.0, 2);
    }

    public boolean isBlockInIslandCircle(int relativeX, int relativeZ, int islandSize) {
        return (Math.pow(relativeX - islandSize / 2.0, 2) + Math.pow(relativeZ - islandSize / 2.0, 2))
                <= Math.pow(islandSize / 2.0, 2);
    }

    static class Messages extends ChatUtils {
        static class Success {

            public static final String GENERATION_DONE = success("Island generation completed.");
        }

        static class Info {
            public static String GENERATION_STARTED(double time) {
                return info("Your generation event has been started. It will take approximately " + (int) time + " seconds.");
            }

            public static String QUEUE_STATUS(int queueSize) {
                return info("Your event has been added to the queue. There are " + (queueSize - 1) + " event(s) before yours.");
            }

            public static String GENERATION_STATUS(int status) {
                return info("Your generation event is " + status + "% completed.");
            }
        }
    }
}
