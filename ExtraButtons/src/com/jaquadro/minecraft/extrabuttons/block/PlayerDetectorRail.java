package com.jaquadro.minecraft.extrabuttons.block;

import com.jaquadro.minecraft.extrabuttons.ExtraButtons;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockRailDetector;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class PlayerDetectorRail extends BlockRailDetector
{
    @SideOnly(Side.CLIENT)
    private IIcon[] iconArray;

    public PlayerDetectorRail ()
    {
        this.setTickRandomly(true);
    }

    @Override
    public void onEntityCollidedWithBlock (World world, int x, int y, int z, Entity entity)
    {
        if (!world.isRemote) {
            int data = world.getBlockMetadata(x, y, z);

            if ((data & 8) == 0) {
                this.setStateIfMinecartInteractsWithRail(world, x, y, z, data);
            }
        }
    }

    @Override
    public void updateTick (World world, int x, int y, int z, Random rand)
    {
        if (!world.isRemote) {
            int data = world.getBlockMetadata(x, y, z);

            if ((data & 8) != 0) {
                this.setStateIfMinecartInteractsWithRail(world, x, y, z, data);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon (int side, int data)
    {
        if ((data & 8) != 0)
            return iconArray[1];
        else
            return iconArray[0];
    }

    private void setStateIfMinecartInteractsWithRail (World world, int x, int y, int z, int data)
    {
        boolean isPowerBitSet = (data & 8) != 0;
        boolean isValidTarget = false;
        float boundAdjust = 0.125F;
        List entities = world.getEntitiesWithinAABB(EntityMinecart.class, AxisAlignedBB.getBoundingBox((double) ((float) x + boundAdjust), (double) y, (double) ((float) z + boundAdjust), (double) ((float) (x + 1) - boundAdjust), (double) ((float) (y + 1) - boundAdjust), (double) ((float) (z + 1) - boundAdjust)));

        if (!entities.isEmpty()) {
            for (Object item : entities) {
                EntityMinecart minecart = (EntityMinecart) item;
                if (minecart.riddenByEntity != null)
                    isValidTarget = true;
            }
        }

        if (isValidTarget && !isPowerBitSet) {
            world.setBlockMetadataWithNotify(x, y, z, data | 8, 3);
            world.notifyBlocksOfNeighborChange(x, y, z, this);
            world.notifyBlocksOfNeighborChange(x, y - 1, z, this);
            world.markBlockRangeForRenderUpdate(x, y, z, x, y, z);
        }

        if (!isValidTarget && isPowerBitSet) {
            world.setBlockMetadataWithNotify(x, y, z, data & 7, 3);
            world.notifyBlocksOfNeighborChange(x, y, z, this);
            world.notifyBlocksOfNeighborChange(x, y - 1, z, this);
            world.markBlockRangeForRenderUpdate(x, y, z, x, y, z);
        }

        if (isValidTarget) {
            world.scheduleBlockUpdate(x, y, z, this, this.tickRate(world));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconArray = new IIcon[] {
                iconRegister.registerIcon(ExtraButtons.MOD_ID + ":player_detector_rail_off"),
                iconRegister.registerIcon(ExtraButtons.MOD_ID + ":player_detector_rail_on"),
        };
    }
}
