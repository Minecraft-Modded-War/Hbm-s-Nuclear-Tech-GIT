package com.hbm.saveddata.satellites;

import net.minecraft.world.World;

public class SatelliteRelay extends Satellite {
	
	public SatelliteRelay() {
		this.satIface = Interfaces.NONE;
	}

	public void onOrbit(World world, double x, double y, double z) {
	}
}
