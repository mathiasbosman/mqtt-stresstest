package be.mathiasbosman.mqttstresstest.domain;

public record DataPointRecord(Object key, Object value) {

  @Override
  public String toString() {
    return "\"" + key + "\" :  \"" + value + "\"";
  }
}
