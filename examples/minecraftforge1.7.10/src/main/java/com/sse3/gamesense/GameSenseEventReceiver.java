package com.sse3.gamesense;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class GameSenseEventReceiver {

	private float lastHealth = 0;
	private int lastFoodLevel = 0;
	private boolean isHungry = false;
	private boolean isStarted = false;
	private Minecraft _mcInst;
	private long lastTickMS = 0;
	private long timeOfDay = 0;
	private int lastAir = 0;
	private ForgeDirection lastFacing = ForgeDirection.NORTH;
	private ItemStack lastHeldItem = null;
	private GameSenseMod gsmInst = null;

	public GameSenseEventReceiver(Minecraft mcInst) {
		this._mcInst = mcInst;
		this.gsmInst = GameSenseMod.instance;
		lastTickMS = System.currentTimeMillis();

		this.reset();
	}

	public void reset() {
		// Reset our data
		this.lastHealth = 0;
		this.lastFoodLevel = 0;
		this.isHungry = false;
		this.isStarted = false;
		this.lastTickMS = 0;
		this.timeOfDay = 0;
		this.lastAir = 0;
		this.lastFacing = ForgeDirection.NORTH;
		this.lastHeldItem = null;
	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void onLivingUpdate(LivingUpdateEvent event) {
		if (!this.isStarted)
			return;
		
		long curElapsed = System.currentTimeMillis() - this.lastTickMS;
		// Update threshold. Periodically update all values at this rate, in milliseconds
		long updateThreshold = 1000;
		boolean doPeriodicUpdate = false;

		// Min time between potential updates: 100ms.
		if (curElapsed > 100 && (event.entity instanceof EntityPlayerSP)) {

			if(curElapsed > updateThreshold) {
				doPeriodicUpdate = true;
				this.lastTickMS = System.currentTimeMillis();
			}
			
			EntityPlayer player = (EntityPlayer) event.entity;

			if (doPeriodicUpdate || player.getHealth() != this.lastHealth) {
				this.lastHealth = player.getHealth();
				float maxHealth = player.getMaxHealth();
				// Post health to sse3 socket
				gsmInst.SendGameEvent("HEALTH", (100 * ((int) this.lastHealth) / ((int) maxHealth)), player);
			}

			if (doPeriodicUpdate || player.getFoodStats().getFoodLevel() != this.lastFoodLevel) {
				this.lastFoodLevel = this._mcInst.thePlayer.getFoodStats().getFoodLevel();
				gsmInst.SendGameEvent("HUNGERLEVEL", this.lastFoodLevel * 5, player);
			}

			if (doPeriodicUpdate || player.getFoodStats().needFood() != this.isHungry) {
				this.isHungry = player.getFoodStats().needFood();
				gsmInst.SendGameEvent("HUNGRY", this.isHungry, player);
			}

			if (doPeriodicUpdate || player.getAir() != this.lastAir) {
				this.lastAir = player.getAir();
				gsmInst.SendGameEvent("AIRLEVEL", (int)(this.lastAir / 3), player);
			}

			 //Compass direction facing
			if (doPeriodicUpdate || getFacing(player) != this.lastFacing) {
				this.lastFacing = getFacing(player);
				gsmInst.SendGameEvent("FACING", this.lastFacing.toString().toUpperCase(), player);
			}
			
			

			if (doPeriodicUpdate || player.getHeldItem() != this.lastHeldItem) {

				this.lastHeldItem = player.getHeldItem();

				// Double check the currentHeldItem is valid.
				if (this.lastHeldItem != null) {

					// Check if player is holding a tool, if so, send game event
					// of what type of tool, material class, and durability
					Item heldItem = this.lastHeldItem.getItem();
					int heldItemDurability = 100 - (int) (heldItem.getDurabilityForDisplay(this.lastHeldItem) * 100);
					String heldItemMaterialName = "";
					String heldItemType = "";

					String heldItemClassName = heldItem.getClass().getSimpleName();
					switch (heldItemClassName) {
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
							heldItemMaterialName = ((ItemHoe) heldItem).getToolMaterialName();
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
					// TODO: Add more held items to send game events for
					}

					if (heldItemType != "") {
						gsmInst.SendGameEvent("TOOL", heldItemType, player);
						gsmInst.SendGameEvent("TOOLMATERIAL", heldItemMaterialName, player);
						gsmInst.SendGameEvent("TOOLDURABILITY", heldItemDurability, player);
						gsmInst.SendGameEvent("SHOWTOOL", 1, player);
					} else {
						gsmInst.SendGameEvent("TOOL", "NONE", player);
						gsmInst.SendGameEvent("TOOLDURABILITY", 0, player);
						gsmInst.SendGameEvent("SHOWTOOL", 1, player);
					}
				} else {
					gsmInst.SendGameEvent("TOOL", "NONE", player);
					gsmInst.SendGameEvent("TOOLDURABILITY", 0, player);
					gsmInst.SendGameEvent("SHOWTOOL", 1, player);
				}
			}

			if (doPeriodicUpdate || this._mcInst.theWorld.getWorldTime() != this.timeOfDay) {
				this.timeOfDay = this._mcInst.theWorld.getWorldTime();
				//sse3Inst.SendGameEvent("TIMEOFDAY", (int)(this.timeOfDay), player);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void onWorldLoad(WorldEvent.Load event) {
		// Just send START event
		gsmInst.SendGameEvent("START", 1, null);
		this.isStarted = true;
	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void onWorldUnload(WorldEvent.Unload event) {
		// Just send FINISH event
		gsmInst.SendGameEvent("FINISH", 1, null);
		this.reset();
	}
	
	 public static ForgeDirection getFacing(Entity entity) {
	        int dir = MathHelper.floor_double((entity.rotationYaw / 90) + 0.5) & 3;
	        return ForgeDirection.VALID_DIRECTIONS[Direction.directionToFacing[dir]];
	    }
}
