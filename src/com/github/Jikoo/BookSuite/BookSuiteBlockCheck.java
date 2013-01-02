package com.github.Jikoo.BookSuite;

import java.util.Arrays;

import org.bukkit.block.Block;

public class BookSuiteBlockCheck {
	
	
	
	
	
	
	
	/**
	 * tests if a given block is an inverted stair block
	 * 
	 * @param b the block to be tested
	 * @return whether the block is an inverted stair
	 */
	public static boolean isInvertedStairs(Block b){
		int[] acceptable = {53, 67, 108, 109, 114, 128, 134, 135, 136};
		for(int i: acceptable)
			if (i==b.getTypeId())return b.getData()>3;
		return false;
	}
}
