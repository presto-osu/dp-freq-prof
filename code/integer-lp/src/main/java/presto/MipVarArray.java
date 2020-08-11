package presto;

import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

/** MIP example with a variable array. */
public class MipVarArray {
  static {
    System.loadLibrary("jniortools");
  }

  static class DataModel {
    public double[][] constraintCoeffs;
    public double[] bounds;
    public double[] objCoeffs;
    public int numVars;
    public int numConstraints;

    public DataModel(int numVars) {
      this.numVars = numVars;
    }
  }

  public double solve(DataModel data) {
    return solve(data, true);
  }

  public double solve(DataModel data, boolean maximize) {
    // Create the linear solver with the CBC backend.
    MPSolver solver = new MPSolver("SimpleMipProgram", MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING);
    MPVariable[] x = new MPVariable[data.numVars];
    for (int j = 0; j < data.numVars; ++j) {
      // x >= 0
      x[j] = solver.makeIntVar(0.0, Double.POSITIVE_INFINITY, "");
    }
    // System.out.println("Number of variables = " + solver.numVariables());

    // Create the constraints.
    for (int i = 0; i < data.numConstraints; ++i) {
      MPConstraint constraint = solver.makeConstraint(Double.NEGATIVE_INFINITY, data.bounds[i], "");
      for (int j = 0; j < data.numVars; ++j) {
        constraint.setCoefficient(x[j], data.constraintCoeffs[i][j]);
      }
    }
    // System.out.println("Number of constraints = " + solver.numConstraints());

    MPObjective objective = solver.objective();
    for (int j = 0; j < data.numVars; ++j) {
      objective.setCoefficient(x[j], data.objCoeffs[j]);
    }

    if (maximize)
      objective.setMaximization();
    else
      objective.setMinimization();

    final MPSolver.ResultStatus resultStatus = solver.solve();

    // Check that the problem has an optimal solution.
    if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
      // System.out.println();
      // System.out.println("Objective value = " + objective.value());
      // double sum = 0;
      // for (int j = 0; j < data.numVars / 2; ++j) {
      //   sum += x[j].solutionValue();
      //   // System.out.println("x[" + j + "] = " + x[j].solutionValue());
      // }
      // System.out.println("Sum x = " + sum);
      // System.out.println("Problem solved in " + solver.wallTime() + " milliseconds");
      // System.out.println("Problem solved in " + solver.iterations() + " iterations");
      // System.out.println("Problem solved in " + solver.nodes() + " branch-and-bound nodes");
      return objective.value();
    }
    System.err.println("The problem does not have an optimal solution.");
    return -1;
  }

  public static void main(String[] args) throws Exception {
    final DataModel data = new DataModel(5);

    data.constraintCoeffs = new double[][] { { 1, -1, 0, 0, 0 }, { 0, 1, -1, 0, 0 }, { 0, 0, 1, -1, 0 },
        { 0, -1, 0, 0, 0 }, };
    data.bounds = new double[] { 0, 0, 0, -1 };
    data.objCoeffs = new double[] { 7, 8, 2, 9, 6 };
    data.numConstraints = 4;

    MipVarArray.v().solve(data);
  }

  private MipVarArray() {
  }

  private static MipVarArray instance;

  public static MipVarArray v() {
    if (instance == null)
      instance = new MipVarArray();
    return instance;
  }
}
