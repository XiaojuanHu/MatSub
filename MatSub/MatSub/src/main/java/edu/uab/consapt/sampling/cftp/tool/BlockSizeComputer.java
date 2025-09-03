package edu.uab.consapt.sampling.cftp.tool;

/**
 * Interface for block size computers for the CFTP algorithm. Implement this
 * interface if you want to define a new block size scheme
 * 
 * @author Sandy Moens
 */
public interface BlockSizeComputer {

	public int getBlockSizeIncrease(int t);
}
