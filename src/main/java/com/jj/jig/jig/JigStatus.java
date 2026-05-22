package com.jj.jig.jig;

public enum JigStatus {
    OnProcess("On Process", "在線使用中", "onprocess"),
    Normal("Normal", "正常存放", "normal"),
    Scrap("Scrap", "進行報廢", "scrap"),
    Hold("Hold", "保留", "hold"),
    Repair("Repair", "維修中", "repair"),
    Maintain("Maintain", "保養", "maintain");

    private final String label;
    private final String labelZh;
    private final String cssClass;

    JigStatus(String label, String labelZh, String cssClass) {
        this.label = label;
        this.labelZh = labelZh;
        this.cssClass = cssClass;
    }

    public String getLabel() {
        return label;
    }

    public String getLabelZh() {
        return labelZh;
    }

    public String getCssClass() {
        return cssClass;
    }
}
