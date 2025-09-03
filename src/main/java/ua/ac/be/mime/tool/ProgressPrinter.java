package ua.ac.be.mime.tool;

public interface ProgressPrinter {

	public static class NoProgressPrinter implements ProgressPrinter {

		public NoProgressPrinter() {
		}

		@Override
		public void restart() {
		}

		@Override
		public void addStepsAndPrint(int i) {
		}

		@Override
		public void printCompletion() {

		}
	}

	public class StepsProgressPrinter implements ProgressPrinter {

		private final int beginUsingHundreds = 50;
		private final long hundredPercent;
		private long dotStep;
		private long curStep;
		private long tens;

		public StepsProgressPrinter(long hundredPercent) {
			this.hundredPercent = hundredPercent * 100;

			computeIntervals();
			restart();
		}

		@Override
		public void restart() {
			this.curStep = 0;
			this.tens = 0;
		}

		private void computeIntervals() {
			this.dotStep = this.hundredPercent >= this.beginUsingHundreds * 100 ? this.hundredPercent / 100
					: (this.hundredPercent >= 1000 ? this.hundredPercent / 10
							: 10);
		}

		@Override
		public void addStepsAndPrint(int step) {
			this.curStep += step * 100;
			while (this.curStep >= this.dotStep) {
				this.curStep -= this.dotStep;
				this.tens++;
				System.out.print(".");
				System.out.flush();
				if (this.tens == 10) {
					this.tens = 0;
					System.out.println("");
				}
			}
		}

		@Override
		public void printCompletion() {
			if (this.dotStep == -1 || this.tens != 0) {
				System.out.println("\nProgress completed!");
			} else {
				System.out.println("Progress completed!");
			}
		}
	}

	public class AllStepsProgressPrinter extends StepsProgressPrinter {

		private int curStep;

		public AllStepsProgressPrinter() {
			super(0);
			restart();
		}

		@Override
		public void restart() {
			this.curStep = 0;
		}

		@Override
		public void addStepsAndPrint(int step) {
			this.curStep += step;
			DebugPrinter.println(this.curStep + "");
		}

		@Override
		public void printCompletion() {
			DebugPrinter.println("Progress completed!");
		}
	}

	public abstract void restart();

	public abstract void addStepsAndPrint(int step);

	public abstract void printCompletion();

}