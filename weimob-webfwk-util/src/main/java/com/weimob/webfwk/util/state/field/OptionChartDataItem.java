package com.weimob.webfwk.util.state.field;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public class OptionChartDataItem {

    private String label;

    private final Map<String, Integer> seresData;

    public OptionChartDataItem(String label, Map<String, Integer> seresData) {
        this.label = label;
        this.seresData = seresData;
    }

    public OptionChartDataItem(String label) {
        this(label, new HashMap<>());
    }

    public OptionChartDataItem addSeresValue (String seresName, Integer seresValue) {
        this.seresData.put(seresName, seresValue);
        return this;
    }
}
