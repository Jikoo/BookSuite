/*******************************************************************************
 * Copyright (c) 2013 Ted Meyer
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Adam Gunn - plugin surrounding libraries
 ******************************************************************************/

package com.github.Jikoo.BookSuite.Library;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Library {

	private List<Voxel> shelves;

	public boolean generateLibrary(Block b, Player p)
	{
		if (! b.getType().equals(Material.BOOKSHELF))
		{
			return false;
		}
		generateLibrary(Voxel.getVoxelFromBlock(b), p);
		return true;
	}

	private void generateLibrary(Voxel v, Player p)
	{
		Queue<Voxel> queue = new LinkedList<Voxel>();
		Map<Voxel, Boolean> map = new HashMap<Voxel, Boolean>();
		queue.add(v);


		while(queue.size() > 0)
		{
			Voxel v1 = queue.remove();
			Voxel[] surrounding = v1.surroundings();
			for(Voxel test : surrounding)
			{
				if (Voxel.getBlockFromVoxel(test, p).getType().equals(Material.BOOKSHELF))
				{
					if (! Boolean.TRUE.equals(map.get(test)))
					{
						map.put(test, true);
						queue.add(test);
						shelves.add(test);
					}
				}
			}
		}
	}

}
