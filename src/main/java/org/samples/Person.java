package org.samples;

import java.util.List;
import java.util.Map;

public class Person
{
  public void getFirstName()
  {
  }
  public void getAge(){
  }
  public void getAge(int limit){
  }

  public <T> void getAge(int limit, T other){
  }

  public <T> void getAge(int limit, T[] other){
  }

  public void getAge(int limit, List<Integer> other){
  }

  public void getAge(int limit, Map<Integer, String> other){
  }

  public <T> void getAge(String limit, T... other){
  }

  public <T> void getAge(int limit, T[][] other){
  }
}
