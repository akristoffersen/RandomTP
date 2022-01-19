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

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import RandomTPAPI;


public class RandomEventHandler {
    ServerPlayer p;
    ServerLevel world;
    int ticker = 0;

    public RandomEventHandler(ServerPlayer p_in, ServerLevel world_in) {
        this.p = p_in;
        this.world = world_in;
    }


    @SubscribeEvent
    public void onTick(TickEvent.WorldTickEvent event) {
        if (this.ticker < 5000){
			ticker++;
		} else {
			//do the method
			ticker = 0;
            RandomTPAPI.randomTPHelper(p, world);
		}
    
}
