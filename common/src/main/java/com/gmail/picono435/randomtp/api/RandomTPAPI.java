package com.gmail.picono435.randomtp.api;

import com.gmail.picono435.randomtp.RandomTP;
import com.gmail.picono435.randomtp.config.Config;
import com.gmail.picono435.randomtp.config.Messages;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
// import net.minecraft.world.biome.Biome;
// import net.minecraftforge.fml.common.registry;

// import net.minecraft.util;
// import net.minecraft.util.Util;
// import net.minecraft.world.biome.Biome;
// import net.minecraft.init.Biomes;

import net.minecraft.world.level.biome.Biome;

import RandomEventHandler;


import java.util.Map;
import java.util.Random;
import java.lang.Thread;
import java.io.File;

// import com.gmail.picono435.randomtp.FileHelper;

// import java.io.File;

import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.nio.file.Paths;
import java.nio.file.Path;



public class RandomTPAPI {

    public static void randomTeleport(ServerPlayer p, ServerLevel world) {
        try  {
            RandomEventHandler handler = new RandomEventHandler(p, world);
            MinecraftForge.EVENT_BUS.register(handler);
            // randomTPHelper(p, world);
        } catch(Exception ex) {
            RandomTP.getLogger().info("Error executing command.");
            ex.printStackTrace();
        }
    }

    public static void randomTPHelper(ServerPlayer p, ServerLevel world) {
        Random r = new Random(12345); // deterministic

        int lowX = ((int)Math.round(Math.abs(p.getX())) + Config.getMinDistance()) * -1;
        int highX = Math.abs((int)Math.round(p.getX()) + Config.getMaxDistance());
        int lowZ = ((int)Math.round(Math.abs(p.getZ())) + Config.getMinDistance()) * -1;
        int highZ = Math.abs((int)Math.round(p.getZ()) + Config.getMaxDistance());
        if(Config.getMaxDistance() == 0) {
            highX = (int) (world.getWorldBorder().getSize() / 2);
            highZ = (int) (world.getWorldBorder().getSize() / 2);
        }
        int x = r.nextInt(highX-lowX) + lowX;
        int y = 50;
        int z = r.nextInt(highZ-lowZ) + lowZ;
        int maxTries = Config.getMaxTries();
        while (!isSafe(world, x, y, z) && (maxTries == -1 || maxTries > 0)) {
            y++;
            if(y >= 120) {
                x = r.nextInt(highX-lowX) + lowX;
                y = 50;
                z = r.nextInt(highZ-lowZ) + lowZ;
                continue;
            }
            if(maxTries > 0){
                maxTries--;
            }
            if(maxTries == 0) {
                TextComponent msg = new TextComponent(Messages.getMaxTries().replaceAll("\\{playerName\\}", p.getName().getString()).replaceAll("&", "§"));
                p.sendMessage(msg, p.getUUID());
                return;
            }
        }

        p.teleportTo(world, x, y, z, p.getXRot(), p.getYRot());

        // logging
        Biome biome = world.getBiome(p.blockPosition());
        String biomeName = biome.toString();


        String str_output = "X: " + String.valueOf(x) + " Y: " + String.valueOf(y) + " Z: " + String.valueOf(z) + " Biome: " + biomeName + "\n";
        String filename = "teleport.log";

        Path file = Paths.get(filename);
        if (!Files.exists(file)) {
            // create the file
            File new_file = new File(filename);
            new_file.createNewFile();
        }

        // List<String> lines = Arrays.asList(str_output);
        Files.write(file, str_output.getBytes(), StandardOpenOption.APPEND);

        TextComponent successful = new TextComponent(Messages.getSuccessful().replaceAll("\\{playerName\\}", p.getName().getString()).replaceAll("\\{blockX\\}", "" + (int)p.position().x).replaceAll("\\{blockY\\}", "" + (int)p.position().y).replaceAll("\\{blockZ\\}", "" + (int)p.position().z).replaceAll("&", "§"));
        p.sendMessage(successful, p.getUUID());
    }

    public static ServerLevel getWorld(String world, MinecraftServer server) {
        try {
            ResourceLocation resourcelocation = ResourceLocation.tryParse(world);
            ResourceKey<Level> registrykey = ResourceKey.create(Registry.DIMENSION_REGISTRY, resourcelocation);
            ServerLevel worldTo = server.getLevel(registrykey);
            return worldTo;
        } catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static boolean checkCooldown(ServerPlayer p, Map<String, Long> cooldowns) {
        int cooldownTime = Config.getCooldown();
        if(cooldowns.containsKey(p.getName().getString())) {
            long secondsLeft = ((cooldowns.get(p.getName().getString())/1000)+cooldownTime) - (System.currentTimeMillis()/1000);
            if(secondsLeft > 0) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public static long getCooldownLeft(ServerPlayer p, Map<String, Long> cooldowns) {
        int cooldownTime = Config.getCooldown();
        long secondsLeft = ((cooldowns.get(p.getName().getString())/1000)+cooldownTime) - (System.currentTimeMillis()/1000);
        return secondsLeft;
    }

    @ExpectPlatform
    public static boolean hasPermission(ServerPlayer player, String permission) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean hasPermission(CommandSourceStack source, String permission) {
        throw new AssertionError();
    }

    public static boolean isSafe(ServerLevel world, int newX, int newY, int newZ) {
        if(newX >= world.getWorldBorder().getMaxX() || newZ >= world.getWorldBorder().getMaxZ()) return false;
        if ((isEmpty(world, newX, newY, newZ)) &&
                (!isDangerBlock(world, newX, newY - 1, newZ))) {
            return true;
        }
        return false;
    }

    public static boolean isEmpty(Level world, int newX, int newY, int newZ) {
        if ((world.isEmptyBlock(new BlockPos(newX, newY, newZ))) && (world.isEmptyBlock(new BlockPos(newX, newY + 1, newZ))) &&
                (world.isEmptyBlock(new BlockPos(newX + 1, newY, newZ))) && (world.isEmptyBlock(new BlockPos(newX - 1, newY, newZ))) &&
                (world.isEmptyBlock(new BlockPos(newX, newY, newZ + 1))) && (world.isEmptyBlock(new BlockPos(newX, newY, newZ - 1)))) {
            return true;
        }
        return false;
    }

    public static boolean isDangerBlock(Level world, int newX, int newY, int newZ) {
        for (Block block : getDangerBlocks()) {
            if (block.equals(world.getBlockState(new BlockPos(newX, newY, newZ)).getBlock())) {
                return true;
            }
        }
        return false;
    }

    public static Block[] getDangerBlocks() {
        return new Block[] {Blocks.LAVA, Blocks.WATER, Blocks.AIR};
    }
}
