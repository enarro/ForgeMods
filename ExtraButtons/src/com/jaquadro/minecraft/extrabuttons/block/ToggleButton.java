package com.jaquadro.minecraft.extrabuttons.block;

import com.jaquadro.minecraft.extrabuttons.ExtraButtons;
import com.jaquadro.minecraft.extrabuttons.tileentity.TileEntityButton;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.minecraftforge.common.ForgeDirection.*;

public class ToggleButton extends BlockContainer
{
    @SideOnly(Side.CLIENT)
    private Icon[] iconArrayOn;

    @SideOnly(Side.CLIENT)
    private Icon[] iconArrayOff;

    public ToggleButton (int id)
    {
        super(id, Material.circuits);
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.tabRedstone);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool (World par1World, int par2, int par3, int par4)
    {
        return null;
    }

    @Override
    public int tickRate (World world)
    {
        return 5;
    }

    @Override
    public boolean isOpaqueCube ()
    {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock ()
    {
        return false;
    }

    @Override
    public boolean canPlaceBlockOnSide (World world, int x, int y, int z, int side)
    {
        ForgeDirection dir = ForgeDirection.getOrientation(side);
        return (dir == NORTH && world.isBlockSolidOnSide(x, y, z + 1, NORTH)) ||
                (dir == SOUTH && world.isBlockSolidOnSide(x, y, z - 1, SOUTH)) ||
                (dir == WEST && world.isBlockSolidOnSide(x + 1, y, z, WEST)) ||
                (dir == EAST && world.isBlockSolidOnSide(x - 1, y, z, EAST));
    }

    @Override
    public boolean canPlaceBlockAt (World world, int x, int y, int z)
    {
        return (world.isBlockSolidOnSide(x - 1, y, z, EAST)) ||
                (world.isBlockSolidOnSide(x + 1, y, z, WEST)) ||
                (world.isBlockSolidOnSide(x, y, z - 1, SOUTH)) ||
                (world.isBlockSolidOnSide(x, y, z + 1, NORTH));
    }

    @Override
    public boolean hasTileEntity (int data)
    {
        return true;
    }

    @Override
    public void onNeighborBlockChange (World world, int x, int y, int z, int neighborId)
    {
        TileEntityButton te = (TileEntityButton) world.getBlockTileEntity(x, y, z);
        int dir = (te != null)
                ? te.getDirection() : 0;

        boolean invalid = false;

        if (!world.isBlockSolidOnSide(x - 1, y, z, EAST) && dir == 1) {
            invalid = true;
        }

        if (!world.isBlockSolidOnSide(x + 1, y, z, WEST) && dir == 2) {
            invalid = true;
        }

        if (!world.isBlockSolidOnSide(x, y, z - 1, SOUTH) && dir == 3) {
            invalid = true;
        }

        if (!world.isBlockSolidOnSide(x, y, z + 1, NORTH) && dir == 4) {
            invalid = true;
        }

        if (invalid) {
            this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
            world.setBlockToAir(x, y, z);
        }
    }

    @Override
    public void setBlockBoundsBasedOnState (IBlockAccess blockAccess, int x, int y, int z)
    {
        TileEntityButton te = (TileEntityButton) blockAccess.getBlockTileEntity(x, y, z);
        if (te != null)
            this.setBlockBoundsByTileEntity(te);
    }

    private void setBlockBoundsByTileEntity (TileEntityButton te)
    {
        int dir = te.getDirection();
        boolean isLatched = te.isDepressed();

        float var4 = 0.375F;
        float var5 = 0.625F;
        float var6 = 0.1875F;
        float depth = 0.125F;

        if (isLatched) {
            depth = 0.0625F;
        }

        if (dir == 1) {
            this.setBlockBounds(0.0F, var4, 0.5F - var6, depth, var5, 0.5F + var6);
        }
        else if (dir == 2) {
            this.setBlockBounds(1.0F - depth, var4, 0.5F - var6, 1.0F, var5, 0.5F + var6);
        }
        else if (dir == 3) {
            this.setBlockBounds(0.5F - var6, var4, 0.0F, 0.5F + var6, var5, depth);
        }
        else if (dir == 4) {
            this.setBlockBounds(0.5F - var6, var4, 1.0F - depth, 0.5F + var6, var5, 1.0F);
        }
    }

    @Override
    public boolean onBlockActivated (World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
    {
        TileEntityButton te = (TileEntityButton) world.getBlockTileEntity(x, y, z);
        if (te == null)
            return false;

        if (te.isDepressed())
            return true;

        int dir = te.getDirection();

        te.setIsDepressed(true);
        te.setIsLatched(!te.isLatched());

        world.markBlockRangeForRenderUpdate(x, y, z, x, y, z);
        world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, "random.click", 0.3F, 0.6F);
        this.updateNeighbors(world, x, y, z, dir);
        world.scheduleBlockUpdate(x, y, z, this.blockID, this.tickRate(world));

        return true;
    }

    @Override
    public void breakBlock (World world, int x, int y, int z, int side, int data)
    {
        TileEntityButton te = (TileEntityButton) world.getBlockTileEntity(x, y, z);
        if (te != null && te.isLatched())
            this.updateNeighbors(world, x, y, z, te.getDirection());

        super.breakBlock(world, x, y, z, side, data);
    }

    @Override
    public int isProvidingWeakPower (IBlockAccess blockAccess, int x, int y, int z, int side)
    {
        TileEntityButton te = (TileEntityButton) blockAccess.getBlockTileEntity(x, y, z);

        return (te != null && te.isLatched()) ? 15 : 0;
    }

    @Override
    public int isProvidingStrongPower (IBlockAccess blockAccess, int x, int y, int z, int side)
    {
        TileEntityButton te = (TileEntityButton) blockAccess.getBlockTileEntity(x, y, z);
        if (te == null || !te.isLatched())
            return 0;

        int dir = te.getDirection();
        return (dir == 5 && side == 1 ? 15 : (dir == 4 && side == 2 ? 15 : (dir == 3 && side == 3 ? 15 : (dir == 2 && side == 4 ? 15 : (dir == 1 && side == 5 ? 15 : 0)))));
    }

    @Override
    public boolean canProvidePower ()
    {
        return true;
    }

    @Override
    public void updateTick (World world, int x, int y, int z, Random rand)
    {
        TileEntityButton te = (TileEntityButton) world.getBlockTileEntity(x, y, z);

        if (te != null && te.isDepressed()) {
            if (!world.isRemote) {
                te.setIsDepressed(false);

                world.markBlockForUpdate(x, y, z);
                world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, "random.click", 0.3F, 0.5F);
            }
        }
    }

    @Override
    public void setBlockBoundsForItemRender ()
    {
        float hx = 0.1875F;
        float hy = 0.125F;
        float hz = 0.125F;
        this.setBlockBounds(0.5F - hx, 0.5F - hy, 0.5F - hz, 0.5F + hx, 0.5F + hy, 0.5F + hz);
    }

    private void updateNeighbors (World world, int x, int y, int z, int dir)
    {
        world.notifyBlocksOfNeighborChange(x, y, z, this.blockID);

        if (dir == 1) {
            world.notifyBlocksOfNeighborChange(x - 1, y, z, this.blockID);
        }
        else if (dir == 2) {
            world.notifyBlocksOfNeighborChange(x + 1, y, z, this.blockID);
        }
        else if (dir == 3) {
            world.notifyBlocksOfNeighborChange(x, y, z - 1, this.blockID);
        }
        else if (dir == 4) {
            world.notifyBlocksOfNeighborChange(x, y, z + 1, this.blockID);
        }
        else {
            world.notifyBlocksOfNeighborChange(x, y - 1, z, this.blockID);
        }
    }

    @Override
    public TileEntity createNewTileEntity (World world)
    {
        return new TileEntityButton();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Icon getBlockTexture (IBlockAccess world, int x, int y, int z, int side)
    {
        int data = world.getBlockMetadata(x, y, z);
        TileEntity baseTE = world.getBlockTileEntity(x, y, z);
        if (baseTE != null && baseTE.getClass().isAssignableFrom(TileEntityButton.class)) {
            TileEntityButton te = (TileEntityButton) baseTE;
            if (te.isLatched())
                return iconArrayOn[data];
        }

        return iconArrayOff[data];
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Icon getIcon (int side, int data)
    {
        return iconArrayOff[data];
    }

    @Override
    public ArrayList<ItemStack> getBlockDropped (World world, int x, int y, int z, int metadata, int fortune)
    {
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();

        int count = quantityDropped(metadata, fortune, world.rand);
        for (int i = 0; i < count; i++) {
            int id = idDropped(metadata, world.rand, fortune);
            if (id > 0)
                ret.add(new ItemStack(id, 1, metadata));
        }
        return ret;
    }

    @Override
    public int damageDropped (int data)
    {
        return data;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks (int blockId, CreativeTabs creativeTabs, List blockList)
    {
        for (int i = 0; i < 16; ++i)
            blockList.add(new ItemStack(blockId, 1, i));
    }

    public static int getBlockFromDye (int data)
    {
        return ~data & 15;
    }

    public static int getDyeFromBlock (int data)
    {
        return ~data & 15;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IconRegister iconRegister) {
        iconArrayOff = new Icon[16];
        iconArrayOn = new Icon[16];

        for (int i = 0; i < 16; i++) {
            iconArrayOff[i] = iconRegister.registerIcon(ExtraButtons.MOD_ID + ":illum_button_off_" + i);
            iconArrayOn[i] = iconRegister.registerIcon(ExtraButtons.MOD_ID + ":illum_button_on_" + i);
        }
    }
}
