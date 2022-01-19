package com.gmail.picono435.randomtp.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.eventbus.api.SubscribeEvent;

//import net.minecraft.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;

import com.gmail.picono435.randomtp.api.RandomTPAPI;


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
        if (this.ticker < 5000) {
            ticker++;
        } else {
            //do the method
            ticker = 0;
            RandomTPAPI.randomTPHelper(p, world);
        }
    }
}