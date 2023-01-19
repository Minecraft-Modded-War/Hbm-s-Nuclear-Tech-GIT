package com.hbm.config;

import com.hbm.items.ModItems;
import com.hbm.core.HbmPotion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;

public class VersatileConfig {

	public static Item getTransmutatorItem() {

		return ModItems.ingot_schraranium;
	}

	public static int getSchrabOreChance() {

		return 100;
	}
	
	public static void applyPotionSickness(EntityLivingBase entity, int duration) {
		
		if(PotionConfig.potionSickness == 0)
			return;
		
		if(PotionConfig.potionSickness == 2)
			duration *= 12;
		
		entity.addPotionEffect(new PotionEffect(HbmPotion.potionsickness, duration * 20));
	}

	public static boolean hasPotionSickness(EntityLivingBase entity) {
		return entity.isPotionActive(HbmPotion.potionsickness);
	}

	static int minute = 60 * 20;
	static int hour = 60 * minute;
	
	public static int getLongDecayChance() {
		return GeneralConfig.enable528 ? 15 * hour : 3 * hour;
	}

	public static int getShortDecayChance() {
		return GeneralConfig.enable528 ? 3 * hour : 15 * minute;
	}
}
