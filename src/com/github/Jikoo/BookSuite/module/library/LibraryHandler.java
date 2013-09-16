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

package com.github.Jikoo.BookSuite.module.library;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class LibraryHandler {
	private static int NEXT_LIBRARY_ID = 0;

	private Map<Voxel, Library> libraries = new HashMap<Voxel, Library>();

	public Library checkLibrary(Block b, Player p) {
		if (libraries.get(Voxel.getVoxelFromBlock(b)) == null) {
			List<Voxel> library = generateLibrary(b, p);
			Library l = new Library(NEXT_LIBRARY_ID++, library);

			if (library.size() > 0) {
				for (Voxel v : library) {
					libraries.put(v, l);
				}
			}
		}

		return libraries.get(Voxel.getVoxelFromBlock(b));
	}

	public List<Voxel> generateLibrary(Block b, Player p) {
		if (!b.getType().equals(Material.BOOKSHELF)) {
			return new LinkedList<Voxel>();
		}
		return generateLibrary(Voxel.getVoxelFromBlock(b), p);
	}

	private List<Voxel> generateLibrary(Voxel v, Player p) {
		List<Voxel> shelves = new LinkedList<Voxel>();
		Queue<Voxel> queue = new LinkedList<Voxel>();
		Map<Voxel, Boolean> map = new HashMap<Voxel, Boolean>();
		queue.add(v);

		while (queue.size() > 0) {
			Voxel v1 = queue.remove();
			Voxel[] surrounding = v1.surroundings();
			for (Voxel test : surrounding) {
				if (Voxel.getBlockFromVoxel(test, p).getType()
						.equals(Material.BOOKSHELF)) {
					if (!Boolean.TRUE.equals(map.get(test))) {
						map.put(test, true);
						queue.add(test);
						shelves.add(test);
					}
				}
			}
		}

		return shelves;
	}
}
