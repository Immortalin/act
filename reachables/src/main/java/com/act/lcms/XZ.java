package com.act.lcms;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class XZ implements Serializable {
  private static final long serialVersionUID = -5116293669998344905L;

  @JsonProperty("time")
  private Double time;

  @JsonProperty("intensity")
  private Double intensity;

  public XZ(Double t, Double i) {
    this.time = t;
    this.intensity = i;
  }

  public XZ() {}

  public Double getTime() {
        return time;
    }

  public Double getIntensity() {
        return intensity;
    }

  public void setTime(Double time) {
    this.time = time;
  }

  public void setIntensity(Double intensity) {
    this.intensity = intensity;
  }
}