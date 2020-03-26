package controller;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class ListCommand {

  @DateTimeFormat(pattern = "yyyyMMddHH")
  private Date from;
  @DateTimeFormat(pattern = "yyyyMMddHH")
  private Date to;

  public Date getFrom() {
    return from;
  }

  public void setFrom(Date from) {
    this.from = from;
  }

  public Date getTo() {
    return to;
  }

  public void setTo(Date to) {
    this.to = to;
  }

}