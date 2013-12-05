package org.rapla.plugin.freiraum.common;

public class ResourceDetailRow
{
	  private String label;
	  private String value;
	  public ResourceDetailRow()
	  {
		  
	  }
	  
	  public ResourceDetailRow(String label, String value)
	  {
		  this.label = label;
		  this.value = value;
	  }
	  
	  public String toString() {
		  return label + ":" + value;
	  }
	  
	  public String getLabel() 
	  {
		return label;
	  }
	  
	  public String getValue() 
	  {
		  return value;
	  }
	  
}