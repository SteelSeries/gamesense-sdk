package com.sse3.gamesense;

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

public class GameSenseEventReceiver {

	private float lastHealth = 0;
	private int lastFoodLevel = 0;
	private boolean isHungry = false;
	private boolean isStarted = false;
	private Minecraft _mcInst;
	private long lastTickMS = 0;
	private long timeOfDay = 0;
	private int lastAir = 0;
	private EnumFacing lastFacing = EnumFacing.NORTH;
	private ItemStack lastHeldItem = null;
	private GameSenseMod gsmInst = null;

	public GameSenseEventReceiver(Minecraft mcInst) {
		_mcInst = mcInst;
		gsmInst = GameSenseMod.instance;
		lastTickMS = System.currentTimeMillis();
		reset();
	}

	public void reset() {
		// Reset our data
		lastHealth = 0;
		lastFoodLevel = 0;
		isHungry = false;
		isStarted = false;
		lastTickMS = 0;
		timeOfDay = 0;
		lastAir = 0;
		lastFacing = EnumFacing.NORTH;
		lastHeldItem = null;
	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void onLivingUpdate(LivingUpdateEvent event) {
		if (!isStarted)
			return;
		
		long curElapsed = System.currentTimeMillis() - lastTickMS;
		// Update threshold. Periodically update all values at this rate, in milliseconds
		long updateThreshold = 1000;
		boolean doPeriodicUpdate = false;

		// Min time between potential updates: 100ms.
		if (curElapsed > 100 && (event.getEntity() instanceof EntityPlayerSP)) {

			if(curElapsed > updateThreshold) {
				doPeriodicUpdate = true;
				lastTickMS = System.currentTimeMillis();
			}
			
			EntityPlayer player = (EntityPlayer) event.getEntity();

			if (doPeriodicUpdate || player.getHealth() != lastHealth) {
				lastHealth = player.getHealth();
				float maxHealth = player.getMaxHealth();
				// Post health to sse3 socket
				gsmInst.SendGameEvent("HEALTH", (100 * ((int) lastHealth) / ((int) maxHealth)), player);
			}

			if (doPeriodicUpdate || player.getFoodStats().getFoodLevel() != lastFoodLevel) {
				lastFoodLevel = _mcInst.thePlayer.getFoodStats().getFoodLevel();
				gsmInst.SendGameEvent("HUNGERLEVEL", lastFoodLevel * 5, player);
			}

			if (doPeriodicUpdate || player.getFoodStats().needFood() != isHungry) {
				isHungry = player.getFoodStats().needFood();
				gsmInst.SendGameEvent("HUNGRY", isHungry, player);
			}

			if (doPeriodicUpdate || player.getAir() != lastAir) {
				lastAir = player.getAir();
				gsmInst.SendGameEvent("AIRLEVEL", (int)(lastAir / 3), player);
			}

			// Compass direction facing
			if (doPeriodicUpdate || player.getHorizontalFacing() != lastFacing) {
				lastFacing = player.getHorizontalFacing();
				gsmInst.SendGameEvent("FACING", lastFacing.toString().toUpperCase(), player);
			}

			if (doPeriodicUpdate || player.getHeldItemMainhand() != lastHeldItem) {

				lastHeldItem = player.getHeldItemMainhand();

				// Double check the currentHeldItem is valid.
				if (lastHeldItem != null) {

					// Check if player is holding a tool, if so, send game event
					// of what type of tool, material class, and durability
					Item heldItem = lastHeldItem.getItem();
					int heldItemDurability = 100 - (int) (heldItem.getDurabilityForDisplay(lastHeldItem) * 100);
					String heldItemMaterialName = "";
					String heldItemType = "";

					String heldItemClassName = heldItem.getClass().getSimpleName();
					if(heldItemClassName.equals("ItemAxe")) {
						heldItemMaterialName = ((ItemTool) heldItem).getToolMaterialName();
						heldItemType = "AXE";
					}else if(heldItemClassName.equals("ItemSpade")) {
						heldItemMaterialName = ((ItemTool) heldItem).getToolMaterialName();
						heldItemType = "SHOVEL";
					}else if(heldItemClassName.equals("ItemPickaxe")) {
						heldItemMaterialName = ((ItemTool) heldItem).getToolMaterialName();
						heldItemType = "PICKAXE";
					}else if(heldItemClassName.equals("ItemHoe")) {
						heldItemMaterialName = ((ItemHoe) heldItem).getMaterialName();
						heldItemType = "HOE";
					}else if(heldItemClassName.equals("ItemSword")) {
						heldItemMaterialName = ((ItemSword) heldItem).getToolMaterialName();
						heldItemType = "SWORD";
					}else if(heldItemClassName.equals("ItemShears")) {
						heldItemMaterialName = "IRON";
						heldItemType = "SHEARS";
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

			if (doPeriodicUpdate || _mcInst.theWorld.getWorldTime() != timeOfDay) {
				timeOfDay = _mcInst.theWorld.getWorldTime();
				//sse3Inst.SendGameEvent("TIMEOFDAY", (int)(this.timeOfDay), player);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void onWorldLoad(WorldEvent.Load event) {
		// Just send START event
		gsmInst.SendGameEvent("START", 1, null);
		isStarted = true;
	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void onWorldUnload(WorldEvent.Unload event) {
		// Just send FINISH event
		gsmInst.SendGameEvent("FINISH", 1, null);
		reset();
	}
}