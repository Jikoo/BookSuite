package com.github.Jikoo.BookSuite.Library;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.github.Jikoo.BookSuite.BookSuite;

public class Voxel {
	private final int X,Y,Z;

	public Voxel(int x, int y, int z)
	{
		this.X = x;
		this.Y = y;
		this.Z = z;
	}

	public Voxel up()
	{
		return new Voxel(X, Y+1, Z);
	}

	public Voxel down()
	{
		return new Voxel(X, Y-1, Z);
	}

	public Voxel east()
	{
		return new Voxel(X+1, Y, Z);
	}

	public Voxel west()
	{
		return new Voxel(X-1, Y, Z);
	}

	public Voxel north()
	{
		return new Voxel(X, Y, Z-1);
	}

	public Voxel south()
	{
		return new Voxel(X, Y, Z+1);
	}

	public Voxel northeast()
	{
		return north().east();
	}

	public Voxel northwest()
	{
		return north().west();
	}

	public Voxel southeast()
	{
		return south().east();
	}

	public Voxel southwest()
	{
		return south().west();
	}

	public Voxel upnortheast()
	{
		return up().north().east();
	}

	public Voxel upnorthwest()
	{
		return up().north().west();
	}

	public Voxel upsoutheast()
	{
		return up().south().east();
	}

	public Voxel upsouthwest()
	{
		return up().south().west();
	}

	public Voxel downnortheast()
	{
		return down().north().east();
	}

	public Voxel downnorthwest()
	{
		return down().north().west();
	}

	public Voxel downsoutheast()
	{
		return down().south().east();
	}

	public Voxel downsouthwest()
	{
		return down().south().west();
	}

	public Voxel[] surroundings()
	{
		Voxel[] v = {up(), down(), south(), east(), north(), west(), 
					 southeast(), northeast(), northwest(), southwest(),
					 upsouthwest(), upsoutheast(), upnortheast(), upnorthwest(),
					 downnorthwest(), downnortheast(), downsoutheast(), downsouthwest()};
		return v;
	}

	public int getX()
	{
		return X;
	}

	public int getY()
	{
		return Y;
	}

	public int getZ()
	{
		return Z;
	}




	public static Voxel getVoxelFromBlock(Block b)
	{
		return new Voxel(b.getX(), b.getY(), b.getZ());
	}

	public static Block getBlockFromVoxel(Voxel v, Player around)
	{
		return around.getWorld().getBlockAt(v.X, v.Y, v.Z);
	}
}
