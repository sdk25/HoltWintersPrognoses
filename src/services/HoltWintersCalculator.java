package services;

public class HoltWintersCalculator {

	//исходные данные
	private double[] y;

	//кол-во исходных данных
	private int n;

	//период
	private int p;

	//начало отсчета по X
	private int offset;
	//шаг у данных по X
	private int step;

	//экспоненциально-сглаженный ряд
	private double[] L;


	//значение тренда
	private double[] T;

	//сезонность
	private double[] S;
	//коэффицинет сезонности 1 периода
	private double S0;


	//ошибки модели
	private double[] E;

	//прогноз по модели Хольта-Винтерса
	private double[] yp;

	//точность прогноза %
	private double A;

	public HoltWintersCalculator(double[] y, int period, int offset, int step, double S0) {
		this.y = y;
		this.offset = offset;
		this.step = step;

		this.n = y.length;
		this.p = period;
		this.S0 = S0;

		this.L = new double[n];
		this.T = new double[n];
		this.S = new double[n];
		this.E = new double[n];
		this.yp = new double[n + p];

	}

	/**
	 *
	 * @param k коэффициент сглаживания ряда
	 * @param b коэффициент сглаживания тренда
	 * @param q коэффициент сглаживания сезонности
	 */
	public void recalculate(double k, double b, double q) {
		//начальные значения
		L[0] = y[0];
		T[0] = 0;
		S[0] = S0;
		yp[0] = y[0];
		E[0] = 0;

		//первый период
		for (int t = 1; t < p; t++) {
			L[t] = k * y[t] / S0 + (1 - k) * (L[t - 1] + T[t - 1]);
			T[t] = b * (L[t] - L[t - 1]) + (1 - b) * T[t - 1];
			S[t] = S0;
			yp[t] = L[t - 1] + T[t - 1];
			E[t] = y[t] - yp[t];
		}

		//остальные периоды
		for (int t = p; t < n; t++) {
			L[t] = k * y[t] / S[t - p] + (1 - k) * (L[t - 1] + T[t - 1]);
			T[t] = b * (L[t] - L[t - 1]) + (1 - b) * T[t - 1];
			S[t] = q * y[t] / L[t] + (1 - q) * S[t - p];
			yp[t] = (L[t - 1] + T[t - 1]) * S[t - p];
			E[t] = y[t] - yp[t];
		}

		//прогноз
		for (int t = n, p_i = 1; t < n + p; t++, p_i++) {
			yp[t] = (L[n - 1] + T[n - 1] * p_i) * S[t - p];
		}

		//расчет ошибки (%)
		A = 0;
		for (int t = 0; t < n; t++) {
			A += ((E[t] * E[t]) / (y[t] * y[t])) / n;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();


		sb.append("┌---┬---┬---------------┬---------------┬---------------┬---------------┬---------------┬---------------┐\n");
		sb.append(String.format("│%3s│%3s│%15s│%15s│%15s│%15s│%15s│%15s│%n", "p", "t", "y[t]", "L[t]", "T[t]", "S[t]", "yp[t]", "E[t]"));
		sb.append("├---┼---┼---------------┼---------------┼---------------┼---------------┼---------------┼---------------┤\n");


		for (int t = 0; t < n; t++) {
			sb.append(String.format("│%3d│%3d│%15.3f│%15.3f│%15.3f│%15.3f│%15.3f│%15.3f│%n", (t / p) + 1, (t % p) * step + offset, y[t], L[t], T[t], S[t], yp[t], E[t]));
			if (((t + 1) % p) * step + offset == offset)
				sb.append("├---┼---┼---------------┼---------------┼---------------┼---------------┼---------------┼---------------┤\n");
		}
		sb.append("├---┴---┴---------------┴---------┼-----┴---------------┴---------------┴---------------┴---------------┘\n");


		sb.append(String.format("│ прогноз (ошибка модели %3.5f) │%n", A));
		sb.append("├---┬---┬---------------┬---------┘\n");
		for (int t = n; t < n + p; t++) {
			sb.append(String.format("│%3d│%3d│%15.3f│%n", (t / p) + 1, (t % p) * step + offset, yp[t]));
			if (((t + 1) % p) * step + offset == offset)
				sb.append("├---┼---┼---------------┤\n");
		}
		sb.append("└---┴---┴---------------┘\n");

		return sb.toString();
	}

	public double[] getT() {
		return T;
	}

	public double[] getS() {
		return S;
	}

	public double[] getE() {
		return E;
	}

	public double[] getL() {
		return L;
	}

	public double[] getYp() {
		return yp;
	}

	public double getA() {
		return A;
	}
}
