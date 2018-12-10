package GUI;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import java.util.Random;

/** Creates a simple real-time chart */
public class SimpleRealTime {
    static Random random = new Random();
    static int Iter = 1;
    double[][] dataSet;
    final XYChart chart;
    final SwingWrapper<XYChart> sw;

    public SimpleRealTime(){

        dataSet = initData();
        chart =
                QuickChart.getChart(
                        "Simple XChart Real-time Demo", "Radians", "Sine", "sine", dataSet[0], dataSet[1]);
        sw = new SwingWrapper<XYChart>(chart);
        sw.displayChart();

    }

    public void UpdateChart(int liczbaWkolejkach){
        dataSet = UpdateDataSet(dataSet, liczbaWkolejkach);
        chart.updateXYSeries("sine", dataSet[1], dataSet[0], null);
        sw.repaintChart();
    }

    private double[][] UpdateDataSet(double[][] dataSet, double liczbaWkolejkach ) {
        double[] part1 = new double[100];
        for(int i=1;i<dataSet[0].length; i++){
            part1[i-1] = dataSet[0][i];
        }
        part1[99]=liczbaWkolejkach;

        double[] part2 = new double[100];
        for(int i=1;i<dataSet[1].length; i++){
            part2[i-1] = dataSet[1][i];
        }
        part2[99]=++Iter;

        System.out.println();
        System.out.println("Part1");
        for(int i=0; i< part1.length;i++){
            System.out.print(part1[i] + " ");
        }
        System.out.println();
        System.out.println("Part2");
        for(int i=0; i< part2.length;i++){
            System.out.print(part2[i] + " ");
        }

        return new double[][]{part1, part2};
    }

    private double[][] initData(){
        double[] xData = new double[100];
        double[] yData = new double[100];

        for(int i=0;i<xData.length;i++)
            xData[i]=0;
        for(int i=0;i<xData.length;i++)
            yData[i]=0;

        return new double[][]{xData, yData};
    }
}