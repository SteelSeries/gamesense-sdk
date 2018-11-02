package com.sse3.gamesense.internal;

import com.sse3.gamesense.GameSenseMod;
import com.sse3.gamesense.config.LoadConfig;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.WorldEvent;

import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

public class EventReceiver
{

    private float lastHealth;
    private int lastFoodLevel;
    private boolean isHungry;
    private boolean isStarted;
    private Minecraft _mcInst;
    private long lastTickMS;
    //private long timeOfDay;
    private int lastAir;
    private EnumFacing lastFacing;
    private ItemStack lastHeldItem;
    private GameSenseMod gsmInst;
    public static LoadConfig config;
    static {
        config = new LoadConfig(new File(Launch.minecraftHome, "/config/GameSense Mod.cfg"));
    }

    public EventReceiver(Minecraft mcInst)
    {
        this._mcInst = mcInst;
        gsmInst = null;
        this.gsmInst = GameSenseMod.instance;
        lastTickMS = 0;
        lastTickMS = System.currentTimeMillis();

        this.reset();
        lastHealth = 0;
        lastFoodLevel = 0;
        isHungry = false;
        isStarted = false;
        //timeOfDay = 0;
        lastAir = 0;
        lastFacing = EnumFacing.NORTH;
        lastHeldItem = null;
    }

    public void reset()
    {
        // Reset our data
        this.lastHealth = 0;
        this.lastFoodLevel = 0;
        this.isHungry = false;
        this.isStarted = false;
        this.lastTickMS = 0;
        //this.timeOfDay = 0;
        this.lastAir = 0;
        this.lastFacing = EnumFacing.NORTH;
        this.lastHeldItem = null;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onLivingUpdate(LivingUpdateEvent event)
    {
        if (!this.isStarted)
            return;
        long curElapsed = System.currentTimeMillis() - this.lastTickMS;
        // Update threshold. Periodically update all values at this rate, in milliseconds
        long updateThreshold = 1000;
        boolean doPeriodicUpdate = false;

        // Min time between potential updates: 100ms.
        if (curElapsed > 100 && (event.getEntity() instanceof EntityPlayerSP)) {

            if(curElapsed > updateThreshold) {
                doPeriodicUpdate = true;
                this.lastTickMS = System.currentTimeMillis();
            }

            EntityPlayer player = (EntityPlayer) event.getEntity();

            if (doPeriodicUpdate || player.getHealth() != this.lastHealth) {
                this.lastHealth = player.getHealth();
                float maxHealth = player.getMaxHealth();
                // Post health to sse3 socket
                gsmInst.SendGameEvent("HEALTH", (100 * ((int) this.lastHealth) / ((int) maxHealth)), player);
            }

            if (doPeriodicUpdate || player.getFoodStats().getFoodLevel() != this.lastFoodLevel) {
                this.lastFoodLevel = this._mcInst.player.getFoodStats().getFoodLevel();
                gsmInst.SendGameEvent("HUNGERLEVEL", this.lastFoodLevel * 5, player);
            }

            if (doPeriodicUpdate || player.getFoodStats().needFood() != this.isHungry) {
                this.isHungry = player.getFoodStats().needFood();
                gsmInst.SendGameEvent("HUNGRY", this.isHungry, player);
            }

            if (doPeriodicUpdate || player.getAir() != this.lastAir) {
                this.lastAir = player.getAir();
                gsmInst.SendGameEvent("AIRLEVEL", (this.lastAir / 3), player);
            }

            // Compass direction facing
            if (doPeriodicUpdate || player.getHorizontalFacing() != this.lastFacing) {
                this.lastFacing = player.getHorizontalFacing();
                gsmInst.SendGameEvent("FACING", this.lastFacing.toString().toUpperCase(), player);
            }

            if (doPeriodicUpdate || player.getHeldItemMainhand() != this.lastHeldItem) {

                this.lastHeldItem = player.getHeldItemMainhand();

                // Double check the currentHeldItem is valid.

                // Check if player is holding a tool, if so, send game event
                // of what type of tool, material class, and durability
                Item heldItem = this.lastHeldItem.getItem();
                int heldItemDurability = 100 - (int) (heldItem.getDurabilityForDisplay(this.lastHeldItem) * 100);
                String heldItemMaterialName = null;
                String heldItemType;

                String heldItemClassName = heldItem.getClass().getSimpleName();
                //System.out.println(heldItemClassName); // DEBUG: get ItemName of Handheld item in logs
                switch (heldItemClassName) {
                    //
                    // Vanilla Minecraft Items
                    //
                    case "ItemAxe": {
                        heldItemMaterialName = ((ItemTool) heldItem).getToolMaterialName();
                        heldItemType = "AXE";
                        break;
                    }
                    case "ItemSpade": {
                        heldItemMaterialName = ((ItemTool) heldItem).getToolMaterialName();
                        heldItemType = "SHOVEL";
                        break;
                    }
                    case "ItemPickaxe": {
                        heldItemMaterialName = ((ItemTool) heldItem).getToolMaterialName();
                        heldItemType = "PICKAXE";
                        break;
                    }
                    case "ItemHoe": {
                        heldItemMaterialName = ((ItemHoe) heldItem).getMaterialName();
                        heldItemType = "HOE";
                        break;
                    }
                    case "ItemSword": {
                        heldItemMaterialName = ((ItemSword) heldItem).getToolMaterialName();
                        heldItemType = "SWORD";
                        break;
                    }
                    case "ItemShears": {
                        // Shears are always IRON
                        heldItemMaterialName = "IRON";
                        heldItemType = "SHEARS";
                        break;
                    }
                    // Add new Vanilla Minecraft Items
                    case "ItemBow": {
                        // Shears are always IRON
                        heldItemType = "SWORD";
                        break;
                    }
                    case "ItemShield": {
                        // Shears are always IRON
                        heldItemType = "SWORD";
                        break;
                    }

                    //
                    // Tinkers Construct
                    //
                    case "Shovel": {
                        heldItemType = "SHOVEL";
                        break;
                    }
                    case "Hatchet": {
                        heldItemType = "AXE";
                        break;
                    }
                    case "Rapier": {
                        heldItemType = "SWORD";
                        break;
                    }
                    case "LongSword": {
                        heldItemType = "SWORD";
                        break;
                    }
                    case "BroadSword": {
                        heldItemType = "SWORD";
                        break;
                    }
                    case "Scythe": {
                        heldItemType = "SHEARS";
                        break;
                    }
                    case "Mattock": {
                        heldItemType = "HOE";
                        break;
                    }
                    case "Kama": {
                        heldItemType = "SWORD";
                        break;
                    }
                    case "Hammer": {
                        heldItemType = "PICKAXE";
                        break;
                    }
                    case "Excavator": {
                        heldItemType = "SHOVEL";
                        break;
                    }
                    case "LumberAxe": {
                        heldItemType = "AXE";
                        break;
                    }
                    case "FryPan": {
                        heldItemType = "SWORD";
                        break;
                    }
                    case "LongBow": {
                        heldItemType = "SWORD";
                        break;
                    }

                    // TODO: Add more held items to send game events for
                    default: {
                        heldItemType = "NONE";
                        break;
                    }
                }

                if (!heldItemType.isEmpty()) { // heldItemType is always true
                    gsmInst.SendGameEvent("TOOL", heldItemType, player);
                    gsmInst.SendGameEvent("TOOLMATERIAL", heldItemMaterialName, player);
                    gsmInst.SendGameEvent("TOOLDURABILITY", heldItemDurability, player);
                    gsmInst.SendGameEvent("SHOWTOOL", 1, player);
                } else {
                    gsmInst.SendGameEvent("TOOL", "NONE", player);
                    gsmInst.SendGameEvent("TOOLDURABILITY", 0, player);
                    gsmInst.SendGameEvent("SHOWTOOL", 1, player);
                }
            }

            //if (doPeriodicUpdate || this._mcInst.world.getWorldTime() != this.timeOfDay) {
            //    this.timeOfDay = this._mcInst.world.getWorldTime();
            //    sse3Inst.SendGameEvent("TIMEOFDAY", (int)(this.timeOfDay), player);
            //}
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onWorldLoad(WorldEvent.Load event)
    {
        // Just send START event
        gsmInst.SendGameEvent("START", 1, null);
        this.isStarted = true;

    }
}