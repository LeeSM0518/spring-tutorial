package servey;

import java.util.List;

public class AnsweredData {

  private List<String> response;
  private Respondent res;

  public List<String> getResponse() {
    return response;
  }

  public void setResponse(List<String> response) {
    this.response = response;
  }

  public Respondent getRes() {
    return res;
  }

  public void setRes(Respondent res) {
    this.res = res;
  }

}