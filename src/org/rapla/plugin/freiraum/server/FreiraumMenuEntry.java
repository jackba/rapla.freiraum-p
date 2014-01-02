package org.rapla.plugin.freiraum.server;

import org.rapla.framework.RaplaContext;
import org.rapla.servletpages.DefaultHTMLMenuEntry;

public class FreiraumMenuEntry extends DefaultHTMLMenuEntry 
{
	public FreiraumMenuEntry(RaplaContext context) {
		super(context);
	}
		
	@Override
	public String getName() {
		return "Freiraum";
	}
	@Override
	public String getLinkName() {
		return "freiraum/index.html";
	}
		
}
