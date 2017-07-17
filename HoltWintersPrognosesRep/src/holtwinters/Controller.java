package holtwinters;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.stream.Stream;


public class Controller implements Initializable {
	@FXML private LineChart<Integer, Double> plot;

	@FXML private TextField kCoef;
	@FXML private TextField bCoef;
	@FXML private TextField qCoef;

	@FXML private Label prognosesError;


	HoltWintersCalculator calculator;
	private int period;
	private int offset;
	private int step;
	private int n;
	private XYChart.Series<Integer, Double> series;
	private XYChart.Series<Integer, Double> prognoses;


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Properties data = readData();
		double[] y = convertStringToDoubleArray(data.getProperty("data"));
		n = y.length;
		period = Integer.parseInt(data.getProperty("period"));
		offset = Integer.parseInt(data.getProperty("offset"));
		step = Integer.parseInt(data.getProperty("step"));
		double S0 = Double.parseDouble(data.getProperty("S0"));

		plot.setTitle(data.getProperty("plotTitle"));
		plot.getXAxis().setLabel(data.getProperty("xTitle"));
		plot.getYAxis().setLabel(data.getProperty("yTitle"));

		calculator = new HoltWintersCalculator(y, period, offset, step, S0);
		calculator.recalculate(0.5, 0.9, 0.5);

		XYChart.Series<Integer, Double> source = new XYChart.Series<>();
		source.setName("Исходные данные");
		for (int t = 0; t < n; t++) {
			source.getData().add(new XYChart.Data<>(t * step + offset, y[t]));
		}

		series = new XYChart.Series<>();
		series.setName("Экспоненциально-сглаженный ряд");

		prognoses = new XYChart.Series<>();
		prognoses.setName(String.format("Модель Хольта-Винтерса"));


		plot.getData().addAll(source, series, prognoses);


		for (int t = 0; t < n; t++) {
			XYChart.Data<Integer, Double> d = source.getData().get(t);
			setToolTip(d.getNode(), String.format(
					"x = %d; y = %,.6f",
					d.getXValue(), d.getYValue()));
		}

		rebuild();
	}

	private void rebuild() {
		System.out.println(calculator);

		series.getData().clear();
		prognoses.getData().clear();

		for (int t = 0; t < n + period; t++) {
			prognoses.getData().add(new XYChart.Data<>(t * step + offset, calculator.getYp()[t]));
		}
		for (int t = 0; t < n; t++) {
			XYChart.Data<Integer, Double> d = prognoses.getData().get(t);
			setToolTip(d.getNode(), String.format(
					"x = %d; y = %,.6f%nТренд = %,.6f%nСезонность = %,.6f%nОшибка = %,.6f",
					d.getXValue(), d.getYValue(),
					calculator.getT()[t], calculator.getS()[t], calculator.getE()[t]));
		}
		for (int t = n; t < n + period; t++) {
			XYChart.Data<Integer, Double> d = prognoses.getData().get(t);
			setToolTip(d.getNode(), String.format(
					"Прогонозируемая точка%nx = %d; y = %,.6f",
					d.getXValue(), d.getYValue()));
		}

		for (int t = 0; t < n; t++) {
			series.getData().add(new XYChart.Data<>(t * step + offset, calculator.getL()[t]));
		}
		for (int t = 0; t < n; t++) {
			XYChart.Data<Integer, Double> d = series.getData().get(t);
			setToolTip(d.getNode(), String.format(
					"x = %d; y = %,.6f",
					d.getXValue(), d.getYValue()));
		}

		prognosesError.setText(String.format("Ошибка модели: %.5f", calculator.getA()));
	}

	/**
	 * Установка тултипа на ноуд, показывающегося при наведении мышью
	 * @param node
	 * @param text
	 */
	private void setToolTip(Node node, String text) {
		Tooltip tooltip = new Tooltip(text);
		node.setOnMouseEntered(e -> tooltip.show(node, e.getScreenX() + 10, e.getScreenY() + 10));
		node.setOnMouseExited(e -> tooltip.hide());
	}

	/**
	 * Показ тултипа возле ноуда
	 * @param node
	 * @param text
	 */
	private void showErrorToolTip(Node node, String text) {
		Tooltip tooltip = new Tooltip(text);
		Bounds bounds = node.localToScreen(node.getBoundsInLocal());
		tooltip.show(node, bounds.getMaxX(), bounds.getMinY());
		EventHandler<? super MouseEvent> action = node.getOnMousePressed();
		node.setOnMousePressed(event -> {
			tooltip.hide();
			if (action != null)
				action.handle(event);
		});
	}

	private Properties readData() {
		File file = new File("data.properties");

		Properties properties = new Properties();
		try (Reader reader = file.exists()
				? new FileReader(file)
				: new InputStreamReader(getClass().getResourceAsStream(file.getName())) ) {

			properties.load(reader);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return properties;
	}

	private double[] convertStringToDoubleArray(String stringArray) {
		String[] stringData = stringArray.split(",\\s*");
		double[] data = new double[stringData.length];

		for (int i = 0; i < stringData.length; i++) {
			data[i] = Double.parseDouble(stringData[i]);
		}

		return data;
	}

	public void recalculateButton(ActionEvent actionEvent) {
		double k;
		double b;
		double q;

		try {
			k = Double.valueOf(kCoef.getText());
			if (k <= 0 || k >= 1) {
				showErrorToolTip(kCoef, "Значение должно находиться между 0 и 1");
				return;
			}
		} catch (Exception exception) {
			showErrorToolTip(kCoef, "Некорректные данные");
			return;
		}

		try {
			b = Double.valueOf(bCoef.getText());
			if (k <= 0 || k >= 1) {
				showErrorToolTip(bCoef, "Значение должно находиться между 0 и 1");
				return;
			}
		} catch (Exception exception) {
			showErrorToolTip(bCoef, "Некорректные данные");
			return;
		}

		try {
			q = Double.valueOf(qCoef.getText());
			if (q <= 0 || q >= 1) {
				showErrorToolTip(qCoef, "Значение должно находиться между 0 и 1");
				return;
			}
		} catch (Exception exception) {
			showErrorToolTip(qCoef, "Некорректные данные");
			return;
		}
		calculator.recalculate(k, b, q);


		rebuild();
	}
}
