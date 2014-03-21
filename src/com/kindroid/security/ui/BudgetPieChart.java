/**
 * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kindroid.security.ui;

import android.content.Context;

import org.achartengine.AbstractDemoChart;
import org.achartengine.chart.AbstractChart;
import org.achartengine.chart.PieChart;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;

/**
 * Budget demo pie chart.
 */

public class BudgetPieChart extends AbstractDemoChart {
	/**
	 * Returns the chart name.
	 * 
	 * @return the chart name
	 */
	private boolean ShowLegend = false;
	private boolean ShowLabels = false;
	double []values;
	
	int[] colors;
	
	
	public void setValues(double[] values) {
		this.values = values;
	}



	public void setColors(int[] colors) {
		this.colors = colors;
	}



	public void setShowLabels(boolean showLabels) {
		ShowLabels = showLabels;
	}



	public void setShowLegend(boolean showLegend) {
		ShowLegend = showLegend;
	}

	public String getName() {
		return "Budget chart";
	}

	/**
	 * Returns the chart description.
	 * 
	 * @return the chart description
	 */
	public String getDesc() {
		return "The budget per project for this year (pie chart)";
	}

	/**
	 * Executes the chart demo.
	 * 
	 * @param context
	 *            the context
	 * @return the built intent
	 */
	public AbstractChart execute(Context context) {
		
		if(values==null||colors==null)
			return null;
		DefaultRenderer renderer = buildCategoryRenderer(colors);
		renderer.setScale(1.4f);
		renderer.setLabelsTextSize(14);
		renderer.setShowLabels(ShowLabels);
		renderer.setShowLegend(ShowLegend);
		renderer.setZoomEnabled(false);
		
		CategorySeries dataset = buildCategoryDataset("Project budget", values);
		PieChart chart = new PieChart(dataset, renderer);
		return chart;
	}

}
