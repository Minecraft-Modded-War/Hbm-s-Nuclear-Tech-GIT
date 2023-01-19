package com.hbm.main;

import com.hbm.blocks.ModBlocks;
import com.hbm.capability.HbmCapability;
import com.hbm.capability.HbmCapability.IHBMData;
import com.hbm.capability.HbmLivingCapability;
import com.hbm.capability.HbmLivingProps;
import com.hbm.config.CompatibilityConfig;
import com.hbm.config.GeneralConfig;
import com.hbm.entity.logic.IChunkLoader;
import com.hbm.entity.mob.*;
import com.hbm.entity.projectile.EntityBurningFOEQ;
import com.hbm.entity.projectile.EntityMeteor;
import com.hbm.forgefluid.FFPipeNetwork;
import com.hbm.handler.*;
import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.interfaces.IBomb;
import com.hbm.inventory.AssemblerRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.armor.ItemArmorMod;
import com.hbm.items.armor.ItemModRevive;
import com.hbm.items.armor.ItemModShackles;
import com.hbm.items.gear.ArmorFSB;
import com.hbm.items.special.ItemHot;
import com.hbm.items.weapon.ItemGunBase;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import com.hbm.packet.*;
import com.hbm.particle.bullet_hit.EntityHitDataHandler;
import com.hbm.saveddata.AuxSavedData;
import com.hbm.saveddata.RadiationSavedData;
import com.hbm.tileentity.machine.rbmk.RBMKDials;
import com.hbm.util.EnchantmentUtil;
import com.hbm.util.EntityDamageUtil;
import com.hbm.world.generator.TimedGenerator;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.RandomChanceWithLooting;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityEvent.EnteringChunk;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.registries.DataSerializerEntry;
import org.apache.logging.log4j.Level;

import java.util.*;
import java.util.Map.Entry;

public class ModEventHandler {

	public static final ResourceLocation ENT_HBM_PROP_ID = new ResourceLocation(MainRegistry.MODID, "HBMLIVINGPROPS");
	public static final ResourceLocation DATA_LOC = new ResourceLocation(MainRegistry.MODID, "HBMDATA");

	public static boolean showMessage = true;
	public static int meteorShower = 0;
	public static Random rand = new Random();

	@SubscribeEvent
	public void soundRegistering(RegistryEvent.Register<SoundEvent> evt) {

		for(SoundEvent e : HBMSoundHandler.ALL_SOUNDS) {
			evt.getRegistry().register(e);
		}
	}
	
	@SubscribeEvent
	public void attachRadCap(AttachCapabilitiesEvent<Entity> e) {
		if(e.getObject() instanceof EntityLivingBase)
			e.addCapability(ENT_HBM_PROP_ID, new HbmLivingCapability.EntityHbmPropsProvider());
		if(e.getObject() instanceof EntityPlayer){
			e.addCapability(DATA_LOC, new HbmCapability.HBMDataProvider());
		}
	}

	@SubscribeEvent
	public void worldUnload(WorldEvent.Unload e) {
		Iterator<FFPipeNetwork> itr = MainRegistry.allPipeNetworks.iterator();
		while(itr.hasNext()) {
			FFPipeNetwork net = itr.next();
			if(net.getNetworkWorld() == e.getWorld()) {
				net.destroySoft();
				itr.remove();
			}
		}
	}

	@SubscribeEvent
	public void enteringChunk(EnteringChunk evt) {
		if(evt.getEntity() instanceof IChunkLoader) {
			((IChunkLoader) evt.getEntity()).loadNeighboringChunks(evt.getNewChunkX(), evt.getNewChunkZ());
		}
	}
	
	@SubscribeEvent
	public void onItemToss(ItemTossEvent event){
		ItemStack yeet = event.getEntityItem().getItem();
		
		if(yeet.getItem() instanceof ItemArmor && ArmorModHandler.hasMods(yeet)) {
			
			ItemStack[] mods = ArmorModHandler.pryMods(yeet);
			ItemStack cladding = mods[ArmorModHandler.cladding];
			
			if(cladding != null && cladding.getItem() == ModItems.cladding_obsidian) {
				event.getEntity().setEntityInvulnerable(true);
			}
		}
		
		if(yeet.getItem() == ModItems.bismuth_tool) {
			event.getEntity().setEntityInvulnerable(true);
		}
	}
	
	@SubscribeEvent
	public void lootTableLoad(LootTableLoadEvent e){
		//Drillgon200: Yeah we're doing this in code. Screw minecraft json.
		if(CompatibilityConfig.modLoot){
			addWeightedRandomToLootTable(e, LootTableList.CHESTS_VILLAGE_BLACKSMITH, new WeightedRandomChestContentFrom1710(new ItemStack(ModItems.armor_polish), 1, 1, 3));
			addWeightedRandomToLootTable(e, LootTableList.CHESTS_VILLAGE_BLACKSMITH, new WeightedRandomChestContentFrom1710(new ItemStack(ModItems.bathwater), 1, 1, 1));
			addWeightedRandomToLootTable(e, LootTableList.CHESTS_ABANDONED_MINESHAFT, new WeightedRandomChestContentFrom1710(new ItemStack(ModItems.bathwater), 1, 1, 1));
			addWeightedRandomToLootTable(e, LootTableList.CHESTS_ABANDONED_MINESHAFT, new WeightedRandomChestContentFrom1710(new ItemStack(ModItems.serum), 1, 1, 5));
			addWeightedRandomToLootTable(e, LootTableList.CHESTS_SIMPLE_DUNGEON, new WeightedRandomChestContentFrom1710(new ItemStack(ModItems.heart_piece), 1, 1, 1));
			addWeightedRandomToLootTable(e, LootTableList.CHESTS_DESERT_PYRAMID, new WeightedRandomChestContentFrom1710(new ItemStack(ModItems.heart_piece), 1, 1, 1));
			addWeightedRandomToLootTable(e, LootTableList.CHESTS_JUNGLE_TEMPLE, new WeightedRandomChestContentFrom1710(new ItemStack(ModItems.heart_piece), 1, 1, 1));
			addWeightedRandomToLootTable(e, LootTableList.CHESTS_SIMPLE_DUNGEON, new WeightedRandomChestContentFrom1710(new ItemStack(ModItems.scrumpy), 1, 1, 1));
			addWeightedRandomToLootTable(e, LootTableList.CHESTS_DESERT_PYRAMID, new WeightedRandomChestContentFrom1710(new ItemStack(ModItems.scrumpy), 1, 1, 1));
		}
	}

	private void addWeightedRandomToLootTable(LootTableLoadEvent e, ResourceLocation loc, WeightedRandomChestContentFrom1710 content){
		if(e.getName().equals(loc)){
			LootCondition[] conds = new LootCondition[0];
			LootFunction[] funcs = new LootFunction[1];
			funcs[0] = new LootFunction(conds){
				@Override
				public ItemStack apply(ItemStack stack, Random rand, LootContext context){
					ItemStack sta = content.theItemId.copy();
					sta.setCount(content.theMinimumChanceToGenerateItem + rand.nextInt(content.theMaximumChanceToGenerateItem - content.theMinimumChanceToGenerateItem + 1));
					return sta;
				}
			};
			LootEntry entry = new LootEntryItem(content.theItemId.getItem(), content.itemWeight, 1, funcs, conds, content.theItemId.getUnlocalizedName() + "_loot");
			LootPool pool = new LootPool(new LootEntry[]{entry}, new LootCondition[]{new RandomChanceWithLooting(0.25F, 0.1F)}, new RandomValueRange(1), new RandomValueRange(0), content.theItemId.getUnlocalizedName() + "_loot");
			e.getTable().addPool(pool);
		}
	}
	
	@SubscribeEvent
	public void itemSmelted(PlayerEvent.ItemSmeltedEvent e) {
		
		if(!e.player.world.isRemote && e.smelting.getItem() == Items.IRON_INGOT && e.player.getRNG().nextInt(64) == 0) {
			
			if(!e.player.inventory.addItemStackToInventory(new ItemStack(ModItems.lodestone)))
				e.player.dropItem(new ItemStack(ModItems.lodestone), false);
			else
				e.player.inventoryContainer.detectAndSendChanges();
		}
		
		if(!e.player.world.isRemote && e.smelting.getItem() == ModItems.ingot_uranium && e.player.getRNG().nextInt(64) == 0) {
			
			if(!e.player.inventory.addItemStackToInventory(new ItemStack(ModItems.quartz_plutonium)))
				e.player.dropItem(new ItemStack(ModItems.quartz_plutonium), false);
			else
				e.player.inventoryContainer.detectAndSendChanges();
		}
	}
	
	@SubscribeEvent
	public void mobSpawn(LivingSpawnEvent event) {
		if(CompatibilityConfig.mobGear){
			EntityLivingBase entity = event.getEntityLiving();
			World world = event.getWorld();

			if(entity instanceof EntityZombie) {
				if(rand.nextInt(64) == 0)
					entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ModItems.gas_mask_m65, 1, world.rand.nextInt(100)));
				if(rand.nextInt(128) == 0)
					entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ModItems.gas_mask, 1, world.rand.nextInt(100)));
				if(rand.nextInt(256) == 0)
					entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ModItems.mask_of_infamy, 1, world.rand.nextInt(100)));
				if(rand.nextInt(1024) == 0)
					entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ModItems.starmetal_plate, 1, world.rand.nextInt(ModItems.starmetal_plate.getMaxDamage(ItemStack.EMPTY))));

				if(rand.nextInt(128) == 0)
					entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.pipe_lead, 1, world.rand.nextInt(100)));
				if(rand.nextInt(128) == 0)
					entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.reer_graar, 1, world.rand.nextInt(100)));
				if(rand.nextInt(128) == 0)
					entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.pipe_rusty, 1, world.rand.nextInt(100)));
				if(rand.nextInt(128) == 0)
					entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.crowbar, 1, world.rand.nextInt(100)));
				if(rand.nextInt(128) == 0)
					entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.geiger_counter, 1));
				if(rand.nextInt(128) == 0)
					entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.steel_pickaxe, 1, world.rand.nextInt(300)));
				if(rand.nextInt(512) == 0)
					entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.stopsign));
				if(rand.nextInt(512) == 0)
					entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.sopsign));
				if(rand.nextInt(512) == 0)
					entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.chernobylsign));
			}
			if(entity instanceof EntitySkeleton) {
				if(rand.nextInt(16) == 0) {
					entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ModItems.gas_mask_m65, 1, world.rand.nextInt(100)));

					if(rand.nextInt(32) == 0) {
						entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.syringe_poison));
					}
				}
			}
		}
	}

	private int parseOInt(Object o){
		if(o == null)
			return 0;
		return (int)o;
	}

	@SubscribeEvent
	public void worldTick(WorldTickEvent event) {
		if(!MainRegistry.allPipeNetworks.isEmpty() && !event.world.isRemote) {
			Iterator<FFPipeNetwork> itr = MainRegistry.allPipeNetworks.iterator();
			while(itr.hasNext()) {
				FFPipeNetwork net = itr.next();
				if(net.getNetworkWorld() != event.world)
					continue;

				if(net != null)
					net.updateTick();

				if(net.getPipes().isEmpty()) {
					net.destroySoft();
					itr.remove();
				}
			}
		}

		if(event.world != null && !event.world.isRemote && GeneralConfig.enableMeteorStrikes) {
			int dimID = event.world.provider.getDimension();
			int dimMeteorShowerChance = parseOInt(CompatibilityConfig.meteorShowerChance.get(dimID));
			int dimMeteorStrikeChance = parseOInt(CompatibilityConfig.meteorStrikeChance.get(dimID));
			if(dimMeteorShowerChance > 0 && dimMeteorStrikeChance > 0){
				if(event.world.rand.nextInt(meteorShower > 0 ? dimMeteorShowerChance : dimMeteorStrikeChance) == 0) {
					if(!event.world.playerEntities.isEmpty()) {
						EntityPlayer p = (EntityPlayer) event.world.playerEntities.get(event.world.rand.nextInt(event.world.playerEntities.size()));
						if(p != null && p.dimension == 0) {
							EntityMeteor meteor = new EntityMeteor(event.world);
							meteor.posX = p.posX + event.world.rand.nextInt(201) - 100;
							meteor.posY = 384;
							meteor.posZ = p.posZ + event.world.rand.nextInt(201) - 100;
							meteor.motionX = event.world.rand.nextDouble() - 0.5;
							meteor.motionY = -2.5;
							meteor.motionZ = event.world.rand.nextDouble() - 0.5;
							event.world.spawnEntity(meteor);
						}
					}
				}

				if(meteorShower > 0) {
					meteorShower--;
					if(meteorShower == 0 && GeneralConfig.enableDebugMode)
						MainRegistry.logger.info("Ended meteor shower.");
				}

				if(event.world.rand.nextInt(dimMeteorStrikeChance * 100) == 0 && GeneralConfig.enableMeteorShowers) {
					int dimMeteorShowerDuration = parseOInt(CompatibilityConfig.meteorShowerDuration.get(dimID));
					meteorShower = (int) (dimMeteorShowerDuration * (0.75 + 0.25 * event.world.rand.nextFloat()));

					if(GeneralConfig.enableDebugMode)
						MainRegistry.logger.info("Started meteor shower! Duration: " + meteorShower);
				}
			}
		}

		// TODO: Make this a config instead of gamerule.
		if (event.world != null && !event.world.isRemote && event.world.getTotalWorldTime() % 40 == 37) {
			//Drillgon200: Retarded hack because I'm not convinced game rules are client sync'd
			PacketDispatcher.wrapper.sendToAll(new SurveyPacket(RBMKDials.getColumnHeight(event.world)));
		}

		if(event.world != null && !event.world.isRemote && GeneralConfig.enableRads) {
			int thunder = AuxSavedData.getThunder(event.world);

			if(thunder > 0)
				AuxSavedData.setThunder(event.world, thunder - 1);

			if(!event.world.loadedEntityList.isEmpty()) {

				RadiationSavedData data = RadiationSavedData.getData(event.world);

				if(data.worldObj == null) {
					data.worldObj = event.world;
				}

				if(event.world.getTotalWorldTime() % 20 == 0 && event.phase == Phase.START) {
					data.updateSystem();
				}

				List<Object> oList = new ArrayList<>();
				oList.addAll(event.world.loadedEntityList);

				for(Object e : oList) {
					if(e instanceof EntityLivingBase) {
						HbmLivingCapability.IEntityHbmProps entRad;
						if(!((EntityLivingBase) e).hasCapability(HbmLivingCapability.EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null))
							continue;

						entRad = ((EntityLivingBase) e).getCapability(HbmLivingCapability.EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null);

						// effect for radiation
						EntityLivingBase entity = (EntityLivingBase) e;
						
						if(entity instanceof EntityPlayer && (((EntityPlayer) entity).capabilities.isCreativeMode || ((EntityPlayer) entity).isSpectator()))
							continue;

						float eRad = entRad.getRads();

						if(entity instanceof EntityCreeper && eRad >= 200 && entity.getHealth() > 0) {

							if(event.world.rand.nextInt(3) == 0) {
								EntityNuclearCreeper creep = new EntityNuclearCreeper(event.world);
								creep.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);

								if(!entity.isDead)
									if(!event.world.isRemote)
										event.world.spawnEntity(creep);
								entity.setDead();
							} else {
								entity.attackEntityFrom(ModDamageSource.radiation, 100F);
							}
							continue;

						} else if(entity instanceof EntityCow && !(entity instanceof EntityMooshroom) && eRad >= 50) {
							EntityMooshroom creep = new EntityMooshroom(event.world);
							creep.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);

							if(!entity.isDead)
								if(!event.world.isRemote)
									event.world.spawnEntity(creep);
							entity.setDead();
							continue;

						} else if(entity instanceof EntityVillager && eRad >= 500) {
							EntityZombie creep = new EntityZombie(event.world);
							creep.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);

							if(!entity.isDead)
								if(!event.world.isRemote)
									event.world.spawnEntity(creep);
							entity.setDead();
							continue;
						} else if(entity instanceof EntityBlaze && eRad >= 700) {
							EntityRADBeast creep = new EntityRADBeast(event.world);
							creep.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);

							if(!entity.isDead)
								if(!event.world.isRemote)
									event.world.spawnEntity(creep);
							entity.setDead();
							continue;
						}

						if(eRad < 200 || entity instanceof EntityNuclearCreeper ||
								entity instanceof EntityMooshroom ||
								entity instanceof EntityZombie ||
								entity instanceof EntitySkeleton) {
							if (eRad > 2500000)
								entRad.setRads(2500000);
						}

						if(eRad >= 1000) {
							entity.attackEntityFrom(ModDamageSource.radiation, 1000F);
							entRad.setRads(0);
							
							if(entity.getHealth() > 0) {
					        	entity.setHealth(0);
					        	entity.onDeath(ModDamageSource.radiation);
							}

						} else if(eRad >= 800) {
							if(event.world.rand.nextInt(300) == 0)
								entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 5 * 30, 0));
							if(event.world.rand.nextInt(300) == 0)
								entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 10 * 20, 2));
							if(event.world.rand.nextInt(300) == 0)
								entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 10 * 20, 2));
							if(event.world.rand.nextInt(500) == 0)
								entity.addPotionEffect(new PotionEffect(MobEffects.POISON, 3 * 20, 2));
							if(event.world.rand.nextInt(700) == 0)
								entity.addPotionEffect(new PotionEffect(MobEffects.WITHER, 3 * 20, 1));
							if(event.world.rand.nextInt(300) == 0)
								entity.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 5 * 20, 3));

						} else if(eRad >= 600) {
							if(event.world.rand.nextInt(300) == 0)
								entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 5 * 30, 0));
							if(event.world.rand.nextInt(300) == 0)
								entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 10 * 20, 2));
							if(event.world.rand.nextInt(300) == 0)
								entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 10 * 20, 2));
							if(event.world.rand.nextInt(500) == 0)
								entity.addPotionEffect(new PotionEffect(MobEffects.POISON, 3 * 20, 1));
							if(event.world.rand.nextInt(300) == 0)
								entity.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 3 * 20, 3));

						} else if(eRad >= 400) {
							if(event.world.rand.nextInt(300) == 0)
								entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 5 * 30, 0));
							if(event.world.rand.nextInt(500) == 0)
								entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 5 * 20, 0));
							if(event.world.rand.nextInt(300) == 0)
								entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 5 * 20, 1));
							if(event.world.rand.nextInt(500) == 0)
								entity.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 3 * 20, 2));

						} else if(eRad >= 200) {
							if(event.world.rand.nextInt(300) == 0)
								entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 5 * 20, 0));
							if(event.world.rand.nextInt(500) == 0)
								entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 5 * 20, 0));
							if(event.world.rand.nextInt(700) == 0)
								entity.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 3 * 20, 2));
						}

					}
				}
			}
		}

		if(event.phase == Phase.START) {
			RadiationWorldHandler.handleWorldDestruction(event.world);
			TimedGenerator.automaton(event.world, 100);
		}
	}
	
	@SubscribeEvent
	public void serverTick(ServerTickEvent e){
		if(e.phase == Phase.START){
			JetpackHandler.serverTick();
			return;
		}

		EntityHitDataHandler.updateSystem();
	}
	
	// Drillgon200: So 1.12.2's going to ignore ISpecialArmor if the damage is
	// unblockable, huh?
	@SubscribeEvent
	public void onEntityHurt(LivingHurtEvent e) {
		EntityLivingBase ent = e.getEntityLiving();
		if(e.getEntityLiving() instanceof EntityPlayer) {
			if(ArmorUtil.checkArmor((EntityPlayer) e.getEntityLiving(), ModItems.euphemium_helmet, ModItems.euphemium_plate, ModItems.euphemium_legs, ModItems.euphemium_boots)) {
				e.setCanceled(true);
			}
		}
		ArmorFSB.handleHurt(e);
		
		/// V1 ///
		if(EntityDamageUtil.wasAttackedByV1(e.getSource())) {
			EntityPlayer attacker = (EntityPlayer) ((EntityDamageSource)e.getSource()).getImmediateSource();
					
			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "vanillaburst");
			data.setInteger("count", (int)Math.min(ent.getMaxHealth() / 2F, 250));
			data.setDouble("motion", 0.1D);
			data.setString("mode", "blockdust");
			data.setInteger("block", Block.getIdFromBlock(Blocks.REDSTONE_BLOCK));
			PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, ent.posX, ent.posY + ent.height * 0.5, ent.posZ), new TargetPoint(ent.dimension, ent.posX, ent.posY, ent.posZ, 50));
					
			if(attacker.getDistanceSq(ent) < 25) {
				attacker.heal(e.getAmount() * 0.5F);
			}
		}
		
		for(int i = 2; i < 6; i++) {
			
			ItemStack armor = ent.getItemStackFromSlot(EntityEquipmentSlot.values()[i]);
			
			if(armor != null && ArmorModHandler.hasMods(armor)) {
				
				for(ItemStack mod : ArmorModHandler.pryMods(armor)) {
					
					if(mod != null && mod.getItem() instanceof ItemArmorMod) {
						((ItemArmorMod)mod.getItem()).modDamage(e, armor);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onEntityAttacked(LivingAttackEvent event) {
		EntityLivingBase e = event.getEntityLiving();

		if(e instanceof EntityPlayer && ArmorUtil.checkArmor((EntityPlayer) e, ModItems.euphemium_helmet, ModItems.euphemium_plate, ModItems.euphemium_legs, ModItems.euphemium_boots)) {
			e.world.playSound(null, e.posX, e.posY, e.posZ, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 5F, 1.0F + e.getRNG().nextFloat() * 0.5F);
			event.setCanceled(true);
		}

		ArmorFSB.handleAttack(event);
	}
	
	@SubscribeEvent
	public void onPlayerFall(PlayerFlyableFallEvent event) {
		ArmorFSB.handleFall(event.getEntityPlayer());
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		EntityPlayer player = event.player;
		ArmorFSB.handleTick(event);

		if(!player.world.isRemote && event.phase == TickEvent.Phase.START) {
			if(!Float.isFinite(player.getHealth()) || !Float.isFinite(player.getAbsorptionAmount())) {
				player.world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.syringeUse, SoundCategory.PLAYERS, 1.0F, 1.0F);
				player.setHealth(player.getMaxHealth());
				player.setAbsorptionAmount(0);
			}
		}

		if(event.phase == Phase.END)
			JetpackHandler.postPlayerTick(event.player);
	}

	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event) {
		HbmLivingProps.setRadiation(event.getEntityLiving(), 0);
		if(event.getEntity().world.isRemote)
			return;

		if(GeneralConfig.enableCataclysm) {
			EntityBurningFOEQ foeq = new EntityBurningFOEQ(event.getEntity().world);
			foeq.setPositionAndRotation(event.getEntity().posX, 500, event.getEntity().posZ, 0.0F, 0.0F);
			event.getEntity().world.spawnEntity(foeq);
		}

		if(event.getEntityLiving() instanceof EntityPlayer) {
			if(ArmorUtil.checkArmor((EntityPlayer) event.getEntityLiving(), ModItems.euphemium_helmet, ModItems.euphemium_plate, ModItems.euphemium_legs, ModItems.euphemium_boots)) {
				event.setCanceled(true);
				event.getEntityLiving().setHealth(event.getEntityLiving().getMaxHealth());
			}
		}
		
		if(!event.getEntityLiving().world.isRemote) {
			
			if(event.getSource() instanceof EntityDamageSource && ((EntityDamageSource)event.getSource()).getTrueSource() instanceof EntityPlayer
					 && !(((EntityDamageSource)event.getSource()).getTrueSource() instanceof FakePlayer)) {
				
				if(event.getEntityLiving() instanceof EntitySpider && event.getEntityLiving().getRNG().nextInt(500) == 0) {
					event.getEntityLiving().dropItem(ModItems.spider_milk, 1);
				}
				
				if(event.getEntityLiving() instanceof EntityCaveSpider && event.getEntityLiving().getRNG().nextInt(100) == 0) {
					event.getEntityLiving().dropItem(ModItems.serum, 1);
				}
				
				if(event.getEntityLiving() instanceof EntityAnimal && event.getEntityLiving().getRNG().nextInt(500) == 0) {
					event.getEntityLiving().dropItem(ModItems.bandaid, 1);
				}
				
				if(event.getEntityLiving() instanceof IMob && event.getEntityLiving().getRNG().nextInt(1000) == 0) {
					event.getEntityLiving().dropItem(ModItems.heart_piece, 1);
				}
				
				if(event.getEntityLiving() instanceof EntityCyberCrab && event.getEntityLiving().getRNG().nextInt(500) == 0) {
					event.getEntityLiving().dropItem(ModItems.wd40, 1);
				}
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityDeathFirst(LivingDeathEvent event){
		for(int i = 2; i < 6; i++) {
			
			ItemStack stack = event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.values()[i]);
			
			if(stack != null && stack.getItem() instanceof ItemArmor && ArmorModHandler.hasMods(stack)) {
				
				ItemStack revive = ArmorModHandler.pryMods(stack)[ArmorModHandler.extra];
				
				if(revive != null) {
					
					//Classic revive
					if(revive.getItem() instanceof ItemModRevive) {
						revive.setItemDamage(revive.getItemDamage() + 1);
						
						if(revive.getItemDamage() >= revive.getMaxDamage()) {
							ArmorModHandler.removeMod(stack, ArmorModHandler.extra);
						} else {
							ArmorModHandler.applyMod(stack, revive);
						}
						
						event.getEntityLiving().setHealth(event.getEntityLiving().getMaxHealth());
						event.getEntityLiving().addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 60, 99));
						event.setCanceled(true);
						return;
					}
					
					//Shackles
					if(revive.getItem() instanceof ItemModShackles && HbmLivingProps.getRadiation(event.getEntityLiving()) < 1000F) {
						
						revive.setItemDamage(revive.getItemDamage() + 1);
						
						int dmg = revive.getItemDamage();
						ArmorModHandler.applyMod(stack, revive);
						
						event.getEntityLiving().setHealth(event.getEntityLiving().getMaxHealth());
						HbmLivingProps.incrementRadiation(event.getEntityLiving(), dmg * dmg);
						event.setCanceled(true);
						return;
					}
				}
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onEntityDeathLast(LivingDeathEvent event){
		EntityLivingBase entity = event.getEntityLiving();
		
		if(EntityDamageUtil.wasAttackedByV1(event.getSource())) {

			NBTTagCompound vdat = new NBTTagCompound();
			vdat.setString("type", "giblets");
			vdat.setInteger("ent", entity.getEntityId());
			PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(vdat, entity.posX, entity.posY + entity.height * 0.5, entity.posZ), new TargetPoint(entity.dimension, entity.posX, entity.posY + entity.height * 0.5, entity.posZ, 150));
			
			entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, SoundCategory.HOSTILE, 2.0F, 0.95F + entity.world.rand.nextFloat() * 0.2F);
			
			EntityPlayer attacker = (EntityPlayer) ((EntityDamageSource)event.getSource()).getImmediateSource();
			
			if(attacker.getDistanceSq(entity) < 100) {
				attacker.heal(entity.getMaxHealth() * 0.25F);
			}
		}
		
		if(entity instanceof EntityPlayer) {
			
			EntityPlayer player = (EntityPlayer) entity;
			
			for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
				
				ItemStack stack = player.inventory.getStackInSlot(i);
				
				if(stack != null && stack.getItem() == ModItems.detonator_deadman) {
					
					if(stack.getTagCompound() != null) {
						
						int x = stack.getTagCompound().getInteger("x");
						int y = stack.getTagCompound().getInteger("y");
						int z = stack.getTagCompound().getInteger("z");

						if(!player.world.isRemote && player.world.getBlockState(new BlockPos(x, y, z)).getBlock() instanceof IBomb) {
							
							((IBomb) player.world.getBlockState(new BlockPos(x, y, z)).getBlock()).explode(player.world, new BlockPos(x, y, z));
							
							if(GeneralConfig.enableExtendedLogging)
								MainRegistry.logger.log(Level.INFO, "[DET] Tried to detonate block at " + x + " / " + y + " / " + z + " by dead man's switch from " + player.getDisplayName() + "!");
						}
						
						player.inventory.setInventorySlotContents(i, null);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event){
		EntityEffectHandler.onUpdate(event.getEntityLiving());
	}

	@SubscribeEvent
	public void onEntityJump(LivingJumpEvent event) {

		if(event.getEntityLiving() instanceof EntityPlayer)
			ArmorFSB.handleJump((EntityPlayer) event.getEntityLiving());
	}

	
	@SubscribeEvent
	public void blockBreak(BlockEvent.BreakEvent event){
		if (!(event.getPlayer() instanceof EntityPlayerMP))
			return;

		Block block = event.getState().getBlock();
		
		if(block == Blocks.COAL_ORE || block == Blocks.COAL_BLOCK || block == ModBlocks.ore_lignite) {
			
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {

				int x = event.getPos().getX() + dir.offsetX;
				int y = event.getPos().getY() + dir.offsetY;
				int z = event.getPos().getZ() + dir.offsetZ;
				
				if(event.getWorld().rand.nextInt(2) == 0 && event.getWorld().getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.AIR)
					event.getWorld().setBlockState(new BlockPos(x, y, z), ModBlocks.gas_coal.getDefaultState());
			}
		}
	}
	
	@SubscribeEvent
	public void clientJoinServer(PlayerLoggedInEvent e) {
		if(e.player instanceof EntityPlayerMP){
			EntityPlayerMP playerMP = (EntityPlayerMP)e.player;
			PacketDispatcher.sendTo(new AssemblerRecipeSyncPacket(AssemblerRecipes.recipeList, AssemblerRecipes.hidden), playerMP);
			JetpackHandler.playerLoggedIn(e);
			IHBMData props = HbmCapability.getData(e.player);
			PacketDispatcher.sendTo(new KeybindPacket(EnumKeybind.TOGGLE_HEAD, props.getEnableHUD()), playerMP);
			PacketDispatcher.sendTo(new KeybindPacket(EnumKeybind.TOGGLE_JETPACK, props.getEnableBackpack()), playerMP);
		}
	}
	
	@SubscribeEvent
	public void worldSave(WorldEvent.Save e) {
		JetpackHandler.worldSave(e);
	}

	@SubscribeEvent
	public void onDataSerializerRegister(RegistryEvent.Register<DataSerializerEntry> evt) {
		evt.getRegistry().register(new DataSerializerEntry(MissileStruct.SERIALIZER).setRegistryName(new ResourceLocation(MainRegistry.MODID, "missile_struct")));
	}
	
	@SubscribeEvent
	public void anvilUpdateEvent(AnvilUpdateEvent event) {

		if (event.getLeft().getItem() instanceof ItemGunBase && event.getRight().getItem() == Items.ENCHANTED_BOOK) {

			event.setOutput(event.getLeft().copy());

			Map<Enchantment, Integer> mapright = EnchantmentHelper.getEnchantments(event.getRight());

			for (Entry<Enchantment, Integer> entry : mapright.entrySet()) {
				Enchantment e = entry.getKey();
				int j = entry.getValue();

				EnchantmentUtil.removeEnchantment(event.getOutput(), e);
				EnchantmentUtil.addEnchantment(event.getOutput(), e, j);
			}

			event.setCost(10);
		}
		if (event.getLeft().getItem() == ModItems.ingot_meteorite && event.getRight().getItem() == ModItems.ingot_meteorite &&
				event.getLeft().getCount() == 1 && event.getRight().getCount() == 1) {

			double h1 = ItemHot.getHeat(event.getLeft());
			double h2 = ItemHot.getHeat(event.getRight());

			if (h1 >= 0.5 && h2 >= 0.5) {

				ItemStack out = new ItemStack(ModItems.ingot_meteorite_forged);
				ItemHot.heatUp(out, (h1 + h2) / 2D);
				event.setOutput(out);
				event.setCost(10);
			}
		}

		if (event.getLeft().getItem() == ModItems.ingot_meteorite_forged && event.getRight().getItem() == ModItems.ingot_meteorite_forged &&
				event.getLeft().getCount() == 1 && event.getRight().getCount() == 1) {

			double h1 = ItemHot.getHeat(event.getLeft());
			double h2 = ItemHot.getHeat(event.getRight());

			if (h1 >= 0.5 && h2 >= 0.5) {

				ItemStack out = new ItemStack(ModItems.blade_meteorite);
				ItemHot.heatUp(out, (h1 + h2) / 2D);
				event.setOutput(out);
				event.setCost(30);
			}
		}

		if (event.getLeft().getItem() == ModItems.meteorite_sword_seared && event.getRight().getItem() == ModItems.ingot_meteorite_forged &&
				event.getLeft().getCount() == 1 && event.getRight().getCount() == 1) {

			double h2 = ItemHot.getHeat(event.getRight());

			if (h2 >= 0.5) {

				ItemStack out = new ItemStack(ModItems.meteorite_sword_reforged);
				event.setOutput(out);
				event.setCost(50);
			}
		}

		if (event.getLeft().getItem() == ModItems.ingot_steel_dusted && event.getRight().getItem() == ModItems.ingot_steel_dusted &&
				event.getLeft().getCount() == event.getRight().getCount()) {

			double h1 = ItemHot.getHeat(event.getLeft());
			double h2 = ItemHot.getHeat(event.getRight());

			if (h2 >= 0.5) {

				int i1 = event.getLeft().getItemDamage();
				int i2 = event.getRight().getItemDamage();

				int i3 = Math.min(i1, i2) + 1;

				boolean done = i3 >= 10;

				ItemStack out;
				if (done) {
					out = new ItemStack(ModItems.ingot_chainsteel, event.getLeft().getCount(), 0);
				} else {
					out = new ItemStack(ModItems.ingot_steel_dusted, event.getLeft().getCount(), i3);
				}

				ItemHot.heatUp(out, done ? 1D : (h1 + h2) / 2D);
				event.setOutput(out);
				event.setCost(event.getLeft().getCount());
			}
		}
	}
	
	@SubscribeEvent
	public void craftingRegister(RegistryEvent.Register<IRecipe> e){
		long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		System.out.println("Memory usage before: " + mem);
		CraftingManager.hack = e;
		CraftingManager.init();
		CraftingManager.hack = null;
		mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		System.out.println("Memory usage after: " + mem);
	}

	@SubscribeEvent
	public void onRecipeRegister(RegistryEvent.Register<IRecipe> evt) {
		IRecipe[] recipes = new IRecipe[12];
		IRecipe recipe = null;
	}
}
