package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hbm.forgefluid.FFUtils;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.IConsumer;
import com.hbm.interfaces.IReactor;
import com.hbm.interfaces.ISource;
import com.hbm.interfaces.ITankPacketAcceptor;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFWatzCore;
import com.hbm.lib.Library;
import com.hbm.packet.AuxElectricityPacket;
import com.hbm.packet.FluidTankPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.world.FWatz;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.items.ItemStackHandler;

public class TileEntityFWatzCore extends TileEntity implements ITickable, IReactor, ISource, IFluidHandler, ITankPacketAcceptor {

	public long power;
	public final static long maxPower = 1000000000000L;
	public boolean cooldown = false;

	public FluidTank tanks[];
	public Fluid[] tankTypes;
	public boolean needsUpdate;

	Random rand = new Random();

	public ItemStackHandler inventory;
	public int age = 0;
	public List<IConsumer> list = new ArrayList<IConsumer>();

	private String customName;

	public TileEntityFWatzCore() {
		inventory = new ItemStackHandler(7) {
			@Override
			protected void onContentsChanged(int slot) {
				markDirty();
				super.onContentsChanged(slot);
			}
		};
		tanks = new FluidTank[3];
		tankTypes = new Fluid[3];
		tanks[0] = new FluidTank(128000);
		tankTypes[0] = ModForgeFluids.coolant;
		tanks[1] = new FluidTank(64000);
		tankTypes[1] = ModForgeFluids.amat;
		tanks[2] = new FluidTank(64000);
		tankTypes[2] = ModForgeFluids.aschrab;
		needsUpdate = false;
	}

	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : "container.fusionaryWatzPlant";
	}

	public boolean hasCustomInventoryName() {
		return this.customName != null && this.customName.length() > 0;
	}

	public void setCustomName(String name) {
		this.customName = name;
	}

	public boolean isUseableByPlayer(EntityPlayer player) {
		if(world.getTileEntity(pos) != this) {
			return false;
		} else {
			return true;
		}
	}

	public int getSingularityType(){
		Item item = inventory.getStackInSlot(2).getItem();
		if(item instanceof ItemFWatzCore){
			return ((ItemFWatzCore)item).type;
		}
		return 0;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		power = compound.getLong("power");
		tankTypes[0] = ModForgeFluids.coolant;
		tankTypes[1] = ModForgeFluids.amat;
		tankTypes[2] = ModForgeFluids.aschrab;
		if(compound.hasKey("tanks"))
			FFUtils.deserializeTankArray(compound.getTagList("tanks", 10), tanks);
		if(compound.hasKey("inventory"))
			inventory.deserializeNBT(compound.getCompoundTag("inventory"));
		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setLong("power", power);
		compound.setTag("inventory", inventory.serializeNBT());
		compound.setTag("tanks", FFUtils.serializeTankArray(tanks));
		return super.writeToNBT(compound);
	}

	@Override
	public void update() {
		if(!world.isRemote && this.isStructureValid(this.world)) {

			age++;
			if(age >= 20) {
				age = 0;
			}

			if(age == 9 || age == 19)
				ffgeuaInit();

			if(inventory.getStackInSlot(2).getItem() == ModItems.meteorite_sword_baleful && this.isRunning()){
				inventory.setStackInSlot(2, new ItemStack(ModItems.meteorite_sword_warped));
			}

			if(hasFuse() && inventory.getStackInSlot(2).getItem() instanceof ItemFWatzCore) {
				ItemFWatzCore itemCore = (ItemFWatzCore)inventory.getStackInSlot(2).getItem();
				if(cooldown) {
					
					tanks[0].fill(new FluidStack(tankTypes[0], itemCore.coolantRefill), true);

					if(tanks[0].getFluidAmount() >= tanks[0].getCapacity()) {
						cooldown = false;
					}

				} else {

					if(tanks[1].getFluidAmount() > itemCore.amatDrain && tanks[2].getFluidAmount() > itemCore.aschrabDrain) {
						tanks[0].drain(itemCore.coolantDrain, true);
						tanks[1].drain(itemCore.amatDrain, true);
						tanks[2].drain(itemCore.aschrabDrain, true);
						needsUpdate = true;
						power += itemCore.powerOutput;
					}

					if(power > maxPower)
						power = maxPower;

					if(tanks[0].getFluidAmount() <= 0) {
						cooldown = true;
					}
				}
			}

			if(power > maxPower)
				power = maxPower;

			power = Library.chargeItemsFromTE(inventory, 0, power, maxPower);

			if(this.inputValidForTank(1, 3))
				if(FFUtils.fillFromFluidContainer(inventory, tanks[1], 3, 5))
					needsUpdate = true;
			if(this.inputValidForTank(2, 4))
				if(FFUtils.fillFromFluidContainer(inventory, tanks[2], 4, 6))
					needsUpdate = true;
			if(needsUpdate) {
				needsUpdate = false;
			}

			if(this.isRunning() && (tanks[1].getFluidAmount() <= 0 || tanks[2].getFluidAmount() <= 0 || !hasFuse() || !(inventory.getStackInSlot(2).getItem() instanceof ItemFWatzCore)) || cooldown || !this.isStructureValid(world))
				this.emptyPlasma();

			if(!this.isRunning() && tanks[1].getFluidAmount() >= 100 && tanks[2].getFluidAmount() >= 100 && hasFuse() && inventory.getStackInSlot(2).getItem() instanceof ItemFWatzCore && !cooldown && this.isStructureValid(world))
				this.fillPlasma();

			PacketDispatcher.wrapper.sendToAllAround(new FluidTankPacket(pos, new FluidTank[] { tanks[0], tanks[1], tanks[2] }), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 25));
			PacketDispatcher.wrapper.sendToAllAround(new AuxElectricityPacket(pos, power), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 25));

		}

	}

	@Override
	public boolean isStructureValid(World world) {
		return FWatz.checkHull(world, pos);
	}

	@Override
	public boolean isCoatingValid(World world) {
		return true;
	}

	@Override
	public boolean hasFuse() {
		return inventory.getStackInSlot(1).getItem() == ModItems.fuse || inventory.getStackInSlot(1).getItem() == ModItems.screwdriver;
	}

	@Override
	public int getWaterScaled(int i) {
		return 0;
	}

	@Override
	public int getCoolantScaled(int i) {
		return 0;
	}

	@Override
	public long getPowerScaled(long i) {
		return (power / 100 * i) / (maxPower / 100);
	}

	@Override
	public int getHeatScaled(int i) {
		return 0;
	}

	public void fillPlasma() {
		if(!this.world.isRemote)
			FWatz.fillPlasma(world, pos);
	}

	public void emptyPlasma() {
		if(!this.world.isRemote)
			FWatz.emptyPlasma(world, pos);
	}

	public boolean isRunning() {
		return FWatz.getPlasma(world, pos) && this.isStructureValid(world);
	}

	protected boolean inputValidForTank(int tank, int slot) {
		if(tanks[tank] != null) {
			if(inventory.getStackInSlot(slot).getItem() == ModItems.fluid_barrel_infinite || isValidFluidForTank(tank, FluidUtil.getFluidContained(inventory.getStackInSlot(slot)))) {
				return true;
			}
		}
		return false;
	}

	private boolean isValidFluidForTank(int tank, FluidStack stack) {
		if(stack == null || tanks[tank] == null)
			return false;
		return stack.getFluid() == tankTypes[tank];
	}
	
	@Override
	public void ffgeua(BlockPos pos, boolean newTact) {

		Library.ffgeua(new BlockPos.MutableBlockPos(pos), newTact, this, world);
	}

	@Override
	public void ffgeuaInit() {
		ffgeua(pos.add(7, -1, 0), getTact());
		ffgeua(pos.add(-7, -1, 0), getTact());
		ffgeua(pos.add(0, -1, 7), getTact());
		ffgeua(pos.add(0, -1, -7), getTact());
	}

	@Override
	public boolean getTact() {
		if(age >= 0 && age < 10) {
			return true;
		}

		return false;
	}

	@Override
	public long getSPower() {
		return power;
	}

	@Override
	public void setSPower(long i) {
		this.power = i;
	}

	@Override
	public List<IConsumer> getList() {
		return list;
	}

	@Override
	public void clearList() {
		this.list.clear();
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return new IFluidTankProperties[]{tanks[0].getTankProperties()[0], tanks[1].getTankProperties()[0], tanks[2].getTankProperties()[0]};
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if(resource == null) {
			return 0;
		} else if(resource.getFluid() == tankTypes[0]) {
			needsUpdate = true;
			return tanks[0].fill(resource, doFill);
		} else if(resource.getFluid() == tankTypes[1]) {
			needsUpdate = true;
			return tanks[1].fill(resource, doFill);
		} else if(resource.getFluid() == tankTypes[2]) {
			needsUpdate = true;
			return tanks[2].fill(resource, doFill);
		} else {
			return 0;
		}
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		return null;
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public void recievePacket(NBTTagCompound[] tags) {
		if(tags.length != 3) {
			return;
		} else {
			tanks[0].readFromNBT(tags[0]);
			tanks[1].readFromNBT(tags[1]);
			tanks[2].readFromNBT(tags[2]);
		}
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ? CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this) : super.getCapability(capability, facing);
	}

}
