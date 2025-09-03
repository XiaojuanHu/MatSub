package edu.uab.consapt.sampling.cftp.tool;

public class DoublingBlockSizeComputer implements BlockSizeComputer {

	@Override
	public int getBlockSizeIncrease(int t) {
		if(t == 0) {
			return 1;
		}
		return t;
	}
}
