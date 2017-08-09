package com.measurement.www.measurement;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bean.entity.Bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.formatter.LineChartValueFormatter;
import lecho.lib.hellocharts.formatter.SimpleLineChartValueFormatter;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class LineActivity extends AppCompatActivity {
    private List<Bean> mlist ;
    private float[] result_info;
    private String[] result_date;
    private LineChartView lineChart;
    private List<PointValue> mPointValues = new ArrayList();
    private List<AxisValue> mAxisXValues = new ArrayList();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line);
        mlist = new ArrayList<>();
        mlist = (List<Bean>) getIntent().getSerializableExtra(QueryActivity.MY_DATA);
        result_info = new float[mlist.size()];
        result_date = new String[mlist.size()];
        for (int i = 0; i < mlist.size(); i++) {
            String s = mlist.get(i).getData1();
            Log.i("main------",s);
            result_info[i] =  Float.parseFloat(s);
            Log.i("main_float---",result_info[i]+"");
            result_date[i] = mlist.get(i).getTimeDetail();
        }
        lineChart = (LineChartView) findViewById(R.id.chart);
        getAxisXLables();//获取x轴的标注
        getAxisPoints();//获取坐标点
        initLineChart();//初始化
        getAxisY();
    }
    /**
     * 设置X 轴的显示
     */
    private void getAxisXLables() {
        for (int i = 0; i < result_date.length; i++) {
            mAxisXValues.add(new AxisValue(i).setLabel(result_date[i]));
        }
    }
    /**
     * 图表的每个点的显示
     */
    private void getAxisPoints() {
        for (int i = 0; i < result_info.length; i++) {
            mPointValues.add(new PointValue(i, result_info[i]));
        }
    }
    private void initLineChart(){
        Line line = new Line(mPointValues).setColor(Color.parseColor("#FFCD41"));  //折线的颜色（橙色）
        List<Line> lines = new ArrayList<>();
        line.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
        line.setCubic(true);//曲线是否平滑，即是曲线还是折线
        line.setFilled(false);//是否填充曲线的面积
        LineChartValueFormatter chartValueFormatter = new SimpleLineChartValueFormatter(2);//设置小数点
        line.setFormatter(chartValueFormatter);
        line.setHasLabels(true);//曲线的数据坐标是否加上备注
        //line.setStrokeWidth(4);//：线的粗细
        line.setPointRadius(3);//点的半径
//      line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);//是否用线显示。如果为false 则没有曲线只有点显示
        line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);
        data.setValueLabelTextSize(10);//设置标签文字字号，默认为12sp
        data.setValueLabelBackgroundEnabled(false);//设置是否显示标签的背景
        data.setValueLabelsTextColor(Color.parseColor("#FF4081"));


        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(true);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
        axisX.setTextColor(Color.GRAY);  //设置字体颜色
        axisX.setName("频率");  //表格名称
        axisX.setTextSize(10);//设置字体大小
        axisX.setMaxLabelChars(8); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length
        axisX.setValues(mAxisXValues);  //填充X轴的坐标名称
        data.setAxisXBottom(axisX); //x 轴在底部
        //data.setAxisXTop(axisX);  //x 轴在顶部
        axisX.setHasLines(true); //x 轴分割线
        // axisX.setInside(true);//设置是否将轴坐标的值显示在图表内侧。
        //axisX.setHasSeparationLine(false);//设置是否显示轴标签与图表之间的分割线

        // Y轴是根据数据的大小自动设置Y轴上限(在下面我会给出固定Y轴数据个数的解决方案)
        Axis axisY = new Axis();  //Y轴
        axisY.setName("");//y轴标注
        axisY.setTextSize(10);//设置字体大小
        data.setAxisYLeft(axisY);  //Y轴设置在左边
        //data.setAxisYRight(axisY);  //y轴设置在右边


        //设置行为属性，支持缩放、滑动以及平移
        lineChart.setInteractive(true);//设置该图表是否可交互。如不可交互，则图表不会响应缩放、滑动、选择或点击等操作。默认值为true，可交互。
        lineChart.setZoomType(ZoomType.HORIZONTAL);//设置缩放类型，可选的类型包括ZoomType.HORIZONTAL_AND_VERTICAL, ZoomType.HORIZONTAL, ZoomType.VERTICAL，默认值为HORIZONTAL_AND_VERTICAL。
        lineChart.setZoomEnabled(true);//设置是否可缩放。
        lineChart.setMaxZoom((float) 20);//设置最大缩放比例。默认值20。
        lineChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChart.setLineChartData(data);
        lineChart.setVisibility(View.VISIBLE);
        /**注：下面的7，10只是代表一个数字去类比而已
         * 当时是为了解决X轴固定数据个数。见（http://forum.xda-developers.com/tools/programming/library-hellocharts-charting-library-t2904456/page2）;
         */
        Viewport v = new Viewport(lineChart.getMaximumViewport());
        v.left = 0;
        v.right= 7;
        lineChart.setCurrentViewport(v);
    }
    private void getAxisY(){
       /* Axis axisY = new Axis().setHasLines(true);
        axisY.setMaxLabelChars(6);//max label length, for example 60
        List<AxisValue> values = new ArrayList<>();
        for(int i = 0; i < 100; i+= 10){
            AxisValue value = new AxisValue(i);
            String label = "";
            value.setLabel(label);
            values.add(value);
        }
        axisY.setValues(values);*/
    }
}
