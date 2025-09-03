package de.unibonn.realkd.knowledgemodeling.learning;

import java.util.List;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import de.unibonn.realkd.knowledgemodeling.constraints.MaxEntConstraint;
import weka.core.Optimization;

/**
 * @author bkang
 */
public class OptimizationFunction extends Optimization {

    private List<MaxEntConstraint> constraints;

    private ConnectedComponent connectedComponent;

    public OptimizationFunction(ConnectedComponent connectedComponent) {

        this.connectedComponent = connectedComponent;

        this.constraints = connectedComponent.getConstraints();
    }
    @Override
    public double objectiveFunction(double[] doubles) throws Exception {
        setEvaluatePosition(doubles);

        double sum = 0.;

        sum += Math.log(connectedComponent.getPartitionFunctionValue());

        for (MaxEntConstraint constraint : constraints) {
            sum -= constraint.getMeasurement() * constraint.getMultiplier();
        }
        return sum;
    }

    @Override
    public double[] evaluateGradient(double[] doubles) throws Exception {
        RealVector pd = new ArrayRealVector(constraints.size());

        setEvaluatePosition(doubles);

        for (MaxEntConstraint constraint : connectedComponent.getConstraints()) {
            pd.addToEntry(constraints.indexOf(constraint), connectedComponent.getSumOfMultipliersActivatedByConstraint(constraint)/connectedComponent.getPartitionFunctionValue() - constraint.getMeasurement());
        }
        return pd.toArray();
    }

    @Override
    public String getRevision() {
        return null;
    }

    private void setEvaluatePosition(double [] x) {
        for (MaxEntConstraint constraint : constraints) {
            constraint.updateMultiplier(x[constraints.indexOf(constraint)]);
        }

        connectedComponent.recomputePartitionFunctionValue();
    }
}
