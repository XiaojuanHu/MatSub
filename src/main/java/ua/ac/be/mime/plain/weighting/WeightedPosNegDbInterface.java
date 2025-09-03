package ua.ac.be.mime.plain.weighting;

public interface WeightedPosNegDbInterface extends PosNegDbInterface {

	public double getTransactionWeight(int tid);

	public double getPosTransactionWeight(int tid);

	public double getNegTransactionWeight(int tid);
}
